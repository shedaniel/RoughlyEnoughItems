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

package me.shedaniel.rei.impl.common.entry.type.collapsed;

import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.entry.CollapsibleEntryRegistry;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.CollectionUtils;
import me.shedaniel.rei.impl.common.InternalLogger;
import me.shedaniel.rei.impl.common.util.HashedEntryStackWrapper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class CollapsibleEntryRegistryImpl implements CollapsibleEntryRegistry {
    private final Map<ResourceLocation, Entry> entries = new LinkedHashMap<>();
    
    @Override
    public <T> void group(ResourceLocation id, Component name, List<? extends EntryStack<? extends T>> stacks) {
        Objects.requireNonNull(stacks, "stacks");
        Entry old = this.entries.put(id, new Entry(id, name, new ListMatcher(CollectionUtils.map(stacks, HashedEntryStackWrapper::new)), true));
        InternalLogger.getInstance().debug("Added collapsible entry group [%s] %s with %d entries", id, name.getString(), stacks.size());
        if (old != null) {
            InternalLogger.getInstance().warn("Overwritten collapsible entry group [%s] %s with %d entries", id, name.getString(), stacks.size());
        }
    }
    
    @Override
    public void group(ResourceLocation id, Component name, Predicate<? extends EntryStack<?>> predicate) {
        Objects.requireNonNull(predicate, "predicate");
        Entry old = this.entries.put(id, new Entry(id, name, (stack, hashExact) -> ((Predicate<EntryStack<?>>) predicate).test(stack), false));
        InternalLogger.getInstance().debug("Added collapsible entry group [%s] %s with dynamic predicate", id, name.getString());
        if (old != null) {
            InternalLogger.getInstance().warn("Overwritten collapsible entry group [%s] %s with dynamic predicate", id, name.getString());
        }
    }
    
    @Override
    public void startReload() {
        this.entries.clear();
    }
    
    @Override
    public void endReload() {
        InternalLogger.getInstance().debug("Registered %d collapsible entry groups: %s", entries.values().size(),
                entries.values().stream().map(entry -> entry.getName().getString()).collect(Collectors.joining(", ")));
    }
    
    @Override
    public void acceptPlugin(REIClientPlugin plugin) {
        plugin.registerCollapsibleEntries(this);
    }
    
    public Collection<Entry> getEntries() {
        return entries.values();
    }
    
    public static class Entry {
        private final ResourceLocation id;
        private final Component name;
        private final Matcher matcher;
        private boolean canCache;
        private boolean expanded;
        
        public Entry(ResourceLocation id, Component name, Matcher matcher, boolean canCache) {
            this.id = id;
            this.name = name;
            this.matcher = matcher;
            this.canCache = canCache;
        }
        
        public ResourceLocation getId() {
            return id;
        }
        
        public String getModId() {
            return id.getNamespace();
        }
        
        public Component getName() {
            return name;
        }
        
        public Matcher getMatcher() {
            return matcher;
        }
        
        public boolean canCache() {
            return canCache;
        }
        
        public boolean isExpanded() {
            return expanded;
        }
        
        public void setExpanded(boolean expanded) {
            this.expanded = expanded;
        }
    }
    
    
    @FunctionalInterface
    public interface Matcher {
        boolean matches(EntryStack<?> stack, long hashExact);
    }
    
    private static class ListMatcher implements Matcher {
        private final Set<HashedEntryStackWrapper> stacks;
        
        public ListMatcher(List<? extends HashedEntryStackWrapper> stacks) {
            this.stacks = new HashSet<>(stacks);
        }
        
        @Override
        public boolean matches(EntryStack<?> stack, long hashExact) {
            return this.stacks.contains(new HashedEntryStackWrapper(stack, hashExact));
        }
    }
}
