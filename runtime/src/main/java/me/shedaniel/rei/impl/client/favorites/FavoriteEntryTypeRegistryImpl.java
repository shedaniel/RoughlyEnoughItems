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

package me.shedaniel.rei.impl.client.favorites;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import me.shedaniel.rei.api.client.config.ConfigManager;
import me.shedaniel.rei.api.client.config.ConfigObject;
import me.shedaniel.rei.api.client.favorites.FavoriteEntry;
import me.shedaniel.rei.api.client.favorites.FavoriteEntryType;
import me.shedaniel.rei.api.client.favorites.SystemFavoriteEntryProvider;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.common.registry.ReloadStage;
import me.shedaniel.rei.api.common.util.CollectionUtils;
import me.shedaniel.rei.impl.client.config.ConfigManagerImpl;
import me.shedaniel.rei.impl.common.InternalLogger;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.mutable.MutableLong;
import org.apache.commons.lang3.tuple.Triple;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@ApiStatus.Internal
public class FavoriteEntryTypeRegistryImpl implements FavoriteEntryType.Registry {
    private final BiMap<ResourceLocation, FavoriteEntryType<?>> registry = HashBiMap.create();
    private final List<Triple<SystemFavoriteEntryProvider<?>, MutableLong, List<FavoriteEntry>>> systemFavorites = Lists.newArrayList();
    private final Map<Component, FavoriteEntryType.Section> sections = Maps.newConcurrentMap();
    private final List<FavoriteEntryType.Section> sectionsList = Lists.newCopyOnWriteArrayList();
    
    @Override
    public ReloadStage getStage() {
        return ReloadStage.START;
    }
    
    @Override
    public void acceptPlugin(REIClientPlugin plugin) {
        plugin.registerFavorites(this);
    }
    
    @Override
    public void register(ResourceLocation id, FavoriteEntryType<?> type) {
        this.registry.put(id, type);
        InternalLogger.getInstance().debug("Added favorite entry type [%s]: %s", id, type);
    }
    
    @Override
    public <A extends FavoriteEntry> @Nullable FavoriteEntryType<A> get(ResourceLocation id) {
        return (FavoriteEntryType<A>) this.registry.get(id);
    }
    
    @Override
    @Nullable
    public ResourceLocation getId(FavoriteEntryType<?> type) {
        return this.registry.inverse().get(type);
    }
    
    @Override
    public FavoriteEntryType.Section getOrCrateSection(Component text) {
        return sections.computeIfAbsent(text, $ -> {
            SectionImpl section = new SectionImpl($);
            sectionsList.add(section);
            return section;
        });
    }
    
    @Override
    public Iterable<FavoriteEntryType.Section> sections() {
        return sectionsList;
    }
    
    @Override
    public <A extends FavoriteEntry> void registerSystemFavorites(SystemFavoriteEntryProvider<A> provider) {
        this.systemFavorites.add(Triple.of(provider, new MutableLong(-1), new ArrayList<>()));
        InternalLogger.getInstance().debug("Added system favorites: %s", provider);
    }
    
    public List<Triple<SystemFavoriteEntryProvider<?>, MutableLong, List<FavoriteEntry>>> getSystemProviders() {
        return this.systemFavorites;
    }
    
    @Override
    public void startReload() {
        this.registry.clear();
        this.systemFavorites.clear();
        this.sections.clear();
        this.sectionsList.clear();
    }
    
    @Override
    public void endReload() {
        if (ConfigObject.getInstance().isFavoritesEnabled()) {
            ConfigManagerImpl.getInstance().getConfig().getConfigFavoriteEntries().removeIf(FavoriteEntry::isInvalid);
            ConfigManagerImpl.getInstance().getConfig().getHiddenFavoriteEntries().removeIf(FavoriteEntry::isInvalid);
            
            ConfigManager.getInstance().saveConfig();
        }
        
        InternalLogger.getInstance().debug("Registered %d favorite entry types", registry.size());
    }
    
    private static class SectionImpl implements FavoriteEntryType.Section {
        private final Component text;
        private final List<CompoundEntry> entries = new ArrayList<>();
        
        public SectionImpl(Component text) {
            this.text = text;
        }
        
        @Override
        public void add(boolean defaultFavorited, FavoriteEntry... entries) {
            this.entries.addAll(CollectionUtils.map(entries,
                    entry -> new CompoundEntry(entry, defaultFavorited)));
        }
        
        @Override
        public Component getText() {
            return text;
        }
        
        @Override
        public List<FavoriteEntry> getEntries() {
            return CollectionUtils.map(entries, CompoundEntry::entry);
        }
        
        @Override
        public List<FavoriteEntry> getDefaultEntries() {
            return CollectionUtils.filterAndMap(entries, CompoundEntry::defaultFavorited, CompoundEntry::entry);
        }
        
        public record CompoundEntry(FavoriteEntry entry, boolean defaultFavorited) {}
    }
}
