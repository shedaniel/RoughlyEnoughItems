/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.gui.widget;

import me.shedaniel.rei.api.RecipeDisplay;
import me.shedaniel.rei.client.ScreenHelper;
import net.minecraft.ChatFormat;
import net.minecraft.client.gui.screen.ingame.AbstractContainerScreen;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.util.Identifier;

import java.awt.*;
import java.util.Optional;
import java.util.function.Supplier;

public class AutoCraftingButtonWidget extends ButtonWidget {
    
    private final Supplier<RecipeDisplay<?>> displaySupplier;
    private String extraTooltip;
    private AbstractContainerScreen<?> containerScreen;
    
    public AutoCraftingButtonWidget(Rectangle rectangle, String text, Supplier<RecipeDisplay> displaySupplier) {
        super(rectangle, text);
        this.displaySupplier = () -> displaySupplier.get();
        Optional<Identifier> recipe = displaySupplier.get().getRecipeLocation();
        extraTooltip = recipe.isPresent() ? I18n.translate("text.rei.recipe_id", ChatFormat.GRAY.toString(), recipe.get().toString()) : "";
        this.containerScreen = ScreenHelper.getLastContainerScreen();
    }
    
    @Override
    public void onPressed() {
        minecraft.openScreen(containerScreen);
        ScreenHelper.getLastOverlay().init();
    }
    
    @Override
    public void render(int mouseX, int mouseY, float delta) {
        this.enabled = true;
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
