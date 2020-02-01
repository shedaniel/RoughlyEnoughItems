/*
 * Copyright (c) 2018, 2019, 2020 shedaniel
 * Licensed under the MIT License (the "License").
 */

package me.shedaniel.rei.api;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public interface ItemStackHook {
    void rei_setRenderEnchantmentGlint(boolean b);
}
