package me.shedaniel.gui;


import net.minecraft.container.Container;
import net.minecraft.entity.player.PlayerEntity;

public class RecipeContainer extends Container {
    
    @Override
    public boolean canUse(PlayerEntity playerEntity) {
        return true;
    }
    
}
