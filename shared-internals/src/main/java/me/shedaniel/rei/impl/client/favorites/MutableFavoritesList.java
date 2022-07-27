package me.shedaniel.rei.impl.client.favorites;

import me.shedaniel.rei.api.client.favorites.FavoriteEntry;

import java.util.List;

public interface MutableFavoritesList extends List<FavoriteEntry> {
    void setAll(List<FavoriteEntry> entries);
}
