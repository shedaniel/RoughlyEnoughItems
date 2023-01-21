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

package me.shedaniel.rei.plugin.common.displays;

import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.basic.BasicDisplay;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import me.shedaniel.rei.plugin.common.BuiltinPlugin;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.ApiStatus;

import java.util.Arrays;
import java.util.List;

public class DefaultCompostingDisplay extends BasicDisplay {
    private static int pages;

    private final int page;

    @Deprecated(forRemoval = true)
    public static DefaultCompostingDisplay of(List<Object2FloatMap.Entry<ItemLike>> inputs, List<EntryIngredient> output, int page) {
        EntryIngredient[] inputIngredients = new EntryIngredient[inputs.size()];
        int i = 0;
        for (Object2FloatMap.Entry<ItemLike> entry : inputs) {
            inputIngredients[i] = EntryIngredients.of(entry.getKey());
            i++;
        }
        return new DefaultCompostingDisplay(Arrays.asList(inputIngredients), output, page);
    }

    @ApiStatus.Internal
    public DefaultCompostingDisplay(List<EntryIngredient> inputs, List<EntryIngredient> outputs, CompoundTag tag) {
        this(inputs, outputs, tag.getInt("page"));
    }

    public DefaultCompostingDisplay(List<EntryIngredient> inputs, List<EntryIngredient> outputs) {
        super(inputs, outputs);
        this.page = pages++;
    }

    @ApiStatus.Internal
    public DefaultCompostingDisplay(List<EntryIngredient> inputs, List<EntryIngredient> outputs, int page) {
        super(inputs, outputs);
        this.page = page;
        if (pages <= page) pages = page + 1;
    }
    
    public int getPage() {
        return page;
    }

    public static int getPages() {
        return pages;
    }
    
    @Override
    public CategoryIdentifier<?> getCategoryIdentifier() {
        return BuiltinPlugin.COMPOSTING;
    }
    
    public static BasicDisplay.Serializer<DefaultCompostingDisplay> serializer() {
        return BasicDisplay.Serializer.ofRecipeLess(DefaultCompostingDisplay::new, (display, tag) -> {
            tag.putInt("page", display.page);
        });
    }
}
