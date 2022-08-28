package me.shedaniel.rei.impl.client.gui.overlay;

import me.shedaniel.rei.api.client.overlay.ScreenOverlay;
import me.shedaniel.rei.impl.client.provider.OverlayProvider;

public class ScreenOverlayProviderImpl implements OverlayProvider {
    @Override
    public ScreenOverlay provide() {
        return new ScreenOverlayImpl();
    }
}
