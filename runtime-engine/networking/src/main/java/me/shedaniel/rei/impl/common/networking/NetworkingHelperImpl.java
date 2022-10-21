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

package me.shedaniel.rei.impl.common.networking;

import me.shedaniel.rei.api.common.networking.NetworkModule;
import me.shedaniel.rei.api.common.networking.NetworkModuleKey;
import me.shedaniel.rei.api.common.networking.NetworkingHelper;
import me.shedaniel.rei.impl.common.Internals;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;

public class NetworkingHelperImpl implements NetworkingHelper {
    private static final Map<NetworkModuleKey<?>, NetworkModule<?>> MODULES = new HashMap<>();
    
    static {
        for (NetworkModule<?> module : Internals.resolveServices(NetworkModule.class)) {
            MODULES.put(module.getKey(), module);
        }
    }
    
    @Override
    public <T> boolean has(NetworkModuleKey<T> moduleKey) {
        return MODULES.containsKey(moduleKey);
    }
    
    @Override
    public <T> boolean canUse(NetworkModuleKey<T> moduleKey) {
        return has(moduleKey) && MODULES.get(moduleKey).canUse(null);
    }
    
    @Override
    public <T> boolean canPlayerUse(ServerPlayer player, NetworkModuleKey<T> moduleKey) {
        return has(moduleKey) && MODULES.get(moduleKey).canUse(player);
    }
    
    @Override
    public <T> void send(Object target, NetworkModuleKey<T> moduleKey, T data) {
        if (canUse(moduleKey)) {
            ((NetworkModule<T>) MODULES.get(moduleKey)).send(target, data);
        }
    }
    
    @SuppressWarnings("Convert2Lambda")
    @Override
    @Environment(EnvType.CLIENT)
    public Client client() {
        return new Client() {
            @Environment(EnvType.CLIENT)
            @Override
            public boolean hasOperatorPermission() {
                try {
                    return Minecraft.getInstance().getConnection().getSuggestionsProvider().hasPermission(1);
                } catch (NullPointerException e) {
                    return true;
                }
            }
        };
    }
    
    @Override
    public void startReload() {
    }
}
