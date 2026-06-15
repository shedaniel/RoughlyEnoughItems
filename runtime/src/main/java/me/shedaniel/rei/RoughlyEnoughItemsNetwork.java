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
import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import io.netty.buffer.Unpooled;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.InputIngredient;
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes;
import me.shedaniel.rei.api.common.transfer.info.stack.SlotAccessor;
import me.shedaniel.rei.api.common.transfer.info.stack.SlotAccessorRegistry;
import me.shedaniel.rei.impl.common.networking.DisplaySyncPacket;
import me.shedaniel.rei.impl.common.transfer.InputSlotCrafter;
import me.shedaniel.rei.impl.common.transfer.NewInputSlotCrafter;
import net.minecraft.ChatFormatting;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.*;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RoughlyEnoughItemsNetwork {
    public static final Identifier DELETE_ITEMS_PACKET = Identifier.fromNamespaceAndPath("roughlyenoughitems", "delete_item");
    public static final Identifier CREATE_ITEMS_PACKET = Identifier.fromNamespaceAndPath("roughlyenoughitems", "create_item");
    public static final Identifier CREATE_ITEMS_HOTBAR_PACKET = Identifier.fromNamespaceAndPath("roughlyenoughitems", "create_item_hotbar");
    public static final Identifier CREATE_ITEMS_GRAB_PACKET = Identifier.fromNamespaceAndPath("roughlyenoughitems", "create_item_grab");
    public static final Identifier CREATE_ITEMS_MESSAGE_PACKET = Identifier.fromNamespaceAndPath("roughlyenoughitems", "ci_msg");
    public static final Identifier MOVE_ITEMS_NEW_PACKET = Identifier.fromNamespaceAndPath("roughlyenoughitems", "move_items_new");
    public static final Identifier NOT_ENOUGH_ITEMS_PACKET = Identifier.fromNamespaceAndPath("roughlyenoughitems", "og_not_enough");
    public static final Identifier SYNC_DISPLAYS_PACKET = Identifier.fromNamespaceAndPath("roughlyenoughitems", "sync_displays");
    
    public static void onInitialize() {
        NetworkManager.registerReceiver(NetworkManager.c2s(), DELETE_ITEMS_PACKET, Collections.singletonList(new SplitPacketTransformer()), (buf, context) -> {
            ServerPlayer player = (ServerPlayer) context.getPlayer();
            if (!player.permissions().hasPermission(new Permission.HasCommandLevel(PermissionLevel.GAMEMASTERS))) {
                player.sendSystemMessage(Component.translatable("text.rei.no_permission_cheat").withStyle(ChatFormatting.RED));
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

            if (!player.permissions().hasPermission(new Permission.HasCommandLevel(PermissionLevel.GAMEMASTERS))) {
                player.sendSystemMessage(Component.translatable("text.rei.no_permission_cheat").withStyle(ChatFormatting.RED));
                return;
            }
            ItemStack stack = buf.readLenientJsonWithCodec(ItemStack.OPTIONAL_CODEC);
            if (player.getInventory().add(stack.copy())) {
                RegistryFriendlyByteBuf newBuf = new RegistryFriendlyByteBuf(Unpooled.buffer(), player.registryAccess());
                newBuf.writeJsonWithCodec(ItemStack.OPTIONAL_CODEC, stack.copy());
                newBuf.writeUtf(player.getScoreboardName(), 32767);
                NetworkManager.sendToPlayer(player, RoughlyEnoughItemsNetwork.CREATE_ITEMS_MESSAGE_PACKET, newBuf);
            } else {
                player.sendSystemMessage(Component.translatable("text.rei.failed_cheat_items"));
            }
        });
        NetworkManager.registerReceiver(NetworkManager.c2s(), CREATE_ITEMS_GRAB_PACKET, Collections.singletonList(new SplitPacketTransformer()), (buf, context) -> {
            ServerPlayer player = (ServerPlayer) context.getPlayer();
            if (!player.permissions().hasPermission(new Permission.HasCommandLevel(PermissionLevel.GAMEMASTERS))) {
                player.sendSystemMessage(Component.translatable("text.rei.no_permission_cheat").withStyle(ChatFormatting.RED));
                return;
            }
            
            AbstractContainerMenu menu = player.containerMenu;
            ItemStack itemStack = buf.readLenientJsonWithCodec(ItemStack.OPTIONAL_CODEC);
            ItemStack stack = itemStack.copy();
            if (!menu.getCarried().isEmpty() && ItemStack.isSameItemSameComponents(menu.getCarried(), stack)) {
                stack.setCount(Mth.clamp(stack.getCount() + menu.getCarried().getCount(), 1, stack.getMaxStackSize()));
            } else if (!menu.getCarried().isEmpty()) {
                return;
            }
            menu.setCarried(stack.copy());
            menu.broadcastChanges();
            RegistryFriendlyByteBuf newBuf = new RegistryFriendlyByteBuf(Unpooled.buffer(), player.registryAccess());
            newBuf.writeJsonWithCodec(ItemStack.OPTIONAL_CODEC, stack.copy());
            newBuf.writeUtf(player.getScoreboardName(), 32767);
            NetworkManager.sendToPlayer(player, RoughlyEnoughItemsNetwork.CREATE_ITEMS_MESSAGE_PACKET, newBuf);
        });
        NetworkManager.registerReceiver(NetworkManager.c2s(), CREATE_ITEMS_HOTBAR_PACKET, Collections.singletonList(new SplitPacketTransformer()), (buf, context) -> {
            ServerPlayer player = (ServerPlayer) context.getPlayer();
            if (!player.permissions().hasPermission(new Permission.HasCommandLevel(PermissionLevel.GAMEMASTERS))) {
                player.sendSystemMessage(Component.translatable("text.rei.no_permission_cheat").withStyle(ChatFormatting.RED));
                return;
            }
            ItemStack stack = buf.readLenientJsonWithCodec(ItemStack.OPTIONAL_CODEC);
            int hotbarSlotId = buf.readVarInt();
            if (hotbarSlotId >= 0 && hotbarSlotId < 9) {
                AbstractContainerMenu menu = player.containerMenu;
                player.getInventory().setItem(hotbarSlotId, stack.copy());
                menu.broadcastChanges();
                RegistryFriendlyByteBuf newBuf = new RegistryFriendlyByteBuf(Unpooled.buffer(), player.registryAccess());
                newBuf.writeJsonWithCodec(ItemStack.OPTIONAL_CODEC, stack.copy());
                newBuf.writeUtf(player.getScoreboardName(), 32767);
                NetworkManager.sendToPlayer(player, RoughlyEnoughItemsNetwork.CREATE_ITEMS_MESSAGE_PACKET, newBuf);
            } else {
                player.sendSystemMessage(Component.translatable("text.rei.failed_cheat_items"));
            }
        });
        NetworkManager.registerReceiver(NetworkManager.c2s(), MOVE_ITEMS_NEW_PACKET, Collections.singletonList(new SplitPacketTransformer()), (packetByteBuf, context) -> {
            ServerPlayer player = (ServerPlayer) context.getPlayer();
            CategoryIdentifier<Display> category = CategoryIdentifier.of(packetByteBuf.readIdentifier());
            AbstractContainerMenu container = player.containerMenu;
            InventoryMenu playerContainer = player.inventoryMenu;
            try {
                boolean shift = packetByteBuf.readBoolean();
                try {
                    CompoundTag nbt = packetByteBuf.readNbt();
                    int version = nbt.getInt("Version").orElse(-1);
                    if (version != 1) throw new IllegalStateException("Server and client REI protocol version mismatch! Server: 1, Client: " + version);
                    List<InputIngredient<ItemStack>> inputs = readInputs(context.registryAccess(), nbt.getListOrEmpty("Inputs"));
                    List<SlotAccessor> input = readSlots(container, player, nbt.getListOrEmpty("InputSlots"));
                    List<SlotAccessor> inventory = readSlots(container, player, nbt.getListOrEmpty("InventorySlots"));
                    NewInputSlotCrafter<AbstractContainerMenu, Container> crafter = new NewInputSlotCrafter<>(container, input, inventory, inputs);
                    crafter.fillInputSlots(player, shift);
                } catch (InputSlotCrafter.NotEnoughMaterialsException e) {
                    if (!(container instanceof RecipeBookMenu)) {
                        return;
                    }
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
        if (Platform.getEnvironment() == Env.SERVER) {
            NetworkManager.registerS2CPayloadType(DisplaySyncPacket.TYPE, DisplaySyncPacket.STREAM_CODEC, List.of(new SplitPacketTransformer()));
        }
    }
    
    private static List<SlotAccessor> readSlots(AbstractContainerMenu menu, Player player, ListTag tag) {
        List<SlotAccessor> slots = new ArrayList<>();
        for (Tag t : tag) {
            slots.add(SlotAccessorRegistry.getInstance().read(menu, player, (CompoundTag) t));
        }
        return slots;
    }
    
    private static List<InputIngredient<ItemStack>> readInputs(RegistryAccess registryAccess, ListTag tag) {
        List<InputIngredient<ItemStack>> inputs = new ArrayList<>();
        for (Tag t : tag) {
            CompoundTag compoundTag = (CompoundTag) t;
            InputIngredient<EntryStack<?>> stacks = InputIngredient.of(compoundTag.getInt("Index").orElseThrow(), EntryIngredient.codec().parse(registryAccess.createSerializationContext(NbtOps.INSTANCE), compoundTag.getListOrEmpty("Ingredient")).getOrThrow());
            inputs.add(InputIngredient.withType(stacks, VanillaEntryTypes.ITEM));
        }
        return inputs;
    }
}
