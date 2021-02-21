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

import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import me.shedaniel.rei.api.ingredient.EntryStack;
import me.shedaniel.rei.api.registry.display.Display;
import me.shedaniel.rei.api.ingredient.util.EntryStacks;
import me.shedaniel.rei.plugin.DefaultPlugin;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Environment(EnvType.CLIENT)
public class DefaultCompostingDisplay implements Display {
    private List<List<EntryStack<?>>> inputs;
    private Object2FloatMap<ItemLike> inputMap;
    private List<List<EntryStack<?>>> output;
    private int page;
    
    public DefaultCompostingDisplay(int page, List<Object2FloatMap.Entry<ItemLike>> inputs, Object2FloatMap<ItemLike> map, ItemStack output) {
        this.page = page;
        {
            List<EntryStack<?>>[] result = new List[inputs.size()];
            int i = 0;
            for (Object2FloatMap.Entry<ItemLike> entry : inputs) {
                result[i] = Collections.singletonList(EntryStacks.of(entry.getKey()));
                i++;
            }
            this.inputs = Arrays.asList(result);
        }
        this.inputMap = map;
        this.output = Collections.singletonList(Collections.singletonList(EntryStacks.of(output)));
    }
    
    public int getPage() {
        return page;
    }
    
    @Override
    public @NotNull List<? extends List<? extends EntryStack<?>>> getInputEntries() {
        return inputs;
    }
    
    public Object2FloatMap<ItemLike> getInputMap() {
        return inputMap;
    }
    
    @Override
    public @NotNull List<? extends List<? extends EntryStack<?>>> getResultingEntries() {
        return output;
    }
    
    @Override
    public @NotNull ResourceLocation getCategoryIdentifier() {
        return DefaultPlugin.COMPOSTING;
    }
    
    @Override
    public @NotNull List<? extends List<? extends EntryStack<?>>> getRequiredEntries() {
        return inputs;
    }
}
