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
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
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

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public class FilteringCategoriesScreen extends Screen {
    private final FilteringCategoriesEntry entry;
    private ListWidget listWidget;
    Screen parent;
    
    public FilteringCategoriesScreen(FilteringCategoriesEntry entry) {
        super(Component.translatable("config.roughlyenoughitems.filtering.filteringQuickCraftCategories.configure.title"));
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
        listWidget = addWidget(new ListWidget(minecraft, width, height, 30, height, BACKGROUND_LOCATION));
        for (CategoryRegistry.CategoryConfiguration<?> configuration : CategoryRegistry.getInstance()) {
            listWidget.addItem(new DefaultListEntry(configuration));
        }
    }
    
    @Override
    public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
        this.listWidget.render(matrices, mouseX, mouseY, delta);
        super.render(matrices, mouseX, mouseY, delta);
        this.font.drawShadow(matrices, this.title.getVisualOrderText(), this.width / 2.0F - this.font.width(this.title) / 2.0F, 12.0F, -1);
    }
    
    private static class ListWidget extends DynamicElementListWidget<ListEntry> {
        private boolean inFocus;
        
        public ListWidget(Minecraft client, int width, int height, int top, int bottom, ResourceLocation backgroundLocation) {
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
            return 35;
        }
        
        @Override
        public boolean changeFocus(boolean lookForwards) {
            return false;
        }
    }
    
    private class DefaultListEntry extends ListEntry {
        private final Button toggleButton;
        private final CategoryRegistry.CategoryConfiguration<?> configuration;
        
        public DefaultListEntry(CategoryRegistry.CategoryConfiguration<?> configuration) {
            this.configuration = configuration;
            {
                Component toggleText = Component.translatable("config.roughlyenoughitems.filtering.filteringQuickCraftCategories.configure.toggle");
                toggleButton = new Button(0, 0, Minecraft.getInstance().font.width(toggleText) + 10, 20, toggleText, button -> {
                    boolean quickCraftingEnabledByDefault = configuration.isQuickCraftingEnabledByDefault();
                    boolean enabled = entry.getValue().getOrDefault(configuration.getCategoryIdentifier(), quickCraftingEnabledByDefault);
                    if (enabled) {
                        // set to false
                        if (!quickCraftingEnabledByDefault) {
                            entry.getValue().remove(configuration.getCategoryIdentifier());
                        } else {
                            entry.getValue().put(configuration.getCategoryIdentifier(), false);
                        }
                    } else {
                        // set to true
                        if (quickCraftingEnabledByDefault) {
                            entry.getValue().remove(configuration.getCategoryIdentifier());
                        } else {
                            entry.getValue().put(configuration.getCategoryIdentifier(), true);
                        }
                    }
                    
                    entry.edited = true;
                }, Supplier::get) {};
            }
        }
        
        @Override
        public void render(PoseStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean isHovered, float delta) {
            if (y + entryHeight < 0 || y > height) {
                return;
            }
            
            Minecraft client = Minecraft.getInstance();
            matrices.pushPose();
            matrices.translate(0, 0, 100);
            configuration.getCategory().getIcon().render(matrices, new Rectangle(x + 2, y + 5, 16, 16), mouseY, mouseY, delta);
            matrices.popPose();
            int xPos = x + 22;
            {
                Component title = configuration.getCategory().getTitle();
                int i = client.font.width(title);
                if (i > entryWidth - 28) {
                    FormattedText titleTrimmed = FormattedText.composite(client.font.substrByWidth(title, entryWidth - 28 - client.font.width("...")), FormattedText.of("..."));
                    client.font.drawShadow(matrices, Language.getInstance().getVisualOrder(titleTrimmed), x + 2, y + 1, 16777215);
                } else {
                    client.font.drawShadow(matrices, title.getVisualOrderText(), xPos, y + 1, 16777215);
                }
            }
            {
                Component subtitle = Component.translatable("config.roughlyenoughitems.filtering.filteringQuickCraftCategories.configure." + entry.getValue().getOrDefault(configuration.getCategoryIdentifier(), configuration.isQuickCraftingEnabledByDefault()))
                        .withStyle(ChatFormatting.GRAY);
                int i = client.font.width(subtitle);
                if (i > entryWidth - 28) {
                    FormattedText subtitleTrimmed = FormattedText.composite(client.font.substrByWidth(subtitle, entryWidth - 28 - client.font.width("...")), FormattedText.of("..."));
                    client.font.drawShadow(matrices, Language.getInstance().getVisualOrder(subtitleTrimmed), x + 2, y + 12, 8421504);
                } else {
                    client.font.drawShadow(matrices, subtitle.getVisualOrderText(), xPos, y + 12, 8421504);
                }
            }
            {
                Component id = Component.literal(configuration.getCategoryIdentifier().toString())
                        .withStyle(ChatFormatting.DARK_GRAY);
                int i = client.font.width(id);
                if (i > entryWidth - 28) {
                    FormattedText idTrimmed = FormattedText.composite(client.font.substrByWidth(id, entryWidth - 28 - client.font.width("...")), FormattedText.of("..."));
                    client.font.drawShadow(matrices, Language.getInstance().getVisualOrder(idTrimmed), x + 2, y + 22, 8421504);
                } else {
                    client.font.drawShadow(matrices, id.getVisualOrderText(), xPos, y + 22, 8421504);
                }
            }
            toggleButton.setX(x + entryWidth - 6 - toggleButton.getWidth());
            toggleButton.setY(y + 5);
            toggleButton.render(matrices, mouseX, mouseY, delta);
        }
        
        @Override
        public List<? extends GuiEventListener> children() {
            return Collections.singletonList(toggleButton);
        }
        
        @Override
        public List<? extends NarratableEntry> narratables() {
            return Collections.singletonList(toggleButton);
        }
    }
}
