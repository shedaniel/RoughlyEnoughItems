package me.shedaniel.rei.api;

public interface IRecipePlugin {
    
    public void registerPluginCategories();
    
    public void registerRecipes();
    
    public void registerSpeedCraft();
    
    default public int getPriority() {
        return 0;
    }
    
}
