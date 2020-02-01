/*
 * Copyright (c) 2018, 2019, 2020 shedaniel
 * Licensed under the MIT License (the "License").
 */

package me.shedaniel.rei.listeners;

import net.minecraft.client.gui.screen.recipebook.RecipeBookGhostSlots;
import net.minecraft.client.gui.screen.recipebook.RecipeBookWidget;
import net.minecraft.client.gui.screen.recipebook.RecipeGroupButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import org.jetbrains.annotations.ApiStatus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@ApiStatus.Internal
@Mixin(RecipeBookWidget.class)
public interface RecipeBookGuiHooks {
    
    @Accessor("ghostSlots")
    RecipeBookGhostSlots rei_getGhostSlots();
    
    @Accessor("searchField")
    TextFieldWidget rei_getSearchField();
    
    @Accessor("tabButtons")
    List<RecipeGroupButtonWidget> rei_getTabButtons();
    
}
