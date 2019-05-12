/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.api;

import net.minecraft.client.gui.DrawableHelper;

public abstract class Renderer extends DrawableHelper implements Renderable {
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
}
