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

package me.shedaniel.rei.impl.client.gui.widget;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import me.shedaniel.rei.api.client.gui.AbstractContainerEventHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import me.shedaniel.rei.api.client.gui.compat.GuiGraphics;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Environment(EnvType.CLIENT)
public abstract class DynamicErrorFreeEntryListWidget<E extends DynamicErrorFreeEntryListWidget.Entry<E>> extends AbstractContainerEventHandler implements Renderable, NarratableEntry {
    protected static final int DRAG_OUTSIDE = -2;
    protected final Minecraft client;
    private final List<E> entries = new Entries();
    public int width;
    public int height;
    public int top;
    public int bottom;
    public int right;
    public int left;
    protected boolean verticallyCenter = true;
    protected int yDrag = -2;
    protected boolean selectionVisible = true;
    protected boolean renderSelection;
    protected int headerHeight;
    protected double scroll;
    protected boolean scrolling;
    @Nullable
    protected E hoveredItem;
    protected E selectedItem;
    protected Identifier backgroundLocation;
    
    public DynamicErrorFreeEntryListWidget(Minecraft client, int width, int height, int top, int bottom, Identifier backgroundLocation) {
        this.client = client;
        this.width = width;
        this.height = height;
        this.top = top;
        this.bottom = bottom;
        this.left = 0;
        this.right = width;
        this.backgroundLocation = backgroundLocation;
    }
    
    public void setRenderSelection(boolean boolean_1) {
        this.selectionVisible = boolean_1;
    }
    
    protected void setRenderHeader(boolean boolean_1, int headerHeight) {
        this.renderSelection = boolean_1;
        this.headerHeight = headerHeight;
        if (!boolean_1)
            this.headerHeight = 0;
    }
    
    public NarrationPriority narrationPriority() {
        if (this.isFocused()) {
            return NarrationPriority.FOCUSED;
        } else {
            return this.hoveredItem != null ? NarrationPriority.HOVERED : NarrationPriority.NONE;
        }
    }
    
    public void updateNarration(NarrationElementOutput narrationElementOutput) {
        E entry = this.hoveredItem;
        if (entry != null) {
            entry.updateNarration(narrationElementOutput.nest());
            this.narrateListElementPosition(narrationElementOutput, entry);
        } else {
            E entry2 = this.getFocused();
            if (entry2 != null) {
                entry2.updateNarration(narrationElementOutput.nest());
                this.narrateListElementPosition(narrationElementOutput, entry2);
            }
        }
        
        narrationElementOutput.add(NarratedElementType.USAGE, Component.translatable("narration.component_list.usage"));
    }
    
    protected void narrateListElementPosition(NarrationElementOutput narrationElementOutput, E entry) {
        List<E> list = this.children();
        if (list.size() > 1) {
            int i = list.indexOf(entry);
            if (i != -1) {
                narrationElementOutput.add(NarratedElementType.POSITION, Component.translatable("narrator.position.list", i + 1, list.size()));
            }
        }
        
    }
    
    public int getItemWidth() {
        return 220;
    }
    
    public E getSelectedItem() {
        return this.selectedItem;
    }
    
    public void selectItem(E item) {
        this.selectedItem = item;
    }
    
    public E getFocused() {
        return (E) super.getFocused();
    }
    
    public final List<E> children() {
        return this.entries;
    }
    
    protected final void clearItems() {
        this.entries.clear();
    }
    
    protected E getItem(int index) {
        return this.children().get(index);
    }
    
    protected int addItem(E item) {
        this.entries.add(item);
        return this.entries.size() - 1;
    }
    
    protected int getItemCount() {
        return this.children().size();
    }
    
    protected boolean isSelected(int index) {
        return Objects.equals(this.getSelectedItem(), this.children().get(index));
    }
    
    protected final E getItemAtPosition(double mouseX, double mouseY) {
        int listMiddleX = this.left + this.width / 2;
        int minX = listMiddleX - this.getItemWidth() / 2;
        int maxX = listMiddleX + this.getItemWidth() / 2;
        int currentY = Mth.floor(mouseY - (double) this.top) - this.headerHeight + (int) this.getScroll() - 4;
        int itemY = 0;
        int itemIndex = -1;
        for (int i = 0; i < entries.size(); i++) {
            E item = getItem(i);
            itemY += item.getItemHeight();
            if (itemY > currentY) {
                itemIndex = i;
                break;
            }
        }
        return mouseX < (double) this.getScrollbarPosition() && mouseX >= minX && mouseX <= maxX && itemIndex >= 0 && currentY >= 0 && itemIndex < this.getItemCount() ? this.children().get(itemIndex) : null;
    }
    
    public void updateSize(int width, int height, int top, int bottom) {
        this.width = width;
        this.height = height;
        this.top = top;
        this.bottom = bottom;
        this.left = 0;
        this.right = width;
    }
    
    public void setLeftPos(int left) {
        this.left = left;
        this.right = left + this.width;
    }
    
    protected int getMaxScrollPosition() {
        List<Integer> list = new ArrayList<>();
        int i = headerHeight;
        for (E entry : entries) {
            i += entry.getItemHeight();
            if (entry.getMorePossibleHeight() >= 0) {
                list.add(i + entry.getMorePossibleHeight());
            }
        }
        list.add(i);
        return list.stream().max(Integer::compare).orElse(0);
    }
    
    protected void clickedHeader(int int_1, int int_2) {
    }
    
    public static void renderBackBackground(GuiGraphics graphics, Identifier backgroundLocation,
                                            int left, int top, int right, int bottom, int yOffset, int color) {
        /*graphics.drawSpecial(source -> {
            VertexConsumer buffer = source.getBuffer(RenderType.guiTextured(backgroundLocation));
            Matrix4f matrix = graphics.pose().last().pose();
            buffer.addVertex(matrix, left, bottom, 0.0F).setUv(left / 32.0F, ((bottom + yOffset) / 32.0F)).setColor(color, color, color, 255);
            buffer.addVertex(matrix, right, bottom, 0.0F).setUv(right / 32.0F, ((bottom + yOffset) / 32.0F)).setColor(color, color, color, 255);
            buffer.addVertex(matrix, right, top, 0.0F).setUv(right / 32.0F, ((top + yOffset) / 32.0F)).setColor(color, color, color, 255);
            buffer.addVertex(matrix, left, top, 0.0F).setUv(left / 32.0F, ((top + yOffset) / 32.0F)).setColor(color, color, color, 255);
        });*/
    }
    
    protected void drawBackground() {
    }
    
    protected void renderDecorations(GuiGraphics graphics, int mouseX, int mouseY) {
    }
    
    protected void renderHeader(GuiGraphics graphics, int rowLeft, int startY) {
    }
    
    @Deprecated
    protected void renderBackBackground(GuiGraphics graphics) {
        DynamicErrorFreeEntryListWidget.renderBackBackground(graphics, backgroundLocation,
                left, top, right, bottom, (int) getScroll(), 32);
    }
    
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        this.drawBackground();
        int scrollbarPosition = this.getScrollbarPosition();
        int int_4 = scrollbarPosition + 6;
        renderBackBackground(graphics);
        int rowLeft = this.getRowLeft();
        int startY = this.top + 4 - (int) this.getScroll();
        if (this.renderSelection)
            this.renderHeader(graphics, rowLeft, startY);
        this.renderList(graphics, rowLeft, startY, mouseX, mouseY, delta);
        this.renderHoleBackground(graphics, 0, this.top, 255, 255);
        this.renderHoleBackground(graphics, this.bottom, this.height, 255, 255);
        graphics.blit(RenderPipelines.GUI_TEXTURED, Screen.HEADER_SEPARATOR, this.left, this.top - 2, 0.0F, 0.0F, this.width, 2, 32, 2);
        graphics.blit(RenderPipelines.GUI_TEXTURED, Screen.FOOTER_SEPARATOR, this.left, this.bottom, 0.0F, 0.0F, this.width, 2, 32, 2);
        int maxScroll = this.getMaxScroll();
        this.renderScrollBar(graphics, maxScroll, scrollbarPosition, int_4);
        this.renderDecorations(graphics, mouseX, mouseY);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        render(GuiGraphics.of(graphics), mouseX, mouseY, delta);
    }
    
    protected void renderScrollBar(GuiGraphics graphics, int maxScroll, int scrollbarPositionMinX, int scrollbarPositionMaxX) {
        if (maxScroll > 0) {
            int height = ((this.bottom - this.top) * (this.bottom - this.top)) / this.getMaxScrollPosition();
            height = Mth.clamp(height, 32, this.bottom - this.top - 8);
            int y = (int) this.getScroll() * (this.bottom - this.top - height) / maxScroll + this.top;
            if (y < this.top) {
                y = this.top;
            }
            
            int finalY = y;
            int finalHeight = height;
            /*graphics.drawSpecial(source -> {
                VertexConsumer buffer = source.getBuffer(RenderType.gui());
                Matrix4f matrix = graphics.pose().last().pose();
                buffer.addVertex(matrix, scrollbarPositionMinX, this.bottom, 0.0F).setColor(0, 0, 0, 255);
                buffer.addVertex(matrix, scrollbarPositionMaxX, this.bottom, 0.0F).setColor(0, 0, 0, 255);
                buffer.addVertex(matrix, scrollbarPositionMaxX, this.top, 0.0F).setColor(0, 0, 0, 255);
                buffer.addVertex(matrix, scrollbarPositionMinX, this.top, 0.0F).setColor(0, 0, 0, 255);
                buffer.addVertex(matrix, scrollbarPositionMinX, finalY + finalHeight, 0.0F).setColor(128, 128, 128, 255);
                buffer.addVertex(matrix, scrollbarPositionMaxX, finalY + finalHeight, 0.0F).setColor(128, 128, 128, 255);
                buffer.addVertex(matrix, scrollbarPositionMaxX, finalY, 0.0F).setColor(128, 128, 128, 255);
                buffer.addVertex(matrix, scrollbarPositionMinX, finalY, 0.0F).setColor(128, 128, 128, 255);
                buffer.addVertex(scrollbarPositionMinX, (finalY + finalHeight - 1), 0.0F).setColor(192, 192, 192, 255);
                buffer.addVertex((scrollbarPositionMaxX - 1), (finalY + finalHeight - 1), 0.0F).setColor(192, 192, 192, 255);
                buffer.addVertex((scrollbarPositionMaxX - 1), finalY, 0.0F).setColor(192, 192, 192, 255);
                buffer.addVertex(scrollbarPositionMinX, finalY, 0.0F).setColor(192, 192, 192, 255);
            });*/
        }
    }
    
    protected void centerScrollOn(E item) {
        double d = (this.bottom - this.top) / -2d;
        for (int i = 0; i < this.children().indexOf(item) && i < this.getItemCount(); i++)
            d += getItem(i).getItemHeight();
        this.capYPosition(d);
    }
    
    protected void ensureVisible(E item) {
        int rowTop = this.getRowTop(this.children().indexOf(item));
        int int_2 = rowTop - this.top - 4 - item.getItemHeight();
        if (int_2 < 0)
            this.scroll(int_2);
        int int_3 = this.bottom - rowTop - item.getItemHeight() * 2;
        if (int_3 < 0)
            this.scroll(-int_3);
    }
    
    protected void scroll(int int_1) {
        this.capYPosition(this.getScroll() + (double) int_1);
        this.yDrag = -2;
    }
    
    public double getScroll() {
        return this.scroll;
    }
    
    public void capYPosition(double double_1) {
        this.scroll = Mth.clamp(double_1, 0.0F, this.getMaxScroll());
    }
    
    protected int getMaxScroll() {
        return Math.max(0, this.getMaxScrollPosition() - (this.bottom - this.top - 4));
    }
    
    public int getScrollBottom() {
        return (int) this.getScroll() - this.height - this.headerHeight;
    }
    
    protected void updateScrollingState(double double_1, double double_2, int int_1) {
        this.scrolling = int_1 == 0 && double_1 >= (double) this.getScrollbarPosition() && double_1 < (double) (this.getScrollbarPosition() + 6);
    }
    
    protected int getScrollbarPosition() {
        return this.width / 2 + 124;
    }
    
    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        this.updateScrollingState(event.x(), event.y(), event.button());
        if (!this.isMouseOver(event.x(), event.y())) {
            return false;
        } else {
            E item = this.getItemAtPosition(event.x(), event.y());
            if (item != null) {
                if (item.mouseClicked(event, doubleClick)) {
                    this.setFocused(item);
                    this.setDragging(true);
                    return true;
                }
            } else if (event.button() == 0) {
                this.clickedHeader((int) (event.x() - (double) (this.left + this.width / 2 - this.getItemWidth() / 2)), (int) (event.y() - (double) this.top) + (int) this.getScroll() - 4);
                return true;
            }
            
            return this.scrolling;
        }
    }
    
    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (this.getFocused() != null) {
            this.getFocused().mouseReleased(event);
        }
        
        return false;
    }
    
    @Override
    public boolean mouseDragged(MouseButtonEvent event, double deltaX, double deltaY) {
        if (super.mouseDragged(event, deltaX, deltaY)) {
            return true;
        } else if (event.button() == 0 && this.scrolling) {
            if (event.y() < (double) this.top) {
                this.capYPosition(0.0F);
            } else if (event.y() > (double) this.bottom) {
                this.capYPosition(this.getMaxScroll());
            } else {
                double double_5 = Math.max(1, this.getMaxScroll());
                int int_2 = this.bottom - this.top;
                int int_3 = Mth.clamp((int) ((float) (int_2 * int_2) / (float) this.getMaxScrollPosition()), 32, int_2 - 8);
                double double_6 = Math.max(1.0D, double_5 / (double) (int_2 - int_3));
                this.capYPosition(this.getScroll() + deltaY * double_6);
            }
            
            return true;
        } else {
            return false;
        }
    }
    
    @Override
    public boolean mouseScrolled(double double_1, double double_2, double amountX, double amountY) {
        for (E entry : entries) {
            if (entry.mouseScrolled(double_1, double_2, amountX, amountY)) {
                return true;
            }
        }
        if (amountY == 0) return false;
        this.capYPosition(this.getScroll() - amountY * (double) (getMaxScroll() / getItemCount()) / 2.0D);
        return true;
    }
    
    @Override
    public boolean keyPressed(KeyEvent event) {
        if (super.keyPressed(event)) {
            return true;
        } else if (event.key() == 264) {
            this.moveSelection(1);
            return true;
        } else if (event.key() == 265) {
            this.moveSelection(-1);
            return true;
        } else {
            return false;
        }
    }
    
    protected void moveSelection(int int_1) {
        if (!this.children().isEmpty()) {
            int int_2 = this.children().indexOf(this.getSelectedItem());
            int int_3 = Mth.clamp(int_2 + int_1, 0, this.getItemCount() - 1);
            E itemListWidget$Item_1 = this.children().get(int_3);
            this.selectItem(itemListWidget$Item_1);
            this.ensureVisible(itemListWidget$Item_1);
        }
        
    }
    
    @Override
    public boolean isMouseOver(double double_1, double double_2) {
        return double_2 >= (double) this.top && double_2 <= (double) this.bottom && double_1 >= (double) this.left && double_1 <= (double) this.right;
    }
    
    protected void renderList(GuiGraphics graphics, int startX, int startY, int int_3, int int_4, float float_1) {
        this.hoveredItem = this.isMouseOver(int_3, int_4) ? this.getItemAtPosition(int_3, int_4) : null;
        int itemCount = this.getItemCount();
        Tesselator tessellator = Tesselator.getInstance();
        
        for (int renderIndex = 0; renderIndex < itemCount; ++renderIndex) {
            E item = this.getItem(renderIndex);
            int itemY = startY + headerHeight;
            for (int i = 0; i < entries.size() && i < renderIndex; i++)
                itemY += entries.get(i).getItemHeight();
            int itemHeight = item.getItemHeight() - 4;
            int itemWidth = this.getItemWidth();
            int itemMinX, itemMaxX;
            if (this.selectionVisible && this.isSelected(renderIndex)) {
                itemMinX = this.left + this.width / 2 - itemWidth / 2;
                itemMaxX = itemMinX + itemWidth;
                float float_2 = this.isFocused() ? 1.0F : 0.5F;
                /*Matrix4f matrix = graphics.pose().last().pose();
                
                int finalItemY = itemY;
                graphics.drawSpecial(source -> {
                    VertexConsumer buffer = source.getBuffer(RenderType.gui());
                    buffer.addVertex(matrix, itemMinX, finalItemY + itemHeight + 2, 0.0F).setColor(float_2, float_2, float_2, 1.0F);
                    buffer.addVertex(matrix, itemMaxX, finalItemY + itemHeight + 2, 0.0F).setColor(float_2, float_2, float_2, 1.0F);
                    buffer.addVertex(matrix, itemMaxX, finalItemY - 2, 0.0F).setColor(float_2, float_2, float_2, 1.0F);
                    buffer.addVertex(matrix, itemMinX, finalItemY - 2, 0.0F).setColor(float_2, float_2, float_2, 1.0F);
                    buffer.addVertex(matrix, itemMinX + 1, finalItemY + itemHeight + 1, 0.0F).setColor(0.0F, 0.0F, 0.0F, 1.0F);
                    buffer.addVertex(matrix, itemMaxX - 1, finalItemY + itemHeight + 1, 0.0F).setColor(0.0F, 0.0F, 0.0F, 1.0F);
                    buffer.addVertex(matrix, itemMaxX - 1, finalItemY - 1, 0.0F).setColor(0.0F, 0.0F, 0.0F, 1.0F);
                    buffer.addVertex(matrix, itemMinX + 1, finalItemY - 1, 0.0F).setColor(0.0F, 0.0F, 0.0F, 1.0F);
                });*/
            }
            
            int y = this.getRowTop(renderIndex);
            int x = this.getRowLeft();
            renderItem(graphics, item, renderIndex, y, x, itemWidth, itemHeight, int_3, int_4, Objects.equals(hoveredItem, item), float_1);
        }
        
    }
    
    protected void renderItem(GuiGraphics graphics, E item, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean isSelected, float delta) {
        item.render(graphics, index, y, x, entryWidth, entryHeight, mouseX, mouseY, isSelected, delta);
    }
    
    protected int getRowLeft() {
        return this.left + this.width / 2 - this.getItemWidth() / 2 + 2;
    }
    
    protected int getRowTop(int index) {
        int integer = top + 4 - (int) this.getScroll() + headerHeight;
        for (int i = 0; i < entries.size() && i < index; i++)
            integer += entries.get(i).getItemHeight();
        return integer;
    }
    
    public boolean isFocused() {
        return false;
    }
    
    protected void renderHoleBackground(GuiGraphics graphics, int y1, int y2, int alpha1, int alpha2) {
        /*Matrix4f matrix = graphics.pose().last().pose();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        graphics.drawSpecial(source -> {
            VertexConsumer buffer = source.getBuffer(RenderType.guiTextured(backgroundLocation));
            buffer.addVertex(matrix, this.left, y2, 0.0F).setUv(0, ((float) y2 / 32.0F)).setColor(64, 64, 64, alpha2);
            buffer.addVertex(matrix, this.left + this.width, y2, 0.0F).setUv(((float) this.width / 32.0F), ((float) y2 / 32.0F)).setColor(64, 64, 64, alpha2);
            buffer.addVertex(matrix, this.left + this.width, y1, 0.0F).setUv(((float) this.width / 32.0F), ((float) y1 / 32.0F)).setColor(64, 64, 64, alpha1);
            buffer.addVertex(matrix, this.left, y1, 0.0F).setUv(0, ((float) y1 / 32.0F)).setColor(64, 64, 64, alpha1);
        });*/
    }
    
    protected E remove(int int_1) {
        E itemListWidget$Item_1 = this.entries.get(int_1);
        return this.removeEntry(this.entries.get(int_1)) ? itemListWidget$Item_1 : null;
    }
    
    protected boolean removeEntry(E itemListWidget$Item_1) {
        boolean boolean_1 = this.entries.remove(itemListWidget$Item_1);
        if (boolean_1 && itemListWidget$Item_1 == this.getSelectedItem()) {
            this.selectItem(null);
        }
        
        return boolean_1;
    }
    
    public static final class SmoothScrollingSettings {
        public static final double CLAMP_EXTENSION = 200;
        
        private SmoothScrollingSettings() {
        }
    }
    
    @Environment(EnvType.CLIENT)
    public abstract static class Entry<E extends Entry<E>> implements GuiEventListener {
        @Deprecated
        DynamicErrorFreeEntryListWidget<E> parent;
        @Nullable
        private NarratableEntry lastNarratable;
        
        public Entry() {
        }
        
        public abstract void render(GuiGraphics graphics, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean isHovered, float delta);
        
        public boolean isMouseOver(double double_1, double double_2) {
            return Objects.equals(this.parent.getItemAtPosition(double_1, double_2), this);
        }
        
        public DynamicErrorFreeEntryListWidget<E> getParent() {
            return parent;
        }
        
        public void setParent(DynamicErrorFreeEntryListWidget<E> parent) {
            this.parent = parent;
        }
        
        public abstract int getItemHeight();
        
        @Deprecated
        public int getMorePossibleHeight() {
            return -1;
        }
        
        public abstract List<? extends NarratableEntry> narratables();
        
        void updateNarration(NarrationElementOutput narrationElementOutput) {
            List<? extends NarratableEntry> list = this.narratables();
            Screen.NarratableSearchResult narratableSearchResult = Screen.findNarratableWidget(list, this.lastNarratable);
            if (narratableSearchResult != null) {
                if (narratableSearchResult.priority().isTerminal()) {
                    this.lastNarratable = narratableSearchResult.entry();
                }
                
                if (list.size() > 1) {
                    narrationElementOutput.add(NarratedElementType.POSITION, Component.translatable("narrator.position.object_list", narratableSearchResult.index() + 1, list.size()));
                    if (narratableSearchResult.priority() == NarrationPriority.FOCUSED) {
                        narrationElementOutput.add(NarratedElementType.USAGE, Component.translatable("narration.component_list.usage"));
                    }
                }
                
                narratableSearchResult.entry().updateNarration(narrationElementOutput.nest());
            }
            
        }
    }
    
    @Environment(EnvType.CLIENT)
    class Entries extends AbstractList<E> {
        private final ArrayList<E> items;
        
        private Entries() {
            this.items = Lists.newArrayList();
        }
        
        @Override
        public void clear() {
            items.clear();
        }
        
        @Override
        public E get(int int_1) {
            return this.items.get(int_1);
        }
        
        @Override
        public int size() {
            return this.items.size();
        }
        
        @Override
        public E set(int int_1, E itemListWidget$Item_1) {
            E itemListWidget$Item_2 = this.items.set(int_1, itemListWidget$Item_1);
            itemListWidget$Item_1.parent = DynamicErrorFreeEntryListWidget.this;
            return itemListWidget$Item_2;
        }
        
        @Override
        public void add(int int_1, E itemListWidget$Item_1) {
            this.items.add(int_1, itemListWidget$Item_1);
            itemListWidget$Item_1.parent = DynamicErrorFreeEntryListWidget.this;
        }
        
        @Override
        public E remove(int int_1) {
            return this.items.remove(int_1);
        }
    }
}

