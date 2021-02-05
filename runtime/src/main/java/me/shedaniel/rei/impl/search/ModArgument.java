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
import me.shedaniel.rei.api.ingredient.EntryStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Unit;
import org.apache.commons.lang3.mutable.Mutable;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Optional;

@ApiStatus.Internal
@Environment(EnvType.CLIENT)
public final class ModArgument extends Argument<Unit, ModArgument.@Nullable ModInfoPair> {
    public static final ModArgument INSTANCE = new ModArgument();
    private static final Style STYLE = Style.EMPTY.withColor(TextColor.fromRgb(0xffa8f3));
    
    @Override
    public String getName() {
        return "mod";
    }
    
    @Override
    public @Nullable String getPrefix() {
        return "@";
    }
    
    @Override
    public boolean matches(Mutable<@Nullable ModInfoPair> data, EntryStack<?> stack, String searchText, Unit filterData) {
        if (data.getValue() == null) {
            Optional<ResourceLocation> id = stack.getIdentifier();
            data.setValue(id.isPresent() ? new ModInfoPair(
                    id.get().getNamespace(),
                    null
            ) : ModInfoPair.EMPTY);
        }
        ModInfoPair pair = data.getValue();
        if (pair.modId == null || pair.modId.contains(searchText)) return true;
        if (pair.modName == null) {
            pair.modName = ClientHelper.getInstance().getModFromModId(pair.modId).toLowerCase(Locale.ROOT);
        }
        return pair.modName.isEmpty() || pair.modName.contains(searchText);
    }
    
    @Override
    public Unit prepareSearchFilter(String searchText) {
        return Unit.INSTANCE;
    }
    
    @Override
    public @NotNull Style getHighlightedStyle() {
        return STYLE;
    }
    
    private ModArgument() {
    }
    
    protected static class ModInfoPair {
        private static final ModInfoPair EMPTY = new ModInfoPair(null, null);
        @Nullable
        private final String modId;
        @Nullable
        private String modName;
        
        public ModInfoPair(@Nullable String modId, @Nullable String modName) {
            this.modId = modId;
            this.modName = modName;
        }
    }
}
