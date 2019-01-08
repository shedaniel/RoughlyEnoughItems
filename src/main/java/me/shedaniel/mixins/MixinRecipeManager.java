package me.shedaniel.mixins;

import me.shedaniel.Core;
import me.shedaniel.listenerdefinitions.RecipeLoadListener;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.packet.SynchronizeRecipesClientPacket;
import net.minecraft.recipe.RecipeManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class MixinRecipeManager {
    
    @Shadow
    RecipeManager recipeManager;
    
    @Inject(method = "onSynchronizeRecipes", at = @At("RETURN"))
    private void onUpdateRecipies(SynchronizeRecipesClientPacket packetIn, CallbackInfo ci) {
        for(RecipeLoadListener listener : Core.getListeners(RecipeLoadListener.class)) {
            listener.recipesLoaded(recipeManager);
        }
    }
    
}
