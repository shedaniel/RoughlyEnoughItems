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

import me.shedaniel.rei.api.EntryRegistry;
import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.api.plugins.REIPluginV0;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.LogManager;
import org.jetbrains.annotations.TestOnly;

import java.util.Collections;
import java.util.Random;

@TestOnly
public class REITestPlugin implements REIPluginV0 {
    
    private Random random = new Random();
    
    @Override
    public void preRegister() {
        LogManager.getLogger().error("REI Test Plugin is enabled! If you see this unintentionally, please report this!");
    }
    
    @Override
    public Identifier getPluginIdentifier() {
        return new Identifier("roughlyenoughitems:test_dev_plugin");
    }
    
    @Override
    public void registerEntries(EntryRegistry entryRegistry) {
        int times = 100;
        for (Item item : Registry.ITEM) {
            for (int i = 0; i < times; i++)
                entryRegistry.queueRegisterEntryAfter(EntryStack.create(item), Collections.singleton(transformStack(EntryStack.create(item))));
            try {
                for (ItemStack stack : entryRegistry.appendStacksForItem(item)) {
                    for (int i = 0; i < times; i++)
                        entryRegistry.registerEntry(transformStack(EntryStack.create(stack)));
                }
            } catch (Exception ignored) {
            }
        }
    }
    
    public EntryStack transformStack(EntryStack stack) {
        stack.setAmount(random.nextInt(Byte.MAX_VALUE));
        stack.setting(EntryStack.Settings.CHECK_AMOUNT, EntryStack.Settings.TRUE);
        return stack;
    }
    
}
