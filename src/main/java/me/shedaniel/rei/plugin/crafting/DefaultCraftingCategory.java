/*
 * Copyright (c) 2018, 2019, 2020 shedaniel
 * Licensed under the MIT License (the "License").
 */

package me.shedaniel.rei.plugin.crafting;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.ints.IntList;
import me.shedaniel.math.api.Point;
import me.shedaniel.math.api.Rectangle;
import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.api.TransferRecipeCategory;
import me.shedaniel.rei.gui.widget.EntryWidget;
import me.shedaniel.rei.gui.widget.RecipeBaseWidget;
import me.shedaniel.rei.gui.widget.Widget;
import me.shedaniel.rei.impl.ScreenHelper;
import me.shedaniel.rei.plugin.DefaultPlugin;
import me.shedaniel.rei.server.ContainerInfo;
import me.shedaniel.rei.server.ContainerInfoHandler;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.container.Container;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public class DefaultCraftingCategory implements TransferRecipeCategory<DefaultCraftingDisplay> {
    
    public static int getSlotWithSize(DefaultCraftingDisplay recipeDisplay, int num, int craftingGridWidth) {
        int x = num % recipeDisplay.getWidth();
        int y = (num - x) / recipeDisplay.getWidth();
        return craftingGridWidth * y + x;
    }
    
    @Override
    public Identifier getIdentifier() {
        return DefaultPlugin.CRAFTING;
    }
    
    @Override
    public EntryStack getLogo() {
        return EntryStack.create(Blocks.CRAFTING_TABLE);
    }
    
    @Override
    public String getCategoryName() {
        return I18n.translate("category.rei.crafting");
    }
    
    @Override
    public List<Widget> setupDisplay(Supplier<DefaultCraftingDisplay> recipeDisplaySupplier, Rectangle bounds) {
        Point startPoint = new Point(bounds.getCenterX() - 58, bounds.getCenterY() - 27);
        List<Widget> widgets = Lists.newLinkedList(Collections.singletonList(new RecipeBaseWidget(bounds) {
            @Override
            public void render(int mouseX, int mouseY, float delta) {
                super.render(mouseX, mouseY, delta);
                MinecraftClient.getInstance().getTextureManager().bindTexture(DefaultPlugin.getDisplayTexture());
                blit(startPoint.x, startPoint.y, 0, 0, 116, 54);
            }
        }));
        List<List<EntryStack>> input = recipeDisplaySupplier.get().getInputEntries();
        List<EntryWidget> slots = Lists.newArrayList();
        for (int y = 0; y < 3; y++)
            for (int x = 0; x < 3; x++)
                slots.add(EntryWidget.create(startPoint.x + 1 + x * 18, startPoint.y + 1 + y * 18));
        for (int i = 0; i < input.size(); i++) {
            if (recipeDisplaySupplier.get() instanceof DefaultShapedDisplay) {
                if (!input.get(i).isEmpty())
                    slots.get(getSlotWithSize(recipeDisplaySupplier.get(), i, 3)).entries(input.get(i));
            } else if (!input.get(i).isEmpty())
                slots.get(i).entries(input.get(i));
        }
        widgets.addAll(slots);
        widgets.add(EntryWidget.create(startPoint.x + 95, startPoint.y + 19).entries(recipeDisplaySupplier.get().getOutputEntries()).noBackground());
        return widgets;
    }
    
    @Override
    public void renderRedSlots(List<Widget> widgets, Rectangle bounds, DefaultCraftingDisplay display, IntList redSlots) {
        ContainerInfo<Container> info = (ContainerInfo<Container>) ContainerInfoHandler.getContainerInfo(getIdentifier(), ScreenHelper.getLastContainerScreen().getContainer().getClass());
        if (info == null)
            return;
        RenderSystem.translatef(0, 0, 400);
        Point startPoint = new Point(bounds.getCenterX() - 58, bounds.getCenterY() - 27);
        int width = info.getCraftingWidth(ScreenHelper.getLastContainerScreen().getContainer());
        for (Integer slot : redSlots) {
            int i = slot;
            int x = i % width;
            int y = MathHelper.floor(i / (float) width);
            DrawableHelper.fill(startPoint.x + 1 + x * 18, startPoint.y + 1 + y * 18, startPoint.x + 1 + x * 18 + 16, startPoint.y + 1 + y * 18 + 16, 0x60ff0000);
        }
        RenderSystem.translatef(0, 0, -400);
    }
}
