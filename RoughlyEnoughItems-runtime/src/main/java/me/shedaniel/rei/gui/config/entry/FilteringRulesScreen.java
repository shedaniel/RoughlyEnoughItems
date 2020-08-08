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
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.Language;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;

import static me.shedaniel.rei.gui.RecipeViewingScreen.CHEST_GUI_TEXTURE;

public class FilteringRulesScreen extends Screen {
    private final FilteringEntry entry;
    private RulesList rulesList;
    Screen parent;
    
    public FilteringRulesScreen(FilteringEntry entry) {
        super(new TranslatableText("config.roughlyenoughitems.filteringRulesScreen"));
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
        {
            Text addText = new LiteralText(" + ");
            addButton(new ButtonWidget(width - 4 - 20, 4, 20, 20, addText, button -> {
                FilteringAddRuleScreen screen = new FilteringAddRuleScreen(entry);
                screen.parent = this;
                client.openScreen(screen);
            }));
        }
        rulesList = addChild(new RulesList(client, width, height, 30, height, BACKGROUND_TEXTURE));
        for (int i = entry.rules.size() - 1; i >= 0; i--) {
            FilteringRule<?> rule = entry.rules.get(i);
            if (rule instanceof ManualFilteringRule)
                rulesList.addItem(new DefaultRuleEntry(rule, entry, (entry, screen) -> {
                    entry.filteringScreen.parent = screen;
                    return entry.filteringScreen;
                }));
            else rulesList.addItem(new DefaultRuleEntry(rule, entry, null));
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
        public boolean mouseClicked(double double_1, double double_2, int int_1) {
            if (super.mouseClicked(double_1, double_2, int_1))
                return true;
            RuleEntry item = getItemAtPosition(double_1, double_2);
            if (item != null) {
                client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                selectItem(item);
                this.setFocused(item);
                this.setDragging(true);
                return true;
            }
            return false;
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
        private final ButtonWidget configureButton;
        private final ButtonWidget deleteButton;
        private final BiFunction<FilteringEntry, Screen, Screen> screenFunction;
        
        public DefaultRuleEntry(FilteringRule<?> rule, FilteringEntry entry, BiFunction<FilteringEntry, Screen, Screen> screenFunction) {
            super(rule);
            this.screenFunction = (screenFunction == null ? rule.createEntryScreen().orElse(null) : screenFunction);
            configureButton = new ButtonWidget(0, 0, 20, 20, Text.of(null), button -> {
                entry.edited = true;
                MinecraftClient.getInstance().openScreen(this.screenFunction.apply(entry, MinecraftClient.getInstance().currentScreen));
            }) {
                @Override
                protected void renderBg(MatrixStack matrices, MinecraftClient client, int mouseX, int mouseY) {
                    super.renderBg(matrices, client, mouseX, mouseY);
                    MinecraftClient.getInstance().getTextureManager().bindTexture(CHEST_GUI_TEXTURE);
                    drawTexture(matrices, x + 3, y + 3, 0, 0, 14, 14);
                }
            };
            {
                Text deleteText = new TranslatableText("config.roughlyenoughitems.filteringRulesScreen.delete");
                deleteButton = new ButtonWidget(0, 0, MinecraftClient.getInstance().textRenderer.getWidth(deleteText) + 10, 20, deleteText, button -> {
                    final Screen screen = MinecraftClient.getInstance().currentScreen;
                    entry.edited = true;
                    entry.rules.remove(rule);
                    screen.init(MinecraftClient.getInstance(), screen.width, screen.height);
                });
            }
            configureButton.active = this.screenFunction != null;
            deleteButton.active = !(rule instanceof ManualFilteringRule);
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
            configureButton.x = x + entryWidth - 25;
            configureButton.y = y + 1;
            configureButton.render(matrices, mouseX, mouseY, delta);
            deleteButton.x = x + entryWidth - 27 - deleteButton.getWidth();
            deleteButton.y = y + 1;
            deleteButton.render(matrices, mouseX, mouseY, delta);
        }
        
        @Override
        public List<? extends Element> children() {
            return Arrays.asList(configureButton, deleteButton);
        }
    }
}
