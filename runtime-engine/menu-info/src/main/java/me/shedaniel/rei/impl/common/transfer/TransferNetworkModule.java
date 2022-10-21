/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020, 2021, 2022 shedaniel
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

package me.shedaniel.rei.impl.common.transfer;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.transformers.SplitPacketTransformer;
import io.netty.buffer.Unpooled;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.networking.NetworkModule;
import me.shedaniel.rei.api.common.networking.NetworkModuleKey;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.RecipeBookMenu;

import java.util.Collections;

public class TransferNetworkModule implements NetworkModule<NetworkModule.TransferData> {
    public static final ResourceLocation ID = new ResourceLocation("roughlyenoughitems", "move_items");
    
    @Override
    public NetworkModuleKey<NetworkModule.TransferData> getKey() {
        return NetworkModule.TRANSFER;
    }
    
    @Override
    public boolean canUse(Object target) {
        return NetworkManager.canServerReceive(TransferNetworkModule.ID);
    }
    
    @Override
    public void onInitialize() {
        NetworkManager.registerReceiver(NetworkManager.c2s(), ID, Collections.singletonList(new SplitPacketTransformer()), (packetByteBuf, context) -> {
            ServerPlayer player = (ServerPlayer) context.getPlayer();
            CategoryIdentifier<Display> category = CategoryIdentifier.of(packetByteBuf.readResourceLocation());
            AbstractContainerMenu container = player.containerMenu;
            InventoryMenu playerContainer = player.inventoryMenu;
            try {
                boolean shift = packetByteBuf.readBoolean();
                try {
                    InputSlotCrafter<AbstractContainerMenu, Display> crafter = InputSlotCrafter.start(category, container, player, packetByteBuf.readAnySizeNbt(), shift);
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
                    player.sendMessage(new TranslatableComponent(e.getMessage()).withStyle(ChatFormatting.RED), Util.NIL_UUID);
                } catch (Exception e) {
                    player.sendMessage(new TranslatableComponent("error.rei.internal.error", e.getMessage()).withStyle(ChatFormatting.RED), Util.NIL_UUID);
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
    
    @Override
    public void send(Object target, TransferData data) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeResourceLocation(data.categoryIdentifier().getIdentifier());
        buf.writeBoolean(data.stacked());
        
        buf.writeNbt(data.displayTag());
        NetworkManager.sendToServer(TransferNetworkModule.ID, buf);
    }
}
