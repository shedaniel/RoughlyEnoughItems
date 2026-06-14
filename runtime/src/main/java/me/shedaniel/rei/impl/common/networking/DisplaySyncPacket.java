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

package me.shedaniel.rei.impl.common.networking;

import dev.architectury.networking.NetworkManager;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import me.shedaniel.rei.RoughlyEnoughItemsNetwork;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.impl.client.registry.display.DisplayRegistryImpl;
import me.shedaniel.rei.impl.common.InternalLogger;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.util.ByIdMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.function.IntFunction;
import java.util.function.UnaryOperator;

public record DisplaySyncPacket(SyncType syncType, Collection<Display> displays, long version) implements CustomPacketPayload {
    public static final Type<DisplaySyncPacket> TYPE = new Type<>(RoughlyEnoughItemsNetwork.SYNC_DISPLAYS_PACKET);
    public static final StreamCodec<? super RegistryFriendlyByteBuf, DisplaySyncPacket> STREAM_CODEC = StreamCodec.composite(
            SyncType.STREAM_CODEC,
            DisplaySyncPacket::syncType,
            Display.streamCodec().apply(codec -> {
                return new StreamCodec<RegistryFriendlyByteBuf, Display>() {
                    @Override
                    public void encode(RegistryFriendlyByteBuf buf, Display display) {
                        RegistryFriendlyByteBuf tmpBuf = new RegistryFriendlyByteBuf(Unpooled.buffer(), buf.registryAccess());
                        try {
                            codec.encode(tmpBuf, display);
                        } catch (Throwable throwable) {
                            tmpBuf.release();
                            buf.writeBoolean(false);
                            InternalLogger.getInstance().debug("Failed to encode display: %s".formatted(display), throwable);
                            return;
                        }
                        
                        buf.writeBoolean(true);
                        RegistryFriendlyByteBuf.writeByteArray(buf, ByteBufUtil.getBytes(tmpBuf));
                        tmpBuf.release();
                    }
                    
                    @Override
                    public Display decode(RegistryFriendlyByteBuf buf) {
                        boolean success = buf.readBoolean();
                        if (!success) return null;
                        RegistryFriendlyByteBuf tmpBuf = new RegistryFriendlyByteBuf(Unpooled.wrappedBuffer(RegistryFriendlyByteBuf.readByteArray(buf)), buf.registryAccess());
                        try {
                            return codec.decode(tmpBuf);
                        } catch (Throwable throwable) {
                            return null;
                        } finally {
                            tmpBuf.release();
                        }
                    }
                };
            }).apply(ByteBufCodecs.<RegistryFriendlyByteBuf, Display, Collection<Display>>collection(ArrayList::new)).map(list -> {
                return list.stream()
                        .filter(Objects::nonNull)
                        .toList();
            }, UnaryOperator.identity()),
            DisplaySyncPacket::displays,
            ByteBufCodecs.LONG,
            DisplaySyncPacket::version,
            DisplaySyncPacket::new
    );
    
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return DisplaySyncPacket.TYPE;
    }
    
    @Environment(EnvType.CLIENT)
    public void handle(NetworkManager.PacketContext context) {
        DisplayRegistryImpl registry = (DisplayRegistryImpl) DisplayRegistry.getInstance();
        if (syncType() == SyncType.SET) {
            InternalLogger.getInstance().info("[REI Server Display Sync] Received server's request to set %d recipes.", displays().size());
            registry.addJob(() -> {
                registry.removeSyncedRecipes();
                for (Display display : displays()) {
                    registry.add(display, DisplayRegistryImpl.SYNCED);
                }
            });
        } else if (syncType() == SyncType.APPEND) {
            InternalLogger.getInstance().info("[REI Server Display Sync] Received server's request to append %d recipes.", displays().size());
            registry.addJob(() -> {
                for (Display display : displays()) {
                    registry.add(display, DisplayRegistryImpl.SYNCED);
                }
            });
        }
    }
    
    public enum SyncType {
        APPEND,
        SET;
        
        public static final IntFunction<SyncType> BY_ID = ByIdMap.continuous(Enum::ordinal, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
        public static final StreamCodec<ByteBuf, SyncType> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, Enum::ordinal);
    }
}
