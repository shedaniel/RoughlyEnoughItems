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

package me.shedaniel.rei.impl.client.favorites;

import com.mojang.serialization.DataResult;
import me.shedaniel.rei.api.client.favorites.FavoriteEntry;
import me.shedaniel.rei.api.client.favorites.FavoriteMenuEntry;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.drag.component.DraggableComponent;
import me.shedaniel.rei.api.client.gui.widgets.Slot;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

public class DelegatingFavoriteEntryProviderImpl extends FavoriteEntry {
    private final Supplier<DataResult<FavoriteEntry>> supplier;
    private final Supplier<CompoundTag> toJson;
    private FavoriteEntry value = null;
    
    public DelegatingFavoriteEntryProviderImpl(Supplier<DataResult<FavoriteEntry>> supplier, Supplier<CompoundTag> toJson) {
        this.supplier = supplier;
        this.toJson = toJson;
    }
    
    @Override
    public FavoriteEntry getUnwrapped() {
        synchronized (this) {
            if (this.value == null) {
                DataResult<FavoriteEntry> result = supplier.get();
                this.value = result.getOrThrow(false, error -> {});
            }
        }
        return Objects.requireNonNull(value).getUnwrapped();
    }
    
    @Override
    public UUID getUuid() {
        return getUnwrapped().getUuid();
    }
    
    @Override
    public boolean isInvalid() {
        try {
            return getUnwrapped().isInvalid();
        } catch (Exception e) {
            return true;
        }
    }
    
    @Override
    public Renderer getRenderer(boolean showcase) {
        return getUnwrapped().getRenderer(showcase);
    }
    
    @Override
    public boolean doAction(int button) {
        return getUnwrapped().doAction(button);
    }
    
    @Override
    public Optional<Supplier<Collection<FavoriteMenuEntry>>> getMenuEntries() {
        return getUnwrapped().getMenuEntries();
    }
    
    @Override
    public long hashIgnoreAmount() {
        return getUnwrapped().hashIgnoreAmount();
    }
    
    @Override
    public FavoriteEntry copy() {
        if (value != null && toJson != null) return value.copy();
        if (value != null) return FavoriteEntry.delegateResult(() -> DataResult.success(value.copy()), toJson);
        return FavoriteEntry.delegateResult(supplier, toJson);
    }
    
    @Override
    public ResourceLocation getType() {
        return getUnwrapped().getType();
    }
    
    @Override
    public CompoundTag save(CompoundTag tag) {
        if (toJson == null) {
            return getUnwrapped().save(tag);
        }
        
        return tag.merge(toJson.get());
    }
    
    @Override
    public boolean isSame(FavoriteEntry other) {
        return getUnwrapped().isSame(other.getUnwrapped());
    }
    
    @Override
    @Nullable
    public DraggableComponent<?> asDraggableComponent(Slot slot) {
        return getUnwrapped().asDraggableComponent(slot);
    }
}
