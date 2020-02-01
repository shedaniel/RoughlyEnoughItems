/*
 * Copyright (c) 2018, 2019, 2020 shedaniel
 * Licensed under the MIT License (the "License").
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
        for (Item item : Registry.ITEM) {
            for (int i = 0; i < 5; i++)
                entryRegistry.queueRegisterEntryAfter(EntryStack.create(item), Collections.singleton(transformStack(EntryStack.create(item))));
            try {
                for (ItemStack stack : entryRegistry.appendStacksForItem(item)) {
                    for (int i = 0; i < 15; i++)
                        entryRegistry.registerEntry(transformStack(EntryStack.create(stack)));
                }
            } catch (Exception ignored) {
            }
        }
    }
    
    public EntryStack transformStack(EntryStack stack) {
        stack.setAmount(random.nextInt(Integer.MAX_VALUE));
        return stack;
    }
    
}
