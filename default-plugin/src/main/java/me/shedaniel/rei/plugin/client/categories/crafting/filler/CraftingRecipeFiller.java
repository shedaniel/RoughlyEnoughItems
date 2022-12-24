/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020, 2021, 2022 shedaniel
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

package me.shedaniel.rei.plugin.client.categories.crafting.filler;

import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.DisplayRenderer;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayCategoryView;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.plugin.common.BuiltinPlugin;
import me.shedaniel.rei.plugin.common.displays.crafting.DefaultCraftingDisplay;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

public interface CraftingRecipeFiller<T extends CraftingRecipe> extends Function<T, Collection<Display>> {
    default void registerCategories(CategoryRegistry registry) {
    }
    
    default void registerExtension(CategoryRegistry registry, ExtensionCallback callback) {
        registry.get(BuiltinPlugin.CRAFTING).registerExtension((display, category, lastView) -> {
            if (getRecipeClass().isInstance(display.getOptionalRecipe().orElse(null))) {
                return new DisplayCategoryView<>() {
                    @Override
                    public DisplayRenderer getDisplayRenderer(DefaultCraftingDisplay<?> display) {
                        return lastView.getDisplayRenderer(display);
                    }
                    
                    @Override
                    public List<Widget> setupDisplay(DefaultCraftingDisplay<?> display, Rectangle bounds) {
                        List<Widget> widgets = lastView.setupDisplay(display, bounds);
                        callback.accept(bounds, widgets, display);
                        return widgets;
                    }
                };
            } else {
                return lastView;
            }
        });
    }
    
    default void registerDisplays(DisplayRegistry registry) {
        registry.registerRecipesFiller(getRecipeClass(), RecipeType.CRAFTING, this::apply);
    }
    
    default Widget createInfoWidget(Rectangle rectangle, DefaultCraftingDisplay<?> display, Component... texts) {
        Point point = new Point(rectangle.getMaxX() - 4, rectangle.y + 4);
        Rectangle bounds = new Rectangle(point.getX() - 9, point.getY() + 1, 8, 8);
        if (display.isShapeless()) {
            bounds.x -= 10;
        }
        Widget widget = Widgets.createTexturedWidget(new ResourceLocation("roughlyenoughitems:textures/gui/info.png"), bounds.getX(), bounds.getY(), 0, 0, bounds.getWidth(), bounds.getHeight(), 1, 1, 1, 1);
        return Widgets.withTooltip(Widgets.withBounds(widget, bounds), texts);
    }
    
    Class<T> getRecipeClass();
    
    @FunctionalInterface
    interface ExtensionCallback {
        void accept(Rectangle bounds, List<Widget> widgets, DefaultCraftingDisplay<?> display);
    }
}
