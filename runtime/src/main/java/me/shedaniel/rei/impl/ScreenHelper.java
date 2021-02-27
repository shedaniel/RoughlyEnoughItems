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
import com.google.common.collect.Sets;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import me.shedaniel.architectury.event.events.GuiEvent;
import me.shedaniel.architectury.event.events.client.ClientTickEvent;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.ConfigManager;
import me.shedaniel.rei.api.ConfigObject;
import me.shedaniel.rei.api.REIHelper;
import me.shedaniel.rei.api.REIOverlay;
import me.shedaniel.rei.api.gui.config.SearchFieldLocation;
import me.shedaniel.rei.api.gui.widgets.TextField;
import me.shedaniel.rei.api.gui.widgets.Tooltip;
import me.shedaniel.rei.api.ingredient.EntryStack;
import me.shedaniel.rei.api.registry.screen.DisplayBoundsProvider;
import me.shedaniel.rei.api.registry.screen.OverlayDecider;
import me.shedaniel.rei.api.registry.screen.ScreenRegistry;
import me.shedaniel.rei.gui.ContainerScreenOverlay;
import me.shedaniel.rei.gui.OverlaySearchField;
import me.shedaniel.rei.gui.RecipeScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import org.apache.logging.log4j.util.TriConsumer;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import static me.shedaniel.rei.impl.Internals.attachInstance;

@ApiStatus.Internal
@Environment(EnvType.CLIENT)
public class ScreenHelper implements ClientModInitializer, REIHelper {
    @ApiStatus.Internal
    public static boolean isWithinRecipeViewingScreen = false;
    private static final ResourceLocation DISPLAY_TEXTURE = new ResourceLocation("roughlyenoughitems", "textures/gui/display.png");
    private static final ResourceLocation DISPLAY_TEXTURE_DARK = new ResourceLocation("roughlyenoughitems", "textures/gui/display_dark.png");
    private OverlaySearchField searchField;
    @ApiStatus.Internal
    public static Set<EntryStack<?>> inventoryStacks = Sets.newHashSet();
    private static ContainerScreenOverlay overlay;
    private static AbstractContainerScreen<?> previousContainerScreen = null;
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
    @Nullable
    public TextField getSearchTextField() {
        return searchField;
    }
    
    @Nullable
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
    public static void reload() {
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
    
    @Override
    public AbstractContainerScreen<?> getPreviousContainerScreen() {
        return previousContainerScreen;
    }
    
    public static void setPreviousContainerScreen(AbstractContainerScreen<?> previousContainerScreen) {
        ScreenHelper.previousContainerScreen = previousContainerScreen;
    }
    
    public static void drawHoveringWidget(PoseStack matrices, int x, int y, TriConsumer<PoseStack, Point, Float> consumer, int width, int height, float delta) {
        Window window = Minecraft.getInstance().getWindow();
        drawHoveringWidget(matrices, window.getGuiScaledWidth(), window.getGuiScaledHeight(), x, y, consumer, width, height, delta);
    }
    
    public static void drawHoveringWidget(PoseStack matrices, int screenWidth, int screenHeight, int x, int y, TriConsumer<PoseStack, Point, Float> consumer, int width, int height, float delta) {
        int actualX = Math.max(x + 12, 6);
        int actualY = Math.min(y - height / 2, screenHeight - height - 6);
        if (actualX + width > screenWidth)
            actualX -= 24 + width;
        if (actualY < 6)
            actualY += 24;
        consumer.accept(matrices, new Point(actualX, actualY), delta);
    }
    
    @Override
    public boolean isDarkThemeEnabled() {
        return ConfigObject.getInstance().isUsingDarkTheme();
    }
    
    @Override
    public @NotNull ResourceLocation getDefaultDisplayTexture() {
        return isDarkThemeEnabled() ? DISPLAY_TEXTURE_DARK : DISPLAY_TEXTURE;
    }
    
    public ScreenHelper() {
        ScreenHelper.instance = this;
        attachInstance(instance, REIHelper.class);
    }
    
    public static SearchFieldLocation getContextualSearchFieldLocation() {
        Window window = Minecraft.getInstance().getWindow();
        for (OverlayDecider decider : ScreenRegistry.getInstance().getDeciders(Minecraft.getInstance().screen.getClass())) {
            if (decider instanceof DisplayBoundsProvider) {
                Rectangle containerBounds = ((DisplayBoundsProvider<Screen>) decider).getScreenBounds(Minecraft.getInstance().screen);
                if (window.getGuiScaledHeight() - 20 <= containerBounds.getMaxY())
                    return SearchFieldLocation.BOTTOM_SIDE;
                else break;
            }
        }
        return ConfigObject.getInstance().getSearchFieldLocation();
    }
    
    public static Rectangle getItemListArea(Rectangle bounds) {
        SearchFieldLocation searchFieldLocation = ScreenHelper.getContextualSearchFieldLocation();
        
        int yOffset = 2;
        if (searchFieldLocation == SearchFieldLocation.TOP_SIDE) yOffset += 24;
        if (!ConfigObject.getInstance().isEntryListWidgetScrolled()) yOffset += 22;
        int heightOffset = 0;
        if (searchFieldLocation == SearchFieldLocation.BOTTOM_SIDE) heightOffset += 24;
        return new Rectangle(bounds.x, bounds.y + yOffset, bounds.width, bounds.height - 1 - yOffset - heightOffset);
    }
    
    public static Rectangle getFavoritesListArea(Rectangle bounds) {
        int yOffset = 8;
        if (ConfigObject.getInstance().doesShowUtilsButtons()) yOffset += 50;
        else if (!ConfigObject.getInstance().isLowerConfigButton()) yOffset += 25;
        return new Rectangle(bounds.x, bounds.y + yOffset, bounds.width, bounds.height - 3 - yOffset);
    }
    
    @Override
    public void onInitializeClient() {
        GuiEvent.INIT_PRE.register((screen, widgets, children) -> {
            if (previousContainerScreen != screen && screen instanceof AbstractContainerScreen)
                previousContainerScreen = (AbstractContainerScreen<?>) screen;
            return InteractionResult.PASS;
        });
        ClientTickEvent.CLIENT_POST.register(minecraft -> {
            if (isOverlayVisible() && getSearchField() != null)
                getSearchField().tick();
        });
    }
    
    @Override
    public void startReload() {
        getOverlay().ifPresent(REIOverlay::queueReloadOverlay);
    }
    
    @Override
    public void endReload() {
        getOverlay().ifPresent(REIOverlay::queueReloadOverlay);
    }
}
