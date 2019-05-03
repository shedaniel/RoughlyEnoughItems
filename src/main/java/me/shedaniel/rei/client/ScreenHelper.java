package me.shedaniel.rei.client;

import com.google.common.collect.Lists;
import me.shedaniel.rei.gui.ContainerScreenOverlay;
import me.shedaniel.rei.gui.widget.SearchFieldWidget;
import me.shedaniel.rei.listeners.ContainerScreenHooks;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.event.client.ClientTickCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.ContainerScreen;
import net.minecraft.client.util.Window;
import net.minecraft.item.ItemStack;
import org.apache.logging.log4j.util.TriConsumer;

import java.awt.*;
import java.util.List;

public class ScreenHelper implements ClientModInitializer {
    
    public static SearchFieldWidget searchField;
    public static List<ItemStack> inventoryStacks = Lists.newArrayList();
    private static boolean overlayVisible = true;
    private static ContainerScreenOverlay overlay;
    private static ContainerScreen lastContainerScreen = null;
    
    public static boolean isOverlayVisible() {
        return overlayVisible;
    }
    
    public static void toggleOverlayVisible() {
        overlayVisible = !overlayVisible;
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
    
    public static ContainerScreen getLastContainerScreen() {
        return lastContainerScreen;
    }
    
    public static void setLastContainerScreen(ContainerScreen lastContainerScreen) {
        ScreenHelper.lastContainerScreen = lastContainerScreen;
    }
    
    public static ContainerScreenHooks getLastContainerScreenHooks() {
        return (ContainerScreenHooks) lastContainerScreen;
    }
    
    public static void drawHoveringWidget(int x, int y, TriConsumer<Integer, Integer, Float> consumer, int width, int height, float delta) {
        Window window = MinecraftClient.getInstance().window;
        drawHoveringWidget(new Dimension(window.getScaledWidth(), window.getScaledHeight()), x, y, consumer, width, height, delta);
    }
    
    public static void drawHoveringWidget(Dimension dimension, int x, int y, TriConsumer<Integer, Integer, Float> consumer, int width, int height, float delta) {
        int int_5 = x + 12;
        int int_6 = y - 12;
        if (int_5 + width > dimension.width)
            int_5 -= 28 + width;
        if (int_6 + height + 6 > dimension.height)
            int_6 = dimension.height - height - 6;
        consumer.accept(int_5, int_6, delta);
    }
    
    @Override
    public void onInitializeClient() {
        ClientTickCallback.EVENT.register(client -> {
            if (lastContainerScreen != client.currentScreen && client.currentScreen instanceof ContainerScreen)
                lastContainerScreen = (ContainerScreen) client.currentScreen;
        });
    }
    
}
