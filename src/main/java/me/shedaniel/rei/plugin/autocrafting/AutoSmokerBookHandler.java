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
import me.shedaniel.rei.plugin.smoking.DefaultSmokingDisplay;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.AbstractContainerScreen;
import net.minecraft.client.gui.screen.ingame.SmokerScreen;
import net.minecraft.container.SmokerContainer;

import java.util.function.Supplier;

public class AutoSmokerBookHandler implements AutoCraftingHandler {
    @Override
    public boolean handle(Supplier<RecipeDisplay> displaySupplier, MinecraftClient minecraft, Screen recipeViewingScreen, AbstractContainerScreen<?> parentScreen, ContainerScreenOverlay overlay) {
        DefaultSmokingDisplay display = (DefaultSmokingDisplay) displaySupplier.get();
        SmokerScreen smokerScreen = (SmokerScreen) parentScreen;
        minecraft.openScreen(smokerScreen);
        ((RecipeBookGuiHooks) smokerScreen.getRecipeBookGui()).rei_getGhostSlots().reset();
        SmokerContainer container = smokerScreen.getContainer();
        minecraft.interactionManager.clickRecipe(container.syncId, display.getOptionalRecipe().get(), Screen.hasShiftDown());
        ScreenHelper.getLastOverlay().init();
        return true;
    }
    
    @Override
    public boolean canHandle(Supplier<RecipeDisplay> displaySupplier, MinecraftClient minecraft, Screen recipeViewingScreen, AbstractContainerScreen<?> parentScreen, ContainerScreenOverlay overlay) {
        return parentScreen instanceof SmokerScreen && displaySupplier.get() instanceof DefaultSmokingDisplay && ((DefaultSmokingDisplay) displaySupplier.get()).getOptionalRecipe().isPresent() && minecraft.player.getRecipeBook().contains(((DefaultSmokingDisplay) displaySupplier.get()).getOptionalRecipe().get());
    }
}
