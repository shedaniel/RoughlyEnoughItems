/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.plugin.smelting;

import me.shedaniel.rei.plugin.DefaultPlugin;
import me.shedaniel.rei.plugin.cooking.DefaultCookingDisplay;
import net.minecraft.recipe.SmeltingRecipe;
import net.minecraft.util.Identifier;

public class DefaultSmeltingDisplay extends DefaultCookingDisplay {
    
    public DefaultSmeltingDisplay(SmeltingRecipe recipe) {
        super(recipe);
    }
    
    @Override
    public Identifier getRecipeCategory() {
        return DefaultPlugin.SMELTING;
    }
    
}
