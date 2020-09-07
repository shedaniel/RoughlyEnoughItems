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

package me.shedaniel.rei.plugin.cooking;

import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.api.TransferRecipeDisplay;
import me.shedaniel.rei.server.ContainerInfo;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.crafting.AbstractCookingRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@OnlyIn(Dist.CLIENT)
public abstract class DefaultCookingDisplay implements TransferRecipeDisplay {
    private AbstractCookingRecipe recipe;
    private List<List<EntryStack>> input;
    private List<EntryStack> output;
    private float xp;
    private double cookTime;
    
    public DefaultCookingDisplay(AbstractCookingRecipe recipe) {
        this.recipe = recipe;
        this.input = EntryStack.ofIngredients(recipe.getIngredients());
        this.output = Collections.singletonList(EntryStack.create(recipe.getResultItem()));
        this.xp = recipe.getExperience();
        this.cookTime = recipe.getCookingTime();
    }
    
    @Override
    public @NotNull Optional<ResourceLocation> getRecipeLocation() {
        return Optional.ofNullable(recipe).map(AbstractCookingRecipe::getId);
    }
    
    @Override
    public @NotNull List<List<EntryStack>> getInputEntries() {
        return input;
    }
    
    @Override
    public @NotNull List<List<EntryStack>> getResultingEntries() {
        return Collections.singletonList(output);
    }
    
    @Override
    public @NotNull List<List<EntryStack>> getRequiredEntries() {
        return input;
    }
    
    public float getXp() {
        return xp;
    }
    
    public double getCookingTime() {
        return cookTime;
    }
    
    @ApiStatus.Internal
    public Optional<AbstractCookingRecipe> getOptionalRecipe() {
        return Optional.ofNullable(recipe);
    }
    
    @Override
    public int getWidth() {
        return 1;
    }
    
    @Override
    public int getHeight() {
        return 1;
    }
    
    @Override
    public List<List<EntryStack>> getOrganisedInputEntries(ContainerInfo<Container> containerInfo, Container container) {
        return input;
    }
    
}
