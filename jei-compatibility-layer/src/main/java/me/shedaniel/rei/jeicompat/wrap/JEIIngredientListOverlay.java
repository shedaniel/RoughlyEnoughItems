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

import lombok.experimental.ExtensionMethod;
import me.shedaniel.rei.api.client.REIRuntime;
import me.shedaniel.rei.api.client.overlay.ScreenOverlay;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.type.EntryType;
import me.shedaniel.rei.jeicompat.JEIPluginDetector;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IIngredientListOverlay;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@ExtensionMethod(JEIPluginDetector.class)
public enum JEIIngredientListOverlay implements IIngredientListOverlay {
    INSTANCE;
    
    @Override
    public Optional<ITypedIngredient<?>> getIngredientUnderMouse() {
        if (!REIRuntime.getInstance().isOverlayVisible()) return Optional.empty();
        ScreenOverlay overlay = REIRuntime.getInstance().getOverlay().get();
        EntryStack<?> stack = overlay.getEntryList().getFocusedStack();
        return stack.typedJeiValueOpWild();
    }
    
    @Override
    @Nullable
    public <T> T getIngredientUnderMouse(@NotNull IIngredientType<T> ingredientType) {
        return getIngredientUnderMouse().flatMap(ingredient -> ingredient.getIngredient(ingredientType)).orElse(null);
    }
    
    @Override
    public boolean hasKeyboardFocus() {
        return REIRuntime.getInstance().isOverlayVisible() && REIRuntime.getInstance().getSearchTextField().isFocused();
    }
    
    @Override
    @NotNull
    public <T> List<T> getVisibleIngredients(IIngredientType<T> ingredientType) {
        if (REIRuntime.getInstance().isOverlayVisible()) {
            EntryType<T> type = ingredientType.unwrapType();
            ScreenOverlay overlay = REIRuntime.getInstance().getOverlay().get();
            return (List<T>) overlay.getEntryList().getEntries()
                    .filter(entryStack -> entryStack.getType() == type)
                    .map(JEIPluginDetector::jeiValue)
                    .collect(Collectors.toList());
        }
        return null;
    }
}
