package me.shedaniel.rei.mixin;

import me.shedaniel.rei.RoughlyEnoughItemsCore;
import me.shedaniel.rei.client.RecipeHelperImpl;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.play.server.SPacketUpdateRecipes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NetHandlerPlayClient.class)
public class MixinClientPlayNetworkHandler {
    
    @Inject(method = "handleUpdateRecipes", at = @At("RETURN"))
    private void onUpdateRecipes(SPacketUpdateRecipes packetIn, CallbackInfo ci) {
        ((RecipeHelperImpl) RoughlyEnoughItemsCore.getRecipeHelper()).recipesLoaded(((NetHandlerPlayClient) ((Object) this)).getRecipeManager());
    }
    
}
