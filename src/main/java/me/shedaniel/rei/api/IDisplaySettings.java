package me.shedaniel.rei.api;

public interface IDisplaySettings<T extends IRecipeDisplay> {
    
    public int getDisplayHeight(IRecipeCategory category);
    
    public int getDisplayWidth(IRecipeCategory category, T display);
    
    public int getMaximumRecipePerPage(IRecipeCategory category);
    
}
