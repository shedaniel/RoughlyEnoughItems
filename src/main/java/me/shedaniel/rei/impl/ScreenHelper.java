/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.impl;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import me.shedaniel.cloth.hooks.ClothClientHooks;
import me.shedaniel.rei.api.ConfigManager;
import me.shedaniel.rei.api.ConfigObject;
import me.shedaniel.rei.gui.ContainerScreenOverlay;
import me.shedaniel.rei.gui.OverlaySearchField;
import me.shedaniel.rei.listeners.ContainerScreenHooks;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.event.client.ClientTickCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.AbstractContainerScreen;
import net.minecraft.client.util.Window;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import org.apache.logging.log4j.util.TriConsumer;
import org.jetbrains.annotations.ApiStatus;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;

public class ScreenHelper implements ClientModInitializer {
    
    /**
     * @deprecated Use getters instead
     */
    @Deprecated @ApiStatus.ScheduledForRemoval public static OverlaySearchField searchField;
    @ApiStatus.Internal public static List<ItemStack> inventoryStacks = Lists.newArrayList();
    private static ContainerScreenOverlay overlay;
    private static AbstractContainerScreen<?> lastContainerScreen = null;
    private static LinkedHashSet<Screen> lastRecipeScreen = Sets.newLinkedHashSetWithExpectedSize(5);
    
    public static OverlaySearchField getSearchField() {
        return searchField;
    }
    
    @Deprecated
    public static void setSearchField(OverlaySearchField searchField) {
        ScreenHelper.searchField = searchField;
    }
    
    public static void storeRecipeScreen(Screen screen) {
        while (lastRecipeScreen.size() >= 5)
            lastRecipeScreen.remove(Iterables.get(lastRecipeScreen, 0));
        lastRecipeScreen.add(screen);
    }
    
    public static boolean hasLastRecipeScreen() {
        return !lastRecipeScreen.isEmpty();
    }
    
    public static Screen getLastRecipeScreen() {
        Screen screen = Iterables.getLast(lastRecipeScreen);
        lastRecipeScreen.remove(screen);
        return screen;
    }
    
    public static void clearData() {
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
    
    public static AbstractContainerScreen<?> getLastContainerScreen() {
        return lastContainerScreen;
    }
    
    public static void setLastContainerScreen(AbstractContainerScreen<?> lastContainerScreen) {
        ScreenHelper.lastContainerScreen = lastContainerScreen;
    }
    
    public static ContainerScreenHooks getLastContainerScreenHooks() {
        return (ContainerScreenHooks) lastContainerScreen;
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
    
    public static boolean isDarkModeEnabled() {
        return ConfigObject.getInstance().isUsingDarkTheme();
    }
    
    @Override
    public void onInitializeClient() {
        ClothClientHooks.SCREEN_INIT_PRE.register((client, screen, screenHooks) -> {
            if (lastContainerScreen != screen && screen instanceof AbstractContainerScreen)
                lastContainerScreen = (AbstractContainerScreen<?>) screen;
            return ActionResult.PASS;
        });
        ClientTickCallback.EVENT.register(minecraftClient -> {
            if (isOverlayVisible() && getSearchField() != null)
                getSearchField().tick();
        });
    }
    
}
