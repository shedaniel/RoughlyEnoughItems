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

package me.shedaniel.rei.impl.client.gui.performance.entry;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import me.shedaniel.clothconfig2.api.Expandable;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.impl.client.gui.performance.PerformanceScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

@Environment(EnvType.CLIENT)
public class SubCategoryListEntry extends PerformanceScreen.PerformanceEntry implements Expandable {
    private static final ResourceLocation CONFIG_TEX = new ResourceLocation("cloth-config2", "textures/gui/cloth_config.png");
    private final List<PerformanceScreen.PerformanceEntry> entries;
    private final CategoryLabelWidget widget;
    private final List<GuiEventListener> children;
    private final Component name;
    private final long totalTime;
    private boolean expanded;
    
    public SubCategoryListEntry(Component name, List<PerformanceScreen.PerformanceEntry> entries, long totalTime, boolean defaultExpanded) {
        this.name = name;
        this.entries = entries;
        this.totalTime = totalTime;
        this.expanded = defaultExpanded;
        this.widget = new CategoryLabelWidget();
        this.children = Lists.newArrayList(new GuiEventListener[]{this.widget});
        this.children.addAll(entries);
    }
    
    @Override
    public boolean isExpanded() {
        return this.expanded;
    }
    
    @Override
    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }
    
    @Override
    public void render(PoseStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean isHovered, float delta) {
        RenderSystem.setShaderTexture(0, CONFIG_TEX);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        this.widget.rectangle.x = x + 3;
        this.widget.rectangle.y = y;
        this.widget.rectangle.width = entryWidth - 6;
        this.widget.rectangle.height = 24;
        this.blit(matrices, x + 3, y + 5, 24, (this.widget.rectangle.contains(mouseX, mouseY) ? 18 : 0) + (this.expanded ? 9 : 0), 9, 9);
        Minecraft.getInstance().font.drawShadow(matrices, this.name.getVisualOrderText(), (float) x + 3 + 15, (float) (y + 6), this.widget.rectangle.contains(mouseX, mouseY) ? -1638890 : -1);
        
        for (PerformanceScreen.PerformanceEntry performanceEntry : this.entries) {
            performanceEntry.setParent(this.getParent());
        }
        
        if (this.expanded) {
            int yy = y + 24;
            
            PerformanceScreen.PerformanceEntry entry;
            for (Iterator<PerformanceScreen.PerformanceEntry> iterator = this.entries.iterator(); iterator.hasNext(); yy += entry.getItemHeight()) {
                entry = iterator.next();
                entry.render(matrices, -1, yy, x + 3 + 15, entryWidth - 15 - 3, entry.getItemHeight(), mouseX, mouseY, isHovered && this.getFocused() == entry, delta);
            }
        }
        FormattedCharSequence timeText = PerformanceScreen.formatTime(totalTime, true);
        Minecraft.getInstance().font.drawShadow(matrices, timeText, (float) x + entryWidth - 6 - 4 - Minecraft.getInstance().font.width(timeText), (float) (y + 6), -1);
    }
    
    @Override
    public int getMorePossibleHeight() {
        if (!this.expanded) {
            return -1;
        } else {
            List<Integer> list = new ArrayList();
            int i = 24;
            
            for (PerformanceScreen.PerformanceEntry entry : this.entries) {
                i += entry.getItemHeight();
                if (entry.getMorePossibleHeight() >= 0) {
                    list.add(i + entry.getMorePossibleHeight());
                }
            }
            
            list.add(i);
            return list.stream().max(Integer::compare).orElse(0) - this.getItemHeight();
        }
    }
    
    public Rectangle getEntryArea(int x, int y, int entryWidth, int entryHeight) {
        this.widget.rectangle.x = x;
        this.widget.rectangle.y = y;
        this.widget.rectangle.width = entryWidth;
        this.widget.rectangle.height = 24;
        return new Rectangle(this.getParent().left, y, this.getParent().right - this.getParent().left, 20);
    }
    
    @Override
    public int getItemHeight() {
        if (!this.expanded) {
            return 24;
        } else {
            int i = 24;
            
            PerformanceScreen.PerformanceEntry entry;
            for (Iterator<PerformanceScreen.PerformanceEntry> iterator = this.entries.iterator(); iterator.hasNext(); i += entry.getItemHeight()) {
                entry = iterator.next();
            }
            
            return i;
        }
    }
    
    @Override
    public List<? extends GuiEventListener> children() {
        return this.expanded ? this.children : Collections.singletonList(this.widget);
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
                SubCategoryListEntry.this.expanded = !SubCategoryListEntry.this.expanded;
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                return true;
            } else {
                return false;
            }
        }
    }
}
