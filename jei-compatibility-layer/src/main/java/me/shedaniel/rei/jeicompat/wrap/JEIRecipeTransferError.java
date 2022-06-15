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

package me.shedaniel.rei.jeicompat.wrap;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

import static me.shedaniel.rei.jeicompat.JEIPluginDetector.TODO;

public class JEIRecipeTransferError implements IRecipeTransferError {
    private final Type type;
    private final Component text;
    @Nullable
    private final Collection<IRecipeSlotView> redSlots;
    
    public JEIRecipeTransferError(Type type, Component text, Collection<IRecipeSlotView> redSlots) {
        this.type = type;
        this.text = text;
        this.redSlots = redSlots;
    }
    
    @Override
    public Type getType() {
        return type;
    }
    
    public Component getText() {
        return text;
    }
    
    public Collection<IRecipeSlotView> getRedSlots() {
        return redSlots;
    }
    
    @Override
    public void showError(PoseStack matrixStack, int mouseX, int mouseY, IRecipeSlotsView recipeLayout, int recipeX, int recipeY) {
        TODO();
    }
}
