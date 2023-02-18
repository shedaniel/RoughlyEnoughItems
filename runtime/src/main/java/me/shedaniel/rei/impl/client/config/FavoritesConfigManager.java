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

package me.shedaniel.rei.impl.client.config;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Jankson;
import me.shedaniel.rei.api.client.favorites.FavoriteEntry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionResult;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.List;

import static me.shedaniel.rei.impl.client.config.ConfigManagerImpl.buildJankson;

@ApiStatus.Internal
public class FavoritesConfigManager {
    private static final FavoritesConfigManager INSTANCE = new FavoritesConfigManager();
    private FavoritesConfigObject object;
    
    public FavoritesConfigManager() {
        AutoConfig.register(FavoritesConfigObject.class, (definition, configClass) -> new JanksonConfigSerializer<>(definition, configClass, buildJankson(Jankson.builder())));
    }
    
    public static FavoritesConfigManager getInstance() {
        return INSTANCE;
    }
    
    public void saveConfig() {
        AutoConfig.getConfigHolder(FavoritesConfigObject.class).registerLoadListener((configHolder, configObject) -> {
            object = configObject;
            return InteractionResult.PASS;
        });
        AutoConfig.getConfigHolder(FavoritesConfigObject.class).save();
    }
    
    public FavoritesConfigObject getConfig() {
        if (object == null) {
            object = AutoConfig.getConfigHolder(FavoritesConfigObject.class).getConfig();
        }
        return object;
    }
    
    public void syncFrom(ConfigManagerImpl manager) {
        ConfigObjectImpl.Basics basics = manager.getConfig().basics;
        FavoritesConfigObject config = this.getConfig();
        config.favorites.addAll(basics.favorites);
        config.hiddenFavorites.addAll(basics.hiddenFavorites);
        config.displays.addAll(basics.displayHistory);
        basics.favorites.clear();
        basics.hiddenFavorites.clear();
        basics.displayHistory.clear();
        manager.saveConfig();
        this.saveConfig();
    }
    
    @Config(name = "roughlyenoughitems/favorites")
    @Environment(EnvType.CLIENT)
    public static final class FavoritesConfigObject implements ConfigData {
        public List<FavoriteEntry> favorites = new ArrayList<>();
        public List<FavoriteEntry> hiddenFavorites = new ArrayList<>();
        public List<CompoundTag> displays = new ArrayList<>();
    }
}
