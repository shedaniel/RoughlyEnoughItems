/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.plugin.autocrafting;

import me.shedaniel.rei.api.AutoTransferHandler;
import me.shedaniel.rei.impl.ScreenHelper;
import me.shedaniel.rei.listeners.RecipeBookGuiHooks;
import me.shedaniel.rei.plugin.blasting.DefaultBlastingDisplay;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.BlastFurnaceScreen;
import net.minecraft.container.BlastFurnaceContainer;

public class AutoBlastingBookHandler implements AutoTransferHandler {
    @Override
    public Result handle(Context context) {
        if (!(context.getContainerScreen() instanceof BlastFurnaceScreen) || !(context.getRecipe() instanceof DefaultBlastingDisplay))
            return Result.createNotApplicable();
        if (!((DefaultBlastingDisplay) context.getRecipe()).getOptionalRecipe().isPresent() || !context.getMinecraft().player.getRecipeBook().contains(((DefaultBlastingDisplay) context.getRecipe()).getOptionalRecipe().get()))
            return Result.createNotApplicable();
        if (!context.isActuallyCrafting())
            return Result.createSuccessful();
        
        DefaultBlastingDisplay display = (DefaultBlastingDisplay) context.getRecipe();
        BlastFurnaceScreen furnaceScreen = (BlastFurnaceScreen) context.getContainerScreen();
        context.getMinecraft().openScreen(furnaceScreen);
        ((RecipeBookGuiHooks) furnaceScreen.getRecipeBookGui()).rei_getGhostSlots().reset();
        BlastFurnaceContainer container = furnaceScreen.getContainer();
        context.getMinecraft().interactionManager.clickRecipe(container.syncId, display.getOptionalRecipe().get(), Screen.hasShiftDown());
        ScreenHelper.getLastOverlay().init();
        return Result.createSuccessful();
    }
}
