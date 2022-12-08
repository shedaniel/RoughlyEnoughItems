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
import dev.architectury.event.events.common.CommandRegistrationEvent;
import me.shedaniel.rei.api.client.entry.filtering.FilteringRuleTypeRegistry;
import me.shedaniel.rei.api.client.entry.filtering.base.BasicFilteringRule;
import me.shedaniel.rei.api.client.favorites.FavoriteEntry;
import me.shedaniel.rei.api.client.favorites.FavoriteEntryType;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.entry.CollapsibleEntryRegistry;
import me.shedaniel.rei.api.client.registry.entry.EntryRegistry;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.comparison.ItemComparatorRegistry;
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes;
import me.shedaniel.rei.api.common.plugins.PluginManager;
import me.shedaniel.rei.api.common.registry.ReloadStage;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.shedaniel.rei.impl.common.InternalLogger;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
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
    private final Random random = new Random();
    private BasicFilteringRule.MarkDirty markDirty;
    
    public REITestPlugin() {
        CommandRegistrationEvent.EVENT.register((dispatcher, registry, selection) -> {
            dispatcher.register(Commands.literal("rei_test_reload_filtering")
                    .then(Commands.argument("item", ItemArgument.item(registry))
                            .executes(context -> {
                                BasicFilteringRule<?> basic = FilteringRuleTypeRegistry.getInstance().basic();
                                basic.hide(EntryStacks.of(context.getArgument("item", ItemInput.class).createItemStack(1, false)));
                                return 0;
                            }))
                    .executes(context -> {
                        if (this.markDirty != null) this.markDirty.markDirty();
                        return 0;
                    }));
        });
    }
    
    @Override
    public void preStage(PluginManager<REIClientPlugin> manager, ReloadStage stage) {
        InternalLogger.getInstance().error("REI Test Plugin is enabled! If you see this unintentionally, please report this!");
    }
    
    @Override
    public void registerEntries(EntryRegistry registry) {
        if (1 + 1 == 2) return;
        int times = 10;
        for (Item item : BuiltInRegistries.ITEM) {
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
    public void registerCollapsibleEntries(CollapsibleEntryRegistry registry) {
        int i = 0;
        for (Item item : BuiltInRegistries.ITEM) {
            if (i++ % 10 != 0)
                continue;
            registry.group(BuiltInRegistries.ITEM.getKey(item), Component.literal(BuiltInRegistries.ITEM.getKey(item).toString()),
                    stack -> stack.getType() == VanillaEntryTypes.ITEM && stack.<ItemStack>castValue().is(item));
        }
    }
    
    @Override
    public void registerBasicEntryFiltering(BasicFilteringRule<?> rule) {
        markDirty = rule.hide(() -> {
            EntryIngredient.Builder builder = EntryIngredient.builder();
            for (Item item : BuiltInRegistries.ITEM) {
                if (random.nextInt() % 10 == 0) {
                    builder.add(EntryStacks.of(item));
                }
            }
            return builder.build();
        });
    }
    
    @Override
    public void registerItemComparators(ItemComparatorRegistry registry) {
        registry.registerNbt(BuiltInRegistries.ITEM.stream().toArray(Item[]::new));
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
