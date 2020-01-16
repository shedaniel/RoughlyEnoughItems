/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.api;

import me.shedaniel.math.api.Rectangle;
import me.shedaniel.rei.api.annotations.Internal;

@Internal
@Deprecated
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
