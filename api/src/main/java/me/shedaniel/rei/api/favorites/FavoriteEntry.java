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
import me.shedaniel.rei.api.gui.Renderer;
import me.shedaniel.rei.api.ingredient.EntryStack;
import me.shedaniel.rei.impl.Internals;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

public abstract class FavoriteEntry {
    public static final String TYPE_KEY = "type";
    private final UUID uuid = UUID.randomUUID();
    
    @NotNull
    public static FavoriteEntry delegate(@NotNull Supplier<FavoriteEntry> supplier, @Nullable Supplier<JsonObject> toJson) {
        return Internals.delegateFavoriteEntry(supplier, toJson);
    }
    
    @Nullable
    public static FavoriteEntry fromJson(@NotNull JsonObject object) {
        return Internals.favoriteEntryFromJson(object);
    }
    
    @NotNull
    public static FavoriteEntry fromEntryStack(@NotNull EntryStack<?> stack) {
        return delegate(() -> FavoriteEntryType.registry().get(FavoriteEntryType.ENTRY_STACK).fromArgs(stack), null);
    }
    
    public static boolean isEntryInvalid(@Nullable FavoriteEntry entry) {
        return entry == null || entry.isInvalid();
    }
    
    @NotNull
    public JsonObject toJson(@NotNull JsonObject object) {
        object.addProperty(TYPE_KEY, getType().toString());
        return Objects.requireNonNull(Objects.requireNonNull(FavoriteEntryType.registry().get(getType())).toJson(this, object));
    }
    
    public UUID getUuid() {
        return uuid;
    }
    
    public abstract boolean isInvalid();
    
    public abstract Renderer getRenderer(boolean showcase);
    
    public abstract boolean doAction(int button);
    
    @NotNull
    public Optional<Supplier<Collection<@NotNull FavoriteMenuEntry>>> getMenuEntries() {
        return Optional.empty();
    }
    
    public abstract int hashIgnoreAmount();
    
    public abstract FavoriteEntry copy();
    
    public abstract ResourceLocation getType();
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FavoriteEntry)) return false;
        FavoriteEntry that = (FavoriteEntry) o;
        FavoriteEntry unwrapped = getUnwrapped();
        FavoriteEntry thatUnwrapped = that.getUnwrapped();
        return unwrapped == thatUnwrapped || unwrapped.isSame(thatUnwrapped);
    }
    
    @Override
    public int hashCode() {
        return hashIgnoreAmount();
    }
    
    public abstract boolean isSame(FavoriteEntry other);
    
    public FavoriteEntry getUnwrapped() {
        return this;
    }
}
