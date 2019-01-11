package me.shedaniel.rei.client;

import me.shedaniel.rei.gui.ContainerGuiOverlay;
import me.shedaniel.rei.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.ContainerGui;

public class GuiHelper {
    
    private static boolean overlayVisible = true;
    private static ContainerGuiOverlay overlay;
    public static TextFieldWidget searchField;
    
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
