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

package me.shedaniel.rei.impl.client.gui.widget.entrylist;

import com.google.common.base.Predicates;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import me.shedaniel.clothconfig2.ClothConfigInitializer;
import me.shedaniel.clothconfig2.api.ScissorsHandler;
import me.shedaniel.clothconfig2.api.scroll.ScrollingContainer;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.REIRuntime;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.impl.client.gui.widget.BatchedEntryRendererManager;
import me.shedaniel.rei.impl.client.gui.widget.EntryWidget;
import me.shedaniel.rei.impl.common.entry.type.collapsed.CollapsedStack;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public class ScrolledEntryListWidget extends CollapsingEntryListWidget {
    private List</*EntryStack<?> | EntryIngredient*/ Object> stacks = new ArrayList<>();
    protected List<EntryListStackEntry> entries = Collections.emptyList();
    protected int blockedCount;
    protected final ScrollingContainer scrolling = new ScrollingContainer() {
        @Override
        public Rectangle getBounds() {
            return ScrolledEntryListWidget.this.getBounds();
        }
        
        @Override
        public int getMaxScrollHeight() {
            return Mth.ceil((stacks.size() + blockedCount) / (innerBounds.width / (float) entrySize())) * entrySize();
        }
    };
    
    @Override
    protected void renderEntries(boolean fastEntryRendering, PoseStack matrices, int mouseX, int mouseY, float delta) {
        ScissorsHandler.INSTANCE.scissor(bounds);
        
        int skip = Math.max(0, Mth.floor(scrolling.scrollAmount() / (float) entrySize()));
        int nextIndex = skip * innerBounds.width / entrySize();
        this.blockedCount = 0;
        BatchedEntryRendererManager helper = new BatchedEntryRendererManager();
        Int2ObjectMap<CollapsedStack> indexedCollapsedStack = getCollapsedStackIndexed();
        
        int i = nextIndex;
        for (int cont = nextIndex; cont < entries.size(); cont++) {
            EntryListStackEntry entry = entries.get(cont);
            Rectangle entryBounds = entry.getBounds();
            
            entryBounds.y = entry.backupY - scrolling.scrollAmountInt();
            if (entryBounds.y > this.bounds.getMaxY()) break;
            if (stacks.size() <= i) break;
            if (notSteppingOnExclusionZones(entryBounds.x, entryBounds.y, entryBounds.width, entryBounds.height)) {
                /*EntryStack<?> | List<EntryStack<?>>*/
                Object stack = stacks.get(i++);
                entry.clearStacks();
                
                if (stack instanceof EntryStack<?> entryStack) {
                    if (!entryStack.isEmpty()) {
                        entry.entry(entryStack);
                        helper.add(entry);
                    }
                } else {
                    List<EntryStack<?>> ingredient = (List<EntryStack<?>>) stack;
                    if (!ingredient.isEmpty()) {
                        entry.entries(ingredient);
                        helper.addSlow(entry);
                    }
                }
                
                CollapsedStack collapsedStack = indexedCollapsedStack.get(i - 1);
                if (collapsedStack != null && collapsedStack.getIngredient().size() > 1) {
                    entry.collapsed(collapsedStack);
                } else {
                    entry.collapsed(null);
                }
            } else {
                blockedCount++;
            }
        }
        
        helper.render(debugger.debugTime, debugger.size, debugger.time, matrices, mouseX, mouseY, delta);
        
        scrolling.updatePosition(delta);
        ScissorsHandler.INSTANCE.removeLastScissor();
        if (scrolling.getMaxScroll() > 0) {
            scrolling.renderScrollBar(0, 1, REIRuntime.getInstance().isDarkThemeEnabled() ? 0.8f : 1f);
        }
    }
    
    @Override
    protected void updateEntries(int entrySize, boolean zoomed) {
        int width = innerBounds.width / entrySize;
        int pageHeight = innerBounds.height / entrySize;
        int slotsToPrepare = Math.max(stacks.size() * 3, width * pageHeight * 3);
        int currentX = 0;
        int currentY = 0;
        List<EntryListStackEntry> entries = Lists.newArrayList();
        for (int i = 0; i < slotsToPrepare; i++) {
            int xPos = currentX * entrySize + innerBounds.x;
            int yPos = currentY * entrySize + innerBounds.y;
            entries.add((EntryListStackEntry) new EntryListStackEntry(this, xPos, yPos, entrySize, zoomed).noBackground());
            currentX++;
            if (currentX >= width) {
                currentX = 0;
                currentY++;
            }
        }
        this.entries = entries;
    }
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (containsChecked(mouseX, mouseY, false) && !Screen.hasControlDown()) {
            scrolling.offset(ClothConfigInitializer.getScrollStep() * -amount, true);
            return true;
        }
        
        return super.mouseScrolled(mouseX, mouseY, amount);
    }
    
    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dx, double dy) {
        if (hasSpace() && scrolling.mouseDragged(mouseX, mouseY, button, dx, dy))
            return true;
        return super.mouseDragged(mouseX, mouseY, button, dx, dy);
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!hasSpace()) return false;
        if (scrolling.updateDraggingState(mouseX, mouseY, button))
            return true;
        return super.mouseClicked(mouseX, mouseY, button);
    }
    
    @Override
    public List</*EntryStack<?> | List<EntryStack<?>>*/ Object> getStacks() {
        return stacks;
    }
    
    @Override
    public void setStacks(List</*EntryStack<?> | List<EntryStack<?>>*/ Object> stacks) {
        this.stacks = stacks;
    }
    
    @Override
    public Stream<EntryStack<?>> getEntries() {
        int skip = Math.max(0, Mth.floor(scrolling.scrollAmount() / (float) entrySize()));
        int nextIndex = skip * innerBounds.width / entrySize();
        return (Stream<EntryStack<?>>) (Stream<? extends EntryStack<?>>) entries.stream()
                .skip(nextIndex)
                .filter(entry -> entry.getBounds().y <= this.bounds.getMaxY())
                .map(EntryWidget::getCurrentEntry)
                .filter(Predicates.not(EntryStack::isEmpty));
    }
    
    @Override
    protected List<EntryListStackEntry> getEntryWidgets() {
        return entries;
    }
}
