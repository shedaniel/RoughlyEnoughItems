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

import me.shedaniel.rei.api.client.config.ConfigManager;
import me.shedaniel.rei.api.client.favorites.FavoriteEntry;
import me.shedaniel.rei.api.client.favorites.FavoriteEntryType;
import me.shedaniel.rei.api.common.util.CollectionUtils;
import me.shedaniel.rei.impl.client.config.ConfigManagerImpl;
import me.shedaniel.rei.impl.client.config.ConfigObjectImpl;
import me.shedaniel.rei.impl.client.gui.ScreenOverlayImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class FavoritesEntriesManager {
    public static final FavoritesEntriesManager INSTANCE = new FavoritesEntriesManager();
    
    private Stream<FavoriteEntry> getDefaultFavorites() {
        return StreamSupport.stream(FavoriteEntryType.registry().sections().spliterator(), false)
                .flatMap(section -> section.getDefaultEntries().stream());
    }
    
    public List<FavoriteEntry> getFavorites() {
        ConfigObjectImpl config = ConfigManagerImpl.getInstance().getConfig();
        List<FavoriteEntry> defaultFavorites = getDefaultFavorites().collect(Collectors.toList());
        defaultFavorites.removeAll(config.getHiddenFavoriteEntries());
        
        List<FavoriteEntry> favorites = new ArrayList<>(config.getFavoriteEntries());
        defaultFavorites.removeAll(favorites);
        favorites.addAll(0, defaultFavorites);
        favorites.removeIf(FavoriteEntry::isInvalid);
        return favorites;
    }
    
    public void remove(FavoriteEntry entry) {
        ConfigObjectImpl config = ConfigManagerImpl.getInstance().getConfig();
        
        config.getFavoriteEntries().remove(entry);
        if (getDefaultFavorites().anyMatch(e -> e.equals(entry)) && !config.getHiddenFavoriteEntries().contains(entry)) {
            config.getHiddenFavoriteEntries().add(entry);
        }
        
        ConfigManager.getInstance().saveConfig();
        FavoritesListWidget widget = ScreenOverlayImpl.getFavoritesListWidget();
        if (widget != null) {
            widget.updateSearch();
        }
    }
    
    public void add(FavoriteEntry entry) {
        ConfigObjectImpl config = ConfigManagerImpl.getInstance().getConfig();
        List<FavoriteEntry> defaultFavorites = getDefaultFavorites().toList();
        
        config.getFavoriteEntries().remove(entry);
        if (CollectionUtils.anyMatch(defaultFavorites, e -> e.equals(entry)) && !config.getHiddenFavoriteEntries().contains(entry)) {
            config.getHiddenFavoriteEntries().add(entry);
        }
        
        for (int i = defaultFavorites.size() - 1; i >= 0; i--) {
            FavoriteEntry e = defaultFavorites.get(i);
            if (!config.getFavoriteEntries().contains(e) && !config.getHiddenFavoriteEntries().contains(e)) {
                config.getFavoriteEntries().add(0, e);
            }
        }
        
        config.getHiddenFavoriteEntries().remove(entry);
        if (!CollectionUtils.anyMatch(defaultFavorites, e -> e.equals(entry))) {
            config.getFavoriteEntries().add(entry);
        }
        
        ConfigManager.getInstance().saveConfig();
        FavoritesListWidget widget = ScreenOverlayImpl.getFavoritesListWidget();
        if (widget != null) {
            widget.updateSearch();
        }
    }
    
    public void setEntries(List<FavoriteEntry> entries) {
        List<FavoriteEntry> defaultFavorites = getDefaultFavorites().collect(Collectors.toList());
        List<FavoriteEntry> hiddenDefaultFavorites = new ArrayList<>(defaultFavorites);
        hiddenDefaultFavorites.removeAll(entries);
        ConfigObjectImpl config = ConfigManagerImpl.getInstance().getConfig();
        config.getHiddenFavoriteEntries().clear();
        config.getHiddenFavoriteEntries().addAll(hiddenDefaultFavorites);
        config.getFavoriteEntries().clear();
        config.getFavoriteEntries().addAll(entries);
        
        ConfigManager.getInstance().saveConfig();
        FavoritesListWidget widget = ScreenOverlayImpl.getFavoritesListWidget();
        if (widget != null) {
            widget.updateSearch();
        }
    }
}
