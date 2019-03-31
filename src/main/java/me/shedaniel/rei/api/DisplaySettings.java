package me.shedaniel.rei.api;

public interface DisplaySettings<T extends RecipeDisplay> {
    
    int getDisplayHeight(RecipeCategory category);
    
    int getDisplayWidth(RecipeCategory category, T display);
    
    int getMaximumRecipePerPage(RecipeCategory category);
    
    default int getFixedRecipesPerPage() {
        return -1;
    }
    
}
