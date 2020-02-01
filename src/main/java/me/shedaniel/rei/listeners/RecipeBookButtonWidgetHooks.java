/*
 * Copyright (c) 2018, 2019, 2020 shedaniel
 * Licensed under the MIT License (the "License").
 */

package me.shedaniel.rei.listeners;

import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@ApiStatus.Internal
@Mixin(TexturedButtonWidget.class)
public interface RecipeBookButtonWidgetHooks {
    @Accessor("texture")
    Identifier rei_getTexture();
}
