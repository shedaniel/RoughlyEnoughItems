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

package me.shedaniel.rei.impl.client.gui.widget;

import com.mojang.blaze3d.vertex.PoseStack;
import me.shedaniel.math.Point;
import me.shedaniel.rei.api.client.ClientHelper;
import me.shedaniel.rei.api.client.config.ConfigObject;
import me.shedaniel.rei.api.client.gui.config.ItemCheatingMode;
import me.shedaniel.rei.api.client.gui.screen.DisplayScreen;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.item.ItemStack;

public abstract class DisplayedEntryWidget extends EntryWidget {
    public int backupY;
    
    protected DisplayedEntryWidget(Point point, int entrySize) {
        super(point);
        this.backupY = point.y;
        getBounds().width = getBounds().height = entrySize;
    }
    
    @Override
    protected void drawHighlighted(PoseStack matrices, int mouseX, int mouseY, float delta) {
        if (!getCurrentEntry().isEmpty())
            super.drawHighlighted(matrices, mouseX, mouseY, delta);
    }
    
    @Override
    public void queueTooltip(PoseStack matrices, int mouseX, int mouseY, float delta) {
        if (ClientHelper.getInstance().isCheating() && !(Minecraft.getInstance().screen instanceof DisplayScreen) && !minecraft.player.containerMenu.getCarried().isEmpty()) {
            return;
        }
        super.queueTooltip(matrices, mouseX, mouseY, delta);
    }
    
    @Override
    protected boolean doAction(double mouseX, double mouseY, int button) {
        if (!(ClientHelper.getInstance().isCheating() && !(Minecraft.getInstance().screen instanceof DisplayScreen))) return false;
        EntryStack<?> entry = getCurrentEntry().copy();
        if (!entry.isEmpty()) {
            if (entry.getType() != VanillaEntryTypes.ITEM) {
                EntryStack<ItemStack> cheatsAs = entry.cheatsAs();
                entry = cheatsAs.isEmpty() ? entry : cheatsAs;
            }
            if (entry.getValueType() == ItemStack.class)
                entry.<ItemStack>castValue().setCount(button != 1 && !Screen.hasShiftDown() == (ConfigObject.getInstance().getItemCheatingMode() == ItemCheatingMode.REI_LIKE) ? 1 : entry.<ItemStack>castValue().getMaxStackSize());
            return ClientHelper.getInstance().tryCheatingEntry(entry);
        }
        
        return super.doAction(mouseX, mouseY, button);
    }
    
    @Override
    public boolean cancelDeleteItems(EntryStack<?> stack) {
        if (!interactable || !ConfigObject.getInstance().isGrabbingItems())
            return super.cancelDeleteItems(stack);
        if (ClientHelper.getInstance().isCheating() && !(Minecraft.getInstance().screen instanceof DisplayScreen)) {
            EntryStack<?> entry = getCurrentEntry().copy();
            if (!entry.isEmpty()) {
                if (entry.getType() != VanillaEntryTypes.ITEM) {
                    EntryStack<ItemStack> cheatsAs = entry.cheatsAs();
                    entry = cheatsAs.isEmpty() ? entry : cheatsAs;
                }
                return EntryStacks.equalsExact(entry, stack);
            }
        }
        return super.cancelDeleteItems(stack);
    }
}
