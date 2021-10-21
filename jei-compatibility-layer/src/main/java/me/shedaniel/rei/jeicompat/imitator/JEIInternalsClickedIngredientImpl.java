/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020, 2021 shedaniel
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

import me.shedaniel.architectury.fluid.FluidStack;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class JEIInternalsClickedIngredientImpl<V> implements JEIInternalsClickedIngredient<V> {
    private final V value;
    private final Rect2i area;
    
    public JEIInternalsClickedIngredientImpl(V value, Rect2i area) {
        this.value = value;
        this.area = area;
    }
    
    public static <V> JEIInternalsClickedIngredient<V> create(V value, Rect2i area) {
        return new JEIInternalsClickedIngredientImpl<>(value, area);
    }
    
    @Override
    public V getValue() {
        return value;
    }
    
    @Override
    @Nullable
    public Rect2i getArea() {
        return area;
    }
    
    @Override
    public ItemStack getCheatItemStack() {
        if (value instanceof FluidStack) {
            FluidStack value = (FluidStack) getValue();
            Item bucketItem = value.getFluid().getBucket();
            if (bucketItem != null) {
                return new ItemStack(bucketItem);
            }
        } else if (value instanceof ItemStack) {
            return (ItemStack) value;
        }
        return null;
    }
}
