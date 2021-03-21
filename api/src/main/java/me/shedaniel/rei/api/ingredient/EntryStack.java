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

package me.shedaniel.rei.api.ingredient;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import me.shedaniel.rei.api.gui.Renderer;
import me.shedaniel.rei.api.gui.widgets.Tooltip;
import me.shedaniel.rei.api.ingredient.entry.EntrySerializer;
import me.shedaniel.rei.api.ingredient.entry.comparison.ComparisonContext;
import me.shedaniel.rei.api.ingredient.entry.renderer.EntryRenderer;
import me.shedaniel.rei.api.ingredient.entry.type.EntryDefinition;
import me.shedaniel.rei.api.ingredient.entry.type.EntryType;
import me.shedaniel.rei.api.util.TextRepresentable;
import me.shedaniel.rei.impl.Internals;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Unit;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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
    
    @ApiStatus.Internal
    static EntryStack<?> readFromJson(JsonElement jsonElement) {
        try {
            JsonObject obj = jsonElement.getAsJsonObject();
            EntryType<Object> type = EntryType.deferred(new ResourceLocation(GsonHelper.getAsString(obj, "type")));
            EntrySerializer<Object> serializer = type.getDefinition().getSerializer();
            if (serializer != null && serializer.supportReading()) {
                Object o = serializer.read(TagParser.parseTag(obj.toString()));
                return EntryStack.of(type, o);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return EntryStack.empty();
    }
    
    @ApiStatus.Internal
    @Nullable
    default JsonElement toJson() {
        try {
            EntrySerializer<T> serializer = getDefinition().getSerializer();
            if (serializer != null && serializer.supportSaving()) {
                JsonObject object = Dynamic.convert(NbtOps.INSTANCE, JsonOps.INSTANCE, serializer.save(this, getValue())).getAsJsonObject();
                object.addProperty("type", getType().getId().toString());
                return object;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    EntryDefinition<T> getDefinition();
    
    default EntryType<T> getType() {
        return getDefinition().getType();
    }
    
    default Class<T> getValueType() {
        return getDefinition().getValueType();
    }
    
    default EntryRenderer<T> getRenderer() {
        EntryRenderer<?> renderer = get(Settings.RENDER).apply(this);
        return renderer == null ? EntryRenderer.empty() : renderer.cast();
    }
    
    @Nullable
    ResourceLocation getIdentifier();
    
    boolean isEmpty();
    
    EntryStack<T> copy();
    
    @ApiStatus.Internal
    default EntryStack<T> rewrap() {
        return copy();
    }
    
    EntryStack<T> normalize();
    
    @Deprecated
    int hashCode();
    
    int hash(ComparisonContext context);
    
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
        public static final Function<EntryStack<?>, EntryRenderer<?>> DEFAULT_RENDERER = stack -> stack.getDefinition().getRenderer();
        public static final BiFunction<EntryStack<?>, Tooltip, Tooltip> DEFAULT_TOOLTIP_PROCESSOR = (stack, tooltip) -> tooltip;
        public static final Settings<Function<EntryStack<?>, EntryRenderer<?>>> RENDER = new Settings<>(DEFAULT_RENDERER);
        @Deprecated
        public static final Settings<BiFunction<EntryStack<?>, Tooltip, Tooltip>> TOOLTIP_PROCESSOR = new Settings<>(DEFAULT_TOOLTIP_PROCESSOR);
        @Deprecated
        public static final Settings<Function<EntryStack<?>, List<Component>>> TOOLTIP_APPEND_EXTRA = new Settings<>(stack -> Collections.emptyList());
        
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
