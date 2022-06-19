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

package me.shedaniel.rei.jeicompat.wrap;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.math.impl.PointHelper;
import me.shedaniel.rei.api.client.gui.DisplayRenderer;
import me.shedaniel.rei.api.client.gui.widgets.*;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.client.registry.display.DisplayCategoryView;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.jeicompat.JEIPluginDetector;
import me.shedaniel.rei.jeicompat.unwrap.JEIUnwrappedCategory;
import mezz.jei.api.gui.ingredient.ICraftingGridHelper;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.category.extensions.IExtendableRecipeCategory;
import mezz.jei.api.recipe.category.extensions.IRecipeCategoryExtension;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICraftingCategoryExtension;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class JEIExtendableRecipeCategory<T, D extends Display, W extends IRecipeCategoryExtension> extends JEIUnwrappedCategory<T, D> implements IExtendableRecipeCategory<T, W> {
    private final JEIPluginDetector.JEIPluginWrapper wrapper;
    
    public JEIExtendableRecipeCategory(JEIPluginDetector.JEIPluginWrapper wrapper, DisplayCategory<D> backingCategory) {
        super(backingCategory);
        this.wrapper = wrapper;
    }
    
    @Override
    public <R extends T> void addCategoryExtension(Class<? extends R> recipeClass, Function<R, ? extends W> extensionFactory) {
        addCategoryExtension(recipeClass, $ -> true, extensionFactory);
    }
    
    @Override
    public <R extends T> void addCategoryExtension(Class<? extends R> recipeClass, Predicate<R> extensionFilter, Function<R, ? extends W> extensionFactory) {
//        List<Triple<Class<?>, Predicate<Object>, Function<Object, IRecipeCategoryExtension>>> triples = this.wrapper.categories.computeIfAbsent(getBackingCategory(), $ -> new ArrayList<>());
//        triples.add(Triple.of(recipeClass, (Predicate) extensionFilter, (Function) extensionFactory));
        CategoryRegistry.getInstance().configure(getBackingCategory().getCategoryIdentifier().<D>cast(), config -> {
            config.registerExtension((display, category, lastView) -> {
                Object origin = DisplayRegistry.getInstance().getDisplayOrigin(display);
                if (recipeClass.isInstance(origin) && extensionFilter.test((R) origin)) {
                    return new ExtendedCategoryView<>((R) origin, lastView, category, extensionFactory.apply((R) origin));
                }
                
                return lastView;
            });
        });
    }
    
    private class ExtendedCategoryView<R> implements DisplayCategoryView<D> {
        private final R origin;
        private final DisplayCategoryView<D> lastView;
        private final DisplayCategory<D> category;
        private final W extension;
        
        public ExtendedCategoryView(R origin, DisplayCategoryView<D> lastView, DisplayCategory<D> category, W extension) {
            this.origin = origin;
            this.lastView = lastView;
            this.category = category;
            this.extension = extension;
        }
        
        @Override
        public DisplayRenderer getDisplayRenderer(D display) {
            return this.lastView.getDisplayRenderer(display);
        }
        
        @Override
        public List<Widget> setupDisplay(D display, Rectangle bounds) {
            List<Widget> widgets = new ArrayList<>();
            
            if (category instanceof JEIWrappedCategory) {
                JEIDisplaySetup.Result result;
                try {
                    result = createForExtension(extension, (JEIWrappedDisplay<R>) display);
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                    widgets.add(Widgets.createRecipeBase(bounds).color(0xFFFF0000));
                    widgets.add(Widgets.createLabel(new Point(bounds.getCenterX(), bounds.getCenterY() - 8), new TextComponent("Failed to initiate JEI integration setRecipe")));
                    widgets.add(Widgets.createLabel(new Point(bounds.getCenterX(), bounds.getCenterY() + 1), new TextComponent("Check console for error")));
                    return widgets;
                }
                
                widgets.addAll(JEIWrappedCategory.setupDisplay(result, ((JEIWrappedCategory<R>) category).getBackingCategory(), (JEIWrappedDisplay<R>) display, bounds, ((JEIWrappedCategory<?>) category).background));
            } else if (extension instanceof ICraftingCategoryExtension) {
                JEIDisplaySetup.Result result;
                try {
                    result = createCraftingForExtension((ICraftingCategoryExtension) extension);
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                    widgets.add(Widgets.createRecipeBase(bounds).color(0xFFFF0000));
                    widgets.add(Widgets.createLabel(new Point(bounds.getCenterX(), bounds.getCenterY() - 8), new TextComponent("Failed to initiate JEI integration setRecipe")));
                    widgets.add(Widgets.createLabel(new Point(bounds.getCenterX(), bounds.getCenterY() + 1), new TextComponent("Check console for error")));
                    return widgets;
                }
                
                List<Widget> setupDisplay = this.lastView.setupDisplay(display, bounds);
                widgets.addAll(setupDisplay.stream()
                        .filter(widget -> !(widget instanceof Slot))
                        .toList());
                for (Widget widget : setupDisplay) {
                    if (widget instanceof Slot slot && slot.isBackgroundEnabled()) {
                        widgets.add(Widgets.createSlotBase(slot.getBounds()));
                    }
                }
                JEIDisplaySetup.addTo(widgets, bounds, result);
            } else {
                widgets.addAll(this.lastView.setupDisplay(display, bounds));
            }
            
            widgets.add(new WidgetWithBounds() {
                @Override
                public Rectangle getBounds() {
                    return bounds;
                }
                
                @Override
                public void render(PoseStack arg, int i, int j, float f) {
                    arg.pushPose();
                    arg.translate(bounds.x + 4, bounds.y + 4, getZ());
                    extension.drawInfo(bounds.width, bounds.height, arg, i - bounds.x, j - bounds.y);
                    arg.popPose();
                    
                    Point mouse = PointHelper.ofMouse();
                    if (containsMouse(mouse)) {
                        Tooltip tooltip = getTooltip(TooltipContext.of(mouse));
                        
                        if (tooltip != null) {
                            tooltip.queue();
                        }
                    }
                }
                
                @Override
                @Nullable
                public Tooltip getTooltip(Point mouse) {
                    List<Component> strings = extension.getTooltipStrings(mouse.x - bounds.x, mouse.y - bounds.y);
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
                    return extension.handleInput(d - bounds.x, e - bounds.y, InputConstants.Type.MOUSE.getOrCreate(i)) || super.mouseClicked(d, e, i);
                }
                
                @Override
                public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
                    double d = PointHelper.getMouseFloatingX();
                    double e = PointHelper.getMouseFloatingY();
                    return extension.handleInput(d - bounds.x - 4, e - bounds.y - 4, InputConstants.getKey(keyCode, scanCode)) || super.keyPressed(keyCode, scanCode, modifiers);
                }
            });
            
            return widgets;
        }
        
        public static <T> JEIDisplaySetup.Result createForExtension(IRecipeCategoryExtension category, JEIWrappedDisplay<T> display) {
            JEIDisplaySetup.Result result = new JEIDisplaySetup.Result();
            JEIRecipeLayoutBuilder builder = new JEIRecipeLayoutBuilder(result.shapelessData);
            // Legacy code
            JEIRecipeLayout<T> layout = new JEIRecipeLayout<>(builder);
            IIngredients ingredients = display.getLegacyIngredients();
            if (ingredients != null) {
                category.setIngredients(ingredients);
                JEIDisplaySetup.applyLegacyTooltip(result, layout);
            }
            result.setSlots(builder.slots);
            return result;
        }
        
        public static <T> JEIDisplaySetup.Result createCraftingForExtension(ICraftingCategoryExtension category) {
            JEIDisplaySetup.Result result = new JEIDisplaySetup.Result();
            JEIRecipeLayoutBuilder builder = new JEIRecipeLayoutBuilder(result.shapelessData);
            category.setRecipe(builder, JEICraftingGridHelper.INSTANCE, JEIWrappedDisplay.getFoci());
            if (builder.isDirty()) {
                result.setSlots(builder.slots);
                return result;
            }
            return result;
        }
    }
}
