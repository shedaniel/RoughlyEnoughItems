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
import lombok.experimental.ExtensionMethod;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.math.impl.PointHelper;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.*;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.jeicompat.JEIPluginDetector;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.LazyLoadedValue;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@ExtensionMethod(JEIPluginDetector.class)
public class JEIWrappedCategory<T> implements DisplayCategory<JEIWrappedDisplay<T>> {
    private final IRecipeCategory<T> backingCategory;
    public final LazyLoadedValue<IDrawable> background;
    private final CategoryIdentifier<? extends JEIWrappedDisplay<T>> identifier;
    
    public JEIWrappedCategory(IRecipeCategory<T> backingCategory) {
        this.backingCategory = backingCategory;
        this.background = new LazyLoadedValue<>(backingCategory::getBackground);
        this.identifier = backingCategory.getRecipeType().categoryId().cast();
    }
    
    public Class<? extends T> getRecipeClass() {
        return backingCategory.getRecipeType().getRecipeClass();
    }
    
    public boolean handlesRecipe(T recipe) {
        return backingCategory.isHandled(recipe);
    }
    
    @Override
    public Renderer getIcon() {
        IDrawable icon = backingCategory.getIcon();
        if (icon != null) {
            return icon.unwrapRenderer();
        }
        
        List<EntryIngredient> workstations = CategoryRegistry.getInstance().get(getCategoryIdentifier()).getWorkstations();
        if (!workstations.isEmpty()) {
            return Widgets.createSlot(new Point(0, 0)).entries(workstations.get(0)).disableBackground().disableHighlight().disableTooltips();
        }
        FormattedCharSequence title = getTitle().getVisualOrderText();
        FormattedCharSequence titleTrimmed = sink -> {
            return title.accept((index, style, codepoint) -> {
                if (index == 0 || index == 1) {
                    sink.accept(index, style, codepoint);
                    return true;
                }
                
                return false;
            });
        };
        return new Renderer() {
            @Override
            public void render(PoseStack matrices, Rectangle bounds, int mouseX, int mouseY, float delta) {
                Font font = Minecraft.getInstance().font;
                font.drawShadow(matrices, titleTrimmed, bounds.getCenterX() - font.width(titleTrimmed) / 2.0F, bounds.getCenterY() - 4.5F, 0xFFFFFF);
            }
            
            @Override
            public int getZ() {
                return 0;
            }
            
            @Override
            public void setZ(int z) {
                
            }
        };
    }
    
    @Override
    public Component getTitle() {
        return backingCategory.getTitle();
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
        return setupDisplay(getBackingCategory(), display, JEIWrappedDisplay.getFoci(), bounds, this.background);
    }
    
    public static <T> List<Widget> setupDisplay(IRecipeCategory<T> category, JEIWrappedDisplay<T> display, IFocusGroup focuses, Rectangle bounds, LazyLoadedValue<IDrawable> backgroundLazy) {
        List<Widget> widgets = new ArrayList<>();
        JEIDisplaySetup.Result result;
        try {
            result = JEIDisplaySetup.create(category, display, focuses);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            widgets.add(Widgets.createRecipeBase(bounds).color(0xFFFF0000));
            widgets.add(Widgets.createLabel(new Point(bounds.getCenterX(), bounds.getCenterY() - 8), Component.literal("Failed to initiate JEI integration setRecipe")));
            widgets.add(Widgets.createLabel(new Point(bounds.getCenterX(), bounds.getCenterY() + 1), Component.literal("Check console for error")));
            return widgets;
        }
        widgets.add(Widgets.createRecipeBase(bounds));
        IDrawable background = category.getBackground();
        if (background != null) {
            widgets.add(Widgets.withTranslate(Widgets.wrapRenderer(bounds, background.unwrapRenderer()), 4, 4, 0));
        }
        widgets.add(new WidgetWithBounds() {
            @Override
            public Rectangle getBounds() {
                return bounds;
            }
            
            @Override
            public void render(PoseStack arg, int mouseX, int mouseY, float f) {
                PoseStack stack = new PoseStack();
                stack.pushPose();
                stack.last().pose().load(arg.last().pose());
                stack.translate(bounds.x + 4, bounds.y + 4, 10);
                category.draw(display.getBackingRecipe(), result, stack, mouseX - bounds.x, mouseY - bounds.y);
                stack.popPose();
                
                Point mouse = new Point(mouseX, mouseY);
                
                if (containsMouse(mouse)) {
                    for (Slot slot : Widgets.<Slot>walk(widgets, listener -> listener instanceof Slot)) {
                        if (slot.containsMouse(mouse) && slot.isHighlightEnabled()) {
                            if (slot.getCurrentTooltip(mouse) != null) {
                                return;
                            }
                        }
                    }
                    
                    Tooltip tooltip = getTooltip(TooltipContext.of(mouse));
                    
                    if (tooltip != null) {
                        tooltip.queue();
                    }
                }
            }
            
            @Override
            @Nullable
            public Tooltip getTooltip(Point mouse) {
                List<Component> strings = category.getTooltipStrings(display.getBackingRecipe(), result, mouse.x - bounds.x - 4, mouse.y - bounds.y - 4);
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
                return category.handleInput(display.getBackingRecipe(), d - bounds.x - 4, e - bounds.y - 4, InputConstants.Type.MOUSE.getOrCreate(i)) || super.mouseClicked(d, e, i);
            }
            
            @Override
            public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
                double d = PointHelper.getMouseFloatingX();
                double e = PointHelper.getMouseFloatingY();
                return category.handleInput(display.getBackingRecipe(), d - bounds.x - 4, e - bounds.y - 4, InputConstants.getKey(keyCode, scanCode)) || super.keyPressed(keyCode, scanCode, modifiers);
            }
        });
        JEIDisplaySetup.addTo(widgets, bounds, result);
        if (result.shapelessData.shapeless) {
            Point shapelessPoint = result.shapelessData.pos;
            
            if (shapelessPoint != null) {
                widgets.add(Widgets.createShapelessIcon(new Point(shapelessPoint.x + 9, shapelessPoint.y - 1)));
            } else {
                widgets.add(Widgets.createShapelessIcon(bounds));
            }
        }
        return widgets;
    }
}
