/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei;

import me.shedaniel.rei.gui.widget.ItemListOverlay;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.ChatFormat;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class RoughlyEnoughItemsNetwork implements ModInitializer {
    
    public static final Identifier DELETE_ITEMS_PACKET = new Identifier("roughlyenoughitems", "delete_item");
    public static final Identifier CREATE_ITEMS_PACKET = new Identifier("roughlyenoughitems", "create_item");
    
    @Override
    public void onInitialize() {
        ServerSidePacketRegistry.INSTANCE.register(DELETE_ITEMS_PACKET, (packetContext, packetByteBuf) -> {
            ServerPlayerEntity player = (ServerPlayerEntity) packetContext.getPlayer();
            if (player.getServer().getPermissionLevel(player.getGameProfile()) < player.getServer().getOpPermissionLevel()) {
                player.addChatMessage(new TranslatableComponent("text.rei.no_permission_cheat").applyFormat(ChatFormat.RED), false);
                return;
            }
            if (!player.inventory.getCursorStack().isEmpty())
                player.inventory.setCursorStack(ItemStack.EMPTY);
        });
        ServerSidePacketRegistry.INSTANCE.register(CREATE_ITEMS_PACKET, (packetContext, packetByteBuf) -> {
            ServerPlayerEntity player = (ServerPlayerEntity) packetContext.getPlayer();
            if (player.getServer().getPermissionLevel(player.getGameProfile()) < player.getServer().getOpPermissionLevel()) {
                player.addChatMessage(new TranslatableComponent("text.rei.no_permission_cheat").applyFormat(ChatFormat.RED), false);
                return;
            }
            ItemStack stack = packetByteBuf.readItemStack();
            if (player.inventory.insertStack(stack.copy()))
                player.addChatMessage(new TextComponent(I18n.translate("text.rei.cheat_items").replaceAll("\\{item_name}", ItemListOverlay.tryGetItemStackName(stack.copy())).replaceAll("\\{item_count}", stack.copy().getAmount() + "").replaceAll("\\{player_name}", player.getEntityName())), false);
            else
                player.addChatMessage(new TranslatableComponent("text.rei.failed_cheat_items"), false);
        });
    }
    
}
