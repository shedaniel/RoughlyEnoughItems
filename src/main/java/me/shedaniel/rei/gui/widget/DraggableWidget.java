package me.shedaniel.rei.gui.widget;

import me.shedaniel.rei.client.ClientHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.Window;

import java.awt.*;

public abstract class DraggableWidget implements HighlightableWidget {
    
    protected boolean dragged = false;
    private Point midPoint, startPoint;
    private int relateX, relateY;
    
    public DraggableWidget() {
        Window window = MinecraftClient.getInstance().window;
        initWidgets(midPoint = new Point(window.getScaledWidth() / 2, window.getScaledHeight() / 2));
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
        Point mouse = ClientHelper.getMouseLocation();
        if (int_1 == 0) {
            if (!dragged) {
                if (getGrabBounds().contains(mouse)) {
                    startPoint = new Point(midPoint.x, midPoint.y);
                    relateX = mouse.x - midPoint.x;
                    relateY = mouse.y - midPoint.y;
                    dragged = true;
                }
            } else {
                Window window = MinecraftClient.getInstance().window;
                midPoint = processMidPoint(midPoint, mouse, startPoint, window, relateX, relateY);
                updateWidgets(midPoint);
            }
            return true;
        }
        for(IWidget widget : getListeners())
            if (widget.mouseDragged(double_1, double_2, int_1, double_3, double_4))
                return true;
        return false;
    }
    
    public abstract Point processMidPoint(Point midPoint, Point mouse, Point startPoint, Window window, int relateX, int relateY);
    
    @Override
    public boolean mouseReleased(double double_1, double double_2, int int_1) {
        if (int_1 == 0)
            if (dragged) {
                dragged = false;
                return true;
            }
        for(IWidget widget : getListeners())
            if (widget.mouseReleased(double_1, double_2, int_1))
                return true;
        return false;
    }
    
}
