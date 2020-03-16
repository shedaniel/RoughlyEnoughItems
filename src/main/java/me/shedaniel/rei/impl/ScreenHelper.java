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

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import me.shedaniel.cloth.hooks.ClothClientHooks;
import me.shedaniel.rei.api.ConfigManager;
import me.shedaniel.rei.api.ConfigObject;
import me.shedaniel.rei.api.REIHelper;
import me.shedaniel.rei.api.widgets.Tooltip;
import me.shedaniel.rei.gui.ContainerScreenOverlay;
import me.shedaniel.rei.gui.OverlaySearchField;
import me.shedaniel.rei.gui.RecipeScreen;
import me.shedaniel.rei.gui.widget.TextFieldWidget;
import me.shedaniel.rei.listeners.ContainerScreenHooks;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.event.client.ClientTickCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.util.Window;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import org.apache.logging.log4j.util.TriConsumer;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;

@ApiStatus.Internal
public class ScreenHelper implements ClientModInitializer, REIHelper {
    
    private OverlaySearchField searchField;
    @ApiStatus.Internal
    public static List<ItemStack> inventoryStacks = Lists.newArrayList();
    private static ContainerScreenOverlay overlay;
    private static HandledScreen<?> lastHandledScreen = null;
    private static LinkedHashSet<RecipeScreen> lastRecipeScreen = Sets.newLinkedHashSetWithExpectedSize(5);
    private static ScreenHelper instance;
    
    /**
     * @return the instance of screen helper
     * @see REIHelper#getInstance()
     */
    @ApiStatus.Internal
    public static ScreenHelper getInstance() {
        return instance;
    }
    
    @Override
    public void queueTooltip(@Nullable Tooltip tooltip) {
        if (overlay != null && tooltip != null) {
            overlay.addTooltip(tooltip);
        }
    }
    
    @Override
    public TextFieldWidget getSearchTextField() {
        return searchField;
    }
    
    @Override
    public List<ItemStack> getInventoryStacks() {
        return inventoryStacks;
    }
    
    public static OverlaySearchField getSearchField() {
        return (OverlaySearchField) getInstance().getSearchTextField();
    }
    
    @ApiStatus.Internal
    public static void setSearchField(OverlaySearchField searchField) {
        getInstance().searchField = searchField;
    }
    
    public static void storeRecipeScreen(RecipeScreen screen) {
        while (lastRecipeScreen.size() >= 5)
            lastRecipeScreen.remove(Iterables.get(lastRecipeScreen, 0));
        lastRecipeScreen.add(screen);
    }
    
    public static boolean hasLastRecipeScreen() {
        return !lastRecipeScreen.isEmpty();
    }
    
    public static Screen getLastRecipeScreen() {
        RecipeScreen screen = Iterables.getLast(lastRecipeScreen);
        lastRecipeScreen.remove(screen);
        screen.recalculateCategoryPage();
        return (Screen) screen;
    }
    
    @ApiStatus.Internal
    public static void clearLastRecipeScreenData() {
        lastRecipeScreen.clear();
    }
    
    public static boolean isOverlayVisible() {
        return ConfigObject.getInstance().isOverlayVisible();
    }
    
    public static void toggleOverlayVisible() {
        ConfigObject.getInstance().setOverlayVisible(!ConfigObject.getInstance().isOverlayVisible());
        ConfigManager.getInstance().saveConfig();
    }
    
    public static Optional<ContainerScreenOverlay> getOptionalOverlay() {
        return Optional.ofNullable(overlay);
    }
    
    public static ContainerScreenOverlay getLastOverlay(boolean reset, boolean setPage) {
        if (overlay == null || reset) {
            overlay = new ContainerScreenOverlay();
            overlay.init();
            getSearchField().setFocused(false);
        }
        return overlay;
    }
    
    public static ContainerScreenOverlay getLastOverlay() {
        return getLastOverlay(false, false);
    }
    
    @Deprecated
    @ApiStatus.ScheduledForRemoval
    public static HandledScreen<?> getLastContainerScreen() {
        return getLastHandledScreen();
    }
    
    @Deprecated
    @ApiStatus.ScheduledForRemoval
    public static HandledScreen<?> getLastScreenWithHandler() {
        return getLastHandledScreen();
    }
    
    public static HandledScreen<?> getLastHandledScreen() {
        return lastHandledScreen;
    }
    
    @Deprecated
    @ApiStatus.ScheduledForRemoval
    public static void setLastContainerScreen(HandledScreen<?> lastScreenWithHandler) {
        setLastHandledScreen(lastScreenWithHandler);
    }
    
    @Deprecated
    @ApiStatus.ScheduledForRemoval
    public static void setLastScreenWithHandler(HandledScreen<?> lastScreenWithHandler) {
        setLastHandledScreen(lastScreenWithHandler);
    }
    
    public static void setLastHandledScreen(HandledScreen<?> lastScreenWithHandler) {
        ScreenHelper.lastHandledScreen = lastScreenWithHandler;
    }
    
    /**
     * @deprecated Please create your own mixin hooks
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval
    public static ContainerScreenHooks getLastContainerScreenHooks() {
        return getLastScreenWithHandlerHooks();
    }
    
    /**
     * @deprecated Please create your own mixin hooks
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval
    public static ContainerScreenHooks getLastScreenWithHandlerHooks() {
        return (ContainerScreenHooks) lastHandledScreen;
    }
    
    public static void drawHoveringWidget(int x, int y, TriConsumer<Integer, Integer, Float> consumer, int width, int height, float delta) {
        Window window = MinecraftClient.getInstance().getWindow();
        drawHoveringWidget(window.getScaledWidth(), window.getScaledHeight(), x, y, consumer, width, height, delta);
    }
    
    public static void drawHoveringWidget(int screenWidth, int screenHeight, int x, int y, TriConsumer<Integer, Integer, Float> consumer, int width, int height, float delta) {
        int actualX = Math.max(x + 12, 6);
        int actualY = Math.min(y - height / 2, screenHeight - height - 6);
        if (actualX + width > screenWidth)
            actualX -= 24 + width;
        if (actualY < 6)
            actualY += 24;
        consumer.accept(actualX, actualY, delta);
    }
    
    @Deprecated
    @ApiStatus.Internal
    @ApiStatus.ScheduledForRemoval
    public static boolean isDarkModeEnabled() {
        return ConfigObject.getInstance().isUsingDarkTheme();
    }
    
    @Override
    public boolean isDarkThemeEnabled() {
        return isDarkModeEnabled();
    }
    
    public ScreenHelper() {
        ScreenHelper.instance = this;
    }
    
    @Override
    public void onInitializeClient() {
        ClothClientHooks.SCREEN_INIT_PRE.register((client, screen, screenHooks) -> {
            if (lastHandledScreen != screen && screen instanceof HandledScreen)
                lastHandledScreen = (HandledScreen<?>) screen;
            return ActionResult.PASS;
        });
        ClientTickCallback.EVENT.register(minecraftClient -> {
            if (isOverlayVisible() && getSearchField() != null)
                getSearchField().tick();
        });
    }
}
