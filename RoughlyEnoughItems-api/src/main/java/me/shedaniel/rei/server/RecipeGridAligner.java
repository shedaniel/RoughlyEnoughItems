/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020 shedaniel
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package me.shedaniel.rei.server;

import net.minecraft.recipe.Ingredient;
import net.minecraft.util.collection.DefaultedList;
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