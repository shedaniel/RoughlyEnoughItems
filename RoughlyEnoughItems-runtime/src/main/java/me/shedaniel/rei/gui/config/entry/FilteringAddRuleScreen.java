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

import me.shedaniel.clothconfig2.gui.widget.DynamicElementListWidget;
import me.shedaniel.rei.impl.filtering.FilteringRule;
import me.shedaniel.rei.impl.filtering.rules.ManualFilteringRule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.Language;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;

public class FilteringAddRuleScreen extends Screen {
    private final FilteringEntry entry;
    private RulesList rulesList;
    Screen parent;
    
    public FilteringAddRuleScreen(FilteringEntry entry) {
        super(new TranslatableText("config.roughlyenoughitems.filteringRulesScreen.new"));
        this.entry = entry;
    }
    
    @Override
    protected void init() {
        super.init();
        {
            Text backText = new LiteralText("↩ ").append(new TranslatableText("gui.back"));
            addButton(new ButtonWidget(4, 4, MinecraftClient.getInstance().textRenderer.getWidth(backText) + 10, 20, backText, button -> {
                client.openScreen(parent);
                this.parent = null;
            }));
        }
        rulesList = addChild(new RulesList(client, width, height, 30, height, BACKGROUND_TEXTURE));
        for (FilteringRule<?> rule : FilteringRule.REGISTRY) {
            if (!(rule instanceof ManualFilteringRule))
                rulesList.addItem(new DefaultRuleEntry(parent, entry, rule.createNew(), null));
        }
        rulesList.selectItem(rulesList.children().get(0));
    }
    
    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.rulesList.render(matrices, mouseX, mouseY, delta);
        super.render(matrices, mouseX, mouseY, delta);
        this.textRenderer.drawWithShadow(matrices, this.title.asOrderedText(), this.width / 2.0F - this.textRenderer.getWidth(this.title) / 2.0F, 12.0F, -1);
    }
    
    public static class RulesList extends DynamicElementListWidget<RuleEntry> {
        private boolean inFocus;
        
        public RulesList(MinecraftClient client, int width, int height, int top, int bottom, Identifier backgroundLocation) {
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
        private final ButtonWidget addButton;
        private final BiFunction<FilteringEntry, Screen, Screen> screenFunction;
        
        public DefaultRuleEntry(Screen parent, FilteringEntry entry, FilteringRule<?> rule, BiFunction<FilteringEntry, Screen, Screen> screenFunction) {
            super(rule);
            this.screenFunction = (screenFunction == null ? rule.createEntryScreen().orElse(null) : screenFunction);
            addButton = new ButtonWidget(0, 0, 20, 20, Text.of(" + "), button -> {
                entry.edited = true;
                MinecraftClient.getInstance().openScreen(this.screenFunction.apply(entry, parent));
                entry.rules.add(0, rule);
            });
            addButton.active = this.screenFunction != null;
        }
        
        @Override
        public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean isHovered, float delta) {
            MinecraftClient client = MinecraftClient.getInstance();
            {
                Text title = getRule().getTitle();
                int i = client.textRenderer.getWidth(title);
                if (i > entryWidth - 28) {
                    StringVisitable titleTrimmed = StringVisitable.concat(client.textRenderer.trimToWidth(title, entryWidth - 28 - client.textRenderer.getStringWidth("...")), StringVisitable.plain("..."));
                    client.textRenderer.drawWithShadow(matrices, Language.getInstance().reorder(titleTrimmed), x + 2, y + 1, 16777215);
                } else {
                    client.textRenderer.drawWithShadow(matrices, title.asOrderedText(), x + 2, y + 1, 16777215);
                }
            }
            {
                Text subtitle = getRule().getSubtitle();
                int i = client.textRenderer.getWidth(subtitle);
                if (i > entryWidth - 28) {
                    StringVisitable subtitleTrimmed = StringVisitable.concat(client.textRenderer.trimToWidth(subtitle, entryWidth - 28 - client.textRenderer.getStringWidth("...")), StringVisitable.plain("..."));
                    client.textRenderer.drawWithShadow(matrices, Language.getInstance().reorder(subtitleTrimmed), x + 2, y + 12, 8421504);
                } else {
                    client.textRenderer.drawWithShadow(matrices, subtitle.asOrderedText(), x + 2, y + 12, 8421504);
                }
            }
            addButton.x = x + entryWidth - 25;
            addButton.y = y + 1;
            addButton.render(matrices, mouseX, mouseY, delta);
        }
        
        @Override
        public List<? extends Element> children() {
            return Collections.singletonList(addButton);
        }
    }
}
