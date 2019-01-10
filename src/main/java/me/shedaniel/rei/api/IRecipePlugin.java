package me.shedaniel.rei.api;

import me.shedaniel.rei.listeners.IListener;

public interface IRecipePlugin extends IListener {
    
    public void registerPluginCategories();
    
    public void registerRecipes();
    
}
