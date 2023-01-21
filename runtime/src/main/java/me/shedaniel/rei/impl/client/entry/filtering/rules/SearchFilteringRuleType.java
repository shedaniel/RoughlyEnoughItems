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

package me.shedaniel.rei.impl.client.entry.filtering.rules;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.entry.filtering.FilteringRule;
import me.shedaniel.rei.api.client.entry.filtering.FilteringRuleType;
import me.shedaniel.rei.api.client.registry.entry.EntryRegistry;
import me.shedaniel.rei.api.client.search.SearchFilter;
import me.shedaniel.rei.api.client.search.SearchProvider;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.impl.client.config.entries.FilteringRuleOptionsScreen;
import me.shedaniel.rei.impl.client.gui.widget.BatchedEntryRendererManager;
import me.shedaniel.rei.impl.client.gui.widget.EntryWidget;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static me.shedaniel.rei.impl.client.gui.widget.entrylist.EntryListWidget.entrySize;

public enum SearchFilteringRuleType implements FilteringRuleType<SearchFilteringRule> {
    INSTANCE;
    
    @Override
    public CompoundTag saveTo(SearchFilteringRule rule, CompoundTag tag) {
        tag.putString("filter", rule.filterStr);
        tag.putBoolean("show", rule.show);
        return tag;
    }
    
    @Override
    public SearchFilteringRule readFrom(CompoundTag tag) {
        String filter = tag.getString("filter");
        boolean show = tag.getBoolean("show");
        return new SearchFilteringRule(filter, show);
    }
    
    @Override
    public SearchFilteringRule createNew() {
        return new SearchFilteringRule("", false);
    }
    
    @Override
    public Component getTitle(SearchFilteringRule rule) {
        return Component.translatable("rule.roughlyenoughitems.filtering.search");
    }
    
    @Override
    public Component getSubtitle(SearchFilteringRule rule) {
        return Component.translatable("rule.roughlyenoughitems.filtering.search.subtitle");
    }
    
    @Override
    @Nullable
    public Function<Screen, Screen> createEntryScreen(SearchFilteringRule rule) {
        return screen -> new FilteringRuleOptionsScreen<>(rule, screen) {
            TextFieldRuleEntry entry = null;
            BooleanRuleEntry show = null;
            List<EntryWidget> entryStacks = new ArrayList<>();
            
            @Override
            public void addEntries(Consumer<RuleEntry> entryConsumer) {
                addEmpty(entryConsumer, 10);
                addText(entryConsumer, Component.translatable("rule.roughlyenoughitems.filtering.search.filter").withStyle(ChatFormatting.GRAY));
                entryConsumer.accept(entry = new TextFieldRuleEntry(width - 36, rule, widget -> {
                    widget.setMaxLength(9999);
                    widget.setResponder(searchTerm -> {
                        SearchFilter filter = SearchProvider.getInstance().createFilter(searchTerm);
                        entryStacks = EntryRegistry.getInstance().getEntryStacks().parallel()
                                .filter(filter)
                                .map(EntryStack::normalize)
                                .map(stack -> new EntryWidget(new Rectangle(0, 0, 18, 18)).noBackground().entry(stack))
                                .collect(Collectors.toList());
                    });
                    if (entry != null) widget.setValue(entry.getWidget().getValue());
                    else widget.setValue(rule.filterStr);
                }));
                addEmpty(entryConsumer, 10);
                addText(entryConsumer, Component.translatable("rule.roughlyenoughitems.filtering.search.show").withStyle(ChatFormatting.GRAY));
                Function<Boolean, Component> function = bool -> {
                    return Component.translatable("rule.roughlyenoughitems.filtering.search.show." + bool);
                };
                entryConsumer.accept(show = new BooleanRuleEntry(width - 36, show == null ? rule.show : show.getBoolean(), rule, function));
                addEmpty(entryConsumer, 10);
                entryConsumer.accept(new SubRulesEntry(rule, () -> function.apply(show == null ? rule.show : show.getBoolean()),
                        Collections.singletonList(new EntryStacksRuleEntry(rule, () -> entryStacks))));
            }
            
            @Override
            public void save() {
                rule.setFilter(entry.getWidget().getValue());
                rule.show = show.getBoolean();
            }
        };
    }
    
    @Override
    public boolean isSingular() {
        return false;
    }
    
    public static class EntryStacksRuleEntry extends FilteringRuleOptionsScreen.RuleEntry {
        private final Supplier<Iterable<EntryWidget>> entryStacks;
        private int totalHeight;
        
        public EntryStacksRuleEntry(FilteringRule<?> rule, Supplier<Iterable<EntryWidget>> entryStacks) {
            super(rule);
            this.entryStacks = entryStacks;
        }
        
        @Override
        public void render(PoseStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean isHovered, float delta) {
            BatchedEntryRendererManager manager = new BatchedEntryRendererManager();
            int entrySize = entrySize();
            int width = entryWidth / entrySize;
            int i = 0;
            for (EntryWidget stack : entryStacks.get()) {
                stack.getBounds().setLocation(x + (i % width) * entrySize, y + (i / width) * entrySize);
                if (stack.getBounds().getMaxY() >= 0 && stack.getBounds().getY() <= Minecraft.getInstance().getWindow().getGuiScaledHeight()) {
                    manager.add(stack);
                }
                i++;
            }
            manager.render(matrices, mouseX, mouseY, delta);
            totalHeight = (i / width + 1) * entrySize;
        }
        
        @Override
        public int getItemHeight() {
            return totalHeight;
        }
        
        @Override
        public List<? extends NarratableEntry> narratables() {
            return Lists.newArrayList();
        }
        
        @Override
        public List<? extends GuiEventListener> children() {
            return Lists.newArrayList();
        }
    }
}
