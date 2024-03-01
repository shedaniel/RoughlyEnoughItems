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

package me.shedaniel.rei.impl.client.config.entries;

import com.mojang.blaze3d.systems.RenderSystem;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.entry.filtering.FilteringRule;
import me.shedaniel.rei.impl.client.gui.widget.UpdatedListWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class FilteringRuleOptionsScreen<T extends FilteringRule<?>> extends Screen {
    private RulesList rulesList;
    Screen parent;
    public T rule;
    
    public FilteringRuleOptionsScreen(T rule, Screen screen) {
        super(Component.translatable("config.roughlyenoughitems.filteringRulesScreen"));
        this.rule = rule;
        this.parent = screen;
    }
    
    @Override
    public void init() {
        super.init();
        if (rulesList != null) save();
        {
            Component doneText = Component.translatable("gui.done");
            int width = Minecraft.getInstance().font.width(doneText);
            addRenderableWidget(new Button(this.width - 4 - width - 10, 4, width + 10, 20, doneText, button -> {
                save();
                minecraft.setScreen(parent);
            }, Supplier::get) {
            });
        }
        rulesList = addWidget(new RulesList(minecraft, width, height, 30, height));
        addEntries(ruleEntry -> rulesList.addItem(ruleEntry));
    }
    
    public abstract void addEntries(Consumer<RuleEntry> entryConsumer);
    
    public abstract void save();
    
    public void addText(Consumer<RuleEntry> entryConsumer, FormattedText text) {
        for (FormattedCharSequence s : font.split(text, width - 80)) {
            entryConsumer.accept(new TextRuleEntry(rule, s));
        }
    }
    
    public void addEmpty(Consumer<RuleEntry> entryConsumer, int height) {
        entryConsumer.accept(new EmptyRuleEntry(rule, height));
    }
    
    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        super.render(graphics, mouseX, mouseY, delta);
        this.rulesList.render(graphics, mouseX, mouseY, delta);
        graphics.drawString(this.font, this.title.getVisualOrderText(), (int) (this.width / 2.0F - this.font.width(this.title) / 2.0F), 12, -1);
    }
    
    public static class RulesList extends UpdatedListWidget<RuleEntry> {
        public RulesList(Minecraft client, int width, int height, int top, int bottom) {
            super(client, width, height, top, bottom);
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
    
    public static abstract class RuleEntry extends UpdatedListWidget.ElementEntry<RuleEntry> {
        private final FilteringRule<?> rule;
        
        public RuleEntry(FilteringRule<?> rule) {
            this.rule = rule;
        }
        
        public FilteringRule<?> getRule() {
            return rule;
        }
    }
    
    public static class TextRuleEntry extends RuleEntry {
        private final FormattedCharSequence text;
        
        public TextRuleEntry(FilteringRule<?> rule, FormattedCharSequence text) {
            super(rule);
            this.text = text;
        }
        
        @Override
        public void render(GuiGraphics graphics, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean isHovered, float delta) {
            graphics.drawString(Minecraft.getInstance().font, text, x + 5, y, -1);
        }
        
        @Override
        public int getItemHeight() {
            return 12;
        }
        
        @Override
        public List<? extends GuiEventListener> children() {
            return Collections.emptyList();
        }
        
        @Override
        public List<? extends NarratableEntry> narratables() {
            return Collections.emptyList();
        }
    }
    
    public static class EmptyRuleEntry extends RuleEntry {
        private final int height;
        
        public EmptyRuleEntry(FilteringRule<?> rule, int height) {
            super(rule);
            this.height = height;
        }
        
        @Override
        public void render(GuiGraphics graphics, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean isHovered, float delta) {
        }
        
        @Override
        public int getItemHeight() {
            return height;
        }
        
        @Override
        public List<? extends GuiEventListener> children() {
            return Collections.emptyList();
        }
        
        @Override
        public List<? extends NarratableEntry> narratables() {
            return Collections.emptyList();
        }
    }
    
    public static class TextFieldRuleEntry extends RuleEntry {
        private final EditBox widget;
        
        public TextFieldRuleEntry(int width, FilteringRule<?> rule, Consumer<EditBox> widgetConsumer) {
            super(rule);
            this.widget = new EditBox(Minecraft.getInstance().font, 0, 0, width, 18, Component.nullToEmpty(""));
            widgetConsumer.accept(widget);
        }
        
        @Override
        public void render(GuiGraphics graphics, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean isHovered, float delta) {
            widget.setX(x + 2);
            widget.setY(y + 2);
            widget.render(graphics, mouseX, mouseY, delta);
        }
        
        @Override
        public int getItemHeight() {
            return 20;
        }
        
        public EditBox getWidget() {
            return widget;
        }
        
        @Override
        public List<? extends GuiEventListener> children() {
            return Collections.singletonList(widget);
        }
        
        @Override
        public List<? extends NarratableEntry> narratables() {
            return Collections.singletonList(widget);
        }
    }
    
    public static class BooleanRuleEntry extends RuleEntry {
        private boolean b;
        private final Button widget;
        
        public BooleanRuleEntry(int width, boolean b, FilteringRule<?> rule, Function<Boolean, Component> textFunction) {
            super(rule);
            this.b = b;
            this.widget = new Button(0, 0, 100, 20, textFunction.apply(b), button -> {
                this.b = !this.b;
                button.setMessage(textFunction.apply(this.b));
            }, Supplier::get) {
            };
        }
        
        public boolean getBoolean() {
            return b;
        }
        
        @Override
        public void render(GuiGraphics graphics, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean isHovered, float delta) {
            widget.setX(x + 2);
            widget.setY(y);
            widget.render(graphics, mouseX, mouseY, delta);
        }
        
        @Override
        public int getItemHeight() {
            return 20;
        }
        
        @Override
        public List<? extends GuiEventListener> children() {
            return Collections.singletonList(widget);
        }
        
        @Override
        public List<? extends NarratableEntry> narratables() {
            return Collections.singletonList(widget);
        }
    }
    
    public static class SubRulesEntry extends RuleEntry {
        private static final ResourceLocation CONFIG_TEX = new ResourceLocation("cloth-config2", "textures/gui/cloth_config.png");
        private final CategoryLabelWidget widget;
        private final List<RuleEntry> rules;
        private final List<GuiEventListener> children;
        private boolean expanded;
        private Supplier<Component> name;
        
        public SubRulesEntry(FilteringRule<?> rule, Supplier<Component> name, List<RuleEntry> rules) {
            super(rule);
            this.rules = rules;
            this.widget = new CategoryLabelWidget();
            this.name = name;
            this.expanded = true;
            this.children = new ArrayList<>(rules);
            this.children.add(widget);
        }
        
        public List<RuleEntry> getRules() {
            return rules;
        }
        
        @Override
        public void render(GuiGraphics graphics, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean isHovered, float delta) {
            RenderSystem.setShaderTexture(0, CONFIG_TEX);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            this.widget.rectangle.x = x + 3;
            this.widget.rectangle.y = y;
            this.widget.rectangle.width = entryWidth - 6;
            this.widget.rectangle.height = 24;
            graphics.blit(CONFIG_TEX, x + 3, y + 5, 24, (this.widget.rectangle.contains(mouseX, mouseY) ? 18 : 0) + (this.expanded ? 9 : 0), 9, 9);
            graphics.drawString(Minecraft.getInstance().font, this.name.get().getVisualOrderText(), x + 3 + 15, y + 6, this.widget.rectangle.contains(mouseX, mouseY) ? -1638890 : -1);
            
            for (RuleEntry performanceEntry : this.rules) {
                performanceEntry.setParent(this.getParent());
            }
            
            if (this.expanded) {
                int yy = y + 24;
                
                RuleEntry entry;
                for (Iterator<RuleEntry> iterator = this.rules.iterator(); iterator.hasNext(); yy += entry.getItemHeight()) {
                    entry = iterator.next();
                    entry.render(graphics, -1, yy, x + 3 + 15, entryWidth - 15 - 3, entry.getItemHeight(), mouseX, mouseY, isHovered && this.getFocused() == entry, delta);
                }
            }
        }
        
        @Override
        public int getMorePossibleHeight() {
            if (!this.expanded) {
                return -1;
            } else {
                List<Integer> list = new ArrayList<>();
                int i = 24;
                
                for (RuleEntry entry : this.rules) {
                    i += entry.getItemHeight();
                    if (entry.getMorePossibleHeight() >= 0) {
                        list.add(i + entry.getMorePossibleHeight());
                    }
                }
                
                list.add(i);
                return list.stream().max(Integer::compare).orElse(0) - this.getItemHeight();
            }
        }
        
        @Override
        public int getItemHeight() {
            if (!this.expanded) {
                return 24;
            } else {
                int i = 24;
                
                RuleEntry entry;
                for (Iterator<RuleEntry> iterator = this.rules.iterator(); iterator.hasNext(); i += entry.getItemHeight()) {
                    entry = iterator.next();
                }
                
                return i;
            }
        }
        
        @Override
        public List<? extends GuiEventListener> children() {
            return children;
        }
        
        @Override
        public List<? extends NarratableEntry> narratables() {
            return Collections.emptyList();
        }
        
        public class CategoryLabelWidget implements GuiEventListener {
            private final Rectangle rectangle = new Rectangle();
            
            public CategoryLabelWidget() {
            }
            
            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                if (this.rectangle.contains(mouseX, mouseY)) {
                    SubRulesEntry.this.expanded = !SubRulesEntry.this.expanded;
                    Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                    return true;
                } else {
                    return false;
                }
            }
            
            @Nullable
            @Override
            public ComponentPath nextFocusPath(FocusNavigationEvent event) {
                return null;
            }
            
            @Override
            public void setFocused(boolean bl) {
            }
            
            @Override
            public boolean isFocused() {
                return false;
            }
        }
    }
}
