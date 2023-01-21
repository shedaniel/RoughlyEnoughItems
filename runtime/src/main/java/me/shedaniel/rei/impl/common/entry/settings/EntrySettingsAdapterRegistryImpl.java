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

package me.shedaniel.rei.impl.common.entry.settings;

import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.settings.EntrySettingsAdapter;
import me.shedaniel.rei.api.common.entry.settings.EntrySettingsAdapterRegistry;
import me.shedaniel.rei.api.common.entry.type.EntryType;
import me.shedaniel.rei.api.common.plugins.REIPlugin;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class EntrySettingsAdapterRegistryImpl implements EntrySettingsAdapterRegistry {
    private final Map<EntryStack.Settings<?>, Multimap<EntryType<?>, EntrySettingsAdapter<?, ?>>> providers = new HashMap<>();
    
    @Override
    public <T,S> void register(EntryType<T> type, EntryStack.Settings<S> settings, EntrySettingsAdapter<T,S> provider) {
        Multimap<EntryType<?>, EntrySettingsAdapter<?, ?>> multimap = this.providers.computeIfAbsent(settings, $ -> Multimaps.newMultimap(new Reference2ObjectOpenHashMap<>(), ArrayList::new));
        multimap.put(type, provider);
    }
    
    @Override
    @Nullable
    public <T,S> S adapt(EntryStack<T> stack, EntryStack.Settings<S> settings, @Nullable S value) {
        Multimap<EntryType<?>, EntrySettingsAdapter<?, ?>> multimap = providers.get(settings);
        if (multimap != null) {
            for (EntrySettingsAdapter<T, S> adapter : (Collection<EntrySettingsAdapter<T, S>>) (Collection<? extends EntrySettingsAdapter<?, ?>>) multimap.get(stack.getType())) {
                value = adapter.provide(stack, settings, value);
            }
        }
        return value;
    }
    
    @Override
    public void startReload() {
        providers.clear();
    }
    
    @Override
    public void acceptPlugin(REIPlugin<?> plugin) {
        plugin.registerEntrySettingsAdapters(this);
    }
}
