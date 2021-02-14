/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020 shedaniel
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

import com.google.common.collect.ImmutableSet;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.SemanticVersion;
import net.fabricmc.loader.api.VersionParsingException;

import java.lang.reflect.InvocationTargetException;

public class RoughlyEnoughItemsInitializer implements ModInitializer {
    @Override
    public void onInitialize() {
        checkRequiredFabricModules();
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            checkClothConfig();
            checkModMenu();
        }
        
        if (RoughlyEnoughItemsState.getErrors().isEmpty()) {
            initializeEntryPoint("me.shedaniel.rei.RoughlyEnoughItemsNetwork");
            
            if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
                initializeEntryPoint("me.shedaniel.rei.RoughlyEnoughItemsCore");
                initializeEntryPoint("me.shedaniel.rei.impl.ClientHelperImpl");
                initializeEntryPoint("me.shedaniel.rei.impl.ScreenHelper");
            }
        }
        
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            initializeEntryPoint("me.shedaniel.rei.impl.ErrorDisplayer");
        }
    }
    
    public void initializeEntryPoint(String className) {
        try {
            Object instance = Class.forName(className).getConstructor().newInstance();
            if (instance instanceof ModInitializer) {
                ((ModInitializer) instance).onInitialize();
            } else if (instance instanceof ClientModInitializer) {
                ((ClientModInitializer) instance).onInitializeClient();
            }
        } catch (InstantiationException | InvocationTargetException | IllegalAccessException | ClassNotFoundException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
    
    public static void checkRequiredFabricModules() {
        ImmutableSet<String> requiredModules = FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT ?
                ImmutableSet.<String>builder()
                        .add("fabric-api-base")
                        .add("fabric-resource-loader-v0")
                        .add("fabric-networking-v0")
                        .add("fabric-lifecycle-events-v1")
                        .add("fabric-rendering-fluids-v1")
                        .build() :
                ImmutableSet.<String>builder()
                        .add("fabric-api-base")
                        .add("fabric-resource-loader-v0")
                        .add("fabric-networking-v0")
                        .add("fabric-lifecycle-events-v1")
                        .build();
        for (String module : requiredModules) {
            boolean moduleLoaded = FabricLoader.getInstance().isModLoaded(module);
            if (!moduleLoaded) {
                RoughlyEnoughItemsState.error("Fabric API is not installed!", "https://www.curseforge.com/minecraft/mc-mods/fabric-api/files/all");
                break;
            }
        }
    }
    
    public static void checkClothConfig() {
        try {
            if (!FabricLoader.getInstance().isModLoaded("cloth-config2")) {
                RoughlyEnoughItemsState.error("Cloth Config is not installed!", "https://www.curseforge.com/minecraft/mc-mods/cloth-config/files/all");
            } else if (SemanticVersion.parse(FabricLoader.getInstance().getModContainer("cloth-config2").get().getMetadata().getVersion().getFriendlyString()).compareTo(SemanticVersion.parse("4.10.9")) < 0) {
                RoughlyEnoughItemsState.error("Your Cloth Config version is too old!", "https://www.curseforge.com/minecraft/mc-mods/cloth-config/files/all");
            }
        } catch (VersionParsingException e) {
            RoughlyEnoughItemsState.error("Failed to parse Cloth Config version: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public static void checkModMenu() {
        try {
            if (FabricLoader.getInstance().isModLoaded("modmenu")) {
                if (SemanticVersion.parse(FabricLoader.getInstance().getModContainer("modmenu").get().getMetadata().getVersion().getFriendlyString()).compareTo(SemanticVersion.parse("1.16.7")) < 0) {
                    RoughlyEnoughItemsState.error("Your Mod Menu version is too old!", "https://www.curseforge.com/minecraft/mc-mods/modmenu/files/all");
                }
            }
        } catch (VersionParsingException e) {
            RoughlyEnoughItemsState.error("Failed to parse Mod Menu version: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
