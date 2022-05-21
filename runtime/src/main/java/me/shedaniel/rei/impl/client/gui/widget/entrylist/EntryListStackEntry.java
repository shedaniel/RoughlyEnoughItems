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

import com.mojang.blaze3d.vertex.PoseStack;
import me.shedaniel.clothconfig2.api.animator.NumberAnimator;
import me.shedaniel.clothconfig2.api.animator.ValueAnimator;
import me.shedaniel.math.Point;
import me.shedaniel.math.impl.PointHelper;
import me.shedaniel.rei.api.client.ClientHelper;
import me.shedaniel.rei.api.client.REIRuntime;
import me.shedaniel.rei.api.client.gui.screen.DisplayScreen;
import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import me.shedaniel.rei.api.client.overlay.ScreenOverlay;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.client.registry.transfer.TransferHandler;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.plugins.PluginManager;
import me.shedaniel.rei.impl.client.gui.widget.AutoCraftingEvaluator;
import me.shedaniel.rei.impl.client.gui.widget.CachedEntryListRender;
import me.shedaniel.rei.impl.client.gui.widget.DisplayedEntryWidget;
import me.shedaniel.rei.impl.client.view.ViewsImpl;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.sounds.SoundEvents;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static me.shedaniel.rei.impl.client.gui.widget.entrylist.EntryListWidget.entrySize;

@SuppressWarnings("UnstableApiUsage")
public class EntryListStackEntry extends DisplayedEntryWidget {
    private final EntryListWidget parent;
    public EntryStack<?> our;
    private NumberAnimator<Double> size = null;
    
    public EntryListStackEntry(EntryListWidget parent, int x, int y, int entrySize, boolean zoomed) {
        super(new Point(x, y), entrySize);
        this.parent = parent;
        if (zoomed) {
            noHighlight();
            size = ValueAnimator.ofDouble(1f)
                    .withConvention(() -> {
                        double mouseX = PointHelper.getMouseFloatingX();
                        double mouseY = PointHelper.getMouseFloatingY();
                        int x1 = getBounds().getCenterX() - entrySize / 2;
                        int y1 = getBounds().getCenterY() - entrySize / 2;
                        boolean hovering = mouseX >= x1 && mouseX < x1 + entrySize && mouseY >= y1 && mouseY < y1 + entrySize;
                        return hovering ? 1.5 : 1.0;
                    }, 200);
        }
    }
    
    @Override
    protected void drawExtra(PoseStack matrices, int mouseX, int mouseY, float delta) {
        if (size != null) {
            size.update(delta);
            int centerX = getBounds().getCenterX();
            int centerY = getBounds().getCenterY();
            int entrySize = (int) Math.round(entrySize() * size.value());
            getBounds().setBounds(centerX - entrySize / 2, centerY - entrySize / 2, entrySize, entrySize);
        }
        super.drawExtra(matrices, mouseX, mouseY, delta);
    }
    
    @Override
    public EntryStack<?> getCurrentEntry() {
        if (our != null) {
            if (CachedEntryListRender.cachedTextureLocation != null) {
                return our;
            }
        }
        
        return super.getCurrentEntry();
    }
    
    @Override
    public boolean containsMouse(double mouseX, double mouseY) {
        return super.containsMouse(mouseX, mouseY) && parent.containsChecked(mouseX, mouseY, true);
    }
}