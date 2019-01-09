package me.shedaniel.listenerdefinitions;

import net.minecraft.item.Item;
import net.minecraft.potion.PotionType;

public interface PotionCraftingAdder {
    
    public void addPotionRecipe(PotionType inputType, Item reagent, PotionType outputType);
    
}
