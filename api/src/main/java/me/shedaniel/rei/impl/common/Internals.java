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

package me.shedaniel.rei.impl.common;

import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.entry.comparison.EntryComparator;
import me.shedaniel.rei.api.common.entry.type.EntryType;
import me.shedaniel.rei.api.common.plugins.PluginManager;
import me.shedaniel.rei.api.common.plugins.PluginView;
import me.shedaniel.rei.api.common.plugins.REIPlugin;
import me.shedaniel.rei.api.common.plugins.REIServerPlugin;
import me.shedaniel.rei.impl.common.provider.*;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;

import java.lang.reflect.Field;
import java.util.List;
import java.util.ServiceLoader;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

@ApiStatus.Internal
public final class Internals {
    private static final EntryStackProvider ENTRY_STACK_PROVIDER = resolveService(EntryStackProvider.class);
    private static final EntryIngredientProvider ENTRY_INGREDIENT_PROVIDER = resolveService(EntryIngredientProvider.class);
    private static final DeferringEntryTypeProvider ENTRY_TYPE_DEFERRED = resolveService(DeferringEntryTypeProvider.class);
    private static final PluginManagerConstructor PLUGIN_MANAGER_CONSTRUCTOR = resolveService(PluginManagerConstructor.class);
    private static final PluginManager<REIPlugin<?>> COMMON_PLUGIN_MANAGER = createPluginManager(
            (Class<REIPlugin<?>>) (Class<?>) REIPlugin.class,
            UnaryOperator.identity());
    private static final PluginManager<REIServerPlugin> SERVER_PLUGIN_MANAGER = createPluginManager(
            REIServerPlugin.class,
            view -> view.then(COMMON_PLUGIN_MANAGER.view()));
    private static final NbtHasherProvider NBT_HASHER_PROVIDER = resolveService(NbtHasherProvider.class);
    private static final CategoryIdentifierConstructor CATEGORY_IDENTIFIER_CONSTRUCTOR = resolveService(CategoryIdentifierConstructor.class);
    private static Supplier<InternalLogger> logger = Internals::throwNotSetup;
    private static Runnable reloadREI = Internals::throwNotSetup;
    
    private static <T> T throwNotSetup() {
        throw new AssertionError("REI Internals have not been initialized!");
    }
    
    public static <T> T resolveService(Class<T> serviceClass) {
        ServiceLoader<T> loader = ServiceLoader.load(serviceClass);
        List<ServiceLoader.Provider<T>> providers = loader.stream().toList();
        if (providers.isEmpty()) {
            throw new IllegalArgumentException("No service providers found for class " + serviceClass.getName());
        } else if (providers.size() > 1) {
            throw new IllegalArgumentException("Multiple service providers found for class " + serviceClass.getName() + ": " +
                                               providers.stream().map(provider -> provider.type().getName())
                                                       .collect(Collectors.joining(", ")));
        } else {
            return providers.get(0).get();
        }
    }
    
    public static <T> List<T> resolveServices(Class<T> serviceClass) {
        ServiceLoader<T> loader = ServiceLoader.load(serviceClass);
        return loader.stream().map(ServiceLoader.Provider::get).toList();
    }
    
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
        return ENTRY_STACK_PROVIDER;
    }
    
    public static EntryIngredientProvider getEntryIngredientProvider() {
        return ENTRY_INGREDIENT_PROVIDER;
    }
    
    public static EntryType<?> deferEntryType(ResourceLocation id) {
        return ENTRY_TYPE_DEFERRED.get(id);
    }
    
    public static <P extends REIPlugin<?>> PluginManager<P> createPluginManager(Class<P> clazz, UnaryOperator<PluginView<P>> constructor) {
        return PLUGIN_MANAGER_CONSTRUCTOR.create(clazz, constructor);
    }
    
    public static PluginManager<REIPlugin<?>> getPluginManager() {
        return COMMON_PLUGIN_MANAGER;
    }
    
    public static PluginManager<REIServerPlugin> getServerPluginManager() {
        return SERVER_PLUGIN_MANAGER;
    }
    
    public static EntryComparator<Tag> getNbtHasher(String[] ignoredKeys) {
        return NBT_HASHER_PROVIDER.provide(ignoredKeys);
    }
    
    public static <T extends Display> CategoryIdentifier<T> getCategoryIdentifier(String location) {
        return CATEGORY_IDENTIFIER_CONSTRUCTOR.create(location);
    }
    
    public static InternalLogger getInternalLogger() {
        return logger.get();
    }
    
    public static void reloadREI() {
        reloadREI.run();
    }
}
