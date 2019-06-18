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
import net.minecraft.client.gui.screen.ingame.CraftingTableScreen;
import net.minecraft.container.CraftingTableContainer;

import java.util.function.Supplier;

public class AutoCraftingTableBookHandler implements AutoCraftingHandler {
    @Override
    public boolean handle(Supplier<RecipeDisplay> displaySupplier, MinecraftClient minecraft, Screen recipeViewingScreen, AbstractContainerScreen<?> parentScreen, ContainerScreenOverlay overlay) {
        DefaultCraftingDisplay display = (DefaultCraftingDisplay) displaySupplier.get();
        CraftingTableScreen craftingTableScreen = (CraftingTableScreen) parentScreen;
        minecraft.openScreen(craftingTableScreen);
        ((RecipeBookGuiHooks) craftingTableScreen.getRecipeBookGui()).rei_getGhostSlots().reset();
        CraftingTableContainer container = craftingTableScreen.getContainer();
        minecraft.interactionManager.clickRecipe(container.syncId, display.getOptionalRecipe().get(), Screen.hasShiftDown());
        ScreenHelper.getLastOverlay().init();
        return true;
    }
    
    @Override
    public boolean canHandle(Supplier<RecipeDisplay> displaySupplier, MinecraftClient minecraft, Screen recipeViewingScreen, AbstractContainerScreen<?> parentScreen, ContainerScreenOverlay overlay) {
        return parentScreen instanceof CraftingTableScreen && displaySupplier.get() instanceof DefaultCraftingDisplay && ((DefaultCraftingDisplay) displaySupplier.get()).getOptionalRecipe().isPresent() && minecraft.player.getRecipeBook().contains(((DefaultCraftingDisplay) displaySupplier.get()).getOptionalRecipe().get());
    }
}
