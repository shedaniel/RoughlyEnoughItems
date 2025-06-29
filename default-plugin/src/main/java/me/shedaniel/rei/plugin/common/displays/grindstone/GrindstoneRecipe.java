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

package me.shedaniel.rei.plugin.common.displays.grindstone;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.OptionalDouble;

public class GrindstoneRecipe {
    @Nullable
    private final ResourceLocation id;
    private final List<ItemStack> topInput;
    private final List<ItemStack> bottomInput;
    private final List<ItemStack> outputs;
    private final OptionalDouble averageXpReward;

    public GrindstoneRecipe(@Nullable ResourceLocation id, List<ItemStack> topInput, List<ItemStack> bottomInput, List<ItemStack> outputs) {
        this(id, topInput, bottomInput, outputs, OptionalDouble.empty());
    }

    public GrindstoneRecipe(@Nullable ResourceLocation id, List<ItemStack> topInput, List<ItemStack> bottomInput, List<ItemStack> outputs, OptionalDouble averageXpReward) {
        this.id = id;
        this.topInput = topInput;
        this.bottomInput = bottomInput;
        this.outputs = outputs;
        this.averageXpReward = averageXpReward;
    }

    public @Nullable ResourceLocation getId() {
        return id;
    }

    public List<ItemStack> getTopInput() {
        return topInput;
    }

    public List<ItemStack> getBottomInput() {
        return bottomInput;
    }

    public List<ItemStack> getOutputs() {
        return outputs;
    }

    public OptionalDouble getAverageXpReward() {
        return averageXpReward;
    }
}
