package me.shedaniel.rei.client;

import com.google.common.collect.Lists;
import me.shedaniel.rei.gui.ContainerGuiOverlay;
import me.shedaniel.rei.gui.widget.TextFieldWidget;
import me.shedaniel.rei.listeners.IMixinContainerGui;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.ContainerGui;
import net.minecraft.item.ItemStack;

import java.util.List;

public class GuiHelper {
    
    public static TextFieldWidget searchField;
    private static boolean overlayVisible = true;
    private static ContainerGuiOverlay overlay;
    public static List<ItemStack> inventoryStacks = Lists.newArrayList();
    private static ContainerGui lastContainerGui;
    private static IMixinContainerGui lastMixinContainerGui;
    
    public static boolean isOverlayVisible() {
        return overlayVisible;
    }
    
    public static void toggleOverlayVisible() {
        overlayVisible = !overlayVisible;
    }
    
    public static ContainerGuiOverlay getLastOverlay() {
        return overlay;
    }
    
    public static void setOverlay(ContainerGuiOverlay overlay) {
        GuiHelper.overlay = overlay;
        overlay.onInitialized();
    }
    
    public static void onTick(MinecraftClient client) {
        if (client.currentGui instanceof ContainerGui && lastContainerGui != client.currentGui) {
            GuiHelper.lastContainerGui = (ContainerGui) client.currentGui;
            GuiHelper.lastMixinContainerGui = (IMixinContainerGui) lastContainerGui;
        }
    }
    
    public static ContainerGui getLastContainerGui() {
        return lastContainerGui;
    }
    
    public static IMixinContainerGui getLastMixinContainerGui() {
        return lastMixinContainerGui;
    }
    
    public static void setLastContainerGui(ContainerGui lastContainerGui) {
        GuiHelper.lastContainerGui = lastContainerGui;
    }
    
    public static void setLastMixinContainerGui(IMixinContainerGui lastMixinContainerGui) {
        GuiHelper.lastMixinContainerGui = lastMixinContainerGui;
    }
    
}
