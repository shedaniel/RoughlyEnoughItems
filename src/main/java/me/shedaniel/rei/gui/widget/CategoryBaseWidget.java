/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.gui.widget;

import me.shedaniel.math.api.Rectangle;

public class CategoryBaseWidget extends RecipeBaseWidget {

    public CategoryBaseWidget(Rectangle bounds) {
        super(bounds);
    }

    @Override
    protected int getYTextureOffset() {
        return 66;
    }

    @Override
    protected boolean isRendering() {
        return true;
    }

}
