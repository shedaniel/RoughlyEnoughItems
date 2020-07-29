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

package me.shedaniel.rei.gui.config.entry;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.impl.filtering.FilteringRule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.TranslatableText;
import org.jetbrains.annotations.ApiStatus;

import java.util.*;
import java.util.function.Consumer;

@ApiStatus.Internal
public class FilteringEntry extends AbstractConfigListEntry<List<EntryStack>> {
    private int width;
    Consumer<List<EntryStack>> saveConsumer;
    Consumer<List<FilteringRule<?>>> rulesSaveConsumer;
    List<EntryStack> defaultValue;
    Set<EntryStack> configFiltered;
    List<FilteringRule<?>> rules;
    boolean edited = false;
    final FilteringScreen filteringScreen = new FilteringScreen(this);
    final FilteringRulesScreen filteringRulesScreen = new FilteringRulesScreen(this);
    private final AbstractButtonWidget buttonWidget = new ButtonWidget(0, 0, 0, 20, new TranslatableText("config.roughlyenoughitems.filteringScreen"), button -> {
        filteringRulesScreen.parent = MinecraftClient.getInstance().currentScreen;
        MinecraftClient.getInstance().openScreen(filteringRulesScreen);
    });
    private final List<Element> children = ImmutableList.of(buttonWidget);
    
    public FilteringEntry(int width, List<EntryStack> configFiltered, List<FilteringRule<?>> rules, List<EntryStack> defaultValue, Consumer<List<EntryStack>> saveConsumer, Consumer<List<FilteringRule<?>>> rulesSaveConsumer) {
        super(NarratorManager.EMPTY, false);
        this.width = width;
        this.configFiltered = new TreeSet<>(Comparator.comparing(EntryStack::hashIgnoreAmount));
        this.configFiltered.addAll(configFiltered);
        this.rules = Lists.newArrayList(rules);
        this.defaultValue = defaultValue;
        this.saveConsumer = saveConsumer;
        this.rulesSaveConsumer = rulesSaveConsumer;
    }
    
    @Override
    public List<EntryStack> getValue() {
        return Lists.newArrayList(configFiltered);
    }
    
    @Override
    public Optional<List<EntryStack>> getDefaultValue() {
        return Optional.ofNullable(defaultValue);
    }
    
    @Override
    public void save() {
        saveConsumer.accept(getValue());
        rulesSaveConsumer.accept(rules);
        this.edited = false;
    }
    
    @Override
    public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean isSelected, float delta) {
        super.render(matrices, index, y, x, entryWidth, entryHeight, mouseX, mouseY, isSelected, delta);
        Window window = MinecraftClient.getInstance().getWindow();
        this.buttonWidget.active = this.isEditable();
        this.buttonWidget.y = y;
        this.buttonWidget.x = x + entryWidth / 2 - width / 2;
        this.buttonWidget.setWidth(width);
        this.buttonWidget.render(matrices, mouseX, mouseY, delta);
    }
    
    @Override
    public List<? extends Element> children() {
        return children;
    }
    
    @Override
    public boolean isEdited() {
        return super.isEdited() || edited;
    }
}
