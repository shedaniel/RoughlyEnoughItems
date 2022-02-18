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

package me.shedaniel.rei.jeicompat.ingredient;

import me.shedaniel.rei.jeicompat.wrap.JEIRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IGuiFluidStackGroup;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

public class JEIGuiIngredientGroupFluid extends JEIGuiIngredientGroup<FluidStack> implements IGuiFluidStackGroup {
    public JEIGuiIngredientGroupFluid(IIngredientType<FluidStack> type, JEIRecipeLayoutBuilder builder) {
        super(type, builder);
    }
    
    @Override
    public void init(int slotIndex, boolean input, int xPosition, int yPosition, int width, int height, int capacityMb, boolean showCapacity, @Nullable IDrawable overlay) {
        SlotWrapper slot = getSlot(slotIndex);
        slot.slot.role = input ? RecipeIngredientRole.INPUT : RecipeIngredientRole.OUTPUT;
        slot.slot.slot.getBounds().setLocation(xPosition - 1, yPosition - 1);
        slot.slot.slot.getBounds().setSize(width + 2, height + 2);
        slot.slot.capacityMb = capacityMb;
        slot.slot.setOverlay(overlay, 0, 0);
    }
}
