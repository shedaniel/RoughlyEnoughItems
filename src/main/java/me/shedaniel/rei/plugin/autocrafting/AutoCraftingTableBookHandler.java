/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.plugin.autocrafting;

import me.shedaniel.rei.api.AutoTransferHandler;
import me.shedaniel.rei.client.ScreenHelper;
import me.shedaniel.rei.listeners.RecipeBookGuiHooks;
import me.shedaniel.rei.plugin.crafting.DefaultCraftingDisplay;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.CraftingTableScreen;
import net.minecraft.container.CraftingTableContainer;

public class AutoCraftingTableBookHandler implements AutoTransferHandler {
    @Override
    public Result handle(Context context) {
        if (!(context.getContainerScreen() instanceof CraftingTableScreen) || !(context.getRecipe() instanceof DefaultCraftingDisplay))
            return Result.createNotApplicable();
        if (!((DefaultCraftingDisplay) context.getRecipe()).getOptionalRecipe().isPresent() || !context.getMinecraft().player.getRecipeBook().contains(((DefaultCraftingDisplay) context.getRecipe()).getOptionalRecipe().get()))
            return Result.createNotApplicable();
        if (!context.isActuallyCrafting())
            return Result.createSuccessful();
        
        DefaultCraftingDisplay display = (DefaultCraftingDisplay) context.getRecipe();
        CraftingTableScreen craftingTableScreen = (CraftingTableScreen) context.getContainerScreen();
        context.getMinecraft().openScreen(craftingTableScreen);
        ((RecipeBookGuiHooks) craftingTableScreen.getRecipeBookGui()).rei_getGhostSlots().reset();
        CraftingTableContainer container = craftingTableScreen.getContainer();
        context.getMinecraft().interactionManager.clickRecipe(container.syncId, display.getOptionalRecipe().get(), Screen.hasShiftDown());
        ScreenHelper.getLastOverlay().init();
        return Result.createSuccessful();
    }
}
