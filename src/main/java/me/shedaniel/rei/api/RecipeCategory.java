/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.api;

import me.shedaniel.rei.gui.RecipeViewingScreen;
import me.shedaniel.rei.gui.renderables.RecipeRenderer;
import me.shedaniel.rei.gui.widget.CategoryBaseWidget;
import me.shedaniel.rei.gui.widget.RecipeBaseWidget;
import me.shedaniel.rei.gui.widget.Widget;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

import java.awt.*;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;


public interface RecipeCategory<T extends RecipeDisplay> {
    
    Identifier getIdentifier();
    
    ItemStack getCategoryIcon();
    
    default Renderer getIcon() {
        return Renderable.fromItemStackSupplier(this::getCategoryIcon);
    }
    
    String getCategoryName();
    
    default RecipeRenderer getSimpleRenderer(T recipe) {
        return Renderable.fromRecipe(recipe::getInput, recipe::getOutput);
    }
    
    default List<Widget> setupDisplay(Supplier<T> recipeDisplaySupplier, Rectangle bounds) {
        return Collections.singletonList(new RecipeBaseWidget(bounds));
    }
    
    default void drawCategoryBackground(Rectangle bounds, int mouseX, int mouseY, float delta) {
        new CategoryBaseWidget(bounds).render();
        DrawableHelper.fill(bounds.x + 17, bounds.y + 5, bounds.x + bounds.width - 17, bounds.y + 17, RecipeViewingScreen.SUB_COLOR.getRGB());
        DrawableHelper.fill(bounds.x + 17, bounds.y + 21, bounds.x + bounds.width - 17, bounds.y + 33, RecipeViewingScreen.SUB_COLOR.getRGB());
    }
    
    default DisplaySettings getDisplaySettings() {
        return new DisplaySettings<T>() {
            @Override
            public int getDisplayHeight(RecipeCategory category) {
                return 66;
            }
            
            @Override
            public int getDisplayWidth(RecipeCategory category, T display) {
                return 150;
            }
            
            @Override
            public int getMaximumRecipePerPage(RecipeCategory category) {
                return 99;
            }
        };
    }
    
    default int getDisplayHeight() {
        return RecipeHelper.getInstance().getCachedCategorySettings(getIdentifier()).map(settings -> settings.getDisplayHeight(this)).orElse(0);
    }
    
    default int getDisplayWidth(T display) {
        return RecipeHelper.getInstance().getCachedCategorySettings(getIdentifier()).map(settings -> settings.getDisplayWidth(this, display)).orElse(0);
    }
    
    default int getMaximumRecipePerPage() {
        return RecipeHelper.getInstance().getCachedCategorySettings(getIdentifier()).map(settings -> settings.getMaximumRecipePerPage(this)).orElse(0);
    }
    
    default boolean checkTags() {
        return false;
    }
    
}
