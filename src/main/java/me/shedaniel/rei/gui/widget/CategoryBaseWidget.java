/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.gui.widget;

import java.awt.*;

public class CategoryBaseWidget extends RecipeBaseWidget {
    
    public CategoryBaseWidget(Rectangle bounds) {
        super(bounds);
    }
    
    @Override
    protected int getTextureOffset() {
        return 66;
    }
    
    @Override
    protected boolean isRendering() {
        return true;
    }
    
}
