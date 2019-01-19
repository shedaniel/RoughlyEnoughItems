package me.shedaniel.rei.network;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayServer;

import java.io.IOException;

public class DeleteItemsPacket implements Packet<INetHandlerPlayServer> {
    
    @Override
    public void readPacketData(PacketBuffer packetBuffer) throws IOException {
    
    }
    
    @Override
    public void writePacketData(PacketBuffer packetBuffer) throws IOException {
    
    }
    
    @Override
    public void processPacket(INetHandlerPlayServer iNetHandlerPlayServer) {
        NetHandlerPlayServer server = (NetHandlerPlayServer) iNetHandlerPlayServer;
        EntityPlayerMP player = server.player;
        
        if (!player.inventory.getItemStack().isEmpty()) {
            player.inventory.setItemStack(ItemStack.EMPTY);
        }
    }
    
}
