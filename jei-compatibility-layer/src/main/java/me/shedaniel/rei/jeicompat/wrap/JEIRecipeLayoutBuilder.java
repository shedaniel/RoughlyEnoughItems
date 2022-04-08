/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020, 2021, 2022 shedaniel
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

package me.shedaniel.rei.jeicompat.wrap;

import me.shedaniel.math.Point;
import me.shedaniel.rei.impl.common.InternalLogger;
import mezz.jei.api.gui.builder.IIngredientAcceptor;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.recipe.RecipeIngredientRole;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class JEIRecipeLayoutBuilder implements IRecipeLayoutBuilder {
    private boolean dirty = false;
    public Predicate<RecipeIngredientRole> rolePredicate;
    public List<JEIRecipeSlot> slots = new ArrayList<>();
    
    @Override
    public IRecipeSlotBuilder addSlot(RecipeIngredientRole role, int x, int y) {
        return addSlot(role, x, y, -1);
    }
    
    public IRecipeSlotBuilder addSlot(RecipeIngredientRole role, int x, int y, int index) {
        if (index == -1) {
            index = getNextId(slots.stream().map(JEIRecipeSlot::getIndex).filter(integer -> integer >= 0)
                    .collect(Collectors.toSet()));
        }
        JEIRecipeSlot slot = new JEIRecipeSlot(index, role, new Point(x, y));
        if (rolePredicate != null && !rolePredicate.test(role)) return slot;
        slots.add(slot);
        return slot;
    }
    
    
    private int getNextId(Set<Integer> keys) {
        for (int i = 0; ; i++) {
            if (!keys.contains(i)) {
                return i;
            }
        }
    }
    
    @Override
    public IIngredientAcceptor<?> addInvisibleIngredients(RecipeIngredientRole role) {
        return _addInvisibleIngredients(role);
    }
    
    public JEIRecipeSlot _addInvisibleIngredients(RecipeIngredientRole role) {
        JEIRecipeSlot slot = new JEIRecipeSlot(-1, role, null);
        if (rolePredicate != null && !rolePredicate.test(role)) return slot;
        slots.add(slot);
        return slot;
    }
    
    @Override
    public void moveRecipeTransferButton(int posX, int posY) {
        markDirty();
    }
    
    @Override
    public void setShapeless() {
        markDirty();
    }
    
    @Override
    public void setShapeless(int posX, int posY) {
        markDirty();
    }
    
    @Override
    public void createFocusLink(IRecipeSlotBuilder... slots) {
        InternalLogger.getInstance().error("createFocusLink is not supported in REI yet!");
    }
    
    private void markDirty() {
        this.dirty = true;
    }
    
    public boolean isDirty() {
        return dirty;
    }
}
