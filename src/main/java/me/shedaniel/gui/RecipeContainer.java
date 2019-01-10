package me.shedaniel.gui;


import net.minecraft.class_3917;
import net.minecraft.container.Container;
import net.minecraft.entity.player.PlayerEntity;

public class RecipeContainer extends Container {
    
    public RecipeContainer() {
        // Hacky senseless id to make it stop complaining
        super(-1);
    }

    @Override
    public boolean canUse(PlayerEntity playerEntity) {
        return true;
    }

    @Override
    public class_3917<?> method_17358() {
        return null;
    }
    
}