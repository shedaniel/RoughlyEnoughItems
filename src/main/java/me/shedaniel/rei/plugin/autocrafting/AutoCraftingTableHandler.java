package me.shedaniel.rei.plugin.autocrafting;

import me.shedaniel.rei.api.AutoCraftingHandler;
import me.shedaniel.rei.client.ScreenHelper;
import me.shedaniel.rei.gui.ContainerScreenOverlay;
import me.shedaniel.rei.listeners.RecipeBookGuiHooks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.AbstractContainerScreen;
import net.minecraft.client.gui.screen.ingame.CraftingTableScreen;
import net.minecraft.container.CraftingTableContainer;

public class AutoCraftingTableHandler implements AutoCraftingHandler {
    @Override
    public boolean handle(MinecraftClient minecraft, Screen recipeViewingScreen, AbstractContainerScreen<?> parentScreen, ContainerScreenOverlay overlay) {
        if (parentScreen instanceof CraftingTableScreen) {
            CraftingTableScreen craftingTableScreen = (CraftingTableScreen) parentScreen;
            minecraft.openScreen(craftingTableScreen);
            ((RecipeBookGuiHooks) craftingTableScreen.getRecipeBookGui()).rei_getGhostSlots().reset();
            CraftingTableContainer container = craftingTableScreen.getContainer();
            ScreenHelper.getLastOverlay().init();
            return true;
        }
        return false;
    }
}
