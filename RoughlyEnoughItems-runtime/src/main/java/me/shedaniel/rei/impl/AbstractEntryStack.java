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

import it.unimi.dsi.fastutil.shorts.Short2ObjectMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectMaps;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import me.shedaniel.rei.api.EntryStack;
import net.minecraft.client.gui.AbstractGui;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public abstract class AbstractEntryStack extends AbstractGui implements EntryStack {
    private static final Short2ObjectMap<Object> EMPTY_SETTINGS = Short2ObjectMaps.emptyMap();
    private Short2ObjectMap<Object> settings = null;
    
    @Override
    public <T> EntryStack setting(Settings<T> settings, T value) {
        short settingsId = settings.getId();
        if (this.settings == null)
            this.settings = Short2ObjectMaps.singleton(settingsId, value);
        else {
            if (this.settings.size() == 1) {
                if (this.settings.containsKey(settingsId)) {
                    this.settings = Short2ObjectMaps.singleton(settingsId, value);
                    return this;
                } else {
                    Short2ObjectMap<Object> singletonSettings = this.settings;
                    this.settings = new Short2ObjectOpenHashMap<>(4, 1);
                    this.settings.putAll(singletonSettings);
                }
            }
            this.settings.put(settingsId, value);
        }
        return this;
    }
    
    @Override
    public <T> EntryStack removeSetting(Settings<T> settings) {
        if (this.settings != null) {
            short settingsId = settings.getId();
            if (this.settings.size() == 1) {
                if (this.settings.containsKey(settingsId)) {
                    this.settings = null;
                }
            } else if (this.settings.remove(settingsId) != null && this.settings.isEmpty()) {
                this.settings = null;
            }
        }
        return this;
    }
    
    @Override
    public EntryStack clearSettings() {
        this.settings = null;
        return this;
    }
    
    protected Short2ObjectMap<Object> getSettings() {
        return this.settings == null ? EMPTY_SETTINGS : this.settings;
    }
    
    @Override
    public <T> T get(Settings<T> settings) {
        Object o = this.settings == null ? null : this.settings.get(settings.getId());
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
