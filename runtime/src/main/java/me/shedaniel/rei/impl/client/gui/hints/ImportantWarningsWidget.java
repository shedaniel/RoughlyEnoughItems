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
import me.shedaniel.rei.RoughlyEnoughItemsCoreClient;
import me.shedaniel.rei.api.client.ClientHelper;
import me.shedaniel.rei.api.client.config.ConfigManager;
import me.shedaniel.rei.api.client.config.ConfigObject;
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
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
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
    private final Rectangle okayButtonBounds = new Rectangle();
    private final Rectangle doNotShowButtonBounds = new Rectangle();
    private List<Component> texts;
    
    public ImportantWarningsWidget() {
        if (((EntryRegistryImpl) EntryRegistry.getInstance()).listeners.add(LISTENER)) {
            String newId = Minecraft.getInstance().hasSingleplayerServer() ?
                    "integrated:" + Minecraft.getInstance().getSingleplayerServer().getWorldData().getLevelName()
                    : InstanceHelper.connectionFromClient() != null ? "server:" + InstanceHelper.connectionFromClient().getId()
                    : "null";
            if (!newId.equals(prevId)) {
                prevId = newId;
                dirty = true;
            }
            dirty = dirty && !ClientHelper.getInstance().canUseMovePackets();
        }
        
        this.visible = dirty && ConfigObject.getInstance().doesPartialRecipesWarning();
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
        graphics.pose().pushPose();
        graphics.pose().translate(0, 0, 900);
        graphics.fill(bounds.x - 5, bounds.y - 5, bounds.getMaxX() + 5, bounds.getMaxY() + 5, 0x90111111);
        int y = bounds.y;
        for (Component text : texts) {
            graphics.pose().pushPose();
            graphics.pose().translate(bounds.x, y, 0);
            graphics.pose().scale(0.5f, 0.5f, 1);
            graphics.drawWordWrap(Minecraft.getInstance().font, text, 0, 0, bounds.width * 2, -1);
            y += Minecraft.getInstance().font.wordWrapHeight(text, bounds.width * 2) / 2 + 5;
            graphics.pose().popPose();
        }

        MutableComponent doNotShowText = Component.translatable("text.rei.recipes.not.full.button.do_not_show_again").
                withStyle(ChatFormatting.RED);
        MutableComponent okayText = Component.translatable("text.rei.recipes.not.full.button.okay");
        int doNotShowTextWidth = Minecraft.getInstance().font.width(doNotShowText);
        int okayTextWidth = Minecraft.getInstance().font.width(okayText);
        int boundsMaxY = bounds.getMaxY();

        graphics.pose().pushPose();
        graphics.pose().translate(bounds.x, boundsMaxY - 9, 0);
        graphics.pose().scale(0.75f, 0.75f, 1);

        int textHeight = 10;
        int buttonSpacingGap = 6;
        int doNotShowButtonAlignCenter = (int) (bounds.x + bounds.width / 2.0 - Minecraft.getInstance().font.width(doNotShowText) * 0.75 / 2);
        int okayButtonAlignCenter = (int) (bounds.x + bounds.width / 2.0 - Minecraft.getInstance().font.width(okayText) * 0.75 / 2);

        this.doNotShowButtonBounds.setBounds(bounds.x, boundsMaxY - (textHeight * 2),
                bounds.getMaxX(), textHeight);
        this.okayButtonBounds.setBounds(bounds.x, boundsMaxY - textHeight + buttonSpacingGap,
                bounds.getMaxX(), textHeight - buttonSpacingGap);

        graphics.drawString(Minecraft.getInstance().font, doNotShowText,
                doNotShowButtonAlignCenter, -textHeight,
                doNotShowButtonBounds.contains(mouseX, mouseY) ? 0xfffff8de : 0xAAFFFFFF);
        graphics.drawString(Minecraft.getInstance().font, okayText,
                okayButtonAlignCenter, buttonSpacingGap,
                okayButtonBounds.contains(mouseX, mouseY) ? 0xfffff8de : 0xAAFFFFFF);
        graphics.pose().popPose();
        graphics.pose().popPose();
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if ((this.visible && button == 0) && (okayButtonBounds.contains(mouseX, mouseY) || doNotShowButtonBounds.contains(mouseX, mouseY))) {
            dirty = false;
            this.visible = false;
            if (doNotShowButtonBounds.contains(mouseX, mouseY)) {
                ConfigObject.getInstance().setDoesPartialRecipesWarning(false);
            }
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
