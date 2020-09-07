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
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.recipebook.IRecipeShownListener;
import net.minecraft.client.gui.recipebook.RecipeBookGui;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.inventory.container.RecipeBookContainer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

@OnlyIn(Dist.CLIENT)
public class DefaultRecipeBookExclusionZones implements Supplier<List<Rectangle>> {
    
    @Override
    public List<Rectangle> get() {
        if (!(Minecraft.getInstance().screen instanceof IRecipeShownListener) || !(REIHelper.getInstance().getPreviousContainerScreen().getMenu() instanceof RecipeBookContainer) ||
            !Minecraft.getInstance().player.getRecipeBook().isOpen(((RecipeBookContainer<?>) REIHelper.getInstance().getPreviousContainerScreen().getMenu()).getRecipeBookType()))
            return Collections.emptyList();
        RecipeBookGui recipeBookWidget = ((IRecipeShownListener) Minecraft.getInstance().screen).getRecipeBookComponent();
        ContainerScreen<?> containerScreen = REIHelper.getInstance().getPreviousContainerScreen();
        List<Rectangle> l = Lists.newArrayList(new Rectangle(containerScreen.getGuiLeft() - 4 - 145, containerScreen.getGuiTop(), 4 + 145 + 30, containerScreen.getYSize()));
        int size = recipeBookWidget.tabButtons.size();
        if (size > 0)
            l.add(new Rectangle(containerScreen.getGuiLeft() - 4 - 145 - 30, containerScreen.getGuiTop(), 30, size * 27));
        return l;
    }
    
}
