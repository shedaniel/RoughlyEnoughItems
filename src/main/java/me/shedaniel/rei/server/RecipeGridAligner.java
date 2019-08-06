package me.shedaniel.rei.server;

import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.util.math.MathHelper;

import java.util.Iterator;

public interface RecipeGridAligner<T> {
    default void alignRecipeToGrid(int craftingGridWidth, int craftingGridHeight, int resultSlotIndex, Recipe<?> recipe, Iterator<T> iterator_1, int int_4) {
        int recipeWidth = craftingGridWidth;
        int recipeHeight = craftingGridHeight;
        if (recipe instanceof ShapedRecipe) {
            ShapedRecipe shapedRecipe = (ShapedRecipe) recipe;
            recipeWidth = shapedRecipe.getWidth();
            recipeHeight = shapedRecipe.getHeight();
        }
        
        int slotId = 0;
        
        for (int int_8 = 0; int_8 < craftingGridHeight; ++int_8) {
            if (slotId == resultSlotIndex) {
                ++slotId;
            }
            
            boolean boolean_1 = (float) recipeHeight < (float) craftingGridHeight / 2.0F;
            int int_9 = MathHelper.floor((float) craftingGridHeight / 2.0F - (float) recipeHeight / 2.0F);
            if (boolean_1 && int_9 > int_8) {
                slotId += craftingGridWidth;
                ++int_8;
            }
            
            for (int int_10 = 0; int_10 < craftingGridWidth; ++int_10) {
                if (!iterator_1.hasNext()) {
                    return;
                }
                
                boolean_1 = (float) recipeWidth < (float) craftingGridWidth / 2.0F;
                int_9 = MathHelper.floor((float) craftingGridWidth / 2.0F - (float) recipeWidth / 2.0F);
                int int_11 = recipeWidth;
                boolean boolean_2 = int_10 < recipeWidth;
                if (boolean_1) {
                    int_11 = int_9 + recipeWidth;
                    boolean_2 = int_9 <= int_10 && int_10 < int_9 + recipeWidth;
                }
                
                if (boolean_2) {
                    this.acceptAlignedInput(iterator_1, slotId, int_4, int_8, int_10);
                } else if (int_11 == int_10) {
                    slotId += craftingGridWidth - int_10;
                    break;
                }
                
                ++slotId;
            }
        }
        
    }
    
    void acceptAlignedInput(Iterator<T> var1, int var2, int var3, int var4, int var5);
}