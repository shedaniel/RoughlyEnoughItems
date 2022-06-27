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

package me.shedaniel.rei.api.client.registry.category.visibility;

import dev.architectury.event.EventResult;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/**
 * Handler for determining the visibility of categories.
 * This is preferred comparing to removing the categories from the registry.
 *
 * @see me.shedaniel.rei.api.client.registry.category.CategoryRegistry#registerVisibilityPredicate(CategoryVisibilityPredicate)
 */
@Environment(EnvType.CLIENT)
public interface CategoryVisibilityPredicate extends Comparable<CategoryVisibilityPredicate> {
    /**
     * Returns the priority of the handler, the higher the priority, the earlier this is called.
     *
     * @return the priority
     */
    default double getPriority() {
        return 0.0;
    }
    
    /**
     * Handles the visibility of the category.
     *
     * @param category the category of the display
     * @return the visibility
     * @see EventResult
     */
    EventResult handleCategory(DisplayCategory<?> category);
    
    /**
     * {@inheritDoc}
     */
    @Override
    default int compareTo(CategoryVisibilityPredicate o) {
        return Double.compare(getPriority(), o.getPriority());
    }
}
