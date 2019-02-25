package me.shedaniel.rei.mixin;

import me.shedaniel.rei.RoughlyEnoughItemsCore;
import me.shedaniel.rei.client.RecipeHelper;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.packet.SynchronizeRecipesS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class MixinClientPlayNetworkHandler {
    
    @Inject(method = "onSynchronizeRecipes", at = @At("RETURN"))
    private void onUpdateRecipes(SynchronizeRecipesS2CPacket packetIn, CallbackInfo ci) {
        ((RecipeHelper) RoughlyEnoughItemsCore.getRecipeHelper()).recipesLoaded(((ClientPlayNetworkHandler) ((Object) this)).getRecipeManager());
    }
    
}
