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

package me.shedaniel.rei.api.common.display.basic;

import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.impl.Internals;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;
import java.util.Optional;

/**
 * A basic implementation of a display, consisting of a list of inputs, a list of outputs
 * and a possible display location.
 */
public abstract class BasicDisplay implements Display {
    protected List<EntryIngredient> inputs;
    protected List<EntryIngredient> outputs;
    protected Optional<Identifier> location;
    
    public BasicDisplay(List<EntryIngredient> inputs, List<EntryIngredient> outputs) {
        this(inputs, outputs, Optional.empty());
    }
    
    public BasicDisplay(List<EntryIngredient> inputs, List<EntryIngredient> outputs, Optional<Identifier> location) {
        this.inputs = inputs;
        this.outputs = outputs;
        this.location = location;
    }
    
    @ApiStatus.Experimental
    public static RegistryAccess registryAccess() {
        return Internals.getRegistryAccess();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public List<EntryIngredient> getInputEntries() {
        return inputs;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public List<EntryIngredient> getOutputEntries() {
        return outputs;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Identifier> getDisplayLocation() {
        return location;
    }
}
