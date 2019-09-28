/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.gui.widget;

import me.shedaniel.math.api.Point;
import me.shedaniel.math.api.Rectangle;
import me.shedaniel.math.impl.PointHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.util.Window;

public abstract class DraggableWidget extends WidgetWithBounds {
    
    public boolean dragged = false;
    private Point midPoint, startPoint;
    private int relateX, relateY;
    
    public DraggableWidget(Point startingPoint) {
        initWidgets(midPoint = startingPoint);
    }
    
    public DraggableWidget() {
        this(new Point(MinecraftClient.getInstance().method_22683().getScaledWidth() / 2, MinecraftClient.getInstance().method_22683().getScaledHeight() / 2));
    }
    
    protected abstract void initWidgets(Point midPoint);
    
    public abstract void updateWidgets(Point midPoint);
    
    public abstract Rectangle getGrabBounds();
    
    public abstract Rectangle getDragBounds();
    
    public final Point getMidPoint() {
        return midPoint;
    }
    
    @Override
    public boolean mouseDragged(double double_1, double double_2, int int_1, double double_3, double double_4) {
        Point mouse = PointHelper.fromMouse();
        if (int_1 == 0) {
            if (!dragged) {
                if (getGrabBounds().contains(mouse)) {
                    startPoint = new Point(midPoint.x, midPoint.y);
                    relateX = mouse.x - midPoint.x;
                    relateY = mouse.y - midPoint.y;
                    dragged = true;
                }
            } else {
                Window window = minecraft.method_22683();
                midPoint = processMidPoint(midPoint, mouse, startPoint, window, relateX, relateY);
                updateWidgets(midPoint);
            }
            return true;
        }
        for (Element listener : children())
            if (listener.mouseDragged(double_1, double_2, int_1, double_3, double_4))
                return true;
        return false;
    }
    
    public abstract Point processMidPoint(Point midPoint, Point mouse, Point startPoint, Window window, int relateX, int relateY);
    
    @Override
    public boolean mouseReleased(double double_1, double double_2, int int_1) {
        if (int_1 == 0)
            if (dragged) {
                dragged = false;
                onMouseReleaseMidPoint(getMidPoint());
                return true;
            }
        for (Element listener : children())
            if (listener.mouseReleased(double_1, double_2, int_1))
                return true;
        return false;
    }
    
    public void onMouseReleaseMidPoint(Point midPoint) {
    }
    
}
