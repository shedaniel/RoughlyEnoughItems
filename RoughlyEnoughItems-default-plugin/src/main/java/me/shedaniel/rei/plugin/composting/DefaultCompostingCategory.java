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

package me.shedaniel.rei.plugin.composting;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.api.RecipeCategory;
import me.shedaniel.rei.api.widgets.Widgets;
import me.shedaniel.rei.gui.entries.RecipeEntry;
import me.shedaniel.rei.gui.widget.Widget;
import me.shedaniel.rei.plugin.DefaultPlugin;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.block.Blocks;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@OnlyIn(Dist.CLIENT)
public class DefaultCompostingCategory implements RecipeCategory<DefaultCompostingDisplay> {
    
    @Override
    public @NotNull ResourceLocation getIdentifier() {
        return DefaultPlugin.COMPOSTING;
    }
    
    @Override
    public @NotNull EntryStack getLogo() {
        return EntryStack.create(Blocks.COMPOSTER);
    }
    
    @Override
    public @NotNull String getCategoryName() {
        return I18n.get("category.rei.composting");
    }
    
    @Override
    public @NotNull RecipeEntry getSimpleRenderer(DefaultCompostingDisplay recipe) {
        return new RecipeEntry() {
            private ITextComponent text = new TranslationTextComponent("text.rei.composting.page", recipe.getPage() + 1);
            
            @Override
            public int getHeight() {
                return 10 + Minecraft.getInstance().font.lineHeight;
            }
            
            @Override
            public void render(MatrixStack matrices, Rectangle rectangle, int mouseX, int mouseY, float delta) {
                Minecraft.getInstance().font.draw(matrices, text.getVisualOrderText(), rectangle.x + 5, rectangle.y + 6, -1);
            }
        };
    }
    
    @Override
    public @NotNull List<Widget> setupDisplay(DefaultCompostingDisplay display, Rectangle bounds) {
        List<Widget> widgets = Lists.newArrayList();
        Point startingPoint = new Point(bounds.x + bounds.width - 55, bounds.y + 110);
        List<EntryStack> stacks = new ArrayList<>(display.getRequiredEntries().get(0));
        int i = 0;
        for (int y = 0; y < 6; y++)
            for (int x = 0; x < 8; x++) {
                EntryStack[] entryStack = {stacks.size() > i ? stacks.get(i) : EntryStack.empty()};
                if (!entryStack[0].isEmpty()) {
                    display.getInputMap().entrySet().parallelStream().filter(entry -> entry.getKey() != null && Objects.equals(entry.getKey().asItem(), entryStack[0].getItem())).findAny().map(Map.Entry::getValue).ifPresent(chance -> {
                        entryStack[0] = entryStack[0].setting(EntryStack.Settings.TOOLTIP_APPEND_EXTRA, s -> Collections.singletonList(new TranslationTextComponent("text.rei.composting.chance", MathHelper.fastFloor(chance * 100)).withStyle(TextFormatting.YELLOW)));
                    });
                }
                widgets.add(Widgets.createSlot(new Point(bounds.getCenterX() - 72 + x * 18, bounds.y + 3 + y * 18)).entry(entryStack[0]).markInput());
                i++;
            }
        widgets.add(Widgets.createArrow(new Point(startingPoint.x - 1, startingPoint.y + 7)));
        widgets.add(Widgets.createResultSlotBackground(new Point(startingPoint.x + 33, startingPoint.y + 8)));
        widgets.add(Widgets.createSlot(new Point(startingPoint.x + 33, startingPoint.y + 8)).entries(display.getResultingEntries().get(0)).disableBackground().markOutput());
        return widgets;
    }
    
    @Override
    public int getDisplayHeight() {
        return 140;
    }
    
    @Override
    public int getFixedRecipesPerPage() {
        return 1;
    }
}