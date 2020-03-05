/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020 shedaniel
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

package me.shedaniel.rei.plugin.cooking;

import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.ints.IntList;
import me.shedaniel.math.api.Point;
import me.shedaniel.math.api.Rectangle;
import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.api.REIHelper;
import me.shedaniel.rei.api.TransferRecipeCategory;
import me.shedaniel.rei.gui.entries.RecipeEntry;
import me.shedaniel.rei.gui.entries.SimpleRecipeEntry;
import me.shedaniel.rei.gui.widget.EntryWidget;
import me.shedaniel.rei.gui.widget.RecipeArrowWidget;
import me.shedaniel.rei.gui.widget.RecipeBaseWidget;
import me.shedaniel.rei.gui.widget.Widget;
import me.shedaniel.rei.plugin.DefaultPlugin;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import java.text.DecimalFormat;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

public class DefaultCookingCategory implements TransferRecipeCategory<DefaultCookingDisplay> {
    private Identifier identifier;
    private EntryStack logo;
    private String categoryName;
    
    public DefaultCookingCategory(Identifier identifier, EntryStack logo, String categoryName) {
        this.identifier = identifier;
        this.logo = logo;
        this.categoryName = categoryName;
    }
    
    @Override
    public void renderRedSlots(List<Widget> widgets, Rectangle bounds, DefaultCookingDisplay display, IntList redSlots) {
        Point startPoint = new Point(bounds.getCenterX() - 41, bounds.getCenterY() - 27);
        RenderSystem.translatef(0, 0, 400);
        if (redSlots.contains(0)) {
            DrawableHelper.fill(startPoint.x + 1, startPoint.y + 1, startPoint.x + 1 + 16, startPoint.y + 1 + 16, 1090453504);
        }
        RenderSystem.translatef(0, 0, -400);
    }
    
    @Override
    public List<Widget> setupDisplay(Supplier<DefaultCookingDisplay> recipeDisplaySupplier, Rectangle bounds) {
        Point startPoint = new Point(bounds.getCenterX() - 41, bounds.y + 10);
        double cookingTime = recipeDisplaySupplier.get().getCookingTime();
        DecimalFormat df = new DecimalFormat("###.##");
        List<Widget> widgets = new LinkedList<>(Collections.singletonList(new RecipeBaseWidget(bounds) {
            @Override
            public void render(int mouseX, int mouseY, float delta) {
                super.render(mouseX, mouseY, delta);
                MinecraftClient.getInstance().getTextureManager().bindTexture(DefaultPlugin.getDisplayTexture());
                blit(startPoint.x, startPoint.y, 0, 177, 82, 34);
                int height = 14 - MathHelper.ceil((System.currentTimeMillis() / 250d % 14d) / 1f);
                blit(startPoint.x + 2, startPoint.y + 31 + (3 - height), 82, 77 + (14 - height), 14, height);
                String text = I18n.translate("category.rei.cooking.time&xp", df.format(recipeDisplaySupplier.get().getXp()), df.format(cookingTime / 20d));
                int length = MinecraftClient.getInstance().textRenderer.getStringWidth(text);
                MinecraftClient.getInstance().textRenderer.draw(text, bounds.x + bounds.width - length - 5, bounds.y + 5, REIHelper.getInstance().isDarkThemeEnabled() ? 0xFFBBBBBB : 0xFF404040);
                
            }
        }));
        widgets.add(RecipeArrowWidget.create(new Point(startPoint.x + 24, startPoint.y + 8), true).time(cookingTime * 50));
        widgets.add(EntryWidget.create(startPoint.x + 1, startPoint.y + 1).entries(recipeDisplaySupplier.get().getInputEntries().get(0)).markIsInput());
        widgets.add(EntryWidget.create(startPoint.x + 61, startPoint.y + 9).entries(recipeDisplaySupplier.get().getOutputEntries()).noBackground().markIsOutput());
        return widgets;
    }
    
    @Override
    public RecipeEntry getSimpleRenderer(DefaultCookingDisplay recipe) {
        return SimpleRecipeEntry.create(Collections.singletonList(recipe.getInputEntries().get(0)), recipe.getOutputEntries());
    }
    
    @Override
    public int getDisplayHeight() {
        return 49;
    }
    
    @Override
    public Identifier getIdentifier() {
        return identifier;
    }
    
    @Override
    public EntryStack getLogo() {
        return logo;
    }
    
    @Override
    public String getCategoryName() {
        return I18n.translate(categoryName);
    }
}
