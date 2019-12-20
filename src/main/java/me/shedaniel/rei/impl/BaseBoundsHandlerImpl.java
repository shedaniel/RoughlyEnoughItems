/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.impl;

import com.google.common.collect.Lists;
import me.shedaniel.math.api.Rectangle;
import me.shedaniel.rei.api.BaseBoundsHandler;
import me.shedaniel.rei.api.DisplayHelper;
import me.shedaniel.rei.api.annotations.Internal;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Pair;

import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;

@Deprecated
@Internal
public class BaseBoundsHandlerImpl implements BaseBoundsHandler {
    
    private static final Comparator<? super Rectangle> RECTANGLE_COMPARER = Comparator.comparingLong(Rectangle::hashCode);
    
    private long lastArea = -1;
    private List<Pair<Pair<Class<?>, Float>, Supplier<List<Rectangle>>>> list = Lists.newArrayList();
    
    @Override
    public Class<?> getBaseSupportedClass() {
        return Screen.class;
    }
    
    @Override
    public Rectangle getLeftBounds(Screen screen) {
        return new Rectangle();
    }
    
    @Override
    public Rectangle getRightBounds(Screen screen) {
        return new Rectangle();
    }
    
    @Override
    public float getPriority() {
        return -5f;
    }
    
    @Override
    public ActionResult isInZone(double mouseX, double mouseY) {
        Class<? extends Screen> screenClass = MinecraftClient.getInstance().currentScreen.getClass();
        for (Pair<Pair<Class<?>, Float>, Supplier<List<Rectangle>>> pair : list) {
            if (pair.getLeft().getLeft().isAssignableFrom(screenClass))
                for (Rectangle zone : pair.getRight().get())
                    if (zone.contains(mouseX, mouseY))
                        return ActionResult.FAIL;
        }
        return ActionResult.PASS;
    }
    
    @Override
    public boolean shouldRecalculateArea(boolean isOnRightSide, Rectangle rectangle) {
        long current = currentHashCode(isOnRightSide);
        if (lastArea == current)
            return false;
        lastArea = current;
        return true;
    }
    
    private long currentHashCode(boolean isOnRightSide) {
        DisplayHelper.DisplayBoundsHandler handler = DisplayHelper.getInstance().getResponsibleBoundsHandler(MinecraftClient.getInstance().currentScreen.getClass());
        return areasHashCode(isOnRightSide ? handler.getRightBounds(MinecraftClient.getInstance().currentScreen) : handler.getLeftBounds(MinecraftClient.getInstance().currentScreen), getExclusionZones(MinecraftClient.getInstance().currentScreen.getClass(), false));
    }
    
    @Override
    public ActionResult canItemSlotWidgetFit(int left, int top, Screen screen, Rectangle fullBounds) {
        Class<? extends Screen> screenClass = screen.getClass();
        for (Pair<Pair<Class<?>, Float>, Supplier<List<Rectangle>>> pair : list) {
            if (pair.getLeft().getLeft().isAssignableFrom(screenClass))
                for (Rectangle zone : pair.getRight().get()) {
                    if (left + 18 >= zone.x && top + 18 >= zone.y && left <= zone.getMaxX() && top <= zone.getMaxY())
                        return ActionResult.FAIL;
                }
        }
        return ActionResult.PASS;
    }
    
    @Override
    public List<Rectangle> getExclusionZones(Class<?> currentScreenClass, boolean sort) {
        List<Rectangle> rectangles = Lists.newArrayList();
        for (Pair<Pair<Class<?>, Float>, Supplier<List<Rectangle>>> pair : list) {
            if (pair.getLeft().getLeft().isAssignableFrom(currentScreenClass))
                rectangles.addAll(pair.getRight().get());
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
        list.add(new Pair<>(new Pair<>(screenClass, 0f), supplier));
    }
    
    private long areasHashCode(Rectangle rectangle, List<Rectangle> exclusionZones) {
        int hashCode = 31 + (rectangle == null ? 0 : rectangle.hashCode());
        for (Rectangle e : exclusionZones)
            hashCode = 31 * hashCode + (e == null ? 0 : e.hashCode());
        return hashCode;
    }
    
}
