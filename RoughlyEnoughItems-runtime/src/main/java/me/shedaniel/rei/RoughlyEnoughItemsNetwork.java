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
import me.shedaniel.rei.api.plugins.REIContainerPlugin;
import me.shedaniel.rei.impl.NetworkingManager;
import me.shedaniel.rei.server.InputSlotCrafter;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.inventory.container.RecipeBookContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import org.objectweb.asm.Type;

import java.util.List;

public class RoughlyEnoughItemsNetwork {
    
    public static final ResourceLocation DELETE_ITEMS_PACKET = new ResourceLocation("roughlyenoughitems", "delete_item");
    public static final ResourceLocation CREATE_ITEMS_PACKET = new ResourceLocation("roughlyenoughitems", "create_item");
    public static final ResourceLocation CREATE_ITEMS_GRAB_PACKET = new ResourceLocation("roughlyenoughitems", "create_item_grab");
    public static final ResourceLocation CREATE_ITEMS_MESSAGE_PACKET = new ResourceLocation("roughlyenoughitems", "ci_msg");
    public static final ResourceLocation MOVE_ITEMS_PACKET = new ResourceLocation("roughlyenoughitems", "move_items");
    public static final ResourceLocation NOT_ENOUGH_ITEMS_PACKET = new ResourceLocation("roughlyenoughitems", "og_not_enough");
    
    public static void onInitialize() {
        RoughlyEnoughItemsInit.scanAnnotation(Type.getType(REIContainerPlugin.class), Runnable::run);
        NetworkingManager.registerC2SHandler(DELETE_ITEMS_PACKET, (packetContext, packetByteBuf) -> {
            ServerPlayerEntity player = packetContext.getSender();
            if (player.getServer().getProfilePermissions(player.getGameProfile()) < player.getServer().getOperatorUserPermissionLevel()) {
                player.displayClientMessage(new TranslationTextComponent("text.rei.no_permission_cheat").withStyle(TextFormatting.RED), false);
                return;
            }
            if (!player.inventory.getCarried().isEmpty()) {
                player.inventory.setCarried(ItemStack.EMPTY);
                player.broadcastCarriedItem();
            }
        });
        NetworkingManager.registerC2SHandler(CREATE_ITEMS_PACKET, (packetContext, packetByteBuf) -> {
            ServerPlayerEntity player = packetContext.getSender();
            if (player.getServer().getProfilePermissions(player.getGameProfile()) < player.getServer().getOperatorUserPermissionLevel()) {
                player.displayClientMessage(new TranslationTextComponent("text.rei.no_permission_cheat").withStyle(TextFormatting.RED), false);
                return;
            }
            ItemStack stack = packetByteBuf.readItem();
            if (player.inventory.add(stack.copy())) {
                NetworkingManager.sendToPlayer(player, RoughlyEnoughItemsNetwork.CREATE_ITEMS_MESSAGE_PACKET, new PacketBuffer(Unpooled.buffer()).writeItem(stack.copy()).writeUtf(player.getScoreboardName(), 32767));
            } else
                player.displayClientMessage(new TranslationTextComponent("text.rei.failed_cheat_items"), false);
        });
        NetworkingManager.registerC2SHandler(CREATE_ITEMS_GRAB_PACKET, (packetContext, packetByteBuf) -> {
            ServerPlayerEntity player = packetContext.getSender();
            if (player.getServer().getProfilePermissions(player.getGameProfile()) < player.getServer().getOperatorUserPermissionLevel()) {
                player.displayClientMessage(new TranslationTextComponent("text.rei.no_permission_cheat").withStyle(TextFormatting.RED), false);
                return;
            }
            
            PlayerInventory inventory = player.inventory;
            ItemStack itemStack = packetByteBuf.readItem();
            ItemStack stack = itemStack.copy();
            if (!inventory.getCarried().isEmpty() && ItemStack.isSameIgnoreDurability(inventory.getCarried(), stack) && ItemStack.tagMatches(inventory.getCarried(), stack)) {
                stack.setCount(MathHelper.clamp(stack.getCount() + inventory.getCarried().getCount(), 1, stack.getMaxStackSize()));
            } else if (!inventory.getCarried().isEmpty()) {
                return;
            }
            inventory.setCarried(stack.copy());
            player.broadcastCarriedItem();
            NetworkingManager.sendToPlayer(player, RoughlyEnoughItemsNetwork.CREATE_ITEMS_MESSAGE_PACKET, new PacketBuffer(Unpooled.buffer()).writeItem(itemStack.copy()).writeUtf(player.getScoreboardName(), 32767));
        });
        NetworkingManager.registerC2SHandler(MOVE_ITEMS_PACKET, (packetContext, packetByteBuf) -> {
            ResourceLocation category = packetByteBuf.readResourceLocation();
            ServerPlayerEntity player = packetContext.getSender();
            Container container = player.containerMenu;
            PlayerContainer playerContainer = player.inventoryMenu;
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
                    if (!(container instanceof RecipeBookContainer))
                        return;
                    PacketBuffer buf = new PacketBuffer(Unpooled.buffer());
                    buf.writeInt(input.size());
                    for (List<ItemStack> stacks : input) {
                        buf.writeInt(stacks.size());
                        for (ItemStack stack : stacks) {
                            buf.writeItem(stack);
                        }
                    }
                    if (NetworkingManager.canPlayerReceive(player, NOT_ENOUGH_ITEMS_PACKET)) {
                        NetworkingManager.sendToPlayer(player, NOT_ENOUGH_ITEMS_PACKET, buf);
                    }
                } catch (IllegalStateException e) {
                    player.sendMessage(new TranslationTextComponent(e.getMessage()).withStyle(TextFormatting.RED), Util.NIL_UUID);
                } catch (Exception e) {
                    player.sendMessage(new TranslationTextComponent("error.rei.internal.error", e.getMessage()).withStyle(TextFormatting.RED), Util.NIL_UUID);
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
    
}
