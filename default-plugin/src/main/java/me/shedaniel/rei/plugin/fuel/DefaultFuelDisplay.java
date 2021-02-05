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

package me.shedaniel.rei.plugin.fuel;

import me.shedaniel.rei.api.ingredient.EntryStack;
import me.shedaniel.rei.api.registry.display.Display;
import me.shedaniel.rei.plugin.DefaultPlugin;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;

import java.util.Collections;
import java.util.List;

@Environment(EnvType.CLIENT)
public class DefaultFuelDisplay implements Display {
    private EntryStack<?> fuel;
    private int fuelTime;
    
    public DefaultFuelDisplay(EntryStack<?> fuel, int fuelTime) {
        this.fuel = fuel;
        this.fuelTime = fuelTime;
    }
    
    @Override
    public List<? extends List<? extends EntryStack<?>>> getInputEntries() {
        return Collections.singletonList(Collections.singletonList(fuel));
    }
    
    @Override
    public List<? extends List<? extends EntryStack<?>>> getResultingEntries() {
        return Collections.emptyList();
    }
    
    @Override
    public ResourceLocation getRecipeCategory() {
        return DefaultPlugin.FUEL;
    }
    
    public int getFuelTime() {
        return fuelTime;
    }
}
