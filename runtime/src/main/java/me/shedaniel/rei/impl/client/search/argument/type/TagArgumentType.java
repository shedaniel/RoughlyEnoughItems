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

import me.shedaniel.rei.api.client.config.ConfigObject;
import me.shedaniel.rei.api.client.gui.config.SearchMode;
import me.shedaniel.rei.api.common.entry.EntryStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Unit;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.stream.Stream;

@ApiStatus.Internal
@Environment(EnvType.CLIENT)
public final class TagArgumentType extends ArgumentType<Unit, String[]> {
    private static final String[] EMPTY_ARRAY = new String[0];
    private static final Style STYLE = Style.EMPTY.withColor(TextColor.fromRgb(0x9efff4));
    
    @Override
    public String getName() {
        return "tag";
    }
    
    @Override
    @Nullable
    public String getPrefix() {
        return "$";
    }
    
    @Override
    public Style getHighlightedStyle() {
        return STYLE;
    }
    
    @Override
    public SearchMode getSearchMode() {
        return ConfigObject.getInstance().getTagSearchMode();
    }
    
    @Override
    public String[] cacheData(EntryStack<?> stack) {
        Stream<TagKey<?>> tags = stack.getTagsFor();
        String[] array = tags.map(TagArgumentType::toString).toArray(String[]::new);
        return array.length == 0 ? EMPTY_ARRAY : array;
    }
    
    @Override
    public void matches(String[] data, EntryStack<?> stack, Unit filterData, ResultSink sink) {
        for (String tag : data) {
            if (!tag.isEmpty() && sink.testString(tag)) {
                return;
            }
        }
    }
    
    @Override
    public Unit prepareSearchFilter(String searchText) {
        return Unit.INSTANCE;
    }
    
    private static String toString(TagKey<?> tagKey) {
        return Objects.toString(tagKey.location());
    }
}
