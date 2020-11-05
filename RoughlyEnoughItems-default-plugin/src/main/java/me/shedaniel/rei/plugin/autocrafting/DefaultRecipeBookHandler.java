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
import me.shedaniel.rei.api.ClientHelper;
import me.shedaniel.rei.api.RecipeDisplay;
import me.shedaniel.rei.api.TransferRecipeDisplay;
import me.shedaniel.rei.plugin.cooking.DefaultCookingDisplay;
import me.shedaniel.rei.plugin.crafting.DefaultCraftingDisplay;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.item.crafting.Recipe;
import org.jetbrains.annotations.NotNull;

@Environment(EnvType.CLIENT)
public class DefaultRecipeBookHandler implements AutoTransferHandler {
    @NotNull
    @Override
    public Result handle(@NotNull Context context) {
        if (context.getRecipe() instanceof TransferRecipeDisplay && ClientHelper.getInstance().canUseMovePackets())
            return Result.createNotApplicable();
        RecipeDisplay display = context.getRecipe();
        if (!(context.getContainer() instanceof RecipeBookMenu))
            return Result.createNotApplicable();
        RecipeBookMenu<?> container = (RecipeBookMenu<?>) context.getContainer();
        if (container == null)
            return Result.createNotApplicable();
        if (display instanceof DefaultCraftingDisplay) {
            DefaultCraftingDisplay craftingDisplay = (DefaultCraftingDisplay) display;
            if (craftingDisplay.getOptionalRecipe().isPresent()) {
                int h = -1, w = -1;
                if (container instanceof CraftingMenu) {
                    h = 3;
                    w = 3;
                } else if (container instanceof InventoryMenu) {
                    h = 2;
                    w = 2;
                }
                if (h == -1 || w == -1)
                    return Result.createNotApplicable();
                Recipe<?> recipe = (craftingDisplay).getOptionalRecipe().get();
                if (craftingDisplay.getHeight() > h || craftingDisplay.getWidth() > w)
                    return Result.createFailed(I18n.get("error.rei.transfer.too_small", h, w));
                if (!context.getMinecraft().player.getRecipeBook().contains(recipe))
                    return Result.createFailed(I18n.get("error.rei.recipe.not.unlocked"));
                if (!context.isActuallyCrafting())
                    return Result.createSuccessful();
                context.getMinecraft().setScreen(context.getContainerScreen());
                if (context.getContainerScreen() instanceof RecipeUpdateListener)
                    ((RecipeUpdateListener) context.getContainerScreen()).getRecipeBookComponent().ghostRecipe.clear();
                context.getMinecraft().gameMode.handlePlaceRecipe(container.containerId, recipe, Screen.hasShiftDown());
                return Result.createSuccessful();
            }
        } else if (display instanceof DefaultCookingDisplay) {
            DefaultCookingDisplay defaultDisplay = (DefaultCookingDisplay) display;
            if (defaultDisplay.getOptionalRecipe().isPresent()) {
                Recipe<?> recipe = (defaultDisplay).getOptionalRecipe().get();
                if (!context.getMinecraft().player.getRecipeBook().contains(recipe))
                    return Result.createFailed(I18n.get("error.rei.recipe.not.unlocked"));
                if (!context.isActuallyCrafting())
                    return Result.createSuccessful();
                context.getMinecraft().setScreen(context.getContainerScreen());
                if (context.getContainerScreen() instanceof RecipeUpdateListener)
                    ((RecipeUpdateListener) context.getContainerScreen()).getRecipeBookComponent().ghostRecipe.clear();
                context.getMinecraft().gameMode.handlePlaceRecipe(container.containerId, recipe, Screen.hasShiftDown());
                return Result.createSuccessful();
            }
        }
        return Result.createNotApplicable();
    }
    
    @Override
    public double getPriority() {
        return -20;
    }
}
