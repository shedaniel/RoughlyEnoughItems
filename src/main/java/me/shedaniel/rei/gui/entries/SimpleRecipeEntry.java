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

package me.shedaniel.rei.gui.entries;

import com.google.common.collect.Lists;
import me.shedaniel.math.Point;
import me.shedaniel.math.api.Rectangle;
import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.api.widgets.Slot;
import me.shedaniel.rei.api.widgets.Tooltip;
import me.shedaniel.rei.api.widgets.Widgets;
import me.shedaniel.rei.utils.CollectionUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class SimpleRecipeEntry extends RecipeEntry {
    
    private static final Comparator<EntryStack> ENTRY_COMPARATOR = Comparator.comparingLong(EntryStack::hashCode);
    private static final Identifier CHEST_GUI_TEXTURE = new Identifier("roughlyenoughitems", "textures/gui/recipecontainer.png");
    private List<Slot> inputWidgets;
    private Slot outputWidget;
    
    @ApiStatus.Internal
    protected SimpleRecipeEntry(List<List<EntryStack>> input, List<EntryStack> output) {
        List<Pair<List<EntryStack>, AtomicInteger>> newList = Lists.newArrayList();
        List<Pair<List<EntryStack>, Integer>> a = CollectionUtils.map(input, stacks -> new Pair<>(stacks, stacks.stream().map(EntryStack::getAmount).max(Integer::compareTo).orElse(1)));
        for (Pair<List<EntryStack>, Integer> pair : a) {
            Pair<List<EntryStack>, AtomicInteger> any = CollectionUtils.findFirstOrNull(newList, pairr -> equalsList(pair.getLeft(), pairr.getLeft()));
            if (any != null) {
                any.getRight().addAndGet(pair.getRight());
            } else
                newList.add(new Pair<>(pair.getLeft(), new AtomicInteger(pair.getRight())));
        }
        List<List<EntryStack>> b = Lists.newArrayList();
        for (Pair<List<EntryStack>, AtomicInteger> pair : newList)
            b.add(pair.getLeft().stream().map(stack -> {
                EntryStack s = stack.copy();
                s.setAmount(pair.getRight().get());
                return s;
            }).collect(Collectors.toList()));
        this.inputWidgets = b.stream().filter(stacks -> !stacks.isEmpty()).map(stacks -> Widgets.createSlot(new Point(0, 0)).entries(stacks).disableBackground().disableHighlight().disableTooltips()).collect(Collectors.toList());
        this.outputWidget = Widgets.createSlot(new Point(0, 0)).entries(CollectionUtils.filter(output, stack -> !stack.isEmpty())).disableBackground().disableHighlight().disableTooltips();
    }
    
    public static RecipeEntry create(Supplier<List<List<EntryStack>>> input, Supplier<List<EntryStack>> output) {
        return create(input.get(), output.get());
    }
    
    public static RecipeEntry create(List<List<EntryStack>> input, List<EntryStack> output) {
        return new SimpleRecipeEntry(input, output);
    }
    
    public static boolean equalsList(List<EntryStack> list_1, List<EntryStack> list_2) {
        List<EntryStack> stacks_1 = list_1.stream().distinct().sorted(ENTRY_COMPARATOR).collect(Collectors.toList());
        List<EntryStack> stacks_2 = list_2.stream().distinct().sorted(ENTRY_COMPARATOR).collect(Collectors.toList());
        if (stacks_1.equals(stacks_2))
            return true;
        if (stacks_1.size() != stacks_2.size())
            return false;
        for (int i = 0; i < stacks_1.size(); i++)
            if (!stacks_1.get(i).equalsIgnoreTagsAndAmount(stacks_2.get(i)))
                return false;
        return true;
    }
    
    @Override
    public void render(Rectangle bounds, int mouseX, int mouseY, float delta) {
        int xx = bounds.x + 4, yy = bounds.y + 2;
        int j = 0;
        int itemsPerLine = getItemsPerLine();
        for (Slot entryWidget : inputWidgets) {
            entryWidget.setZ(getZ() + 50);
            entryWidget.getBounds().setLocation(xx, yy);
            entryWidget.render(mouseX, mouseY, delta);
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
        MinecraftClient.getInstance().getTextureManager().bindTexture(CHEST_GUI_TEXTURE);
        blit(xx, yy, 0, 28, 18, 18);
        xx += 18;
        outputWidget.setZ(getZ() + 50);
        outputWidget.getBounds().setLocation(xx, yy);
        outputWidget.render(mouseX, mouseY, delta);
    }
    
    @Nullable
    @Override
    public Tooltip getTooltip(Point point) {
        for (Slot widget : inputWidgets) {
            if (widget.containsMouse(point))
                return widget.getCurrentTooltip(point);
        }
        if (outputWidget.containsMouse(point))
            return outputWidget.getCurrentTooltip(point);
        return null;
    }
    
    @Override
    public int getHeight() {
        return 4 + getItemsHeight() * 18;
    }
    
    public int getItemsHeight() {
        return MathHelper.ceil(((float) inputWidgets.size()) / (getItemsPerLine() - 2));
    }
    
    public int getItemsPerLine() {
        return MathHelper.floor((getWidth() - 4f) / 18f);
    }
    
}
