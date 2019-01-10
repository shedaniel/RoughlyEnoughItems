package me.shedaniel.rei.mixin;

import me.shedaniel.rei.RoughlyEnoughItemsCore;
import me.shedaniel.rei.listeners.RecipeSync;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.packet.SynchronizeRecipesClientPacket;
import net.minecraft.recipe.RecipeManager;
import org.apache.logging.log4j.core.Core;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class MixinClientPlayNetworkHandler {
    
    @Shadow @Final private RecipeManager recipeManager;
    
    @Inject(method = "onSynchronizeRecipes", at = @At("RETURN"))
    private void onUpdateRecipes(SynchronizeRecipesClientPacket packetIn, CallbackInfo ci) {
        RoughlyEnoughItemsCore.getListeners(RecipeSync.class).forEach(recipeSync -> recipeSync.recipesLoaded(this.recipeManager));
    }
    
}
