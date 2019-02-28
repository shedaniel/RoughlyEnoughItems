package me.shedaniel.rei.api;

public interface IDisplaySettings<T extends IRecipeDisplay> {
    
    int getDisplayHeight(IRecipeCategory category);
    
    int getDisplayWidth(IRecipeCategory category, T display);
    
    int getMaximumRecipePerPage(IRecipeCategory category);
    
}
