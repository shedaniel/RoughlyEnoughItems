/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.gui.widget;

import me.shedaniel.math.api.Rectangle;
import net.minecraft.text.Text;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public abstract class LateRenderedButton extends ButtonWidget implements LateRenderable {
    protected LateRenderedButton(Rectangle rectangle, Text text) {
        super(rectangle, text);
    }
    
    protected LateRenderedButton(Rectangle rectangle, String text) {
        super(rectangle, text);
    }
}
