/*
 * Copyright (c) 2018, 2019, 2020 shedaniel
 * Licensed under the MIT License (the "License").
 */

package me.shedaniel.rei.plugin.stonecutting;

import me.shedaniel.math.api.Point;
import me.shedaniel.math.api.Rectangle;
import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.api.RecipeCategory;
import me.shedaniel.rei.gui.widget.EntryWidget;
import me.shedaniel.rei.gui.widget.RecipeBaseWidget;
import me.shedaniel.rei.gui.widget.Widget;
import me.shedaniel.rei.plugin.DefaultPlugin;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.util.Identifier;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

public class DefaultStoneCuttingCategory implements RecipeCategory<DefaultStoneCuttingDisplay> {
    
    @Override
    public Identifier getIdentifier() {
        return DefaultPlugin.STONE_CUTTING;
    }
    
    @Override
    public EntryStack getLogo() {
        return EntryStack.create(Blocks.STONECUTTER);
    }
    
    @Override
    public String getCategoryName() {
        return I18n.translate("category.rei.stone_cutting");
    }
    
    @Override
    public List<Widget> setupDisplay(Supplier<DefaultStoneCuttingDisplay> recipeDisplaySupplier, Rectangle bounds) {
        Point startPoint = new Point(bounds.getCenterX() - 41, bounds.getCenterY() - 13);
        List<Widget> widgets = new LinkedList<>(Collections.singletonList(new RecipeBaseWidget(bounds) {
            @Override
            public void render(int mouseX, int mouseY, float delta) {
                super.render(mouseX, mouseY, delta);
                MinecraftClient.getInstance().getTextureManager().bindTexture(DefaultPlugin.getDisplayTexture());
                blit(startPoint.x, startPoint.y, 0, 221, 82, 26);
            }
        }));
        widgets.add(EntryWidget.create(startPoint.x + 4, startPoint.y + 5).entries(recipeDisplaySupplier.get().getInputEntries().get(0)).noBackground());
        widgets.add(EntryWidget.create(startPoint.x + 61, startPoint.y + 5).entries(recipeDisplaySupplier.get().getOutputEntries()).noBackground());
        return widgets;
    }
    
    @Override
    public int getDisplayHeight() {
        return 36;
    }
    
}
