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

import com.google.common.collect.Lists;
import lombok.experimental.ExtensionMethod;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.CollectionUtils;
import me.shedaniel.rei.api.common.util.ImmutableTextComponent;
import me.shedaniel.rei.impl.common.InternalLogger;
import me.shedaniel.rei.jeicompat.JEIPluginDetector;
import me.shedaniel.rei.plugin.client.BuiltinClientPlugin;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.vanilla.IVanillaRecipeFactory;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IIngredientVisibility;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@ExtensionMethod(JEIPluginDetector.class)
public class JEIRecipeRegistration implements IRecipeRegistration {
    private final List<Runnable> post;
    
    public JEIRecipeRegistration(List<Runnable> post) {
        this.post = post;
    }
    
    @Override
    @NotNull
    public IJeiHelpers getJeiHelpers() {
        return JEIJeiHelpers.INSTANCE;
    }
    
    @Override
    @NotNull
    public IIngredientManager getIngredientManager() {
        return JEIIngredientManager.INSTANCE;
    }
    
    @Override
    @NotNull
    public IVanillaRecipeFactory getVanillaRecipeFactory() {
        return JEIVanillaRecipeFactory.INSTANCE;
    }
    
    @Override
    public IIngredientVisibility getIngredientVisibility() {
        return JEIIngredientVisibility.INSTANCE;
    }
    
    @Override
    public void addRecipes(@NotNull Collection<?> recipes, @NotNull ResourceLocation categoryId) {
        post.add(() -> {
            addRecipes0(recipes, categoryId);
        });
    }
    
    public static void addRecipes0(@NotNull Collection<?> recipes, @NotNull ResourceLocation categoryId) {
        CategoryIdentifier<Display> categoryIdentifier = CategoryIdentifier.of(categoryId);
        DisplayRegistry registry = DisplayRegistry.getInstance();
        if (recipes instanceof List<?> && recipes.size() >= 100) {
            addRecipesOptimized((List<Object>) recipes, categoryIdentifier, registry);
            return;
        }
        
        for (Object recipe : recipes) {
            Collection<Display> displays = registry.tryFillDisplay(recipe);
            
            if (displays.isEmpty()) {
                InternalLogger.getInstance().warn("No displays found for recipe: %s for category %s", recipe, categoryId);
                return;
            }
            
            boolean registered = false;
            
            for (Display display : displays) {
                if (Objects.equals(display.getCategoryIdentifier(), categoryIdentifier)) {
                    registry.add(display, recipe);
                    registered = true;
                }
            }
            
            if (!registered) {
                InternalLogger.getInstance().warn("No displays matched category for recipe: %s for category %s", recipe, categoryId);
            }
        }
    }
    
    @Override
    public <T> void addRecipes(RecipeType<T> recipeType, List<T> recipes) {
        addRecipes(recipes, recipeType.getUid());
    }
    
    private static void addRecipesOptimized(List<Object> recipes, @NotNull CategoryIdentifier<?> categoryId, DisplayRegistry registry) {
        List<CompletableFuture<List<Supplier<Collection<Display>>>>> completableFutures = Lists.newArrayList();
        Function<Object, Supplier<Collection<Display>>> tryFillDisplay = o -> {
            try {
                Collection<Display> displays = registry.tryFillDisplay(o);
                return () -> displays;
            } catch (RuntimeException e) {
                if (e.getCause() instanceof ConcurrentModificationException) {
                    InternalLogger.getInstance().debug("Failed to parallelize recipe: %s for category %s", o, categoryId);
                    return () -> registry.tryFillDisplay(o);
                } else {
                    throw e;
                }
            }
        };
        CollectionUtils.partition(recipes, 50).forEach(list -> {
            completableFutures.add(CompletableFuture.supplyAsync(() -> {
                return CollectionUtils.map(list, tryFillDisplay);
            }));
        });
        
        try {
            CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[0])).get(120, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            e.printStackTrace();
        }
        
        int i = 0;
        boolean contains = false;
        boolean registered = false;
        
        for (CompletableFuture<List<Supplier<Collection<Display>>>> future : completableFutures) {
            List<Supplier<Collection<Display>>> displayCollection = future.getNow(null);
            
            if (displayCollection != null) {
                int j = 0;
                
                for (Supplier<Collection<Display>> displays : displayCollection) {
                    Object origin = recipes.get(i * 50 + j);
                    
                    for (Display display : displays.get()) {
                        if (Objects.equals(display.getCategoryIdentifier(), categoryId)) {
                            registry.add(display, origin);
                            registered = true;
                        }
                    }
                }
                
                if (displayCollection.size() > 0) {
                    contains = true;
                }
            }
            
            i++;
        }
        
        if (!contains) {
            InternalLogger.getInstance().warn("No displays found for recipes: %s for category %s", recipes.stream().map(Objects::toString)
                    .collect(Collectors.joining(", ")), categoryId);
        } else if (!registered) {
            InternalLogger.getInstance().warn("No displays matched category for recipes: %s for category %s", recipes.stream().map(Objects::toString)
                    .collect(Collectors.joining(", ")), categoryId);
        }
    }
    
    @Override
    public <T> void addIngredientInfo(@NotNull T ingredient, @NotNull IIngredientType<T> ingredientType, @NotNull Component @NotNull ... descriptionComponents) {
        EntryStack<T> stack = ingredient.unwrapStack(ingredientType);
        BuiltinClientPlugin.getInstance().registerInformation(stack, stack.asFormattedText(), components -> {
            Collections.addAll(components, descriptionComponents);
            return components;
        });
    }
    
    @Override
    public <T> void addIngredientInfo(@NotNull List<T> ingredients, @NotNull IIngredientType<T> ingredientType, @NotNull Component @NotNull ... descriptionComponents) {
        EntryIngredient ingredient = ingredientType.unwrapList(ingredients);
        BuiltinClientPlugin.getInstance().registerInformation(ingredient, ImmutableTextComponent.EMPTY, components -> {
            Collections.addAll(components, descriptionComponents);
            return components;
        });
    }
}
