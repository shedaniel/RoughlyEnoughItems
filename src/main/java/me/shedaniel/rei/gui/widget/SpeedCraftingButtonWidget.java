/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.gui.widget;

import me.shedaniel.rei.api.RecipeDisplay;
import me.shedaniel.rei.api.SpeedCraftFunctional;
import me.shedaniel.rei.client.ScreenHelper;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.recipe.Recipe;
import net.minecraft.util.Formatting;

import java.awt.*;
import java.util.Optional;
import java.util.function.Supplier;

public class SpeedCraftingButtonWidget extends ButtonWidget {
    
    private final Supplier<RecipeDisplay> displaySupplier;
    private final SpeedCraftFunctional functional;
    private String extraTooltip;
    
    public SpeedCraftingButtonWidget(Rectangle rectangle, String text, SpeedCraftFunctional functional, Supplier<RecipeDisplay> displaySupplier) {
        super(rectangle, text);
        this.displaySupplier = displaySupplier;
        this.functional = functional;
        Optional<Recipe> recipe = displaySupplier.get().getRecipe();
        extraTooltip = recipe.isPresent() ? I18n.translate("text.rei.recipe_id", Formatting.GRAY.toString(), recipe.get().getId().toString()) : "";
    }
    
    @Override
    public void onPressed() {
        minecraft.openScreen(ScreenHelper.getLastContainerScreen());
        ScreenHelper.getLastOverlay().init();
        functional.performAutoCraft(ScreenHelper.getLastContainerScreen(), displaySupplier.get());
    }
    
    @Override
    public void render(int mouseX, int mouseY, float delta) {
        this.enabled = functional != null && functional.acceptRecipe(ScreenHelper.getLastContainerScreen(), displaySupplier.get());
        super.render(mouseX, mouseY, delta);
    }
    
    @Override
    public Optional<String> getTooltips() {
        if (this.minecraft.options.advancedItemTooltips)
            if (enabled)
                return Optional.ofNullable(I18n.translate("text.speed_craft.move_items") + extraTooltip);
            else
                return Optional.ofNullable(I18n.translate("text.speed_craft.failed_move_items") + extraTooltip);
        if (enabled)
            return Optional.ofNullable(I18n.translate("text.speed_craft.move_items"));
        else
            return Optional.ofNullable(I18n.translate("text.speed_craft.failed_move_items"));
    }
}
