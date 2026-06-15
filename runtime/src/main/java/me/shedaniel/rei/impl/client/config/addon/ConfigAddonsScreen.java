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

package me.shedaniel.rei.impl.client.config.addon;

import me.shedaniel.rei.api.client.config.addon.ConfigAddon;
import me.shedaniel.rei.api.client.config.addon.ConfigAddonRegistry;
import me.shedaniel.rei.impl.client.gui.InternalTextures;
import me.shedaniel.rei.impl.client.gui.widget.UpdatedListWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ComponentPath;
import me.shedaniel.rei.api.client.gui.compat.GuiGraphics;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public class ConfigAddonsScreen extends me.shedaniel.rei.impl.client.gui.screen.REIScreen {
    private AddonsList rulesList;
    private final Screen parent;
    
    public ConfigAddonsScreen(Screen parent) {
        super(Component.translatable("text.rei.addons"));
        this.parent = parent;
    }
    
    @Override
    public void init() {
        super.init();
        {
            Component backText = Component.literal("↩ ").append(Component.translatable("gui.back"));
            addRenderableWidget(Button.builder(backText, button -> {
                minecraft.setScreen(parent);
            }).bounds(4, 4, Minecraft.getInstance().font.width(backText) + 10, 20).build());
        }
        rulesList = addWidget(new AddonsList(minecraft, width, height, 30, height));
        ConfigAddonRegistryImpl addonRegistry = (ConfigAddonRegistryImpl) ConfigAddonRegistry.getInstance();
        for (ConfigAddon addon : addonRegistry.getAddons()) {
            rulesList.addItem(new DefaultAddonEntry(parent, addon));
        }
    }
    
    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        this.rulesList.render(graphics, mouseX, mouseY, delta);
        super.render(graphics, mouseX, mouseY, delta);
        graphics.drawString(this.font, this.title.getVisualOrderText(), (int) (this.width / 2.0F - this.font.width(this.title) / 2.0F), 12, -1);
    }
    
    public static class AddonsList extends UpdatedListWidget<AddonEntry> {
        private boolean inFocus;
        
        public AddonsList(Minecraft client, int width, int height, int top, int bottom) {
            super(client, width, height, top, bottom);
        }
        
        @Override
        protected boolean isSelected(int index) {
            return false;
        }
        
        @Override
        protected int addItem(AddonEntry item) {
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
    
    public static abstract class AddonEntry extends UpdatedListWidget.ElementEntry<AddonEntry> {
        @Override
        public int getItemHeight() {
            return 26;
        }
        
        @Override
        @Nullable
        public ComponentPath nextFocusPath(FocusNavigationEvent focusNavigationEvent) {
            return null;
        }
    }
    
    public static class DefaultAddonEntry extends AddonEntry {
        private final Button configureButton;
        private final ConfigAddon addon;
        
        public DefaultAddonEntry(Screen parent, ConfigAddon addon) {
            this.addon = addon;
            this.configureButton = new Button.Plain(0, 0, 20, 20, Component.nullToEmpty(null), button -> {
                Minecraft.getInstance().setScreen(this.addon.createScreen(Minecraft.getInstance().screen));
            }, Supplier::get) {
                @Override
                public void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
                    graphics.blit(RenderPipelines.GUI_TEXTURED, InternalTextures.CHEST_GUI_TEXTURE, getX() + 3, getY() + 3, 0, 0, 14, 14, 256, 256);
                }
            };
        }
        
        @Override
        public void render(GuiGraphics graphics, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean isHovered, float delta) {
            Minecraft client = Minecraft.getInstance();
            {
                Component title = addon.getName();
                int i = client.font.width(title);
                if (i > entryWidth - 28) {
                    FormattedText titleTrimmed = FormattedText.composite(client.font.substrByWidth(title, entryWidth - 28 - client.font.width("...")), FormattedText.of("..."));
                    graphics.drawString(client.font, Language.getInstance().getVisualOrder(titleTrimmed), x + 2, y + 1, 0xFFFFFFFF);
                } else {
                    graphics.drawString(client.font, title.getVisualOrderText(), x + 2, y + 1, 0xFFFFFFFF);
                }
            }
            {
                Component subtitle = addon.getDescription();
                int i = client.font.width(subtitle);
                if (i > entryWidth - 28) {
                    FormattedText subtitleTrimmed = FormattedText.composite(client.font.substrByWidth(subtitle, entryWidth - 28 - client.font.width("...")), FormattedText.of("..."));
                    graphics.drawString(client.font, Language.getInstance().getVisualOrder(subtitleTrimmed), x + 2, y + 12, 0xFF808080);
                } else {
                    graphics.drawString(client.font, subtitle.getVisualOrderText(), x + 2, y + 12, 0xFF808080);
                }
            }
            configureButton.setX(x + entryWidth - 25);
            configureButton.setY(y + 1);
            configureButton.extractRenderState(graphics, mouseX, mouseY, delta);
        }
        
        @Override
        public List<? extends GuiEventListener> children() {
            return Collections.singletonList(configureButton);
        }
        
        @Override
        public List<? extends NarratableEntry> narratables() {
            return Collections.singletonList(configureButton);
        }
    }
}
