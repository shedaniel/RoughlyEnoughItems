/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.api;

import me.shedaniel.math.api.Rectangle;
import me.shedaniel.rei.gui.RecipeViewingScreen;
import me.shedaniel.rei.gui.entries.RecipeEntry;
import me.shedaniel.rei.gui.entries.SimpleRecipeEntry;
import me.shedaniel.rei.gui.widget.PanelWidget;
import me.shedaniel.rei.gui.widget.RecipeBaseWidget;
import me.shedaniel.rei.gui.widget.Widget;
import me.shedaniel.rei.impl.ScreenHelper;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.util.Identifier;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;


public interface RecipeCategory<T extends RecipeDisplay> {
    
    /**
     * Gets the identifier of the category, must be unique
     *
     * @return the unique identifier of the category
     */
    Identifier getIdentifier();
    
    /**
     * Gets the renderer of the icon, allowing developers to render things other than items
     *
     * @return the renderer of the icon
     */
    default EntryStack getLogo() {
        return EntryStack.empty();
    }
    
    /**
     * Gets the category name
     *
     * @return the name
     */
    String getCategoryName();
    
    /**
     * Gets the recipe renderer for the category, used in {@link me.shedaniel.rei.gui.VillagerRecipeViewingScreen} for rendering simple recipes
     *
     * @param recipe the recipe to render
     * @return the recipe renderer
     */
    @SuppressWarnings("unchecked")
    default RecipeEntry getSimpleRenderer(T recipe) {
        return SimpleRecipeEntry.create(recipe::getInputEntries, recipe::getOutputEntries);
    }
    
    /**
     * Setup the widgets for displaying the recipe
     *
     * @param recipeDisplaySupplier the supplier for getting the recipe
     * @param bounds                the bounds of the display, configurable with overriding {@link RecipeCategory#getDisplaySettings()}
     * @return the list of widgets
     */
    default List<Widget> setupDisplay(Supplier<T> recipeDisplaySupplier, Rectangle bounds) {
        return Collections.singletonList(new RecipeBaseWidget(bounds));
    }
    
    /**
     * Draws the category background, used in {@link RecipeViewingScreen}
     *
     * @param bounds the bounds of the whole recipe viewing screen
     * @param mouseX the x coordinates for the mouse
     * @param mouseY the y coordinates for the mouse
     * @param delta  the delta
     */
    default void drawCategoryBackground(Rectangle bounds, int mouseX, int mouseY, float delta) {
        PanelWidget.render(bounds, -1);
        if (ScreenHelper.isDarkModeEnabled()) {
            DrawableHelper.fill(bounds.x + 17, bounds.y + 5, bounds.x + bounds.width - 17, bounds.y + 17, 0xFF404040);
            DrawableHelper.fill(bounds.x + 17, bounds.y + 19, bounds.x + bounds.width - 17, bounds.y + 31, 0xFF404040);
        } else {
            DrawableHelper.fill(bounds.x + 17, bounds.y + 5, bounds.x + bounds.width - 17, bounds.y + 17, 0xFF9E9E9E);
            DrawableHelper.fill(bounds.x + 17, bounds.y + 19, bounds.x + bounds.width - 17, bounds.y + 31, 0xFF9E9E9E);
        }
    }
    
    /**
     * Gets the recipe display height
     *
     * @return the recipe display height
     */
    default int getDisplayHeight() {
        return 66;
    }
    
    /**
     * Gets the recipe display width
     *
     * @param display the recipe display
     * @return the recipe display width
     */
    default int getDisplayWidth(T display) {
        return 150;
    }
    
    /**
     * Gets the maximum recipe per page.
     *
     * @return the maximum amount of recipes for page
     */
    default int getMaximumRecipePerPage() {
        return 99;
    }
    
    /**
     * Gets the fixed amount of recipes per page.
     *
     * @return the amount of recipes, returns -1 if not fixed
     */
    default int getFixedRecipesPerPage() {
        return -1;
    }
    
    /**
     * Gets whether the category will check tags, useful for potions
     *
     * @return whether the category will check tags
     * @deprecated no longer used
     */
    @Deprecated
    default boolean checkTags() {
        return false;
    }
    
}
