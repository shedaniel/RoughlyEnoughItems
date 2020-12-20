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

package me.shedaniel.rei.api.favorites;

import com.google.gson.JsonObject;
import me.shedaniel.rei.impl.Internals;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface FavoriteEntryType<T extends FavoriteEntry> {
    ResourceLocation ENTRY_STACK = new ResourceLocation("roughlyenoughitems", "entry_stack");
    
    @NotNull
    static Registry registry() {
        return Internals.getFavoriteEntryTypeRegistry();
    }
    
    @NotNull
    T fromJson(@NotNull JsonObject object);
    
    @NotNull
    T fromArgs(Object... args);
    
    @NotNull
    JsonObject toJson(@NotNull T entry, @NotNull JsonObject object);
    
    @ApiStatus.NonExtendable
    interface Registry {
        void register(ResourceLocation id, FavoriteEntryType<?> type);
        
        @Nullable <A extends FavoriteEntry> FavoriteEntryType<A> get(ResourceLocation id);
        
        @Nullable
        ResourceLocation getId(FavoriteEntryType<?> type);
        
        @NotNull
        Section getOrCrateSection(Component text);
        
        @NotNull
        Iterable<Section> sections();
    }
    
    @ApiStatus.NonExtendable
    interface Section {
        void add(@NotNull FavoriteEntry... entries);
        
        @NotNull
        Component getText();
        
        @NotNull
        List<FavoriteEntry> getEntries();
    }
}
