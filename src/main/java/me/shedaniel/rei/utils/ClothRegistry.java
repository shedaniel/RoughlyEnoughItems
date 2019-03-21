package me.shedaniel.rei.utils;

import me.shedaniel.cloth.api.ClientUtils;
import me.shedaniel.cloth.hooks.ClothClientHooks;
import me.shedaniel.rei.RoughlyEnoughItemsCore;
import me.shedaniel.rei.api.TabGetter;
import me.shedaniel.rei.client.RecipeHelperImpl;
import me.shedaniel.rei.client.ScreenHelper;
import me.shedaniel.rei.gui.ContainerScreenOverlay;
import net.minecraft.client.gui.ContainerScreen;
import net.minecraft.client.gui.ingame.CreativePlayerInventoryScreen;
import net.minecraft.client.gui.widget.RecipeBookButtonWidget;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.ActionResult;

public class ClothRegistry {
    
    public static void register() {
        ClothClientHooks.SYNC_RECIPES.register((minecraftClient, recipeManager, synchronizeRecipesS2CPacket) -> {
            ((RecipeHelperImpl) RoughlyEnoughItemsCore.getRecipeHelper()).recipesLoaded(recipeManager);
        });
        ClothClientHooks.SCREEN_ADD_BUTTON.register((minecraftClient, screen, abstractButtonWidget) -> {
            if (RoughlyEnoughItemsCore.getConfigManager().getConfig().disableRecipeBook && screen instanceof ContainerScreen && abstractButtonWidget instanceof RecipeBookButtonWidget)
                return ActionResult.FAIL;
            return ActionResult.PASS;
        });
        ClothClientHooks.SCREEN_INIT_POST.register((minecraftClient, screen, screenHooks) -> {
            if (screen instanceof ContainerScreen) {
                if (screen instanceof CreativePlayerInventoryScreen) {
                    TabGetter tabGetter = (TabGetter) screen;
                    if (tabGetter.rei_getSelectedTab() != ItemGroup.INVENTORY.getIndex())
                        return;
                }
                ScreenHelper.setLastContainerScreen((ContainerScreen) screen);
                screenHooks.cloth_getInputListeners().add(ScreenHelper.getLastOverlay(true, false));
            }
        });
        ClothClientHooks.SCREEN_RENDER_POST.register((minecraftClient, screen, i, i1, v) -> {
            if (screen instanceof ContainerScreen) {
                if (screen instanceof CreativePlayerInventoryScreen) {
                    TabGetter tabGetter = (TabGetter) screen;
                    if (tabGetter.rei_getSelectedTab() != ItemGroup.INVENTORY.getIndex())
                        return;
                }
                ScreenHelper.getLastOverlay().drawOverlay(i, i1, v);
            }
        });
        ClothClientHooks.SCREEN_MOUSE_SCROLLED.register((minecraftClient, screen, v, v1, v2) -> {
            if (screen instanceof ContainerScreen && !(screen instanceof CreativePlayerInventoryScreen)) {
                ContainerScreenOverlay overlay = ScreenHelper.getLastOverlay();
                if (ScreenHelper.isOverlayVisible() && ContainerScreenOverlay.getItemListOverlay().getListArea().contains(ClientUtils.getMouseLocation()))
                    if (overlay.mouseScrolled(v, v1, v2))
                        return ActionResult.SUCCESS;
            }
            return ActionResult.PASS;
        });
    }
    
}
