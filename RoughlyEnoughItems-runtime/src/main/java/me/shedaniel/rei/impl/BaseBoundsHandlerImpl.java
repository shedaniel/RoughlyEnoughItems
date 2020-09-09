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
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.BaseBoundsHandler;
import me.shedaniel.rei.api.DisplayHelper;
import me.shedaniel.rei.gui.config.DisplayPanelLocation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Tuple;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.ApiStatus;

import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;

@ApiStatus.Internal
@OnlyIn(Dist.CLIENT)
public class BaseBoundsHandlerImpl implements BaseBoundsHandler {
    
    private static final Comparator<? super Rectangle> RECTANGLE_COMPARER = Comparator.comparingLong(Rectangle::hashCode);
    
    private long lastArea = -1;
    private List<Tuple<Tuple<Class<?>, Float>, Supplier<List<Rectangle>>>> list = Lists.newArrayList();
    
    @Override
    public boolean isHandingScreen(Class<?> screen) {
        return Screen.class.isAssignableFrom(screen);
    }
    
    @Override
    public float getPriority() {
        return -5f;
    }
    
    @Override
    public ActionResultType isInZone(double mouseX, double mouseY) {
        Class<? extends Screen> screenClass = Minecraft.getInstance().screen.getClass();
        for (Tuple<Tuple<Class<?>, Float>, Supplier<List<Rectangle>>> pair : list) {
            if (pair.getA().getA().isAssignableFrom(screenClass))
                for (Rectangle zone : pair.getB().get())
                    if (zone.contains(mouseX, mouseY))
                        return ActionResultType.FAIL;
        }
        return ActionResultType.PASS;
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
        return areasHashCode(DisplayHelper.getInstance().getOverlayBounds(location, Minecraft.getInstance().screen), getExclusionZones(Minecraft.getInstance().screen.getClass(), false));
    }
    
    @Override
    public List<Rectangle> getExclusionZones(Class<?> currentScreenClass, boolean sort) {
        List<Rectangle> rectangles = Lists.newArrayList();
        for (Tuple<Tuple<Class<?>, Float>, Supplier<List<Rectangle>>> pair : list) {
            if (pair.getA().getA().isAssignableFrom(currentScreenClass))
                rectangles.addAll(pair.getB().get());
        }
        if (sort)
            rectangles.sort(RECTANGLE_COMPARER);
        return rectangles;
    }
    
    @Override
    public int supplierSize() {
        return list.size();
    }
    
    @Override
    public void registerExclusionZones(Class<?> screenClass, Supplier<List<Rectangle>> supplier) {
        list.add(new Tuple<>(new Tuple<>(screenClass, 0f), supplier));
    }
    
    private long areasHashCode(Rectangle rectangle, List<Rectangle> exclusionZones) {
        int hashCode = 31 + (rectangle == null ? 0 : rectangle.hashCode());
        for (Rectangle e : exclusionZones)
            hashCode = 31 * hashCode + (e == null ? 0 : e.hashCode());
        return hashCode;
    }
    
}
