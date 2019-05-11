package me.shedaniel.rei.api;

public interface DisplaySettings<T extends RecipeDisplay> {
    
    /**
     * Gets the recipe display height
     *
     * @param category the category of the display
     * @return the height
     */
    int getDisplayHeight(RecipeCategory category);
    
    /**
     * Gets the recipe display width
     *
     * @param category the category of the display
     * @param display  the display of the recipe
     * @return the width
     */
    int getDisplayWidth(RecipeCategory category, T display);
    
    /**
     * Gets the maximum amount of recipe displays of the category displayed at the same time.
     * @param category the category of the displays
     * @return the maximum amount
     */
    int getMaximumRecipePerPage(RecipeCategory category);
    
    /**
     * Gets the fixed amount of recipes per page.
     * @return the amount of recipes, returns -1 if not fixed
     */
    default int getFixedRecipesPerPage() {
        return -1;
    }
    
}
