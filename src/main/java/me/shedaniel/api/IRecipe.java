package me.shedaniel.api;

import net.minecraft.item.ItemStack;

import java.util.List;

/**
 * Created by James on 7/27/2018.
 */
public interface IRecipe<T> {

    public String getId();

    public List<T> getOutput();

    public List<List<T>> getInput();
    
    public List<List<T>> getRecipeRequiredInput();
    
}
