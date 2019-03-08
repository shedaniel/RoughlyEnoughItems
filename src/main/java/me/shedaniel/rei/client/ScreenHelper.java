package me.shedaniel.rei.client;

import com.google.common.collect.Lists;
import me.shedaniel.rei.RoughlyEnoughItemsCore;
import me.shedaniel.rei.gui.ContainerScreenOverlay;
import me.shedaniel.rei.gui.widget.TextFieldWidget;
import me.shedaniel.rei.listeners.ContainerScreenHooks;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.event.client.ClientTickCallback;
import net.minecraft.client.gui.ContainerScreen;
import net.minecraft.client.gui.InputListener;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.RecipeBookButtonWidget;
import net.minecraft.item.ItemStack;

import java.util.List;

public class ScreenHelper implements ClientModInitializer {
    
    public static TextFieldWidget searchField;
    public static List<ItemStack> inventoryStacks = Lists.newArrayList();
    private static boolean overlayVisible = true;
    private static ContainerScreenOverlay overlay;
    private static ContainerScreen lastContainerScreen;
    
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
    
    public static void disableRecipeBook(ContainerScreen lastContainerScreen, List<InputListener> listeners, List<ButtonWidget> buttonWidgets) {
        RoughlyEnoughItemsCore.LOGGER.info("%d %d", listeners.size(), buttonWidgets.size());
        for(InputListener listener : listeners)
            if (listener instanceof RecipeBookButtonWidget)
                listeners.remove(listener);
        for(ButtonWidget buttonWidget : buttonWidgets)
            if (buttonWidget instanceof RecipeBookButtonWidget)
                buttonWidgets.remove(buttonWidget);
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
    
    @Override
    public void onInitializeClient() {
        ClientTickCallback.EVENT.register(client -> {
            if (lastContainerScreen != client.currentScreen && client.currentScreen instanceof ContainerScreen)
                lastContainerScreen = (ContainerScreen) client.currentScreen;
        });
    }
    
}
