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

package me.shedaniel.rei.impl.common.display;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import io.netty.buffer.ByteBuf;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.display.DisplaySerializer;
import me.shedaniel.rei.api.common.display.DisplaySerializerRegistry;
import me.shedaniel.rei.api.common.plugins.REICommonPlugin;
import me.shedaniel.rei.api.common.registry.ReloadStage;
import me.shedaniel.rei.impl.common.InternalLogger;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class DisplaySerializerRegistryImpl implements DisplaySerializerRegistry {
    private final BiMap<Identifier, DisplaySerializer<?>> serializers = HashBiMap.create();
    
    @Override
    public ReloadStage getStage() {
        return ReloadStage.START;
    }
    
    @Override
    public void startReload() {
        this.serializers.clear();
    }
    
    @Override
    public <D extends Display> void register(Identifier id, DisplaySerializer<D> serializer) {
        InternalLogger.getInstance().debug("Added display serializer [%s] %s", id, serializer);
        this.serializers.put(id, serializer);
    }
    
    @Override
    @Nullable
    public DisplaySerializer<?> get(Identifier id) {
        return this.serializers.get(id);
    }
    
    @Override
    @Nullable
    public Identifier getId(DisplaySerializer<?> serializer) {
        return this.serializers.inverse().get(serializer);
    }
    
    @Override
    public boolean isRegistered(DisplaySerializer<?> serializer) {
        return this.serializers.containsValue(serializer);
    }
    
    @Override
    public Codec<Display> codec() {
        return serializerCodec().dispatch(Display::getSerializer, DisplaySerializer::codec);
    }
    
    @Override
    public StreamCodec<RegistryFriendlyByteBuf, Display> streamCodec() {
        return serializerStreamCodec().<RegistryFriendlyByteBuf>cast()
                .dispatch(Display::getSerializer, DisplaySerializer::streamCodec);
    }
    
    @Override
    public void acceptPlugin(REICommonPlugin plugin) {
        plugin.registerDisplaySerializer(this);
    }
    
    private Codec<DisplaySerializer<?>> serializerCodec() {
        return Identifier.CODEC.flatXmap(id -> {
            return Optional.ofNullable(this.get(id))
                    .map(DataResult::success)
                    .orElseGet(() -> DataResult.error(() -> "Unknown display serializer id: " + id));
        }, serializer -> {
            if (isRegistered(serializer)) {
                return DataResult.success(getId(serializer));
            } else {
                return DataResult.error(() -> "Unregistered display serializer: " + serializer);
            }
        });
    }
    
    private StreamCodec<ByteBuf, DisplaySerializer<?>> serializerStreamCodec() {
        return new StreamCodec<>() {
            @Override
            public DisplaySerializer<?> decode(ByteBuf object) {
                Identifier id = new FriendlyByteBuf(object).readIdentifier();
                DisplaySerializer<?> serializer = get(id);
                if (serializer == null) {
                    throw new NullPointerException("Unknown display serializer id: " + id);
                } else {
                    return serializer;
                }
            }
            
            @Override
            public void encode(ByteBuf buf, DisplaySerializer<?> serializer) {
                if (isRegistered(serializer)) {
                    new FriendlyByteBuf(buf).writeIdentifier(getId(serializer));
                } else {
                    throw new IllegalArgumentException("Unregistered display serializer: " + serializer);
                }
            }
        };
    }
}
