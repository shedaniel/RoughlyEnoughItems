/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020, 2021, 2022, 2023 shedaniel
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

package me.shedaniel.rei.api.common.transfer;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.ints.*;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

public class RecipeFinder {
    public final Int2IntMap idToAmountMap = new Int2IntOpenHashMap();
    
    public static int getItemId(ItemStack stack) {
        return StackedContents.getStackingIndex(stack);
    }
    
    public static ItemStack getStackFromId(int itemId) {
        return StackedContents.fromStackingIndex(itemId);
    }
    
    public void addNormalItem(ItemStack stack) {
        if (!stack.isDamaged() && !stack.isEnchanted() && !stack.has(DataComponents.CUSTOM_NAME)) {
            this.addItem(stack);
        }
    }
    
    public void addItem(ItemStack stack) {
        this.addItem(stack, 64);
    }
    
    public void addItem(ItemStack stack, int count) {
        if (!stack.isEmpty()) {
            int itemId = getItemId(stack);
            int itemCount = Math.min(count, stack.getCount());
            this.addItem(itemId, itemCount);
        }
    }
    
    public boolean contains(int itemId) {
        return this.idToAmountMap.get(itemId) > 0;
    }
    
    /**
     * Takes an amount from the finder
     *
     * @return the amount taken
     */
    public int take(int itemId, int amount) {
        int mapAmount = this.idToAmountMap.get(itemId);
        if (mapAmount >= amount) {
            this.idToAmountMap.put(itemId, mapAmount - amount);
            return itemId;
        } else {
            return 0;
        }
    }
    
    private void addItem(int itemId, int itemCount) {
        this.idToAmountMap.put(itemId, this.idToAmountMap.get(itemId) + itemCount);
    }
    
    public boolean findRecipe(NonNullList<Ingredient> ingredients, @Nullable IntList intList_1) {
        return this.findRecipe(ingredients, intList_1, 1);
    }
    
    public boolean findRecipe(NonNullList<Ingredient> ingredients, @Nullable IntList intList_1, int maxCrafts) {
        return (new RecipeFinder.Filter(ingredients)).find(maxCrafts, intList_1);
    }
    
    public int countRecipeCrafts(NonNullList<Ingredient> ingredients, @Nullable IntList intList_1) {
        return this.countRecipeCrafts(ingredients, Integer.MAX_VALUE, intList_1);
    }
    
    public int countRecipeCrafts(NonNullList<Ingredient> ingredients, int maxCrafts, @Nullable IntList intList_1) {
        return (new RecipeFinder.Filter(ingredients)).countCrafts(maxCrafts, intList_1);
    }
    
    public void clear() {
        this.idToAmountMap.clear();
    }
    
    class Filter {
        private final List<Ingredient> ingredients = Lists.newArrayList();
        private final int ingredientCount;
        private final int[] usableIngredientItemIds;
        private final int usableIngredientSize;
        private final BitSet bitSet;
        private final IntList path = new IntArrayList();
        private final NonNullList<Ingredient> ingredientsInput;
        
        public Filter(NonNullList<Ingredient> ingredientsInput) {
            this.ingredientsInput = ingredientsInput;
            this.ingredients.addAll(new ArrayList<>(ingredientsInput));
            this.ingredients.removeIf(Ingredient::isEmpty);
            this.ingredientCount = this.ingredients.size();
            this.usableIngredientItemIds = this.getUsableIngredientItemIds();
            this.usableIngredientSize = this.usableIngredientItemIds.length;
            this.bitSet = new BitSet(this.ingredientCount + this.usableIngredientSize + this.ingredientCount + this.ingredientCount * this.usableIngredientSize);
            
            for (int ingredientIndex = 0; ingredientIndex < this.ingredients.size(); ++ingredientIndex) {
                IntList possibleStacks = this.ingredients.get(ingredientIndex).getStackingIds();
                
                // Loops over usable ingredients
                for (int usableIngredientIndex = 0; usableIngredientIndex < this.usableIngredientSize; ++usableIngredientIndex) {
                    if (possibleStacks.contains(this.usableIngredientItemIds[usableIngredientIndex])) {
                        this.bitSet.set(this.getIndex(true, usableIngredientIndex, ingredientIndex));
                    }
                }
            }
            
        }
        
        @SuppressWarnings("deprecation")
        public boolean find(int maxCrafts, @Nullable IntList intList_1) {
            if (maxCrafts <= 0) {
                return true;
            } else {
                int int_2;
                for (int_2 = 0; this.dfs(maxCrafts); ++int_2) {
                    RecipeFinder.this.take(this.usableIngredientItemIds[this.path.getInt(0)], maxCrafts);
                    int int_3 = this.path.size() - 1;
                    this.setSatisfied(this.path.getInt(int_3));
                    
                    for (int int_4 = 0; int_4 < int_3; ++int_4) {
                        this.toggleResidual((int_4 & 1) == 0, this.path.get(int_4), this.path.get(int_4 + 1));
                    }
                    
                    this.path.clear();
                    this.bitSet.clear(0, this.ingredientCount + this.usableIngredientSize);
                }
                
                boolean boolean_1 = int_2 == this.ingredientCount;
                boolean boolean_2 = boolean_1 && intList_1 != null;
                if (boolean_2) {
                    intList_1.clear();
                }
                
                this.bitSet.clear(0, this.ingredientCount + this.usableIngredientSize + this.ingredientCount);
                int int_5 = 0;
                List<Ingredient> list_1 = new ArrayList<>(ingredientsInput);
                
                for (Ingredient ingredient : list_1) {
                    if (boolean_2 && ingredient.isEmpty()) {
                        intList_1.add(0);
                    } else {
                        for (int int_7 = 0; int_7 < this.usableIngredientSize; ++int_7) {
                            if (this.hasResidual(false, int_5, int_7)) {
                                this.toggleResidual(true, int_7, int_5);
                                RecipeFinder.this.addItem(this.usableIngredientItemIds[int_7], maxCrafts);
                                if (boolean_2) {
                                    intList_1.add(this.usableIngredientItemIds[int_7]);
                                }
                            }
                        }
                        
                        ++int_5;
                    }
                }
                
                return boolean_1;
            }
        }
        
        private int[] getUsableIngredientItemIds() {
            IntCollection intCollection_1 = new IntAVLTreeSet();
            
            for (Ingredient ingredient_1 : this.ingredients) {
                intCollection_1.addAll(ingredient_1.getStackingIds());
            }
            
            IntIterator intIterator_1 = intCollection_1.iterator();
            
            while (intIterator_1.hasNext()) {
                if (!RecipeFinder.this.contains(intIterator_1.nextInt())) {
                    intIterator_1.remove();
                }
            }
            
            return intCollection_1.toIntArray();
        }
        
        private boolean dfs(int amount) {
            int usableIngredientSize = this.usableIngredientSize;
            
            for (int int_3 = 0; int_3 < usableIngredientSize; ++int_3) {
                if (RecipeFinder.this.idToAmountMap.get(this.usableIngredientItemIds[int_3]) >= amount) {
                    this.visit(false, int_3);
                    
                    while (!this.path.isEmpty()) {
                        int int_4 = this.path.size();
                        boolean boolean_1 = (int_4 & 1) == 1;
                        int int_5 = this.path.getInt(int_4 - 1);
                        if (!boolean_1 && !this.isSatisfied(int_5)) {
                            break;
                        }
                        
                        int int_6 = boolean_1 ? this.ingredientCount : usableIngredientSize;
                        
                        int int_8;
                        for (int_8 = 0; int_8 < int_6; ++int_8) {
                            if (!this.hasVisited(boolean_1, int_8) && this.hasConnection(boolean_1, int_5, int_8) && this.hasResidual(boolean_1, int_5, int_8)) {
                                this.visit(boolean_1, int_8);
                                break;
                            }
                        }
                        
                        int_8 = this.path.size();
                        if (int_8 == int_4) {
                            this.path.removeInt(int_8 - 1);
                        }
                    }
                    
                    if (!this.path.isEmpty()) {
                        return true;
                    }
                }
            }
            
            return false;
        }
        
        private boolean isSatisfied(int int_1) {
            return this.bitSet.get(this.getSatisfiedIndex(int_1));
        }
        
        private void setSatisfied(int int_1) {
            this.bitSet.set(this.getSatisfiedIndex(int_1));
        }
        
        private int getSatisfiedIndex(int int_1) {
            return this.ingredientCount + this.usableIngredientSize + int_1;
        }
        
        private boolean hasConnection(boolean boolean_1, int int_1, int int_2) {
            return this.bitSet.get(this.getIndex(boolean_1, int_1, int_2));
        }
        
        private boolean hasResidual(boolean boolean_1, int int_1, int int_2) {
            return boolean_1 != this.bitSet.get(1 + this.getIndex(boolean_1, int_1, int_2));
        }
        
        private void toggleResidual(boolean boolean_1, int int_1, int int_2) {
            this.bitSet.flip(1 + this.getIndex(boolean_1, int_1, int_2));
        }
        
        private int getIndex(boolean boolean_1, int int_1, int int_2) {
            int int_3 = boolean_1 ? int_1 * this.ingredientCount + int_2 : int_2 * this.ingredientCount + int_1;
            return this.ingredientCount + this.usableIngredientSize + this.ingredientCount + 2 * int_3;
        }
        
        private void visit(boolean boolean_1, int int_1) {
            this.bitSet.set(this.getVisitedIndex(boolean_1, int_1));
            this.path.add(int_1);
        }
        
        private boolean hasVisited(boolean boolean_1, int int_1) {
            return this.bitSet.get(this.getVisitedIndex(boolean_1, int_1));
        }
        
        private int getVisitedIndex(boolean boolean_1, int int_1) {
            return (boolean_1 ? 0 : this.ingredientCount) + int_1;
        }
        
        public int countCrafts(int maxCrafts, @Nullable IntList intList_1) {
            int int_2 = 0;
            int crafts = Math.min(maxCrafts, this.getMinIngredientCount()) + 1;
            
            while (true) {
                while (true) {
                    int int_4 = (int_2 + crafts) / 2;
                    if (this.find(int_4, null)) {
                        if (crafts - int_2 <= 1) {
                            if (int_4 > 0) {
                                this.find(int_4, intList_1);
                            }
                            
                            return int_4;
                        }
                        
                        int_2 = int_4;
                    } else {
                        crafts = int_4;
                    }
                }
            }
        }
        
        @SuppressWarnings("deprecation")
        private int getMinIngredientCount() {
            int min = Integer.MAX_VALUE;
            
            for (Ingredient ingredient : this.ingredients) {
                int maxIngredientCount = 0;
                
                int currStackingId;
                for (IntListIterator stackingIds = ingredient.getStackingIds().iterator(); stackingIds.hasNext(); maxIngredientCount = Math.max(maxIngredientCount, RecipeFinder.this.idToAmountMap.get(currStackingId))) {
                    currStackingId = stackingIds.next();
                }
                
                if (min > 0) {
                    min = Math.min(min, maxIngredientCount);
                }
            }
            
            return min;
        }
    }
}
