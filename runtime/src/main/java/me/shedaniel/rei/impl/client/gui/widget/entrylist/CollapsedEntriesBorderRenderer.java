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

package me.shedaniel.rei.impl.client.gui.widget.entrylist;

import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.longs.*;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.impl.common.entry.type.collapsed.CollapsedStack;
import net.minecraft.client.gui.GuiComponent;

import static me.shedaniel.rei.impl.client.gui.widget.entrylist.EntryListWidget.entrySize;

public class CollapsedEntriesBorderRenderer extends GuiComponent {
    private static final int TOP = 0b00;
    private static final int BOTTOM = 0b01;
    private static final int LEFT = 0b10;
    private static final int RIGHT = 0b11;
    private static final int TOP_O = 0b100;
    private static final int BOTTOM_O = 0b101;
    private static final int LEFT_O = 0b110;
    private static final int RIGHT_O = 0b111;
    
    public void render(PoseStack matrices, Iterable<EntryListStackEntry> entries, Object2IntMap<CollapsedStack> collapsedStackIndicesGlobal) {
        if (collapsedStackIndicesGlobal.isEmpty()) return;
        LongSet edgeSet = new LongOpenHashSet();
        int entrySize = entrySize();
        // bit 0-1: direction
        // bit 2 occupied: has edge
        // bit 3-14: collapsed stack index
        // bit 15-30: y
        // bit 31-46: x
        for (EntryListStackEntry entry : entries) {
            if (entry.getCollapsedStack() != null && entry.getCollapsedStack().isExpanded()) {
                Rectangle entryBounds = entry.getBounds();
                int collapsedStackIndices = collapsedStackIndicesGlobal.getInt(entry.getCollapsedStack());
                long base = getPackedLong(entryBounds.getCenterX() + 100, entryBounds.getCenterY() + 100, collapsedStackIndices, 0, false);
                if (!edgeSet.add(base | TOP)) edgeSet.add(base | TOP_O);
                if (!edgeSet.add(base | BOTTOM)) edgeSet.add(base | BOTTOM_O);
                if (!edgeSet.add(base | LEFT)) edgeSet.add(base | LEFT_O);
                if (!edgeSet.add(base | RIGHT)) edgeSet.add(base | RIGHT_O);
                // to left (base - ((long) entrySize << 31)) | RIGHT)
                long nL = withDirD(shiftLongX(base, -entrySize), RIGHT, false);
                if (edgeSet.contains(nL)) {
                    edgeSet.add(occupiedLong(nL));
                    edgeSet.add(base | LEFT_O);
                }
                // to right
                nL = withDirD(shiftLongX(base, entrySize), LEFT, false);
                if (edgeSet.contains(nL)) {
                    edgeSet.add(occupiedLong(nL));
                    edgeSet.add(base | RIGHT_O);
                }
                // to top
                nL = withDirD(shiftLongY(base, -entrySize), BOTTOM, false);
                if (edgeSet.contains(nL)) {
                    edgeSet.add(occupiedLong(nL));
                    edgeSet.add(base | TOP_O);
                }
                // to bottom
                nL = withDirD(shiftLongY(base, entrySize), TOP, false);
                if (edgeSet.contains(nL)) {
                    edgeSet.add(occupiedLong(nL));
                    edgeSet.add(base | BOTTOM_O);
                }
            }
        }
        
        LongIterator iterator = edgeSet.iterator();
        LongList toRemove = new LongArrayList();
        while (iterator.hasNext()) {
            long l = iterator.nextLong();
            if ((l & 0b100) != 0) {
                toRemove.add(l);
                toRemove.add(l & ~0b100);
            }
        }
        edgeSet.removeAll(toRemove);
    
        matrices.pushPose();
        matrices.translate(-100, -100, 0);
        
        iterator = edgeSet.iterator();
        while (iterator.hasNext()) {
            long l = iterator.nextLong();
            int x = (int) (l >> 31);
            int y = (int) ((l >> 15) & 0xFFFF);
            int collapsedStackIndices = (int) ((l >> 3) & 0b111111111111);
            int x1 = x - entrySize / 2;
            int y1 = y - entrySize / 2;
            int x2 = x1 + entrySize;
            int y2 = y1 + entrySize;
            int direction = (int) (l & RIGHT);
            int fStart, fEnd;
            switch (direction) {// top
                case 0:
                    fStart = edgeSet.contains(shiftLongX(l, -entrySize)) || edgeSet.contains(withDirD(l, LEFT, false)) ? 0 : 1;
                    fEnd = edgeSet.contains(shiftLongX(l, entrySize)) || edgeSet.contains(withDirD(l, RIGHT, false)) ? 0 : 1;
                    if (fStart == 1 && edgeSet.contains(getPackedLong(x - entrySize, y - entrySize, collapsedStackIndices, RIGHT, false)))
                        fStart = -1;
                    if (fEnd == 1 && edgeSet.contains(getPackedLong(x + entrySize, y - entrySize, collapsedStackIndices, LEFT, false)))
                        fEnd = -1;
                    fillGradient(matrices, x1 + fStart, y1, x2 - fEnd, y1 + 1, 0x67FFFFFF, 0x67FFFFFF);
                    break;
// bottom
                case 1:
                    fStart = edgeSet.contains(shiftLongX(l, -entrySize)) || edgeSet.contains(withDirD(l, LEFT, false)) ? 0 : 1;
                    fEnd = edgeSet.contains(shiftLongX(l, entrySize)) || edgeSet.contains(withDirD(l, RIGHT, false)) ? 0 : 1;
                    if (fStart == 1 && edgeSet.contains(getPackedLong(x - entrySize, y + entrySize, collapsedStackIndices, RIGHT, false)))
                        fStart = -1;
                    if (fEnd == 1 && edgeSet.contains(getPackedLong(x + entrySize, y + entrySize, collapsedStackIndices, LEFT, false)))
                        fEnd = -1;
                    fillGradient(matrices, x1 + fStart, y2 - 1, x2 - fEnd, y2, 0x67FFFFFF, 0x67FFFFFF);
                    break;
// left
                case 2:
                    fStart = edgeSet.contains(shiftLongY(l, -entrySize)) ? 0 : 1;
                    fEnd = edgeSet.contains(shiftLongY(l, entrySize)) ? 0 : 1;
                    if (fStart == 1 && edgeSet.contains(getPackedLong(x - entrySize, y - entrySize, collapsedStackIndices, BOTTOM, false)))
                        fStart = 0;
                    if (fEnd == 1 && edgeSet.contains(getPackedLong(x - entrySize, y + entrySize, collapsedStackIndices, TOP, false)))
                        fEnd = 0;
                    fillGradient(matrices, x1, y1 + fStart, x1 + 1, y2 - fEnd, 0x67FFFFFF, 0x67FFFFFF);
                    break;
// right
                case 3:
                    fStart = edgeSet.contains(shiftLongY(l, -entrySize)) ? 0 : 1;
                    fEnd = edgeSet.contains(shiftLongY(l, entrySize)) ? 0 : 1;
                    if (fStart == 1 && edgeSet.contains(getPackedLong(x + entrySize, y - entrySize, collapsedStackIndices, BOTTOM, false)))
                        fStart = 0;
                    if (fEnd == 1 && edgeSet.contains(getPackedLong(x + entrySize, y + entrySize, collapsedStackIndices, TOP, false)))
                        fEnd = 0;
                    fillGradient(matrices, x2 - 1, y1 + fStart, x2, y2 - fEnd, 0x67FFFFFF, 0x67FFFFFF);
                    break;
            }
        }
    
        matrices.popPose();
    }
    
    private static long getPackedLong(int x, int y, int collapsedStackIndices, int direction, boolean occupied) {
        long l = ((long) x << 31) | ((long) y << 15) | ((long) collapsedStackIndices << 3) | direction;
        if (occupied) l |= 0b100;
        return l;
    }
    
    private static long shiftLongX(long l, int dX) {
        return l + ((long) dX << 31);
    }
    
    private static long shiftLongY(long l, int dX) {
        return l + ((long) dX << 15);
    }
    
    private static long withDirD(long l, int dir, boolean occupied) {
        return (l & ~RIGHT_O) | dir | (occupied ? 0b100 : 0);
    }
    
    private static long occupiedLong(long l) {
        return l | 0b100;
    }
}
