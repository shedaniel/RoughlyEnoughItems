package me.shedaniel.network;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayServer;

import java.io.IOException;

/**
 * Created by James on 7/29/2018.
 */
public class CheatPacket implements Packet<INetHandlerPlayServer> {
    
    private ItemStack stack;
    
    public CheatPacket() {
    }
    
    public CheatPacket(ItemStack stack) {
        this.stack = stack;
    }
    
    @Override
    public void readPacketData(PacketBuffer packetBuffer) throws IOException {
        stack = ItemStack.read(packetBuffer.readCompoundTag());
    }
    
    @Override
    public void writePacketData(PacketBuffer packetBuffer) throws IOException {
        NBTTagCompound tag = new NBTTagCompound();
        stack.write(tag);
        packetBuffer.writeCompoundTag(tag);
    }
    
    @Override
    public void processPacket(INetHandlerPlayServer iNetHandlerPlayServer) {
        NetHandlerPlayServer server = (NetHandlerPlayServer) iNetHandlerPlayServer;
        EntityPlayerMP player = server.player;
        player.inventory.addItemStackToInventory(stack);
    }
}
