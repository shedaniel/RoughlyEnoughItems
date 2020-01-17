/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.gui.widget;

import net.minecraft.client.gui.widget.ButtonWidget;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class ReloadConfigButtonWidget extends ButtonWidget {
    public ReloadConfigButtonWidget(int x, int y, int width, int height, String text, PressAction action) {
        super(x, y, width, height, text, action);
    }
}
