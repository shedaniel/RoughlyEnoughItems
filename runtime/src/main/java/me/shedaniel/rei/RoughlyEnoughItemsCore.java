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

package me.shedaniel.rei;

import com.google.common.collect.ImmutableList;
import dev.architectury.platform.Mod;
import dev.architectury.platform.Platform;
import dev.architectury.registry.ReloadListenerRegistry;
import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.annotation.Nullable;
import me.shedaniel.rei.api.common.entry.type.EntryType;
import me.shedaniel.rei.api.common.plugins.PluginManager;
import me.shedaniel.rei.api.common.plugins.PluginView;
import me.shedaniel.rei.api.common.plugins.REIPlugin;
import me.shedaniel.rei.api.common.plugins.REIServerPlugin;
import me.shedaniel.rei.api.common.registry.ReloadStage;
import me.shedaniel.rei.impl.Internals;
import me.shedaniel.rei.impl.common.InternalLogger;
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
import me.shedaniel.rei.impl.common.logging.*;
import me.shedaniel.rei.impl.common.logging.performance.PerformanceLogger;
import me.shedaniel.rei.impl.common.logging.performance.PerformanceLoggerImpl;
import me.shedaniel.rei.impl.common.plugins.PluginManagerImpl;
import me.shedaniel.rei.impl.common.registry.RecipeManagerContextImpl;
import me.shedaniel.rei.impl.common.transfer.MenuInfoRegistryImpl;
import me.shedaniel.rei.impl.init.PluginDetector;
import me.shedaniel.rei.impl.init.PrimitivePlatformAdapter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.util.Unit;
import org.apache.commons.lang3.mutable.MutableLong;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.jetbrains.annotations.ApiStatus;

import java.util.Comparator;
import java.util.ServiceLoader;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;

@ApiStatus.Internal
public class RoughlyEnoughItemsCore {
    @ApiStatus.Internal
    public static final InternalLogger LOGGER = new TransformingLogger(new MultiLogger(ImmutableList.of(
            make(new FileLogger(Platform.getGameFolder().resolve("logs/rei.log")), logger -> {
                PrimitivePlatformAdapter adapter = PrimitivePlatformAdapter.get();
                boolean fabric = Platform.isFabric();
                logger.info("========================================");
                logger.info("Minecraft: " + adapter.getMinecraftVersion());
                logger.info("Side: " + (adapter.isClient() ? "client" : "server"));
                logger.info("Development: " + adapter.isDev());
                logger.info("Version: " + Platform.getOptionalMod("roughlyenoughitems").map(Mod::getVersion).orElse(null));
                logger.info("Loader:");
                logger.info("- " + (fabric ? "Fabric" : "Forge") + ": " + Platform.getOptionalMod(fabric ? "fabricloader" : "forge").map(Mod::getVersion).orElse(null));
                if (fabric) logger.info("- Fabric API: " + Platform.getOptionalMod("fabric").map(Mod::getVersion).orElse(null));
                logger.info("Dependencies:");
                logger.info("- Cloth Config: " + Platform.getOptionalMod(fabric ? "cloth-config2" : "cloth_config").map(Mod::getVersion).orElse(null));
                logger.info("- Architectury: " + Platform.getOptionalMod("architectury").map(Mod::getVersion).orElse(null));
                String mixin = "null";
                try {
                    mixin = (String) Class.forName("org.spongepowered.asm.launch.MixinBootstrap").getDeclaredField("VERSION").get(null);
                } catch (Throwable ignored) {
                }
                logger.info("Mixin: " + mixin);
                logger.info("OS: " + System.getProperty("os.name") + " " + System.getProperty("os.version") + " (" + System.getProperty("os.arch") + ")");
                logger.info("Java: " + System.getProperty("java.version") + " (" + System.getProperty("java.vendor") + ")");
                logger.info("========================================");
                logger.info("Mods:");
                for (Mod mod : Platform.getMods().stream().sorted(Comparator.comparing(Mod::getModId)).toList()) {
                    logger.info("- " + mod.getModId() + ": " + mod.getVersion());
                }
                logger.info("========================================");
            }),
            new FilteringLogger(new FileLogger(Platform.getGameFolder().resolve("logs/rei-issues.log")), Level.WARN),
            new Log4JLogger(LogManager.getFormatterLogger("REI"))
    )), message -> "[REI] " + message);
    public static final PerformanceLogger PERFORMANCE_LOGGER = new PerformanceLoggerImpl();
    private static final ServiceLoader<PluginDetector> PLUGIN_DETECTOR_LOADER = ServiceLoader.load(PluginDetector.class);
    
    private static <T> T make(T object, Consumer<T> consumer) {
        consumer.accept(object);
        return object;
    }
    
    static {
        attachCommonInternals();
        if (Platform.getEnvironment() == Env.CLIENT) {
            EnvExecutor.runInEnv(Env.CLIENT, () -> RoughlyEnoughItemsCoreClient::attachClientInternals);
        }
    }
    
    public static void attachCommonInternals() {
        Internals.attachInstanceSupplier(LOGGER, "logger");
        CategoryIdentifierImpl.attach();
        Internals.attachInstance((Function<ResourceLocation, EntryType<?>>) DeferringEntryTypeProviderImpl.INSTANCE, "entryTypeDeferred");
        Internals.attachInstance(EntryStackProviderImpl.INSTANCE, Internals.EntryStackProvider.class);
        Internals.attachInstance(NbtHasherProviderImpl.INSTANCE, Internals.NbtHasherProvider.class);
        Internals.attachInstance(EntryIngredientImpl.INSTANCE, Internals.EntryIngredientProvider.class);
        Internals.attachInstanceSupplier(new PluginManagerImpl<>(
                REIPlugin.class,
                UnaryOperator.identity(),
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
        PluginDetector detector = getPluginDetector();
        detector.detectCommonPlugins();
        detector.detectServerPlugins();
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
    
    public static PluginDetector getPluginDetector() {
        return PLUGIN_DETECTOR_LOADER.findFirst().orElseThrow();
    }
}
