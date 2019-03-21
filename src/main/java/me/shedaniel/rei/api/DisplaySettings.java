package me.shedaniel.rei.api;

public interface DisplaySettings<T extends RecipeDisplay> {
    
    int getDisplayHeight(RecipeCategory category);
    
    int getDisplayWidth(RecipeCategory category, T display);
    
    int getMaximumRecipePerPage(RecipeCategory category);
    
    default int getFixedRecipesPerPage() {
        return -1;
    }
    
    default VisableType canDisplay(T display) {
        return VisableType.ALWAYS;
    }
    
    public static enum VisableType {
        ALWAYS, PASS, NEVER;
    }
    
}
