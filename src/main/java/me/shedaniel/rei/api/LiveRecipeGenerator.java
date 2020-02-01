/*
 * Copyright (c) 2018, 2019, 2020 shedaniel
 * Licensed under the MIT License (the "License").
 */

package me.shedaniel.rei.api;

import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Optional;

public interface LiveRecipeGenerator<T extends RecipeDisplay> {
    
    Identifier getCategoryIdentifier();
    
    default Optional<List<T>> getRecipeFor(EntryStack entry) {
        return Optional.empty();
    }
    
    default Optional<List<T>> getUsageFor(EntryStack entry) {
        return Optional.empty();
    }
    
}
