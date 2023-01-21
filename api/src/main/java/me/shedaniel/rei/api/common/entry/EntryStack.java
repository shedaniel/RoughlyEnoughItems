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

package me.shedaniel.rei.api.common.entry;

import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import me.shedaniel.rei.api.client.config.ConfigObject;
import me.shedaniel.rei.api.client.entry.renderer.EntryRenderer;
import me.shedaniel.rei.api.client.entry.renderer.EntryRendererRegistry;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import me.shedaniel.rei.api.client.gui.widgets.TooltipContext;
import me.shedaniel.rei.api.common.entry.comparison.ComparisonContext;
import me.shedaniel.rei.api.common.entry.type.EntryDefinition;
import me.shedaniel.rei.api.common.entry.type.EntryType;
import me.shedaniel.rei.api.common.entry.type.EntryTypeRegistry;
import me.shedaniel.rei.api.common.util.TextRepresentable;
import me.shedaniel.rei.impl.Internals;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Unit;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * A basic entry stack. An entry stack is a pair of an {@link EntryDefinition} and a {@link T}.
 * <p>
 * Settings may be applied to the entry stack.
 * You may want to use {@link me.shedaniel.rei.api.client.util.ClientEntryStacks} for applying
 * rendering specific settings.
 *
 * @param <T> the type of entry
 * @see EntryStack#empty() for getting the singleton empty entry stack
 * @see me.shedaniel.rei.api.common.util.EntryStacks for creating entry stacks from vanilla items and fluids
 * @see me.shedaniel.rei.api.client.util.ClientEntryStacks for creating entry stacks from renderers
 */
@ApiStatus.NonExtendable
public interface EntryStack<T> extends TextRepresentable, Renderer {
    /**
     * Returns an empty entry stack. This is the singleton instance of {@link EntryStack} that is
     * built-in to the implementation.
     * <p>
     * The entry stack has a type of {@link me.shedaniel.rei.api.common.entry.type.BuiltinEntryTypes#EMPTY}.
     *
     * @return the empty entry stack
     */
    static EntryStack<Unit> empty() {
        return Internals.getEntryStackProvider().empty();
    }
    
    /**
     * Creates an {@link EntryStack} from the given {@link EntryDefinition} and {@link T}.
     * <p>
     * Whether {@param value} accepts {@code null} depends on {@link EntryDefinition#acceptsNull()}.
     *
     * @param definition the entry definition
     * @param value      the value
     * @param <T>        the type of entry
     * @return the entry stack
     */
    static <T> EntryStack<T> of(EntryDefinition<T> definition, T value) {
        return Internals.getEntryStackProvider().of(definition, value);
    }
    
    /**
     * Creates an {@link EntryStack} from the given {@link EntryType} and {@link T}.
     * <p>
     * The {@link EntryDefinition} deferred from the type <b>must</b> be registered,
     * this method is not deferred.
     * <p>
     * Whether {@param value} accepts {@code null} depends on {@link EntryDefinition#acceptsNull()}.
     *
     * @param type  the entry type
     * @param value the value
     * @param <T>   the type of entry
     * @return the entry stack
     * @throws NullPointerException     if the {@link EntryDefinition} is not found
     * @throws IllegalArgumentException if the {@link EntryDefinition} does not accept {@code null}, and {@code value} is {@code null}
     */
    static <T> EntryStack<T> of(EntryType<T> type, T value) {
        return of(type.getDefinition(), value);
    }
    
    /**
     * Reads an {@link EntryStack} from the given {@link CompoundTag}.
     * <p>
     * The compound tag must contain "type" for resolving the {@link EntryDefinition} for
     * the {@link EntryStack}.
     *
     * @param tag the tag
     * @return the entry stack
     * @throws NullPointerException          if the {@link EntryDefinition} is not found
     * @throws UnsupportedOperationException if the {@link EntryDefinition} does not support reading from a tag
     * @see EntrySerializer#supportReading()
     * @see EntryIngredient#read(ListTag)
     */
    static EntryStack<?> read(CompoundTag tag) {
        ResourceLocation type = new ResourceLocation(tag.getString("type"));
        EntryDefinition<?> definition = EntryTypeRegistry.getInstance().get(type);
        if (definition == null) throw new NullPointerException("Read missing entry type: " + type);
        EntrySerializer<?> serializer = definition.getSerializer();
        if (serializer != null && serializer.supportReading()) {
            return EntryStack.of((EntryDefinition<Object>) definition, serializer.read(tag));
        }
        throw new UnsupportedOperationException(definition.getType().getId() + " does not support deserialization!");
    }
    
    /**
     * Saves the entry stack to a {@link CompoundTag}. This is only supported if the entry stack has a serializer.
     *
     * @return the saved tag
     * @throws UnsupportedOperationException if the {@link EntryDefinition} does not support saving to a tag
     * @see EntrySerializer#supportSaving()
     * @see EntryIngredient#saveIngredient()
     * @since 8.3
     */
    @Nullable
    default CompoundTag saveStack() {
        return save();
    }
    
    /**
     * Saves the entry stack to a {@link CompoundTag}. This is only supported if the entry stack has a serializer.
     *
     * @return the saved tag
     * @throws UnsupportedOperationException if the {@link EntryDefinition} does not support saving to a tag
     * @see EntrySerializer#supportSaving()
     * @see EntryIngredient#saveIngredient()
     * @deprecated use {@link #saveStack()} instead
     */
    @Nullable
    @Deprecated(forRemoval = true)
    default CompoundTag save() {
        if (supportSaving()) {
            CompoundTag tag = getDefinition().getSerializer().save(this, getValue());
            tag.putString("type", getType().getId().toString());
            return tag;
        }
        throw new UnsupportedOperationException(getType().getId() + " does not support serialization!");
    }
    
    /**
     * Returns whether the {@link EntryDefinition} of this {@link EntryStack} supports saving to a tag.
     *
     * @return whether the {@link EntryDefinition} of this {@link EntryStack} supports saving to a tag
     */
    default boolean supportSaving() {
        EntrySerializer<T> serializer = getDefinition().getSerializer();
        return serializer != null && serializer.supportSaving();
    }
    
    /**
     * Returns the tooltip for this {@link EntryStack}, can be {@code null}.
     * <p>
     * The base implementation depends on {@link EntryRenderer#getTooltip(EntryStack, TooltipContext)},
     * see {@link EntryStack#getRenderer()} to see how the renderer is resolved.
     * <p>
     * It is possible to process the base tooltip at a per {@link EntryType} level
     * using {@link EntryRendererRegistry#transformTooltip(EntryType, EntryRendererRegistry.TooltipTransformer)}.
     * <p>
     * This tooltip can be appended by {@link EntryStack#tooltip(Component...)},
     * and further processed by {@link me.shedaniel.rei.api.client.util.ClientEntryStacks#setTooltipProcessor(EntryStack, BiFunction)}.
     *
     * @param context       the tooltip context
     * @param appendModName whether to append the mod name
     * @return the tooltip, can be {@code null}
     */
    @Nullable
    @Environment(EnvType.CLIENT)
    Tooltip getTooltip(TooltipContext context, boolean appendModName);
    
    /**
     * Returns the tooltip for this {@link EntryStack}, can be {@code null}.
     * <p>
     * The base implementation depends on {@link EntryRenderer#getTooltip(EntryStack, TooltipContext)},
     * see {@link EntryStack#getRenderer()} to see how the renderer is resolved.
     * <p>
     * It is possible to process the base tooltip at a per {@link EntryType} level
     * using {@link EntryRendererRegistry#transformTooltip(EntryType, EntryRendererRegistry.TooltipTransformer)}.
     * <p>
     * This tooltip can be appended by {@link EntryStack#tooltip(Component...)},
     * and further processed by {@link me.shedaniel.rei.api.client.util.ClientEntryStacks#setTooltipProcessor(EntryStack, BiFunction)}.
     *
     * @param context the tooltip context
     * @return the tooltip, can be {@code null}
     */
    @Override
    @Nullable
    default Tooltip getTooltip(TooltipContext context) {
        return getTooltip(context, ConfigObject.getInstance().shouldAppendModNames());
    }
    
    /**
     * Returns the {@link EntryDefinition} of this {@link EntryStack}.
     *
     * @return the {@link EntryDefinition} of this {@link EntryStack}
     */
    EntryDefinition<T> getDefinition();
    
    /**
     * Returns the {@link EntryType} of this {@link EntryStack}.
     *
     * @return the {@link EntryType} of this {@link EntryStack}
     */
    default EntryType<T> getType() {
        return getDefinition().getType();
    }
    
    /**
     * Returns the base {@link Class} type from the {@link EntryType} of this {@link EntryStack}.
     *
     * @return the base {@link Class} type
     */
    default Class<T> getValueType() {
        return getDefinition().getValueType();
    }
    
    /**
     * Returns the {@link EntryRenderer} of this {@link EntryStack}.
     * <p>
     * The base implementation is at {@link EntryDefinition#getRenderer()},
     * then is processed by {@link EntryRendererRegistry}.
     * <p>
     * To modify the renderer at a per stack level,
     * use {@link me.shedaniel.rei.api.client.util.ClientEntryStacks#setRenderer(EntryStack, EntryRenderer)}.
     *
     * @return the {@link EntryRenderer} of this {@link EntryStack}
     */
    @Environment(EnvType.CLIENT)
    default EntryRenderer<T> getRenderer() {
        EntryRenderer<?> renderer = get(Settings.RENDERER).apply(this);
        return renderer == null ? EntryRenderer.empty() : renderer.cast();
    }
    
    /**
     * Returns the identifier for this {@link EntryStack}, used in identifier search argument type.
     *
     * @return the identifier for this {@link EntryStack}, can be {@code null}
     * @see EntryDefinition#getIdentifier(EntryStack, Object)
     */
    @Nullable
    ResourceLocation getIdentifier();
    
    /**
     * Returns the container namespace of this {@link EntryStack}, used for determining the
     * responsible mod for the entry.
     * <p>
     * It is possible to modify this at a per {@link EntryStack} level using {@link Settings#CONTAINING_NS},
     * however it isn't recommended to do so.
     *
     * @return the container namespace for this {@link EntryStack}, can be {@code null}
     * @see EntryDefinition#getContainingNamespace(EntryStack, Object)
     */
    @Nullable
    String getContainingNamespace();
    
    /**
     * Returns whether this {@link EntryStack} is empty, empty entries are not displayed,
     * and are considered invalid.
     * Empty entries will be treated equally to {@link EntryStack#empty()}.
     *
     * @return whether this {@link EntryStack} is empty
     * @see EntryDefinition#isEmpty(EntryStack, Object)
     */
    boolean isEmpty();
    
    /**
     * Returns a copy of this {@link EntryStack}.
     * The copied stack will retain the same settings applied, with a copied value.
     *
     * @return a copy for an entry
     */
    EntryStack<T> copy();
    
    /**
     * Returns a copy of this {@link EntryStack}.
     * The copied stack will retain the value object, with no settings applied.
     *
     * @return a copy for an entry
     */
    default EntryStack<T> rewrap() {
        return copy();
    }
    
    /**
     * Returns a copy of this {@link EntryStack}.
     * The copied stack will have no settings applied.
     * <p>
     * The new value should be functionally equivalent to the original value,
     * but should have a normalized state.
     * <p>
     * For example, an {@link net.minecraft.world.item.ItemStack} should have its
     * amount removed, but its tags kept.
     *
     * @return a copy for an entry
     * @see EntryDefinition#normalize(EntryStack, Object)
     */
    EntryStack<T> normalize();
    
    /**
     * Returns a copy of this {@link EntryStack}.
     * The copied stack will have no settings applied.
     * <p>
     * The new value should be the bare minimum to match the original value.
     * <p>
     * For example, an {@link net.minecraft.world.item.ItemStack} should have its
     * amount and tags removed.
     *
     * @return a copy for an entry
     * @see EntryDefinition#wildcard(EntryStack, Object)
     * @since 6.2
     */
    EntryStack<T> wildcard();
    
    /**
     * Returns a stream of {@link TagKey} for this {@link EntryStack}.
     * It is not guaranteed that the stream is ordered, or that the {@link TagKey}
     * contains the registry key.
     *
     * @return a stream of {@link TagKey} for this {@link EntryStack}
     * @see EntryDefinition#getTagsFor(EntryStack, Object)
     */
    Stream<TagKey<?>> getTagsFor();
    
    /**
     * Returns a hash code of this {@link EntryStack}. This method
     * uses the {@link me.shedaniel.rei.api.common.entry.comparison.ComparisonContext#EXACT}
     * comparison context.
     *
     * @return a hash code of the entry
     * @see me.shedaniel.rei.api.common.util.EntryStacks#hash(EntryStack, ComparisonContext)
     * @see me.shedaniel.rei.api.common.util.EntryStacks#hashExact(EntryStack)
     * @see me.shedaniel.rei.api.common.util.EntryStacks#hashFuzzy(EntryStack)
     */
    @Deprecated
    int hashCode();
    
    /**
     * Returns whether the given object is equals to this {@link EntryStack}. This method
     * uses the {@link me.shedaniel.rei.api.common.entry.comparison.ComparisonContext#EXACT}
     * comparison context.
     *
     * @return whether the given object is equals to this {@link EntryStack}
     * @see me.shedaniel.rei.api.common.util.EntryStacks#equals(EntryStack, EntryStack, ComparisonContext)
     * @see me.shedaniel.rei.api.common.util.EntryStacks#equalsExact(EntryStack, EntryStack)
     * @see me.shedaniel.rei.api.common.util.EntryStacks#equalsFuzzy(EntryStack, EntryStack)
     */
    @Deprecated
    boolean equals(Object o);
    
    /**
     * Returns the {@link T} value of this {@link EntryStack}.
     *
     * @return the value of this {@link EntryStack}
     * @see EntryStack#castValue() for casting the value to a different type
     */
    T getValue();
    
    /**
     * Returns the {@link T} value of this {@link EntryStack} in {@link R}.
     *
     * @return the value of this {@link EntryStack} in {@link R}
     */
    default <R> R castValue() {
        return (R) getValue();
    }
    
    /**
     * Applies a setting to this {@link EntryStack}.
     * <p>
     * It is generally not recommended to use this method, but to instead use the helper
     * methods such as {@link EntryStack#tooltip(Component...)} and
     * the methods in {@link me.shedaniel.rei.api.client.util.ClientEntryStacks}.
     *
     * @param settings the setting to apply
     * @param value    the value of the setting to apply
     * @param <R>      the type of the setting
     * @return this {@link EntryStack}
     */
    <R> EntryStack<T> setting(Settings<R> settings, R value);
    
    /**
     * Removes a setting from this {@link EntryStack}.
     *
     * @param settings the setting to remove
     * @param <R>      the type of the setting
     * @return this {@link EntryStack}
     */
    <R> EntryStack<T> removeSetting(Settings<R> settings);
    
    /**
     * Clears all settings from this {@link EntryStack}.
     * <p>
     * You may also use {@link EntryStack#rewrap()} to clone without the settings.
     *
     * @return this {@link EntryStack}
     */
    EntryStack<T> clearSettings();
    
    /**
     * Returns the value of a {@link Settings} of this {@link EntryStack}.
     * <p>
     * This method returns the default value of the setting if the setting is not set.
     *
     * @param settings the setting to get
     * @param <R>      the type of the setting
     * @return the value of the setting
     */
    <R> R get(Settings<R> settings);
    
    /**
     * Returns the value of a {@link Settings} of this {@link EntryStack},
     * or {@code null} if the setting is not set.
     *
     * @param settings the setting to get
     * @param <R>      the type of the setting
     * @return the value of the setting, or {@code null} if the setting is not set
     */
    @Nullable <R> R getNullable(Settings<R> settings);
    
    /**
     * Appends a tooltip to this {@link EntryStack}. This method will replace any existing appended tooltips.
     * <p>
     * You can transform the tooltip on a {@link EntryType} level
     * using {@link EntryRendererRegistry#transformTooltip(EntryType, EntryRendererRegistry.TooltipTransformer)}.
     * <p>
     * To modify the tooltip, use {@link me.shedaniel.rei.api.client.util.ClientEntryStacks#setTooltipProcessor(EntryStack, BiFunction)} instead.
     *
     * @param tooltips the tooltips to append
     * @return this {@link EntryStack}
     * @see EntryStack#getTooltip(TooltipContext, boolean) for how the tooltip is resolved
     */
    @Environment(EnvType.CLIENT)
    default EntryStack<T> tooltip(Component... tooltips) {
        return tooltip(Arrays.asList(tooltips));
    }
    
    /**
     * Appends a tooltip to this {@link EntryStack}. This method will replace any existing appended tooltips.
     * <p>
     * You can transform the tooltip on a {@link EntryType} level
     * using {@link EntryRendererRegistry#transformTooltip(EntryType, EntryRendererRegistry.TooltipTransformer)}.
     * <p>
     * To modify the tooltip, use {@link me.shedaniel.rei.api.client.util.ClientEntryStacks#setTooltipProcessor(EntryStack, BiFunction)} instead.
     *
     * @param tooltips the tooltips to append
     * @return this {@link EntryStack}
     * @see EntryStack#getTooltip(TooltipContext, boolean) for how the tooltip is resolved
     */
    @Environment(EnvType.CLIENT)
    default EntryStack<T> tooltip(List<Component> tooltips) {
        return tooltip(stack -> tooltips);
    }
    
    /**
     * Appends a tooltip to this {@link EntryStack}. This method will replace any existing appended tooltips.
     * <p>
     * You can transform the tooltip on a {@link EntryType} level
     * using {@link EntryRendererRegistry#transformTooltip(EntryType, EntryRendererRegistry.TooltipTransformer)}.
     * <p>
     * To modify the tooltip, use {@link me.shedaniel.rei.api.client.util.ClientEntryStacks#setTooltipProcessor(EntryStack, BiFunction)} instead.
     *
     * @param tooltipProvider the provider for the tooltips to append
     * @return this {@link EntryStack}
     * @see EntryStack#getTooltip(TooltipContext, boolean) for how the tooltip is resolved
     */
    @Environment(EnvType.CLIENT)
    default EntryStack<T> tooltip(Function<EntryStack<?>, List<Component>> tooltipProvider) {
        return setting(Settings.TOOLTIP_APPEND_EXTRA, tooltipProvider);
    }
    
    /**
     * Returns the cheated stack of this {@link EntryStack}.
     *
     * @return the cheated stack of this {@link EntryStack}
     * @see EntryDefinition#cheatsAs(EntryStack, Object)
     */
    EntryStack<ItemStack> cheatsAs();
    
    @Deprecated
    class Settings<R> {
        @ApiStatus.Internal
        private static final List<Settings<?>> SETTINGS = new ArrayList<>();
        
        @Environment(EnvType.CLIENT)
        public static Settings<Function<EntryStack<?>, EntryRenderer<?>>> RENDERER;
        @Environment(EnvType.CLIENT)
        public static Settings<BiFunction<EntryStack<?>, Tooltip, Tooltip>> TOOLTIP_PROCESSOR;
        @Environment(EnvType.CLIENT)
        public static Settings<BiFunction<EntryStack<?>, String, String>> CONTAINING_NS;
        @Environment(EnvType.CLIENT)
        public static Settings<Function<EntryStack<?>, List<Component>>> TOOLTIP_APPEND_EXTRA;
        @Environment(EnvType.CLIENT)
        @Deprecated
        @ApiStatus.Internal
        public static Settings<Boolean> FLUID_AMOUNT_VISIBLE;
        @Environment(EnvType.CLIENT)
        public static Settings<Float> FLUID_RENDER_RATIO;
        
        static {
            EnvExecutor.runInEnv(Env.CLIENT, () -> () -> {
                RENDERER = new Settings<>(stack -> EntryRendererRegistry.getInstance().get(stack));
                TOOLTIP_PROCESSOR = new Settings<>((stack, tooltip) -> tooltip);
                CONTAINING_NS = new Settings<>((stack, ns) -> ns);
                TOOLTIP_APPEND_EXTRA = new Settings<>(stack -> Collections.emptyList());
                FLUID_RENDER_RATIO = new Settings<>(1.0F);
                FLUID_AMOUNT_VISIBLE = new Settings<>(true);
            });
        }
        
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
    
    /**
     * Casts this {@link EntryStack} to a {@link EntryStack} of the given type.
     *
     * @param <O> the new type
     * @return the casted {@link EntryStack}
     */
    @ApiStatus.NonExtendable
    default <O> EntryStack<O> cast() {
        return (EntryStack<O>) this;
    }
}
