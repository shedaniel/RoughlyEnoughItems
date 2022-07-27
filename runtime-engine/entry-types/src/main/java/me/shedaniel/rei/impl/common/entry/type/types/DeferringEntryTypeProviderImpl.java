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

package me.shedaniel.rei.impl.common.entry.type.types;

import com.google.common.base.Suppliers;
import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.common.entry.type.BuiltinEntryTypes;
import me.shedaniel.rei.api.common.entry.type.EntryDefinition;
import me.shedaniel.rei.api.common.entry.type.EntryType;
import me.shedaniel.rei.impl.Internals;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Unit;
import org.jetbrains.annotations.ApiStatus;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

@ApiStatus.Internal
public class DeferringEntryTypeProviderImpl implements Internals.DeferringEntryTypeProvider {
    private static final ResourceLocation RENDERING_ID = new ResourceLocation("rendering");
    private final Map<ResourceLocation, EntryType<?>> typeCache = new ConcurrentHashMap<>();
    private final Supplier<EntryType<Unit>> empty = Suppliers.memoize(() -> emptyType(BuiltinEntryTypes.EMPTY_ID));
    @Environment(EnvType.CLIENT)
    private Supplier<EntryType<Renderer>> render;
    
    public DeferringEntryTypeProviderImpl() {
        if (Platform.getEnvironment() == Env.CLIENT) {
            render = Suppliers.memoize(() -> renderingType(RENDERING_ID));
        }
    }
    
    @Override
    public EntryType<?> get(ResourceLocation id) {
        if (id.equals(BuiltinEntryTypes.EMPTY_ID)) {
            return typeCache.computeIfAbsent(id, $ -> empty.get());
        } else if (id.equals(RENDERING_ID) && Platform.getEnvironment() == Env.CLIENT) {
            return typeCache.computeIfAbsent(id, $ -> render.get());
        }
        return typeCache.computeIfAbsent(id, DeferredEntryTypeImpl::new);
    }
    
    public EntryType<Unit> emptyType(ResourceLocation id) {
        int hashCode = id.hashCode();
        return new EntryType<>() {
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
    
    @Environment(EnvType.CLIENT)
    public EntryType<Renderer> renderingType(ResourceLocation id) {
        int hashCode = id.hashCode();
        return new EntryType<>() {
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
}
