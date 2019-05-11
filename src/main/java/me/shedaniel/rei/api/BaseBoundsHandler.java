/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.api;

import net.minecraft.client.gui.Screen;

import java.awt.*;
import java.util.List;

public interface BaseBoundsHandler extends DisplayHelper.DisplayBoundsHandler<Screen> {
    List<Rectangle> getCurrentExclusionZones(Class<? extends Screen> currentScreenClass, boolean isOnRightSide);
    
    void registerExclusionZones(Class<? extends Screen> screenClass, ExclusionZoneSupplier supplier);
    
    public static interface ExclusionZoneSupplier {
        List<Rectangle> apply(boolean isOnRightSide);
    }
}
