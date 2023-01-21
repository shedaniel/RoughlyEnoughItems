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

package me.shedaniel.rei.plugin.client.exclusionzones;

import com.google.common.collect.Lists;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.registry.screen.ExclusionZonesProvider;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraft.world.inventory.RecipeBookMenu;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Environment(EnvType.CLIENT)
public class DefaultRecipeBookExclusionZones implements ExclusionZonesProvider<RecipeUpdateListener> {
    @Override
    public Collection<Rectangle> provide(RecipeUpdateListener screen) {
        if (!(screen instanceof AbstractContainerScreen<?> containerScreen) || !(((AbstractContainerScreen<?>) screen).getMenu() instanceof RecipeBookMenu) ||
            !Minecraft.getInstance().player.getRecipeBook().isOpen(((RecipeBookMenu<?>) ((AbstractContainerScreen<?>) screen).getMenu()).getRecipeBookType()))
            return Collections.emptyList();
        RecipeBookComponent recipeBookWidget = screen.getRecipeBookComponent();
        List<Rectangle> l = Lists.newArrayList(new Rectangle(containerScreen.leftPos - 4 - 145, containerScreen.topPos, 4 + 145 + 30, containerScreen.imageHeight));
        int size = recipeBookWidget.tabButtons.size();
        if (size > 0)
            l.add(new Rectangle(containerScreen.leftPos - 4 - 145 - 30, containerScreen.topPos, 30, size * 27));
        return l;
    }
}
