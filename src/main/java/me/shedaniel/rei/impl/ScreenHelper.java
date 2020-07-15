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
import me.shedaniel.cloth.api.client.events.v0.ClothClientHooks;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.math.api.Executor;
import me.shedaniel.rei.RoughlyEnoughItemsState;
import me.shedaniel.rei.api.ConfigManager;
import me.shedaniel.rei.api.ConfigObject;
import me.shedaniel.rei.api.REIHelper;
import me.shedaniel.rei.api.REIOverlay;
import me.shedaniel.rei.api.widgets.Tooltip;
import me.shedaniel.rei.gui.ContainerScreenOverlay;
import me.shedaniel.rei.gui.OverlaySearchField;
import me.shedaniel.rei.gui.RecipeScreen;
import me.shedaniel.rei.gui.WarningAndErrorScreen;
import me.shedaniel.rei.gui.config.SearchFieldLocation;
import me.shedaniel.rei.gui.widget.TextFieldWidget;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.client.ClientTickCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.ContainerScreen;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import org.apache.logging.log4j.util.TriConsumer;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;

@ApiStatus.Internal
@Environment(EnvType.CLIENT)
public class ScreenHelper implements ClientModInitializer, REIHelper {
    
    private OverlaySearchField searchField;
    @ApiStatus.Internal
    public static List<ItemStack> inventoryStacks = Lists.newArrayList();
    private static ContainerScreenOverlay overlay;
    private static ContainerScreen<?> previousContainerScreen = null;
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
    
    @Override
    public Optional<REIOverlay> getOverlay() {
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
    
    /**
     * @see REIHelper#getPreviousContainerScreen()
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval
    public static ContainerScreen<?> getLastHandledScreen() {
        return previousContainerScreen;
    }
    
    @Override
    public ContainerScreen<?> getPreviousContainerScreen() {
        return previousContainerScreen;
    }
    
    public static void setPreviousContainerScreen(ContainerScreen<?> previousContainerScreen) {
        ScreenHelper.previousContainerScreen = previousContainerScreen;
    }
    
    public static void drawHoveringWidget(MatrixStack matrices, int x, int y, TriConsumer<MatrixStack, Point, Float> consumer, int width, int height, float delta) {
        Window window = MinecraftClient.getInstance().getWindow();
        drawHoveringWidget(matrices, window.getScaledWidth(), window.getScaledHeight(), x, y, consumer, width, height, delta);
    }
    
    public static void drawHoveringWidget(MatrixStack matrices, int screenWidth, int screenHeight, int x, int y, TriConsumer<MatrixStack, Point, Float> consumer, int width, int height, float delta) {
        int actualX = Math.max(x + 12, 6);
        int actualY = Math.min(y - height / 2, screenHeight - height - 6);
        if (actualX + width > screenWidth)
            actualX -= 24 + width;
        if (actualY < 6)
            actualY += 24;
        consumer.accept(matrices, new Point(actualX, actualY), delta);
    }
    
    /**
     * @deprecated Please switch to {@link REIHelper#isDarkThemeEnabled()}
     */
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
    
    public static Rectangle getItemListArea(Rectangle bounds) {
        return new Rectangle(bounds.x, bounds.y + 2 + (ConfigObject.getInstance().getSearchFieldLocation() == SearchFieldLocation.TOP_SIDE ? 24 : 0) + (ConfigObject.getInstance().isEntryListWidgetScrolled() ? 0 : 22), bounds.width, bounds.height - (ConfigObject.getInstance().getSearchFieldLocation() != SearchFieldLocation.CENTER ? 27 + 22 : 27) + (!ConfigObject.getInstance().isEntryListWidgetScrolled() ? 0 : 22));
    }
    
    public static Rectangle getFavoritesListArea(Rectangle bounds) {
        int offset = 6 + (!ConfigObject.getInstance().isLowerConfigButton() ? 25 : 0) + (ConfigObject.getInstance().doesShowUtilsButtons() ? 25 : 0);
        return new Rectangle(bounds.x, bounds.y + 2 + offset, bounds.width, bounds.height - 5 - offset);
    }
    
    @Override
    public void onInitializeClient() {
        ClothClientHooks.SCREEN_INIT_PRE.register((client, screen, screenHooks) -> {
            if ((!RoughlyEnoughItemsState.getErrors().isEmpty() || !RoughlyEnoughItemsState.getWarnings().isEmpty()) && !(screen instanceof WarningAndErrorScreen)) {
                WarningAndErrorScreen warningAndErrorScreen = WarningAndErrorScreen.INSTANCE.get();
                warningAndErrorScreen.setParent(screen);
                try {
                    if (client.currentScreen != null) client.currentScreen.removed();
                } catch (Throwable ignored) {
                }
                client.currentScreen = null;
                client.openScreen(warningAndErrorScreen);
            } else if (previousContainerScreen != screen && screen instanceof ContainerScreen)
                previousContainerScreen = (ContainerScreen<?>) screen;
            return ActionResult.PASS;
        });
        boolean loaded = FabricLoader.getInstance().isModLoaded("fabric-events-lifecycle-v0");
        if (!loaded) {
            RoughlyEnoughItemsState.error("Fabric API is not installed!", "https://www.curseforge.com/minecraft/mc-mods/fabric-api/files/all");
            return;
        }
        Executor.run(() -> () -> {
            ClientTickCallback.EVENT.register(minecraftClient -> {
                if (isOverlayVisible() && getSearchField() != null)
                    getSearchField().tick();
            });
        });
    }
}
