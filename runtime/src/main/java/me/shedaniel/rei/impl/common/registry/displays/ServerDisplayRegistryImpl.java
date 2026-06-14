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

package me.shedaniel.rei.impl.common.registry.displays;

import com.google.common.base.Stopwatch;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.event.events.common.TickEvent;
import dev.architectury.networking.NetworkManager;
import dev.architectury.utils.GameInstance;
import it.unimi.dsi.fastutil.ints.IntIntPair;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import me.shedaniel.rei.api.client.registry.display.reason.DisplayAdditionReason;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.display.DisplaySerializerRegistry;
import me.shedaniel.rei.api.common.plugins.PluginManager;
import me.shedaniel.rei.api.common.plugins.REICommonPlugin;
import me.shedaniel.rei.api.common.registry.display.ServerDisplayRegistry;
import me.shedaniel.rei.api.common.util.CollectionUtils;
import me.shedaniel.rei.impl.common.InternalLogger;
import me.shedaniel.rei.impl.common.networking.DisplaySyncPacket;
import me.shedaniel.rei.impl.common.plugins.ReloadManagerImpl;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

public class ServerDisplayRegistryImpl extends AbstractDisplayRegistry<REICommonPlugin, ServerDisplayRegistryImpl.ServerDisplaysHolder> implements ServerDisplayRegistry, DisplayConsumerImpl {
    private static final Comparator<RecipeHolder<?>> RECIPE_COMPARATOR = Comparator.comparing((RecipeHolder<?> o) -> o.id().location().getNamespace()).thenComparing(o -> o.id().location().getPath());
    private final Object2LongMap<UUID> playerVersionMap = new Object2LongOpenHashMap<>();
    private int reloadVersionHash = UUID.randomUUID().hashCode();
    
    public ServerDisplayRegistryImpl() {
        super(ServerDisplaysHolder::new);
        
        int[] tick = {0};
        TickEvent.SERVER_POST.register(instance -> {
            if (tick[0]++ % 2 == 0 && !PluginManager.areAnyReloading() && ReloadManagerImpl.countRunningReloadTasks() == 0) {
                long version = this.currentVersion();
                holder().everySecond();
                long currentVersion = this.currentVersion();
                if (version != currentVersion) {
                    InternalLogger.getInstance().debug("Updating players with new displays version %X [from %X]", currentVersion, version);
                    updatePlayers(instance.registryAccess(), CollectionUtils.filterToList(instance.getPlayerList().getPlayers(), player -> NetworkManager.canPlayerReceive(player, DisplaySyncPacket.TYPE)));
                    return;
                }
                List<ServerPlayer> toUpdate = new ArrayList<>();
                for (ServerPlayer player : instance.getPlayerList().getPlayers()) {
                    if (!NetworkManager.canPlayerReceive(player, DisplaySyncPacket.TYPE)) {
                        continue;
                    }
                    long versionHash = playerVersionMap.getLong(player.getUUID());
                    if (versionHash != currentVersion) {
                        InternalLogger.getInstance().debug("Player %s has outdated displays version %X [latest version: %X]", player.getGameProfile().getName(), versionHash, currentVersion);
                        toUpdate.add(player);
                    }
                }
                if (!toUpdate.isEmpty()) {
                    updatePlayers(instance.registryAccess(), toUpdate);
                }
            }
        });
        PlayerEvent.PLAYER_JOIN.register(player -> {
            playerVersionMap.put(player.getUUID(), 0);
        });
        PlayerEvent.PLAYER_QUIT.register(player -> {
            playerVersionMap.removeLong(player.getUUID());
        });
    }
    
    private void updatePlayers(RegistryAccess registryAccess, List<ServerPlayer> players) {
        Supplier<List<Packet<?>>> resetPacket = Suppliers.memoize(() -> getResetPackets(registryAccess));
        Map<IntIntPair, List<Packet<?>>> updatesMap = new HashMap<>();
        Function<IntIntPair, List<Packet<?>>> updatePackets = pair -> {
            return updatesMap.computeIfAbsent(pair, $ -> getUpdatePackets(registryAccess, pair.leftInt(), pair.rightInt()));
        };
        
        long version = this.currentVersion();
        for (ServerPlayer player : players) {
            long playerVersion = playerVersionMap.getLong(player.getUUID());
            
            // Check if the player has the same reload hash first
            long playerReloadHash = playerVersion >>> 32;
            playerVersionMap.put(player.getUUID(), version);
            if (playerReloadHash != reloadVersionHash) {
                InternalLogger.getInstance().debug("Player %s has outdated displays version %X [latest version: %X], sending reset packet request.", player.getGameProfile().getName(), playerVersion, version);
                for (Packet<?> packet : resetPacket.get()) {
                    player.connection.send(packet);
                }
            } else if (playerVersion != version) {
                // Let's request updates from the version of the player to the current version
                int playerMinorVersion = (int) playerVersion;
                int currentMinorVersion = (int) version;
                if (playerMinorVersion > currentMinorVersion) {
                    InternalLogger.getInstance().debug("Player %s has too new displays version %X [latest version: %X], sending reset packet request.", player.getGameProfile().getName(), playerVersion, version);
                    // Reset the player
                    for (Packet<?> packet : resetPacket.get()) {
                        player.connection.send(packet);
                    }
                } else {
                    InternalLogger.getInstance().debug("Player %s has outdated displays version %X [latest version: %X], sending update packets.", player.getGameProfile().getName(), playerVersion, version);
                    // Update the player
                    for (Packet<?> packet : updatePackets.apply(IntIntPair.of(playerMinorVersion, currentMinorVersion))) {
                        player.connection.send(packet);
                    }
                }
            }
        }
    }
    
    private List<Packet<?>> getResetPackets(RegistryAccess registryAccess) {
        List<Set<Display>> displays = holder().displaysByVersion;
        if (displays.isEmpty() || (displays.size() == 1 && displays.get(0).isEmpty())) {
            return List.of();
        }
        
        List<Packet<?>> packets = new ArrayList<>();
        NetworkManager.collectPackets(packets::add, NetworkManager.s2c(), new DisplaySyncPacket(DisplaySyncPacket.SyncType.SET, new AbstractCollection<>() {
            @Override
            public Iterator<Display> iterator() {
                return displays.stream().flatMap(Set::stream).iterator();
            }
            
            @Override
            public int size() {
                return displays.stream().mapToInt(Set::size).sum();
            }
        }, currentVersion()), registryAccess);
        return packets;
    }
    
    private List<Packet<?>> getUpdatePackets(RegistryAccess registryAccess, int from, int to) {
        List<Set<Display>> displays = holder().displaysByVersion.subList(from, to);
        if (displays.isEmpty() || (displays.size() == 1 && displays.get(0).isEmpty())) {
            return List.of();
        }
        
        List<Packet<?>> packets = new ArrayList<>();
        NetworkManager.collectPackets(packets::add, NetworkManager.s2c(), new DisplaySyncPacket(DisplaySyncPacket.SyncType.APPEND, new AbstractCollection<>() {
            @Override
            public Iterator<Display> iterator() {
                return displays.stream().flatMap(Set::stream).iterator();
            }
            
            @Override
            public int size() {
                return displays.stream().mapToInt(Set::size).sum();
            }
        }, currentVersion()), registryAccess);
        return packets;
    }
    
    @Override
    public void startReload() {
        this.reloadVersionHash = UUID.randomUUID().hashCode();
        super.startReload();
    }
    
    private long currentVersion() {
        return ((long) this.reloadVersionHash) << 32 | this.holder().displaysByVersion.size();
    }
    
    @Override
    public void acceptPlugin(REICommonPlugin plugin) {
        plugin.registerDisplays(this);
    }
    
    @Override
    public boolean add(Display display, @Nullable Object origin) {
        if (DisplaySerializerRegistry.getInstance().isRegistered(display.getSerializer())) {
            return super.add(display, origin);
        } else {
            InternalLogger.getInstance().throwException(new IllegalStateException("Tried to add display [%s] with unregistered serializer: %s".formatted(display, display.getSerializer())));
            return false;
        }
    }
    
    @Override
    public void endReload() {
        InternalLogger.getInstance().debug("Found preliminary %d displays", size());
        fillRecipes();
    }
    
    private void fillRecipes() {
        Stopwatch stopwatch = Stopwatch.createStarted();
        int lastSize = size();
        if (!fillers().isEmpty()) {
            List<RecipeHolder<?>> allSortedRecipes = getAllSortedRecipes();
            for (int i = allSortedRecipes.size() - 1; i >= 0; i--) {
                RecipeHolder<?> recipe = allSortedRecipes.get(i);
                try {
                    addWithReason(recipe, DisplayAdditionReason.RECIPE_MANAGER);
                } catch (Throwable e) {
                    InternalLogger.getInstance().error("Failed to fill display for recipe: %s [%s]", recipe.value(), recipe.id(), e);
                }
            }
        }
        InternalLogger.getInstance().debug("Filled %d displays from recipe manager in %s", size() - lastSize, stopwatch.stop());
    }
    
    private List<RecipeHolder<?>> getAllSortedRecipes() {
        return GameInstance.getServer().getRecipeManager().getRecipes().parallelStream().sorted(RECIPE_COMPARATOR).toList();
    }
    
    public static class ServerDisplaysHolder extends DisplaysHolderImpl {
        private final List<Set<Display>> displaysByVersion = new ArrayList<>();
        private final Set<Display> pendingDisplays = new ReferenceOpenHashSet<>();
        private int timeUntilCollection = 0;
        
        @Override
        public void add(Display display, @Nullable Object origin) {
            super.add(display, origin);
            this.pendingDisplays.add(display);
            this.timeUntilCollection = 10;
        }
        
        @Override
        protected void removeFallout(Display display) {
            this.pendingDisplays.remove(display);
            this.displaysByVersion.forEach(set -> set.remove(display));
        }
        
        private void everySecond() {
            if (this.timeUntilCollection > 0) {
                if (--this.timeUntilCollection <= 0) {
                    this.displaysByVersion.add(new ReferenceOpenHashSet<>(this.pendingDisplays));
                    this.pendingDisplays.clear();
                }
            }
        }
    }
}
