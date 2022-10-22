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

package me.shedaniel.rei.impl;

import me.shedaniel.rei.api.client.registry.entry.EntryRegistry;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@ApiStatus.Internal
public interface VersionAdapter {
    VersionAdapter INSTANCE = load();
    
    private static VersionAdapter load() {
        try {
            Class.forName("me.shedaniel.rei.impl.init.versions.Version1_19_3AdapterCheck");
            return (VersionAdapter) Class.forName("me.shedaniel.rei.impl.init.versions.Version1_19_3Adapter").getDeclaredConstructor().newInstance();
        } catch (Throwable throwable) {
        }
        try {
            Class.forName("me.shedaniel.rei.impl.init.versions.Version1_19_1AdapterCheck");
            return (VersionAdapter) Class.forName("me.shedaniel.rei.impl.init.versions.Version1_19_1Adapter").getDeclaredConstructor().newInstance();
        } catch (Throwable throwable) {
        }
        try {
            return (VersionAdapter) Class.forName("me.shedaniel.rei.impl.init.versions.Version1_19Adapter").getDeclaredConstructor().newInstance();
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }
    
    @Environment(EnvType.CLIENT)
    default void registerDefaultItems(EntryRegistry registry) {
        for (Item item : Registry.ITEM) {
            try {
                registry.addEntries(EntryIngredients.ofItemStacks(registry.appendStacksForItem(item)));
            } catch (Exception ignored) {
                registry.addEntry(EntryStacks.of(item));
            }
        }
    }
    
    @Environment(EnvType.CLIENT)
    List<ItemStack> appendStacksForItem(Item item, Comparator<ItemStack> comparator);
    
    @Environment(EnvType.CLIENT)
    ResourceLocation spriteAtlasLocation(TextureAtlasSprite sprite);
    
    @Environment(EnvType.CLIENT)
    ResourceLocation spriteName(TextureAtlasSprite sprite);
    
    @Environment(EnvType.CLIENT)
    int spriteWidth(TextureAtlasSprite sprite);
    
    @Environment(EnvType.CLIENT)
    int spriteHeight(TextureAtlasSprite sprite);
    
    <T> Optional<Holder<T>> getHolder(Registry<T> registry, ResourceKey<T> key);
    
    <T> Optional<Holder<T>> getHolder(Registry<T> registry, int id);
    
    @Environment(EnvType.CLIENT)
    void sendCommand(String command);
    
    @Environment(EnvType.CLIENT)
    Comparator<? super EntryStack<?>> getEntryGroupComparator();
}
