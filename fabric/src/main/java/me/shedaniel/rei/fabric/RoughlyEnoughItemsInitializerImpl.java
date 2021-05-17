/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020, 2021 shedaniel
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

package me.shedaniel.rei.fabric;

import com.google.common.collect.ImmutableSet;
import me.shedaniel.rei.RoughlyEnoughItemsState;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.SemanticVersion;
import net.fabricmc.loader.api.VersionParsingException;

public class RoughlyEnoughItemsInitializerImpl {
    public static boolean isClient() {
        return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT;
    }
    
    public static void checkMods() {
        ImmutableSet<String> requiredModules = isClient() ?
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
        if (!FabricLoader.getInstance().isModLoaded("architectury")) {
            RoughlyEnoughItemsState.error("Architectury API is not installed!", "https://www.curseforge.com/minecraft/mc-mods/architectury-fabric/files/all");
        }
        if (isClient()) {
            try {
                if (!FabricLoader.getInstance().isModLoaded("cloth-config2")) {
                    RoughlyEnoughItemsState.error("Cloth Config is not installed!", "https://www.curseforge.com/minecraft/mc-mods/cloth-config/files/all");
                } else if (SemanticVersion.parse(FabricLoader.getInstance().getModContainer("cloth-config2").get().getMetadata().getVersion().getFriendlyString()).compareTo(SemanticVersion.parse("4.10.9")) < 0) {
                    RoughlyEnoughItemsState.error("Your Cloth Config version is too old!", "https://www.curseforge.com/minecraft/mc-mods/cloth-config/files/all");
                }
            } catch (VersionParsingException e) {
                e.printStackTrace();
            }
        }
    }
    
    public static boolean isDev() {
        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }
}
