/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020 shedaniel
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

package me.shedaniel.rei.gui.widget;

import me.shedaniel.math.Point;
import me.shedaniel.rei.api.ClientHelper;
import me.shedaniel.rei.api.ConfigObject;
import me.shedaniel.rei.api.EntryStack;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;

import static me.shedaniel.rei.gui.widget.EntryListWidget.entrySize;

public class EntryListEntryWidget extends EntryWidget {
    public int backupY;
    
    protected EntryListEntryWidget(Point point) {
        super(point);
        this.backupY = point.y;
        getBounds().width = getBounds().height = entrySize();
    }
    
    @Override
    protected void drawHighlighted(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        if (!getCurrentEntry().isEmpty())
            super.drawHighlighted(matrices, mouseX, mouseY, delta);
    }
    
    @Override
    public void queueTooltip(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        if (ClientHelper.getInstance().isCheating() && !minecraft.player.inventory.getCursorStack().isEmpty()) {
            return;
        }
        super.queueTooltip(matrices, mouseX, mouseY, delta);
    }
    
    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (!interactable)
            return super.mouseReleased(mouseX, mouseY, button);
        if (containsMouse(mouseX, mouseY) && ClientHelper.getInstance().isCheating()) {
            EntryStack entry = getCurrentEntry().copy();
            if (!entry.isEmpty() && wasClicked()) {
                if (entry.getType() == EntryStack.Type.FLUID) {
                    Item bucketItem = entry.getFluid().getBucketItem();
                    if (bucketItem != null) {
                        entry = EntryStack.create(bucketItem);
                    }
                }
                if (entry.getType() == EntryStack.Type.ITEM)
                    entry.setAmount(button != 1 && !Screen.hasShiftDown() ? 1 : entry.getItemStack().getMaxCount());
                return ClientHelper.getInstance().tryCheatingEntry(entry);
            }
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }
    
    @Override
    protected boolean cancelDeleteItems(EntryStack stack) {
        if (!interactable || !ConfigObject.getInstance().isGrabbingItems())
            return super.cancelDeleteItems(stack);
        if (ClientHelper.getInstance().isCheating()) {
            EntryStack entry = getCurrentEntry().copy();
            if (!entry.isEmpty()) {
                if (entry.getType() == EntryStack.Type.FLUID) {
                    Item bucketItem = entry.getFluid().getBucketItem();
                    if (bucketItem != null) {
                        entry = EntryStack.create(bucketItem);
                    }
                }
                return entry.equalsIgnoreAmount(stack);
            }
        }
        return super.cancelDeleteItems(stack);
    }
}
