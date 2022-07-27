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

package me.shedaniel.rei.impl.client.gui.widget.favorites;

import me.shedaniel.rei.api.client.REIRuntime;
import me.shedaniel.rei.api.client.config.ConfigManager;
import me.shedaniel.rei.api.client.favorites.FavoriteEntry;
import me.shedaniel.rei.api.client.favorites.FavoriteEntryType;
import me.shedaniel.rei.api.client.overlay.OverlayListWidget;
import me.shedaniel.rei.api.client.overlay.ScreenOverlay;
import me.shedaniel.rei.api.common.util.CollectionUtils;
import me.shedaniel.rei.impl.ClientInternals;
import me.shedaniel.rei.impl.client.config.ConfigManagerInternal;
import me.shedaniel.rei.impl.client.favorites.MutableFavoritesList;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class FavoritesEntriesManager implements ClientInternals.FavoritesEntriesListProvider {
    
    public static List<FavoriteEntry> getConfigFavoriteEntries() {
        ConfigManagerInternal manager = ConfigManagerInternal.getInstance();
        return (List<FavoriteEntry>) manager.get("basics.favorites");
    }
    
    public static List<FavoriteEntry> getConfigHiddenFavoriteEntries() {
        ConfigManagerInternal manager = ConfigManagerInternal.getInstance();
        return (List<FavoriteEntry>) manager.get("basics.hiddenFavorites");
    }
    
    private static Stream<FavoriteEntry> getDefaultFavorites() {
        return StreamSupport.stream(FavoriteEntryType.registry().sections().spliterator(), false)
                .flatMap(section -> section.getDefaultEntries().stream());
    }
    
    public static List<FavoriteEntry> getFavorites() {
        List<FavoriteEntry> defaultFavorites = getDefaultFavorites().collect(Collectors.toList());
        defaultFavorites.removeAll(getConfigHiddenFavoriteEntries());
        
        List<FavoriteEntry> favorites = new ArrayList<>(getConfigFavoriteEntries());
        defaultFavorites.removeAll(favorites);
        favorites.addAll(0, defaultFavorites);
        favorites.removeIf(FavoriteEntry::isInvalid);
        return favorites;
    }
    
    public static void remove(FavoriteEntry entry) {
        getConfigFavoriteEntries().remove(entry);
        if (getDefaultFavorites().anyMatch(e -> e.equals(entry)) && !getConfigHiddenFavoriteEntries().contains(entry)) {
            getConfigHiddenFavoriteEntries().add(entry);
        }
        
        ConfigManager.getInstance().saveConfig();
        REIRuntime.getInstance().getOverlay().flatMap(ScreenOverlay::getFavoritesList).ifPresent(OverlayListWidget::queueReloadSearch);
    }
    
    public static void add(FavoriteEntry entry) {
        List<FavoriteEntry> defaultFavorites = getDefaultFavorites().toList();
        
        getConfigFavoriteEntries().remove(entry);
        if (CollectionUtils.anyMatch(defaultFavorites, e -> e.equals(entry)) && !getConfigHiddenFavoriteEntries().contains(entry)) {
            getConfigHiddenFavoriteEntries().add(entry);
        }
        
        for (int i = defaultFavorites.size() - 1; i >= 0; i--) {
            FavoriteEntry e = defaultFavorites.get(i);
            if (!getConfigFavoriteEntries().contains(e) && !getConfigHiddenFavoriteEntries().contains(e)) {
                getConfigFavoriteEntries().add(0, e);
            }
        }
        
        getConfigHiddenFavoriteEntries().remove(entry);
        if (!CollectionUtils.anyMatch(defaultFavorites, e -> e.equals(entry))) {
            getConfigFavoriteEntries().add(entry);
        }
        
        ConfigManager.getInstance().saveConfig();
        REIRuntime.getInstance().getOverlay().flatMap(ScreenOverlay::getFavoritesList).ifPresent(OverlayListWidget::queueReloadSearch);
    }
    
    public static void setEntries(List<FavoriteEntry> entries) {
        List<FavoriteEntry> defaultFavorites = getDefaultFavorites().toList();
        List<FavoriteEntry> hiddenDefaultFavorites = new ArrayList<>(defaultFavorites);
        hiddenDefaultFavorites.removeAll(entries);
        getConfigHiddenFavoriteEntries().clear();
        getConfigHiddenFavoriteEntries().addAll(hiddenDefaultFavorites);
        getConfigFavoriteEntries().clear();
        getConfigFavoriteEntries().addAll(entries);
        
        ConfigManager.getInstance().saveConfig();
        REIRuntime.getInstance().getOverlay().flatMap(ScreenOverlay::getFavoritesList).ifPresent(OverlayListWidget::queueReloadSearch);
    }
    
    @Override
    public List<FavoriteEntry> get() {
        return new ListView();
    }
    
    private static class ListView extends AbstractList<FavoriteEntry> implements MutableFavoritesList {
        @Override
        public FavoriteEntry get(int index) {
            return getFavorites().get(index);
        }
        
        @Override
        public int size() {
            return getFavorites().size();
        }
        
        @Override
        public void add(int index, FavoriteEntry entry) {
            FavoritesEntriesManager.add(entry);
        }
        
        @Override
        public boolean remove(Object o) {
            if (o instanceof FavoriteEntry) {
                FavoritesEntriesManager.remove((FavoriteEntry) o);
                return true;
            } else {
                return false;
            }
        }
        
        @Override
        public void setAll(List<FavoriteEntry> entries) {
            FavoritesEntriesManager.setEntries(entries);
        }
    }
}
