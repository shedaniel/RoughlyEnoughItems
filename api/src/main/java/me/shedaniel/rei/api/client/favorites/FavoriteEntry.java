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
import me.shedaniel.rei.api.client.entry.region.RegionEntry;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.util.ClientEntryStacks;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.impl.ClientInternals;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

@Environment(EnvType.CLIENT)
public abstract class FavoriteEntry implements RegionEntry<FavoriteEntry> {
    public static final String TYPE_KEY = "type";
    private final UUID uuid = UUID.randomUUID();
    
    public static FavoriteEntry delegate(Supplier<FavoriteEntry> supplier, @Nullable Supplier<CompoundTag> toJson) {
        return delegateResult(() -> DataResult.success(supplier.get(), Lifecycle.stable()), toJson);
    }
    
    public static FavoriteEntry delegateResult(Supplier<DataResult<FavoriteEntry>> supplier, @Nullable Supplier<CompoundTag> toJson) {
        return ClientInternals.delegateFavoriteEntry(supplier, toJson);
    }
    
    @Nullable
    public static FavoriteEntry read(CompoundTag object) {
        return readResult(object).result().orElse(null);
    }
    
    public static DataResult<FavoriteEntry> readResult(CompoundTag object) {
        return ClientInternals.favoriteEntryFromJson(object);
    }
    
    public static FavoriteEntry readDelegated(CompoundTag object) {
        CompoundTag copy = object.copy();
        return delegateResult(() -> readResult(object), () -> copy);
    }
    
    public static FavoriteEntry fromEntryStack(EntryStack<?> stack) {
        return delegateResult(() -> FavoriteEntryType.registry().get(FavoriteEntryType.ENTRY_STACK).fromArgs(stack), null);
    }
    
    @Override
    public boolean isEntryInvalid() {
        return isInvalid();
    }
    
    public CompoundTag save(CompoundTag tag) {
        tag.putString(TYPE_KEY, getType().toString());
        return Objects.requireNonNull(Objects.requireNonNull(FavoriteEntryType.registry().get(getType())).save(this, tag));
    }
    
    @Override
    public UUID getUuid() {
        return uuid;
    }
    
    public abstract boolean isInvalid();
    
    public abstract Renderer getRenderer(boolean showcase);
    
    @Override
    public abstract boolean doAction(int button);
    
    @Override
    public Optional<Supplier<Collection<FavoriteMenuEntry>>> getMenuEntries() {
        return Optional.empty();
    }
    
    public abstract long hashIgnoreAmount();
    
    @Override
    public abstract FavoriteEntry copy();
    
    public abstract ResourceLocation getType();
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FavoriteEntry that)) return false;
        try {
            FavoriteEntry unwrapped = getUnwrapped();
            FavoriteEntry thatUnwrapped = that.getUnwrapped();
            return unwrapped == thatUnwrapped || unwrapped.isSame(thatUnwrapped);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            return false;
        }
    }
    
    @Override
    public int hashCode() {
        int result = 1;
        result = 31 * result + getType().hashCode();
        result = 31 * result + Long.hashCode(hashIgnoreAmount());
        return result;
    }
    
    public abstract boolean isSame(FavoriteEntry other);
    
    public FavoriteEntry getUnwrapped() {
        return this;
    }
    
    @Override
    public EntryStack<?> toStack() {
        return ClientEntryStacks.of(getRenderer(false));
    }
    
    @Override
    public FavoriteEntry asFavorite() {
        return copy();
    }
}
