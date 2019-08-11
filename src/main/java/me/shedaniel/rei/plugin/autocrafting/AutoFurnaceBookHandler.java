/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.plugin.autocrafting;

import me.shedaniel.rei.api.AutoTransferHandler;
import me.shedaniel.rei.impl.ScreenHelper;
import me.shedaniel.rei.listeners.RecipeBookGuiHooks;
import me.shedaniel.rei.plugin.smelting.DefaultSmeltingDisplay;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.FurnaceScreen;
import net.minecraft.container.FurnaceContainer;

public class AutoFurnaceBookHandler implements AutoTransferHandler {
    @Override
    public Result handle(Context context) {
        if (!(context.getContainerScreen() instanceof FurnaceScreen) || !(context.getRecipe() instanceof DefaultSmeltingDisplay))
            return Result.createNotApplicable();
        if (!((DefaultSmeltingDisplay) context.getRecipe()).getOptionalRecipe().isPresent() || !context.getMinecraft().player.getRecipeBook().contains(((DefaultSmeltingDisplay) context.getRecipe()).getOptionalRecipe().get()))
            return Result.createNotApplicable();
        if (!context.isActuallyCrafting())
            return Result.createSuccessful();
        
        DefaultSmeltingDisplay display = (DefaultSmeltingDisplay) context.getRecipe();
        FurnaceScreen furnaceScreen = (FurnaceScreen) context.getContainerScreen();
        context.getMinecraft().openScreen(furnaceScreen);
        ((RecipeBookGuiHooks) furnaceScreen.getRecipeBookGui()).rei_getGhostSlots().reset();
        FurnaceContainer container = furnaceScreen.getContainer();
        context.getMinecraft().interactionManager.clickRecipe(container.syncId, display.getOptionalRecipe().get(), Screen.hasShiftDown());
        ScreenHelper.getLastOverlay().init();
        return Result.createSuccessful();
    }
}
