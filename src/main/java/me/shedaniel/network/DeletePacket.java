package me.shedaniel.network;

import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.PacketByteBuf;

import java.io.IOException;

public class DeletePacket implements Packet<ServerPlayNetworkHandler> {
    
    @Override
    public void apply(ServerPlayNetworkHandler iNetHandlerPlayServer) {
        ServerPlayNetworkHandler server = (ServerPlayNetworkHandler) iNetHandlerPlayServer;
        ServerPlayerEntity player = server.player;
        
        if (!player.inventory.getCursorStack().isEmpty()) {
            player.inventory.setCursorStack(ItemStack.EMPTY);
        }
    }
    
    @Override
    public void read(PacketByteBuf packetByteBuf) throws IOException {
    
    }
    
    @Override
    public void write(PacketByteBuf packetByteBuf) throws IOException {
    
    }
    
}
