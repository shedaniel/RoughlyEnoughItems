/*
 * Copyright (c) 2018, 2019, 2020 shedaniel
 * Licensed under the MIT License (the "License").
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
