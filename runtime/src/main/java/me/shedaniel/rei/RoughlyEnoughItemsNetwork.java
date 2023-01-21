/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020, 2021, 2022, 2023 shedaniel
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package me.shedaniel.rei;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.transformers.SplitPacketTransformer;
import io.netty.buffer.Unpooled;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.impl.common.transfer.InputSlotCrafter;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.item.ItemStack;

import java.util.Collections;

public class RoughlyEnoughItemsNetwork {
    public static final ResourceLocation DELETE_ITEMS_PACKET = new ResourceLocation("roughlyenoughitems", "delete_item");
    public static final ResourceLocation CREATE_ITEMS_PACKET = new ResourceLocation("roughlyenoughitems", "create_item");
    public static final ResourceLocation CREATE_ITEMS_HOTBAR_PACKET = new ResourceLocation("roughlyenoughitems", "create_item_hotbar");
    public static final ResourceLocation CREATE_ITEMS_GRAB_PACKET = new ResourceLocation("roughlyenoughitems", "create_item_grab");
    public static final ResourceLocation CREATE_ITEMS_MESSAGE_PACKET = new ResourceLocation("roughlyenoughitems", "ci_msg");
    public static final ResourceLocation MOVE_ITEMS_PACKET = new ResourceLocation("roughlyenoughitems", "move_items");
    public static final ResourceLocation NOT_ENOUGH_ITEMS_PACKET = new ResourceLocation("roughlyenoughitems", "og_not_enough");
    
    public static void onInitialize() {
        NetworkManager.registerReceiver(NetworkManager.c2s(), DELETE_ITEMS_PACKET, Collections.singletonList(new SplitPacketTransformer()), (buf, context) -> {
            ServerPlayer player = (ServerPlayer) context.getPlayer();
            if (player.getServer().getProfilePermissions(player.getGameProfile()) < player.getServer().getOperatorUserPermissionLevel()) {
                player.displayClientMessage(Component.translatable("text.rei.no_permission_cheat").withStyle(ChatFormatting.RED), false);
                return;
            }
            AbstractContainerMenu menu = player.containerMenu;
            if (!menu.getCarried().isEmpty()) {
                menu.setCarried(ItemStack.EMPTY);
                menu.broadcastChanges();
            }
        });
        NetworkManager.registerReceiver(NetworkManager.c2s(), CREATE_ITEMS_PACKET, Collections.singletonList(new SplitPacketTransformer()), (buf, context) -> {
            ServerPlayer player = (ServerPlayer) context.getPlayer();
            if (player.getServer().getProfilePermissions(player.getGameProfile()) < player.getServer().getOperatorUserPermissionLevel()) {
                player.displayClientMessage(Component.translatable("text.rei.no_permission_cheat").withStyle(ChatFormatting.RED), false);
                return;
            }
            ItemStack stack = buf.readItem();
            if (player.getInventory().add(stack.copy())) {
                NetworkManager.sendToPlayer(player, RoughlyEnoughItemsNetwork.CREATE_ITEMS_MESSAGE_PACKET, new FriendlyByteBuf(Unpooled.buffer()).writeItem(stack.copy()).writeUtf(player.getScoreboardName(), 32767));
            } else {
                player.displayClientMessage(Component.translatable("text.rei.failed_cheat_items"), false);
            }
        });
        NetworkManager.registerReceiver(NetworkManager.c2s(), CREATE_ITEMS_GRAB_PACKET, Collections.singletonList(new SplitPacketTransformer()), (buf, context) -> {
            ServerPlayer player = (ServerPlayer) context.getPlayer();
            if (player.getServer().getProfilePermissions(player.getGameProfile()) < player.getServer().getOperatorUserPermissionLevel()) {
                player.displayClientMessage(Component.translatable("text.rei.no_permission_cheat").withStyle(ChatFormatting.RED), false);
                return;
            }
            
            AbstractContainerMenu menu = player.containerMenu;
            ItemStack itemStack = buf.readItem();
            ItemStack stack = itemStack.copy();
            if (!menu.getCarried().isEmpty() && ItemStack.isSameItemSameTags(menu.getCarried(), stack)) {
                stack.setCount(Mth.clamp(stack.getCount() + menu.getCarried().getCount(), 1, stack.getMaxStackSize()));
            } else if (!menu.getCarried().isEmpty()) {
                return;
            }
            menu.setCarried(stack.copy());
            menu.broadcastChanges();
            NetworkManager.sendToPlayer(player, RoughlyEnoughItemsNetwork.CREATE_ITEMS_MESSAGE_PACKET, new FriendlyByteBuf(Unpooled.buffer()).writeItem(itemStack.copy()).writeUtf(player.getScoreboardName(), 32767));
        });
        NetworkManager.registerReceiver(NetworkManager.c2s(), CREATE_ITEMS_HOTBAR_PACKET, Collections.singletonList(new SplitPacketTransformer()), (buf, context) -> {
            ServerPlayer player = (ServerPlayer) context.getPlayer();
            if (player.getServer().getProfilePermissions(player.getGameProfile()) < player.getServer().getOperatorUserPermissionLevel()) {
                player.displayClientMessage(Component.translatable("text.rei.no_permission_cheat").withStyle(ChatFormatting.RED), false);
                return;
            }
            ItemStack stack = buf.readItem();
            int hotbarSlotId = buf.readVarInt();
            if (hotbarSlotId >= 0 && hotbarSlotId < 9) {
                AbstractContainerMenu menu = player.containerMenu;
                player.getInventory().items.set(hotbarSlotId, stack.copy());
                menu.broadcastChanges();
                NetworkManager.sendToPlayer(player, RoughlyEnoughItemsNetwork.CREATE_ITEMS_MESSAGE_PACKET, new FriendlyByteBuf(Unpooled.buffer()).writeItem(stack.copy()).writeUtf(player.getScoreboardName(), 32767));
            } else {
                player.displayClientMessage(Component.translatable("text.rei.failed_cheat_items"), false);
            }
        });
        NetworkManager.registerReceiver(NetworkManager.c2s(), MOVE_ITEMS_PACKET, Collections.singletonList(new SplitPacketTransformer()), (packetByteBuf, context) -> {
            ServerPlayer player = (ServerPlayer) context.getPlayer();
            CategoryIdentifier<Display> category = CategoryIdentifier.of(packetByteBuf.readResourceLocation());
            AbstractContainerMenu container = player.containerMenu;
            InventoryMenu playerContainer = player.inventoryMenu;
            try {
                boolean shift = packetByteBuf.readBoolean();
                try {
                    InputSlotCrafter<AbstractContainerMenu, Container, Display> crafter = InputSlotCrafter.start(category, container, player, packetByteBuf.readAnySizeNbt(), shift);
                } catch (InputSlotCrafter.NotEnoughMaterialsException e) {
                    if (!(container instanceof RecipeBookMenu)) {
                        return;
                    }
                    // TODO Implement Ghost Recipes
                    /*FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
                    buf.writeInt(input.size());
                    for (List<ItemStack> stacks : input) {
                        buf.writeInt(stacks.size());
                        for (ItemStack stack : stacks) {
                            buf.writeItem(stack);
                        }
                    }
                    NetworkManager.sendToPlayer(player, NOT_ENOUGH_ITEMS_PACKET, buf);*/
                } catch (IllegalStateException e) {
                    player.sendSystemMessage(Component.translatable(e.getMessage()).withStyle(ChatFormatting.RED));
                } catch (Exception e) {
                    player.sendSystemMessage(Component.translatable("error.rei.internal.error", e.getMessage()).withStyle(ChatFormatting.RED));
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
