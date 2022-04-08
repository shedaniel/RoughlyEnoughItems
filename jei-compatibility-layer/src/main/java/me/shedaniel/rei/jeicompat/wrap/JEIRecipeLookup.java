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
import me.shedaniel.rei.api.client.view.ViewSearchBuilder;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.impl.display.DisplaySpec;
import me.shedaniel.rei.jeicompat.JEIPluginDetector;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IRecipeLookup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

@ExtensionMethod(JEIPluginDetector.class)
public class JEIRecipeLookup<R> implements IRecipeLookup<R> {
    private final CategoryIdentifier<?> categoryIdentifier;
    private final List<IFocus<?>> focui = new ArrayList<>();
    private boolean includesHidden = false;
    
    public JEIRecipeLookup(CategoryIdentifier<?> categoryIdentifier) {
        this.categoryIdentifier = categoryIdentifier;
    }
    
    @Override
    public IRecipeLookup<R> limitFocus(Collection<IFocus<?>> focuses) {
        this.focui.addAll(focuses);
        return this;
    }
    
    @Override
    public IRecipeLookup<R> includeHidden() {
        this.includesHidden = true;
        return this;
    }
    
    @Override
    public Stream<R> get() {
        ViewSearchBuilder builder = ViewSearchBuilder.builder()
                .filterCategory(categoryIdentifier);
        for (IFocus<?> focus : focui) {
            EntryStack<?> stack = focus.getTypedValue().unwrapStack();
            if (focus.getMode() == IFocus.Mode.INPUT) {
                builder.addUsagesFor(stack);
            } else {
                builder.addRecipesFor(stack);
            }
        }
        if (includesHidden) {
            builder.processingVisibilityHandlers(false);
        }
        return builder.streamDisplays()
                .map(DisplaySpec::provideInternalDisplay)
                .map(display -> (R) display.jeiValue());
    }
}
