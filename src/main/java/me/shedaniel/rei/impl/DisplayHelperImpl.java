/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import me.shedaniel.rei.api.BaseBoundsHandler;
import me.shedaniel.rei.api.DisplayHelper;

import java.awt.*;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DisplayHelperImpl implements DisplayHelper {
    
    private static final Comparator<DisplayBoundsHandler<?>> BOUNDS_HANDLER_COMPARATOR;
    private static final DisplayBoundsHandler<Object> EMPTY = new DisplayBoundsHandler() {
        @Override
        public Class getBaseSupportedClass() {
            return null;
        }
        
        @Override
        public Rectangle getLeftBounds(Object screen) {
            return new Rectangle();
        }
        
        @Override
        public Rectangle getRightBounds(Object screen) {
            return new Rectangle();
        }
        
        @Override
        public float getPriority() {
            return -10f;
        }
    };
    
    static {
        Comparator<DisplayBoundsHandler<?>> comparator = Comparator.comparingDouble(DisplayBoundsHandler::getPriority);
        BOUNDS_HANDLER_COMPARATOR = comparator.reversed();
    }
    
    private List<DisplayBoundsHandler<?>> screenDisplayBoundsHandlers = Lists.newArrayList();
    private Map<Class<?>, DisplayBoundsHandler<?>> handlerCache = Maps.newHashMap();
    private Map<Class, List<DisplayBoundsHandler<?>>> handlerSortedCache = Maps.newHashMap();
    private BaseBoundsHandler baseBoundsHandler;
    private Class tempScreen;
    
    @Override
    public List<DisplayBoundsHandler<?>> getSortedBoundsHandlers(Class<?> screenClass) {
        if (handlerSortedCache.containsKey(screenClass))
            return handlerSortedCache.get(screenClass);
        tempScreen = screenClass;
        handlerSortedCache.put(screenClass, screenDisplayBoundsHandlers.stream().filter(this::filterResponsible).sorted(BOUNDS_HANDLER_COMPARATOR).collect(Collectors.toList()));
        return handlerSortedCache.get(screenClass);
    }
    
    @Override
    public List<DisplayBoundsHandler<?>> getAllBoundsHandlers() {
        return screenDisplayBoundsHandlers;
    }
    
    @Override
    public DisplayBoundsHandler<?> getResponsibleBoundsHandler(Class<?> screenClass) {
        if (handlerCache.containsKey(screenClass))
            return handlerCache.get(screenClass);
        tempScreen = screenClass;
        handlerCache.put(screenClass, screenDisplayBoundsHandlers.stream().filter(this::filterResponsible).sorted(BOUNDS_HANDLER_COMPARATOR).findAny().orElse(EMPTY));
        return handlerCache.get(screenClass);
    }
    
    public boolean filterResponsible(DisplayBoundsHandler handler) {
        return handler.getBaseSupportedClass().isAssignableFrom(tempScreen);
    }
    
    @Override
    public void registerBoundsHandler(DisplayBoundsHandler<?> handler) {
        screenDisplayBoundsHandlers.add(handler);
    }
    
    @Override
    public BaseBoundsHandler getBaseBoundsHandler() {
        return baseBoundsHandler;
    }
    
    public void setBaseBoundsHandler(BaseBoundsHandler baseBoundsHandler) {
        this.baseBoundsHandler = baseBoundsHandler;
    }
    
    public void resetData() {
        screenDisplayBoundsHandlers.clear();
    }
    
    public void resetCache() {
        handlerCache.clear();
        handlerSortedCache.clear();
    }
    
}
