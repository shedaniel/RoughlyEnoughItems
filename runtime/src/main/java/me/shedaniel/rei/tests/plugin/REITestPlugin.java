/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020 shedaniel
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

package me.shedaniel.rei.tests.plugin;

import me.shedaniel.rei.api.plugins.REIPlugin;
import me.shedaniel.rei.api.registry.entry.EntryRegistry;
import me.shedaniel.rei.api.ingredient.EntryStack;
import me.shedaniel.rei.api.ingredient.util.EntryStacks;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.LogManager;
import org.jetbrains.annotations.TestOnly;

import java.util.Random;

@TestOnly
@Environment(EnvType.CLIENT)
public class REITestPlugin implements REIPlugin {
    
    private Random random = new Random();
    
    @Override
    public void preRegister() {
        LogManager.getLogger().error("REI Test Plugin is enabled! If you see this unintentionally, please report this!");
    }
    
    @Override
    public void registerEntries(EntryRegistry registry) {
        int times = 10;
        for (Item item : Registry.ITEM) {
            for (int i = 0; i < times; i++)
                registry.registerEntryAfter(EntryStacks.of(item), transformStack(EntryStacks.of(item)));
            try {
                for (ItemStack stack : registry.appendStacksForItem(item)) {
                    for (int i = 0; i < times; i++)
                        registry.registerEntry(transformStack(EntryStacks.of(stack)));
                }
            } catch (Exception ignored) {
            }
        }
    }
    
    public EntryStack<ItemStack> transformStack(EntryStack<ItemStack> stack) {
        CompoundTag tag = stack.getValue().getOrCreateTag();
        tag.putInt("Whatever", random.nextInt(Integer.MAX_VALUE));
        return stack;
    }
    
}
