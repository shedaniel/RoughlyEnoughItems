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

import dev.architectury.platform.Platform;
import dev.architectury.registry.ReloadListenerRegistry;
import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import me.shedaniel.rei.api.common.display.DisplaySerializerRegistry;
import me.shedaniel.rei.api.common.entry.comparison.FluidComparatorRegistry;
import me.shedaniel.rei.api.common.entry.comparison.ItemComparatorRegistry;
import me.shedaniel.rei.api.common.entry.settings.EntrySettingsAdapterRegistry;
import me.shedaniel.rei.api.common.entry.type.EntryTypeRegistry;
import me.shedaniel.rei.api.common.fluid.FluidSupportProvider;
import me.shedaniel.rei.api.common.plugins.PluginManager;
import me.shedaniel.rei.api.common.plugins.REIPlugin;
import me.shedaniel.rei.api.common.registry.RecipeManagerContext;
import me.shedaniel.rei.api.common.registry.ReloadStage;
import me.shedaniel.rei.api.common.transfer.info.MenuInfoRegistry;
import me.shedaniel.rei.impl.Internals;
import me.shedaniel.rei.impl.common.InternalLogger;
import me.shedaniel.rei.impl.common.logging.performance.PerformanceLogger;
import me.shedaniel.rei.impl.common.logging.performance.PerformanceLoggerImpl;
import me.shedaniel.rei.impl.init.PluginDetector;
import net.minecraft.server.packs.PackType;
import net.minecraft.util.Unit;
import org.apache.commons.lang3.mutable.MutableLong;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@ApiStatus.Internal
public class RoughlyEnoughItemsCore {
    public static final PerformanceLogger PERFORMANCE_LOGGER = new PerformanceLoggerImpl();
    public static final List<PluginDetector> PLUGIN_DETECTORS = Internals.resolveServices(PluginDetector.class);
    
    static {
        attachCommonInternals();
        if (Platform.getEnvironment() == Env.CLIENT) {
            EnvExecutor.runInEnv(Env.CLIENT, () -> RoughlyEnoughItemsCoreClient::attachClientInternals);
        }
    }
    
    public static void attachCommonInternals() {
        PluginManager.getInstance().registerReloadable(EntryTypeRegistry.class);
        PluginManager.getInstance().registerReloadable(EntrySettingsAdapterRegistry.class);
        PluginManager.getInstance().registerReloadable(RecipeManagerContext.class);
        PluginManager.getInstance().registerReloadable(ItemComparatorRegistry.class);
        PluginManager.getInstance().registerReloadable(FluidComparatorRegistry.class);
        PluginManager.getInstance().registerReloadable(DisplaySerializerRegistry.class);
        PluginManager.getInstance().registerReloadable(FluidSupportProvider.class);
        PluginManager.getServerInstance().registerReloadable(MenuInfoRegistry.class);
        Internals.attachInstanceSupplier((Runnable) () -> RoughlyEnoughItemsCore.reloadPlugins(null, null), "reloadREI");
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
    
    public static void reloadPlugins(MutableLong lastReload, @Nullable ReloadStage start) {
        if (lastReload != null) {
            if (lastReload.getValue() > 0 && System.currentTimeMillis() - lastReload.getValue() <= 5000) {
                InternalLogger.getInstance().warn("Suppressing Reload Plugins of stage " + start);
                return;
            }
            lastReload.setValue(System.currentTimeMillis());
        }
        if (start == null) PERFORMANCE_LOGGER.clear();
        if (Platform.getEnvironment() == Env.CLIENT) {
            if (RoughlyEnoughItemsCoreClient.reloadPluginsClient(start)) return;
        }
        _reloadPlugins(start);
    }
    
    public void onInitialize() {
        for (PluginDetector detector : PLUGIN_DETECTORS) {
            detector.detectCommonPlugins();
            detector.detectServerPlugins();
        }
        RoughlyEnoughItemsNetwork.onInitialize();
        
        if (Platform.getEnvironment() == Env.SERVER) {
            MutableLong lastReload = new MutableLong(-1);
            ReloadListenerRegistry.register(PackType.SERVER_DATA, (preparationBarrier, resourceManager, profilerFiller, profilerFiller2, executor, executor2) -> {
                return preparationBarrier.wait(Unit.INSTANCE).thenRunAsync(PluginManager::reloadAll, executor2);
            });
        }
    }
}
