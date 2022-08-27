package me.shedaniel.rei.impl.client.gui.overlay;

import me.shedaniel.rei.api.client.overlay.OverlayListWidget;
import org.jetbrains.annotations.ApiStatus;

import java.util.Optional;

@ApiStatus.Internal
public interface FavoritesListProvider {
    Optional<OverlayListWidget> getFavoritesList();
}
