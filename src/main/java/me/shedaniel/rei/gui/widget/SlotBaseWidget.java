/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.gui.widget;

import java.awt.*;

public class SlotBaseWidget extends RecipeBaseWidget {
    
    public SlotBaseWidget(Rectangle bounds) {
        super(bounds);
    }
    
    @Override
    public int getInnerColor() {
        return -7631989;
    }
    
    @Override
    protected int getTextureOffset() {
        return -66;
    }
    
    @Override
    protected boolean isRendering() {
        return true;
    }
    
}
