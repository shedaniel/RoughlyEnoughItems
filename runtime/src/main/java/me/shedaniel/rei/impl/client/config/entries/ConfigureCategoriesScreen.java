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

import me.shedaniel.clothconfig2.gui.widget.DynamicElementListWidget;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.widgets.Label;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;

import java.util.*;
import java.util.function.Supplier;

public class ConfigureCategoriesScreen extends Screen {
    private final Map<CategoryIdentifier<?>, Boolean> filteringQuickCraftCategories;
    private final Set<CategoryIdentifier<?>> hiddenCategories;
    private final List<CategoryIdentifier<?>> categoryOrdering;
    private ListWidget listWidget;
    public Runnable editedSink = () -> {};
    public Screen parent;
    
    public ConfigureCategoriesScreen(Map<CategoryIdentifier<?>, Boolean> filteringQuickCraftCategories, Set<CategoryIdentifier<?>> hiddenCategories, List<CategoryIdentifier<?>> categoryOrdering) {
        super(Component.translatable("config.roughlyenoughitems.configureCategories.title"));
        this.filteringQuickCraftCategories = filteringQuickCraftCategories;
        this.hiddenCategories = hiddenCategories;
        this.categoryOrdering = categoryOrdering;
        for (CategoryRegistry.CategoryConfiguration<?> configuration : CategoryRegistry.getInstance()) {
            if (!this.categoryOrdering.contains(configuration.getCategoryIdentifier())) {
                this.categoryOrdering.add(configuration.getCategoryIdentifier());
            }
        }
    }
    
    public Map<CategoryIdentifier<?>, Boolean> getFilteringQuickCraftCategories() {
        return filteringQuickCraftCategories;
    }
    
    public Set<CategoryIdentifier<?>> getHiddenCategories() {
        return hiddenCategories;
    }
    
    public List<CategoryIdentifier<?>> getCategoryOrdering() {
        return categoryOrdering;
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
        listWidget = addWidget(new ListWidget(minecraft, width, height, 30, height, BACKGROUND_LOCATION));
        this.resetListEntries();
    }
    
    public void resetListEntries() {
        listWidget.children().clear();
        List<CategoryRegistry.CategoryConfiguration<?>> configurations = new ArrayList<>(CategoryRegistry.getInstance().stream().toList());
        configurations.sort(Comparator.comparingInt(o -> {
            int indexOf = categoryOrdering.indexOf(o.getCategoryIdentifier());
            return indexOf == -1 ? Integer.MAX_VALUE : indexOf;
        }));
        for (CategoryRegistry.CategoryConfiguration<?> configuration : configurations) {
            listWidget.addItem(new DefaultListEntry(configuration));
        }
    }
    
    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        this.listWidget.render(graphics, mouseX, mouseY, delta);
        super.render(graphics, mouseX, mouseY, delta);
        graphics.drawString(this.font, this.title.getVisualOrderText(), (int) (this.width / 2.0F - this.font.width(this.title) / 2.0F), 12, -1);
    }
    
    @Override
    public void onClose() {
        this.minecraft.setScreen(parent);
    }
    
    private static class ListWidget extends DynamicElementListWidget<ListEntry> {
        private boolean inFocus;
        
        public ListWidget(Minecraft client, int width, int height, int top, int bottom, ResourceLocation backgroundLocation) {
            super(client, width, height, top, bottom, backgroundLocation);
        }
        
        @Override
        protected boolean isSelected(int index) {
            return false;
        }
        
        @Override
        public ListEntry getSelectedItem() {
            return null;
        }
        
        @Override
        protected int addItem(ListEntry item) {
            return super.addItem(item);
        }
        
        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (super.mouseClicked(mouseX, mouseY, button))
                return true;
            ListEntry item = getItemAtPosition(mouseX, mouseY);
            if (item != null) {
                client.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
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
    
    private static abstract class ListEntry extends DynamicElementListWidget.ElementEntry<ListEntry> {
        @Override
        public int getItemHeight() {
            return 45;
        }
    }
    
    private class DefaultListEntry extends ListEntry {
        private final Label visibilityToggleButton, quickCraftToggleButton;
        private final Button upButton, downButton;
        private final CategoryRegistry.CategoryConfiguration<?> configuration;
        
        public DefaultListEntry(CategoryRegistry.CategoryConfiguration<?> configuration) {
            this.configuration = configuration;
            {
                Component toggleText = Component.translatable("config.roughlyenoughitems.filtering.filteringQuickCraftCategories.configure.toggle");
                visibilityToggleButton = Widgets.createClickableLabel(new Point(), toggleText, $ -> {
                    boolean enabled = !hiddenCategories.contains(configuration.getCategoryIdentifier());
                    if (enabled) {
                        // set to false
                        hiddenCategories.add(configuration.getCategoryIdentifier());
                    } else {
                        // set to true
                        hiddenCategories.remove(configuration.getCategoryIdentifier());
                    }
        
                    editedSink.run();
                }).leftAligned();
                quickCraftToggleButton = Widgets.createClickableLabel(new Point(), toggleText, $ -> {
                    boolean quickCraftingEnabledByDefault = configuration.isQuickCraftingEnabledByDefault();
                    boolean enabled = filteringQuickCraftCategories.getOrDefault(configuration.getCategoryIdentifier(), quickCraftingEnabledByDefault);
                    if (enabled) {
                        // set to false
                        if (!quickCraftingEnabledByDefault) {
                            filteringQuickCraftCategories.remove(configuration.getCategoryIdentifier());
                        } else {
                            filteringQuickCraftCategories.put(configuration.getCategoryIdentifier(), false);
                        }
                    } else {
                        // set to true
                        if (quickCraftingEnabledByDefault) {
                            filteringQuickCraftCategories.remove(configuration.getCategoryIdentifier());
                        } else {
                            filteringQuickCraftCategories.put(configuration.getCategoryIdentifier(), true);
                        }
                    }
                    
                    editedSink.run();
                }).leftAligned();
            }
            {
                this.upButton = new Button(0, 0, 20, 20, Component.literal("↑"), button -> {
                    int index = categoryOrdering.indexOf(configuration.getCategoryIdentifier());
                    if (index > 0) {
                        categoryOrdering.remove(index);
                        categoryOrdering.add(index - 1, configuration.getCategoryIdentifier());
                        editedSink.run();
                        resetListEntries();
                    }
                }, Supplier::get) {};
                this.downButton = new Button(0, 0, 20, 20, Component.literal("↓"), button -> {
                    int index = categoryOrdering.indexOf(configuration.getCategoryIdentifier());
                    if (index < categoryOrdering.size() - 1) {
                        categoryOrdering.remove(index);
                        categoryOrdering.add(index + 1, configuration.getCategoryIdentifier());
                        editedSink.run();
                        resetListEntries();
                    }
                }, Supplier::get) {};
                this.upButton.active = categoryOrdering.indexOf(configuration.getCategoryIdentifier()) > 0;
                this.downButton.active = categoryOrdering.indexOf(configuration.getCategoryIdentifier()) < categoryOrdering.size() - 1;
            }
        }
        
        @Override
        public void render(GuiGraphics graphics, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean isHovered, float delta) {
            if (y + entryHeight < 0 || y > height) {
                return;
            }
            
            Minecraft client = Minecraft.getInstance();
            graphics.pose().pushPose();
            graphics.pose().translate(0, 0, 100);
            configuration.getCategory().getIcon().render(graphics, new Rectangle(x + 2, y, 16, 16), mouseY, mouseY, delta);
            graphics.pose().popPose();
            int xPos = x + 22;
            {
                Component title = configuration.getCategory().getTitle();
                int i = client.font.width(title);
                if (i > entryWidth - 28) {
                    FormattedText titleTrimmed = FormattedText.composite(client.font.substrByWidth(title, entryWidth - 28 - client.font.width("...")), FormattedText.of("..."));
                    graphics.drawString(client.font, Language.getInstance().getVisualOrder(titleTrimmed), x + 2, y + 1, 16777215);
                } else {
                    graphics.drawString(client.font, title.getVisualOrderText(), xPos, y + 1, 16777215);
                }
            }
            {
                Component id = Component.literal(configuration.getCategoryIdentifier().toString())
                        .withStyle(ChatFormatting.DARK_GRAY);
                int i = client.font.width(id);
                if (i > entryWidth - 28) {
                    FormattedText idTrimmed = FormattedText.composite(client.font.substrByWidth(id, entryWidth - 28 - client.font.width("...")), FormattedText.of("..."));
                    graphics.drawString(client.font, Language.getInstance().getVisualOrder(idTrimmed), x + 2, y + 12, 8421504);
                } else {
                    graphics.drawString(client.font, id.getVisualOrderText(), xPos, y + 12, 8421504);
                }
            }
            boolean shown = !hiddenCategories.contains(configuration.getCategoryIdentifier());
            {
                Component subtitle = Component.translatable("config.roughlyenoughitems.configureCategories.visibility." + shown)
                        .withStyle(shown ? ChatFormatting.GREEN : ChatFormatting.RED);
                int i = client.font.drawShadow(matrices, subtitle.getVisualOrderText(), xPos, y + 22, 8421504);
                visibilityToggleButton.getPoint().setLocation(i + 3, y + 22);
                visibilityToggleButton.render(matrices, mouseX, mouseY, delta);
            }
            if (shown) {
                Component subtitle = Component.translatable("config.roughlyenoughitems.filtering.filteringQuickCraftCategories.configure." + filteringQuickCraftCategories.getOrDefault(configuration.getCategoryIdentifier(), configuration.isQuickCraftingEnabledByDefault()))
                        .withStyle(ChatFormatting.GRAY);
                int i = client.font.drawShadow(matrices, subtitle.getVisualOrderText(), xPos, y + 32, 8421504);
                quickCraftToggleButton.getPoint().setLocation(i + 3, y + 32);
                quickCraftToggleButton.render(matrices, mouseX, mouseY, delta);
            } else {
                quickCraftToggleButton.getPoint().setLocation(-12390, -12390);
            }
            upButton.setX(x + entryWidth - 20);
            upButton.setY(y + entryHeight / 2 - 21);
            upButton.render(matrices, mouseX, mouseY, delta);
            downButton.setX(x + entryWidth - 20);
            downButton.setY(y + entryHeight / 2 + 1);
            downButton.render(graphics, mouseX, mouseY, delta);
        }
        
        @Override
        public List<? extends GuiEventListener> children() {
            return List.of(visibilityToggleButton, quickCraftToggleButton, upButton, downButton);
        }
        
        @Override
        public List<? extends NarratableEntry> narratables() {
            return List.of();
        }
    }
}
