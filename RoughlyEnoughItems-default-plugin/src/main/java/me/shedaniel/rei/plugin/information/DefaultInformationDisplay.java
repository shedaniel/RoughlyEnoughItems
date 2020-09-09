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
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class DefaultInformationDisplay implements RecipeDisplay {
    private List<EntryStack> entryStacks;
    private List<ITextComponent> texts;
    private ITextComponent name;
    
    protected DefaultInformationDisplay(List<EntryStack> entryStacks, ITextComponent name) {
        this.entryStacks = entryStacks;
        this.name = name;
        this.texts = Lists.newArrayList();
    }
    
    public static DefaultInformationDisplay createFromEntries(List<EntryStack> entryStacks, ITextComponent name) {
        return new DefaultInformationDisplay(entryStacks, name);
    }
    
    public static DefaultInformationDisplay createFromEntry(EntryStack entryStack, ITextComponent name) {
        return createFromEntries(Collections.singletonList(entryStack), name);
    }
    
    @Override
    public @NotNull List<List<EntryStack>> getInputEntries() {
        return Collections.singletonList(entryStacks);
    }
    
    @Override
    public @NotNull List<List<EntryStack>> getResultingEntries() {
        return Collections.singletonList(entryStacks);
    }
    
    public DefaultInformationDisplay line(ITextComponent line) {
        texts.add(line);
        return this;
    }
    
    public DefaultInformationDisplay lines(ITextComponent... lines) {
        texts.addAll(Arrays.asList(lines));
        return this;
    }
    
    public DefaultInformationDisplay lines(Collection<ITextComponent> lines) {
        texts.addAll(lines);
        return this;
    }
    
    List<EntryStack> getEntryStacks() {
        return entryStacks;
    }
    
    ITextComponent getName() {
        return name;
    }
    
    List<ITextComponent> getTexts() {
        return texts;
    }
    
    @Override
    public @NotNull ResourceLocation getRecipeCategory() {
        return DefaultPlugin.INFO;
    }
}
