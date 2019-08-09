package me.shedaniel.rei.server;

import net.minecraft.recipe.Ingredient;
import net.minecraft.util.DefaultedList;
import net.minecraft.util.math.MathHelper;

import java.util.Iterator;

public interface RecipeGridAligner<T> {
    default void alignRecipeToGrid(int int_1, int int_2, int int_3, DefaultedList<Ingredient> recipe_1, Iterator<T> iterator_1, int int_4) {
        int int_5 = int_1;
        int int_6 = int_2;
        //      if (recipe_1 instanceof ShapedRecipe) {
        //         ShapedRecipe shapedRecipe_1 = (ShapedRecipe)recipe_1;
        //         int_5 = shapedRecipe_1.getWidth();
        //         int_6 = shapedRecipe_1.getHeight();
        //      }
        
        int int_7 = 0;
        
        for (int int_8 = 0; int_8 < int_2; ++int_8) {
            if (int_7 == int_3) {
                ++int_7;
            }
            
            boolean boolean_1 = (float) int_6 < (float) int_2 / 2.0F;
            int int_9 = MathHelper.floor((float) int_2 / 2.0F - (float) int_6 / 2.0F);
            if (boolean_1 && int_9 > int_8) {
                int_7 += int_1;
                ++int_8;
            }
            
            for (int int_10 = 0; int_10 < int_1; ++int_10) {
                if (!iterator_1.hasNext()) {
                    return;
                }
                
                boolean_1 = (float) int_5 < (float) int_1 / 2.0F;
                int_9 = MathHelper.floor((float) int_1 / 2.0F - (float) int_5 / 2.0F);
                int int_11 = int_5;
                boolean boolean_2 = int_10 < int_5;
                if (boolean_1) {
                    int_11 = int_9 + int_5;
                    boolean_2 = int_9 <= int_10 && int_10 < int_9 + int_5;
                }
                
                if (boolean_2) {
                    this.acceptAlignedInput(iterator_1, int_7, int_4, int_8, int_10);
                } else if (int_11 == int_10) {
                    int_7 += int_1 - int_10;
                    break;
                }
                
                ++int_7;
            }
        }
        
    }
    
    void acceptAlignedInput(Iterator<T> var1, int var2, int var3, int var4, int var5);
}