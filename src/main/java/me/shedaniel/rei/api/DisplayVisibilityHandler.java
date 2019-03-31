package me.shedaniel.rei.api;

public interface DisplayVisibilityHandler {
    
    default float getPriority() {
        return 0f;
    }
    
    DisplayVisibility handleDisplay(RecipeCategory category, RecipeDisplay display);
    
}
