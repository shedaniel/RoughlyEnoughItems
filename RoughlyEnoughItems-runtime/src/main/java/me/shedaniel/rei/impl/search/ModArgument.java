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

import me.shedaniel.rei.api.ClientHelper;
import me.shedaniel.rei.api.EntryStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

@ApiStatus.Internal
@Environment(EnvType.CLIENT)
public final class ModArgument extends Argument {
    public static final ModArgument INSTANCE = new ModArgument();
    
    @Override
    public String getName() {
        return "mod";
    }
    
    @Override
    public @Nullable String getPrefix() {
        return "@";
    }
    
    @Override
    public boolean matches(Object[] data, EntryStack stack, String searchText, Object searchData) {
        if (data[getDataOrdinal()] == null) {
            data[getDataOrdinal()] = new String[]{
                    stack.getIdentifier().map(ResourceLocation::getNamespace).orElse("").toLowerCase(Locale.ROOT),
                    null
            };
        }
        String[] strings = (String[]) data[getDataOrdinal()];
        if (strings[0].isEmpty() || strings[0].contains(searchText)) return true;
        if (strings[1] == null) {
            strings[1] = ClientHelper.getInstance().getModFromModId(strings[0]).toLowerCase(Locale.ROOT);
        }
        return strings[1].isEmpty() || strings[1].contains(searchText);
    }
    
    private ModArgument() {
    }
}
