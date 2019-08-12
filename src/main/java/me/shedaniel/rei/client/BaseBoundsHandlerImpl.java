/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.client;

import com.google.common.collect.Lists;
import me.shedaniel.rei.RoughlyEnoughItemsCore;
import me.shedaniel.rei.api.BaseBoundsHandler;
import me.shedaniel.rei.api.DisplayHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Pair;

import java.awt.*;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

public class BaseBoundsHandlerImpl implements BaseBoundsHandler {
    
    private static final Function<? super Rectangle, Long> RECTANGLE_LONG_FUNCTION = r -> r.x + 1000l * r.y + 1000000l * r.width + 1000000000l * r.height;
    private static final Comparator<Pair<Pair<Class<?>, Float>, ExclusionZoneSupplier>> LIST_PAIR_COMPARATOR;
    private static final Comparator<? super Rectangle> RECTANGLE_COMPARER = Comparator.comparingLong(RECTANGLE_LONG_FUNCTION::apply);
    
    static {
        Comparator<Pair<Pair<Class<?>, Float>, ExclusionZoneSupplier>> comparator = Comparator.comparingDouble(value -> value.getLeft().getRight());
        LIST_PAIR_COMPARATOR = comparator.reversed();
    }
    
    private long lastArea = -1;
    private List<Pair<Pair<Class<?>, Float>, ExclusionZoneSupplier>> list = Lists.newArrayList();
    
    @Override
    public Class getBaseSupportedClass() {
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
    public ActionResult isInZone(boolean isOnRightSide, double mouseX, double mouseY) {
        for (Rectangle zone : getCurrentExclusionZones(MinecraftClient.getInstance().currentScreen.getClass(), isOnRightSide, false))
            if (zone.contains(mouseX, mouseY))
                return ActionResult.FAIL;
        return ActionResult.PASS;
    }
    
    @Override
    public boolean shouldRecalculateArea(boolean isOnRightSide, Rectangle rectangle) {
        long current = getStringFromCurrent(isOnRightSide);
        if (lastArea == current)
            return false;
        lastArea = current;
        return true;
    }
    
    private DisplayHelper.DisplayBoundsHandler getHandler() {
        return RoughlyEnoughItemsCore.getDisplayHelper().getResponsibleBoundsHandler(MinecraftClient.getInstance().currentScreen.getClass());
    }
    
    private long getStringFromCurrent(boolean isOnRightSide) {
        return getLongFromAreas(isOnRightSide ? getHandler().getRightBounds(MinecraftClient.getInstance().currentScreen) : getHandler().getLeftBounds(MinecraftClient.getInstance().currentScreen), getCurrentExclusionZones(MinecraftClient.getInstance().currentScreen.getClass(), isOnRightSide, false));
    }
    
    @Override
    public ActionResult canItemSlotWidgetFit(boolean isOnRightSide, int left, int top, Screen screen, Rectangle fullBounds) {
        for (Rectangle currentExclusionZone : getCurrentExclusionZones(MinecraftClient.getInstance().currentScreen.getClass(), isOnRightSide, false))
            if (left + 18 >= currentExclusionZone.x && top + 18 >= currentExclusionZone.y && left <= currentExclusionZone.x + currentExclusionZone.width && top <= currentExclusionZone.y + currentExclusionZone.height)
                return ActionResult.FAIL;
        return ActionResult.PASS;
    }
    
    @Override
    public List<Rectangle> getCurrentExclusionZones(Class<? extends Screen> currentScreenClass, boolean isOnRightSide, boolean sort) {
        List<Rectangle> rectangles = Lists.newArrayList();
        for (Pair<Pair<Class<?>, Float>, ExclusionZoneSupplier> pair : list) {
            if (pair.getLeft().getLeft().isAssignableFrom(currentScreenClass))
                rectangles.addAll(pair.getRight().apply(isOnRightSide));
        }
        if (sort)
            rectangles.sort(RECTANGLE_COMPARER);
        return rectangles;
    }
    
    @Override
    public void registerExclusionZones(Class<? extends Screen> screenClass, ExclusionZoneSupplier supplier) {
        list.add(new Pair<>(new Pair<>(screenClass, 0f), supplier));
    }
    
    public long getLongFromAreas(Rectangle rectangle, List<Rectangle> exclusionZones) {
        long a = RECTANGLE_LONG_FUNCTION.apply(rectangle);
        for (Rectangle exclusionZone : exclusionZones)
            a -= RECTANGLE_LONG_FUNCTION.apply(exclusionZone);
        return a;
    }
    
}
