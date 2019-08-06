/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.gui.widget;

import me.shedaniel.rei.api.AutoCraftingHandler;
import me.shedaniel.rei.api.RecipeDisplay;
import me.shedaniel.rei.api.RecipeHelper;
import me.shedaniel.rei.client.ScreenHelper;
import net.minecraft.client.gui.screen.ingame.AbstractContainerScreen;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.awt.*;
import java.util.Optional;
import java.util.function.Supplier;

public class AutoCraftingButtonWidget extends ButtonWidget {
    
    private final Supplier<RecipeDisplay> displaySupplier;
    private String extraTooltip;
    private AbstractContainerScreen<?> containerScreen;
    
    public AutoCraftingButtonWidget(Rectangle rectangle, String text, Supplier<RecipeDisplay> displaySupplier) {
        super(rectangle, text);
        this.displaySupplier = () -> displaySupplier.get();
        Optional<Identifier> recipe = displaySupplier.get().getRecipeLocation();
        extraTooltip = recipe.isPresent() ? I18n.translate("text.rei.recipe_id", Formatting.GRAY.toString(), recipe.get().toString()) : "";
        this.containerScreen = ScreenHelper.getLastContainerScreen();
    }
    
    @Override
    public void onPressed() {
        for (AutoCraftingHandler autoCraftingHandler : RecipeHelper.getInstance().getSortedAutoCraftingHandler())
            if (autoCraftingHandler.canHandle(displaySupplier, minecraft, minecraft.currentScreen, containerScreen, ScreenHelper.getLastOverlay()))
                try {
                    if (autoCraftingHandler.handle(displaySupplier, minecraft, minecraft.currentScreen, containerScreen, ScreenHelper.getLastOverlay()))
                        return;
                } catch (Exception e) {
                    e.printStackTrace();
                }
        minecraft.openScreen(containerScreen);
        ScreenHelper.getLastOverlay().init();
    }
    
    @Override
    public void render(int mouseX, int mouseY, float delta) {
        this.enabled = false;
        for (AutoCraftingHandler autoCraftingHandler : RecipeHelper.getInstance().getSortedAutoCraftingHandler())
            if (autoCraftingHandler.canHandle(displaySupplier, minecraft, minecraft.currentScreen, containerScreen, ScreenHelper.getLastOverlay())) {
                enabled = true;
                break;
            }
        super.render(mouseX, mouseY, delta);
    }
    
    @Override
    public Optional<String> getTooltips() {
        if (this.minecraft.options.advancedItemTooltips)
            if (enabled)
                return Optional.ofNullable(I18n.translate("text.auto_craft.move_items") + extraTooltip);
            else
                return Optional.ofNullable(I18n.translate("text.auto_craft.failed_move_items") + extraTooltip);
        if (enabled)
            return Optional.ofNullable(I18n.translate("text.auto_craft.move_items"));
        else
            return Optional.ofNullable(I18n.translate("text.auto_craft.failed_move_items"));
    }
}
