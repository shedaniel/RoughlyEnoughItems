/*
 * Copyright (c) 2018, 2019, 2020 shedaniel
 * Licensed under the MIT License (the "License").
 */

package me.shedaniel.rei.plugin.information;

import com.google.common.collect.Lists;
import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.api.RecipeDisplay;
import me.shedaniel.rei.plugin.DefaultPlugin;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

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
        return Collections.emptyList();
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
