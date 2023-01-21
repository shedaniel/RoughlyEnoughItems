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

package me.shedaniel.rei.api.common.display.basic;

import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.display.SimpleDisplaySerializer;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

/**
 * A basic implementation of a display, consisting of a list of inputs, a list of outputs
 * and a possible display location.
 */
public abstract class BasicDisplay implements Display {
    protected List<EntryIngredient> inputs;
    protected List<EntryIngredient> outputs;
    protected Optional<ResourceLocation> location;
    
    public BasicDisplay(List<EntryIngredient> inputs, List<EntryIngredient> outputs) {
        this(inputs, outputs, Optional.empty());
    }
    
    public BasicDisplay(List<EntryIngredient> inputs, List<EntryIngredient> outputs, Optional<ResourceLocation> location) {
        this.inputs = inputs;
        this.outputs = outputs;
        this.location = location;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public List<EntryIngredient> getInputEntries() {
        return inputs;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public List<EntryIngredient> getOutputEntries() {
        return outputs;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<ResourceLocation> getDisplayLocation() {
        return location;
    }
    
    /**
     * A basic serializer for {@link BasicDisplay}s.
     *
     * @param <P> the type of the display
     */
    public static class Serializer<P extends BasicDisplay> implements SimpleDisplaySerializer<P> {
        protected final Constructor<P> constructor;
        protected final ExtraSerializer<P> extraSerializer;
        protected EntryIngredientsProvider<P> inputEntries = EntryIngredientsProvider.pass();
        protected EntryIngredientsProvider<P> outputEntries = EntryIngredientsProvider.pass();
        
        /**
         * Creates a new serializer with a constructor for list of inputs, list of outputs and a possible location.
         *
         * @param constructor the constructor for the display
         * @param <P>         the type of the display
         * @return the serializer
         */
        public static <P extends BasicDisplay> Serializer<P> ofSimple(SimpleConstructor<P> constructor) {
            return new Serializer<>(constructor, (p, tag) -> {});
        }
        
        /**
         * Creates a new serializer with a constructor for list of inputs, list of outputs and the serialization tag.
         *
         * @param constructor the constructor for the display
         * @param <P>         the type of the display
         * @return the serializer
         */
        public static <P extends BasicDisplay> Serializer<P> ofRecipeLess(RecipeLessConstructor<P> constructor) {
            return new Serializer<>(constructor, (p, tag) -> {});
        }
        
        /**
         * Creates a new serializer with a constructor for list of inputs and list of outputs.
         *
         * @param constructor the constructor for the display
         * @param <P>         the type of the display
         * @return the serializer
         */
        public static <P extends BasicDisplay> Serializer<P> ofSimpleRecipeLess(SimpleRecipeLessConstructor<P> constructor) {
            return new Serializer<>(constructor, (p, tag) -> {});
        }
        
        /**
         * Creates a new serializer with a constructor for list of inputs, list of outputs, a possible location, the serialization tag.
         *
         * @param constructor the constructor for the display
         * @param <P>         the type of the display
         * @return the serializer
         */
        public static <P extends BasicDisplay> Serializer<P> of(Constructor<P> constructor) {
            return new Serializer<>(constructor, (p, tag) -> {});
        }
        
        private Serializer(Constructor<P> constructor) {
            this(constructor, (p, tag) -> {});
        }
        
        /**
         * Creates a new serializer with a constructor for list of inputs, list of outputs and a possible location,
         * with a serializer for serializing extra data.
         *
         * @param constructor     the constructor for the display
         * @param extraSerializer the extra serializer for the display
         * @param <P>             the type of the display
         * @return the serializer
         */
        public static <P extends BasicDisplay> Serializer<P> ofSimple(SimpleConstructor<P> constructor, ExtraSerializer<P> extraSerializer) {
            return new Serializer<>(constructor, extraSerializer);
        }
        
        /**
         * Creates a new serializer with a constructor for list of inputs, list of outputs and the serialization tag,
         * with a serializer for serializing extra data.
         *
         * @param constructor     the constructor for the display
         * @param extraSerializer the extra serializer for the display
         * @param <P>             the type of the display
         * @return the serializer
         */
        public static <P extends BasicDisplay> Serializer<P> ofRecipeLess(RecipeLessConstructor<P> constructor, ExtraSerializer<P> extraSerializer) {
            return new Serializer<>(constructor, extraSerializer);
        }
        
        /**
         * Creates a new serializer with a constructor for list of inputs and list of outputs,
         * with a serializer for serializing extra data.
         *
         * @param constructor     the constructor for the display
         * @param extraSerializer the extra serializer for the display
         * @param <P>             the type of the display
         * @return the serializer
         */
        public static <P extends BasicDisplay> Serializer<P> ofSimpleRecipeLess(SimpleRecipeLessConstructor<P> constructor, ExtraSerializer<P> extraSerializer) {
            return new Serializer<>(constructor, extraSerializer);
        }
        
        /**
         * Creates a new serializer with a constructor for list of inputs, list of outputs, a possible location, the serialization tag,
         * with a serializer for serializing extra data.
         *
         * @param constructor     the constructor for the display
         * @param extraSerializer the extra serializer for the display
         * @param <P>             the type of the display
         * @return the serializer
         */
        public static <P extends BasicDisplay> Serializer<P> of(Constructor<P> constructor, ExtraSerializer<P> extraSerializer) {
            return new Serializer<>(constructor, extraSerializer);
        }
        
        protected Serializer(Constructor<P> constructor, ExtraSerializer<P> extraSerializer) {
            this.constructor = constructor;
            this.extraSerializer = extraSerializer;
        }
        
        /**
         * Sets the provider for the list of inputs.
         *
         * @param provider the provider
         * @return the serializer, for chaining
         */
        public Serializer<P> inputProvider(EntryIngredientsProvider<P> provider) {
            this.inputEntries = provider;
            return this;
        }
        
        /**
         * Sets the provider for the list of outputs.
         *
         * @param provider the provider
         * @return the serializer, for chaining
         */
        public Serializer<P> outputProvider(EntryIngredientsProvider<P> provider) {
            this.outputEntries = provider;
            return this;
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public CompoundTag saveExtra(CompoundTag tag, P display) {
            display.getDisplayLocation().ifPresent(location -> tag.putString("location", location.toString()));
            extraSerializer.serialize(display, tag);
            return tag;
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public P read(CompoundTag tag) {
            List<EntryIngredient> input = EntryIngredients.read(tag.getList("input", Tag.TAG_LIST));
            List<EntryIngredient> output = EntryIngredients.read(tag.getList("output", Tag.TAG_LIST));
            ResourceLocation location;
            if (tag.contains("location", Tag.TAG_STRING)) {
                location = new ResourceLocation(tag.getString("location"));
            } else {
                location = null;
            }
            return constructor.construct(input, output, Optional.ofNullable(location), tag);
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public List<EntryIngredient> getInputIngredients(P display) {
            List<EntryIngredient> entries = this.inputEntries.getEntries(display);
            if (entries != null) return entries;
            return SimpleDisplaySerializer.super.getInputIngredients(display);
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public List<EntryIngredient> getOutputIngredients(P display) {
            List<EntryIngredient> entries = this.outputEntries.getEntries(display);
            if (entries != null) return entries;
            return SimpleDisplaySerializer.super.getOutputIngredients(display);
        }
        
        @FunctionalInterface
        public interface Constructor<R> {
            R construct(List<EntryIngredient> input, List<EntryIngredient> output, Optional<ResourceLocation> location, CompoundTag tag);
        }
        
        @FunctionalInterface
        public interface SimpleConstructor<R extends Display> extends Constructor<R> {
            @Override
            default R construct(List<EntryIngredient> input, List<EntryIngredient> output, Optional<ResourceLocation> location, CompoundTag tag) {
                return construct(input, output, location);
            }
            
            R construct(List<EntryIngredient> input, List<EntryIngredient> output, Optional<ResourceLocation> location);
        }
        
        @FunctionalInterface
        public interface RecipeLessConstructor<R extends Display> extends Constructor<R> {
            @Override
            default R construct(List<EntryIngredient> input, List<EntryIngredient> output, Optional<ResourceLocation> location, CompoundTag tag) {
                return construct(input, output, tag);
            }
            
            R construct(List<EntryIngredient> input, List<EntryIngredient> output, CompoundTag tag);
        }
        
        @FunctionalInterface
        public interface SimpleRecipeLessConstructor<R extends Display> extends Constructor<R> {
            @Override
            default R construct(List<EntryIngredient> input, List<EntryIngredient> output, Optional<ResourceLocation> location, CompoundTag tag) {
                return construct(input, output);
            }
            
            R construct(List<EntryIngredient> input, List<EntryIngredient> output);
        }
        
        @FunctionalInterface
        public interface ExtraSerializer<R extends Display> {
            void serialize(R display, CompoundTag tag);
        }
        
        @FunctionalInterface
        public interface EntryIngredientsProvider<R extends Display> {
            @Nullable
            List<EntryIngredient> getEntries(R display);
            
            static <R extends Display> EntryIngredientsProvider<R> pass() {
                return display -> null;
            }
        }
    }
}
