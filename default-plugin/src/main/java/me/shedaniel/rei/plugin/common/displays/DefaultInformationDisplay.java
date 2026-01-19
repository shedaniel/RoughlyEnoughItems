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

package me.shedaniel.rei.plugin.common.displays;

import com.google.common.collect.Lists;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.display.DisplaySerializer;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.plugin.common.BuiltinPlugin;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

import java.util.*;

public class DefaultInformationDisplay implements Display {
    public static final DisplaySerializer<DefaultInformationDisplay> SERIALIZER = DisplaySerializer.of(
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    EntryIngredient.codec().fieldOf("stacks").forGetter(DefaultInformationDisplay::getEntryStacks),
                    ComponentSerialization.CODEC.fieldOf("name").forGetter(DefaultInformationDisplay::getName),
                    ComponentSerialization.CODEC.listOf().fieldOf("texts").forGetter(DefaultInformationDisplay::getTexts)
            ).apply(instance, (stacks, name, texts) -> new DefaultInformationDisplay(stacks, name).lines(texts))),
            StreamCodec.composite(
                    EntryIngredient.streamCodec(),
                    DefaultInformationDisplay::getEntryStacks,
                    ComponentSerialization.STREAM_CODEC,
                    DefaultInformationDisplay::getName,
                    ComponentSerialization.STREAM_CODEC.apply(ByteBufCodecs.list()),
                    DefaultInformationDisplay::getTexts,
                    (stacks, name, texts) -> new DefaultInformationDisplay(stacks, name).lines(texts)
            ));
    
    private final EntryIngredient entryStacks;
    private final List<Component> texts;
    private final Component name;
    
    protected DefaultInformationDisplay(EntryIngredient entryStacks, Component name) {
        this.entryStacks = entryStacks;
        this.name = name;
        this.texts = Lists.newArrayList();
    }
    
    public static DefaultInformationDisplay createFromEntries(EntryIngredient entryStacks, Component name) {
        return new DefaultInformationDisplay(entryStacks, name);
    }
    
    public static DefaultInformationDisplay createFromEntry(EntryStack<?> entryStack, Component name) {
        return createFromEntries(EntryIngredient.of(entryStack), name);
    }
    
    @Override
    public List<EntryIngredient> getInputEntries() {
        return Collections.singletonList(entryStacks);
    }
    
    @Override
    public List<EntryIngredient> getOutputEntries() {
        return Collections.singletonList(entryStacks);
    }
    
    public DefaultInformationDisplay line(Component line) {
        texts.add(line);
        return this;
    }
    
    public DefaultInformationDisplay lines(Component... lines) {
        texts.addAll(Arrays.asList(lines));
        return this;
    }
    
    public DefaultInformationDisplay lines(Collection<Component> lines) {
        texts.addAll(lines);
        return this;
    }
    
    public EntryIngredient getEntryStacks() {
        return entryStacks;
    }
    
    public Component getName() {
        return name;
    }
    
    public List<Component> getTexts() {
        return texts;
    }
    
    @Override
    public CategoryIdentifier<?> getCategoryIdentifier() {
        return BuiltinPlugin.INFO;
    }
    
    @Override
    public Optional<Identifier> getDisplayLocation() {
        return Optional.empty();
    }
    
    @Override
    public DisplaySerializer<? extends Display> getSerializer() {
        return SERIALIZER;
    }
}
