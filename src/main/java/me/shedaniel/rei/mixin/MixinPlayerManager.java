package me.shedaniel.rei.mixin;

import io.netty.buffer.Unpooled;
import me.shedaniel.rei.RoughlyEnoughItemsNetwork;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.PacketByteBuf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerManager.class)
public class MixinPlayerManager {
    @Inject(method = "onPlayerConnect", at = @At("TAIL"))
    public void onPlayerConnect(ClientConnection connection, ServerPlayerEntity player, CallbackInfo callback) {
        ServerSidePacketRegistry.INSTANCE.sendToPlayer(player, RoughlyEnoughItemsNetwork.REI_ON_SERVER_PACKET, new PacketByteBuf(Unpooled.buffer()));
    }
}
