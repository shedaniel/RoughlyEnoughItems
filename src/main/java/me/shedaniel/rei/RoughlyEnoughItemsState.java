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

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Pair;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@ApiStatus.Internal
public class RoughlyEnoughItemsState {
    private RoughlyEnoughItemsState() {}
    
    private static List<Pair<String, String>> failedToLoad = new ArrayList<>();
    private static Set<String> failedToLoadSet = new LinkedHashSet<>();
    
    public static void failedToLoad(String reason) {
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER || FabricLoader.getInstance().isDevelopmentEnvironment())
            throw new RuntimeException(reason);
        if (RoughlyEnoughItemsState.failedToLoadSet.add(reason + " " + null))
            RoughlyEnoughItemsState.failedToLoad.add(new Pair<>(reason, null));
    }
    
    public static void failedToLoad(String reason, String link) {
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER || FabricLoader.getInstance().isDevelopmentEnvironment())
            throw new RuntimeException(reason + " " + link);
        if (RoughlyEnoughItemsState.failedToLoadSet.add(reason + " " + link))
            RoughlyEnoughItemsState.failedToLoad.add(new Pair<>(reason, link));
    }
    
    public static List<Pair<String, String>> getFailedToLoad() {
        return failedToLoad;
    }
}
