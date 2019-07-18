/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.client;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import me.shedaniel.rei.RoughlyEnoughItemsCore;
import me.shedaniel.rei.api.BaseBoundsHandler;
import me.shedaniel.rei.api.DisplayHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.EnumActionResult;

import java.awt.*;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class BaseBoundsHandlerImpl implements BaseBoundsHandler {
    
    private static final Function<Rectangle, String> RECTANGLE_STRING_FUNCTION = rectangle -> rectangle.x + "," + rectangle.y + "," + rectangle.width + "," + rectangle.height;
    private static final Comparator<Rectangle> RECTANGLE_COMPARATOR = BaseBoundsHandlerImpl::compare;
    private static final Comparator<Pair<Pair<Class<?>, Float>, Function<Boolean, List<Rectangle>>>> LIST_PAIR_COMPARATOR;
    
    static {
        Comparator<Pair<Pair<Class<?>, Float>, Function<Boolean, List<Rectangle>>>> comparator = Comparator.comparingDouble(value -> value.getFirst().getSecond());
        LIST_PAIR_COMPARATOR = comparator.reversed();
    }
    
    private String lastArea = null;
    private List<Pair<Pair<Class<?>, Float>, Function<Boolean, List<Rectangle>>>> list = Lists.newArrayList();
    
    private static int compare(Rectangle o1, Rectangle o2) {return RECTANGLE_STRING_FUNCTION.apply(o1).compareTo(RECTANGLE_STRING_FUNCTION.apply(o2));}
    
    @Override
    public Class getBaseSupportedClass() {
        return GuiScreen.class;
    }
    
    @Override
    public Rectangle getLeftBounds(GuiScreen screen) {
        return new Rectangle();
    }
    
    @Override
    public Rectangle getRightBounds(GuiScreen screen) {
        return new Rectangle();
    }
    
    @Override
    public float getPriority() {
        return -5f;
    }
    
    @Override
    public EnumActionResult isInZone(boolean isOnRightSide, double mouseX, double mouseY) {
        for(Rectangle zone : getCurrentExclusionZones(Minecraft.getInstance().currentScreen.getClass(), isOnRightSide))
            if (zone.contains(mouseX, mouseY))
                return EnumActionResult.FAIL;
        return EnumActionResult.PASS;
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
        return RoughlyEnoughItemsCore.getDisplayHelper().getResponsibleBoundsHandler(Minecraft.getInstance().currentScreen.getClass());
    }
    
    private String getStringFromCurrent(boolean isOnRightSide) {
        return getStringFromAreas(isOnRightSide ? getHandler().getRightBounds(Minecraft.getInstance().currentScreen) : getHandler().getLeftBounds(Minecraft.getInstance().currentScreen), getCurrentExclusionZones(Minecraft.getInstance().currentScreen.getClass(), isOnRightSide));
    }
    
    @Override
    public EnumActionResult canItemSlotWidgetFit(boolean isOnRightSide, int left, int top, GuiScreen screen, Rectangle fullBounds) {
        List<Rectangle> currentExclusionZones = getCurrentExclusionZones(Minecraft.getInstance().currentScreen.getClass(), isOnRightSide);
        for(Rectangle currentExclusionZone : currentExclusionZones)
            if (left + 18 >= currentExclusionZone.x && top + 18 >= currentExclusionZone.y && left <= currentExclusionZone.x + currentExclusionZone.width && top <= currentExclusionZone.y + currentExclusionZone.height)
                return EnumActionResult.FAIL;
        return EnumActionResult.PASS;
    }
    
    public List<Rectangle> getCurrentExclusionZones(Class<?> currentScreenClass, boolean isOnRightSide) {
        List<Pair<Pair<Class<?>, Float>, Function<Boolean, List<Rectangle>>>> only = list.stream().filter(pair -> pair.getFirst().getFirst().isAssignableFrom(currentScreenClass)).collect(Collectors.toList());
        only.sort(LIST_PAIR_COMPARATOR);
        List<Rectangle> rectangles = Lists.newArrayList();
        only.forEach(pair -> rectangles.addAll(pair.getSecond().apply(isOnRightSide)));
        return rectangles;
    }
    
    @Override
    public void registerExclusionZones(Class<?> screenClass, Function<Boolean, List<Rectangle>> supplier) {
        list.add(new Pair<>(new Pair<>(screenClass, 0f), supplier));
    }
    
    public String getStringFromAreas(Rectangle rectangle, List<Rectangle> exclusionZones) {
        List<Rectangle> sorted = Lists.newArrayList(exclusionZones);
        sorted.sort(RECTANGLE_COMPARATOR);
        return RECTANGLE_STRING_FUNCTION.apply(rectangle) + ":" + sorted.stream().map(RECTANGLE_STRING_FUNCTION::apply).collect(Collectors.joining("|"));
    }
    
}
