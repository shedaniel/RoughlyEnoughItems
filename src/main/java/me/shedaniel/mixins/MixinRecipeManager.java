package me.shedaniel.mixins;

import me.shedaniel.listenerdefinitions.RecipeLoadListener;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.network.play.server.SPacketUpdateRecipes;
import org.dimdev.riftloader.RiftLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NetHandlerPlayClient.class)
public class MixinRecipeManager {
    
    @Shadow
    RecipeManager recipeManager;
    
    @Inject(method = "handleUpdateRecipes", at = @At("RETURN"))
    private void onUpdateRecipies(SPacketUpdateRecipes packetIn, CallbackInfo ci) {
        for(RecipeLoadListener listener : RiftLoader.instance.getListeners(RecipeLoadListener.class)) {
            listener.recipesLoaded(recipeManager);
        }
    }
}
