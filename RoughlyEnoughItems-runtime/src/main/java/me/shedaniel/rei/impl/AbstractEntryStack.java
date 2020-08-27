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

import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import me.shedaniel.rei.api.EntryStack;
import net.minecraft.client.gui.GuiComponent;
import org.jetbrains.annotations.ApiStatus;

import java.util.Map;

@ApiStatus.Internal
public abstract class AbstractEntryStack extends GuiComponent implements EntryStack {
    private Reference2ObjectMap<Settings<?>, Object> settings = new Reference2ObjectOpenHashMap<>();
    
    @Override
    public <T> EntryStack setting(Settings<T> settings, T value) {
        this.settings.put(settings, value);
        return this;
    }
    
    @Override
    public <T> EntryStack removeSetting(Settings<T> settings) {
        this.settings.remove(settings);
        return this;
    }
    
    @Override
    public EntryStack clearSettings() {
        this.settings.clear();
        return this;
    }
    
    protected Map<Settings<?>, Object> getSettings() {
        return settings;
    }
    
    @Override
    public <T> T get(Settings<T> settings) {
        Object o = this.settings.get(settings);
        if (o == null)
            return settings.getDefaultValue();
        return (T) o;
    }
    
    @Override
    public boolean equals(EntryStack stack, boolean ignoreTags, boolean ignoreAmount) {
        if (ignoreTags && ignoreAmount)
            return equalsIgnoreTagsAndAmount(stack);
        if (ignoreAmount)
            return equalsIgnoreAmount(stack);
        if (ignoreTags)
            return equalsIgnoreTags(stack);
        return equalsAll(stack);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof EntryStack))
            return false;
        EntryStack stack = (EntryStack) obj;
        boolean checkTags = get(Settings.CHECK_TAGS).get() || stack.get(Settings.CHECK_TAGS).get();
        boolean checkAmount = get(Settings.CHECK_AMOUNT).get() || stack.get(Settings.CHECK_AMOUNT).get();
        return equals(stack, !checkTags, !checkAmount);
    }
    
    @Override
    public int hashCode() {
        boolean checkTags = get(Settings.CHECK_TAGS).get();
        boolean checkAmount = get(Settings.CHECK_AMOUNT).get();
        if (!checkAmount && !checkTags)
            return hashIgnoreAmountAndTags();
        if (!checkAmount)
            return hashIgnoreAmount();
        if (!checkTags)
            return hashIgnoreTags();
        return hashOfAll();
    }
    
    @Override
    public int getZ() {
        return getBlitOffset();
    }
    
    @Override
    public void setZ(int z) {
        setBlitOffset(z);
    }
}
