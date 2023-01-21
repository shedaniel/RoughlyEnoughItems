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

package me.shedaniel.rei.api.common.transfer.info;

import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.transfer.info.simple.SimpleMenuInfoProvider;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.inventory.AbstractContainerMenu;

import java.util.Optional;

/**
 * A provider of {@link MenuInfo}, to provide info conditionally, or dynamically.
 *
 * @param <T> the type of the menu
 * @param <D> the type of display
 * @see SimpleMenuInfoProvider
 */
public interface MenuInfoProvider<T extends AbstractContainerMenu, D extends Display> {
    @Environment(EnvType.CLIENT)
    Optional<MenuInfo<T, D>> provideClient(D display, MenuSerializationContext<T, ?, D> context, T menu);
    
    Optional<MenuInfo<T, D>> provide(CategoryIdentifier<D> category, T menu, MenuSerializationContext<T, ?, D> context, CompoundTag networkTag);
}
