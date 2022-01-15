/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020, 2021, 2022 shedaniel
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

package me.shedaniel.rei.impl.client.registry.screen;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.RoughlyEnoughItemsCore;
import me.shedaniel.rei.api.client.gui.config.DisplayPanelLocation;
import me.shedaniel.rei.api.client.registry.screen.ExclusionZones;
import me.shedaniel.rei.api.client.registry.screen.ExclusionZonesProvider;
import me.shedaniel.rei.api.common.plugins.PluginManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.InteractionResult;
import org.jetbrains.annotations.ApiStatus;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@ApiStatus.Internal
@Environment(EnvType.CLIENT)
public class ExclusionZonesImpl implements ExclusionZones {
    private static final Comparator<? super Rectangle> RECTANGLE_COMPARER = Comparator.comparingLong(Rectangle::hashCode);
    
    private long lastArea = -1;
    private final Multimap<Class<?>, Function<Screen, Collection<Rectangle>>> list = HashMultimap.create();
    
    @Override
    public <R extends Screen> boolean isHandingScreen(Class<R> screen) {
        return Screen.class.isAssignableFrom(screen);
    }
    
    @Override
    public double getPriority() {
        return -5.0;
    }
    
    @Override
    public InteractionResult isInZone(double mouseX, double mouseY) {
        Screen screen = Minecraft.getInstance().screen;
        Class<? extends Screen> screenClass = screen.getClass();
        
        synchronized (list) {
            for (Map.Entry<Class<?>, Collection<Function<Screen, Collection<Rectangle>>>> collectionEntry : list.asMap().entrySet()) {
                if (collectionEntry.getKey().isAssignableFrom(screenClass)) {
                    for (Function<Screen, Collection<Rectangle>> listSupplier : collectionEntry.getValue()) {
                        for (Rectangle zone : listSupplier.apply(screen)) {
                            if (zone.contains(mouseX, mouseY)) {
                                return InteractionResult.FAIL;
                            }
                        }
                    }
                }
            }
        }
        
        return InteractionResult.PASS;
    }
    
    @Override
    public boolean shouldRecalculateArea(DisplayPanelLocation location, Rectangle rectangle) {
        long current = currentHashCode(location);
        if (lastArea == current)
            return false;
        lastArea = current;
        return true;
    }
    
    private long currentHashCode(DisplayPanelLocation location) {
        return areasHashCode(getExclusionZones(Minecraft.getInstance().screen, false));
    }
    
    @Override
    @Deprecated
    public List<Rectangle> getExclusionZones(Class<?> currentScreenClass, boolean sort) {
        return getExclusionZones(currentScreenClass, Minecraft.getInstance().screen, sort);
    }
    
    @Override
    public List<Rectangle> getExclusionZones(Screen screen, boolean sort) {
        if (screen == null) return Lists.newArrayList();
        return getExclusionZones(screen.getClass(), screen, sort);
    }
    
    public List<Rectangle> getExclusionZones(Class<?> screenClass, Screen screen, boolean sort) {
        if (screen == null || !screenClass.isAssignableFrom(screen.getClass())) {
            return Lists.newArrayList();
        }
        
        List<Rectangle> rectangles = Lists.newArrayList();
        synchronized (list) {
            for (Map.Entry<Class<?>, Collection<Function<Screen, Collection<Rectangle>>>> collectionEntry : list.asMap().entrySet()) {
                if (collectionEntry.getKey().isAssignableFrom(screenClass)) {
                    for (Function<Screen, Collection<Rectangle>> listSupplier : collectionEntry.getValue()) {
                        rectangles.addAll(listSupplier.apply(screen));
                    }
                }
            }
        }
        if (sort) {
            rectangles.sort(RECTANGLE_COMPARER);
        }
        return rectangles;
    }
    
    @Override
    public int getZonesCount() {
        return list.size();
    }
    
    @Override
    public <T> void register(Class<? extends T> screenClass, ExclusionZonesProvider<? extends T> provider) {
        synchronized (list) {
            list.put(screenClass, screen -> ((ExclusionZonesProvider<T>) provider).provide((T) screen));
        }
        
        if (!PluginManager.areAnyReloading()) {
            RoughlyEnoughItemsCore.LOGGER.warn("Detected ExclusionZonesImpl modification at runtime, this may cause issues, a single ExclusionZonesProvider can dynamically provide boundaries instead!", new RuntimeException());
        }
    }
    
    private long areasHashCode(List<Rectangle> exclusionZones) {
        int hashCode = 31;
        for (Rectangle e : exclusionZones)
            hashCode = 31 * hashCode + (e == null ? 0 : e.hashCode());
        return hashCode;
    }
    
}
