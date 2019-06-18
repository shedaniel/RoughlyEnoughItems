/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.plugin.autocrafting;

import me.shedaniel.rei.api.AutoCraftingHandler;
import me.shedaniel.rei.api.RecipeDisplay;
import me.shedaniel.rei.client.ScreenHelper;
import me.shedaniel.rei.gui.ContainerScreenOverlay;
import me.shedaniel.rei.listeners.RecipeBookGuiHooks;
import me.shedaniel.rei.plugin.crafting.DefaultCraftingDisplay;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.AbstractContainerScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.container.PlayerContainer;

import java.util.function.Supplier;

public class AutoInventoryBookHandler implements AutoCraftingHandler {
    @Override
    public boolean handle(Supplier<RecipeDisplay> displaySupplier, MinecraftClient minecraft, Screen recipeViewingScreen, AbstractContainerScreen<?> parentScreen, ContainerScreenOverlay overlay) {
        DefaultCraftingDisplay display = (DefaultCraftingDisplay) displaySupplier.get();
        InventoryScreen inventoryScreen = (InventoryScreen) parentScreen;
        minecraft.openScreen(inventoryScreen);
        ((RecipeBookGuiHooks) inventoryScreen.getRecipeBookGui()).rei_getGhostSlots().reset();
        PlayerContainer container = inventoryScreen.getContainer();
        minecraft.interactionManager.clickRecipe(container.syncId, display.getOptionalRecipe().get(), Screen.hasShiftDown());
        ScreenHelper.getLastOverlay().init();
        return true;
    }
    
    @Override
    public boolean canHandle(Supplier<RecipeDisplay> displaySupplier, MinecraftClient minecraft, Screen recipeViewingScreen, AbstractContainerScreen<?> parentScreen, ContainerScreenOverlay overlay) {
        return parentScreen instanceof InventoryScreen && displaySupplier.get() instanceof DefaultCraftingDisplay && ((DefaultCraftingDisplay) displaySupplier.get()).getOptionalRecipe().isPresent() && minecraft.player.getRecipeBook().contains(((DefaultCraftingDisplay) displaySupplier.get()).getOptionalRecipe().get()) && ((DefaultCraftingDisplay) displaySupplier.get()).getWidth() <= 2 && ((DefaultCraftingDisplay) displaySupplier.get()).getHeight() <= 2;
    }
}
