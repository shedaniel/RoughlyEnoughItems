/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.plugin;

import com.google.common.collect.Lists;
import me.shedaniel.rei.api.RecipeDisplay;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class DefaultBrewingDisplay implements RecipeDisplay {
    
    private ItemStack input, output;
    private Ingredient reactant;
    
    public DefaultBrewingDisplay(ItemStack input, Ingredient reactant, ItemStack output) {
        this.input = input;
        this.reactant = reactant;
        this.output = output;
    }
    
    @Override
    public Optional<Recipe> getRecipe() {
        return Optional.empty();
    }
    
    @Override
    public List<List<ItemStack>> getInput() {
        return Lists.newArrayList(Collections.singletonList(input), Lists.newArrayList(reactant.getStackArray()));
    }
    
    @Override
    public List<ItemStack> getOutput() {
        return Collections.singletonList(output);
    }
    
    @Override
    public Identifier getRecipeCategory() {
        return DefaultPlugin.BREWING;
    }
    
    public List<ItemStack> getOutput(int slot) {
        List<ItemStack> stack = new ArrayList<>();
        for(int i = 0; i < slot * 2; i++)
            stack.add(new ItemStack(Blocks.AIR));
        for(int i = 0; i < 6 - slot * 2; i++)
            stack.addAll(getOutput());
        return stack;
    }
    
    @Override
    public List<List<ItemStack>> getRequiredItems() {
        return Collections.singletonList(Collections.singletonList(ItemStack.EMPTY));
    }
}
