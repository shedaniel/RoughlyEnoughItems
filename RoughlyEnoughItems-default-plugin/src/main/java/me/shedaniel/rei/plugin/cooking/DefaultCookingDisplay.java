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
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.level.block.entity.FurnaceBlockEntity;
import org.jetbrains.annotations.ApiStatus;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Environment(EnvType.CLIENT)
public abstract class DefaultCookingDisplay implements TransferRecipeDisplay {
    private static List<EntryStack> fuel;
    
    static {
        fuel = FurnaceBlockEntity.getFuel().keySet().stream().map(Item::getDefaultInstance).map(EntryStack::create).map(e -> e.setting(EntryStack.Settings.TOOLTIP_APPEND_EXTRA, stack -> Collections.singletonList(new TranslatableComponent("category.rei.smelting.fuel").withStyle(ChatFormatting.YELLOW)))).collect(Collectors.toList());
    }
    
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
    
    public static List<EntryStack> getFuel() {
        return fuel;
    }
    
    @Override
    public Optional<ResourceLocation> getRecipeLocation() {
        return Optional.ofNullable(recipe).map(AbstractCookingRecipe::getId);
    }
    
    @Override
    public List<List<EntryStack>> getInputEntries() {
        return input;
    }
    
    @Override
    public List<List<EntryStack>> getResultingEntries() {
        return Collections.singletonList(output);
    }
    
    @Override
    public List<List<EntryStack>> getRequiredEntries() {
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
    public List<List<EntryStack>> getOrganisedInputEntries(ContainerInfo<AbstractContainerMenu> containerInfo, AbstractContainerMenu container) {
        return input;
    }
    
}
