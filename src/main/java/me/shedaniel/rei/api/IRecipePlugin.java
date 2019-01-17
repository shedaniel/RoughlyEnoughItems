package me.shedaniel.rei.api;

import me.shedaniel.rei.listeners.IListener;

public interface IRecipePlugin {
    
    public void registerPluginCategories();
    
    public void registerRecipes();
    
    public void registerSpeedCraft();
    
    default public int getPriority() {
        return 0;
    }
    
}
