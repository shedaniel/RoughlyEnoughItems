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

import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.comparison.EntryComparator;
import me.shedaniel.rei.api.common.entry.type.EntryDefinition;
import me.shedaniel.rei.api.common.entry.type.EntryType;
import me.shedaniel.rei.api.common.plugins.PluginManager;
import me.shedaniel.rei.api.common.plugins.REIPlugin;
import me.shedaniel.rei.api.common.plugins.REIServerPlugin;
import me.shedaniel.rei.api.common.transfer.info.MenuInfoRegistry;
import me.shedaniel.rei.impl.common.InternalLogger;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Unit;
import org.jetbrains.annotations.ApiStatus;

import java.lang.reflect.Field;
import java.util.function.Function;
import java.util.function.Supplier;

@ApiStatus.Internal
public final class Internals {
    private static Supplier<EntryStackProvider> entryStackProvider = Internals::throwNotSetup;
    private static Supplier<EntryIngredientProvider> entryIngredientProvider = Internals::throwNotSetup;
    private static Function<ResourceLocation, EntryType<?>> entryTypeDeferred = (object) -> throwNotSetup();
    private static Supplier<PluginManager<REIPlugin<?>>> commonPluginManager = Internals::throwNotSetup;
    private static Supplier<PluginManager<REIServerPlugin>> serverPluginManager = Internals::throwNotSetup;
    private static Supplier<NbtHasherProvider> nbtHasherProvider = Internals::throwNotSetup;
    private static Function<String, CategoryIdentifier<?>> categoryIdentifier = (object) -> throwNotSetup();
    private static Supplier<MenuInfoRegistry> stubMenuInfoRegistry = Internals::throwNotSetup;
    private static Supplier<InternalLogger> logger = Internals::throwNotSetup;
    
    private static <T> T throwNotSetup() {
        throw new AssertionError("REI Internals have not been initialized!");
    }
    
    @ApiStatus.Internal
    public static <T> void attachInstance(T instance, Class<T> clazz) {
        attachInstanceSupplier(instance, clazz.getSimpleName());
    }
    
    @ApiStatus.Internal
    public static <T> void attachInstanceSupplier(T instance, String name) {
        attachInstance((Supplier<T>) () -> instance, name);
    }
    
    public static <T> void attachInstance(T instance, String name) {
        try {
            for (Field field : Internals.class.getDeclaredFields()) {
                if (field.getName().equalsIgnoreCase(name)) {
                    field.setAccessible(true);
                    field.set(null, instance);
                    return;
                }
            }
            throw new RuntimeException("Failed to attach " + instance + " with field name: " + name);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
    
    public static EntryStackProvider getEntryStackProvider() {
        return entryStackProvider.get();
    }
    
    public static EntryIngredientProvider getEntryIngredientProvider() {
        return entryIngredientProvider.get();
    }
    
    public static EntryType<?> deferEntryType(ResourceLocation id) {
        return entryTypeDeferred.apply(id);
    }
    
    public static PluginManager<REIPlugin<?>> getPluginManager() {
        return commonPluginManager.get();
    }
    
    public static PluginManager<REIServerPlugin> getServerPluginManager() {
        return serverPluginManager.get();
    }
    
    public static EntryComparator<Tag> getNbtHasher(String[] ignoredKeys) {
        return nbtHasherProvider.get().provide(ignoredKeys);
    }
    
    public static <T extends Display> CategoryIdentifier<T> getCategoryIdentifier(String location) {
        return (CategoryIdentifier<T>) categoryIdentifier.apply(location);
    }
    
    public static InternalLogger getInternalLogger() {
        return logger.get();
    }
    
    public interface EntryStackProvider {
        EntryStack<Unit> empty();
        
        <T> EntryStack<T> of(EntryDefinition<T> definition, T value);
    }
    
    public interface EntryIngredientProvider {
        EntryIngredient empty();
        
        EntryIngredient of(EntryStack<?> stack);
        
        EntryIngredient of(EntryStack<?>... stacks);
        
        EntryIngredient of(Iterable<EntryStack<?>> stacks);
        
        EntryIngredient.Builder builder();
        
        EntryIngredient.Builder builder(int initialCapacity);
    }
    
    public interface NbtHasherProvider {
        EntryComparator<Tag> provide(String... ignoredKeys);
    }
}
