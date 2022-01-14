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

package me.shedaniel.rei.impl.client.entry.renderer;

import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import me.shedaniel.rei.api.client.entry.renderer.EntryRenderer;
import me.shedaniel.rei.api.client.entry.renderer.EntryRendererProvider;
import me.shedaniel.rei.api.client.entry.renderer.EntryRendererRegistry;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.type.EntryType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

public class EntryRendererRegistryImpl implements EntryRendererRegistry {
    private final Multimap<EntryType<?>, EntryRendererProvider<?>> providers = Multimaps.newListMultimap(new Reference2ObjectOpenHashMap<>(), ArrayList::new);
    
    @Override
    public <T> void register(EntryType<T> type, EntryRendererProvider<T> provider) {
        this.providers.put(type, provider);
    }
    
    @Override
    public <T> EntryRenderer<T> get(EntryStack<T> stack) {
        EntryRenderer<T> renderer = stack.getDefinition().getRenderer();
        for (EntryRendererProvider<T> provider : (Collection<EntryRendererProvider<T>>) (Collection<? extends EntryRendererProvider<?>>) providers.get(stack.getType())) {
            renderer = Objects.requireNonNull(provider.provide(stack, renderer));
        }
        return renderer;
    }
    
    @Override
    public void startReload() {
        providers.clear();
    }
    
    @Override
    public void acceptPlugin(REIClientPlugin plugin) {
        plugin.registerEntryRenderers(this);
    }
}
