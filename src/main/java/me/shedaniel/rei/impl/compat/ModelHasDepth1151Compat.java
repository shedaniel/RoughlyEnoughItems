/*
 * Copyright (c) 2018, 2019, 2020 shedaniel
 * Licensed under the MIT License (the "License").
 */

package me.shedaniel.rei.impl.compat;

import net.minecraft.client.render.model.BakedModel;

import java.util.function.Predicate;

public class ModelHasDepth1151Compat implements Predicate<BakedModel> {
    @Override
    public boolean test(BakedModel bakedModel) {
        return bakedModel.hasDepth();
    }
}
