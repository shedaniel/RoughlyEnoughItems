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

package me.shedaniel.rei.impl.init.versions;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.comparison.ComparisonContext;
import me.shedaniel.rei.api.common.entry.type.EntryDefinition;
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes;
import me.shedaniel.rei.impl.VersionAdapter;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class Version1_19Adapter implements VersionAdapter {
    @Override
    @Environment(EnvType.CLIENT)
    public List<ItemStack> appendStacksForItem(Item item, Comparator<ItemStack> comparator) {
        NonNullList<ItemStack> list = NonNullList.create();
        LongSet set = new LongOpenHashSet();
        EntryDefinition<ItemStack> itemDefinition = VanillaEntryTypes.ITEM.getDefinition();
        for (CreativeModeTab tab : CreativeModeTab.TABS) {
            if (tab != CreativeModeTab.TAB_HOTBAR && tab != CreativeModeTab.TAB_INVENTORY) {
                NonNullList<ItemStack> tabList = NonNullList.create();
                item.fillItemCategory(tab, tabList);
                for (ItemStack stack : tabList) {
                    if (set.add(itemDefinition.hash(null, stack, ComparisonContext.EXACT))) {
                        list.add(stack);
                    }
                }
            }
        }
        if (list.isEmpty()) {
            return Collections.singletonList(item.getDefaultInstance());
        }
        if (list.size() > 1) {
            list.sort(comparator);
        }
        return list;
    }
    
    @Override
    @Environment(EnvType.CLIENT)
    public ResourceLocation spriteAtlasLocation(TextureAtlasSprite sprite) {
        return sprite.atlas().location();
    }
    
    @Override
    @Environment(EnvType.CLIENT)
    public ResourceLocation spriteName(TextureAtlasSprite sprite) {
        return sprite.getName();
    }
    
    @Override
    @Environment(EnvType.CLIENT)
    public int spriteWidth(TextureAtlasSprite sprite) {
        return sprite.getWidth();
    }
    
    @Override
    @Environment(EnvType.CLIENT)
    public int spriteHeight(TextureAtlasSprite sprite) {
        return sprite.getHeight();
    }
    
    @Override
    public <T> Optional<Holder<T>> getHolder(Registry<T> registry, ResourceKey<T> key) {
        return registry.getHolder(key);
    }
    
    @Override
    public <T> Optional<Holder<T>> getHolder(Registry<T> registry, int id) {
        return registry.getHolder(id);
    }
    
    @Override
    @Environment(EnvType.CLIENT)
    public void sendCommand(String command) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;
        player.command(command);
    }
    
    @Override
    @Environment(EnvType.CLIENT)
    public Comparator<? super EntryStack<?>> getEntryGroupComparator() {
        return Comparator.comparingInt(stack -> {
            if (stack.getType() == VanillaEntryTypes.ITEM) {
                CreativeModeTab group = ((ItemStack) stack.getValue()).getItem().getItemCategory();
                if (group != null)
                    return group.getId();
            }
            return Integer.MAX_VALUE;
        });
    }
}
