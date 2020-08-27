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

package me.shedaniel.rei.plugin.stripping;

import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.api.RecipeDisplay;
import me.shedaniel.rei.plugin.DefaultPlugin;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

@Environment(EnvType.CLIENT)
public class DefaultStrippingDisplay implements RecipeDisplay {
    private EntryStack in, out;
    
    public DefaultStrippingDisplay(EntryStack in, EntryStack out) {
        this.in = in;
        this.out = out;
    }
    
    public DefaultStrippingDisplay(ItemStack in, ItemStack out) {
        this.in = EntryStack.create(in);
        this.out = EntryStack.create(out);
    }
    
    public final EntryStack getIn() {
        return in;
    }
    
    public final EntryStack getOut() {
        return out;
    }
    
    @Override
    public @NotNull List<List<EntryStack>> getInputEntries() {
        return Collections.singletonList(Collections.singletonList(in));
    }
    
    @Override
    public @NotNull List<List<EntryStack>> getResultingEntries() {
        return Collections.singletonList(Collections.singletonList(out));
    }
    
    @Override
    public @NotNull ResourceLocation getRecipeCategory() {
        return DefaultPlugin.STRIPPING;
    }
    
    @Override
    public @NotNull List<List<EntryStack>> getRequiredEntries() {
        return getInputEntries();
    }
}
