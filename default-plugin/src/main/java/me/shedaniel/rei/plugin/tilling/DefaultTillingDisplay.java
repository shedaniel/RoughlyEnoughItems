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

package me.shedaniel.rei.plugin.tilling;

import me.shedaniel.rei.api.ingredient.EntryStack;
import me.shedaniel.rei.api.Display;
import me.shedaniel.rei.api.ingredient.util.EntryStacks;
import me.shedaniel.rei.plugin.DefaultPlugin;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

@Environment(EnvType.CLIENT)
public class DefaultTillingDisplay implements Display {
    private EntryStack<?> in, out;
    
    public DefaultTillingDisplay(EntryStack<?> in, EntryStack<?> out) {
        this.in = in;
        this.out = out;
    }
    
    public DefaultTillingDisplay(ItemStack in, ItemStack out) {
        this.in = EntryStacks.of(in);
        this.out = EntryStacks.of(out);
    }
    
    public final EntryStack<?> getIn() {
        return in;
    }
    
    public final EntryStack<?> getOut() {
        return out;
    }
    
    @Override
    public @NotNull List<? extends List<? extends EntryStack<?>>> getInputEntries() {
        return Collections.singletonList(Collections.singletonList(in));
    }
    
    @Override
    public @NotNull List<? extends List<? extends EntryStack<?>>> getResultingEntries() {
        return Collections.singletonList(Collections.singletonList(out));
    }
    
    @Override
    public @NotNull ResourceLocation getRecipeCategory() {
        return DefaultPlugin.TILLING;
    }
    
    @Override
    public @NotNull List<? extends List<? extends EntryStack<?>>> getRequiredEntries() {
        return getInputEntries();
    }
}
