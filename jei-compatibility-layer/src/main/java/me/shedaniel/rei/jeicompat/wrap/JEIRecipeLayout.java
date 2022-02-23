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

import lombok.experimental.ExtensionMethod;
import me.shedaniel.rei.api.common.entry.type.EntryType;
import me.shedaniel.rei.jeicompat.JEIPluginDetector;
import me.shedaniel.rei.jeicompat.ingredient.JEIGuiIngredientGroup;
import me.shedaniel.rei.jeicompat.ingredient.JEIGuiIngredientGroupFluid;
import me.shedaniel.rei.jeicompat.ingredient.JEIGuiIngredientGroupItem;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ingredient.IGuiFluidStackGroup;
import mezz.jei.api.gui.ingredient.IGuiIngredientGroup;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.IFocus;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@ExtensionMethod(JEIPluginDetector.class)
public class JEIRecipeLayout<T> implements IRecipeLayout {
    private final Map<EntryType<?>, JEIGuiIngredientGroup<?>> groups = new HashMap<>();
    public final JEIRecipeLayoutBuilder builder;
    
    public JEIRecipeLayout(JEIRecipeLayoutBuilder builder) {
        this.builder = builder;
    }
    
    @Override
    @NotNull
    public IGuiItemStackGroup getItemStacks() {
        return (IGuiItemStackGroup) getIngredientsGroup(VanillaTypes.ITEM);
    }
    
    @Override
    @NotNull
    public IGuiFluidStackGroup getFluidStacks() {
        return (IGuiFluidStackGroup) getIngredientsGroup(VanillaTypes.FLUID);
    }
    
    @Override
    @NotNull
    public <T> IGuiIngredientGroup<T> getIngredientsGroup(@NotNull IIngredientType<T> ingredientType) {
        return (IGuiIngredientGroup<T>) groups.computeIfAbsent(ingredientType.unwrapType(), type -> {
            if (Objects.equals(ingredientType.getIngredientClass(), ItemStack.class))
                return new JEIGuiIngredientGroupItem(ingredientType.cast(), builder);
            if (Objects.equals(ingredientType.getIngredientClass(), FluidStack.class))
                return new JEIGuiIngredientGroupFluid(ingredientType.cast(), builder);
            return new JEIGuiIngredientGroup<>(ingredientType, builder);
        });
    }
    
    public Map<EntryType<?>, JEIGuiIngredientGroup<?>> getGroups() {
        return groups;
    }
    
    @Nullable
    public IFocus<?> getFocus() {
        List<IFocus<?>> foci = JEIWrappedDisplay.getFoci();
        if (foci.isEmpty()) return null;
        return foci.get(0);
    }
    
    @Override
    @Nullable
    public <V> IFocus<V> getFocus(@NotNull IIngredientType<V> ingredientType) {
        IFocus<?> focus = getFocus();
        if (focus == null) return null;
        ITypedIngredient<V> typedIngredient = (ITypedIngredient<V>) focus.getTypedValue();
        return new JEIFocus<>(focus.getRole(), new JEITypedIngredient<>(typedIngredient.getType(), typedIngredient.getIngredient()));
    }
    
    @Override
    public void moveRecipeTransferButton(int posX, int posY) {
        builder.moveRecipeTransferButton(posX, posY);
    }
    
    @Override
    public void setShapeless() {
        builder.setShapeless();
    }
}
