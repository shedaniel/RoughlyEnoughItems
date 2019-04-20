package me.shedaniel.rei.mixin;

import me.shedaniel.rei.RoughlyEnoughItemsCore;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.packet.GameJoinS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class MixinClientPlayNetworkHandler {
    
    @Inject(method = "onGameJoin", at = @At("HEAD"))
    public void onGameJoin(GameJoinS2CPacket packet, CallbackInfo callbackInfo) {
        RoughlyEnoughItemsCore.reiIsOnServer = false;
    }
    
}
