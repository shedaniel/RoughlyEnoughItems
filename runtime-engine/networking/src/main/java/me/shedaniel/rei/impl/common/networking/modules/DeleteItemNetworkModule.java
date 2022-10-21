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

package me.shedaniel.rei.impl.common.networking.modules;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.transformers.SplitPacketTransformer;
import io.netty.buffer.Unpooled;
import me.shedaniel.rei.api.common.networking.NetworkModule;
import me.shedaniel.rei.api.common.networking.NetworkModuleKey;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Unit;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

import java.util.Collections;

public class DeleteItemNetworkModule implements NetworkModule<Unit> {
    public static final ResourceLocation ID = new ResourceLocation("roughlyenoughitems", "delete_item");
    
    @Override
    public NetworkModuleKey<Unit> getKey() {
        return NetworkModule.DELETE_ITEM;
    }
    
    @Override
    public boolean canUse(Object target) {
        return NetworkManager.canServerReceive(DeleteItemNetworkModule.ID);
    }
    
    @Override
    public void onInitialize() {
        NetworkManager.registerReceiver(NetworkManager.c2s(), ID, Collections.singletonList(new SplitPacketTransformer()), (buf, context) -> {
            ServerPlayer player = (ServerPlayer) context.getPlayer();
            if (player.getServer().getProfilePermissions(player.getGameProfile()) < player.getServer().getOperatorUserPermissionLevel()) {
                player.displayClientMessage(new TranslatableComponent("text.rei.no_permission_cheat").withStyle(ChatFormatting.RED), false);
                return;
            }
            AbstractContainerMenu menu = player.containerMenu;
            if (!menu.getCarried().isEmpty()) {
                menu.setCarried(ItemStack.EMPTY);
                menu.broadcastChanges();
            }
        });
    }
    
    @Override
    public void send(Object target, Unit data) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        NetworkManager.sendToServer(DeleteItemNetworkModule.ID, buf);
    }
}
