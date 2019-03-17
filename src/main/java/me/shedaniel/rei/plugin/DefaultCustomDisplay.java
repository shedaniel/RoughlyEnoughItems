package me.shedaniel.rei.plugin;

import com.google.common.collect.Lists;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class DefaultCustomDisplay implements DefaultCraftingDisplay {
    
    private List<List<ItemStack>> input;
    private List<ItemStack> output;
    private IRecipe possibleRecipe;
    private int width, height;
    
    public DefaultCustomDisplay(List<List<ItemStack>> input, List<ItemStack> output, IRecipe possibleRecipe) {
        this.input = input;
        this.output = output;
        this.possibleRecipe = possibleRecipe;
        List<Boolean> row = Lists.newArrayList(false, false, false);
        List<Boolean> column = Lists.newArrayList(false, false, false);
        for(int i = 0; i < 9; i++)
            if (i < input.size()) {
                List<ItemStack> stacks = input.get(i);
                if (stacks.stream().filter(stack -> !stack.isEmpty()).count() > 0) {
                    row.set((i - (i % 3)) / 3, true);
                    column.set(i % 3, true);
                }
            }
        width = (int) column.stream().filter(b -> b).count();
        height = (int) row.stream().filter(b -> b).count();
    }
    
    public DefaultCustomDisplay(List<List<ItemStack>> input, List<ItemStack> output) {
        this(input, output, null);
    }
    
    @Override
    public Optional<IRecipe> getRecipe() {
        return Optional.ofNullable(possibleRecipe);
    }
    
    @Override
    public List<List<ItemStack>> getInput() {
        return Collections.unmodifiableList(input);
    }
    
    @Override
    public List<ItemStack> getOutput() {
        return Collections.unmodifiableList(output);
    }
    
    @Override
    public List<List<ItemStack>> getRequiredItems() {
        return Collections.unmodifiableList(input);
    }
    
    @Override
    public int getWidth() {
        return width;
    }
    
    @Override
    public int getHeight() {
        return height;
    }
    
}
