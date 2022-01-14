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

package me.shedaniel.rei.plugin.client.exclusionzones;

import com.google.common.collect.Ordering;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.config.ConfigObject;
import me.shedaniel.rei.api.client.registry.screen.ExclusionZonesProvider;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.world.effect.MobEffectInstance;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Environment(EnvType.CLIENT)
public class DefaultPotionEffectExclusionZones implements ExclusionZonesProvider<EffectRenderingInventoryScreen<?>> {
    @Override
    public Collection<Rectangle> provide(EffectRenderingInventoryScreen<?> screen) {
        if (!screen.canSeeEffects())
            return Collections.emptyList();
        boolean leftSideMobEffects = ConfigObject.getInstance().isLeftSideMobEffects();
        Collection<MobEffectInstance> activePotionEffects = Minecraft.getInstance().player.getActiveEffects();
        int x;
        boolean fullWidth;
        
        if (!leftSideMobEffects) {
            x = screen.leftPos + screen.imageWidth + 2;
            int availableWidth = screen.width - x;
            fullWidth = availableWidth >= 120;
            
            if (availableWidth < 32) {
                return Collections.emptyList();
            }
        } else {
            fullWidth = screen.leftPos >= 120;
            x = screen.leftPos - (fullWidth ? 124 : 36);
        }
        
        if (activePotionEffects.isEmpty())
            return Collections.emptyList();
        List<Rectangle> zones = new ArrayList<>();
        int y = screen.topPos;
        int height = 33;
        if (activePotionEffects.size() > 5)
            height = 132 / (activePotionEffects.size() - 1);
        for (MobEffectInstance instance : Ordering.natural().sortedCopy(activePotionEffects)) {
            zones.add(new Rectangle(x, y, fullWidth ? 120 : 32, 32));
            y += height;
        }
        return zones;
    }
}
