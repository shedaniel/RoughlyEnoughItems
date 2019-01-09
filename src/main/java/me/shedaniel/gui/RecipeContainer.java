package me.shedaniel.gui;


import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;

public class RecipeContainer extends Container {
    
    @Override
    public boolean canInteractWith(EntityPlayer entityPlayer) {
        return true;
    }
    
}
