/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.gui.widget;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.shedaniel.rei.api.AutoCraftingHandler;
import me.shedaniel.rei.api.Identifier;
import me.shedaniel.rei.api.RecipeDisplay;
import me.shedaniel.rei.api.RecipeHelper;
import me.shedaniel.rei.client.ScreenHelper;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;

import java.awt.*;
import java.util.Optional;
import java.util.function.Supplier;

public class AutoCraftingButtonWidget extends ButtonWidget {
    
    private final Supplier<RecipeDisplay> displaySupplier;
    private String extraTooltip;
    private GuiContainer containerScreen;
    
    public AutoCraftingButtonWidget(Rectangle rectangle, String text, Supplier<RecipeDisplay> displaySupplier) {
        super(rectangle, text);
        this.displaySupplier = () -> displaySupplier.get();
        Optional<Identifier> recipe = displaySupplier.get().getRecipeLocation();
        extraTooltip = recipe.isPresent() ? I18n.format("text.rei.recipe_id", ChatFormatting.GRAY.toString(), recipe.get().toString()) : "";
        this.containerScreen = ScreenHelper.getLastContainerScreen();
    }
    
    @Override
    public void onPressed() {
        for(AutoCraftingHandler autoCraftingHandler : RecipeHelper.getInstance().getSortedAutoCraftingHandler())
            if (autoCraftingHandler.canHandle(displaySupplier, minecraft, minecraft.currentScreen, containerScreen, ScreenHelper.getLastOverlay()))
                if (autoCraftingHandler.handle(displaySupplier, minecraft, minecraft.currentScreen, containerScreen, ScreenHelper.getLastOverlay()))
                    return;
        minecraft.displayGuiScreen(containerScreen);
        ScreenHelper.getLastOverlay().init();
    }
    
    @Override
    public void render(int mouseX, int mouseY, float delta) {
        this.enabled = false;
        for(AutoCraftingHandler autoCraftingHandler : RecipeHelper.getInstance().getSortedAutoCraftingHandler())
            if (autoCraftingHandler.canHandle(displaySupplier, minecraft, minecraft.currentScreen, containerScreen, ScreenHelper.getLastOverlay())) {
                enabled = true;
                break;
            }
        super.render(mouseX, mouseY, delta);
    }
    
    @Override
    public Optional<String> getTooltips() {
        if (this.minecraft.gameSettings.advancedItemTooltips)
            if (enabled)
                return Optional.ofNullable(I18n.format("text.auto_craft.move_items") + extraTooltip);
            else
                return Optional.ofNullable(I18n.format("text.auto_craft.failed_move_items") + extraTooltip);
        if (enabled)
            return Optional.ofNullable(I18n.format("text.auto_craft.move_items"));
        else
            return Optional.ofNullable(I18n.format("text.auto_craft.failed_move_items"));
    }
}
