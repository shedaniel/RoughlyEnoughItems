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

import com.mojang.serialization.DataResult;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import me.shedaniel.architectury.event.events.client.ClientLifecycleEvent;
import me.shedaniel.architectury.networking.NetworkManager;
import me.shedaniel.architectury.networking.transformers.SplitPacketTransformer;
import me.shedaniel.architectury.utils.Env;
import me.shedaniel.architectury.utils.EnvExecutor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.Tag;
import net.minecraft.tags.TagCollection;
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
    public static final Map<String, Map<Tag<?>, RawTagData>> RAW_TAG_DATA_MAP = new ConcurrentHashMap<>();
    public static final Map<String, Map<ResourceLocation, TagData>> TAG_DATA_MAP = new HashMap<>();
    public static Map<String, Consumer<Consumer<DataResult<Map<ResourceLocation, TagData>>>>> requestedTags = new HashMap<>();
    
    public static final class RawTagData {
        private final List<ResourceLocation> otherElements;
        private final List<ResourceLocation> otherTags;
        
        public RawTagData(List<ResourceLocation> otherElements, List<ResourceLocation> otherTags) {
            this.otherElements = otherElements;
            this.otherTags = otherTags;
        }
        
        public List<ResourceLocation> otherElements() {
            return otherElements;
        }
        
        public List<ResourceLocation> otherTags() {
            return otherTags;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            RawTagData that = (RawTagData) obj;
            return Objects.equals(this.otherElements, that.otherElements) &&
                    Objects.equals(this.otherTags, that.otherTags);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(otherElements, otherTags);
        }
        
        @Override
        public String toString() {
            return "RawTagData[" +
                    "otherElements=" + otherElements + ", " +
                    "otherTags=" + otherTags + ']';
        }
        
    }
    
    public static final class TagData {
        private final IntList otherElements;
        private final List<ResourceLocation> otherTags;
        
        public TagData(IntList otherElements, List<ResourceLocation> otherTags) {
            this.otherElements = otherElements;
            this.otherTags = otherTags;
        }
        
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
        
        public IntList otherElements() {
            return otherElements;
        }
        
        public List<ResourceLocation> otherTags() {
            return otherTags;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            TagData that = (TagData) obj;
            return Objects.equals(this.otherElements, that.otherElements) &&
                    Objects.equals(this.otherTags, that.otherTags);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(otherElements, otherTags);
        }
        
        @Override
        public String toString() {
            return "TagData[" +
                    "otherElements=" + otherElements + ", " +
                    "otherTags=" + otherTags + ']';
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
            String resourceKey = buf.readUtf();
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
    public static void requestTagData(String resourceKey, Consumer<DataResult<Map<ResourceLocation, TagData>>> callback) {
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
            buf.writeUtf(resourceKey);
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
        public static String nextResourceKey;
        public static Consumer<DataResult<Map<ResourceLocation, TagData>>> nextCallback;
        
        private static void init() {
            ClientLifecycleEvent.CLIENT_WORLD_LOAD.register(world -> {
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
    
    public static <T> void create(String tagCollectionId, TagCollection<? extends T> tagCollection, Registry<? extends T> registry, ResourceLocation tagKey, Consumer<DataResult<TagNode<T>>> callback) {
        requestTagData(tagCollectionId, result -> {
            callback.accept(result.flatMap(dataMap -> dataMap != null ? resolveTag(tagKey, tagCollection, registry, dataMap).orElse(DataResult.error("No tag data")) : DataResult.error("No tag data")));
        });
    }
    
    private static <T> Optional<DataResult<TagNode<T>>> resolveTag(ResourceLocation tagKey, TagCollection<? extends T> tagCollection, Registry<? extends T> registry, Map<ResourceLocation, TagData> tagDataMap) {
        TagData tagData = tagDataMap.get(tagKey);
        if (tagData == null) return Optional.empty();
        
        TagNode<T> self = TagNode.ofReference(tagKey);
        List<T> holders = new ArrayList<>();
        for (int element : tagData.otherElements()) {
            T holder = registry.byId(element);
            if (holder != null) {
                holders.add(holder);
            }
        }
        if (!holders.isEmpty()) {
            self.addValuesChild(holders);
        }
        for (ResourceLocation childTagId : tagData.otherTags()) {
            if (tagCollection.getAvailableTags().contains(childTagId)) {
                Optional<DataResult<TagNode<T>>> resultOptional = resolveTag(childTagId, tagCollection, registry, tagDataMap);
                if (resultOptional.isPresent()) {
                    DataResult<TagNode<T>> result = resultOptional.get();
                    if (result.error().isPresent())
                        return Optional.of(DataResult.error(result.error().get().message()));
                    self.addChild(result.result().get());
                }
            }
        }
        return Optional.of(DataResult.success(self));
    }
}
