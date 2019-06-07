/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.api;

import me.shedaniel.rei.client.ScreenHelper;
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
    
    /**
     * Gets the identifier of the category, must be unique
     *
     * @return the unique identifier of the category
     */
    Identifier getIdentifier();
    
    /**
     * Gets the stack to render for the icon
     *
     * @return the stack to render
     * @deprecated use {@link RecipeCategory#getIcon()} instead
     */
    @Deprecated
    default ItemStack getCategoryIcon() {
        return ItemStack.EMPTY;
    }
    
    /**
     * Gets the renderer of the icon, allowing developers to render things other than items
     *
     * @return the renderer of the icon
     */
    @SuppressWarnings("deprecation")
    default Renderer getIcon() {
        return Renderable.fromItemStackSupplier(this::getCategoryIcon);
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
    default RecipeRenderer getSimpleRenderer(T recipe) {
        return Renderable.fromRecipe(recipe::getInput, recipe::getOutput);
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
        new CategoryBaseWidget(bounds).render();
        if (ScreenHelper.isDarkModeEnabled()) {
            DrawableHelper.fill(bounds.x + 17, bounds.y + 5, bounds.x + bounds.width - 17, bounds.y + 17, 0xFF404040);
            DrawableHelper.fill(bounds.x + 17, bounds.y + 21, bounds.x + bounds.width - 17, bounds.y + 33, 0xFF404040);
        } else {
            DrawableHelper.fill(bounds.x + 17, bounds.y + 5, bounds.x + bounds.width - 17, bounds.y + 17, 0xFF9E9E9E);
            DrawableHelper.fill(bounds.x + 17, bounds.y + 21, bounds.x + bounds.width - 17, bounds.y + 33, 0xFF9E9E9E);
        }
    }
    
    /**
     * Gets the display settings for the category, used for getting the bounds for the display
     *
     * @return the display settings
     */
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
    
    /**
     * Gets the recipe display height
     * Please do not override this, use {@link RecipeCategory#getDisplaySettings()} instead
     *
     * @return the recipe display height
     */
    default int getDisplayHeight() {
        return RecipeHelper.getInstance().getCachedCategorySettings(getIdentifier()).map(settings -> settings.getDisplayHeight(this)).orElse(0);
    }
    
    /**
     * Gets the recipe display width
     * Please do not override this, use {@link RecipeCategory#getDisplaySettings()} instead
     *
     * @param display the recipe display
     * @return the recipe display width
     */
    default int getDisplayWidth(T display) {
        return RecipeHelper.getInstance().getCachedCategorySettings(getIdentifier()).map(settings -> settings.getDisplayWidth(this, display)).orElse(0);
    }
    
    /**
     * Gets the maximum recipe per page.
     * Please do not override this, use {@link RecipeCategory#getDisplaySettings()} instead
     *
     * @return the maximum amount of recipes for page
     */
    default int getMaximumRecipePerPage() {
        return RecipeHelper.getInstance().getCachedCategorySettings(getIdentifier()).map(settings -> settings.getMaximumRecipePerPage(this)).orElse(0);
    }
    
    /**
     * Gets whether the category will check tags, useful for potions
     *
     * @return whether the category will check tags
     */
    default boolean checkTags() {
        return false;
    }
    
}
