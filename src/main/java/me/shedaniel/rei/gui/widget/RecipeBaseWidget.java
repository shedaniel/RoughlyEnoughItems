/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.gui.widget;

import me.shedaniel.math.api.Rectangle;
import me.shedaniel.rei.RoughlyEnoughItemsCore;
import me.shedaniel.rei.api.annotations.ToBeRemoved;

public class RecipeBaseWidget extends PanelWidget {
    
    public RecipeBaseWidget(Rectangle bounds) {
        super(bounds);
    }
    
    @Override
    protected int getYTextureOffset() {
        return getTextureOffset();
    }
    
    @ToBeRemoved
    @Deprecated
    protected int getTextureOffset() {
        return RoughlyEnoughItemsCore.getConfigManager().getConfig().isUsingLightGrayRecipeBorder() ? 0 : 66;
    }
    
}
