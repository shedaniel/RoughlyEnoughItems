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

package me.shedaniel.rei.impl.common.entry;

import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.common.entry.type.BuiltinEntryTypes;
import me.shedaniel.rei.api.common.entry.type.EntryDefinition;
import me.shedaniel.rei.api.common.entry.type.EntryType;
import me.shedaniel.rei.impl.client.entry.type.types.RenderingEntryDefinition;
import me.shedaniel.rei.impl.common.entry.type.EntryTypeDeferred;
import me.shedaniel.rei.impl.common.entry.type.types.EmptyEntryDefinition;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Unit;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public enum DeferringEntryTypeProviderImpl implements Function<ResourceLocation, EntryType<?>> {
    INSTANCE;
    ResourceLocation RENDERING_ID = new ResourceLocation("rendering");
    private Map<ResourceLocation, EntryType<?>> typeCache = new ConcurrentHashMap<>();
    private EntryType<Unit> empty;
    @Environment(EnvType.CLIENT)
    private EntryType<Renderer> render;
    
    @Override
    public EntryType<?> apply(ResourceLocation id) {
        if (id.equals(BuiltinEntryTypes.EMPTY_ID)) {
            return typeCache.computeIfAbsent(id, this::emptyType);
        } else if (id.equals(RENDERING_ID) && Platform.getEnvironment() == Env.CLIENT) {
            return typeCache.computeIfAbsent(id, this::renderingType);
        }
        return typeCache.computeIfAbsent(id, EntryTypeDeferred::new);
    }
    
    public EntryType<Unit> emptyType(ResourceLocation id) {
        if (empty == null) {
            int hashCode = id.hashCode();
            empty = new EntryType<>() {
                @Override
                public ResourceLocation getId() {
                    return id;
                }
                
                @Override
                public EntryDefinition<Unit> getDefinition() {
                    return EmptyEntryDefinition.EMPTY;
                }
                
                @Override
                public int hashCode() {
                    return hashCode;
                }
            };
        }
        return empty;
    }
    
    @Environment(EnvType.CLIENT)
    public EntryType<Renderer> renderingType(ResourceLocation id) {
        if (render == null) {
            int hashCode = id.hashCode();
            render = new EntryType<>() {
                @Override
                public ResourceLocation getId() {
                    return id;
                }
                
                @Override
                public EntryDefinition<Renderer> getDefinition() {
                    return RenderingEntryDefinition.RENDERING;
                }
                
                @Override
                public int hashCode() {
                    return hashCode;
                }
            };
        }
        return render;
    }
}
