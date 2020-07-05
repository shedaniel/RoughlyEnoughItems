/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020 shedaniel
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package me.shedaniel.rei.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.BaseBoundsHandler;
import me.shedaniel.rei.api.DisplayHelper;
import me.shedaniel.rei.api.OverlayDecider;
import me.shedaniel.rei.gui.config.DisplayPanelLocation;
import me.shedaniel.rei.utils.CollectionUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.Window;
import org.jetbrains.annotations.ApiStatus;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@ApiStatus.Internal
@Environment(EnvType.CLIENT)
public class DisplayHelperImpl implements DisplayHelper {
    
    private static final Comparator<OverlayDecider> BOUNDS_HANDLER_COMPARATOR = Comparator.comparingDouble(OverlayDecider::getPriority).reversed();
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
    
    private List<OverlayDecider> screenDisplayBoundsHandlers = Lists.newArrayList();
    private Map<Class<?>, DisplayBoundsHandler<?>> handlerCache = Maps.newHashMap();
    private Map<Class<?>, List<OverlayDecider>> deciderSortedCache = Maps.newHashMap();
    private Map<Class<?>, List<DisplayBoundsHandler<?>>> handlerSortedCache = Maps.newHashMap();
    private BaseBoundsHandler baseBoundsHandler;
    private Class<?> tempScreen;
    
    @SuppressWarnings("rawtypes")
    @Override
    public List<DisplayBoundsHandler<?>> getSortedBoundsHandlers(Class<?> screenClass) {
        List<DisplayBoundsHandler<?>> possibleCached = handlerSortedCache.get(screenClass);
        if (possibleCached != null)
            return possibleCached;
        tempScreen = screenClass;
        handlerSortedCache.put(screenClass, (List) CollectionUtils.castAndMap(CollectionUtils.filter(screenDisplayBoundsHandlers, this::filterResponsible), DisplayBoundsHandler.class));
        return handlerSortedCache.get(screenClass);
    }
    
    @Override
    public List<OverlayDecider> getSortedOverlayDeciders(Class<?> screenClass) {
        List<OverlayDecider> possibleCached = deciderSortedCache.get(screenClass);
        if (possibleCached != null)
            return possibleCached;
        tempScreen = screenClass;
        deciderSortedCache.put(screenClass, (List) CollectionUtils.filter(screenDisplayBoundsHandlers, this::filterResponsible));
        return deciderSortedCache.get(screenClass);
    }
    
    @Override
    public List<OverlayDecider> getAllOverlayDeciders() {
        return Collections.unmodifiableList(screenDisplayBoundsHandlers);
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
    
    @Override
    public <T> Rectangle getOverlayBounds(DisplayPanelLocation location, T screen) {
        Window window = MinecraftClient.getInstance().getWindow();
        int scaledWidth = window.getScaledWidth();
        int scaledHeight = window.getScaledHeight();
        for (OverlayDecider decider : getSortedOverlayDeciders(screen.getClass())) {
            if (decider instanceof DisplayBoundsProvider) {
                Rectangle containerBounds = ((DisplayBoundsProvider<T>) decider).getScreenBounds(screen);
                if (location == DisplayPanelLocation.LEFT) {
                    if (containerBounds.x < 10) continue;
                    return new Rectangle(2, 0, containerBounds.x - 2, scaledHeight);
                } else {
                    if (scaledWidth - containerBounds.getMaxX() < 10) continue;
                    return new Rectangle(containerBounds.getMaxX() + 2, 0, scaledWidth - containerBounds.getMaxX() - 4, scaledHeight);
                }
            } else if (decider instanceof DisplayBoundsHandler) {
                return location == DisplayPanelLocation.LEFT ? ((DisplayBoundsHandler<T>) decider).getLeftBounds(screen) : ((DisplayBoundsHandler<T>) decider).getRightBounds(screen);
            }
        }
        return new Rectangle();
    }
    
    private boolean filterResponsible(OverlayDecider handler) {
        return handler.isHandingScreen(tempScreen);
    }
    
    @Override
    public void registerHandler(OverlayDecider decider) {
        screenDisplayBoundsHandlers.add(decider);
        screenDisplayBoundsHandlers.sort(BOUNDS_HANDLER_COMPARATOR);
    }
    
    @ApiStatus.Internal
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
