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

import me.shedaniel.rei.api.ingredient.EntryStack;
import me.shedaniel.rei.api.ingredient.entry.EntryDefinition;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Unit;
import org.apache.commons.lang3.mutable.Mutable;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

@ApiStatus.Internal
@Environment(EnvType.CLIENT)
public final class TagArgument extends Argument<Unit, String[]> {
    public static final TagArgument INSTANCE = new TagArgument();
    private static final Minecraft minecraft = Minecraft.getInstance();
    private static final String[] EMPTY_ARRAY = new String[0];
    private static final Style STYLE = Style.EMPTY.withColor(TextColor.fromRgb(0x9efff4));
    
    @Override
    public String getName() {
        return "tag";
    }
    
    @Override
    public @Nullable String getPrefix() {
        return "$";
    }
    
    @Override
    public @NotNull Style getHighlightedStyle() {
        return STYLE;
    }
    
    @Override
    public boolean matches(Mutable<String[]> data, EntryStack<?> stack, String searchText, Unit filterData) {
        if (data.getValue() == null) {
            Collection<ResourceLocation> tags = ((EntryDefinition<Object>) stack.getDefinition()).getTagsFor((EntryStack<Object>) stack, stack.getValue());
            if (tags.isEmpty()) {
                data.setValue(EMPTY_ARRAY);
            } else {
                data.setValue(new String[tags.size()]);
                int i = 0;
                
                for (ResourceLocation identifier : tags) {
                    data.getValue()[i] = identifier.toString();
                    i++;
                }
            }
        }
        for (String tag : data.getValue()) {
            if (tag.isEmpty() || tag.contains(searchText)) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public Unit prepareSearchFilter(String searchText) {
        return Unit.INSTANCE;
    }
    
    private TagArgument() {
    }
}
