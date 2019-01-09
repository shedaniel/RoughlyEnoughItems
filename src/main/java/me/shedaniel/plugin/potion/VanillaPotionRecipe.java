package me.shedaniel.plugin.potion;

import me.shedaniel.api.IRecipe;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class VanillaPotionRecipe implements IRecipe<ItemStack> {
    
    private ItemStack[] input, reactWith, output;
    
    @Override
    public String getId() {
        return "potion";
    }
    
    public VanillaPotionRecipe(ItemStack[] input, ItemStack[] reactWith, ItemStack[] output) {
        this.input = input;
        this.reactWith = reactWith;
        this.output = output;
    }
    
    @Override
    public List<ItemStack> getOutput() {
        return Arrays.asList(output);
    }
    
    @Override
    public List<List<ItemStack>> getInput() {
        List<List<ItemStack>> input = new LinkedList<>();
        input.add(new ArrayList<>(Arrays.asList(this.input)));
        input.add(new ArrayList<>(Arrays.asList(this.reactWith)));
        return input;
    }
    
    @Override
    public List<List<ItemStack>> getRecipeRequiredInput() {
        List<List<ItemStack>> input = new LinkedList<>();
        input.add(new ArrayList<>(Arrays.asList(this.input)));
        input.add(new ArrayList<>(Arrays.asList(this.reactWith)));
        return input;
    }
    
    public List<ItemStack> getOutput(int slot) {
        List<ItemStack> stack = new ArrayList<>();
        for(int i = 0; i < slot * 2; i++)
            stack.add(new ItemStack(Blocks.AIR));
        for(int i = 0; i < 6 - slot * 2; i++)
            stack.addAll(getOutput());
        return stack;
    }
    
}
