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

package me.shedaniel.rei.plugin.information;

import com.google.common.collect.Lists;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.ingredient.EntryIngredient;
import me.shedaniel.rei.api.common.ingredient.EntryStack;
import me.shedaniel.rei.plugin.common.BuiltinPlugin;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Environment(EnvType.CLIENT)
public class DefaultInformationDisplay implements Display {
    private EntryIngredient entryStacks;
    private List<Component> texts;
    private Component name;
    
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
    
    List<? extends EntryStack<?>> getEntryStacks() {
        return entryStacks;
    }
    
    Component getName() {
        return name;
    }
    
    List<Component> getTexts() {
        return texts;
    }
    
    @Override
    public CategoryIdentifier<?> getCategoryIdentifier() {
        return BuiltinPlugin.INFO;
    }
}
