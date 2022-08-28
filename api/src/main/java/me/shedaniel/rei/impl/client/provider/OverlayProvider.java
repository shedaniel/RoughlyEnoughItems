package me.shedaniel.rei.impl.client.provider;

import me.shedaniel.rei.api.client.overlay.ScreenOverlay;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public interface OverlayProvider {
    ScreenOverlay provide();
}
