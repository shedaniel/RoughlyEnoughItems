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
import me.shedaniel.math.FloatingRectangle;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.math.impl.PointHelper;
import me.shedaniel.rei.api.client.ClientHelper;
import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import me.shedaniel.rei.api.client.gui.widgets.TooltipContext;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.impl.client.gui.widget.CachedEntryListRender;
import me.shedaniel.rei.impl.client.gui.widget.DisplayedEntryWidget;
import me.shedaniel.rei.impl.common.entry.type.collapsed.CollapsedStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static me.shedaniel.rei.impl.client.gui.widget.entrylist.EntryListWidget.entrySize;

@SuppressWarnings("UnstableApiUsage")
public class EntryListStackEntry extends DisplayedEntryWidget {
    private final CollapsingEntryListWidget parent;
    public EntryStack<?> our;
    private NumberAnimator<Double> size = null;
    private CollapsedStack collapsedStack = null;
    private List<FloatingRectangle> collapsedBounds = null;
    
    public EntryListStackEntry(CollapsingEntryListWidget parent, int x, int y, int entrySize, boolean zoomed) {
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
    
    @Override
    protected void drawBackground(PoseStack matrices, int mouseX, int mouseY, float delta) {
        Rectangle bounds = getBounds();
        
        if (collapsedStack != null) {
            fillGradient(matrices, bounds.x, bounds.y, bounds.getMaxX(), bounds.getMaxY(), 0x34FFFFFF, 0x34FFFFFF);
        }
        
        super.drawBackground(matrices, mouseX, mouseY, delta);
    }
    
    @Override
    protected void drawCurrentEntry(PoseStack matrices, int mouseX, int mouseY, float delta) {
        if (collapsedStack != null && !collapsedStack.isExpanded()) {
            Rectangle bounds = getBounds();
            List<EntryStack<?>> stacks = collapsedStack.getIngredient();
            float fullSize = bounds.getWidth();
            
            matrices.pushPose();
            matrices.translate(0, 0, 10);
            
            for (int i = stacks.size() - 1; i >= 0; i--) {
                EntryStack<?> stack = stacks.get(i);
                
                if (i >= collapsedBounds.size()) {
                    continue;
                }
                
                FloatingRectangle value = collapsedBounds.get(i);
                double x = bounds.x + value.x * fullSize;
                double y = bounds.y + value.y * fullSize;
                
                double scaledSize = value.width * fullSize;
                
                stack.render(matrices, new Rectangle(x - scaledSize / 2, y - scaledSize / 2, scaledSize, scaledSize), mouseX, mouseY, delta);
                
                matrices.translate(0, 0, 10);
            }
            
            matrices.popPose();
        } else {
            super.drawCurrentEntry(matrices, mouseX, mouseY, delta);
        }
    }
    
    @Override
    protected boolean doAction(double mouseX, double mouseY, int button) {
        if (collapsedStack != null && button == 0 && Screen.hasAltDown()) {
            parent.updatedCount++;
            collapsedStack.setExpanded(!collapsedStack.isExpanded());
            parent.updateEntriesPosition();
            Widgets.produceClickSound();
            return true;
        }
        
        return super.doAction(mouseX, mouseY, button);
    }
    
    public void collapsed(CollapsedStack collapsedStack) {
        this.collapsedStack = collapsedStack;
        if (collapsedStack == null) {
            this.collapsedBounds = null;
        } else {
            List<EntryStack<?>> ingredient = collapsedStack.getIngredient();
            if (ingredient.size() == 0) this.collapsedBounds = null;
            else if (ingredient.size() == 1) {
                this.collapsedBounds = List.of(new FloatingRectangle(0, 0, 1, 1));
            } else {
                this.collapsedBounds = List.of(new FloatingRectangle(0.44, 0.56, 0.9, 0.8),
                        new FloatingRectangle(0.56, 0.44, 0.9, 0.8));
            }
        }
    }
    
    @Override
    @Nullable
    public Tooltip getCurrentTooltip(TooltipContext context) {
        if (this.collapsedStack != null) {
            if (!this.collapsedStack.isExpanded()) {
                Tooltip tooltip = Tooltip.create(context.getPoint(), Component.translatable("text.rei.collapsed.entry", collapsedStack.getName()));
                tooltip.add(new CollapsedEntriesTooltip(collapsedStack));
                tooltip.add(Component.translatable(Minecraft.ON_OSX ? "text.rei.collapsed.entry.hint.expand.macos" : "text.rei.collapsed.entry.hint.expand", collapsedStack.getName(), collapsedStack.getIngredient().size())
                        .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
                ClientHelper.getInstance().appendModIdToTooltips(tooltip, collapsedStack.getModId());
                return tooltip;
            }
        }
        
        Tooltip tooltip = super.getCurrentTooltip(context);
        if (tooltip != null && this.collapsedStack != null) {
            tooltip.entries().add(Mth.clamp(tooltip.entries().size() - 1, 0, tooltip.entries().size() - 1), Tooltip.entry(Component.translatable(Minecraft.ON_OSX ? "text.rei.collapsed.entry.hint.collapse.macos" : "text.rei.collapsed.entry.hint.collapse", collapsedStack.getName(), collapsedStack.getIngredient().size())
                    .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC)));
        }
        return tooltip;
    }
    
    @Nullable
    public CollapsedStack getCollapsedStack() {
        return collapsedStack;
    }
    
    @Override
    protected long getCyclingInterval() {
        return 100;
    }
}