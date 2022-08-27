package me.shedaniel.rei.api.client.gui.widgets;

import me.shedaniel.rei.impl.client.ClientInternals;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

@ApiStatus.Experimental
public interface TooltipQueue {
    static TooltipQueue getInstance() {
        return ClientInternals.getTooltipQueue();
    }
    
    void queue(@Nullable Tooltip tooltip);
    
    void clear();
    
    @Nullable
    Tooltip get();
}
