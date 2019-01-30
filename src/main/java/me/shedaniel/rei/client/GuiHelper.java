package me.shedaniel.rei.client;

import com.google.common.collect.Lists;
import me.shedaniel.rei.gui.ContainerScreenOverlay;
import me.shedaniel.rei.gui.widget.TextFieldWidget;
import me.shedaniel.rei.listeners.IMixinContainerScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.ContainerScreen;
import net.minecraft.item.ItemStack;

import java.util.List;

public class GuiHelper {
    
    public static TextFieldWidget searchField;
    public static List<ItemStack> inventoryStacks = Lists.newArrayList();
    private static boolean overlayVisible = true;
    private static ContainerScreenOverlay overlay;
    private static ContainerScreen lastContainerScreen;
    private static IMixinContainerScreen lastMixinContainerScreen;
    
    public static boolean isOverlayVisible() {
        return overlayVisible;
    }
    
    public static void toggleOverlayVisible() {
        overlayVisible = !overlayVisible;
    }
    
    public static ContainerScreenOverlay getLastOverlay() {
        return overlay;
    }
    
    public static void setOverlay(ContainerScreenOverlay overlay) {
        GuiHelper.overlay = overlay;
        overlay.onInitialized();
    }
    
    public static void onTick(MinecraftClient client) {
        if (client.currentScreen instanceof ContainerScreen && lastContainerScreen != client.currentScreen) {
            GuiHelper.lastContainerScreen = (ContainerScreen) client.currentScreen;
            GuiHelper.lastMixinContainerScreen = (IMixinContainerScreen) lastContainerScreen;
        }
    }
    
    public static ContainerScreen getLastContainerScreen() {
        return lastContainerScreen;
    }
    
    public static void setLastContainerScreen(ContainerScreen lastContainerScreen) {
        GuiHelper.lastContainerScreen = lastContainerScreen;
    }
    
    public static IMixinContainerScreen getLastMixinContainerScreen() {
        return lastMixinContainerScreen;
    }
    
    public static void setLastMixinContainerScreen(IMixinContainerScreen lastMixinContainerScreen) {
        GuiHelper.lastMixinContainerScreen = lastMixinContainerScreen;
    }
    
}
