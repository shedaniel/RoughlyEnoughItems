/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020, 2021, 2022, 2023 shedaniel
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including limitation the rights
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

package me.shedaniel.rei.impl.common.networking;

import me.shedaniel.rei.RoughlyEnoughItemsNetwork;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

/**
 * The {@link CustomPacketPayload} types used by REI's cheat / recipe-transfer
 * networking. Migrated to the payload + {@link StreamCodec} model required by
 * Architectury 21 (Minecraft 26.2), mirroring {@link DisplaySyncPacket}.
 */
public final class REIPackets {
    private REIPackets() {}
    
    /** C2S: clear the currently dragged item (no payload data). */
    public record DeleteItems() implements CustomPacketPayload {
        public static final Type<DeleteItems> TYPE = new Type<>(RoughlyEnoughItemsNetwork.DELETE_ITEMS_PACKET);
        public static final StreamCodec<RegistryFriendlyByteBuf, DeleteItems> STREAM_CODEC =
                StreamCodec.unit(new DeleteItems());
        
        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }
    
    /** C2S: create (give) an item stack into the player's inventory. */
    public record CreateItems(ItemStack stack) implements CustomPacketPayload {
        public static final Type<CreateItems> TYPE = new Type<>(RoughlyEnoughItemsNetwork.CREATE_ITEMS_PACKET);
        public static final StreamCodec<RegistryFriendlyByteBuf, CreateItems> STREAM_CODEC =
                StreamCodec.composite(ItemStack.OPTIONAL_STREAM_CODEC, CreateItems::stack, CreateItems::new);
        
        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }
    
    /** C2S: create an item stack directly onto the cursor (grab). */
    public record CreateItemsGrab(ItemStack stack) implements CustomPacketPayload {
        public static final Type<CreateItemsGrab> TYPE = new Type<>(RoughlyEnoughItemsNetwork.CREATE_ITEMS_GRAB_PACKET);
        public static final StreamCodec<RegistryFriendlyByteBuf, CreateItemsGrab> STREAM_CODEC =
                StreamCodec.composite(ItemStack.OPTIONAL_STREAM_CODEC, CreateItemsGrab::stack, CreateItemsGrab::new);
        
        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }
    
    /** C2S: create an item stack into a specific hotbar slot. */
    public record CreateItemsHotbar(ItemStack stack, int hotbarSlot) implements CustomPacketPayload {
        public static final Type<CreateItemsHotbar> TYPE = new Type<>(RoughlyEnoughItemsNetwork.CREATE_ITEMS_HOTBAR_PACKET);
        public static final StreamCodec<RegistryFriendlyByteBuf, CreateItemsHotbar> STREAM_CODEC =
                StreamCodec.composite(
                        ItemStack.OPTIONAL_STREAM_CODEC, CreateItemsHotbar::stack,
                        ByteBufCodecs.VAR_INT, CreateItemsHotbar::hotbarSlot,
                        CreateItemsHotbar::new);
        
        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }
    
    /** C2S: fill a menu's input slots from the player's inventory (recipe transfer). */
    public record MoveItemsNew(Identifier category, boolean shift, CompoundTag data) implements CustomPacketPayload {
        public static final Type<MoveItemsNew> TYPE = new Type<>(RoughlyEnoughItemsNetwork.MOVE_ITEMS_NEW_PACKET);
        public static final StreamCodec<RegistryFriendlyByteBuf, MoveItemsNew> STREAM_CODEC =
                StreamCodec.composite(
                        Identifier.STREAM_CODEC, MoveItemsNew::category,
                        ByteBufCodecs.BOOL, MoveItemsNew::shift,
                        ByteBufCodecs.COMPOUND_TAG, MoveItemsNew::data,
                        MoveItemsNew::new);
        
        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }
    
    /** S2C: notify the client that an item was created (for the chat message). */
    public record CreateItemsMessage(ItemStack stack, String playerName) implements CustomPacketPayload {
        public static final Type<CreateItemsMessage> TYPE = new Type<>(RoughlyEnoughItemsNetwork.CREATE_ITEMS_MESSAGE_PACKET);
        public static final StreamCodec<RegistryFriendlyByteBuf, CreateItemsMessage> STREAM_CODEC =
                StreamCodec.composite(
                        ItemStack.OPTIONAL_STREAM_CODEC, CreateItemsMessage::stack,
                        ByteBufCodecs.stringUtf8(32767), CreateItemsMessage::playerName,
                        CreateItemsMessage::new);
        
        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }
    
    /** S2C: reserved "not enough items" notification (no payload data; handling is currently disabled). */
    public record NotEnoughItems() implements CustomPacketPayload {
        public static final Type<NotEnoughItems> TYPE = new Type<>(RoughlyEnoughItemsNetwork.NOT_ENOUGH_ITEMS_PACKET);
        public static final StreamCodec<RegistryFriendlyByteBuf, NotEnoughItems> STREAM_CODEC =
                StreamCodec.unit(new NotEnoughItems());
        
        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }
}
