/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.plugin.brewing;

import net.minecraft.item.Item;
import net.minecraft.item.crafting.Ingredient;

public class BrewingRecipe {
    
    public final Item input;
    public final Ingredient ingredient;
    public final Item output;
    
    public BrewingRecipe(Item object_1, Ingredient ingredient_1, Item object_2) {
        this.input = object_1;
        this.ingredient = ingredient_1;
        this.output = object_2;
    }
    
}