package me.shedaniel.rei.network;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import org.dimdev.rift.network.Message;
import org.dimdev.rift.network.ServerMessageContext;

public class DeleteItemsMessage extends Message {
    
    @Override
    public void write(PacketBuffer buffer) {
    
    }
    
    @Override
    public void read(PacketBuffer buffer) {
    
    }
    
    @Override
    public void process(ServerMessageContext context) {
        EntityPlayerMP player = context.getSender();
        if (!player.inventory.getItemStack().isEmpty())
            player.inventory.setItemStack(ItemStack.EMPTY);
    }
}
