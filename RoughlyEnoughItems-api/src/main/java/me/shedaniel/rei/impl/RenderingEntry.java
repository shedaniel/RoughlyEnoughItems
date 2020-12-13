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

package me.shedaniel.rei.impl;

import me.shedaniel.architectury.utils.Fraction;
import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.api.entry.BuiltinEntryTypes;
import me.shedaniel.rei.api.entry.ComparisonContext;
import me.shedaniel.rei.api.entry.EntryDefinition;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Unit;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

@ApiStatus.OverrideOnly
public abstract class RenderingEntry extends GuiComponent implements EntryStack<Unit> {
    @Override
    public @NotNull EntryDefinition<Unit> getDefinition() {
        return BuiltinEntryTypes.RENDERING.getDefinition();
    }
    
    @Override
    public Optional<ResourceLocation> getIdentifier() {
        return Optional.empty();
    }
    
    @Override
    public Fraction getAmount() {
        return Fraction.zero();
    }
    
    @Override
    public void setAmount(Fraction amount) {
        
    }
    
    @Override
    public boolean isEmpty() {
        return false;
    }
    
    @Override
    public EntryStack<Unit> copy() {
        return this;
    }
    
    @Override
    public Unit getValue() {
        return Unit.INSTANCE;
    }
    
    @Override
    public boolean equals(EntryStack<Unit> other, ComparisonContext context) {
        return this == other;
    }
    
    @Override
    public int hash(ComparisonContext context) {
        return System.identityHashCode(this);
    }
    
    @Override
    public int getZ() {
        return getBlitOffset();
    }
    
    @Override
    public void setZ(int z) {
        setBlitOffset(z);
    }
    
    @Override
    public <R> EntryStack<Unit> setting(Settings<R> settings, R value) {
        return this;
    }
    
    @Override
    public <R> EntryStack<Unit> removeSetting(Settings<R> settings) {
        return this;
    }
    
    @Override
    public EntryStack<Unit> clearSettings() {
        return this;
    }
    
    @Override
    public <T> T get(Settings<T> settings) {
        return settings.getDefaultValue();
    }
}
