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

package me.shedaniel.rei.forge;

import me.shedaniel.rei.RoughlyEnoughItemsState;
import me.shedaniel.rei.impl.init.PrimitivePlatformAdapter;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLLoader;
import org.apache.maven.artifact.versioning.ComparableVersion;

public class PrimitivePlatformAdapterImpl implements PrimitivePlatformAdapter {
    @Override
    public boolean isClient() {
        return FMLEnvironment.dist == Dist.CLIENT;
    }
    
    @Override
    public void checkMods() {
        if (ModList.get().isLoaded("moreoverlays")) {
            RoughlyEnoughItemsState.error("REI is not compatible with MoreOverlays, and actually contains Builtin Inventory Highlighting, other features can be installed via different mods!");
        }
    }
    
    @Override
    public boolean isDev() {
        return !FMLLoader.isProduction();
    }
    
    @Override
    public String getMinecraftVersion() {
        return ModList.get().getModContainerById("minecraft").get().getModInfo().getVersion().toString();
    }
    
    @Override
    public int compareVersions(String version1, String version2) {
        ComparableVersion v1 = new ComparableVersion(version1);
        ComparableVersion v2 = new ComparableVersion(version2);
        
        try {
            return v1.compareTo(v2);
        } catch (IllegalStateException e) {
            new IllegalStateException("Failed to compare versions: " + version1 + " and " + version2, e).printStackTrace();
            return 0;
        }
    }
}
