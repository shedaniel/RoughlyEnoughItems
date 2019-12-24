/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.plugin.smoking;

import me.shedaniel.rei.plugin.DefaultPlugin;
import me.shedaniel.rei.plugin.cooking.DefaultCookingDisplay;
import net.minecraft.recipe.SmokingRecipe;
import net.minecraft.util.Identifier;

public class DefaultSmokingDisplay extends DefaultCookingDisplay {

    public DefaultSmokingDisplay(SmokingRecipe recipe) {
        super(recipe);
    }

    @Override
    public Identifier getRecipeCategory() {
        return DefaultPlugin.SMOKING;
    }
}
