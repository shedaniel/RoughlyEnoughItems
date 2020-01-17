/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import me.shedaniel.math.api.Rectangle;
import me.shedaniel.rei.api.BaseBoundsHandler;
import me.shedaniel.rei.api.DisplayHelper;
import org.jetbrains.annotations.ApiStatus;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ApiStatus.Internal
public class DisplayHelperImpl implements DisplayHelper {
    
    private static final Comparator<DisplayBoundsHandler<?>> BOUNDS_HANDLER_COMPARATOR;
    private static final DisplayBoundsHandler<Object> EMPTY = new DisplayBoundsHandler<Object>() {
        @Override
        public Class<Object> getBaseSupportedClass() {
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
    private Map<Class<?>, List<DisplayBoundsHandler<?>>> handlerSortedCache = Maps.newHashMap();
    private BaseBoundsHandler baseBoundsHandler;
    private Class<?> tempScreen;
    
    @Override
    public List<DisplayBoundsHandler<?>> getSortedBoundsHandlers(Class<?> screenClass) {
        List<DisplayBoundsHandler<?>> possibleCached = handlerSortedCache.get(screenClass);
        if (possibleCached != null)
            return possibleCached;
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
        DisplayBoundsHandler<?> possibleCached = handlerCache.get(screenClass);
        if (possibleCached != null)
            return possibleCached;
        List<DisplayBoundsHandler<?>> handlers = getSortedBoundsHandlers(screenClass);
        handlerCache.put(screenClass, handlers.isEmpty() ? EMPTY : handlers.get(0));
        return handlerCache.get(screenClass);
    }
    
    @Deprecated
    public boolean filterResponsible(DisplayBoundsHandler<?> handler) {
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
    
    @ApiStatus.Internal
    public void setBaseBoundsHandler(BaseBoundsHandler baseBoundsHandler) {
        this.baseBoundsHandler = baseBoundsHandler;
    }
    
    @ApiStatus.Internal
    public void resetData() {
        screenDisplayBoundsHandlers.clear();
    }
    
    @ApiStatus.Internal
    public void resetCache() {
        handlerCache.clear();
        handlerSortedCache.clear();
    }
    
}
