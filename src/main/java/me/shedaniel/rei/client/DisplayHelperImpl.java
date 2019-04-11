package me.shedaniel.rei.client;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import me.shedaniel.rei.api.DisplayHelper;

import java.awt.*;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class DisplayHelperImpl implements DisplayHelper {
    
    private static final Comparator BOUNDS_HANDLER_COMPARATOR = Comparator.comparingDouble(value -> {
        if (value instanceof DisplayBoundsHandler)
            return (double) ((DisplayBoundsHandler) value).getPriority();
        return -Double.MAX_VALUE;
    }).reversed();
    private static final DisplayBoundsHandler EMPTY = new DisplayBoundsHandler() {
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
    };
    private List<DisplayBoundsHandler> screenDisplayBoundsHandlerMap = Lists.newArrayList();
    private Map<Class, DisplayBoundsHandler> handlerCache = Maps.newHashMap();
    
    @Override
    public List<DisplayBoundsHandler> getSortedBoundsHandlers(Class screenClass) {
        List<DisplayBoundsHandler> list = Lists.newArrayList(screenDisplayBoundsHandlerMap.stream().filter(handler -> handler.getBaseSupportedClass().isAssignableFrom(screenClass)).collect(Collectors.toList()));
        list.sort(BOUNDS_HANDLER_COMPARATOR);
        return list;
    }
    
    @Override
    public DisplayBoundsHandler getResponsibleBoundsHandler(Class screenClass) {
        Optional<DisplayBoundsHandler> handler = handlerCache.entrySet().stream().filter(entry -> entry.getKey().equals(screenClass)).map(Map.Entry::getValue).findAny();
        if (handler.isPresent())
            return handler.get();
        List<DisplayBoundsHandler> sortedBoundsHandlers = getSortedBoundsHandlers(screenClass);
        handlerCache.put(screenClass, sortedBoundsHandlers.isEmpty() ? EMPTY : sortedBoundsHandlers.get(0));
        return handlerCache.get(screenClass);
    }
    
    @Override
    public void registerBoundsHandler(DisplayBoundsHandler handler) {
        screenDisplayBoundsHandlerMap.add(handler);
    }
    
    public void resetCache() {
        handlerCache.clear();
    }
    
}
