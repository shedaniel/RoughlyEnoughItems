package me.shedaniel.rei.api;

import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public interface IRecipeCategory<T extends IRecipeDisplay> {
    
    public Identifier getIdentifier();
    
    public ItemStack getCategoryIcon();
    
    public String getCategoryName();
    
    default public boolean usesFullPage() {
        return false;
    }
    
}
