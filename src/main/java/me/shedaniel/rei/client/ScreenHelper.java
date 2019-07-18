/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.client;

import com.google.common.collect.Lists;
import me.shedaniel.rei.RoughlyEnoughItemsClient;
import me.shedaniel.rei.gui.ContainerScreenOverlay;
import me.shedaniel.rei.gui.widget.SearchFieldWidget;
import me.shedaniel.rei.api.ContainerScreenHooks;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.List;
import java.util.Optional;

public class ScreenHelper {
    
    public static SearchFieldWidget searchField;
    public static List<ItemStack> inventoryStacks = Lists.newArrayList();
    public static GuiContainer lastContainerScreen = null;
    private static boolean overlayVisible = true;
    private static ContainerScreenOverlay overlay;
    
    public static boolean isOverlayVisible() {
        return overlayVisible;
    }
    
    public static void toggleOverlayVisible() {
        overlayVisible = !overlayVisible;
    }
    
    public static Optional<ContainerScreenOverlay> getOptionalOverlay() {
        return Optional.ofNullable(overlay);
    }
    
    public static ContainerScreenOverlay getLastOverlay(boolean reset, boolean setPage) {
        if (overlay == null || reset) {
            overlay = new ContainerScreenOverlay();
            overlay.init(setPage);
        }
        return overlay;
    }
    
    public static ContainerScreenOverlay getLastOverlay() {
        return getLastOverlay(false, false);
    }
    
    public static GuiContainer getLastContainerScreen() {
        return lastContainerScreen;
    }
    
    public static void setLastContainerScreen(GuiContainer lastContainerScreen) {
        ScreenHelper.lastContainerScreen = lastContainerScreen;
    }
    
    public static ContainerScreenHooks getLastContainerScreenHooks() {
        return (ContainerScreenHooks) lastContainerScreen;
    }
    
    public static void drawHoveringWidget(int x, int y, TriConsumer<Integer, Integer, Float> consumer, int width, int height, float delta) {
        MainWindow window = Minecraft.getInstance().mainWindow;
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
        return RoughlyEnoughItemsClient.getConfigManager().getConfig().darkTheme;
    }
    
}
