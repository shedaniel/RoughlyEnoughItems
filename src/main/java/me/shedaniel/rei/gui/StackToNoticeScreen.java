/*
 * Copyright (c) 2018, 2019, 2020 shedaniel
 * Licensed under the MIT License (the "License").
 */

package me.shedaniel.rei.gui;

import me.shedaniel.rei.api.EntryStack;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public interface StackToNoticeScreen {
    @ApiStatus.Internal
    void addIngredientStackToNotice(EntryStack stack);
    
    @ApiStatus.Internal
    void addResultStackToNotice(EntryStack stack);
}