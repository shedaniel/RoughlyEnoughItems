/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020 shedaniel
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

import com.google.common.collect.Lists;
import io.netty.buffer.Unpooled;
import me.shedaniel.math.api.Executor;
import me.shedaniel.rei.server.InputSlotCrafter;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class RoughlyEnoughItemsNetwork implements ModInitializer {
    
    public static final ResourceLocation DELETE_ITEMS_PACKET = new ResourceLocation("roughlyenoughitems", "delete_item");
    public static final ResourceLocation CREATE_ITEMS_PACKET = new ResourceLocation("roughlyenoughitems", "create_item");
    public static final ResourceLocation CREATE_ITEMS_GRAB_PACKET = new ResourceLocation("roughlyenoughitems", "create_item_grab");
    public static final ResourceLocation CREATE_ITEMS_MESSAGE_PACKET = new ResourceLocation("roughlyenoughitems", "ci_msg");
    public static final ResourceLocation MOVE_ITEMS_PACKET = new ResourceLocation("roughlyenoughitems", "move_items");
    public static final ResourceLocation NOT_ENOUGH_ITEMS_PACKET = new ResourceLocation("roughlyenoughitems", "og_not_enough");
    
    @Override
    public void onInitialize() {
        boolean loaded = FabricLoader.getInstance().isModLoaded("fabric-networking-v0");
        if (!loaded) {
            RoughlyEnoughItemsState.error("Fabric API is not installed!", "https://www.curseforge.com/minecraft/mc-mods/fabric-api/files/all");
            return;
        }
        Executor.run(() -> () -> {
            FabricLoader.getInstance().getEntrypoints("rei_containers", Runnable.class).forEach(Runnable::run);
            ServerSidePacketRegistry.INSTANCE.register(DELETE_ITEMS_PACKET, (packetContext, packetByteBuf) -> {
                ServerPlayer player = (ServerPlayer) packetContext.getPlayer();
                if (player.getServer().getProfilePermissions(player.getGameProfile()) < player.getServer().getOperatorUserPermissionLevel()) {
                    player.displayClientMessage(new TranslatableComponent("text.rei.no_permission_cheat").withStyle(ChatFormatting.RED), false);
                    return;
                }
                if (!player.inventory.getCarried().isEmpty()) {
                    player.inventory.setCarried(ItemStack.EMPTY);
                    player.broadcastCarriedItem();
                }
            });
            ServerSidePacketRegistry.INSTANCE.register(CREATE_ITEMS_PACKET, (packetContext, packetByteBuf) -> {
                ServerPlayer player = (ServerPlayer) packetContext.getPlayer();
                if (player.getServer().getProfilePermissions(player.getGameProfile()) < player.getServer().getOperatorUserPermissionLevel()) {
                    player.displayClientMessage(new TranslatableComponent("text.rei.no_permission_cheat").withStyle(ChatFormatting.RED), false);
                    return;
                }
                ItemStack stack = packetByteBuf.readItem();
                if (player.inventory.add(stack.copy())) {
                    ServerSidePacketRegistry.INSTANCE.sendToPlayer(player, RoughlyEnoughItemsNetwork.CREATE_ITEMS_MESSAGE_PACKET, new FriendlyByteBuf(Unpooled.buffer()).writeItem(stack.copy()).writeUtf(player.getScoreboardName(), 32767));
                } else
                    player.displayClientMessage(new TranslatableComponent("text.rei.failed_cheat_items"), false);
            });
            ServerSidePacketRegistry.INSTANCE.register(CREATE_ITEMS_GRAB_PACKET, (packetContext, packetByteBuf) -> {
                ServerPlayer player = (ServerPlayer) packetContext.getPlayer();
                if (player.getServer().getProfilePermissions(player.getGameProfile()) < player.getServer().getOperatorUserPermissionLevel()) {
                    player.displayClientMessage(new TranslatableComponent("text.rei.no_permission_cheat").withStyle(ChatFormatting.RED), false);
                    return;
                }
                
                Inventory inventory = player.inventory;
                ItemStack itemStack = packetByteBuf.readItem();
                ItemStack stack = itemStack.copy();
                if (!inventory.getCarried().isEmpty() && ItemStack.isSameIgnoreDurability(inventory.getCarried(), stack) && ItemStack.tagMatches(inventory.getCarried(), stack)) {
                    stack.setCount(Mth.clamp(stack.getCount() + inventory.getCarried().getCount(), 1, stack.getMaxStackSize()));
                } else if (!inventory.getCarried().isEmpty()) {
                    return;
                }
                inventory.setCarried(stack.copy());
                player.broadcastCarriedItem();
                ServerSidePacketRegistry.INSTANCE.sendToPlayer(player, RoughlyEnoughItemsNetwork.CREATE_ITEMS_MESSAGE_PACKET, new FriendlyByteBuf(Unpooled.buffer()).writeItem(itemStack.copy()).writeUtf(player.getScoreboardName(), 32767));
            });
            ServerSidePacketRegistry.INSTANCE.register(MOVE_ITEMS_PACKET, (packetContext, packetByteBuf) -> {
                ResourceLocation category = packetByteBuf.readResourceLocation();
                ServerPlayer player = (ServerPlayer) packetContext.getPlayer();
                AbstractContainerMenu container = player.containerMenu;
                InventoryMenu playerContainer = player.inventoryMenu;
                try {
                    boolean shift = packetByteBuf.readBoolean();
                    NonNullList<List<ItemStack>> input = NonNullList.create();
                    int mapSize = packetByteBuf.readInt();
                    for (int i = 0; i < mapSize; i++) {
                        List<ItemStack> list = Lists.newArrayList();
                        int count = packetByteBuf.readInt();
                        for (int j = 0; j < count; j++) {
                            list.add(packetByteBuf.readItem());
                        }
                        input.add(list);
                    }
                    try {
                        InputSlotCrafter.start(category, container, player, input, shift);
                    } catch (InputSlotCrafter.NotEnoughMaterialsException e) {
                        if (!(container instanceof RecipeBookMenu))
                            return;
                        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
                        buf.writeInt(input.size());
                        for (List<ItemStack> stacks : input) {
                            buf.writeInt(stacks.size());
                            for (ItemStack stack : stacks) {
                                buf.writeItem(stack);
                            }
                        }
                        if (ServerSidePacketRegistry.INSTANCE.canPlayerReceive(player, NOT_ENOUGH_ITEMS_PACKET)) {
                            ServerSidePacketRegistry.INSTANCE.sendToPlayer(player, NOT_ENOUGH_ITEMS_PACKET, buf);
                        }
                    } catch (IllegalStateException e) {
                        player.sendMessage(new TranslatableComponent(e.getMessage()).withStyle(ChatFormatting.RED), Util.NIL_UUID);
                    } catch (Exception e) {
                        player.sendMessage(new TranslatableComponent("error.rei.internal.error", e.getMessage()).withStyle(ChatFormatting.RED), Util.NIL_UUID);
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        });
    }
    
}
