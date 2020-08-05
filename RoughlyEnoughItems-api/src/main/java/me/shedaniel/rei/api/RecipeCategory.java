/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020 shedaniel
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package me.shedaniel.rei.api;

import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.widgets.Widgets;
import me.shedaniel.rei.gui.entries.RecipeEntry;
import me.shedaniel.rei.gui.entries.SimpleRecipeEntry;
import me.shedaniel.rei.gui.widget.Widget;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;

import java.util.Collections;
import java.util.List;

@Environment(EnvType.CLIENT)
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
    @ApiStatus.OverrideOnly
    default RecipeEntry getSimpleRenderer(T recipe) {
        return SimpleRecipeEntry.from(recipe::getInputEntries, recipe::getResultingEntries);
    }
    
    /**
     * Setup the widgets for displaying the recipe
     *
     * @param recipeDisplay the recipe
     * @param bounds        the bounds of the display, configurable with overriding the width, height methods.
     * @return the list of widgets
     */
    @ApiStatus.OverrideOnly
    default List<Widget> setupDisplay(T recipeDisplay, Rectangle bounds) {
        return Collections.singletonList(Widgets.createCategoryBase(bounds));
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
    
}
