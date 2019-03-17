package me.shedaniel.rei.network;

import me.shedaniel.rei.gui.widget.ItemListOverlay;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;

import java.io.IOException;

public class CreateItemPacket implements Packet<INetHandlerPlayServer> {
    
    private ItemStack stack;
    
    public CreateItemPacket(ItemStack stack) {
        this.stack = stack;
    }
    
    @Override
    public void readPacketData(PacketBuffer buf) throws IOException {
        stack = ItemStack.read(buf.readCompoundTag());
    }
    
    @Override
    public void writePacketData(PacketBuffer buf) throws IOException {
        NBTTagCompound tag = new NBTTagCompound();
        stack.write(tag);
        buf.writeCompoundTag(tag);
    }
    
    @Override
    public void processPacket(INetHandlerPlayServer handler) {
        EntityPlayerMP player = ((NetHandlerPlayServer) handler).player;
        if (player.inventory.addItemStackToInventory(stack.copy()))
            player.sendStatusMessage(new TextComponentString(I18n.format("text.rei.cheat_items").replaceAll("\\{item_name}", ItemListOverlay.tryGetItemStackName(stack.copy())).replaceAll("\\{item_count}", stack.copy().getCount() + "").replaceAll("\\{player_name}", player.getScoreboardName())), false);
        else
            player.sendStatusMessage(new TextComponentTranslation("text.rei.failed_cheat_items"), false);
    }
    
}
