/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.gui.widget;

import me.shedaniel.reiclothconfig2.api.MouseUtils;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IGuiEventListener;

import java.awt.*;

public abstract class DraggableWidget extends WidgetWithBounds {
    
    public boolean dragged = false;
    private Point midPoint, startPoint;
    private int relateX, relateY;
    
    public DraggableWidget(Point startingPoint) {
        initWidgets(midPoint = startingPoint);
    }
    
    public DraggableWidget() {
        this(new Point(Minecraft.getInstance().mainWindow.getScaledWidth() / 2, Minecraft.getInstance().mainWindow.getScaledHeight() / 2));
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
        Point mouse = MouseUtils.getMouseLocation();
        if (int_1 == 0) {
            if (!dragged) {
                if (getGrabBounds().contains(mouse)) {
                    startPoint = new Point(midPoint.x, midPoint.y);
                    relateX = mouse.x - midPoint.x;
                    relateY = mouse.y - midPoint.y;
                    dragged = true;
                }
            } else {
                MainWindow window = minecraft.mainWindow;
                midPoint = processMidPoint(midPoint, mouse, startPoint, window, relateX, relateY);
                updateWidgets(midPoint);
            }
            return true;
        }
        for(IGuiEventListener listener : getChildren())
            if (listener.mouseDragged(double_1, double_2, int_1, double_3, double_4))
                return true;
        return false;
    }
    
    public abstract Point processMidPoint(Point midPoint, Point mouse, Point startPoint, MainWindow window, int relateX, int relateY);
    
    @Override
    public boolean mouseReleased(double double_1, double double_2, int int_1) {
        if (int_1 == 0)
            if (dragged) {
                dragged = false;
                onMouseReleaseMidPoint(getMidPoint());
                return true;
            }
        for(IGuiEventListener listener : getChildren())
            if (listener.mouseReleased(double_1, double_2, int_1))
                return true;
        return false;
    }
    
    public void onMouseReleaseMidPoint(Point midPoint) {}
    
}
