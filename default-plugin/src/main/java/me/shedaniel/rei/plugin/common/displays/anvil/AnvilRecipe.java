/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020, 2021, 2022, 2023 shedaniel
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

package me.shedaniel.rei.plugin.common.displays.anvil;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class AnvilRecipe {
    @Nullable
    private final ResourceLocation id;
    private final List<ItemStack> leftInput;
    private final List<ItemStack> rightInputs;
    private final List<ItemStack> outputs;
    
    public AnvilRecipe(@Nullable ResourceLocation id, List<ItemStack> leftInput, List<ItemStack> rightInputs, List<ItemStack> outputs) {
        this.id = id;
        this.leftInput = leftInput;
        this.rightInputs = rightInputs;
        this.outputs = outputs;
    }
    
    public ResourceLocation getId() {
        return id;
    }
    
    public List<ItemStack> getLeftInput() {
        return leftInput;
    }
    
    public List<ItemStack> getRightInputs() {
        return rightInputs;
    }
    
    public List<ItemStack> getOutputs() {
        return outputs;
    }
}
