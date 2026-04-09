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

package me.shedaniel.rei.plugin.test;

import dev.architectury.event.events.common.CommandRegistrationEvent;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.comparison.ItemComparatorRegistry;
import me.shedaniel.rei.api.common.plugins.REICommonPlugin;
import me.shedaniel.rei.api.common.registry.display.ServerDisplayRegistry;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;

public class REITestCommonPlugin implements REICommonPlugin {
    public REITestCommonPlugin() {
        CommandRegistrationEvent.EVENT.register((dispatcher, registry, selection) -> {
            dispatcher.register(Commands.literal("rei_server_test_add_displays")
                    .then(Commands.argument("item", ItemArgument.item(registry))
                            .executes(context -> {
                                try {
                                    Class<?> displayClass = Class.forName("me.shedaniel.rei.plugin.common.displays.DefaultPathingDisplay");
                                    Item item = context.getArgument("item", ItemInput.class).item().value();
                                    Display display = (Display) displayClass.getDeclaredConstructor(EntryStack.class, EntryStack.class)
                                            .newInstance(EntryStacks.of(item), EntryStacks.of(item));
                                    ServerDisplayRegistry.getInstance().add(display);
                                } catch (Throwable throwable) {
                                    throwable.printStackTrace();
                                }
                                return 0;
                            })));
        });
    }
    
    @Override
    public void registerItemComparators(ItemComparatorRegistry registry) {
        registry.registerComponents(BuiltInRegistries.ITEM.stream().toArray(Item[]::new));
    }
}
