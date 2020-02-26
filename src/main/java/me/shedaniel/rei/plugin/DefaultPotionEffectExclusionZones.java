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

package me.shedaniel.rei.plugin;

import com.google.common.collect.Ordering;
import me.shedaniel.math.api.Rectangle;
import me.shedaniel.rei.impl.ScreenHelper;
import me.shedaniel.rei.listeners.AbstractInventoryScreenHooks;
import me.shedaniel.rei.listeners.ContainerScreenHooks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import net.minecraft.entity.effect.StatusEffectInstance;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public class DefaultPotionEffectExclusionZones implements Supplier<List<Rectangle>> {
    @Override
    public List<Rectangle> get() {
        if (!(ScreenHelper.getLastContainerScreen() instanceof AbstractInventoryScreen) || !((AbstractInventoryScreenHooks) ScreenHelper.getLastContainerScreen()).rei_doesOffsetGuiForEffects())
            return Collections.emptyList();
        Collection<StatusEffectInstance> activePotionEffects = MinecraftClient.getInstance().player.getStatusEffects();
        if (activePotionEffects.isEmpty())
            return Collections.emptyList();
        ContainerScreenHooks hooks = ScreenHelper.getLastContainerScreenHooks();
        List<Rectangle> list = new ArrayList<>();
        int x = hooks.rei_getContainerLeft() - 124;
        int y = hooks.rei_getContainerTop();
        int height = 33;
        if (activePotionEffects.size() > 5)
            height = 132 / (activePotionEffects.size() - 1);
        for (StatusEffectInstance instance : Ordering.natural().sortedCopy(activePotionEffects)) {
            list.add(new Rectangle(x, y, 166, height));
            y += height;
        }
        return list;
    }
}
