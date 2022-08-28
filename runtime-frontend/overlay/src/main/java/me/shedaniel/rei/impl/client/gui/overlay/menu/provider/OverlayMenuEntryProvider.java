package me.shedaniel.rei.impl.client.gui.overlay.menu.provider;

import me.shedaniel.rei.api.client.favorites.FavoriteMenuEntry;
import me.shedaniel.rei.impl.client.ClientInternals;

import java.util.List;

public interface OverlayMenuEntryProvider {
    List<OverlayMenuEntryProvider> PROVIDERS = ClientInternals.resolveServices(OverlayMenuEntryProvider.class);
    
    List<FavoriteMenuEntry> provide(Type type);
    
    enum Type {
        CRAFTABLE_FILTER,
        CONFIG,
        ;
    }
}
