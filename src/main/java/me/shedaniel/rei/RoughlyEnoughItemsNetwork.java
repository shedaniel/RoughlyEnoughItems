/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.netty.buffer.Unpooled;
import me.shedaniel.rei.server.InputSlotCrafter;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.container.Container;
import net.minecraft.container.CraftingContainer;
import net.minecraft.container.PlayerContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class RoughlyEnoughItemsNetwork implements ModInitializer {
    
    public static final Identifier DELETE_ITEMS_PACKET = new Identifier("roughlyenoughitems", "delete_item");
    public static final Identifier CREATE_ITEMS_PACKET = new Identifier("roughlyenoughitems", "create_item");
    public static final Identifier CREATE_ITEMS_MESSAGE_PACKET = new Identifier("roughlyenoughitems", "ci_msg");
    public static final Identifier MOVE_ITEMS_PACKET = new Identifier("roughlyenoughitems", "move_items");
    public static final Identifier NOT_ENOUGH_ITEMS_PACKET = new Identifier("roughlyenoughitems", "og_not_enough");
    
    @Override
    public void onInitialize() {
        FabricLoader.getInstance().getEntrypoints("rei_containers", Runnable.class).forEach(Runnable::run);
        ServerSidePacketRegistry.INSTANCE.register(DELETE_ITEMS_PACKET, (packetContext, packetByteBuf) -> {
            ServerPlayerEntity player = (ServerPlayerEntity) packetContext.getPlayer();
            if (player.getServer().getPermissionLevel(player.getGameProfile()) < player.getServer().getOpPermissionLevel()) {
                player.addChatMessage(new TranslatableText("text.rei.no_permission_cheat").formatted(Formatting.RED), false);
                return;
            }
            if (!player.inventory.getCursorStack().isEmpty())
                player.inventory.setCursorStack(ItemStack.EMPTY);
        });
        ServerSidePacketRegistry.INSTANCE.register(CREATE_ITEMS_PACKET, (packetContext, packetByteBuf) -> {
            ServerPlayerEntity player = (ServerPlayerEntity) packetContext.getPlayer();
            if (player.getServer().getPermissionLevel(player.getGameProfile()) < player.getServer().getOpPermissionLevel()) {
                player.addChatMessage(new TranslatableText("text.rei.no_permission_cheat").formatted(Formatting.RED), false);
                return;
            }
            ItemStack stack = packetByteBuf.readItemStack();
            if (player.inventory.insertStack(stack.copy())) {
                ServerSidePacketRegistry.INSTANCE.sendToPlayer(player, RoughlyEnoughItemsNetwork.CREATE_ITEMS_MESSAGE_PACKET, new PacketByteBuf(Unpooled.buffer()).writeItemStack(stack.copy()).writeString(player.getEntityName(), 32767));
            } else
                player.addChatMessage(new TranslatableText("text.rei.failed_cheat_items"), false);
        });
        ServerSidePacketRegistry.INSTANCE.register(MOVE_ITEMS_PACKET, (packetContext, packetByteBuf) -> {
            Identifier category = packetByteBuf.readIdentifier();
            ServerPlayerEntity player = (ServerPlayerEntity) packetContext.getPlayer();
            Container container = player.container;
            PlayerContainer playerContainer = player.playerContainer;
            try {
                boolean shift = packetByteBuf.readBoolean();
                Map<Integer, List<ItemStack>> input = Maps.newHashMap();
                int mapSize = packetByteBuf.readInt();
                for (int i = 0; i < mapSize; i++) {
                    List<ItemStack> list = Lists.newArrayList();
                    int count = packetByteBuf.readInt();
                    for (int j = 0; j < count; j++) {
                        list.add(packetByteBuf.readItemStack());
                    }
                    input.put(i, list);
                }
                try {
                    InputSlotCrafter.start(category, container, player, input, shift);
                } catch (InputSlotCrafter.NotEnoughMaterialsException e) {
                    if (!(container instanceof CraftingContainer))
                        return;
                    PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
                    buf.writeInt(input.size());
                    input.entrySet().stream().sorted(Comparator.comparingInt(Map.Entry::getKey)).forEach(entry -> {
                        List<ItemStack> stacks = entry.getValue();
                        buf.writeInt(stacks.size());
                        for (ItemStack stack : stacks) {
                            buf.writeItemStack(stack);
                        }
                    });
                    if (ServerSidePacketRegistry.INSTANCE.canPlayerReceive(player, NOT_ENOUGH_ITEMS_PACKET)) {
                        ServerSidePacketRegistry.INSTANCE.sendToPlayer(player, NOT_ENOUGH_ITEMS_PACKET, buf);
                    }
                } catch (IllegalStateException e) {
                    player.sendMessage(new TranslatableText(e.getMessage()).formatted(Formatting.RED));
                } catch (Exception e) {
                    player.sendMessage(new TranslatableText("error.rei.internal.error", e.getMessage()).formatted(Formatting.RED));
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
    
}
