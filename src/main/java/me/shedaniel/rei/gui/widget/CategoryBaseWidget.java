/*
 * Copyright (c) 2018, 2019, 2020 shedaniel
 * Licensed under the MIT License (the "License").
 */

package me.shedaniel.rei.gui.widget;

import me.shedaniel.math.api.Rectangle;

public class CategoryBaseWidget extends RecipeBaseWidget {
    
    public CategoryBaseWidget(Rectangle bounds) {
        super(bounds);
    }
    
    @Override
    protected int getYTextureOffset() {
        return 66;
    }
    
    @Override
    protected boolean isRendering() {
        return true;
    }
    
}
