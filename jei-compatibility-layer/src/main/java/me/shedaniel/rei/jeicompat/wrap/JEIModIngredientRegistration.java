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

import me.shedaniel.rei.api.client.registry.entry.EntryRegistry;
import me.shedaniel.rei.api.common.entry.type.EntryType;
import me.shedaniel.rei.api.common.entry.type.EntryTypeRegistry;
import me.shedaniel.rei.api.common.util.CollectionUtils;
import me.shedaniel.rei.jeicompat.JEIPluginDetector;
import mezz.jei.api.helpers.IColorHelper;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.subtypes.ISubtypeManager;
import mezz.jei.api.registration.IModIngredientRegistration;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Locale;

import static me.shedaniel.rei.jeicompat.JEIPluginDetector.TODO;

public class JEIModIngredientRegistration implements IModIngredientRegistration {
    private final JEIPluginDetector.JEIPluginWrapper wrapper;
    private final EntryTypeRegistry registry;
    
    public JEIModIngredientRegistration(JEIPluginDetector.JEIPluginWrapper wrapper, EntryTypeRegistry registry) {
        this.wrapper = wrapper;
        this.registry = registry;
    }
    
    @Override
    @NotNull
    public ISubtypeManager getSubtypeManager() {
        return JEISubtypeManager.INSTANCE;
    }
    
    @Override
    @NotNull
    public IColorHelper getColorHelper() {
        return JEIColorHelper.INSTANCE;
    }
    
    @Override
    public <V> void register(@NotNull IIngredientType<V> ingredientType, @NotNull Collection<V> allIngredients, @NotNull IIngredientHelper<V> ingredientHelper,
            @NotNull IIngredientRenderer<V> ingredientRenderer) {
        ResourceLocation location = new ResourceLocation(wrapper.backingPlugin.getPluginUid() + "_" + ingredientType.getIngredientClass().getSimpleName().toLowerCase(Locale.ROOT));
        registry.register(location, new JEIEntryDefinition<>(EntryType.deferred(location), ingredientType, ingredientHelper, ingredientRenderer));
        wrapper.entryRegistry.add(() -> {
            EntryRegistry.getInstance().addEntries(CollectionUtils.map(allIngredients, JEIPluginDetector::unwrapStack));
        }); 
    }
}
