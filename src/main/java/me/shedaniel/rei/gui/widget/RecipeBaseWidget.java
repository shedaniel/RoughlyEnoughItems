/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.gui.widget;

import me.shedaniel.math.api.Rectangle;
import me.shedaniel.rei.api.ConfigObject;

public class RecipeBaseWidget extends PanelWidget {
    
    public RecipeBaseWidget(Rectangle bounds) {
        super(bounds);
    }
    
    @Override
    protected int getYTextureOffset() {
        return ConfigObject.getInstance().isUsingLightGrayRecipeBorder() ? 0 : 66;
    }
    
}
