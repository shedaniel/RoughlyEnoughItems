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

package me.shedaniel.rei.plugin.common.displays.tag;

import com.mojang.serialization.DataResult;
import dev.architectury.event.events.client.ClientLifecycleEvent;
import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.transformers.SplitPacketTransformer;
import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import org.jetbrains.annotations.ApiStatus;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

@ApiStatus.Internal
public class TagNodes {
    public static final ResourceLocation REQUEST_TAGS_PACKET_C2S = new ResourceLocation("roughlyenoughitems", "request_tags_c2s");
    public static final ResourceLocation REQUEST_TAGS_PACKET_S2C = new ResourceLocation("roughlyenoughitems", "request_tags_s2c");
    
    public static final Map<String, ResourceKey<? extends Registry<?>>> TAG_DIR_MAP = new HashMap<>();
    public static final ThreadLocal<String> CURRENT_TAG_DIR = new ThreadLocal<>();
    public static final Map<String, Map<CollectionWrapper<?>, RawTagData>> RAW_TAG_DATA_MAP = new ConcurrentHashMap<>();
    public static final Map<ResourceKey<? extends Registry<?>>, Map<ResourceLocation, TagData>> TAG_DATA_MAP = new HashMap<>();
    public static Map<ResourceKey<? extends Registry<?>>, Consumer<Consumer<DataResult<Map<ResourceLocation, TagData>>>>> requestedTags = new HashMap<>();
    
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
    
    public record RawTagData(List<ResourceLocation> otherElements, List<ResourceLocation> otherTags) {
    }
    
    public record TagData(IntList otherElements, List<ResourceLocation> otherTags) {
        private static TagData fromNetwork(FriendlyByteBuf buf) {
            int count = buf.readVarInt();
            IntList otherElements = new IntArrayList(count + 1);
            for (int i = 0; i < count; i++) {
                otherElements.add(buf.readVarInt());
            }
            count = buf.readVarInt();
            List<ResourceLocation> otherTags = new ArrayList<>(count + 1);
            for (int i = 0; i < count; i++) {
                otherTags.add(buf.readResourceLocation());
            }
            return new TagData(otherElements, otherTags);
        }
        
        private void toNetwork(FriendlyByteBuf buf) {
            buf.writeVarInt(otherElements.size());
            for (int integer : otherElements) {
                buf.writeVarInt(integer);
            }
            buf.writeVarInt(otherTags.size());
            for (ResourceLocation tag : otherTags) {
                writeResourceLocation(buf, tag);
            }
        }
    }
    
    private static void writeResourceLocation(FriendlyByteBuf buf, ResourceLocation location) {
        if (location.getNamespace().equals("minecraft")) {
            buf.writeUtf(location.getPath());
        } else {
            buf.writeUtf(location.toString());
        }
    }
    
    public static void init() {
        EnvExecutor.runInEnv(Env.CLIENT, () -> Client::init);
        
        NetworkManager.registerReceiver(NetworkManager.c2s(), REQUEST_TAGS_PACKET_C2S, Collections.singletonList(new SplitPacketTransformer()), (buf, context) -> {
            UUID uuid = buf.readUUID();
            ResourceKey<? extends Registry<?>> resourceKey = ResourceKey.createRegistryKey(buf.readResourceLocation());
            FriendlyByteBuf newBuf = new FriendlyByteBuf(Unpooled.buffer());
            newBuf.writeUUID(uuid);
            Map<ResourceLocation, TagData> dataMap = TAG_DATA_MAP.getOrDefault(resourceKey, Collections.emptyMap());
            newBuf.writeInt(dataMap.size());
            for (Map.Entry<ResourceLocation, TagData> entry : dataMap.entrySet()) {
                writeResourceLocation(newBuf, entry.getKey());
                entry.getValue().toNetwork(newBuf);
            }
            NetworkManager.sendToPlayer((ServerPlayer) context.getPlayer(), REQUEST_TAGS_PACKET_S2C, newBuf);
        });
    }
    
    @Environment(EnvType.CLIENT)
    public static void requestTagData(ResourceKey<? extends Registry<?>> resourceKey, Consumer<DataResult<Map<ResourceLocation, TagData>>> callback) {
        if (Minecraft.getInstance().getSingleplayerServer() != null) {
            callback.accept(DataResult.success(TAG_DATA_MAP.get(resourceKey)));
        } else if (!NetworkManager.canServerReceive(REQUEST_TAGS_PACKET_C2S)) {
            callback.accept(DataResult.error("Cannot request tags from server"));
        } else if (requestedTags.containsKey(resourceKey)) {
            requestedTags.get(resourceKey).accept(callback);
            callback.accept(DataResult.success(TAG_DATA_MAP.getOrDefault(resourceKey, Collections.emptyMap())));
        } else {
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
            UUID uuid = UUID.randomUUID();
            buf.writeUUID(uuid);
            buf.writeResourceLocation(resourceKey.location());
            Client.nextUUID = uuid;
            Client.nextResourceKey = resourceKey;
            List<Consumer<DataResult<Map<ResourceLocation, TagData>>>> callbacks = new CopyOnWriteArrayList<>();
            callbacks.add(callback);
            Client.nextCallback = mapDataResult -> {
                requestedTags.put(resourceKey, c -> c.accept(mapDataResult));
                for (Consumer<DataResult<Map<ResourceLocation, TagData>>> consumer : callbacks) {
                    consumer.accept(mapDataResult);
                }
            };
            requestedTags.put(resourceKey, callbacks::add);
            NetworkManager.sendToServer(REQUEST_TAGS_PACKET_C2S, buf);
        }
    }
    
    private static class Client {
        public static UUID nextUUID;
        public static ResourceKey<? extends Registry<?>> nextResourceKey;
        public static Consumer<DataResult<Map<ResourceLocation, TagData>>> nextCallback;
        
        private static void init() {
            ClientLifecycleEvent.CLIENT_LEVEL_LOAD.register(world -> {
                requestedTags.clear();
            });
            NetworkManager.registerReceiver(NetworkManager.s2c(), REQUEST_TAGS_PACKET_S2C, (buf, context) -> {
                UUID uuid = buf.readUUID();
                if (nextUUID.equals(uuid)) {
                    Map<ResourceLocation, TagData> map = new HashMap<>();
                    int count = buf.readInt();
                    for (int i = 0; i < count; i++) {
                        map.put(buf.readResourceLocation(), TagData.fromNetwork(buf));
                    }
                    
                    TAG_DATA_MAP.put(nextResourceKey, map);
                    nextCallback.accept(DataResult.success(map));
                    
                    nextUUID = null;
                    nextResourceKey = null;
                    nextCallback = null;
                }
            });
        }
    }
    
    public static <T> void create(TagKey<T> tagKey, Consumer<DataResult<TagNode<T>>> callback) {
        Registry<T> registry = ((Registry<Registry<T>>) Registry.REGISTRY).get((ResourceKey<Registry<T>>) tagKey.registry());
        requestTagData(tagKey.registry(), result -> {
            callback.accept(result.flatMap(dataMap -> dataMap != null ? resolveTag(tagKey, registry, dataMap).orElse(DataResult.error("No tag data")) : DataResult.error("No tag data")));
        });
    }
    
    private static <T> Optional<DataResult<TagNode<T>>> resolveTag(TagKey<T> tagKey, Registry<T> registry, Map<ResourceLocation, TagData> tagDataMap) {
        TagData tagData = tagDataMap.get(tagKey.location());
        if (tagData == null) return Optional.empty();
        
        TagNode<T> self = TagNode.ofReference(tagKey);
        List<Holder<T>> holders = new ArrayList<>();
        for (int element : tagData.otherElements()) {
            Optional<Holder<T>> holder = registry.getHolder(element);
            if (holder.isPresent()) {
                holders.add(holder.get());
            }
        }
        if (!holders.isEmpty()) {
            self.addValuesChild(HolderSet.direct(holders));
        }
        for (ResourceLocation childTagId : tagData.otherTags()) {
            TagKey<T> childTagKey = TagKey.create(tagKey.registry(), childTagId);
            if (registry.getTag(childTagKey).isPresent()) {
                Optional<DataResult<TagNode<T>>> resultOptional = resolveTag(childTagKey, registry, tagDataMap);
                if (resultOptional.isPresent()) {
                    DataResult<TagNode<T>> result = resultOptional.get();
                    if (result.error().isPresent()) return Optional.of(DataResult.error(result.error().get().message()));
                    self.addChild(result.result().get());
                }
            }
        }
        return Optional.of(DataResult.success(self));
    }
}
