package me.shedaniel.rei.mixin;

import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.packet.GameStateChangeS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class MixinClientPlayerNetworkHandler {
    
    @Inject(method = "onGameStateChange", at = @At("RETURN"))
    public void onGameStateChange(GameStateChangeS2CPacket packet, CallbackInfo callbackInfo) {
        switch (packet.getReason()) {
            case 1:
                System.out.println("End Raining");
                break;
            case 2:
                System.out.println("Start Raining");
                break;
            case 7:
                System.out.println("Change rain color to " + packet.getValue());
                break;
            case 8:
                System.out.println("Change thunder color to " + packet.getValue());
                break;
        }
    }
    
}
