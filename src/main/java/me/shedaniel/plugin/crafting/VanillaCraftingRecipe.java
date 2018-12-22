package me.shedaniel.plugin.crafting;

import me.shedaniel.api.IRecipe;
import net.minecraft.item.ItemStack;

public abstract class VanillaCraftingRecipe implements IRecipe<ItemStack> {
    
    public int getWidth() {
        return 2;
    }
    
    public int getHeight() {
        return 2;
    }
    
}
