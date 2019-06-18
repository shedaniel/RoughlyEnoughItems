/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.api;

import net.minecraft.client.gui.DrawableHelper;

public abstract class Renderer extends DrawableHelper {
    /**
     * Gets the current blit offset
     *
     * @return the blit offset
     */
    public int getBlitOffset() {
        return this.blitOffset;
    }
    
    /**
     * Sets the current blit offset
     *
     * @param offset the new blit offset
     */
    public void setBlitOffset(int offset) {
        this.blitOffset = offset;
    }
    
    /**
     * Renders of the renderable
     *
     * @param x      the x coordinate of the renderable
     * @param y      the y coordinate of the renderable
     * @param mouseX the x coordinate of the mouse
     * @param mouseY the y coordinate of the mouse
     * @param delta  the delta
     */
    public abstract void render(int x, int y, double mouseX, double mouseY, float delta);
}
