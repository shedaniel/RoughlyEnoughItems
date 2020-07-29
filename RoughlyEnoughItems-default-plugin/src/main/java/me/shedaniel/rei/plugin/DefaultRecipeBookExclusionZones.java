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

import com.google.common.collect.Lists;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.REIHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.ContainerScreen;
import net.minecraft.client.gui.screen.recipebook.RecipeBookProvider;
import net.minecraft.client.gui.screen.recipebook.RecipeBookWidget;
import net.minecraft.container.CraftingContainer;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

@Environment(EnvType.CLIENT)
public class DefaultRecipeBookExclusionZones implements Supplier<List<Rectangle>> {
    
    @Override
    public List<Rectangle> get() {
        if (!(MinecraftClient.getInstance().currentScreen instanceof RecipeBookProvider) || !(REIHelper.getInstance().getPreviousContainerScreen().getContainer() instanceof CraftingContainer) ||
            !MinecraftClient.getInstance().player.getRecipeBook().isGuiOpen(((CraftingContainer<?>) REIHelper.getInstance().getPreviousContainerScreen().getContainer()).getCategory()))
            return Collections.emptyList();
        RecipeBookWidget recipeBookWidget = ((RecipeBookProvider) MinecraftClient.getInstance().currentScreen).getRecipeBookWidget();
        ContainerScreen<?> containerScreen = REIHelper.getInstance().getPreviousContainerScreen();
        List<Rectangle> l = Lists.newArrayList(new Rectangle(containerScreen.x - 4 - 145, containerScreen.y, 4 + 145 + 30, containerScreen.containerHeight));
        int size = recipeBookWidget.tabButtons.size();
        if (size > 0)
            l.add(new Rectangle(containerScreen.x - 4 - 145 - 30, containerScreen.y, 30, size * 27));
        return l;
    }
    
}
