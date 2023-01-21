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

package me.shedaniel.rei.impl.client.gui.widget.entrylist;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.impl.common.entry.type.collapsed.CollapsedStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class CollapsingEntryListWidget extends EntryListWidget {
    private List</*EntryStack<?> | CollapsedStack*/ Object> collapsedStacks = new ArrayList<>();
    private Int2ObjectMap<CollapsedStack> collapsedStackIndexed = new Int2ObjectOpenHashMap<>();
    protected int updatedCount;
    protected int lastUpdatedCount;
    
    public abstract List</*EntryStack<?> | List<EntryStack<?>>*/ Object> getStacks();
    
    protected abstract void setStacks(List</*EntryStack<?> | List<EntryStack<?>>*/ Object> stacks);
    
    protected final Int2ObjectMap<CollapsedStack> getCollapsedStackIndexed() {
        return collapsedStackIndexed;
    }
    
    @Override
    public boolean isEmpty() {
        return collapsedStacks.isEmpty();
    }
    
    @Override
    protected void setCollapsedStacks(List</*EntryStack<?> | CollapsedStack*/ Object> stacks) {
        this.collapsedStacks = stacks;
        updateStacks();
    }
    
    @Override
    public void updateEntriesPosition() {
        if (updatedCount != lastUpdatedCount) {
            updateStacks();
        }
        
        super.updateEntriesPosition();
    }
    
    private void updateStacks() {
        lastUpdatedCount = updatedCount;
        
        List</*EntryStack<?> | List<EntryStack<?>>*/ Object> stacks = new ArrayList<>((int) (collapsedStacks.size() * 1.5));
        Map</*EntryStack<?> | List<EntryStack<?>>*/ Object, CollapsedStack> collapsedStackMap = new Reference2ObjectOpenHashMap<>();
        Int2ObjectMap<CollapsedStack> collapsedStackIndexed = new Int2ObjectOpenHashMap<>();
        
        for (Object obj : collapsedStacks) {
            if (obj instanceof EntryStack<?> stack) {
                stacks.add(stack);
            } else if (obj instanceof CollapsedStack stack) {
                List<EntryStack<?>> ingredient = stack.getIngredient();
                if (stack.isExpanded()) {
                    stacks.addAll(ingredient);
                    collapsedStackMap.put(ingredient.get(0), stack);
                } else {
                    stacks.add(ingredient);
                    collapsedStackMap.put(ingredient, stack);
                }
            }
        }
        
        setStacks(stacks);
        
        int index = 0;
        for (/*EntryStack<?> | EntryIngredient*/ Object stack : stacks) {
            CollapsedStack collapsedStack = collapsedStackMap.get(stack);
            
            if (collapsedStack != null) {
                collapsedStackIndexed.put(index, collapsedStack);
                
                if (collapsedStack.isExpanded()) {
                    int size = collapsedStack.getIngredient().size();
                    for (int i = 1; i < size; i++) {
                        collapsedStackIndexed.put(index + i, collapsedStack);
                    }
                }
            }
            
            index++;
        }
        
        this.collapsedStackIndexed = collapsedStackIndexed;
    }
}
