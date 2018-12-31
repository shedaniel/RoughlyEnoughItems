package me.shedaniel.plugin;

import me.shedaniel.api.IRecipe;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class RandomRecipe implements IRecipe<ItemStack> {
    
    private String id;
    
    public RandomRecipe(String id) {
        this.id = id;
    }
    
    @Override
    public String getId() {
        return id;
    }
    
    @Override
    public List<ItemStack> getOutput() {
        return new LinkedList<>(Arrays.asList(new ItemStack[]{new ItemStack(Blocks.BEETROOTS.getItem())}));
    }
    
    @Override
    public List<List<ItemStack>> getInput() {
        return new LinkedList<>(Arrays.asList(new LinkedList<>(Arrays.asList(new ItemStack[]{new ItemStack(Blocks.OAK_LOG.getItem())}))));
    }
}
