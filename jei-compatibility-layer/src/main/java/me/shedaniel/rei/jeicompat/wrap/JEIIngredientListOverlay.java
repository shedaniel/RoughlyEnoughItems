/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020, 2021 shedaniel
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

import com.google.common.collect.ImmutableList;
import me.shedaniel.rei.api.client.REIRuntime;
import me.shedaniel.rei.api.client.overlay.ScreenOverlay;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.jeicompat.JEIPluginDetector;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.runtime.IIngredientListOverlay;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static me.shedaniel.rei.jeicompat.JEIPluginDetector.unwrap;

public enum JEIIngredientListOverlay implements IIngredientListOverlay {
    INSTANCE;
    
    @Override
    @Nullable
    public Object getIngredientUnderMouse() {
        if (!REIRuntime.getInstance().isOverlayVisible()) return null;
        ScreenOverlay overlay = REIRuntime.getInstance().getOverlay().get();
        EntryStack<?> stack = overlay.getEntryList().getFocusedStack();
        if (stack.isEmpty()) return null;
        return unwrap(stack);
    }
    
    @Override
    @Nullable
    public <T> T getIngredientUnderMouse(@NotNull IIngredientType<T> ingredientType) {
        Object underMouse = getIngredientUnderMouse();
        if (underMouse == null) {
            return null;
        }
        if (ingredientType.getIngredientClass().isInstance(underMouse.getClass())) {
            return (T) underMouse;
        }
        return null;
    }
    
    @Override
    public boolean hasKeyboardFocus() {
        return REIRuntime.getInstance().isOverlayVisible() && REIRuntime.getInstance().getSearchTextField().isFocused();
    }
    
    @Override
    @NotNull
    public ImmutableList<Object> getVisibleIngredients() {
        if (REIRuntime.getInstance().isOverlayVisible()) {
            ScreenOverlay overlay = REIRuntime.getInstance().getOverlay().get();
            return overlay.getEntryList().getEntries().map(JEIPluginDetector::unwrap)
                    .collect(ImmutableList.toImmutableList());
        }
        return null;
    }
}
