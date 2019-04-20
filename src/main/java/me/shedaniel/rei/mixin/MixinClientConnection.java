package me.shedaniel.rei.mixin;

import io.netty.channel.Channel;
import me.shedaniel.rei.RoughlyEnoughItemsCore;
import net.minecraft.network.ClientConnection;
import net.minecraft.text.TextComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientConnection.class)
public class MixinClientConnection {
    
    @Shadow
    private Channel channel;
    
    @Inject(method = "disconnect", at = @At("HEAD"))
    public void disconnect(TextComponent reason, CallbackInfo callback) {
        if (channel.isOpen())
            RoughlyEnoughItemsCore.reiIsOnServer = false;
    }
    
}
