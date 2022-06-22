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

package me.shedaniel.rei.api.client.gui;

import com.google.common.base.Predicates;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.widgets.*;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.type.EntryDefinition;
import me.shedaniel.rei.api.common.util.CollectionUtils;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SimpleDisplayRenderer extends DisplayRenderer implements WidgetHolder {
    protected static final ResourceLocation CHEST_GUI_TEXTURE = new ResourceLocation("roughlyenoughitems", "textures/gui/recipecontainer.png");
    protected List<Slot> inputWidgets;
    protected List<Slot> outputWidgets;
    protected List<GuiEventListener> widgets;
    
    @ApiStatus.Internal
    protected SimpleDisplayRenderer(List<EntryIngredient> input, List<EntryIngredient> output) {
        this.inputWidgets = CollectionUtils.map(simplify(input), this::createSlot);
        this.outputWidgets = CollectionUtils.map(simplify(output), this::createSlot);
        this.widgets = Stream.concat(inputWidgets.stream(), outputWidgets.stream()).collect(Collectors.toList());
    }
    
    protected Slot createSlot(EntryIngredient ingredient) {
        return Widgets.createSlot(new Point(0, 0))
                .entries(CollectionUtils.filterToList(ingredient, stack -> !stack.isEmpty()))
                .disableBackground()
                .disableHighlight()
                .disableTooltips();
    }
    
    public static List<EntryIngredient> simplify(List<EntryIngredient> original) {
        List<EntryIngredient> out = new ArrayList<>();
        for (EntryIngredient ingredient : original) {
            EntryIngredient filter = ingredient.filter(Predicates.not(EntryStack::isEmpty));
            if (filter.isEmpty()) continue;
            EntryIngredient orNull = CollectionUtils.findFirstOrNull(out, s -> equalsList(filter, s));
            if (orNull == null) {
                out.add(filter);
            } else {
                out.set(out.indexOf(orNull), orNull.map(stack -> {
                    for (EntryStack<?> filterStack : filter) {
                        if (EntryStacks.equalsExact(filterStack, stack)) {
                            EntryDefinition<Object> definition = (EntryDefinition<Object>) filterStack.getDefinition();
                            Object newValue = definition.add(stack.getValue(), filterStack.getValue());
                            
                            if (newValue != null) {
                                stack = EntryStack.of(definition, newValue);
                            }
                        }
                    }
                    
                    return stack;
                }));
            }
        }
        return out;
    }
    
    public static DisplayRenderer from(List<EntryIngredient> input, List<EntryIngredient> output) {
        return new SimpleDisplayRenderer(input, output);
    }
    
    public static boolean equalsList(EntryIngredient left, EntryIngredient right) {
        LongSet leftBytes = new LongOpenHashSet(left.size());
        for (EntryStack<?> entryStack : left) {
            leftBytes.add(EntryStacks.hashExact(entryStack));
        }
        if (leftBytes.size() > right.size()) return false;
        LongSet rightBytes = new LongOpenHashSet(right.size());
        for (EntryStack<?> entryStack : right) {
            rightBytes.add(EntryStacks.hashExact(entryStack));
            
            if (rightBytes.size() > leftBytes.size()) return false;
        }
        return leftBytes.equals(rightBytes);
    }
    
    @Override
    public void render(PoseStack matrices, Rectangle bounds, int mouseX, int mouseY, float delta) {
        int xx = bounds.x + 4, yy = bounds.y + 2;
        int j = 0;
        int itemsPerLine = getItemsPerLine();
        for (Slot entryWidget : inputWidgets) {
            entryWidget.setZ(getZ() + 50);
            entryWidget.getBounds().setLocation(xx, yy);
            entryWidget.render(matrices, mouseX, mouseY, delta);
            xx += 18;
            j++;
            if (j >= getItemsPerLine() - 2) {
                yy += 18;
                xx = bounds.x + 4;
                j = 0;
            }
        }
        xx = bounds.x + 4 + 18 * (getItemsPerLine() - 2);
        yy = bounds.y + getHeight() / 2 - 8;
        RenderSystem.setShaderTexture(0, CHEST_GUI_TEXTURE);
        blit(matrices, xx, yy, 0, 28, 18, 18);
        xx += 18;
        yy += outputWidgets.size() * -9 + 9;
        for (Slot outputWidget : outputWidgets) {
            outputWidget.setZ(getZ() + 50);
            outputWidget.getBounds().setLocation(xx, yy);
            outputWidget.render(matrices, mouseX, mouseY, delta);
            yy += 18;
        }
    }
    
    @Nullable
    @Override
    public Tooltip getTooltip(TooltipContext context) {
        for (Slot widget : inputWidgets) {
            if (widget.containsMouse(context.getPoint()))
                return widget.getCurrentTooltip(context);
        }
        for (Slot widget : outputWidgets) {
            if (widget.containsMouse(context.getPoint()))
                return widget.getCurrentTooltip(context);
        }
        return null;
    }
    
    @Override
    public int getHeight() {
        return Math.max(4 + getItemsHeight() * 18, 4 + outputWidgets.size() * 18);
    }
    
    public int getItemsHeight() {
        return Math.max(Mth.ceil(((float) inputWidgets.size()) / (getItemsPerLine() - 2)), outputWidgets.size());
    }
    
    public int getItemsPerLine() {
        return Mth.floor((getWidth() - 4f) / 18f);
    }
    
    @Override
    public List<? extends GuiEventListener> children() {
        return widgets;
    }
}
