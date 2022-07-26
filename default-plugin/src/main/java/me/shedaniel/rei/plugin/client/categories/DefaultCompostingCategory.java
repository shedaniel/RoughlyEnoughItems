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

package me.shedaniel.rei.plugin.client.categories;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.DisplayRenderer;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.shedaniel.rei.plugin.common.BuiltinPlugin;
import me.shedaniel.rei.plugin.common.displays.DefaultCompostingDisplay;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ComposterBlock;

import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public class DefaultCompostingCategory implements DisplayCategory<DefaultCompostingDisplay> {
    @Override
    public CategoryIdentifier<? extends DefaultCompostingDisplay> getCategoryIdentifier() {
        return BuiltinPlugin.COMPOSTING;
    }
    
    @Override
    public Renderer getIcon() {
        return EntryStacks.of(Blocks.COMPOSTER);
    }
    
    @Override
    public Component getTitle() {
        return Component.translatable("category.rei.composting");
    }
    
    @Override
    public DisplayRenderer getDisplayRenderer(DefaultCompostingDisplay display) {
        return new DisplayRenderer() {
            private Component text = Component.translatable("text.rei.composting.page", display.getPage() + 1);
            
            @Override
            public int getHeight() {
                return 10 + Minecraft.getInstance().font.lineHeight;
            }
            
            @Override
            public void render(PoseStack matrices, Rectangle rectangle, int mouseX, int mouseY, float delta) {
                Minecraft.getInstance().font.draw(matrices, text.getVisualOrderText(), rectangle.x + 5, rectangle.y + 6, -1);
            }
        };
    }
    
    @Override
    public List<Widget> setupDisplay(DefaultCompostingDisplay display, Rectangle bounds) {
        List<Widget> widgets = Lists.newArrayList();
        Point startingPoint = new Point(bounds.x + bounds.width - 55, bounds.y + 110);
        List<EntryIngredient> stacks = new ArrayList<>(display.getInputEntries());
        widgets.add(Widgets.createRecipeBase(bounds));
        int i = 0;
        for (int y = 0; y < 5; y++)
            for (int x = 0; x < 7; x++) {
                EntryIngredient entryIngredient = stacks.size() > i ? stacks.get(i) : EntryIngredient.empty();
                if (!entryIngredient.isEmpty()) {
                    ItemStack firstStack = (ItemStack) entryIngredient.get(0).getValue();
                    float chance = ComposterBlock.COMPOSTABLES.getFloat(firstStack.getItem());
                    if (chance > 0.0f) {
                        entryIngredient = entryIngredient.map(stack -> stack.copy().tooltip(Component.translatable("text.rei.composting.chance", Mth.clamp(Mth.fastFloor(chance * 100), 0, 100)).withStyle(ChatFormatting.YELLOW)));
                    }
                }
                widgets.add(Widgets.createSlot(new Point(bounds.getCenterX() - 72 + 9 + x * 18, bounds.y + 12 + y * 18)).entries(entryIngredient).markInput());
                i++;
            }
        widgets.add(Widgets.createArrow(new Point(startingPoint.x - 1 - 5, startingPoint.y + 7 - 5)));
        widgets.add(Widgets.createResultSlotBackground(new Point(startingPoint.x + 33 - 5, startingPoint.y + 8 - 5)));
        widgets.add(Widgets.createSlot(new Point(startingPoint.x + 33 - 5, startingPoint.y + 8 - 5)).entries(display.getOutputEntries().get(0)).disableBackground().markOutput());
        return widgets;
    }
    
    @Override
    public int getDisplayHeight() {
        return 140;
    }
    
    @Override
    public int getFixedDisplaysPerPage() {
        return 1;
    }
}