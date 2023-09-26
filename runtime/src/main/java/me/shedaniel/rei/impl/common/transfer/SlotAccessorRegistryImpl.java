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

package me.shedaniel.rei.impl.common.transfer;

import me.shedaniel.rei.api.common.plugins.REIServerPlugin;
import me.shedaniel.rei.api.common.transfer.info.stack.SlotAccessor;
import me.shedaniel.rei.api.common.transfer.info.stack.SlotAccessorRegistry;
import me.shedaniel.rei.impl.common.InternalLogger;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

public class SlotAccessorRegistryImpl implements SlotAccessorRegistry {
    private final Map<ResourceLocation, Serializer> map = new HashMap<>();
    
    @Override
    public void startReload() {
        this.map.clear();
    }
    
    @Override
    public void endReload() {
        InternalLogger.getInstance().debug("Registered %d slot accessor serializers", map.size());
    }
    
    @Override
    public void acceptPlugin(REIServerPlugin plugin) {
        plugin.registerSlotAccessors(this);
    }
    
    @Override
    public void register(ResourceLocation id, Predicate<SlotAccessor> accessorPredicate, Serializer serializer) {
        this.map.put(id, new Serializer() {
            @Override
            public SlotAccessor read(AbstractContainerMenu menu, Player player, CompoundTag tag) {
                return serializer.read(menu, player, tag);
            }
            
            @Override
            @Nullable
            public CompoundTag save(AbstractContainerMenu menu, Player player, SlotAccessor accessor) {
                if (!accessorPredicate.test(accessor)) {
                    return null;
                }
                return serializer.save(menu, player, accessor);
            }
        });
        InternalLogger.getInstance().debug("Added slot accessor serializer: %s [%s]", serializer, id);
    }
    
    @Override
    @Nullable
    public Serializer get(ResourceLocation id) {
        return this.map.get(id);
    }
    
    @Override
    public CompoundTag save(AbstractContainerMenu menu, Player player, SlotAccessor accessor) {
        for (Map.Entry<ResourceLocation, Serializer> entry : map.entrySet()) {
            CompoundTag tag = entry.getValue().save(menu, player, accessor);
            if (tag != null) {
                tag.putString("id", entry.getKey().toString());
                return tag;
            }
        }
        return null;
    }
    
    @Override
    public SlotAccessor read(AbstractContainerMenu menu, Player player, CompoundTag tag) {
        String id = tag.getString("id");
        Serializer serializer = map.get(new ResourceLocation(id));
        if (serializer == null) {
            throw new NullPointerException("No serializer found for " + id);
        }
        return serializer.read(menu, player, tag);
    }
}
