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

package me.shedaniel.rei.jeicompat.wrap;

import com.mojang.blaze3d.vertex.PoseStack;
import lombok.experimental.ExtensionMethod;
import me.shedaniel.math.Rectangle;
import me.shedaniel.math.impl.PointHelper;
import me.shedaniel.rei.api.client.gui.widgets.Panel;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.jeicompat.JEIPluginDetector;
import mezz.jei.api.gui.ITickTimer;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.drawable.IDrawableBuilder;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.ICraftingGridHelper;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredientType;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

@ExtensionMethod(JEIPluginDetector.class)
public enum JEIGuiHelper implements IGuiHelper {
    INSTANCE;
    
    @Override
    @NotNull
    public IDrawableBuilder drawableBuilder(@NotNull ResourceLocation resourceLocation, int u, int v, int width, int height) {
        return new JEIDrawableBuilder(resourceLocation, u, v, width, height);
    }
    
    @Override
    @NotNull
    public IDrawableAnimated createAnimatedDrawable(@NotNull IDrawableStatic drawable, int ticksPerCycle, @NotNull IDrawableAnimated.StartDirection startDirection, boolean inverted) {
        if (inverted) {
            if (startDirection == IDrawableAnimated.StartDirection.LEFT)
                startDirection = IDrawableAnimated.StartDirection.RIGHT;
            else if (startDirection == IDrawableAnimated.StartDirection.RIGHT)
                startDirection = IDrawableAnimated.StartDirection.LEFT;
            else if (startDirection == IDrawableAnimated.StartDirection.TOP)
                startDirection = IDrawableAnimated.StartDirection.BOTTOM;
            else if (startDirection == IDrawableAnimated.StartDirection.BOTTOM)
                startDirection = IDrawableAnimated.StartDirection.TOP;
        }
        
        int maxValue = startDirection == IDrawableAnimated.StartDirection.TOP || startDirection == IDrawableAnimated.StartDirection.BOTTOM ? drawable.getHeight() : drawable.getWidth();
        return createAnimatedDrawable(drawable, createTickTimer(ticksPerCycle, maxValue, !inverted), startDirection);
    }
    
    @NotNull
    public IDrawableAnimated createAnimatedDrawable(@NotNull IDrawableStatic drawable, ITickTimer timer, @NotNull IDrawableAnimated.StartDirection startDirection) {
        return new IDrawableAnimated() {
            @Override
            public int getWidth() {
                return drawable.getWidth();
            }
            
            @Override
            public int getHeight() {
                return drawable.getHeight();
            }
            
            @Override
            public void draw(@NotNull PoseStack matrixStack, int xOffset, int yOffset) {
                int maskTop = 0, maskBottom = 0, maskLeft = 0, maskRight = 0;
                switch (startDirection) {
                    case TOP:
                        maskBottom = timer.getValue();
                        break;
                    case BOTTOM:
                        maskTop = timer.getValue();
                        break;
                    case LEFT:
                        maskRight = timer.getValue();
                        break;
                    case RIGHT:
                        maskLeft = timer.getValue();
                        break;
                }
                drawable.draw(matrixStack, xOffset, yOffset, maskTop, maskBottom, maskLeft, maskRight);
            }
        };
    }
    
    @Override
    @NotNull
    public IDrawableStatic getSlotDrawable() {
        Panel base = Widgets.createSlotBase(new Rectangle(0, 0, 18, 18));
        return new IDrawableStatic() {
            @Override
            public void draw(@NotNull PoseStack matrixStack, int xOffset, int yOffset, int maskTop, int maskBottom, int maskLeft, int maskRight) {
                base.getBounds().setLocation(xOffset, yOffset);
                base.render(matrixStack, PointHelper.getMouseX(), PointHelper.getMouseY(), 0);
            }
            
            @Override
            public void draw(@NotNull PoseStack matrixStack, int xOffset, int yOffset) {
                draw(matrixStack, xOffset, yOffset, 0, 0, 0, 0);
            }
            
            @Override
            public int getWidth() {
                return base.getBounds().getWidth();
            }
            
            @Override
            public int getHeight() {
                return base.getBounds().getHeight();
            }
        };
    }
    
    @Override
    @NotNull
    public IDrawableStatic createBlankDrawable(int width, int height) {
        return new IDrawableStatic() {
            @Override
            public void draw(@NotNull PoseStack matrixStack, int xOffset, int yOffset, int maskTop, int maskBottom, int maskLeft, int maskRight) {
                
            }
            
            @Override
            public void draw(@NotNull PoseStack matrixStack, int xOffset, int yOffset) {
                
            }
            
            @Override
            public int getWidth() {
                return width;
            }
            
            @Override
            public int getHeight() {
                return height;
            }
        };
    }
    
    @Override
    public <V> IDrawable createDrawableIngredient(IIngredientType<V> type, V ingredient) {
        EntryStack<?> stack = ingredient.unwrapStack(type);
        return new IDrawable() {
            @Override
            public int getWidth() {
                return 16;
            }
            
            @Override
            public int getHeight() {
                return 16;
            }
            
            @Override
            public void draw(@NotNull PoseStack matrixStack, int xOffset, int yOffset) {
                stack.render(matrixStack, new Rectangle(xOffset, yOffset, getWidth(), getHeight()), PointHelper.getMouseX(), PointHelper.getMouseY(), 0);
            }
        };
    }
    
    @Override
    public ICraftingGridHelper createCraftingGridHelper() {
        return JEICraftingGridHelper.INSTANCE;
    }
    
    @Override
    @NotNull
    public ITickTimer createTickTimer(int ticksPerCycle, int maxValue, boolean countDown) {
        return new ITickTimer() {
            @Override
            public int getValue() {
                float value = (System.currentTimeMillis() % (ticksPerCycle * 50L)) / (ticksPerCycle * 50F);
                if (countDown) value = 1 - value;
                return Math.round(value * getMaxValue());
            }
            
            @Override
            public int getMaxValue() {
                return maxValue;
            }
        };
    }
}
