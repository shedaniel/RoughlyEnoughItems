package me.shedaniel.rei.api.registry.screens;

import me.shedaniel.math.Rectangle;

public interface DisplayBoundsProvider<T> extends OverlayDecider {
    /**
     * @param screen the screen
     * @return the boundary of the base container panel.
     */
    Rectangle getScreenBounds(T screen);
    
    /**
     * Gets the base supported class for the bounds handler
     *
     * @return the base class
     */
    Class<?> getBaseSupportedClass();
    
    @Override
    default boolean isHandingScreen(Class<?> screen) {
        return getBaseSupportedClass().isAssignableFrom(screen);
    }
}