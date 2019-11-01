/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.gui.widget;

import me.shedaniel.math.api.Rectangle;
import me.shedaniel.rei.api.annotations.ToBeRemoved;
import net.minecraft.text.Text;

@ToBeRemoved
@Deprecated
public class DetailedButtonWidget extends ButtonWidget {
    
    private DetailedButtonWidget.PressAction pressAction;
    
    public DetailedButtonWidget(Rectangle rectangle, Text text, PressAction pressAction) {
        super(rectangle, text);
        this.pressAction = pressAction;
    }
    
    public DetailedButtonWidget(Rectangle rectangle, String text, PressAction pressAction) {
        super(rectangle, text);
        this.pressAction = pressAction;
    }
    
    public DetailedButtonWidget(int x, int y, int width, int height, String text, PressAction pressAction) {
        super(x, y, width, height, text);
        this.pressAction = pressAction;
    }
    
    public DetailedButtonWidget(int x, int y, int width, int height, Text text, PressAction pressAction) {
        super(x, y, width, height, text);
        this.pressAction = pressAction;
    }
    
    @Override
    public void onPressed() {
        if (pressAction != null)
            pressAction.onPress(this);
    }
    
    public interface PressAction {
        void onPress(ButtonWidget var1);
    }
    
}
