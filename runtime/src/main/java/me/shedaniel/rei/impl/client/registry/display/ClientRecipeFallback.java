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

package me.shedaniel.rei.impl.client.registry.display;

import me.shedaniel.rei.RoughlyEnoughItemsCoreClient;
import me.shedaniel.rei.api.client.config.ConfigObject;
import me.shedaniel.rei.api.client.gui.config.ForceLocalRecipesMode;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.impl.common.InternalLogger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import me.shedaniel.rei.impl.init.PlatformAdapter;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.WritableRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.MultiPackResourceManager;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.tags.TagLoader;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.display.RecipeDisplayEntry;
import net.minecraft.world.item.crafting.display.RecipeDisplayId;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Synthesizes recipe displays from the client's own data packs when connected to a
 * server that does not synchronise recipe data (see {@link ForceLocalRecipesMode}).
 *
 * <p>This is purely client-side and additive: locally-loaded displays are injected
 * into {@link DisplayRegistryImpl} during reload with a {@link DisplayRegistryImpl.ClientFallbackOrigin}
 * origin, deduplicated against any recipe-book entries the server did send, and removed
 * again if the server later pushes its own REI display sync.
 */
public final class ClientRecipeFallback {
    private ClientRecipeFallback() {
    }

    /**
     * Recipe-display ids the server advertised through the vanilla recipe book
     * ({@code ClientRecipeUpdateEvent.ADD}). Used to avoid double-counting recipes
     * that both the server and the local fallback provide.
     */
    private static final Set<RecipeDisplayId> serverProvidedIds = ConcurrentHashMap.newKeySet();

    /**
     * Bumped whenever the player joins/leaves a world, so async loads or cached
     * results from a previous connection are discarded.
     */
    private static volatile int generation = 0;
    /** The connection the cached entries were loaded for. */
    @Nullable
    private static volatile ClientPacketListener loadedConnection = null;
    /** Cached locally-loaded displays for the current connection, or {@code null} if not loaded yet. */
    @Nullable
    private static volatile List<RecipeDisplayEntry> cachedEntries = null;
    /** Whether an async load is currently in flight. */
    private static volatile boolean loading = false;
    /** Set once the server pushes its own REI display sync; disables AUTO fallback. */
    private static volatile boolean receivedServerDisplaySync = false;

    private static ForceLocalRecipesMode mode() {
        return ConfigObject.getInstance().getForceLocalRecipes();
    }

    /**
     * Whether the fallback should contribute displays right now: enabled by config,
     * connected to a remote (non-integrated) server. In AUTO it only applies until the
     * server pushes its own display sync; ALWAYS applies regardless.
     */
    public static boolean shouldInject() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.getConnection() == null || mc.isLocalServer()) {
            return false;
        }
        return switch (mode()) {
            case NEVER -> false;
            case AUTO -> !receivedServerDisplaySync;
            case ALWAYS -> true;
        };
    }

    /** Called when the player joins or leaves a world; invalidates all cached state. */
    public static void reset() {
        generation++;
        loadedConnection = null;
        cachedEntries = null;
        loading = false;
        receivedServerDisplaySync = false;
        serverProvidedIds.clear();
    }

    /**
     * Records recipe-book ids the server advertised, so the fallback can dedup against them.
     * If the fallback already injected its displays (e.g. the player unlocked a recipe
     * mid-session), the now-overlapping local displays are removed so the server's win and we
     * don't render duplicates.
     */
    public static void onServerRecipeBookAdd(List<RecipeDisplayId> ids) {
        if (ids.isEmpty()) {
            return;
        }
        boolean hadInjected = cachedEntriesForCurrentConnection() != null;
        serverProvidedIds.addAll(ids);
        if (hadInjected && shouldInject()) {
            Set<RecipeDisplayId> newlyAdded = Set.copyOf(ids);
            try {
                DisplayRegistry registry = DisplayRegistry.getInstance();
                if (registry instanceof DisplayRegistryImpl impl) {
                    impl.addJob(() -> impl.removeFallbackRecipes(newlyAdded));
                }
            } catch (Throwable throwable) {
                InternalLogger.getInstance().error("[Local Recipes] Failed to remove overlapping fallback displays after server recipe add", throwable);
            }
        }
    }

    public static void onServerRecipeBookRemove(Set<RecipeDisplayId> ids) {
        serverProvidedIds.removeAll(ids);
    }

    /**
     * Called when the server pushes its own REI display sync. In AUTO this supersedes the
     * local fallback: existing fallback displays are dropped and no more are injected.
     */
    public static void onServerDisplaySync() {
        receivedServerDisplaySync = true;
        if (mode() == ForceLocalRecipesMode.AUTO) {
            try {
                DisplayRegistry registry = DisplayRegistry.getInstance();
                if (registry instanceof DisplayRegistryImpl impl) {
                    impl.addJob(impl::removeFallbackRecipes);
                }
            } catch (Throwable throwable) {
                InternalLogger.getInstance().error("[Local Recipes] Failed to remove fallback displays after server sync", throwable);
            }
        }
    }

    /**
     * Injects the locally-loaded fallback displays into the registry during its reload.
     * If the displays have not been loaded yet, kicks off an async load and requests a
     * reload once it completes (so this method stays cheap and off the data-pack thread).
     */
    public static void injectInto(DisplayRegistryImpl registry) {
        if (!shouldInject()) {
            return;
        }
        List<RecipeDisplayEntry> entries = cachedEntriesForCurrentConnection();
        if (entries == null) {
            ensureLoaded();
            return;
        }
        registry.addFallbackRecipes(entries, serverProvidedIds);
    }

    @Nullable
    private static List<RecipeDisplayEntry> cachedEntriesForCurrentConnection() {
        List<RecipeDisplayEntry> entries = cachedEntries;
        if (entries != null && loadedConnection == Minecraft.getInstance().getConnection()) {
            return entries;
        }
        return null;
    }

    /**
     * Ensures the local recipe set is (being) loaded for the current connection. Safe to call
     * repeatedly (e.g. from search); at most one load runs at a time. When the load finishes it
     * triggers a plugin reload so {@link #injectInto} can add the displays.
     */
    public static void ensureLoaded() {
        if (loading || !shouldInject() || cachedEntriesForCurrentConnection() != null) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        ClientPacketListener connection = mc.getConnection();
        if (connection == null) {
            return;
        }
        int gen = generation;
        loading = true;
        RegistryAccess registryAccess = connection.registryAccess();
        FeatureFlagSet enabledFeatures = connection.enabledFeatures();
        CompletableFuture.supplyAsync(() -> loadLocalRecipeDisplays(registryAccess, enabledFeatures))
                .whenComplete((entries, throwable) -> {
                    if (throwable != null) {
                        InternalLogger.getInstance().error("[Local Recipes] Failed to load recipes from client data packs", throwable);
                    }
                    Minecraft.getInstance().execute(() -> {
                        loading = false;
                        // Discard if the world changed or the connection was replaced while loading.
                        if (gen != generation || Minecraft.getInstance().getConnection() != connection) {
                            return;
                        }
                        cachedEntries = entries != null ? entries : Collections.emptyList();
                        loadedConnection = connection;
                        if (shouldInject()) {
                            InternalLogger.getInstance().info("[Local Recipes] Loaded %d local recipe displays; reloading to inject", cachedEntries.size());
                            RoughlyEnoughItemsCoreClient.reloadPlugins(null, null);
                        }
                    });
                });
    }

    /**
     * Loads the client's data-pack recipes and converts them into {@link RecipeDisplayEntry}
     * objects, mirroring what a dedicated server sends through the recipe book. Runs off-thread.
     */
    private static List<RecipeDisplayEntry> loadLocalRecipeDisplays(RegistryAccess registryAccess, FeatureFlagSet enabledFeatures) {
        InternalLogger.getInstance().info("[Local Recipes] Loading recipes from client data packs...");
        List<RecipeDisplayEntry> entries = new ArrayList<>();

        // The SERVER_DATA pack stack the client could load locally: vanilla data plus every mod's
        // data pack. Gathering mod data packs is loader-specific, so it goes through the platform.
        List<PackResources> packs = PlatformAdapter.get().gatherClientDataPacks();

        try (MultiPackResourceManager dataManager = new MultiPackResourceManager(PackType.SERVER_DATA, packs)) {
            // Recipe ingredients reference tags from builtin registries (e.g. #minecraft:planks).
            // Bind those from the loaded packs so tag ingredients resolve while parsing.
            int tagCount = 0;
            for (Registry<?> registry : BuiltInRegistries.REGISTRY) {
                if (registry instanceof @SuppressWarnings({"unchecked", "rawtypes"})WritableRegistry writable) {
                    try {
                        TagLoader.loadTagsForRegistry(dataManager, writable);
                        tagCount++;
                    } catch (Exception e) {
                        // Some registries have no tag directories; skip silently.
                    }
                }
            }
            InternalLogger.getInstance().debug("[Local Recipes] Loaded tags for %d builtin registries", tagCount);

            List<Registry.PendingTags<?>> pendingTags = TagLoader.loadTagsForExistingRegistries(dataManager, registryAccess);
            for (Registry.PendingTags<?> pt : pendingTags) {
                pt.apply();
            }

            // Parse the recipes directly. This produces a RecipeManager holding the same
            // RecipeDisplayEntry objects (with self-consistent RecipeDisplayIds) that a dedicated
            // server would send through the recipe book.
            RecipeManager recipeManager = new RecipeManager(registryAccess);
            recipeManager.reload(new PreparableReloadListener.SharedState(dataManager), Runnable::run,
                    CompletableFuture::completedFuture, Runnable::run).join();
            // reload() only parses recipes; the recipe-display index is built separately by
            // finalizeRecipeLoading, exactly as the server does before sending the recipe book.
            recipeManager.finalizeRecipeLoading(enabledFeatures);

            recipeManager.getRecipes().forEach(holder ->
                    recipeManager.listDisplaysForRecipe(holder.id(), entries::add));
        }
        InternalLogger.getInstance().info("[Local Recipes] Built %d local recipe displays", entries.size());
        return entries;
    }
}
