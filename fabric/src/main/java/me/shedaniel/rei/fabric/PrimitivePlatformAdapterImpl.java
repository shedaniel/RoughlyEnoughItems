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

package me.shedaniel.rei.fabric;

import me.shedaniel.rei.impl.init.PrimitivePlatformAdapter;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.SemanticVersion;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.VersionParsingException;

public class PrimitivePlatformAdapterImpl implements PrimitivePlatformAdapter {
    @Override
    public boolean isClient() {
        return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT;
    }
    
    @Override
    public void checkMods() {
    }
    
    @Override
    public boolean isDev() {
        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }
    
    @Override
    public String getMinecraftVersion() {
        return FabricLoader.getInstance().getModContainer("minecraft").get().getMetadata().getVersion().getFriendlyString();
    }
    
    @Override
    public int compareVersions(String version1, String version2) {
        version1 = version1.contains("-") ? version1.substring(0, version1.indexOf("-")) : version1;
        version2 = version2.contains("-") ? version2.substring(0, version2.indexOf("-")) : version2;
        version1 = version1.contains("+") ? version1.substring(0, version1.indexOf("+")) : version1;
        version2 = version2.contains("+") ? version2.substring(0, version2.indexOf("+")) : version2;
        Version v1, v2;
        
        try {
            v1 = SemanticVersion.parse(version1);
        } catch (VersionParsingException e) {
            new IllegalStateException("Failed to parse version: " + version1, e).printStackTrace();
            return 0;
        }
        
        try {
            v2 = SemanticVersion.parse(version2);
        } catch (VersionParsingException e) {
            new IllegalStateException("Failed to parse version: " + version2, e).printStackTrace();
            return 0;
        }
        
        return v1.compareTo(v2);
    }
}
