package me.shedaniel.rei.network;

import me.shedaniel.rei.gui.widget.ItemListOverlay;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;

import java.io.IOException;

public class CreateItemPacket implements Packet<INetHandlerPlayServer> {
    
    private ItemStack itemStack;
    
    public CreateItemPacket(ItemStack itemStack) {
        this.itemStack = itemStack;
    }
    
    @Override
    public void readPacketData(PacketBuffer buf) throws IOException {
        this.itemStack = buf.readItemStack();
    }
    
    @Override
    public void writePacketData(PacketBuffer buf) throws IOException {
        buf.writeItemStack(itemStack);
    }
    
    @Override
    public void processPacket(INetHandlerPlayServer handler) {
        EntityPlayerMP player = ((NetHandlerPlayServer) handler).player;
        if (player.getServer().getPermissionLevel(player.getGameProfile()) < player.getServer().getOpPermissionLevel()) {
            player.sendStatusMessage(new TextComponentTranslation("text.rei.no_permission_cheat").applyTextStyle(TextFormatting.RED), false);
            return;
        }
        ItemStack stack = itemStack.copy();
        if (player.inventory.addItemStackToInventory(stack.copy())) {
            player.sendStatusMessage(new TextComponentString(I18n.format("text.rei.cheat_items").replaceAll("\\{item_name}", ItemListOverlay.tryGetItemStackName(stack.copy())).replaceAll("\\{item_count}", stack.copy().getCount() + "").replaceAll("\\{player_name}", player.getName().getFormattedText())), false);
        } else
            player.sendStatusMessage(new TextComponentTranslation("text.rei.failed_cheat_items"), false);
    }
}
