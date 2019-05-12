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
import net.minecraft.client.gui.Screen;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Pair;

import java.awt.*;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class BaseBoundsHandlerImpl implements BaseBoundsHandler {
    
    private static final Function<Rectangle, String> RECTANGLE_STRING_FUNCTION = rectangle -> rectangle.x + "," + rectangle.y + "," + rectangle.width + "," + rectangle.height;
    private static final Comparator<Rectangle> RECTANGLE_COMPARATOR = BaseBoundsHandlerImpl::compare;
    private static final Comparator<Pair<Pair<Class<? extends Screen>, Float>, ExclusionZoneSupplier>> LIST_PAIR_COMPARATOR;
    
    static {
        Comparator<Pair<Pair<Class<? extends Screen>, Float>, ExclusionZoneSupplier>> comparator = Comparator.comparingDouble(value -> value.getLeft().getRight());
        LIST_PAIR_COMPARATOR = comparator.reversed();
    }
    
    private String lastArea = null;
    private List<Pair<Pair<Class<? extends Screen>, Float>, ExclusionZoneSupplier>> list = Lists.newArrayList();
    
    private static int compare(Rectangle o1, Rectangle o2) {return RECTANGLE_STRING_FUNCTION.apply(o1).compareTo(RECTANGLE_STRING_FUNCTION.apply(o2));}
    
    @Override
    public Class getBaseSupportedClass() {
        return Screen.class;
    }
    
    @Override
    public Rectangle getLeftBounds(Screen screen) {
        return DisplayHelper.DisplayBoundsHandler.EMPTY;
    }
    
    @Override
    public Rectangle getRightBounds(Screen screen) {
        return DisplayHelper.DisplayBoundsHandler.EMPTY;
    }
    
    @Override
    public float getPriority() {
        return -5f;
    }
    
    @Override
    public ActionResult isInZone(boolean isOnRightSide, double mouseX, double mouseY) {
        for(Rectangle zone : getCurrentExclusionZones(MinecraftClient.getInstance().currentScreen.getClass(), isOnRightSide))
            if (zone.contains(mouseX, mouseY))
                return ActionResult.FAIL;
        return ActionResult.PASS;
    }
    
    @Override
    public boolean shouldRecalculateArea(boolean isOnRightSide, Rectangle rectangle) {
        if (lastArea == null) {
            lastArea = getStringFromCurrent(isOnRightSide);
            return false;
        }
        if (lastArea.contentEquals(getStringFromCurrent(isOnRightSide)))
            return false;
        lastArea = getStringFromCurrent(isOnRightSide);
        return true;
    }
    
    private DisplayHelper.DisplayBoundsHandler getHandler() {
        return RoughlyEnoughItemsCore.getDisplayHelper().getResponsibleBoundsHandler(MinecraftClient.getInstance().currentScreen.getClass());
    }
    
    private String getStringFromCurrent(boolean isOnRightSide) {
        return getStringFromAreas(isOnRightSide ? getHandler().getRightBounds(MinecraftClient.getInstance().currentScreen) : getHandler().getLeftBounds(MinecraftClient.getInstance().currentScreen), getCurrentExclusionZones(MinecraftClient.getInstance().currentScreen.getClass(), isOnRightSide));
    }
    
    @Override
    public ActionResult canItemSlotWidgetFit(boolean isOnRightSide, int left, int top, Screen screen, Rectangle fullBounds) {
        List<Rectangle> currentExclusionZones = getCurrentExclusionZones(MinecraftClient.getInstance().currentScreen.getClass(), isOnRightSide);
        for(Rectangle currentExclusionZone : currentExclusionZones)
            if (left + 18 >= currentExclusionZone.x && top + 18 >= currentExclusionZone.y && left <= currentExclusionZone.x + currentExclusionZone.width && top <= currentExclusionZone.y + currentExclusionZone.height)
                return ActionResult.FAIL;
        return ActionResult.PASS;
    }
    
    public List<Rectangle> getCurrentExclusionZones(Class<? extends Screen> currentScreenClass, boolean isOnRightSide) {
        List<Pair<Pair<Class<? extends Screen>, Float>, ExclusionZoneSupplier>> only = list.stream().filter(pair -> pair.getLeft().getLeft().isAssignableFrom(currentScreenClass)).collect(Collectors.toList());
        only.sort(LIST_PAIR_COMPARATOR);
        List<Rectangle> rectangles = Lists.newArrayList();
        only.forEach(pair -> rectangles.addAll(pair.getRight().apply(isOnRightSide)));
        return rectangles;
    }
    
    @Override
    public void registerExclusionZones(Class<? extends Screen> screenClass, ExclusionZoneSupplier supplier) {
        list.add(new Pair<>(new Pair<>(screenClass, 0f), supplier));
    }
    
    public String getStringFromAreas(Rectangle rectangle, List<Rectangle> exclusionZones) {
        List<Rectangle> sorted = Lists.newArrayList(exclusionZones);
        sorted.sort(RECTANGLE_COMPARATOR);
        return RECTANGLE_STRING_FUNCTION.apply(rectangle) + ":" + sorted.stream().map(RECTANGLE_STRING_FUNCTION::apply).collect(Collectors.joining("|"));
    }
    
}
