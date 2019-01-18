package me.shedaniel.rei.plugin;

import me.shedaniel.rei.api.IRecipeDisplay;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DefaultBrewingDisplay implements IRecipeDisplay {
    
    private ItemStack input, output;
    private Ingredient reactant;
    
    public DefaultBrewingDisplay(ItemStack input, Ingredient reactant, ItemStack output) {
        this.input = input;
        this.reactant = reactant;
        this.output = output;
    }
    
    @Override
    public IRecipe getRecipe() {
        return null;
    }
    
    @Override
    public List<List<ItemStack>> getInput() {
        return Arrays.asList(Arrays.asList(input), Arrays.asList(reactant.getMatchingStacks()));
    }
    
    @Override
    public List<ItemStack> getOutput() {
        return Arrays.asList(output);
    }
    
    @Override
    public ResourceLocation getRecipeCategory() {
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
        return Arrays.asList(Arrays.asList(new ItemStack(Items.AIR)));
    }
}
