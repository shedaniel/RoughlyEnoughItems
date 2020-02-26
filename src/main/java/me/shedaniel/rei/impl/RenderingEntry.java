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

import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.gui.widget.QueuedTooltip;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

@ApiStatus.Internal
public abstract class RenderingEntry extends DrawableHelper implements EntryStack {
    @Override
    public Optional<Identifier> getIdentifier() {
        return Optional.empty();
    }
    
    @Override
    public Type getType() {
        return Type.RENDER;
    }
    
    @Override
    public int getAmount() {
        return 0;
    }
    
    @Override
    public void setAmount(int amount) {
    
    }
    
    @Override
    public boolean isEmpty() {
        return false;
    }
    
    @Override
    public EntryStack copy() {
        return this;
    }
    
    @Override
    public Object getObject() {
        return null;
    }
    
    @Override
    public boolean equals(EntryStack stack, boolean ignoreTags, boolean ignoreAmount) {
        return stack == this;
    }
    
    @Override
    public boolean equalsIgnoreTagsAndAmount(EntryStack stack) {
        return stack == this;
    }
    
    @Override
    public boolean equalsIgnoreTags(EntryStack stack) {
        return stack == this;
    }
    
    @Override
    public boolean equalsIgnoreAmount(EntryStack stack) {
        return stack == this;
    }
    
    @Override
    public boolean equalsAll(EntryStack stack) {
        return stack == this;
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
    public <T> EntryStack setting(Settings<T> settings, T value) {
        return this;
    }
    
    @Override
    public <T> EntryStack removeSetting(Settings<T> settings) {
        return this;
    }
    
    @Override
    public EntryStack clearSettings() {
        return this;
    }
    
    @Override
    public <T> EntryStack addSetting(Settings<T> settings, T value) {
        return this;
    }
    
    @Override
    public <T> T get(Settings<T> settings) {
        return settings.getDefaultValue();
    }
    
    @Nullable
    @Override
    public QueuedTooltip getTooltip(int mouseX, int mouseY) {
        return null;
    }
}
