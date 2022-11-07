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

package me.shedaniel.rei.impl.client.init;

import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.client.ClientGuiEvent;
import dev.architectury.event.events.client.ClientRecipeUpdateEvent;
import me.shedaniel.rei.api.client.REIRuntime;
import me.shedaniel.rei.api.client.config.ConfigManager;
import me.shedaniel.rei.api.client.config.ConfigObject;
import me.shedaniel.rei.api.client.config.addon.ConfigAddonRegistry;
import me.shedaniel.rei.api.client.entry.filtering.FilteringRuleTypeRegistry;
import me.shedaniel.rei.api.client.entry.renderer.EntryRendererRegistry;
import me.shedaniel.rei.api.client.favorites.FavoriteEntryType;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.client.registry.entry.CollapsibleEntryRegistry;
import me.shedaniel.rei.api.client.registry.entry.EntryRegistry;
import me.shedaniel.rei.api.client.registry.screen.ScreenRegistry;
import me.shedaniel.rei.api.client.registry.transfer.TransferHandlerRegistry;
import me.shedaniel.rei.api.client.search.SearchProvider;
import me.shedaniel.rei.api.client.search.method.InputMethodRegistry;
import me.shedaniel.rei.api.client.subsets.SubsetsRegistry;
import me.shedaniel.rei.api.client.view.Views;
import me.shedaniel.rei.api.common.plugins.PluginManager;
import me.shedaniel.rei.api.common.plugins.PluginView;
import me.shedaniel.rei.api.common.plugins.REIPluginProvider;
import me.shedaniel.rei.api.common.registry.ReloadStage;
import me.shedaniel.rei.impl.client.ClientInternals;
import me.shedaniel.rei.impl.common.InternalLogger;
import me.shedaniel.rei.impl.common.init.CoreInitialization;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.apache.commons.lang3.mutable.MutableLong;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.concurrent.*;

@ApiStatus.Internal
public class CoreClientInitialization {
    public static final Event<ClientRecipeUpdateEvent> PRE_UPDATE_RECIPES = EventFactory.createLoop();
    private static final ExecutorService RELOAD_PLUGINS = Executors.newSingleThreadScheduledExecutor(task -> {
        Thread thread = new Thread(task, "REI-ReloadPlugins");
        thread.setDaemon(true);
        thread.setUncaughtExceptionHandler(($, exception) -> {
            InternalLogger.getInstance().throwException(exception);
        });
        return thread;
    });
    private static final List<Future<?>> RELOAD_TASKS = new CopyOnWriteArrayList<>();
    
    public void onInitializeClient() {
        attachClientInternals();
        loadTestPlugins();
        
        MutableLong startReload = new MutableLong(-1);
        MutableLong endReload = new MutableLong(-1);
        PRE_UPDATE_RECIPES.register(recipeManager -> {
            CoreInitialization.reloadPlugins(startReload, ReloadStage.START);
        });
        ClientRecipeUpdateEvent.EVENT.register(recipeManager -> {
            CoreInitialization.reloadPlugins(endReload, ReloadStage.END);
        });
        ClientGuiEvent.INIT_PRE.register((screen, access) -> {
            List<ReloadStage> stages = PluginManager.getInstance().view().getObservedStages();
            
            if (Minecraft.getInstance().level != null && Minecraft.getInstance().player != null && stages.contains(ReloadStage.START)
                && !stages.contains(ReloadStage.END) && !PluginManager.areAnyReloading() && screen instanceof AbstractContainerScreen) {
                for (Future<?> task : RELOAD_TASKS) {
                    if (!task.isDone()) {
                        return EventResult.pass();
                    }
                }
                
                InternalLogger.getInstance().error("Detected missing stage: END! This is possibly due to issues during client recipe reload! REI will force a reload of the recipes now!");
                CoreInitialization.reloadPlugins(endReload, ReloadStage.END);
            }
            
            return EventResult.pass();
        });
    }
    
    public static void attachClientInternals() {
        PluginManager<REIClientPlugin> manager = ClientInternals.getPluginManager();
        manager.registerReloadable(EntryRendererRegistry.class);
        manager.registerReloadable(Views.class);
        manager.registerReloadable(InputMethodRegistry.class);
        manager.registerReloadable(SearchProvider.class);
        manager.registerReloadable(ConfigManager.class);
        manager.registerReloadable(EntryRegistry.class);
        manager.registerReloadable(CollapsibleEntryRegistry.class);
        manager.registerReloadable(FilteringRuleTypeRegistry.getInstance().basic());
        manager.registerReloadable(CategoryRegistry.class);
        manager.registerReloadable(DisplayRegistry.class);
        manager.registerReloadable(ScreenRegistry.class);
        manager.registerReloadable(FavoriteEntryType.Registry.class);
        manager.registerReloadable(SubsetsRegistry.class);
        manager.registerReloadable(TransferHandlerRegistry.class);
        manager.registerReloadable(REIRuntime.class);
        manager.registerReloadable(ConfigAddonRegistry.class);
    }
    
    private void loadTestPlugins() {
        if (System.getProperty("rei.test", "false").equals("true")) {
            try {
                PluginView.getClientInstance().registerPlugin((REIPluginProvider<? extends REIClientPlugin>) Class.forName("me.shedaniel.rei.plugin.test.REITestPlugin")
                        .getDeclaredConstructor()
                        .newInstance());
            } catch (InstantiationException | ClassNotFoundException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }
    
    public static boolean reloadPluginsClient(@Nullable ReloadStage start) {
        if (ConfigObject.getInstance().doesRegisterRecipesInAnotherThread()) {
            Future<?>[] futures = new Future<?>[1];
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> CoreInitialization._reloadPlugins(start), RELOAD_PLUGINS)
                    .whenComplete((unused, throwable) -> {
                        // Remove the future from the list of futures
                        if (futures[0] != null) {
                            RELOAD_TASKS.remove(futures[0]);
                            futures[0] = null;
                        }
                    });
            futures[0] = future;
            RELOAD_TASKS.add(future);
            return true;
        }
        
        return false;
    }
}
