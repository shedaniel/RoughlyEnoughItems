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
import me.shedaniel.rei.plugin.smelting.DefaultSmeltingDisplay;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.AbstractContainerScreen;
import net.minecraft.client.gui.screen.ingame.FurnaceScreen;
import net.minecraft.container.FurnaceContainer;

import java.util.function.Supplier;

public class AutoFurnaceBookHandler implements AutoCraftingHandler {
    @Override
    public boolean handle(Supplier<RecipeDisplay> displaySupplier, MinecraftClient minecraft, Screen recipeViewingScreen, AbstractContainerScreen<?> parentScreen, ContainerScreenOverlay overlay) {
        DefaultSmeltingDisplay display = (DefaultSmeltingDisplay) displaySupplier.get();
        FurnaceScreen furnaceScreen = (FurnaceScreen) parentScreen;
        minecraft.openScreen(furnaceScreen);
        ((RecipeBookGuiHooks) furnaceScreen.getRecipeBookGui()).rei_getGhostSlots().reset();
        FurnaceContainer container = furnaceScreen.getContainer();
        minecraft.interactionManager.clickRecipe(container.syncId, display.getOptionalRecipe().get(), Screen.hasShiftDown());
        ScreenHelper.getLastOverlay().init();
        return true;
    }
    
    @Override
    public boolean canHandle(Supplier<RecipeDisplay> displaySupplier, MinecraftClient minecraft, Screen recipeViewingScreen, AbstractContainerScreen<?> parentScreen, ContainerScreenOverlay overlay) {
        return parentScreen instanceof FurnaceScreen && displaySupplier.get() instanceof DefaultSmeltingDisplay && ((DefaultSmeltingDisplay) displaySupplier.get()).getOptionalRecipe().isPresent() && minecraft.player.getRecipeBook().contains(((DefaultSmeltingDisplay) displaySupplier.get()).getOptionalRecipe().get());
    }
}
