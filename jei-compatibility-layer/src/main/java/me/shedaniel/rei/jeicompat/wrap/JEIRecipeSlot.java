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

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import lombok.experimental.ExtensionMethod;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.widgets.Slot;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.type.EntryType;
import me.shedaniel.rei.jeicompat.JEIPluginDetector;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotTooltipCallback;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.client.gui.GuiComponent;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

@ExtensionMethod(JEIPluginDetector.class)
public class JEIRecipeSlot implements IRecipeSlotBuilder, IRecipeSlotView {
    public RecipeIngredientRole role;
    @Nullable
    public final Slot slot;
    private boolean visible;
    @Nullable
    public String name;
    public Integer capacityMb;
    public IRecipeSlotTooltipCallback tooltipCallback = (recipeSlotView, tooltip) -> {};
    public final Map<EntryType<?>, IIngredientRenderer<?>> renderers = new HashMap<>();
    @Nullable
    public Widget background;
    @Nullable
    public Widget overlay;
    public final int index;
    
    public JEIRecipeSlot(int index, RecipeIngredientRole role, @Nullable Point pos) {
        this.index = index;
        this.role = role;
        this.slot = Widgets.createSlot(new Point(pos == null ? 0 : pos.x, pos == null ? 0 : pos.y)).disableBackground();
        this.visible = pos != null;
    }
    
    public int getIndex() {
        return index;
    }
    
    public boolean isVisible() {
        return visible;
    }
    
    @Override
    public <I> IRecipeSlotBuilder addIngredients(IIngredientType<I> ingredientType, List<@Nullable I> ingredients) {
        for (I ingredient : ingredients) {
            this.slot.entry(ingredient.unwrapStack(ingredientType));
        }
        return this;
    }
    
    @Override
    public <I> IRecipeSlotBuilder addIngredient(IIngredientType<I> ingredientType, I ingredient) {
        this.slot.entry(ingredient.unwrapStack(ingredientType));
        return this;
    }
    
    @Override
    public IRecipeSlotBuilder addIngredientsUnsafe(List<?> ingredients) {
        for (Object ingredient : ingredients) {
            addIngredient(ingredient.unwrapDefinition().jeiType().cast(), ingredient);
        }
        return this;
    }
    
    @Override
    public IRecipeSlotBuilder addTooltipCallback(IRecipeSlotTooltipCallback tooltipCallback) {
        IRecipeSlotTooltipCallback old = this.tooltipCallback;
        this.tooltipCallback = (recipeSlotView, tooltip) -> {
            old.onTooltip(recipeSlotView, tooltip);
            tooltipCallback.onTooltip(recipeSlotView, tooltip);
        };
        return this;
    }
    
    @Override
    public IRecipeSlotBuilder setSlotName(String slotName) {
        this.name = slotName;
        return this;
    }
    
    @Override
    public IRecipeSlotBuilder setBackground(IDrawable background, int xOffset, int yOffset) {
        this.background = Widgets.withTranslate(Widgets.wrapRenderer(() -> slot.getInnerBounds().clone(), background.unwrapRenderer()), xOffset, yOffset, 0);
        return this;
    }
    
    @Override
    public IRecipeSlotBuilder setOverlay(IDrawable overlay, int xOffset, int yOffset) {
        this.overlay = Widgets.withTranslate(Widgets.wrapRenderer(() -> slot.getInnerBounds().clone(), overlay.unwrapRenderer()), xOffset, yOffset, 0);
        return this;
    }
    
    @Override
    public IRecipeSlotBuilder setFluidRenderer(int capacityMb, boolean showCapacity, int width, int height) {
        slot.getBounds().setSize(width + 2, height + 2);
        this.capacityMb = capacityMb;
        return this;
    }
    
    @Override
    public <T> IRecipeSlotBuilder setCustomRenderer(IIngredientType<T> ingredientType, IIngredientRenderer<T> ingredientRenderer) {
        renderers.put(ingredientType.unwrapType(), ingredientRenderer);
        return this;
    }
    
    @Override
    public <T> Stream<T> getIngredients(IIngredientType<T> ingredientType) {
        EntryType<T> type = ingredientType.unwrapType();
        return (Stream<T>) slot.getEntries().stream()
                .filter(stack -> stack.getType() == type)
                .map(JEIPluginDetector::jeiValue);
    }
    
    @Override
    public Stream<ITypedIngredient<?>> getAllIngredients() {
        return slot.getEntries().stream()
                .map(JEIPluginDetector::typedJeiValue);
    }
    
    @Override
    public boolean isEmpty() {
        return slot.getEntries().isEmpty();
    }
    
    @Override
    public <T> Optional<T> getDisplayedIngredient(IIngredientType<T> ingredientType) {
        EntryStack<?> entry = slot.getCurrentEntry();
        ITypedIngredient<?> value = entry.typedJeiValue();
        return value.getIngredient(ingredientType);
    }
    
    @Override
    public Optional<ITypedIngredient<?>> getDisplayedIngredient() {
        EntryStack<?> entry = slot.getCurrentEntry();
        return entry.typedJeiValueOpWild();
    }
    
    @Override
    public Optional<String> getSlotName() {
        return Optional.ofNullable(name);
    }
    
    @Override
    public RecipeIngredientRole getRole() {
        return role;
    }
    
    @Override
    public void drawHighlight(PoseStack stack, int color) {
        RenderSystem.disableDepthTest();
        RenderSystem.colorMask(true, true, true, false);
        Rectangle bounds = slot.getInnerBounds().clone();
        try {
            m_93179_.invoke(slot, stack, bounds.x, bounds.y, bounds.getMaxX(), bounds.getMaxY(), color, color);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        RenderSystem.colorMask(true, true, true, true);
        RenderSystem.enableDepthTest();
    }
    
    private static final Method m_93179_ = ObfuscationReflectionHelper.findMethod(GuiComponent.class, "m_93179_",
            PoseStack.class, Integer.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE);
}
