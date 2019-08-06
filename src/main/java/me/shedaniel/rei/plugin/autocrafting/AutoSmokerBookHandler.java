/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.plugin.autocrafting;

import me.shedaniel.rei.api.AutoTransferHandler;
import me.shedaniel.rei.client.ScreenHelper;
import me.shedaniel.rei.listeners.RecipeBookGuiHooks;
import me.shedaniel.rei.plugin.smoking.DefaultSmokingDisplay;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.SmokerScreen;
import net.minecraft.container.SmokerContainer;

public class AutoSmokerBookHandler implements AutoTransferHandler {
    @Override
    public Result handle(Context context) {
        if (!(context.getContainerScreen() instanceof SmokerScreen) || !(context.getRecipe() instanceof DefaultSmokingDisplay))
            return Result.createNotApplicable();
        if (!((DefaultSmokingDisplay) context.getRecipe()).getOptionalRecipe().isPresent() || !context.getMinecraft().player.getRecipeBook().contains(((DefaultSmokingDisplay) context.getRecipe()).getOptionalRecipe().get()))
            return Result.createNotApplicable();
        if (!context.isActuallyCrafting())
            return Result.createSuccessful();
        
        DefaultSmokingDisplay display = (DefaultSmokingDisplay) context.getRecipe();
        SmokerScreen smokerScreen = (SmokerScreen) context.getContainerScreen();
        context.getMinecraft().openScreen(smokerScreen);
        ((RecipeBookGuiHooks) smokerScreen.getRecipeBookGui()).rei_getGhostSlots().reset();
        SmokerContainer container = smokerScreen.getContainer();
        context.getMinecraft().interactionManager.clickRecipe(container.syncId, display.getOptionalRecipe().get(), Screen.hasShiftDown());
        ScreenHelper.getLastOverlay().init();
        return Result.createSuccessful();
    }
}
