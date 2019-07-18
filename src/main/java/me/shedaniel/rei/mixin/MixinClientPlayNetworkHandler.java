package me.shedaniel.rei.mixin;

import me.shedaniel.rei.RoughlyEnoughItemsClient;
import me.shedaniel.rei.RoughlyEnoughItemsCore;
import me.shedaniel.rei.client.RecipeHelperImpl;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.network.play.server.SPacketUpdateRecipes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.CompletableFuture;

@Mixin(NetHandlerPlayClient.class)
public class MixinClientPlayNetworkHandler {
    
    @Shadow @Final private RecipeManager recipeManager;
    
    @Inject(method = "handleUpdateRecipes", at = @At("RETURN"))
    private void handleUpdateRecipes(SPacketUpdateRecipes packet, CallbackInfo ci) {
        if (RoughlyEnoughItemsClient.getConfigManager().getConfig().registerRecipesInAnotherThread)
            CompletableFuture.runAsync(() -> ((RecipeHelperImpl) RoughlyEnoughItemsCore.getRecipeHelper()).recipesLoaded(recipeManager), RoughlyEnoughItemsCore.SYNC_RECIPES);
        else
            ((RecipeHelperImpl) RoughlyEnoughItemsCore.getRecipeHelper()).recipesLoaded(recipeManager);
    }
    
}
