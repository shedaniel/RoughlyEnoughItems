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

package me.shedaniel.rei.impl.client.networking.modules;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.transformers.SplitPacketTransformer;
import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import io.netty.buffer.Unpooled;
import me.shedaniel.rei.api.common.networking.NetworkModule;
import me.shedaniel.rei.api.common.networking.NetworkModuleKey;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.Collections;
import java.util.Map;

public class CheatItemStatusNetworkModule implements NetworkModule<Map.Entry<ItemStack, String>> {
    public static final ResourceLocation ID = new ResourceLocation("roughlyenoughitems", "ci_msg");
    
    @Override
    public NetworkModuleKey<Map.Entry<ItemStack, String>> getKey() {
        return NetworkModule.CHEAT_STATUS_REPLY;
    }
    
    @Override
    public boolean canUse(Object target) {
        return NetworkManager.canPlayerReceive((ServerPlayer) target, CheatItemStatusNetworkModule.ID);
    }
    
    @Override
    public void onInitialize() {
        EnvExecutor.runInEnv(Env.CLIENT, () -> () -> {
            NetworkManager.registerReceiver(NetworkManager.c2s(), ID, Collections.singletonList(new SplitPacketTransformer()), (buf, context) -> {
                ItemStack stack = buf.readItem();
                String player = buf.readUtf(32767);
                if (Minecraft.getInstance().player != null) {
                    Minecraft.getInstance().player.displayClientMessage(new TextComponent(I18n.get("text.rei.cheat_items").replaceAll("\\{item_name}", EntryStacks.of(stack.copy()).asFormattedText().getString()).replaceAll("\\{item_count}", stack.copy().getCount() + "").replaceAll("\\{player_name}", player)), false);
                }
            });
        });
    }
    
    @Override
    public void send(Object target, Map.Entry<ItemStack, String> data) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeItem(data.getKey().copy());
        buf.writeUtf(data.getValue(), 32767);
        NetworkManager.sendToServer(CheatItemStatusNetworkModule.ID, buf);
    }
}
