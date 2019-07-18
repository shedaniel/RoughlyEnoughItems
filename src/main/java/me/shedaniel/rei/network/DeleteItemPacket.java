package me.shedaniel.rei.network;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayServer;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;

import java.io.IOException;

public class DeleteItemPacket implements Packet<INetHandlerPlayServer> {
    @Override
    public void readPacketData(PacketBuffer buf) throws IOException {
    
    }
    
    @Override
    public void writePacketData(PacketBuffer buf) throws IOException {
    
    }
    
    @Override
    public void processPacket(INetHandlerPlayServer handler) {
        EntityPlayerMP player = ((NetHandlerPlayServer) handler).player;
        if (player.getServer().getPermissionLevel(player.getGameProfile()) < player.getServer().getOpPermissionLevel()) {
            player.sendStatusMessage(new TextComponentTranslation("text.rei.no_permission_cheat").applyTextStyle(TextFormatting.RED), false);
            return;
        }
        if (!player.inventory.getItemStack().isEmpty())
            player.inventory.setItemStack(ItemStack.EMPTY);
    }
}
