/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.plugin.autocrafting;

import me.shedaniel.rei.api.AutoCraftingHandler;
import me.shedaniel.rei.api.RecipeDisplay;
import me.shedaniel.rei.client.ScreenHelper;
import me.shedaniel.rei.gui.ContainerScreenOverlay;
import me.shedaniel.rei.api.RecipeBookGuiHooks;
import me.shedaniel.rei.plugin.crafting.DefaultCraftingDisplay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.inventory.Container;

import java.util.function.Supplier;

public class AutoInventoryBookHandler implements AutoCraftingHandler {
    @Override
    public boolean handle(Supplier<RecipeDisplay> displaySupplier, Minecraft minecraft, GuiScreen recipeViewingScreen, GuiContainer parentScreen, ContainerScreenOverlay overlay) {
        DefaultCraftingDisplay display = (DefaultCraftingDisplay) displaySupplier.get();
        GuiInventory inventoryScreen = (GuiInventory) parentScreen;
        minecraft.displayGuiScreen(inventoryScreen);
        ((RecipeBookGuiHooks) inventoryScreen.func_194310_f()).rei_getGhostSlots().clear();
        Container container = inventoryScreen.inventorySlots;
        minecraft.playerController.func_203413_a(container.windowId, display.getOptionalRecipe().get(), GuiScreen.isShiftKeyDown());
        ScreenHelper.getLastOverlay().init();
        return true;
    }
    
    @Override
    public boolean canHandle(Supplier<RecipeDisplay> displaySupplier, Minecraft minecraft, GuiScreen recipeViewingScreen, GuiContainer parentScreen, ContainerScreenOverlay overlay) {
        return parentScreen instanceof GuiInventory && displaySupplier.get() instanceof DefaultCraftingDisplay && ((DefaultCraftingDisplay) displaySupplier.get()).getOptionalRecipe().isPresent() && minecraft.player.getRecipeBook().isUnlocked(((DefaultCraftingDisplay) displaySupplier.get()).getOptionalRecipe().get()) && ((DefaultCraftingDisplay) displaySupplier.get()).getWidth() <= 2 && ((DefaultCraftingDisplay) displaySupplier.get()).getHeight() <= 2;
    }
}
