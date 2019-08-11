/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.plugin.autocrafting;

import me.shedaniel.rei.api.AutoTransferHandler;
import me.shedaniel.rei.impl.ScreenHelper;
import me.shedaniel.rei.listeners.RecipeBookGuiHooks;
import me.shedaniel.rei.plugin.crafting.DefaultCraftingDisplay;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.container.PlayerContainer;

public class AutoInventoryBookHandler implements AutoTransferHandler {
    @Override
    public Result handle(Context context) {
        if (!(context.getContainerScreen() instanceof InventoryScreen) || !(context.getRecipe() instanceof DefaultCraftingDisplay))
            return Result.createNotApplicable();
        if (!((DefaultCraftingDisplay) context.getRecipe()).getOptionalRecipe().isPresent() || !context.getMinecraft().player.getRecipeBook().contains(((DefaultCraftingDisplay) context.getRecipe()).getOptionalRecipe().get()))
            return Result.createNotApplicable();
        if (((DefaultCraftingDisplay) context.getRecipe()).getWidth() > 2 || ((DefaultCraftingDisplay) context.getRecipe()).getHeight() > 2)
            return Result.createFailed("error.rei.transfer.too_small");
        if (!context.isActuallyCrafting())
            return Result.createSuccessful();
        
        DefaultCraftingDisplay display = (DefaultCraftingDisplay) context.getRecipe();
        InventoryScreen inventoryScreen = (InventoryScreen) context.getContainerScreen();
        context.getMinecraft().openScreen(inventoryScreen);
        ((RecipeBookGuiHooks) inventoryScreen.getRecipeBookGui()).rei_getGhostSlots().reset();
        PlayerContainer container = inventoryScreen.getContainer();
        context.getMinecraft().interactionManager.clickRecipe(container.syncId, display.getOptionalRecipe().get(), Screen.hasShiftDown());
        ScreenHelper.getLastOverlay().init();
        return Result.createSuccessful();
    }
}
