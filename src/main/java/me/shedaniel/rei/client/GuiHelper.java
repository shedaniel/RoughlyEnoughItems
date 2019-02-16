package me.shedaniel.rei.client;

import com.google.common.collect.Lists;
import me.shedaniel.rei.gui.ContainerGuiOverlay;
import me.shedaniel.rei.gui.widget.TextFieldWidget;
import me.shedaniel.rei.listeners.IMixinGuiContainer;
import me.shedaniel.rei.update.UpdateAnnouncer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;
import org.dimdev.rift.listener.client.ClientTickable;

import java.util.List;

public class GuiHelper implements ClientTickable {
    
    public static TextFieldWidget searchField;
    public static List<ItemStack> inventoryStacks = Lists.newArrayList();
    private static boolean overlayVisible = true;
    private static ContainerGuiOverlay overlay;
    private static GuiContainer lastGuiContainer;
    
    public static boolean isOverlayVisible() {
        return overlayVisible;
    }
    
    public static void toggleOverlayVisible() {
        overlayVisible = !overlayVisible;
    }
    
    public static ContainerGuiOverlay getLastOverlay(boolean reset) {
        if (overlay == null || reset) {
            overlay = new ContainerGuiOverlay();
            overlay.onInitialized();
        }
        return overlay;
    }
    
    public static ContainerGuiOverlay getLastOverlay() {
        return getLastOverlay(false);
    }
    
    public static GuiContainer getLastGuiContainer() {
        return lastGuiContainer;
    }
    
    public static void setLastGuiContainer(GuiContainer lastGuiContainer) {
        GuiHelper.lastGuiContainer = lastGuiContainer;
    }
    
    public static IMixinGuiContainer getLastMixinGuiContainer() {
        return (IMixinGuiContainer) lastGuiContainer;
    }
    
    
    @Override
    public void clientTick(Minecraft client) {
        if (client.currentScreen instanceof GuiContainer && lastGuiContainer != client.currentScreen) {
            GuiHelper.lastGuiContainer = (GuiContainer) client.currentScreen;
        }
        UpdateAnnouncer.clientTick(client);
    }
    
}
