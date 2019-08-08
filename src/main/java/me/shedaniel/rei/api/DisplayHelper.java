/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.api;

import me.shedaniel.rei.RoughlyEnoughItemsCore;
import net.minecraft.util.ActionResult;

import java.awt.*;
import java.util.List;

import static net.minecraft.util.ActionResult.PASS;

public interface DisplayHelper {
    
    /**
     * Gets the sorted version of all responsible bounds handlers
     *
     * @param screenClass the class for checking responsible bounds handlers
     * @return the sorted list of responsible bounds handlers
     * @see DisplayHelper#getResponsibleBoundsHandler(Class) for the unsorted version
     */
    List<DisplayBoundsHandler<?>> getSortedBoundsHandlers(Class<?> screenClass);
    
    /**
     * Gets all registered bounds handlers
     *
     * @return the list of registered bounds handlers
     */
    List<DisplayBoundsHandler<?>> getAllBoundsHandlers();
    
    /**
     * Gets all responsible bounds handlers
     *
     * @param screenClass the class for checking responsible bounds handlers
     * @return the the list of responsible bounds handlers
     * @see DisplayHelper#getSortedBoundsHandlers(Class) for the sorted version
     */
    DisplayBoundsHandler<?> getResponsibleBoundsHandler(Class<?> screenClass);
    
    /**
     * Registers a bounds handler
     *
     * @param handler the handler to register
     */
    void registerBoundsHandler(DisplayBoundsHandler<?> handler);
    
    /**
     * Gets the base bounds handler api for exclusion zones
     *
     * @return the base bounds handler
     */
    BaseBoundsHandler getBaseBoundsHandler();
    
    public static interface DisplayBoundsHandler<T> {
        /**
         * Gets the base supported class for the bounds handler
         *
         * @return the base class
         */
        Class<?> getBaseSupportedClass();
        
        /**
         * Gets the left bounds of the overlay
         *
         * @param screen the current screen
         * @return the left bounds
         */
        Rectangle getLeftBounds(T screen);
        
        /**
         * Gets the right bounds of the overlay
         *
         * @param screen the current screen
         * @return the right bounds
         */
        Rectangle getRightBounds(T screen);
        
        /**
         * Checks if item slot can fit the screen
         *
         * @param isOnRightSide whether the user has set the overlay to the right
         * @param left          the left x coordinates of the stack
         * @param top           the top y coordinates for the stack
         * @param screen        the current screen
         * @param fullBounds    the current bounds
         * @return whether the item slot can fit
         * @see BaseBoundsHandler#registerExclusionZones(Class, BaseBoundsHandler.ExclusionZoneSupplier) for easier api
         */
        default ActionResult canItemSlotWidgetFit(boolean isOnRightSide, int left, int top, T screen, Rectangle fullBounds) {
            return PASS;
        }
        
        /**
         * Checks if mouse is inside the overlay
         *
         * @param isOnRightSide whether the user has set the overlay to the right
         * @param mouseX        mouse's x coordinates
         * @param mouseY        mouse's y coordinates
         * @return whether mouse is inside the overlay
         */
        default ActionResult isInZone(boolean isOnRightSide, double mouseX, double mouseY) {
            return PASS;
        }
        
        /**
         * Gets the item list bounds by the overlay bounds
         *
         * @param rectangle the overlay bounds
         * @return the item list bounds
         */
        default Rectangle getItemListArea(Rectangle rectangle) {
            return new Rectangle(rectangle.x + 2, rectangle.y + 24, rectangle.width - 4, rectangle.height - (RoughlyEnoughItemsCore.getConfigManager().getConfig().isSideSearchField() ? 27 + 22 : 27));
        }
        
        /**
         * Checks if REI should recalculate the overlay bounds
         *
         * @param isOnRightSide whether the user has set the overlay to the right
         * @param rectangle     the current overlay bounds
         * @return whether REI should recalculate the overlay bounds
         */
        default boolean shouldRecalculateArea(boolean isOnRightSide, Rectangle rectangle) {
            return false;
        }
        
        /**
         * Gets the priority of the handler, the higher it is, the earlier it is called.
         *
         * @return the priority in float
         */
        default float getPriority() {
            return 0f;
        }
    }
    
}
