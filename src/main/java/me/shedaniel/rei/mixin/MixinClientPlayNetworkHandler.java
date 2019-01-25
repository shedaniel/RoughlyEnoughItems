package me.shedaniel.rei.mixin;

import me.shedaniel.rei.RoughlyEnoughItemsCore;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.packet.SynchronizeRecipesClientPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class MixinClientPlayNetworkHandler {
    
    @Inject(method = "onSynchronizeRecipes", at = @At("RETURN"))
    private void onUpdateRecipes(SynchronizeRecipesClientPacket packetIn, CallbackInfo ci) {
        RoughlyEnoughItemsCore.getRecipeHelper().recipesLoaded(((ClientPlayNetworkHandler) ((Object) this)).getRecipeManager());
    }
    
}
