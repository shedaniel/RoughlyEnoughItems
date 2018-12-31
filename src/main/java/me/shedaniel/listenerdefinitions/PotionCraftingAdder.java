package me.shedaniel.listenerdefinitions;

import net.minecraft.item.Item;
import net.minecraft.potion.Potion;

public interface PotionCraftingAdder extends IEvent {
    public void addPotionRecipe(Potion inputType, Item reagent, Potion outputType);
}
