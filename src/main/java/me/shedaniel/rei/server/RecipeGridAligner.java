/*
 * Copyright (c) 2018, 2019, 2020 shedaniel
 * Licensed under the MIT License (the "License").
 */

package me.shedaniel.rei.server;

import net.minecraft.recipe.Ingredient;
import net.minecraft.util.DefaultedList;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.ApiStatus;

import java.util.Iterator;

@ApiStatus.Internal
public interface RecipeGridAligner<T> {
    default void alignRecipeToGrid(int int_1, int int_2, int int_3, DefaultedList<Ingredient> recipe_1, Iterator<T> iterator_1, int int_4) {
        int int_7 = 0;
        
        for (int int_8 = 0; int_8 < int_2; ++int_8) {
            if (int_7 == int_3) {
                ++int_7;
            }
            
            boolean boolean_1 = (float) int_2 < (float) int_2 / 2.0F;
            int int_9 = MathHelper.floor((float) int_2 / 2.0F - (float) int_2 / 2.0F);
            if (boolean_1 && int_9 > int_8) {
                int_7 += int_1;
                ++int_8;
            }
            
            for (int int_10 = 0; int_10 < int_1; ++int_10) {
                if (!iterator_1.hasNext()) {
                    return;
                }
                
                boolean_1 = (float) int_1 < (float) int_1 / 2.0F;
                int_9 = MathHelper.floor((float) int_1 / 2.0F - (float) int_1 / 2.0F);
                int int_11 = int_1;
                boolean boolean_2 = int_10 < int_1;
                if (boolean_1) {
                    int_11 = int_9 + int_1;
                    boolean_2 = int_9 <= int_10 && int_10 < int_9 + int_1;
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