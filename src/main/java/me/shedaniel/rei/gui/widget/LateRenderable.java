/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.gui.widget;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public interface LateRenderable {
    void lateRender(int mouseX, int mouseY, float delta);
}
