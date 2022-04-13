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

package me.shedaniel.rei.plugin.test;

import com.google.common.collect.ImmutableList;
import me.shedaniel.rei.api.client.favorites.FavoriteEntry;
import me.shedaniel.rei.api.client.favorites.FavoriteEntryType;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.entry.EntryRegistry;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.comparison.ItemComparatorRegistry;
import me.shedaniel.rei.api.common.plugins.PluginManager;
import me.shedaniel.rei.api.common.registry.ReloadStage;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.shedaniel.rei.impl.common.InternalLogger;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import org.jetbrains.annotations.TestOnly;

import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@TestOnly
@Environment(EnvType.CLIENT)
public class REITestPlugin implements REIClientPlugin {
    private Random random = new Random();
    
    @Override
    public void preStage(PluginManager<REIClientPlugin> manager, ReloadStage stage) {
        InternalLogger.getInstance().error("REI Test Plugin is enabled! If you see this unintentionally, please report this!");
    }
    
    @Override
    public void registerEntries(EntryRegistry registry) {
        int times = 100;
        for (Item item : Registry.ITEM) {
            EntryStack<ItemStack> base = EntryStacks.of(item);
            registry.addEntriesAfter(base, IntStream.range(0, times).mapToObj(value -> transformStack(EntryStacks.of(item))).collect(Collectors.toList()));
            try {
                for (ItemStack stack : registry.appendStacksForItem(item)) {
                    registry.addEntries(IntStream.range(0, times).mapToObj(value -> transformStack(EntryStacks.of(stack))).collect(Collectors.toList()));
                }
            } catch (Exception ignored) {
            }
        }
    }
    
    @Override
    public void registerItemComparators(ItemComparatorRegistry registry) {
        registry.registerNbt(Registry.ITEM.stream().toArray(Item[]::new));
    }
    
    public EntryStack<ItemStack> transformStack(EntryStack<ItemStack> stack) {
        CompoundTag tag = stack.getValue().getOrCreateTag();
        tag.putInt("Whatever", random.nextInt(Integer.MAX_VALUE));
        return stack;
    }
    
    @Override
    public void registerFavorites(FavoriteEntryType.Registry registry) {
        registry.registerSystemFavorites(() -> {
            GameType mode = Minecraft.getInstance().gameMode.getPlayerMode();
            switch (mode) {
                case SURVIVAL:
                    return ImmutableList.of(FavoriteEntry.fromEntryStack(EntryStacks.of(Items.STONE)));
                case CREATIVE:
                    return ImmutableList.of(FavoriteEntry.fromEntryStack(EntryStacks.of(Items.PACKED_ICE)));
                case ADVENTURE:
                    return ImmutableList.of(FavoriteEntry.fromEntryStack(EntryStacks.of(Items.ANVIL)));
                case SPECTATOR:
                default:
                    return ImmutableList.of();
            }
        });
    }
}
