package me.shedaniel.rei.client;

import com.google.common.collect.Lists;
import me.shedaniel.rei.gui.ContainerGuiOverlay;
import me.shedaniel.rei.gui.widget.TextFieldWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.List;

public class GuiHelper {
    
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
    
    public static ContainerGuiOverlay getLastOverlay(boolean reset, boolean setPage) {
        if (overlay == null || reset) {
            overlay = new ContainerGuiOverlay();
            overlay.init(setPage);
        }
        return overlay;
    }
    
    public static ContainerGuiOverlay getLastOverlay() {
        return getLastOverlay(false, false);
    }
    
    public static GuiContainer getLastGuiContainer() {
        return lastGuiContainer;
    }
    
    public static void setLastGuiContainer(GuiContainer lastGuiContainer) {
        GuiHelper.lastGuiContainer = lastGuiContainer;
    }
    
    public static void clientTick(TickEvent.ClientTickEvent event) {
        Minecraft client = Minecraft.getInstance();
        if (client.currentScreen instanceof GuiContainer && lastGuiContainer != client.currentScreen)
            GuiHelper.lastGuiContainer = (GuiContainer) client.currentScreen;
    }
    
}
