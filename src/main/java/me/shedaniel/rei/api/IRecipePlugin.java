package me.shedaniel.rei.api;

public interface IRecipePlugin {
    
    default public void onFirstLoad() {}
    
    public void registerPluginCategories();
    
    public void registerRecipes();
    
    public void registerSpeedCraft();
    
    default public int getPriority() {
        return 0;
    }
    
}
