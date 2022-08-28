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

package me.shedaniel.rei.impl.client.gui.overlay.entries;

import com.mojang.blaze3d.vertex.PoseStack;
import me.shedaniel.clothconfig2.api.animator.NumberAnimator;
import me.shedaniel.clothconfig2.api.animator.ValueAnimator;
import me.shedaniel.math.FloatingRectangle;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.math.impl.PointHelper;
import me.shedaniel.rei.api.client.ClientHelper;
import me.shedaniel.rei.api.client.entry.renderer.EntryRenderer;
import me.shedaniel.rei.api.client.gui.widgets.Slot;
import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.util.ClientEntryStacks;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.impl.client.gui.overlay.entries.cache.CachedEntryListRender;
import me.shedaniel.rei.impl.client.gui.overlay.widgets.DisplayedEntryWidget;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static me.shedaniel.rei.impl.client.util.InternalEntryBounds.entrySize;

@SuppressWarnings("UnstableApiUsage")
public class EntryListStackEntry extends DisplayedEntryWidget {
    private final CollapsingEntryListWidget parent;
    public EntryStack<?> our;
    public Widget extra;
    private NumberAnimator<Double> size = null;
    private CollapsedStack collapsedStack = null;
    private List<FloatingRectangle> collapsedBounds = null;
    
    public EntryListStackEntry(CollapsingEntryListWidget parent, Slot slot, int entrySize, boolean zoomed) {
        super(slot);
        this.parent = parent;
        slot.size(entrySize);
        slot.noBackground();
        slot.cyclingInterval(100L);
        slot.appendContainsPointFunction((s, point) -> parent.containsChecked(point.x, point.y, true));
        
        if (zoomed) {
            slot.noHighlight();
            size = ValueAnimator.ofDouble(1f)
                    .withConvention(() -> {
                        double mouseX = PointHelper.getMouseFloatingX();
                        double mouseY = PointHelper.getMouseFloatingY();
                        int x1 = slot.getBounds().getCenterX() - entrySize / 2;
                        int y1 = slot.getBounds().getCenterY() - entrySize / 2;
                        boolean hovering = mouseX >= x1 && mouseX < x1 + entrySize && mouseY >= y1 && mouseY < y1 + entrySize;
                        return hovering ? 1.5 : 1.0;
                    }, 200);
        }
        
        this.extra = new Widget() {
            @Override
            public void render(PoseStack poses, int mouseX, int mouseY, float delta) {
                drawBackground(poses, mouseX, mouseY, delta);
                drawExtra(poses, mouseX, mouseY, delta);
            }
            
            @Override
            public List<? extends GuiEventListener> children() {
                return List.of();
            }
        };
    }
    
    public static EntryListStackEntry createSlot(CollapsingEntryListWidget parent, int x, int y, int entrySize, boolean zoomed) {
        return new EntryListStackEntry(parent, Widgets.createSlot(new Point(x, y)), entrySize, zoomed);
    }
    
    private void drawExtra(PoseStack matrices, int mouseX, int mouseY, float delta) {
        if (size != null) {
            size.update(delta);
            int centerX = slot.getBounds().getCenterX();
            int centerY = slot.getBounds().getCenterY();
            int entrySize = (int) Math.round(entrySize() * size.value());
            slot.getBounds().setBounds(centerX - entrySize / 2, centerY - entrySize / 2, entrySize, entrySize);
        }
    }
    
    private void drawBackground(PoseStack matrices, int mouseX, int mouseY, float delta) {
        Rectangle bounds = slot.getBounds();
        
        if (collapsedStack != null) {
            fillGradient(matrices, bounds.x, bounds.y, bounds.getMaxX(), bounds.getMaxY(), 0x34FFFFFF, 0x34FFFFFF);
        }
    }
    
    protected void drawCollapsedStack(PoseStack matrices, Rectangle bounds, int mouseX, int mouseY, float delta) {
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
    }
    
    @Override
    public boolean doMouse(Slot slot, double mouseX, double mouseY, int button) {
        if (collapsedStack != null) {
            parent.updatedCount++;
            collapsedStack.setExpanded(!collapsedStack.isExpanded());
            parent.updateEntriesPosition();
            Widgets.produceClickSound();
            return true;
        }
        
        return super.doMouse(slot, mouseX, mouseY, button);
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
    public Tooltip apply(Tooltip tooltip) {
        if (this.collapsedStack != null) {
            if (!this.collapsedStack.isExpanded()) {
                tooltip = Tooltip.create(new Point(tooltip.getX(), tooltip.getY()), new TranslatableComponent("text.rei.collapsed.entry", collapsedStack.getName()));
                tooltip.add((TooltipComponent) new CollapsedEntriesTooltip(collapsedStack));
                tooltip.add(new TranslatableComponent("text.rei.collapsed.entry.hint.expand", collapsedStack.getName(), collapsedStack.getIngredient().size())
                        .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
                ClientHelper.getInstance().appendModIdToTooltips(tooltip, collapsedStack.getModId());
                return tooltip;
            }
        }
        
        tooltip = super.apply(tooltip);
        if (this.collapsedStack != null) {
            tooltip.entries().add(Mth.clamp(tooltip.entries().size() - 1, 0, tooltip.entries().size() - 1), Tooltip.entry(new TranslatableComponent("text.rei.collapsed.entry.hint.collapse", collapsedStack.getName(), collapsedStack.getIngredient().size())
                    .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC)));
        }
        return tooltip;
    }
    
    @Nullable
    public CollapsedStack getCollapsedStack() {
        return collapsedStack;
    }
    
    public void updateEntries() {
        if (collapsedStack != null && !collapsedStack.isExpanded()) {
            EntryStack<?> rewrap = slot.getCurrentEntry().rewrap();
            slot.clearEntries();
            slot.entry(ClientEntryStacks.setRenderer(rewrap, (EntryRenderer<Object>) (entry, matrices, bounds, mouseX, mouseY, delta) -> {
                drawCollapsedStack(matrices, bounds, mouseX, mouseY, delta);
            }));
        } else if (our != null) {
            if (CachedEntryListRender.cachedTextureLocation != null) {
                slot.clearEntries();
                slot.entry(our);
            }
        }
    }
}