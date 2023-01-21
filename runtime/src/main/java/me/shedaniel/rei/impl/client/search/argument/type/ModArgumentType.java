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

package me.shedaniel.rei.impl.client.search.argument.type;

import me.shedaniel.rei.api.client.ClientHelper;
import me.shedaniel.rei.api.client.config.ConfigObject;
import me.shedaniel.rei.api.client.gui.config.SearchMode;
import me.shedaniel.rei.api.common.entry.EntryStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.Unit;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

@ApiStatus.Internal
@Environment(EnvType.CLIENT)
public final class ModArgumentType extends ArgumentType<Unit, ModArgumentType.@Nullable ModInfoPair> {
    public static final ModArgumentType INSTANCE = new ModArgumentType();
    private static final Style STYLE = Style.EMPTY.withColor(TextColor.fromRgb(0xffa8f3));
    
    @Override
    public String getName() {
        return "mod";
    }
    
    @Override
    @Nullable
    public String getPrefix() {
        return "@";
    }
    
    @Override
    public SearchMode getSearchMode() {
        return ConfigObject.getInstance().getModSearchMode();
    }
    
    @Override
    @Nullable
    public ModInfoPair cacheData(EntryStack<?> stack) {
        String containingNs = stack.getContainingNamespace();
        return containingNs != null ? new ModInfoPair(
                containingNs,
                null
        ) : ModInfoPair.EMPTY;
    }
    
    @Override
    public void matches(@Nullable ModInfoPair pair, EntryStack<?> stack, Unit filterData, ResultSink sink) {
        if (pair.modId == null || sink.testString(pair.modId)) return;
        if (pair.modName == null) {
            pair.modName = ClientHelper.getInstance().getModFromModId(pair.modId).toLowerCase(Locale.ROOT);
        }
        sink.testString(pair.modName);
    }
    
    @Override
    public Unit prepareSearchFilter(String searchText) {
        return Unit.INSTANCE;
    }
    
    @Override
    public Style getHighlightedStyle() {
        return STYLE;
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
