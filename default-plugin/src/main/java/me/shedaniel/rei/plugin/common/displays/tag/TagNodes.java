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

package me.shedaniel.rei.plugin.common.displays.tag;

import com.google.common.collect.Maps;
import com.mojang.serialization.DataResult;
import dev.architectury.event.events.client.ClientLifecycleEvent;
import dev.architectury.networking.NetworkManager;
import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import me.shedaniel.rei.api.common.util.UUIDUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

@ApiStatus.Internal
public class TagNodes {
    public static final Identifier REQUEST_TAGS_C2S_PACKET_ID = Identifier.fromNamespaceAndPath("roughlyenoughitems", "request_tags_c2s");
    public static final Identifier REQUEST_TAGS_S2C_PACKET_ID = Identifier.fromNamespaceAndPath("roughlyenoughitems", "request_tags_s2c");
    
    public static final CustomPacketPayload.Type<C2STagDataPacket> REQUEST_TAGS_C2S_PACKET_TYPE = new CustomPacketPayload.Type<>(REQUEST_TAGS_C2S_PACKET_ID);
    public static final CustomPacketPayload.Type<S2CTagDataPacket> REQUEST_TAGS_S2C_PACKET_TYPE = new CustomPacketPayload.Type<>(REQUEST_TAGS_S2C_PACKET_ID);
    
    public static final Map<String, ResourceKey<? extends Registry<?>>> TAG_DIR_MAP = new HashMap<>();
    public static final ThreadLocal<String> CURRENT_TAG_DIR = new ThreadLocal<>();
    public static final Map<String, Map<CollectionWrapper<?>, RawTagData>> RAW_TAG_DATA_MAP = new ConcurrentHashMap<>();
    public static final Map<ResourceKey<? extends Registry<?>>, Map<Identifier, TagData>> TAG_DATA_MAP = new HashMap<>();
    public static Map<ResourceKey<? extends Registry<?>>, Consumer<Consumer<DataResult<Map<Identifier, TagData>>>>> requestedTags = new HashMap<>();
    
    public static class CollectionWrapper<T> {
        private final Collection<T> collection;
        
        public CollectionWrapper(Collection<T> collection) {
            this.collection = collection;
        }
        
        @Override
        public boolean equals(Object obj) {
            return obj instanceof CollectionWrapper && ((CollectionWrapper) obj).collection == collection;
        }
        
        @Override
        public int hashCode() {
            return System.identityHashCode(collection);
        }
    }
    
    public record RawTagData(List<Identifier> otherElements, List<Identifier> otherTags) {
    }
    
    public record TagData(IntList otherElements, List<Identifier> otherTags) {
        public static final StreamCodec<RegistryFriendlyByteBuf, TagData> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.collection(IntArrayList::new, ByteBufCodecs.VAR_INT), TagData::otherElements,
                ByteBufCodecs.collection(ArrayList::new, Identifier.STREAM_CODEC), TagData::otherTags,
                TagData::new
        );
    }
    
    public record C2STagDataPacket(UUID uuid, Identifier registryName) implements CustomPacketPayload {
        public static final StreamCodec<RegistryFriendlyByteBuf, C2STagDataPacket> STREAM_CODEC = StreamCodec.composite(
                UUIDUtils.STREAM_CODEC, C2STagDataPacket::uuid,
                Identifier.STREAM_CODEC, C2STagDataPacket::registryName,
                C2STagDataPacket::new
        );
        
        @Override
        public @NotNull Type<? extends CustomPacketPayload> type() {
            return REQUEST_TAGS_C2S_PACKET_TYPE;
        }
    }
    
    public record S2CTagDataPacket(UUID uuid, Map<Identifier, TagData> map) implements CustomPacketPayload {
        public static final StreamCodec<RegistryFriendlyByteBuf, S2CTagDataPacket> STREAM_CODEC = StreamCodec.composite(
                UUIDUtils.STREAM_CODEC, S2CTagDataPacket::uuid,
                ByteBufCodecs.map(
                        Maps::newHashMapWithExpectedSize,
                        Identifier.STREAM_CODEC,
                        TagData.STREAM_CODEC
                ), S2CTagDataPacket::map,
                S2CTagDataPacket::new
        );
        
        @Override
        public @NotNull Type<? extends CustomPacketPayload> type() {
            return REQUEST_TAGS_S2C_PACKET_TYPE;
        }
    }
    
    public static void init() {
        EnvExecutor.runInEnv(Env.CLIENT, () -> Client::init);
        EnvExecutor.runInEnv(Env.SERVER, () -> Server::init);
        
        NetworkManager.registerReceiver(
                NetworkManager.c2s(),
                REQUEST_TAGS_C2S_PACKET_TYPE,
                C2STagDataPacket.STREAM_CODEC,
                (C2STagDataPacket payload, NetworkManager.PacketContext context) -> {
                    ResourceKey<? extends Registry<?>> registryKey = ResourceKey.createRegistryKey(payload.registryName);
                    Map<Identifier, TagData> dataMap = TAG_DATA_MAP.getOrDefault(registryKey, Collections.emptyMap());
                    var packet = new S2CTagDataPacket(payload.uuid, dataMap);
                    NetworkManager.sendToPlayer((ServerPlayer) context.getPlayer(), packet);
                }
        );
    }
    
    @Environment(EnvType.CLIENT)
    public static void requestTagData(ResourceKey<? extends Registry<?>> resourceKey, Consumer<DataResult<Map<Identifier, TagData>>> callback) {
        if (Minecraft.getInstance().getSingleplayerServer() != null) {
            callback.accept(DataResult.success(TAG_DATA_MAP.get(resourceKey)));
        } else if (!NetworkManager.canServerReceive(REQUEST_TAGS_C2S_PACKET_ID)) {
            callback.accept(DataResult.error(() -> "Cannot request tags from server"));
        } else if (requestedTags.containsKey(resourceKey)) {
            requestedTags.get(resourceKey).accept(callback);
            callback.accept(DataResult.success(TAG_DATA_MAP.getOrDefault(resourceKey, Collections.emptyMap())));
        } else {
            UUID uuid = UUID.randomUUID();
            var packet = new C2STagDataPacket(uuid, resourceKey.identifier());
            Client.nextUUID = uuid;
            Client.nextResourceKey = resourceKey;
            List<Consumer<DataResult<Map<Identifier, TagData>>>> callbacks = new CopyOnWriteArrayList<>();
            callbacks.add(callback);
            Client.nextCallback = mapDataResult -> {
                requestedTags.put(resourceKey, c -> c.accept(mapDataResult));
                for (Consumer<DataResult<Map<Identifier, TagData>>> consumer : callbacks) {
                    consumer.accept(mapDataResult);
                }
            };
            requestedTags.put(resourceKey, callbacks::add);
            NetworkManager.sendToServer(packet);
        }
    }
    
    private static class Server {
        private static void init() {
            NetworkManager.registerS2CPayloadType(REQUEST_TAGS_S2C_PACKET_TYPE, S2CTagDataPacket.STREAM_CODEC);
        }
    }
    
    private static class Client {
        public static UUID nextUUID;
        public static ResourceKey<? extends Registry<?>> nextResourceKey;
        public static Consumer<DataResult<Map<Identifier, TagData>>> nextCallback;
        
        private static void init() {
            ClientLifecycleEvent.CLIENT_LEVEL_LOAD.register(world -> {
                requestedTags.clear();
            });
            
            NetworkManager.registerReceiver(
                    NetworkManager.s2c(),
                    REQUEST_TAGS_S2C_PACKET_TYPE,
                    S2CTagDataPacket.STREAM_CODEC,
                    (S2CTagDataPacket payload, NetworkManager.PacketContext context) -> {
                        if (!nextUUID.equals(payload.uuid)) return;
                        
                        TAG_DATA_MAP.put(nextResourceKey, payload.map);
                        nextCallback.accept(DataResult.success(payload.map));
                        
                        nextUUID = null;
                        nextResourceKey = null;
                        nextCallback = null;
                    }
            );
        }
    }
    
    public static <T> void create(TagKey<T> tagKey, Consumer<DataResult<TagNode<T>>> callback) {
        Registry<T> registry = ((Registry<Registry<T>>) BuiltInRegistries.REGISTRY).getValueOrThrow((ResourceKey<Registry<T>>) tagKey.registry());
        requestTagData(tagKey.registry(), result -> {
            callback.accept(result.flatMap(dataMap -> dataMap != null ? resolveTag(tagKey, registry, dataMap).orElse(DataResult.error(() -> "No tag data")) : DataResult.error(() -> "No tag data")));
        });
    }
    
    private static <T> Optional<DataResult<TagNode<T>>> resolveTag(TagKey<T> tagKey, Registry<T> registry, Map<Identifier, TagData> tagDataMap) {
        TagData tagData = tagDataMap.get(tagKey.location());
        if (tagData == null) return resolveTagFromRegistry(tagKey, registry);
        
        TagNode<T> self = TagNode.ofReference(tagKey);
        List<Holder<T>> holders = new ArrayList<>();
        for (int element : tagData.otherElements()) {
            Optional<Holder.Reference<T>> holder = registry.get(element);
            if (holder.isPresent()) {
                holders.add(holder.get());
            }
        }
        if (!holders.isEmpty()) {
            self.addValuesChild(HolderSet.direct(holders));
        }
        for (Identifier childTagId : tagData.otherTags()) {
            TagKey<T> childTagKey = TagKey.create(tagKey.registry(), childTagId);
            if (registry.get(childTagKey).isPresent()) {
                Optional<DataResult<TagNode<T>>> resultOptional = resolveTag(childTagKey, registry, tagDataMap);
                if (resultOptional.isPresent()) {
                    DataResult<TagNode<T>> result = resultOptional.get();
                    if (result.error().isPresent())
                        return Optional.of(DataResult.error(() -> result.error().get().message()));
                    self.addChild(result.result().get());
                }
            }
        }
        return Optional.of(DataResult.success(self));
    }
    
    private static <T> Optional<DataResult<TagNode<T>>> resolveTagFromRegistry(TagKey<T> tagKey, Registry<T> registry) {
        Optional<HolderSet.Named<T>> holders = registry.get(tagKey);
        if (holders.isEmpty()) return Optional.empty();
        
        TagNode<T> self = TagNode.ofReference(tagKey);
        self.addValuesChild(holders.get());
        return Optional.of(DataResult.success(self));
    }
}
