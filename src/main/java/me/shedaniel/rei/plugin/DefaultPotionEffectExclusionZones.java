/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.plugin;

import com.google.common.collect.Ordering;
import me.shedaniel.rei.client.ScreenHelper;
import me.shedaniel.rei.api.AbstractInventoryScreenHooks;
import me.shedaniel.rei.api.ContainerScreenHooks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.potion.PotionEffect;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class DefaultPotionEffectExclusionZones implements Function<Boolean, List<Rectangle>> {
    @Override
    public List<Rectangle> apply(Boolean isOnRightSide) {
        if (isOnRightSide || !(ScreenHelper.getLastContainerScreen() instanceof GuiContainer) || !((AbstractInventoryScreenHooks) ScreenHelper.getLastContainerScreen()).rei_doesOffsetGuiForEffects())
            return Collections.emptyList();
        Collection<PotionEffect> activePotionEffects = Minecraft.getInstance().player.getActivePotionEffects();
        if (activePotionEffects.isEmpty())
            return Collections.emptyList();
        ContainerScreenHooks hooks = ScreenHelper.getLastContainerScreenHooks();
        List<Rectangle> list = new ArrayList<>();
        int x = hooks.rei_getContainerLeft() - 124;
        int y = hooks.rei_getContainerTop();
        int height = 33;
        if (activePotionEffects.size() > 5)
            height = 132 / (activePotionEffects.size() - 1);
        for(PotionEffect instance : Ordering.natural().sortedCopy(activePotionEffects)) {
            list.add(new Rectangle(x, y, 166, height));
            y += height;
        }
        return list;
    }
}
