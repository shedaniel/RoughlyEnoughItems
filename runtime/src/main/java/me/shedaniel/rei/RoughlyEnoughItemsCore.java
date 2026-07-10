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
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.platform.Mod;
import dev.architectury.platform.Platform;
import dev.architectury.registry.ReloadListenerRegistry;
import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import dev.architectury.utils.GameInstance;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.type.EntryType;
import me.shedaniel.rei.api.common.plugins.REICommonPlugin;
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
import me.shedaniel.rei.impl.common.plugins.ReloadInterruptionContext;
import me.shedaniel.rei.impl.common.plugins.ReloadManagerImpl;
import me.shedaniel.rei.impl.common.registry.displays.ServerDisplayRegistryImpl;
import me.shedaniel.rei.impl.common.transfer.SlotAccessorRegistryImpl;
import me.shedaniel.rei.impl.common.util.InstanceHelper;
import me.shedaniel.rei.impl.init.PlatformAdapter;
import me.shedaniel.rei.impl.init.PluginDetector;
import me.shedaniel.rei.impl.init.PrimitivePlatformAdapter;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.util.Unit;
import net.minecraft.world.item.crafting.Ingredient;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.jetbrains.annotations.ApiStatus;

import java.util.Comparator;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

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
                logger.info("Version: " + getOptionalMod("roughlyenoughitems").map(Mod::getVersion).orElse(null));
                logger.info("Loader:");
                logger.info("- " + (fabric ? "Fabric" : "Forge") + ": " + getOptionalMod(fabric ? "fabricloader" : "forge").map(Mod::getVersion).orElse(null));
                if (fabric) logger.info("- Fabric API: " + getOptionalMod("fabric-api", "fabric").map(Mod::getVersion).orElse(null));
                logger.info("Dependencies:");
                logger.info("- Cloth Config: " + getOptionalMod(fabric ? "cloth-config" : "cloth_config", fabric ? "cloth-config2" : "cloth_config").map(Mod::getVersion).orElse(null));
                logger.info("- Architectury: " + getOptionalMod("architectury").map(Mod::getVersion).orElse(null));
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

    private static Optional<Mod> getOptionalMod(String... ids) {
        for (String id : ids) {
            try {
                return Platform.getOptionalMod(id);
            } catch (Throwable ignored) {
            }
        }

        return Optional.empty();
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
        Internals.attachInstance((Function<Ingredient, EntryIngredient>) ingredient -> PlatformAdapter.get().fromIngredient(ingredient), "ingredientToEntryIngredient");
        Internals.attachInstance((Function<Identifier, EntryType<?>>) DeferringEntryTypeProviderImpl.INSTANCE, "entryTypeDeferred");
        Internals.attachInstance((Supplier<RegistryAccess>) () -> InstanceHelper.getInstance().registryAccess(), "registryAccess");
        Internals.attachInstance(EntryStackProviderImpl.INSTANCE, Internals.EntryStackProvider.class);
        Internals.attachInstance(NbtHasherProviderImpl.INSTANCE, Internals.NbtHasherProvider.class);
        Internals.attachInstance(EntryIngredientImpl.INSTANCE, Internals.EntryIngredientProvider.class);
        Internals.attachInstanceSupplier(new PluginManagerImpl<>(
                REICommonPlugin.class,
                new SlotAccessorRegistryImpl(),
                new EntryTypeRegistryImpl(),
                new EntrySettingsAdapterRegistryImpl(),
                new ItemComparatorRegistryImpl(),
                new FluidComparatorRegistryImpl(),
                new DisplaySerializerRegistryImpl(),
                new ServerDisplayRegistryImpl(),
                new FluidSupportProviderImpl()), "commonPluginManager");
    }
    
    public void onInitialize() {
        PluginDetector detector = getPluginDetector();
        detector.detectCommonPlugins();
        RoughlyEnoughItemsNetwork.onInitialize();
        
        if (Platform.getEnvironment() == Env.SERVER) {
            LifecycleEvent.SERVER_STARTED.register(server -> {
                ReloadManagerImpl.reloadPlugins(null, ReloadInterruptionContext.ofNever());
            });
            ReloadListenerRegistry.register(PackType.SERVER_DATA, (sharedState, executor, preparationBarrier, executor2) -> {
                return preparationBarrier.wait(Unit.INSTANCE).thenRunAsync(() -> {
                    if (GameInstance.getServer() == null) return;
                    ReloadManagerImpl.reloadPlugins(null, ReloadInterruptionContext.ofNever());
                }, executor2);
            }, Identifier.fromNamespaceAndPath("roughlyenoughitems", "reload_plugins"));
        }
    }
    
    public static PluginDetector getPluginDetector() {
        return PLUGIN_DETECTOR_LOADER.findFirst().orElseThrow();
    }
}
