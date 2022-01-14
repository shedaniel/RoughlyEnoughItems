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

package me.shedaniel.rei.api.common.transfer.info.simple;

import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.display.DisplaySerializerRegistry;
import me.shedaniel.rei.api.common.transfer.info.MenuInfo;
import me.shedaniel.rei.api.common.transfer.info.MenuInfoProvider;
import me.shedaniel.rei.api.common.transfer.info.MenuSerializationContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Function;

/**
 * A simple implementation of {@link MenuInfoProvider} that provides a {@link Display} for a {@link AbstractContainerMenu}.
 *
 * @param <T> the type of the menu
 * @param <D> the type of display
 */
public interface SimpleMenuInfoProvider<T extends AbstractContainerMenu, D extends Display> extends MenuInfoProvider<T, D> {
    static <T extends AbstractContainerMenu, D extends Display> SimpleMenuInfoProvider<T, D> of(Function<D, @Nullable MenuInfo<T, D>> provider) {
        return provider::apply;
    }
    
    @Override
    default Optional<MenuInfo<T, D>> provideClient(D display, MenuSerializationContext<T, ?, D> context, T menu) {
        return Optional.ofNullable(create(display));
    }
    
    @Override
    default Optional<MenuInfo<T, D>> provide(CategoryIdentifier<D> category, T menu, MenuSerializationContext<T, ?, D> context, CompoundTag networkTag) {
        D display = read(category, menu, context, networkTag);
        if (display == null) return Optional.empty();
        return Optional.ofNullable(create(display));
    }
    
    @Nullable
    MenuInfo<T, D> create(D display);
    
    @Nullable
    default D read(CategoryIdentifier<D> category, T menu, MenuSerializationContext<T, ?, D> context, CompoundTag networkTag) {
        if (DisplaySerializerRegistry.getInstance().hasSerializer(category)) {
            return DisplaySerializerRegistry.getInstance().read(category, networkTag);
        } else {
            return null;
        }
    }
}
