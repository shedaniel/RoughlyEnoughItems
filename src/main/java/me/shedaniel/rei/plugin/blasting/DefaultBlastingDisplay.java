/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.plugin.blasting;

import me.shedaniel.rei.plugin.DefaultPlugin;
import me.shedaniel.rei.plugin.cooking.DefaultCookingDisplay;
import net.minecraft.recipe.BlastingRecipe;
import net.minecraft.util.Identifier;

public class DefaultBlastingDisplay extends DefaultCookingDisplay {
    
    public DefaultBlastingDisplay(BlastingRecipe recipe) {
        super(recipe);
    }
    
    @Override
    public Identifier getRecipeCategory() {
        return DefaultPlugin.BLASTING;
    }
    
}
