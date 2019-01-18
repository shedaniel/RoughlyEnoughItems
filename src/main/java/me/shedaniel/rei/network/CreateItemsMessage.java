package me.shedaniel.rei.network;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.TextComponentTranslation;
import org.dimdev.rift.network.Message;
import org.dimdev.rift.network.ServerMessageContext;

public class CreateItemsMessage extends Message {
    
    private ItemStack stack;
    
    public CreateItemsMessage(ItemStack stack) {
        this.stack = stack;
    }
    
    @Override
    public void write(PacketBuffer buffer) {
        buffer.writeItemStack(stack);
    }
    
    @Override
    public void read(PacketBuffer buffer) {
        stack = buffer.readItemStack();
    }
    
    @Override
    public void process(ServerMessageContext context) {
        EntityPlayerMP player = context.getSender();
        if (player.inventory.addItemStackToInventory(stack.copy()))
            player.sendMessage(new TextComponentTranslation("text.rei.cheat_items", stack.getDisplayName().getFormattedText(), stack.getCount(), player.getName()), ChatType.SYSTEM);
        else
            player.sendMessage(new TextComponentTranslation("text.rei.failed_cheat_items"), ChatType.SYSTEM);
    }
    
}
