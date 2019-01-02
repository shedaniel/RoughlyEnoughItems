package me.shedaniel.network;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Packet;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sortme.ChatMessageType;
import net.minecraft.text.TranslatableTextComponent;
import net.minecraft.util.PacketByteBuf;

import java.io.IOException;

/**
 * Created by James on 7/29/2018.
 */
public class CheatPacket implements Packet<ServerPlayNetworkHandler> {
    
    private ItemStack stack;
    
    public CheatPacket() {
    }
    
    public CheatPacket(ItemStack stack) {
        this.stack = stack;
    }
    
    @Override
    public void read(PacketByteBuf packetBuffer) throws IOException {
        stack = ItemStack.fromTag(packetBuffer.readCompoundTag());
    }
    
    @Override
    public void write(PacketByteBuf packetBuffer) throws IOException {
        CompoundTag tag = new CompoundTag();
        stack.setTag(tag);
        packetBuffer.writeCompoundTag(tag);
    }
    
    @Override
    public void apply(ServerPlayNetworkHandler iNetHandlerPlayServer) {
        ServerPlayNetworkHandler server = (ServerPlayNetworkHandler) iNetHandlerPlayServer;
        ServerPlayerEntity player = server.player;
        if (player.inventory.insertStack(stack.copy()))
            player.sendChatMessage(new TranslatableTextComponent("text.rei.cheat_items", stack.getDisplayName().getFormattedText(), stack.getAmount(), player.getEntityName()), ChatMessageType.SYSTEM);
        else player.sendChatMessage(new TranslatableTextComponent("text.rei.failed_cheat_items"), ChatMessageType.SYSTEM);
    }
    
}
