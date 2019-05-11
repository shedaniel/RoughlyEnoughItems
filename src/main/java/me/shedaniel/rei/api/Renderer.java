/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.api;

import net.minecraft.client.gui.DrawableHelper;

public abstract class Renderer extends DrawableHelper implements Renderable {
    public int getBlitOffset() {
        return this.blitOffset;
    }
    
    public void setBlitOffset(int offset) {
        this.blitOffset = offset;
    }
}
