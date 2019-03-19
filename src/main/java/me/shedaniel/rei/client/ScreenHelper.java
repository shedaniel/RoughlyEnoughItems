package me.shedaniel.rei.client;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlStateManager;
import me.shedaniel.rei.RoughlyEnoughItemsCore;
import me.shedaniel.rei.gui.ContainerScreenOverlay;
import me.shedaniel.rei.gui.widget.TextFieldWidget;
import me.shedaniel.rei.listeners.ContainerScreenHooks;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.event.client.ClientTickCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.ContainerScreen;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.InputListener;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.RecipeBookButtonWidget;
import net.minecraft.client.render.GuiLighting;
import net.minecraft.client.util.Window;
import net.minecraft.item.ItemStack;

import java.awt.*;
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
    
    public static ContainerScreenOverlay getLastOverlay(boolean reset, boolean setPage) {
        if (overlay == null || reset) {
            overlay = new ContainerScreenOverlay();
            overlay.onInitialized(setPage);
        }
        return overlay;
    }
    
    public static ContainerScreenOverlay getLastOverlay() {
        return getLastOverlay(false, false);
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
    
    public static void drawHoveringWidget(int x, int y, Drawable drawable, int width, int height, float delta) {
        Window window = MinecraftClient.getInstance().window;
        drawHoveringWidget(new Dimension(window.getScaledWidth(), window.getScaledHeight()), x, y, drawable, width, height, delta);
    }
    
    public static void drawHoveringWidget(Dimension dimension, int x, int y, Drawable drawable, int width, int height, float delta) {
        int int_5 = x + 12;
        int int_6 = y - 12;
        
        if (int_5 + width > dimension.width)
            int_5 -= 28 + width;
        if (int_6 + height + 6 > dimension.height)
            int_6 = dimension.height - height - 6;
        
        drawable.draw(int_5, int_6, delta);
        //        zOffset = 300.0F;
        //        itemRenderer.zOffset = 300.0F;
        //        int int_9 = -267386864;
        //        drawGradientRect(int_5 - 3, int_6 - 4, int_5 + width + 3, int_6 - 3, -267386864, -267386864);
        //        drawGradientRect(int_5 - 3, int_6 + height + 3, int_5 + width + 3, int_6 + height + 4, -267386864, -267386864);
        //        drawGradientRect(int_5 - 3, int_6 - 3, int_5 + width + 3, int_6 + height + 3, -267386864, -267386864);
        //        drawGradientRect(int_5 - 4, int_6 - 3, int_5 - 3, int_6 + height + 3, -267386864, -267386864);
        //        drawGradientRect(int_5 + width + 3, int_6 - 3, int_5 + width + 4, int_6 + height + 3, -267386864, -267386864);
        //        int int_10 = 1347420415;
        //        int int_11 = 1344798847;
        //        drawGradientRect(int_5 - 3, int_6 - 3 + 1, int_5 - 3 + 1, int_6 + height + 3 - 1, 1347420415, 1344798847);
        //        drawGradientRect(int_5 + width + 2, int_6 - 3 + 1, int_5 + width + 3, int_6 + height + 3 - 1, 1347420415, 1344798847);
        //        drawGradientRect(int_5 - 3, int_6 - 3, int_5 + width + 3, int_6 - 3 + 1, 1347420415, 1347420415);
        //        drawGradientRect(int_5 - 3, int_6 + height + 2, int_5 + width + 3, int_6 + height + 3, 1344798847, 1344798847);
    }
    
    @Override
    public void onInitializeClient() {
        ClientTickCallback.EVENT.register(client -> {
            if (lastContainerScreen != client.currentScreen && client.currentScreen instanceof ContainerScreen)
                lastContainerScreen = (ContainerScreen) client.currentScreen;
        });
    }
    
}
