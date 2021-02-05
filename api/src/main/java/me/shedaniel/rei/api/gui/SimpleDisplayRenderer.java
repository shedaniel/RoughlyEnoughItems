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

package me.shedaniel.rei.api.gui;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import me.shedaniel.architectury.utils.Fraction;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.gui.widgets.Slot;
import me.shedaniel.rei.api.gui.widgets.Tooltip;
import me.shedaniel.rei.api.gui.widgets.Widgets;
import me.shedaniel.rei.api.ingredient.EntryIngredient;
import me.shedaniel.rei.api.ingredient.EntryStack;
import me.shedaniel.rei.api.ingredient.util.EntryStacks;
import me.shedaniel.rei.api.util.CollectionUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class SimpleDisplayRenderer extends DisplayRenderer {
    private static final Comparator<EntryStack<?>> ENTRY_COMPARATOR = Comparator.comparingLong(EntryStacks::hashExact);
    private static final ResourceLocation CHEST_GUI_TEXTURE = new ResourceLocation("roughlyenoughitems", "textures/gui/recipecontainer.png");
    private List<Slot> inputWidgets;
    private List<Slot> outputWidgets;
    
    @ApiStatus.Internal
    private SimpleDisplayRenderer(List<EntryIngredient> input, List<EntryIngredient> output) {
        this.inputWidgets = simplify(input).stream().filter(stacks -> !stacks.isEmpty()).map(stacks -> Widgets.createSlot(new Point(0, 0)).entries(stacks).disableBackground().disableHighlight().disableTooltips()).collect(Collectors.toList());
        this.outputWidgets = CollectionUtils.map(simplify(output), outputStacks ->
                Widgets.createSlot(new Point(0, 0)).entries(CollectionUtils.filter(outputStacks, stack -> !stack.isEmpty())).disableBackground().disableHighlight().disableTooltips());
    }
    
    private static List<EntryIngredient> simplify(List<EntryIngredient> original) {
        Map<EntryIngredient, AtomicReference<Fraction>> inputCounter = Maps.newLinkedHashMap();
        original.stream().collect(Collectors.groupingBy(stacks -> CollectionUtils.mapAndMax(stacks, EntryStack::getAmount, Fraction::compareTo).orElse(Fraction.zero())))
                .forEach((fraction, value) -> {
                    if (!fraction.equals(Fraction.zero())) {
                        value.forEach(stackList -> {
                            EntryIngredient stacks = inputCounter.keySet().stream().filter(s -> equalsList(stackList, s)).findFirst().orElse(stackList);
                            AtomicReference<Fraction> reference = inputCounter.computeIfAbsent(stacks, s -> new AtomicReference<>(Fraction.zero()));
                            reference.set(reference.get().add(fraction));
                        });
                    }
                });
        return inputCounter.entrySet().stream().map(entry -> EntryIngredient.of(CollectionUtils.map(entry.getKey(), stack -> {
            EntryStack<?> s = stack.copy();
            s.setAmount(entry.getValue().get());
            return s;
        }))).collect(Collectors.toList());
    }
    
    public static DisplayRenderer from(Supplier<List<EntryIngredient>> input, Supplier<List<EntryIngredient>> output) {
        return from(input.get(), output.get());
    }
    
    public static DisplayRenderer from(List<EntryIngredient> input, List<EntryIngredient> output) {
        return new SimpleDisplayRenderer(input, output);
    }
    
    public static boolean equalsList(EntryIngredient left, EntryIngredient right) {
        IntSet leftBytes = new IntOpenHashSet(left.size());
        for (EntryStack<?> entryStack : left) {
            leftBytes.add(EntryStacks.hashIgnoreCount(entryStack));
        }
        if (leftBytes.size() > right.size()) return false;
        IntSet rightBytes = new IntOpenHashSet(right.size());
        for (EntryStack<?> entryStack : right) {
            rightBytes.add(EntryStacks.hashIgnoreCount(entryStack));
            
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
        Minecraft.getInstance().getTextureManager().bind(CHEST_GUI_TEXTURE);
        blit(matrices, xx, yy, 0, 28, 18, 18);
        xx += 18;
        yy += outputWidgets.size() * -9 + 9;
        for (Slot outputWidget : outputWidgets) {
            outputWidget.setZ(getZ() + 50);
            outputWidget.getBounds().setLocation(xx, yy);
            outputWidget.render(matrices, mouseX, mouseY, delta);
        }
    }
    
    @Nullable
    @Override
    public Tooltip getTooltip(Point point) {
        for (Slot widget : inputWidgets) {
            if (widget.containsMouse(point))
                return widget.getCurrentTooltip(point);
        }
        for (Slot widget : outputWidgets) {
            if (widget.containsMouse(point))
                return widget.getCurrentTooltip(point);
        }
        return null;
    }
    
    @Override
    public int getHeight() {
        return 4 + getItemsHeight() * 18;
    }
    
    public int getItemsHeight() {
        return Mth.ceil(((float) inputWidgets.size()) / (getItemsPerLine() - 2));
    }
    
    public int getItemsPerLine() {
        return Mth.floor((getWidth() - 4f) / 18f);
    }
    
}
