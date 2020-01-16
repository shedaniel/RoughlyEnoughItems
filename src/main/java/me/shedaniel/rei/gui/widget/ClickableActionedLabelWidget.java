/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.gui.widget;

import me.shedaniel.math.api.Point;
import me.shedaniel.rei.api.annotations.Internal;

import java.util.function.Consumer;

@Deprecated
@Internal
public class ClickableActionedLabelWidget extends ClickableLabelWidget {
    private Consumer<ClickableLabelWidget> onClicked;
    
    ClickableActionedLabelWidget(Point point, String text, Consumer<ClickableLabelWidget> onClicked) {
        super(point, text);
        this.onClicked = onClicked;
    }
    
    @Override
    public void onLabelClicked() {
        onClicked.accept(this);
    }
}
