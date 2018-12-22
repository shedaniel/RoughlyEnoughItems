package me.shedaniel.api;

import net.minecraft.item.ItemStack;

import java.util.List;
import java.util.Map;

/**
 * Created by James on 8/5/2018.
 */
public interface IRecipeManager {
    
    public void addRecipe(String id, IRecipe recipe);
    
    public void addRecipe(String id, List<? extends IRecipe> recipes);
    
    public void addDisplayAdapter(IDisplayCategory adapter);
    
    public Map<IDisplayCategory, List<IRecipe>> getRecipesFor(ItemStack stack);
}
