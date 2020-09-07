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

import com.mojang.blaze3d.matrix.MatrixStack;
import me.shedaniel.clothconfig2.forge.gui.widget.DynamicElementListWidget;
import me.shedaniel.rei.impl.filtering.FilteringRule;
import me.shedaniel.rei.impl.filtering.rules.ManualFilteringRule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.util.text.LanguageMap;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.ITextProperties;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;

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
        super(new TranslationTextComponent("config.roughlyenoughitems.filteringRulesScreen"));
        this.entry = entry;
    }
    
    @Override
    protected void init() {
        super.init();
        {
            ITextComponent backText = new StringTextComponent("↩ ").append(new TranslationTextComponent("gui.back"));
            addButton(new Button(4, 4, Minecraft.getInstance().font.width(backText) + 10, 20, backText, button -> {
                minecraft.setScreen(parent);
                this.parent = null;
            }));
        }
        {
            ITextComponent addText = new StringTextComponent(" + ");
            addButton(new Button(width - 4 - 20, 4, 20, 20, addText, button -> {
                FilteringAddRuleScreen screen = new FilteringAddRuleScreen(entry);
                screen.parent = this;
                minecraft.setScreen(screen);
            }));
        }
        rulesList = addWidget(new RulesList(minecraft, width, height, 30, height, BACKGROUND_LOCATION));
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
        public boolean mouseClicked(double double_1, double double_2, int int_1) {
            if (super.mouseClicked(double_1, double_2, int_1))
                return true;
            RuleEntry item = getItemAtPosition(double_1, double_2);
            if (item != null) {
                client.getSoundManager().play(SimpleSound.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
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
        private final Button configureButton;
        private final Button deleteButton;
        private final BiFunction<FilteringEntry, Screen, Screen> screenFunction;
        
        public DefaultRuleEntry(FilteringRule<?> rule, FilteringEntry entry, BiFunction<FilteringEntry, Screen, Screen> screenFunction) {
            super(rule);
            this.screenFunction = (screenFunction == null ? rule.createEntryScreen().orElse(null) : screenFunction);
            configureButton = new Button(0, 0, 20, 20, ITextComponent.nullToEmpty(null), button -> {
                entry.edited = true;
                Minecraft.getInstance().setScreen(this.screenFunction.apply(entry, Minecraft.getInstance().screen));
            }) {
                @Override
                protected void renderBg(MatrixStack matrices, Minecraft client, int mouseX, int mouseY) {
                    super.renderBg(matrices, client, mouseX, mouseY);
                    Minecraft.getInstance().getTextureManager().bind(CHEST_GUI_TEXTURE);
                    blit(matrices, x + 3, y + 3, 0, 0, 14, 14);
                }
            };
            {
                ITextComponent deleteText = new TranslationTextComponent("config.roughlyenoughitems.filteringRulesScreen.delete");
                deleteButton = new Button(0, 0, Minecraft.getInstance().font.width(deleteText) + 10, 20, deleteText, button -> {
                    final Screen screen = Minecraft.getInstance().screen;
                    entry.edited = true;
                    entry.rules.remove(rule);
                    screen.init(Minecraft.getInstance(), screen.width, screen.height);
                });
            }
            configureButton.active = this.screenFunction != null;
            deleteButton.active = !(rule instanceof ManualFilteringRule);
        }
        
        @Override
        public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean isHovered, float delta) {
            Minecraft client = Minecraft.getInstance();
            {
                ITextComponent title = getRule().getTitle();
                int i = client.font.width(title);
                if (i > entryWidth - 28) {
                    ITextProperties titleTrimmed = ITextProperties.composite(client.font.substrByWidth(title, entryWidth - 28 - client.font.width("...")), ITextProperties.of("..."));
                    client.font.drawShadow(matrices, LanguageMap.getInstance().getVisualOrder(titleTrimmed), x + 2, y + 1, 16777215);
                } else {
                    client.font.drawShadow(matrices, title.getVisualOrderText(), x + 2, y + 1, 16777215);
                }
            }
            {
                ITextComponent subtitle = getRule().getSubtitle();
                int i = client.font.width(subtitle);
                if (i > entryWidth - 28) {
                    ITextProperties subtitleTrimmed = ITextProperties.composite(client.font.substrByWidth(subtitle, entryWidth - 28 - client.font.width("...")), ITextProperties.of("..."));
                    client.font.drawShadow(matrices, LanguageMap.getInstance().getVisualOrder(subtitleTrimmed), x + 2, y + 12, 8421504);
                } else {
                    client.font.drawShadow(matrices, subtitle.getVisualOrderText(), x + 2, y + 12, 8421504);
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
        public List<? extends IGuiEventListener> children() {
            return Arrays.asList(configureButton, deleteButton);
        }
    }
}
