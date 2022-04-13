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

import dev.architectury.hooks.fluid.forge.FluidStackHooksForge;
import lombok.experimental.ExtensionMethod;
import me.shedaniel.rei.api.common.entry.comparison.EntryComparator;
import me.shedaniel.rei.api.common.entry.comparison.FluidComparatorRegistry;
import me.shedaniel.rei.api.common.entry.comparison.ItemComparatorRegistry;
import me.shedaniel.rei.api.common.entry.type.EntryType;
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes;
import me.shedaniel.rei.jeicompat.JEIPluginDetector;
import mezz.jei.api.ingredients.IIngredientTypeWithSubtypes;
import mezz.jei.api.ingredients.subtypes.IIngredientSubtypeInterpreter;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@ExtensionMethod(JEIPluginDetector.class)
public enum JEISubtypeRegistration implements ISubtypeRegistration {
    INSTANCE;
    
    @Override
    public <B, I> void registerSubtypeInterpreter(IIngredientTypeWithSubtypes<B, I> type, B base, IIngredientSubtypeInterpreter<I> interpreter) {
        EntryType<I> reiType = type.unwrapType();
        if (reiType == VanillaEntryTypes.ITEM) {
            registerSubtypeInterpreter((Item) base, (IIngredientSubtypeInterpreter<ItemStack>) interpreter);
        } else if (reiType == VanillaEntryTypes.FLUID) {
            registerSubtypeInterpreter((Fluid) base, (IIngredientSubtypeInterpreter<FluidStack>) interpreter);
        }
    }
    
    @Override
    public void registerSubtypeInterpreter(@NotNull Item item, @NotNull IIngredientSubtypeInterpreter<ItemStack> interpreter) {
        if (interpreter == null) return;
        ItemComparatorRegistry.getInstance().register(wrapItemComparator(interpreter), item);
    }
    
    @Override
    public void registerSubtypeInterpreter(@NotNull Fluid fluid, @NotNull IIngredientSubtypeInterpreter<FluidStack> interpreter) {
        if (interpreter == null) return;
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
        return (context, stack) -> {
            try {
                return Objects.hashCode(interpreter.apply(stack, context.wrapContext()));
            } catch (NullPointerException e) {
                return 0;
            }
        };
    }
    
    private static EntryComparator<dev.architectury.fluid.FluidStack> wrapFluidComparator(IIngredientSubtypeInterpreter<FluidStack> interpreter) {
        return (context, stack) -> {
            try {
                return Objects.hashCode(interpreter.apply(FluidStackHooksForge.toForge(stack), context.wrapContext()));
            } catch (NullPointerException e) {
                return 0;
            }
        };
    }
}
