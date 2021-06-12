/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020, 2021 shedaniel
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

package me.shedaniel.rei;

import dev.architectury.platform.Platform;
import dev.architectury.registry.ReloadListenerRegistry;
import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.comparison.FluidComparatorRegistry;
import me.shedaniel.rei.api.common.entry.comparison.ItemComparatorRegistry;
import me.shedaniel.rei.api.common.entry.type.BuiltinEntryTypes;
import me.shedaniel.rei.api.common.entry.type.EntryDefinition;
import me.shedaniel.rei.api.common.entry.type.EntryType;
import me.shedaniel.rei.api.common.entry.type.EntryTypeRegistry;
import me.shedaniel.rei.api.common.fluid.FluidSupportProvider;
import me.shedaniel.rei.api.common.plugins.PluginManager;
import me.shedaniel.rei.api.common.plugins.PluginView;
import me.shedaniel.rei.api.common.plugins.REIPlugin;
import me.shedaniel.rei.api.common.plugins.REIServerPlugin;
import me.shedaniel.rei.api.common.transfer.info.MenuInfoRegistry;
import me.shedaniel.rei.impl.Internals;
import me.shedaniel.rei.impl.client.entry.type.types.RenderingEntryDefinition;
import me.shedaniel.rei.impl.common.category.CategoryIdentifierImpl;
import me.shedaniel.rei.impl.common.display.DisplaySerializerRegistryImpl;
import me.shedaniel.rei.impl.common.entry.EmptyEntryStack;
import me.shedaniel.rei.impl.common.entry.EntryIngredientImpl;
import me.shedaniel.rei.impl.common.entry.TypedEntryStack;
import me.shedaniel.rei.impl.common.entry.comparison.FluidComparatorRegistryImpl;
import me.shedaniel.rei.impl.common.entry.comparison.ItemComparatorRegistryImpl;
import me.shedaniel.rei.impl.common.entry.comparison.NbtHasherProviderImpl;
import me.shedaniel.rei.impl.common.entry.type.EntryTypeDeferred;
import me.shedaniel.rei.impl.common.entry.type.EntryTypeRegistryImpl;
import me.shedaniel.rei.impl.common.entry.type.types.EmptyEntryDefinition;
import me.shedaniel.rei.impl.common.fluid.FluidSupportProviderImpl;
import me.shedaniel.rei.impl.common.plugins.PluginManagerImpl;
import me.shedaniel.rei.impl.common.registry.RecipeManagerContextImpl;
import me.shedaniel.rei.impl.common.transfer.MenuInfoRegistryImpl;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.util.Unit;
import org.apache.commons.lang3.mutable.MutableLong;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.UnaryOperator;

@ApiStatus.Internal
public class RoughlyEnoughItemsCore {
    @ApiStatus.Internal
    public static final Logger LOGGER = LogManager.getFormatterLogger("REI");
    
    static {
        attachCommonInternals();
        if (Platform.getEnvironment() == Env.CLIENT) {
            EnvExecutor.runInEnv(Env.CLIENT, () -> RoughlyEnoughItemsCoreClient::attachClientInternals);
        }
    }
    
    public static void attachCommonInternals() {
        CategoryIdentifierImpl.attach();
        Internals.attachInstance((Function<ResourceLocation, EntryType<?>>) new Function<ResourceLocation, EntryType<?>>() {
            ResourceLocation RENDERING_ID = new ResourceLocation("rendering");
            private Map<ResourceLocation, EntryType<?>> typeCache = new ConcurrentHashMap<>();
            private EntryType<Unit> empty;
            @Environment(EnvType.CLIENT)
            private EntryType<Renderer> render;
            
            @Override
            public EntryType<?> apply(ResourceLocation id) {
                if (id.equals(BuiltinEntryTypes.EMPTY_ID)) {
                    return typeCache.computeIfAbsent(id, this::emptyType);
                } else if (id.equals(RENDERING_ID) && Platform.getEnv() == EnvType.CLIENT) {
                    return typeCache.computeIfAbsent(id, this::renderingType);
                }
                return typeCache.computeIfAbsent(id, EntryTypeDeferred::new);
            }
            
            public EntryType<Unit> emptyType(ResourceLocation id) {
                if (empty == null) {
                    int hashCode = id.hashCode();
                    empty = new EntryType<>() {
                        @Override
                        public ResourceLocation getId() {
                            return id;
                        }
                        
                        @Override
                        public EntryDefinition<Unit> getDefinition() {
                            return EmptyEntryDefinition.EMPTY;
                        }
                        
                        @Override
                        public int hashCode() {
                            return hashCode;
                        }
                    };
                }
                return empty;
            }
            
            @Environment(EnvType.CLIENT)
            public EntryType<Renderer> renderingType(ResourceLocation id) {
                if (render == null) {
                    int hashCode = id.hashCode();
                    render = new EntryType<>() {
                        @Override
                        public ResourceLocation getId() {
                            return id;
                        }
                        
                        @Override
                        public EntryDefinition<Renderer> getDefinition() {
                            return RenderingEntryDefinition.RENDERING;
                        }
                        
                        @Override
                        public int hashCode() {
                            return hashCode;
                        }
                    };
                }
                return render;
            }
        }, "entryTypeDeferred");
        Internals.attachInstance(new Internals.EntryStackProvider() {
            @Override
            public EntryStack<Unit> empty() {
                return EmptyEntryStack.EMPTY;
            }
            
            @Override
            public <T> EntryStack<T> of(EntryDefinition<T> definition, T value) {
                if (Objects.equals(definition.getType().getId(), BuiltinEntryTypes.EMPTY_ID)) {
                    return empty().cast();
                }
                
                return new TypedEntryStack<>(definition, value);
            }
        }, Internals.EntryStackProvider.class);
        Internals.attachInstance(new NbtHasherProviderImpl(), Internals.NbtHasherProvider.class);
        Internals.attachInstance(EntryIngredientImpl.provide(), Internals.EntryIngredientProvider.class);
        Internals.attachInstanceSupplier(new PluginManagerImpl<>(
                REIPlugin.class,
                UnaryOperator.identity(),
                usedTime -> {
                    RoughlyEnoughItemsCore.LOGGER.info("Reloaded Plugin Manager [%s] with %d entry types, %d item comparators, %d fluid comparators and %d fluid support providers in %dms.",
                            REIPlugin.class.getSimpleName(),
                            EntryTypeRegistry.getInstance().values().size(),
                            ItemComparatorRegistry.getInstance().comparatorSize(),
                            FluidComparatorRegistry.getInstance().comparatorSize(),
                            FluidSupportProvider.getInstance().size(),
                            usedTime
                    );
                },
                new EntryTypeRegistryImpl(),
                new RecipeManagerContextImpl<>(RecipeManagerContextImpl.supplier()),
                new ItemComparatorRegistryImpl(),
                new FluidComparatorRegistryImpl(),
                new DisplaySerializerRegistryImpl(),
                new FluidSupportProviderImpl()), "commonPluginManager");
        Internals.attachInstanceSupplier(new PluginManagerImpl<>(
                REIServerPlugin.class,
                view -> view.then(PluginView.getInstance()),
                usedTime -> {
                    RoughlyEnoughItemsCore.LOGGER.info("Reloaded Plugin Manager [%s] with %d menu infos in %dms.",
                            REIServerPlugin.class.getSimpleName(),
                            MenuInfoRegistry.getInstance().infoSize(),
                            usedTime
                    );
                },
                new MenuInfoRegistryImpl()), "serverPluginManager");
    }
    
    public static void _reloadPlugins() {
        try {
            for (PluginManager<? extends REIPlugin<?>> instance : PluginManager.getActiveInstances()) {
                instance.startReload();
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }
    
    public void onInitialize() {
        PluginDetector.detectCommonPlugins();
        PluginDetector.detectServerPlugins();
        RoughlyEnoughItemsNetwork.onInitialize();
        
        if (Platform.getEnvironment() == Env.SERVER) {
            MutableLong lastReload = new MutableLong(-1);
            ReloadListenerRegistry.register(PackType.SERVER_DATA, (preparationBarrier, resourceManager, profilerFiller, profilerFiller2, executor, executor2) -> {
                return preparationBarrier.wait(Unit.INSTANCE).thenRunAsync(RoughlyEnoughItemsCore::_reloadPlugins, executor2);
            });
        }
    }
}
