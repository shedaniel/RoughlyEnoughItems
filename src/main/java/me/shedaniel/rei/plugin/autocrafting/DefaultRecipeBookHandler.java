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

package me.shedaniel.rei.plugin.autocrafting;

import me.shedaniel.rei.api.AutoTransferHandler;
import me.shedaniel.rei.api.RecipeDisplay;
import me.shedaniel.rei.api.TransferRecipeDisplay;
import me.shedaniel.rei.impl.ScreenHelper;
import me.shedaniel.rei.plugin.cooking.DefaultCookingDisplay;
import me.shedaniel.rei.plugin.crafting.DefaultCraftingDisplay;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.recipebook.RecipeBookProvider;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.container.CraftingContainer;
import net.minecraft.container.CraftingTableContainer;
import net.minecraft.container.PlayerContainer;
import net.minecraft.recipe.Recipe;

public class DefaultRecipeBookHandler implements AutoTransferHandler {
    @Override
    public Result handle(Context context) {
        if (context.getRecipe() instanceof TransferRecipeDisplay && DefaultCategoryHandler.canUseMovePackets())
            return Result.createNotApplicable();
        RecipeDisplay display = context.getRecipe();
        if (!(context.getContainer() instanceof CraftingContainer))
            return Result.createNotApplicable();
        CraftingContainer<?> container = (CraftingContainer<?>) context.getContainer();
        if (display instanceof DefaultCraftingDisplay) {
            DefaultCraftingDisplay craftingDisplay = (DefaultCraftingDisplay) display;
            if (craftingDisplay.getOptionalRecipe().isPresent()) {
                int h = -1, w = -1;
                if (container instanceof CraftingTableContainer) {
                    h = 3;
                    w = 3;
                } else if (container instanceof PlayerContainer) {
                    h = 2;
                    w = 2;
                }
                if (h == -1 || w == -1)
                    return Result.createNotApplicable();
                Recipe<?> recipe = (craftingDisplay).getOptionalRecipe().get();
                if (craftingDisplay.getHeight() > h || craftingDisplay.getWidth() > w)
                    return Result.createFailed(I18n.translate("error.rei.transfer.too_small", h, w));
                if (!context.getMinecraft().player.getRecipeBook().contains(recipe))
                    return Result.createFailed(I18n.translate("error.rei.recipe.not.unlocked"));
                if (!context.isActuallyCrafting())
                    return Result.createSuccessful();
                context.getMinecraft().openScreen(context.getContainerScreen());
                if (context.getContainerScreen() instanceof RecipeBookProvider)
                    ((RecipeBookProvider) context.getContainerScreen()).getRecipeBookWidget().ghostSlots.reset();
                context.getMinecraft().interactionManager.clickRecipe(container.syncId, recipe, Screen.hasShiftDown());
                ScreenHelper.getLastOverlay().init();
            }
        } else if (display instanceof DefaultCookingDisplay) {
            DefaultCookingDisplay defaultDisplay = (DefaultCookingDisplay) display;
            if (defaultDisplay.getOptionalRecipe().isPresent()) {
                Recipe<?> recipe = (defaultDisplay).getOptionalRecipe().get();
                if (!context.getMinecraft().player.getRecipeBook().contains(recipe))
                    return Result.createFailed(I18n.translate("error.rei.recipe.not.unlocked"));
                if (!context.isActuallyCrafting())
                    return Result.createSuccessful();
                context.getMinecraft().openScreen(context.getContainerScreen());
                if (context.getContainerScreen() instanceof RecipeBookProvider)
                    ((RecipeBookProvider) context.getContainerScreen()).getRecipeBookWidget().ghostSlots.reset();
                context.getMinecraft().interactionManager.clickRecipe(container.syncId, recipe, Screen.hasShiftDown());
                ScreenHelper.getLastOverlay().init();
            }
        }
        return Result.createNotApplicable();
    }
    
    @Override
    public double getPriority() {
        return -20;
    }
}
