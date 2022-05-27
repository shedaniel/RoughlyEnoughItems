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

package me.shedaniel.rei.jeicompat.imitator;

import com.mojang.blaze3d.vertex.PoseStack;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.math.impl.PointHelper;
import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import me.shedaniel.rei.api.client.gui.widgets.TooltipContext;
import me.shedaniel.rei.api.common.util.CollectionUtils;
import me.shedaniel.rei.api.common.util.EntryStacks;
import mezz.jei.api.ingredients.IIngredientRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraftforge.client.RenderProperties;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class JEIItemStackRendererImitator implements IIngredientRenderer<ItemStack> {
    @Override
    public List<Component> getTooltip(ItemStack ingredient, TooltipFlag tooltipFlag) {
        Tooltip tooltip = EntryStacks.of(ingredient).getTooltip(TooltipContext.of(new Point(0, 0)));
        if (tooltip == null) return new ArrayList<>();
        return CollectionUtils.filterAndMap(tooltip.entries(), Tooltip.Entry::isText, Tooltip.Entry::getAsText);
    }
    
    @Override
    public void render(PoseStack stack, int xPosition, int yPosition, @Nullable ItemStack ingredient) {
        if (ingredient == null) return;
        EntryStacks.of(ingredient).render(stack, new Rectangle(xPosition, yPosition, getWidth(), getHeight()), PointHelper.getMouseX(), PointHelper.getMouseY(), Minecraft.getInstance().getDeltaFrameTime());
    }
    
    @Override
    public Font getFontRenderer(Minecraft minecraft, ItemStack ingredient) {
        Font font = RenderProperties.get(ingredient).getFont(ingredient);
        if (font == null) return minecraft.font;
        return font;
    }
}
