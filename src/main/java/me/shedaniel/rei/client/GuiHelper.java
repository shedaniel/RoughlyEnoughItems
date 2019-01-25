package me.shedaniel.rei.client;

import com.google.common.collect.Lists;
import me.shedaniel.rei.gui.ContainerGuiOverlay;
import me.shedaniel.rei.gui.widget.TextFieldWidget;
import me.shedaniel.rei.listeners.IMixinContainerGui;
import net.minecraft.client.gui.ContainerGui;
import net.minecraft.item.ItemStack;

import java.util.List;

public class GuiHelper {
    
    public static TextFieldWidget searchField;
    private static boolean overlayVisible = true;
    private static ContainerGuiOverlay overlay;
    public static List<ItemStack> inventoryStacks = Lists.newArrayList();
    
    public static boolean isOverlayVisible() {
        return overlayVisible;
    }
    
    public static void toggleOverlayVisible() {
        overlayVisible = !overlayVisible;
    }
    
    public static ContainerGuiOverlay getOverlay(IMixinContainerGui lastGui) {
        if (overlay == null) {
            overlay = new ContainerGuiOverlay(lastGui);
            overlay.onInitialized();
        }
        return overlay;
    }
    
    public static ContainerGuiOverlay getLastOverlay() {
        return overlay;
    }
    
    public static void setOverlay(ContainerGuiOverlay overlay) {
        GuiHelper.overlay = overlay;
        overlay.onInitialized();
    }
    
    public static void resetOverlay() {
        overlay = null;
    }
    
}
