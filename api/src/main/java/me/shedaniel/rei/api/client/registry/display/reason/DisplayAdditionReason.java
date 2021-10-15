package me.shedaniel.rei.api.client.registry.display.reason;

import org.jetbrains.annotations.ApiStatus;

/**
 * Reason for adding a display, used for {@link me.shedaniel.rei.api.client.registry.display.DisplayRegistry#tryFillDisplay(Object, DisplayAdditionReason...)}
 * Plugins may filter their filler with reasons, this class can be implemented to provide additional context to fillers.
 */
@ApiStatus.Experimental
public interface DisplayAdditionReason {
    DisplayAdditionReason[] NONE = new DisplayAdditionReason[0];
    /**
     * Denotes that the display is added automatically by REI's RecipeManager,
     * fillers which do not wish to be added with this should filter with this.
     */
    DisplayAdditionReason RECIPE_MANAGER = simple();
    
    static DisplayAdditionReason simple() {
        return new DisplayAdditionReason() {};
    }
}
