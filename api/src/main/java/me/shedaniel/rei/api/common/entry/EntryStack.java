/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020 shedaniel
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

package me.shedaniel.rei.api.common.entry;

import me.shedaniel.math.Point;
import me.shedaniel.rei.api.client.config.ConfigObject;
import me.shedaniel.rei.api.client.entry.renderer.EntryRenderer;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import me.shedaniel.rei.api.common.entry.type.EntryDefinition;
import me.shedaniel.rei.api.common.entry.type.EntryType;
import me.shedaniel.rei.api.common.entry.type.EntryTypeRegistry;
import me.shedaniel.rei.api.common.util.TextRepresentable;
import me.shedaniel.rei.impl.Internals;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Unit;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

@Environment(EnvType.CLIENT)
public interface EntryStack<T> extends TextRepresentable, Renderer {
    static EntryStack<Unit> empty() {
        return Internals.getEntryStackProvider().empty();
    }
    
    static <T> EntryStack<T> of(EntryDefinition<T> definition, T value) {
        return Internals.getEntryStackProvider().of(definition, value);
    }
    
    static <T> EntryStack<T> of(EntryType<T> type, T value) {
        return of(type.getDefinition(), value);
    }
    
    static EntryStack<?> read(CompoundTag tag) {
        EntryDefinition<?> definition = EntryTypeRegistry.getInstance().get(new ResourceLocation(tag.getString("type")));
        EntrySerializer<?> serializer = definition.getSerializer();
        if (serializer != null && serializer.supportReading()) {
            return EntryStack.of((EntryDefinition<Object>) definition, serializer.read(tag));
        }
        throw new UnsupportedOperationException(definition.getType().getId() + " does not support deserialization!");
    }
    
    @Nullable
    default CompoundTag save() {
        EntrySerializer<T> serializer = getDefinition().getSerializer();
        if (serializer != null && serializer.supportSaving()) {
            CompoundTag tag = serializer.save(this, getValue());
            tag.putString("type", getType().getId().toString());
            return tag;
        }
        throw new UnsupportedOperationException(getType().getId() + " does not support serialization!");
    }
    
    @Nullable
    @Environment(EnvType.CLIENT)
    Tooltip getTooltip(Point mouse, boolean appendModName);
    
    @Override
    @Nullable
    default Tooltip getTooltip(Point mouse) {
        return getTooltip(mouse, ConfigObject.getInstance().shouldAppendModNames());
    }
    
    EntryDefinition<T> getDefinition();
    
    default EntryType<T> getType() {
        return getDefinition().getType();
    }
    
    default Class<T> getValueType() {
        return getDefinition().getValueType();
    }
    
    @Environment(EnvType.CLIENT)
    default EntryRenderer<T> getRenderer() {
        EntryRenderer<?> renderer = get(Settings.RENDER).apply(this);
        return renderer == null ? EntryRenderer.empty() : renderer.cast();
    }
    
    @Nullable
    ResourceLocation getIdentifier();
    
    boolean isEmpty();
    
    EntryStack<T> copy();
    
    default EntryStack<T> rewrap() {
        return copy();
    }
    
    EntryStack<T> normalize();
    
    Collection<ResourceLocation> getTagsFor();
    
    @Deprecated
    int hashCode();
    
    @Deprecated
    boolean equals(Object o);
    
    T getValue();
    
    <R> EntryStack<T> setting(Settings<R> settings, R value);
    
    <R> EntryStack<T> removeSetting(Settings<R> settings);
    
    EntryStack<T> clearSettings();
    
    <R> R get(Settings<R> settings);
    
    default EntryStack<T> tooltip(Component... tooltips) {
        return tooltip(Arrays.asList(tooltips));
    }
    
    default EntryStack<T> tooltip(List<Component> tooltips) {
        return tooltip(stack -> tooltips);
    }
    
    default EntryStack<T> tooltip(Function<EntryStack<?>, List<Component>> tooltipProvider) {
        return setting(Settings.TOOLTIP_APPEND_EXTRA, tooltipProvider);
    }
    
    class Settings<R> {
        @ApiStatus.Internal
        private static final List<Settings<?>> SETTINGS = new ArrayList<>();
        
        public static final Supplier<Boolean> TRUE = () -> true;
        public static final Supplier<Boolean> FALSE = () -> false;
        @Environment(EnvType.CLIENT)
        public static final Function<EntryStack<?>, EntryRenderer<?>> DEFAULT_RENDERER = stack -> stack.getDefinition().getRenderer();
        @Environment(EnvType.CLIENT)
        public static final Function<EntryStack<?>, EntryRenderer<?>> EMPTY_RENDERER = stack -> EntryRenderer.empty();
        public static final BiFunction<EntryStack<?>, Tooltip, Tooltip> DEFAULT_TOOLTIP_PROCESSOR = (stack, tooltip) -> tooltip;
        @Environment(EnvType.CLIENT)
        public static final Settings<Function<EntryStack<?>, EntryRenderer<?>>> RENDER = new Settings<>(DEFAULT_RENDERER);
        @Deprecated
        public static final Settings<BiFunction<EntryStack<?>, Tooltip, Tooltip>> TOOLTIP_PROCESSOR = new Settings<>(DEFAULT_TOOLTIP_PROCESSOR);
        @Deprecated
        public static final Settings<Function<EntryStack<?>, List<Component>>> TOOLTIP_APPEND_EXTRA = new Settings<>(stack -> Collections.emptyList());
        @Environment(EnvType.CLIENT)
        public static final Float DEFAULT_RENDER_RATIO = 1.0F;
        @Environment(EnvType.CLIENT)
        public static final Settings<Float> FLUID_RENDER_RATIO = new Settings<>(DEFAULT_RENDER_RATIO);
        
        private R defaultValue;
        private short id;
        
        @ApiStatus.Internal
        public Settings(R defaultValue) {
            this.defaultValue = defaultValue;
            SETTINGS.add(this);
            this.id = (short) SETTINGS.indexOf(this);
        }
        
        @ApiStatus.Internal
        public static <R> Settings<R> getById(short id) {
            return (Settings<R>) SETTINGS.get(id);
        }
        
        public R getDefaultValue() {
            return defaultValue;
        }
        
        @ApiStatus.Internal
        public short getId() {
            return id;
        }
    }
    
    @ApiStatus.NonExtendable
    default <O> EntryStack<O> cast() {
        return (EntryStack<O>) this;
    }
}
