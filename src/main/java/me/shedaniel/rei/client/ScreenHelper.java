package me.shedaniel.rei.client;

import com.google.common.collect.Lists;
import me.shedaniel.rei.gui.ContainerScreenOverlay;
import me.shedaniel.rei.gui.widget.TextFieldWidget;
import me.shedaniel.rei.listeners.ContainerScreenHooks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;
import org.dimdev.rift.listener.client.ClientTickable;

import java.util.List;

public class ScreenHelper implements ClientTickable {
    
    public static TextFieldWidget searchField;
    public static List<ItemStack> inventoryStacks = Lists.newArrayList();
    private static boolean overlayVisible = true;
    private static ContainerScreenOverlay overlay;
    private static GuiContainer lastContainerScreen;
    
    public static boolean isOverlayVisible() {
        return overlayVisible;
    }
    
    public static void toggleOverlayVisible() {
        overlayVisible = !overlayVisible;
    }
    
    public static ContainerScreenOverlay getLastOverlay(boolean reset) {
        if (overlay == null || reset) {
            overlay = new ContainerScreenOverlay();
            overlay.onInitialized();
        }
        return overlay;
    }
    
    public static ContainerScreenOverlay getLastOverlay() {
        return getLastOverlay(false);
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
    
    @Override
    public void clientTick(Minecraft client) {
        if (lastContainerScreen != client.currentScreen && client.currentScreen instanceof GuiContainer)
            lastContainerScreen = (GuiContainer) client.currentScreen;
    }
    
}
