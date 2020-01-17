/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.api;

import me.shedaniel.math.api.Rectangle;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public interface OptimalEntryStack {
    default void optimisedRenderStart(float delta) {
    }
    
    default void optimisedRenderBase(Rectangle bounds, int mouseX, int mouseY, float delta) {
    }
    
    default void optimisedRenderOverlay(Rectangle bounds, int mouseX, int mouseY, float delta) {
    }
    
    default void optimisedRenderEnd(float delta) {
    }
}
