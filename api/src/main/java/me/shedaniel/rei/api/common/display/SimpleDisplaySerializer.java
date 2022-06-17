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

package me.shedaniel.rei.api.common.display;

import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.minecraft.nbt.CompoundTag;

import java.util.List;

/**
 * A simple display serializer that serializes both the input and output of a display.
 *
 * @param <D> the display type
 */
public interface SimpleDisplaySerializer<D extends Display> extends DisplaySerializer<D> {
    /**
     * {@inheritDoc}
     */
    @Override
    default CompoundTag save(CompoundTag tag, D display) {
        tag.put("input", EntryIngredients.save(getInputIngredients(display)));
        tag.put("output", EntryIngredients.save(getOutputIngredients(display)));
        tag = saveExtra(tag, display);
        return tag;
    }
    
    /**
     * Returns the input ingredients of the display for serialization.
     *
     * @param display the display
     * @return the input ingredients
     */
    default List<EntryIngredient> getInputIngredients(D display) {
        return display.getInputEntries();
    }
    
    /**
     * Returns the output ingredients of the display for serialization.
     *
     * @param display the display
     * @return the output ingredients
     */
    default List<EntryIngredient> getOutputIngredients(D display) {
        return display.getOutputEntries();
    }
    
    /**
     * Serializes the extra data of the display into the tag.
     *
     * @param tag     the tag
     * @param display the display
     * @return the tag
     */
    CompoundTag saveExtra(CompoundTag tag, D display);
}
