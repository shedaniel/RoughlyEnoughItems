/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020, 2021 shedaniel
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

import me.shedaniel.architectury.hooks.forge.FluidStackHooksForge;
import me.shedaniel.rei.api.common.entry.comparison.EntryComparator;
import me.shedaniel.rei.api.common.entry.comparison.FluidComparatorRegistry;
import me.shedaniel.rei.api.common.entry.comparison.ItemComparatorRegistry;
import mezz.jei.api.ingredients.subtypes.IIngredientSubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

import static me.shedaniel.rei.jeicompat.JEIPluginDetector.wrapContext;

public enum JEISubtypeRegistration implements ISubtypeRegistration {
    INSTANCE;
    
    @Override
    public void registerSubtypeInterpreter(@NotNull Item item, @NotNull ISubtypeInterpreter interpreter) {
        registerSubtypeInterpreter(item, (IIngredientSubtypeInterpreter) interpreter);
    }
    
    @Override
    public void registerSubtypeInterpreter(@NotNull Item item, @NotNull IIngredientSubtypeInterpreter<ItemStack> interpreter) {
        ItemComparatorRegistry.getInstance().register(wrapItemComparator(interpreter), item);
    }
    
    @Override
    public void registerSubtypeInterpreter(@NotNull Fluid fluid, @NotNull IIngredientSubtypeInterpreter<FluidStack> interpreter) {
        FluidComparatorRegistry.getInstance().register(wrapFluidComparator(interpreter), fluid);
    }
    
    @Override
    public void useNbtForSubtypes(@NotNull Item @NotNull ... items) {
        ItemComparatorRegistry.getInstance().registerNbt(items);
    }
    
    @Override
    public void useNbtForSubtypes(@NotNull Fluid @NotNull ... fluids) {
        FluidComparatorRegistry.getInstance().registerNbt(fluids);
    }
    
    @Override
    public boolean hasSubtypeInterpreter(@NotNull ItemStack itemStack) {
        return ItemComparatorRegistry.getInstance().containsComparator(itemStack.getItem());
    }
    
    @Override
    public boolean hasSubtypeInterpreter(@NotNull FluidStack fluidStack) {
        return FluidComparatorRegistry.getInstance().containsComparator(fluidStack.getFluid());
    }
    
    private static EntryComparator<ItemStack> wrapItemComparator(IIngredientSubtypeInterpreter<ItemStack> interpreter) {
        return (context, stack) -> interpreter.apply(stack, wrapContext(context)).hashCode();
    }
    
    private static EntryComparator<me.shedaniel.architectury.fluid.FluidStack> wrapFluidComparator(IIngredientSubtypeInterpreter<FluidStack> interpreter) {
        return (context, stack) -> interpreter.apply(FluidStackHooksForge.toForge(stack), wrapContext(context)).hashCode();
    }
}
