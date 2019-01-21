package me.shedaniel.rei.network;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayServer;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;

import java.io.IOException;

public class CreateItemsPacket implements Packet<INetHandlerPlayServer> {
    
    private ItemStack stack;
    
    public CreateItemsPacket() {
    }
    
    public CreateItemsPacket(ItemStack stack) {
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
        if (player.inventory.addItemStackToInventory(stack.copy()))
            player.sendMessage(new TextComponentString(I18n.format("text.rei.cheat_items")
                    .replaceAll("\\{item_name}", stack.copy().getDisplayName().getFormattedText())
                    .replaceAll("\\{item_count}", stack.copy().getCount() + "")
                    .replaceAll("\\{player_name}", player.getScoreboardName())
            ), ChatType.SYSTEM);
        else
            player.sendMessage(new TextComponentTranslation("text.rei.failed_cheat_items"), ChatType.SYSTEM);
    }
    
}
