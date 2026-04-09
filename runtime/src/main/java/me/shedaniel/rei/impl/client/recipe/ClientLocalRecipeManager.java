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

package me.shedaniel.rei.impl.client.recipe;

import dev.architectury.registry.ReloadListenerRegistry;
import me.shedaniel.rei.RoughlyEnoughItemsCoreClient;
import me.shedaniel.rei.impl.common.InternalLogger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.commands.Commands;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.permissions.PermissionSet;
import net.minecraft.tags.TagLoader;
import net.minecraft.util.Unit;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.crafting.RecipeManager;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;

public final class ClientLocalRecipeManager {
    private static final Object LOCK = new Object();
    private static final Identifier RELOAD_ID = Identifier.fromNamespaceAndPath("roughlyenoughitems", "client_local_recipes");
    private static int reloadGeneration = 0;
    private static @Nullable ReloadableServerResources resources;
    private static boolean initialized = false;
    private static boolean reloadListenerRegistered = false;

    private ClientLocalRecipeManager() {
    }

    public static void init() {
        synchronized (LOCK) {
            if (initialized) {
                return;
            }
            initialized = true;
        }

        registerReloadListenerIfPossible();
    }

    public static void scheduleReload() {
        registerReloadListenerIfPossible();
        Minecraft client = Minecraft.getInstance();
        reload(client.getResourceManager(), ForkJoinPool.commonPool(), client::execute);
    }

    public static void clear() {
        synchronized (LOCK) {
            reloadGeneration++;
            resources = null;
        }
    }

    public static @Nullable RecipeManager getRecipeManager() {
        ReloadableServerResources resources = ClientLocalRecipeManager.resources;
        return resources == null ? null : resources.getRecipeManager();
    }

    private static LayeredRegistryAccess<RegistryLayer> createClientRecipeRegistryAccess() {
        // ReloadableServerResources expects the normal server registry layers even when we run the recipe reload on
        // the client. Using the client registry stack fails later because it has [STATIC, REMOTE] instead of the
        // server's RELOADABLE layer, and merging the connection composite access duplicates builtin registries.
        //
        // Starting from RegistryLayer.createRegistryAccess() gives Mojang the exact layer shape it expects:
        // STATIC is already populated with the builtin registries, WORLDGEN/DIMENSIONS/RELOADABLE start empty, and
        // ReloadableServerRegistries will populate RELOADABLE during loadResources(...).
        return RegistryLayer.createRegistryAccess();
    }

    private static void registerReloadListenerIfPossible() {
        synchronized (LOCK) {
            if (reloadListenerRegistered) {
                return;
            }
        }

        try {
            // Register lazily after the client has started up. Architectury can assert if this happens too early
            // during entrypoint initialization, but the cache should still work via manual scheduleReload calls.
            ReloadListenerRegistry.register(PackType.CLIENT_RESOURCES, (sharedState, preparationExecutor, barrier, reloadExecutor) ->
                    barrier.wait(Unit.INSTANCE)
                            .thenComposeAsync(unit -> reload(Minecraft.getInstance().getResourceManager(), preparationExecutor, reloadExecutor), reloadExecutor), RELOAD_ID);
            synchronized (LOCK) {
                reloadListenerRegistered = true;
            }
        } catch (AssertionError error) {
            InternalLogger.getInstance().warn("Client local recipe reload listener could not be registered yet; continuing with manual reloads only.");
            InternalLogger.getInstance().debug("Client local recipe reload listener registration failed.", error);
        }
    }

    private static CompletableFuture<Void> reload(ResourceManager resourceManager, Executor preparationExecutor, Executor reloadExecutor) {
        Minecraft client = Minecraft.getInstance();
        ClientPacketListener connection = client.getConnection();
        if (connection == null) {
            clear();
            return CompletableFuture.completedFuture(null);
        }

        int generation;
        synchronized (LOCK) {
            generation = ++reloadGeneration;
        }

        // Client recipe reloads still go through the server-side resource loader, so we must hand it the normal
        // server registry layer shape instead of the client's composite remote registry access.
        LayeredRegistryAccess<RegistryLayer> registryAccess;
        try {
            LayeredRegistryAccess<RegistryLayer> createdRegistryAccess = createClientRecipeRegistryAccess();
            registryAccess = createdRegistryAccess;
        } catch (Throwable throwable) {
            synchronized (LOCK) {
                if (generation == reloadGeneration && client.getConnection() == connection) {
                    resources = null;
                }
            }
            InternalLogger.getInstance().error("Failed to create client local recipe registry access!", throwable);
            RoughlyEnoughItemsCoreClient.queueClientRecipeFallbackSync();
            return CompletableFuture.completedFuture(null);
        }
        FeatureFlagSet enabledFeatures = connection.enabledFeatures();

        return ReloadableServerResources.loadResources(resourceManager, registryAccess,
                        TagLoader.loadTagsForExistingRegistries(resourceManager, connection.registryAccess()),
                        enabledFeatures, Commands.CommandSelection.INTEGRATED, PermissionSet.NO_PERMISSIONS,
                        preparationExecutor, reloadExecutor)
                .thenAcceptAsync(resources -> {
                    boolean accepted = false;
                    synchronized (LOCK) {
                        if (generation == reloadGeneration && client.getConnection() == connection) {
                            ClientLocalRecipeManager.resources = resources;
                            accepted = true;
                        }
                    }

                    if (accepted) {
                        InternalLogger.getInstance().debug("Reloaded %d client local recipes.", resources.getRecipeManager().getRecipes().size());
                        RoughlyEnoughItemsCoreClient.queueClientRecipeFallbackSync();
                    }
                }, reloadExecutor)
                .exceptionally(throwable -> {
                    boolean accepted = false;
                    synchronized (LOCK) {
                        if (generation == reloadGeneration && client.getConnection() == connection) {
                            resources = null;
                            accepted = true;
                        }
                    }

                    if (accepted) {
                        InternalLogger.getInstance().error("Failed to reload client local recipes!", throwable);
                        RoughlyEnoughItemsCoreClient.queueClientRecipeFallbackSync();
                    }
                    return null;
                });
    }
}
