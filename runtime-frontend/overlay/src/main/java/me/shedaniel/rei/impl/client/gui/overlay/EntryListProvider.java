package me.shedaniel.rei.impl.client.gui.overlay;

import me.shedaniel.rei.api.client.overlay.OverlayListWidget;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public interface EntryListProvider {
    OverlayListWidget getEntryList();
}
