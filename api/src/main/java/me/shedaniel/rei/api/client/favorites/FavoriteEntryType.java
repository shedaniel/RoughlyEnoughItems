/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020, 2021, 2022 shedaniel
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

package me.shedaniel.rei.api.client.favorites;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.Lifecycle;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.plugins.PluginManager;
import me.shedaniel.rei.api.common.registry.Reloadable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Environment(EnvType.CLIENT)
public interface FavoriteEntryType<T extends FavoriteEntry> {
    /**
     * A builtin type of favorites, wrapping a {@link EntryStack}.
     */
    ResourceLocation ENTRY_STACK = new ResourceLocation("roughlyenoughitems", "entry_stack");
    
    static Registry registry() {
        return PluginManager.getClientInstance().get(FavoriteEntryType.Registry.class);
    }
    
    @ApiStatus.ScheduledForRemoval
    @Deprecated
    default T read(CompoundTag object) {
        throw new UnsupportedOperationException("Implementation of FavoriteEntryType must override #read or #readResult");
    }
    
    default DataResult<T> readResult(CompoundTag object) {
        return DataResult.success(read(object), Lifecycle.stable());
    }
    
    @ApiStatus.ScheduledForRemoval
    @Deprecated
    default T fromArgs(Object... args) {
        throw new UnsupportedOperationException("Implementation of FavoriteEntryType must override #fromArgs or #fromArgsResult");
    }
    
    default DataResult<T> fromArgsResult(Object... args) {
        return DataResult.success(fromArgs(args), Lifecycle.stable());
    }
    
    CompoundTag save(T entry, CompoundTag tag);
    
    @ApiStatus.NonExtendable
    interface Registry extends Reloadable<REIClientPlugin> {
        void register(ResourceLocation id, FavoriteEntryType<?> type);
        
        @Nullable
        <A extends FavoriteEntry> FavoriteEntryType<A> get(ResourceLocation id);
        
        @Nullable
        ResourceLocation getId(FavoriteEntryType<?> type);
        
        Section getOrCrateSection(Component text);
        
        Iterable<Section> sections();
        
        /**
         * @deprecated {@link FavoriteEntryType.Section#add(boolean, FavoriteEntry...)}
         */
        @Deprecated(forRemoval = true)
        <A extends FavoriteEntry> void registerSystemFavorites(SystemFavoriteEntryProvider<A> provider);
    }
    
    @ApiStatus.NonExtendable
    interface Section {
        default void add(FavoriteEntry... entries) {
            add(false, entries);
        }
    
        @ApiStatus.Experimental
        void add(boolean defaultFavorited, FavoriteEntry... entries);
        
        Component getText();
        
        List<FavoriteEntry> getEntries();
        
        @ApiStatus.Experimental
        List<FavoriteEntry> getDefaultEntries();
    }
}
