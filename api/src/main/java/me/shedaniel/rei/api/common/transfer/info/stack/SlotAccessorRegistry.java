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

package me.shedaniel.rei.api.common.transfer.info.stack;

import me.shedaniel.rei.api.common.plugins.PluginManager;
import me.shedaniel.rei.api.common.plugins.REIServerPlugin;
import me.shedaniel.rei.api.common.registry.Reloadable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

@ApiStatus.Experimental
public interface SlotAccessorRegistry extends Reloadable<REIServerPlugin> {
    /**
     * @return the instance of {@link SlotAccessorRegistry}
     */
    static SlotAccessorRegistry getInstance() {
        return PluginManager.getServerInstance().get(SlotAccessorRegistry.class);
    }
    
    void register(ResourceLocation id, Predicate<SlotAccessor> accessorPredicate, Serializer serializer);
    
    @Nullable
    Serializer get(ResourceLocation id);
    
    CompoundTag save(AbstractContainerMenu menu, Player player, SlotAccessor accessor);
    
    SlotAccessor read(AbstractContainerMenu menu, Player player, CompoundTag tag);
    
    interface Serializer {
        SlotAccessor read(AbstractContainerMenu menu, Player player, CompoundTag tag);
        
        @Nullable
        CompoundTag save(AbstractContainerMenu menu, Player player, SlotAccessor accessor);
    }
}
