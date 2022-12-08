/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020, 2021, 2022 shedaniel
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

package me.shedaniel.rei.impl.client.config.entries;

import com.mojang.blaze3d.vertex.PoseStack;
import me.shedaniel.clothconfig2.gui.widget.DynamicElementListWidget;
import me.shedaniel.rei.api.client.entry.filtering.FilteringRule;
import me.shedaniel.rei.api.client.entry.filtering.FilteringRuleType;
import me.shedaniel.rei.api.client.entry.filtering.FilteringRuleTypeRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

public class FilteringAddRuleScreen extends Screen {
    private final FilteringEntry entry;
    private RulesList rulesList;
    Screen parent;
    
    public FilteringAddRuleScreen(FilteringEntry entry) {
        super(Component.translatable("config.roughlyenoughitems.filteringRulesScreen.new"));
        this.entry = entry;
    }
    
    @Override
    public void init() {
        super.init();
        {
            Component backText = Component.literal("↩ ").append(Component.translatable("gui.back"));
            addRenderableWidget(new Button(4, 4, Minecraft.getInstance().font.width(backText) + 10, 20, backText, button -> {
                minecraft.setScreen(parent);
                this.parent = null;
            }, Supplier::get) {});
        }
        rulesList = addWidget(new RulesList(minecraft, width, height, 30, height, BACKGROUND_LOCATION));
        for (FilteringRuleType<?> rule : FilteringRuleTypeRegistry.getInstance()) {
            if (!rule.isSingular())
                rulesList.addItem(new DefaultRuleEntry(parent, entry, rule.createNew(), null));
        }
        rulesList.selectItem(rulesList.children().get(0));
    }
    
    @Override
    public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
        this.rulesList.render(matrices, mouseX, mouseY, delta);
        super.render(matrices, mouseX, mouseY, delta);
        this.font.drawShadow(matrices, this.title.getVisualOrderText(), this.width / 2.0F - this.font.width(this.title) / 2.0F, 12.0F, -1);
    }
    
    public static class RulesList extends DynamicElementListWidget<RuleEntry> {
        private boolean inFocus;
        
        public RulesList(Minecraft client, int width, int height, int top, int bottom, ResourceLocation backgroundLocation) {
            super(client, width, height, top, bottom, backgroundLocation);
        }
        
        @Override
        public boolean changeFocus(boolean lookForwards) {
            if (!this.inFocus && this.getItemCount() == 0) {
                return false;
            } else {
                this.inFocus = !this.inFocus;
                if (this.inFocus && this.getSelectedItem() == null && this.getItemCount() > 0) {
                    this.moveSelection(1);
                } else if (this.inFocus && this.getSelectedItem() != null) {
                    this.getSelectedItem();
                }
                
                return this.inFocus;
            }
        }
        
        @Override
        protected boolean isSelected(int index) {
            return Objects.equals(this.getSelectedItem(), this.children().get(index));
        }
        
        @Override
        protected int addItem(RuleEntry item) {
            return super.addItem(item);
        }
        
        @Override
        public int getItemWidth() {
            return width - 40;
        }
        
        @Override
        protected int getScrollbarPosition() {
            return width - 14;
        }
    }
    
    public static abstract class RuleEntry extends DynamicElementListWidget.ElementEntry<RuleEntry> {
        private final FilteringRule<?> rule;
        
        public RuleEntry(FilteringRule<?> rule) {
            this.rule = rule;
        }
        
        public FilteringRule<?> getRule() {
            return rule;
        }
        
        @Override
        public int getItemHeight() {
            return 26;
        }
        
        @Override
        public boolean changeFocus(boolean lookForwards) {
            return false;
        }
    }
    
    public static class DefaultRuleEntry extends RuleEntry {
        private final Button addButton;
        private final Function<Screen, Screen> screenFunction;
        
        public DefaultRuleEntry(Screen parent, FilteringEntry entry, FilteringRule<?> rule, Function<Screen, Screen> screenFunction) {
            super(rule);
            this.screenFunction = (screenFunction == null ? ((FilteringRuleType<FilteringRule<?>>) rule.getType()).createEntryScreen(rule) : screenFunction);
            addButton = new Button(0, 0, 20, 20, Component.nullToEmpty(" + "), button -> {
                entry.edited = true;
                Minecraft.getInstance().setScreen(this.screenFunction.apply(parent));
                entry.rules.add(0, rule);
            }, Supplier::get) {};
            addButton.active = this.screenFunction != null;
        }
        
        @Override
        public void render(PoseStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean isHovered, float delta) {
            Minecraft client = Minecraft.getInstance();
            {
                Component title = ((FilteringRuleType<FilteringRule<?>>) getRule().getType()).getTitle(getRule());
                int i = client.font.width(title);
                if (i > entryWidth - 28) {
                    FormattedText titleTrimmed = FormattedText.composite(client.font.substrByWidth(title, entryWidth - 28 - client.font.width("...")), FormattedText.of("..."));
                    client.font.drawShadow(matrices, Language.getInstance().getVisualOrder(titleTrimmed), x + 2, y + 1, 16777215);
                } else {
                    client.font.drawShadow(matrices, title.getVisualOrderText(), x + 2, y + 1, 16777215);
                }
            }
            {
                Component subtitle = ((FilteringRuleType<FilteringRule<?>>) getRule().getType()).getSubtitle(getRule());
                int i = client.font.width(subtitle);
                if (i > entryWidth - 28) {
                    FormattedText subtitleTrimmed = FormattedText.composite(client.font.substrByWidth(subtitle, entryWidth - 28 - client.font.width("...")), FormattedText.of("..."));
                    client.font.drawShadow(matrices, Language.getInstance().getVisualOrder(subtitleTrimmed), x + 2, y + 12, 8421504);
                } else {
                    client.font.drawShadow(matrices, subtitle.getVisualOrderText(), x + 2, y + 12, 8421504);
                }
            }
            addButton.setX(x + entryWidth - 25);
            addButton.setY(y + 1);
            addButton.render(matrices, mouseX, mouseY, delta);
        }
        
        @Override
        public List<? extends GuiEventListener> children() {
            return Collections.singletonList(addButton);
        }
        
        @Override
        public List<? extends NarratableEntry> narratables() {
            return Collections.singletonList(addButton);
        }
    }
}
