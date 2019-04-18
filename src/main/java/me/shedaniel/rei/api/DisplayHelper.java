package me.shedaniel.rei.api;

import me.shedaniel.rei.RoughlyEnoughItemsCore;
import net.minecraft.util.ActionResult;

import java.awt.*;
import java.util.List;

import static net.minecraft.util.ActionResult.PASS;

public interface DisplayHelper {
    
    List<DisplayBoundsHandler> getSortedBoundsHandlers(Class screenClass);
    
    DisplayBoundsHandler getResponsibleBoundsHandler(Class screenClass);
    
    void registerBoundsHandler(DisplayBoundsHandler handler);
    
    public static interface DisplayBoundsHandler<T> {
        Class getBaseSupportedClass();
        
        Rectangle getLeftBounds(T screen);
        
        Rectangle getRightBounds(T screen);
        
        default ActionResult canItemSlotWidgetFit(boolean isOnRightSide, int left, int top, T screen, Rectangle fullBounds) {
            return PASS;
        }
        
        default Rectangle getItemListArea(Rectangle rectangle) {
            return new Rectangle(rectangle.x + 2, rectangle.y + 24, rectangle.width - 4, rectangle.height - (RoughlyEnoughItemsCore.getConfigManager().getConfig().sideSearchField ? 27 + 22 : 27));
        }
        
        default float getPriority() {
            return 0f;
        }
    }
    
}
