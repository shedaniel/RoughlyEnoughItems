package me.shedaniel.rei.utils;

import me.shedaniel.cloth.api.EventPriority;
import me.shedaniel.cloth.hooks.ClothHooks;
import me.shedaniel.rei.RoughlyEnoughItemsCore;
import me.shedaniel.rei.api.TabGetter;
import me.shedaniel.rei.client.RecipeHelperImpl;
import me.shedaniel.rei.client.ScreenHelper;
import net.minecraft.client.gui.ContainerScreen;
import net.minecraft.client.gui.ingame.CreativePlayerInventoryScreen;
import net.minecraft.client.gui.widget.RecipeBookButtonWidget;
import net.minecraft.item.ItemGroup;

public class ClothRegistry {
    
    public static void register() {
        ClothHooks.CLIENT_SYNC_RECIPES.registerListener(event -> {
            ((RecipeHelperImpl) RoughlyEnoughItemsCore.getRecipeHelper()).recipesLoaded(event.getManager());
        });
        ClothHooks.CLIENT_SCREEN_ADD_BUTTON.registerListener(event -> {
            if (RoughlyEnoughItemsCore.getConfigManager().getConfig().disableRecipeBook && event.getScreen() instanceof ContainerScreen && event.getButtonWidget() instanceof RecipeBookButtonWidget)
                event.setCancelled(true);
        }, EventPriority.LOWEST);
        ClothHooks.CLIENT_POST_INIT_SCREEN.registerListener(post -> {
            if (post.getScreen() instanceof ContainerScreen) {
                if (post.getScreen() instanceof CreativePlayerInventoryScreen) {
                    TabGetter tabGetter = (TabGetter) post.getScreen();
                    if (tabGetter.rei_getSelectedTab() != ItemGroup.INVENTORY.getIndex())
                        return;
                }
                ScreenHelper.setLastContainerScreen((ContainerScreen) post.getScreen());
                post.getInputListeners().add(ScreenHelper.getLastOverlay(true, false));
            }
        }, EventPriority.LOWEST);
        ClothHooks.CLIENT_POST_DRAW_SCREEN.registerListener(post -> {
            if (post.getScreen() instanceof ContainerScreen) {
                if (post.getScreen() instanceof CreativePlayerInventoryScreen) {
                    TabGetter tabGetter = (TabGetter) post.getScreen();
                    if (tabGetter.rei_getSelectedTab() != ItemGroup.INVENTORY.getIndex())
                        return;
                }
                ScreenHelper.getLastOverlay().drawOverlay(post.getMouseX(), post.getMouseY(), post.getDelta());
            }
        }, EventPriority.LOWEST);
    }
    
}
