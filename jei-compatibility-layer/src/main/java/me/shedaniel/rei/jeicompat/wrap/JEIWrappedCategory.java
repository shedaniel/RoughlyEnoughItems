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

package me.shedaniel.rei.jeicompat.wrap;

import com.mojang.blaze3d.vertex.PoseStack;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.math.impl.PointHelper;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.util.ImmutableTextComponent;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;
import net.minecraft.util.LazyLoadedValue;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static me.shedaniel.rei.jeicompat.JEIPluginDetector.wrapDrawable;

public class JEIWrappedCategory<T> implements DisplayCategory<JEIWrappedDisplay<T>> {
    private final IRecipeCategory<T> backingCategory;
    private final LazyLoadedValue<IDrawable> background;
    private final CategoryIdentifier<? extends JEIWrappedDisplay<T>> identifier;
    
    public JEIWrappedCategory(IRecipeCategory<T> backingCategory) {
        this.backingCategory = backingCategory;
        this.background = new LazyLoadedValue<>(backingCategory::getBackground);
        this.identifier = CategoryIdentifier.of(backingCategory.getUid());
    }
    
    public Class<? extends T> getRecipeClass() {
        return backingCategory.getRecipeClass();
    }
    
    public boolean handlesRecipe(T recipe) {
        return backingCategory.isHandled(recipe);
    }
    
    @Override
    public Renderer getIcon() {
        return wrapDrawable(backingCategory.getIcon());
    }
    
    @Override
    public Component getTitle() {
        return new ImmutableTextComponent(backingCategory.getTitle());
    }
    
    @Override
    public int getDisplayWidth(JEIWrappedDisplay<T> display) {
        return this.background.get().getWidth() + 8;
    }
    
    @Override
    public CategoryIdentifier<? extends JEIWrappedDisplay<T>> getCategoryIdentifier() {
        return identifier;
    }
    
    @Override
    public int getDisplayHeight() {
        return this.background.get().getHeight() + 8;
    }
    
    public IRecipeCategory<T> getBackingCategory() {
        return backingCategory;
    }
    
    @Override
    public List<Widget> setupDisplay(JEIWrappedDisplay<T> display, Rectangle bounds) {
        List<Widget> widgets = new ArrayList<>();
        widgets.add(Widgets.createRecipeBase(bounds));
        widgets.add(Widgets.createDrawableWidget((helper, matrices, mouseX, mouseY, delta) -> {
            this.background.get().draw(matrices, bounds.x + 4, bounds.y + 4);
        }));
        widgets.add(new Widget() {
            @Override
            public void render(PoseStack arg, int i, int j, float f) {
                arg.pushPose();
                arg.translate(bounds.x + 4, bounds.y + 4, getZ());
                backingCategory.draw(display.getBackingRecipe(), arg, i, j);
                arg.popPose();
                
                Point mouse = PointHelper.ofMouse();
                if (containsMouse(mouse)) {
                    Tooltip tooltip = getTooltip(mouse);
                    
                    if (tooltip != null) {
                        tooltip.queue();
                    }
                }
            }
            
            @Override
            @Nullable
            public Tooltip getTooltip(Point mouse) {
                List<Component> strings = backingCategory.getTooltipStrings(display.getBackingRecipe(), mouse.x - bounds.x, mouse.y - bounds.y);
                if (strings.isEmpty()) {
                    return null;
                }
                return Tooltip.create(mouse, strings);
            }
            
            @Override
            public List<? extends GuiEventListener> children() {
                return Collections.emptyList();
            }
            
            @Override
            public boolean mouseClicked(double d, double e, int i) {
                return backingCategory.handleClick(display.getBackingRecipe(), d - bounds.x, e - bounds.y, i) || super.mouseClicked(d, e, i);
            }
        });
        JEIRecipeLayout<T> layout = new JEIRecipeLayout<>(this, display);
        backingCategory.setRecipe(layout, display.getBackingRecipe(), display.getIngredients());
        layout.addTo(widgets, bounds);
        return widgets;
    }
}
