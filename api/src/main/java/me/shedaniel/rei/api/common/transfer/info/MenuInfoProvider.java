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
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.ApiStatus;

import java.util.Optional;

/**
 * A provider of {@link MenuInfo}, to provide info conditionally, or dynamically.
 *
 * @param <T> the type of the menu
 * @param <D> the type of display
 */
@FunctionalInterface
public interface MenuInfoProvider<T extends AbstractContainerMenu, D extends Display> {
    @Environment(EnvType.CLIENT)
    default Optional<MenuInfo<T, D>> provideClient(D display, T menu) {
        return provide((CategoryIdentifier<D>) display.getCategoryIdentifier(), (Class<T>) menu.getClass());
    }
    
    default Optional<MenuInfo<T, D>> provide(CategoryIdentifier<D> display, T menu, MenuSerializationProviderContext<T, ?, D> context, CompoundTag networkTag) {
        Optional<MenuInfo<T, D>> menuInfo = provide(display, (Class<T>) menu.getClass());
        if (menuInfo.isPresent()) {
            menuInfo.get().read(new MenuSerializationContext<T, Player, D>() {
                @Override
                public MenuInfo<T, D> getContainerInfo() {
                    return menuInfo.get();
                }
                
                @Override
                public T getMenu() {
                    return context.getMenu();
                }
                
                @Override
                public Player getPlayerEntity() {
                    return context.getPlayerEntity();
                }
                
                @Override
                public CategoryIdentifier<D> getCategoryIdentifier() {
                    return context.getCategoryIdentifier();
                }
            }, networkTag);
        }
        return menuInfo;
    }
    
    @Deprecated
    @ApiStatus.ScheduledForRemoval
    Optional<MenuInfo<T, D>> provide(CategoryIdentifier<D> categoryId, Class<T> menuClass);
}
