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
import me.shedaniel.rei.plugin.blasting.DefaultBlastingDisplay;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.AbstractContainerScreen;
import net.minecraft.client.gui.screen.ingame.BlastFurnaceScreen;
import net.minecraft.container.BlastFurnaceContainer;

import java.util.function.Supplier;

public class AutoBlastingBookHandler implements AutoCraftingHandler {
    @Override
    public boolean handle(Supplier<RecipeDisplay> displaySupplier, MinecraftClient minecraft, Screen recipeViewingScreen, AbstractContainerScreen<?> parentScreen, ContainerScreenOverlay overlay) {
        DefaultBlastingDisplay display = (DefaultBlastingDisplay) displaySupplier.get();
        BlastFurnaceScreen furnaceScreen = (BlastFurnaceScreen) parentScreen;
        minecraft.openScreen(furnaceScreen);
        ((RecipeBookGuiHooks) furnaceScreen.getRecipeBookGui()).rei_getGhostSlots().reset();
        BlastFurnaceContainer container = furnaceScreen.getContainer();
        minecraft.interactionManager.clickRecipe(container.syncId, display.getOptionalRecipe().get(), Screen.hasShiftDown());
        ScreenHelper.getLastOverlay().init();
        return true;
    }
    
    @Override
    public boolean canHandle(Supplier<RecipeDisplay> displaySupplier, MinecraftClient minecraft, Screen recipeViewingScreen, AbstractContainerScreen<?> parentScreen, ContainerScreenOverlay overlay) {
        return parentScreen instanceof BlastFurnaceScreen && displaySupplier.get() instanceof DefaultBlastingDisplay && ((DefaultBlastingDisplay) displaySupplier.get()).getOptionalRecipe().isPresent() && minecraft.player.getRecipeBook().contains(((DefaultBlastingDisplay) displaySupplier.get()).getOptionalRecipe().get());
    }
}
