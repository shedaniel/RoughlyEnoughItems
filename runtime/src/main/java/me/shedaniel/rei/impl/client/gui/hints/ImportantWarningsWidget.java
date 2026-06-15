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

package me.shedaniel.rei.impl.client.gui.hints;

import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.ClientHelper;
import me.shedaniel.rei.api.client.gui.config.DisplayPanelLocation;
import me.shedaniel.rei.api.client.gui.widgets.WidgetWithBounds;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.registry.entry.EntryRegistry;
import me.shedaniel.rei.api.client.registry.screen.ScreenRegistry;
import me.shedaniel.rei.impl.common.entry.type.EntryRegistryImpl;
import me.shedaniel.rei.impl.common.entry.type.EntryRegistryListener;
import me.shedaniel.rei.impl.common.util.InstanceHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import me.shedaniel.rei.api.client.gui.compat.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.List;

public class ImportantWarningsWidget extends WidgetWithBounds {
    private static final EntryRegistryListener LISTENER = new EntryRegistryListener() {
    };
    private static String prevId = "";
    private static boolean dirty = false;
    private boolean visible;
    private final Rectangle bounds;
    private final Rectangle buttonBounds = new Rectangle();
    private List<Component> texts;
    
    public ImportantWarningsWidget() {
        if (((EntryRegistryImpl) EntryRegistry.getInstance()).listeners.add(LISTENER)) {
            String newId = Minecraft.getInstance().hasSingleplayerServer() ?
                    "integrated:" + Minecraft.getInstance().getSingleplayerServer().getWorldData().getLevelName()
                    : InstanceHelper.connectionFromClient() != null && InstanceHelper.connectionFromClient().getServerData() != null
                    ? "server:" + InstanceHelper.connectionFromClient().getServerData().ip
                    : "null";
            if (!newId.equals(prevId)) {
                prevId = newId;
                dirty = true;
            }
            dirty = dirty && !ClientHelper.getInstance().canUseMovePackets();
        }
        
        this.visible = dirty;
        this.texts = List.of(
                Component.translatable("text.rei.recipes.not.full.title").withStyle(ChatFormatting.RED),
                Component.translatable("text.rei.recipes.not.full.desc", Component.translatable("text.rei.recipes.not.full.desc.command").withStyle(ChatFormatting.AQUA, ChatFormatting.UNDERLINE)).withStyle(ChatFormatting.GRAY)
        );
        this.bounds = ScreenRegistry.getInstance().getOverlayBounds(DisplayPanelLocation.LEFT, Minecraft.getInstance().screen);
        this.bounds.setBounds(this.bounds.x + 10, this.bounds.y + 10, this.bounds.width - 20, this.bounds.height - 20);
        int heightRequired = -5;
        for (Component text : texts) {
            heightRequired += Minecraft.getInstance().font.wordWrapHeight(text, this.bounds.width * 2) / 2;
            heightRequired += 5;
        }
        this.bounds.height = Math.min(heightRequired + 20, this.bounds.height);
    }
    
    @Override
    public Rectangle getBounds() {
        return bounds;
    }
    
    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        if (!visible)
            return;
        graphics.pose().pushMatrix();
        graphics.fill(bounds.x - 5, bounds.y - 5, bounds.getMaxX() + 5, bounds.getMaxY() + 5, 0x90111111);
        int y = bounds.y;
        for (Component text : texts) {
            graphics.pose().pushMatrix();
            graphics.pose().translate(bounds.x, y);
            graphics.pose().scale(0.5f, 0.5f);
            graphics.drawWordWrap(Minecraft.getInstance().font, text, 0, 0, bounds.width * 2, -1);
            y += Minecraft.getInstance().font.wordWrapHeight(text, bounds.width * 2) / 2 + 5;
            graphics.pose().popMatrix();
        }
        
        MutableComponent okayText = Component.translatable("text.rei.recipes.not.full.button.okay");
        graphics.pose().pushMatrix();
        graphics.pose().translate(bounds.x + bounds.width / 2 - Minecraft.getInstance().font.width(okayText) * 0.75f / 2, bounds.getMaxY() - 9);
        graphics.pose().scale(0.75f, 0.75f);
        this.buttonBounds.setBounds(bounds.x, bounds.getMaxY() - 20, bounds.width, 20);
        graphics.drawString(Minecraft.getInstance().font, okayText, 0, 0,
                buttonBounds.contains(mouseX, mouseY) ? 0xfffff8de : 0xAAFFFFFF);
        graphics.pose().popMatrix();
        graphics.pose().popMatrix();
    }
    
    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (this.visible && event.button() == 0 && buttonBounds.contains(event.x(), event.y())) {
            dirty = false;
            this.visible = false;
            Widgets.produceClickSound();
            return true;
        }
        return false;
    }
    
    @Override
    public List<? extends GuiEventListener> children() {
        return List.of();
    }
}
