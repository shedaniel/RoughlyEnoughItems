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

package me.shedaniel.rei.impl.search;

import me.shedaniel.architectury.fluid.FluidStack;
import me.shedaniel.rei.api.EntryStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

@ApiStatus.Internal
@Environment(EnvType.CLIENT)
public final class TagArgument extends Argument {
    public static final TagArgument INSTANCE = new TagArgument();
    private static final Minecraft minecraft = Minecraft.getInstance();
    
    @Override
    public String getName() {
        return "tag";
    }
    
    @Override
    public @Nullable String getPrefix() {
        return "$";
    }
    
    @Override
    public boolean matches(Object[] data, EntryStack<?> stack, String searchText, Object searchData) {
        if (data[getDataOrdinal()] == null) {
            if (stack.getValueType() == ItemStack.class) {
                Collection<ResourceLocation> tagsFor = minecraft.getConnection().getTags().getItems().getMatchingTags(((ItemStack) stack.getValue()).getItem());
                data[getDataOrdinal()] = new String[tagsFor.size()];
                int i = 0;
                
                for (ResourceLocation identifier : tagsFor) {
                    ((String[]) data[getDataOrdinal()])[i] = identifier.toString();
                    i++;
                }
            } else if (stack.getValueType() == FluidStack.class) {
                Collection<ResourceLocation> tagsFor = minecraft.getConnection().getTags().getFluids().getMatchingTags(((FluidStack) stack.getValue()).getFluid());
                data[getDataOrdinal()] = new String[tagsFor.size()];
                int i = 0;
                
                for (ResourceLocation identifier : tagsFor) {
                    ((String[]) data[getDataOrdinal()])[i] = identifier.toString();
                    i++;
                }
            } else
                data[getDataOrdinal()] = new String[0];
        }
        String[] tags = (String[]) data[getDataOrdinal()];
        for (String tag : tags)
            if (tag.isEmpty() || tag.contains(searchText))
                return true;
        return false;
    }
    
    private TagArgument() {
    }
}
