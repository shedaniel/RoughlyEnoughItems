package me.shedaniel.rei.client;

import me.shedaniel.rei.gui.ContainerGuiOverlay;
import me.shedaniel.rei.gui.widget.TextFieldWidget;
import me.shedaniel.rei.listeners.IMixinContainerGui;
import net.minecraft.client.gui.ContainerGui;

public class GuiHelper {
    
    public static TextFieldWidget searchField;
    private static boolean overlayVisible = true;
    private static ContainerGuiOverlay overlay;
    
    public static boolean isOverlayVisible() {
        return overlayVisible;
    }
    
    public static void toggleOverlayVisible() {
        overlayVisible = !overlayVisible;
    }
    
    public static ContainerGuiOverlay getOverlay(ContainerGui lastGui) {
        if (overlay == null) {
            overlay = new ContainerGuiOverlay(lastGui);
            overlay.onInitialized();
        }
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
