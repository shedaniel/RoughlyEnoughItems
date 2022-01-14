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

package me.shedaniel.rei;

import com.google.common.collect.ImmutableList;
import dev.architectury.platform.Platform;
import dev.architectury.registry.ReloadListenerRegistry;
import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.annotation.Nullable;
import me.shedaniel.rei.api.common.entry.comparison.FluidComparatorRegistry;
import me.shedaniel.rei.api.common.entry.comparison.ItemComparatorRegistry;
import me.shedaniel.rei.api.common.entry.type.EntryType;
import me.shedaniel.rei.api.common.entry.type.EntryTypeRegistry;
import me.shedaniel.rei.api.common.fluid.FluidSupportProvider;
import me.shedaniel.rei.api.common.plugins.PluginManager;
import me.shedaniel.rei.api.common.plugins.PluginView;
import me.shedaniel.rei.api.common.plugins.REIPlugin;
import me.shedaniel.rei.api.common.plugins.REIServerPlugin;
import me.shedaniel.rei.api.common.registry.ReloadStage;
import me.shedaniel.rei.api.common.transfer.info.MenuInfoRegistry;
import me.shedaniel.rei.impl.Internals;
import me.shedaniel.rei.impl.common.category.CategoryIdentifierImpl;
import me.shedaniel.rei.impl.common.display.DisplaySerializerRegistryImpl;
import me.shedaniel.rei.impl.common.entry.DeferringEntryTypeProviderImpl;
import me.shedaniel.rei.impl.common.entry.EntryIngredientImpl;
import me.shedaniel.rei.impl.common.entry.EntryStackProviderImpl;
import me.shedaniel.rei.impl.common.entry.comparison.FluidComparatorRegistryImpl;
import me.shedaniel.rei.impl.common.entry.comparison.ItemComparatorRegistryImpl;
import me.shedaniel.rei.impl.common.entry.comparison.NbtHasherProviderImpl;
import me.shedaniel.rei.impl.common.entry.settings.EntrySettingsAdapterRegistryImpl;
import me.shedaniel.rei.impl.common.entry.type.EntryTypeRegistryImpl;
import me.shedaniel.rei.impl.common.fluid.FluidSupportProviderImpl;
import me.shedaniel.rei.impl.common.logging.FileLogger;
import me.shedaniel.rei.impl.common.logging.Log4JLogger;
import me.shedaniel.rei.impl.common.logging.Logger;
import me.shedaniel.rei.impl.common.logging.MultiLogger;
import me.shedaniel.rei.impl.common.logging.performance.PerformanceLogger;
import me.shedaniel.rei.impl.common.logging.performance.PerformanceLoggerImpl;
import me.shedaniel.rei.impl.common.plugins.PluginManagerImpl;
import me.shedaniel.rei.impl.common.registry.RecipeManagerContextImpl;
import me.shedaniel.rei.impl.common.transfer.MenuInfoRegistryImpl;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.util.Unit;
import org.apache.commons.lang3.mutable.MutableLong;
import org.apache.logging.log4j.LogManager;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.Function;
import java.util.function.UnaryOperator;

@ApiStatus.Internal
public class RoughlyEnoughItemsCore {
    @ApiStatus.Internal
    public static final Logger LOGGER = new MultiLogger(ImmutableList.of(
            new FileLogger(Platform.getGameFolder().resolve("logs/rei.log")),
            new Log4JLogger(LogManager.getFormatterLogger("REI"))
    ));
    public static final PerformanceLogger PERFORMANCE_LOGGER = new PerformanceLoggerImpl();
    
    static {
        attachCommonInternals();
        if (Platform.getEnvironment() == Env.CLIENT) {
            EnvExecutor.runInEnv(Env.CLIENT, () -> RoughlyEnoughItemsCoreClient::attachClientInternals);
        }
    }
    
    public static void attachCommonInternals() {
        CategoryIdentifierImpl.attach();
        Internals.attachInstance((Function<ResourceLocation, EntryType<?>>) DeferringEntryTypeProviderImpl.INSTANCE, "entryTypeDeferred");
        Internals.attachInstance(EntryStackProviderImpl.INSTANCE, Internals.EntryStackProvider.class);
        Internals.attachInstance(NbtHasherProviderImpl.INSTANCE, Internals.NbtHasherProvider.class);
        Internals.attachInstance(EntryIngredientImpl.INSTANCE, Internals.EntryIngredientProvider.class);
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
                new EntrySettingsAdapterRegistryImpl(),
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
    
    public static void _reloadPlugins(@Nullable ReloadStage stage) {
        if (stage == null) {
            for (ReloadStage reloadStage : ReloadStage.values()) {
                _reloadPlugins(reloadStage);
            }
            return;
        }
        try {
            for (PluginManager<? extends REIPlugin<?>> instance : PluginManager.getActiveInstances()) {
                instance.view().pre(stage);
            }
            for (PluginManager<? extends REIPlugin<?>> instance : PluginManager.getActiveInstances()) {
                instance.startReload(stage);
            }
            for (PluginManager<? extends REIPlugin<?>> instance : PluginManager.getActiveInstances()) {
                instance.view().post(stage);
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
                return preparationBarrier.wait(Unit.INSTANCE).thenRunAsync(() -> {
                    PERFORMANCE_LOGGER.clear();
                    RoughlyEnoughItemsCore._reloadPlugins(null);
                }, executor2);
            });
        }
    }
}
