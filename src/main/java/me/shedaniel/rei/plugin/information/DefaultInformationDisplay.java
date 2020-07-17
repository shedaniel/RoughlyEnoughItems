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
import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.api.RecipeDisplay;
import me.shedaniel.rei.plugin.DefaultPlugin;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Environment(EnvType.CLIENT)
public class DefaultInformationDisplay implements RecipeDisplay {
    private List<EntryStack> entryStacks;
    private List<Text> texts;
    private Text name;
    
    protected DefaultInformationDisplay(List<EntryStack> entryStacks, Text name) {
        this.entryStacks = entryStacks;
        this.name = name;
        this.texts = Lists.newArrayList();
    }
    
    public static DefaultInformationDisplay createFromEntries(List<EntryStack> entryStacks, Text name) {
        return new DefaultInformationDisplay(entryStacks, name);
    }
    
    public static DefaultInformationDisplay createFromEntry(EntryStack entryStack, Text name) {
        return createFromEntries(Collections.singletonList(entryStack), name);
    }
    
    @Override
    public List<List<EntryStack>> getInputEntries() {
        return Collections.singletonList(entryStacks);
    }
    
    @Override
    public List<EntryStack> getOutputEntries() {
        return entryStacks;
    }
    
    public DefaultInformationDisplay line(Text line) {
        texts.add(line);
        return this;
    }
    
    public DefaultInformationDisplay lines(Text... lines) {
        texts.addAll(Arrays.asList(lines));
        return this;
    }
    
    public DefaultInformationDisplay lines(Collection<Text> lines) {
        texts.addAll(lines);
        return this;
    }
    
    List<EntryStack> getEntryStacks() {
        return entryStacks;
    }
    
    Text getName() {
        return name;
    }
    
    List<Text> getTexts() {
        return texts;
    }
    
    @Override
    public Identifier getRecipeCategory() {
        return DefaultPlugin.INFO;
    }
}
