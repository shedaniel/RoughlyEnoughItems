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

package me.shedaniel.rei.jeicompat.ingredient;

import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import lombok.experimental.ExtensionMethod;
import me.shedaniel.rei.api.client.gui.widgets.Slot;
import me.shedaniel.rei.jeicompat.JEIPluginDetector;
import me.shedaniel.rei.jeicompat.wrap.JEIRecipeLayoutBuilder;
import me.shedaniel.rei.jeicompat.wrap.JEIRecipeSlot;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IGuiIngredient;
import mezz.jei.api.gui.ingredient.IGuiIngredientGroup;
import mezz.jei.api.gui.ingredient.ITooltipCallback;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.RecipeIngredientRole;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static me.shedaniel.rei.jeicompat.JEIPluginDetector.WILL_NOT_BE_IMPLEMENTED;

@ExtensionMethod(JEIPluginDetector.class)
public class JEIGuiIngredientGroup<T> implements IGuiIngredientGroup<T> {
    private final IIngredientType<T> type;
    private final JEIRecipeLayoutBuilder builder;
    private final Int2ObjectMap<SlotWrapper> slots = new Int2ObjectOpenHashMap<>();
    public final List<ITooltipCallback<T>> tooltipCallbacks = new ArrayList<>();
    
    public JEIGuiIngredientGroup(IIngredientType<T> type, JEIRecipeLayoutBuilder builder) {
        this.type = type;
        this.builder = builder;
    }
    
    public IIngredientType<T> getType() {
        return type;
    }
    
    protected SlotWrapper getSlot(int slotIndex) {
        return slots.computeIfAbsent(slotIndex, i -> new SlotWrapper(i, (JEIRecipeSlot) builder.addSlot(RecipeIngredientRole.RENDER_ONLY, 0, 0, i)));
    }
    
    public Int2ObjectMap<SlotWrapper> getSlots() {
        return slots;
    }
    
    @Override
    public void set(@NotNull IIngredients ingredients) {
        List<List<T>> inputs = ingredients.getInputs(type);
        List<List<T>> outputs = ingredients.getOutputs(type);
        int inputIndex = 0, outputIndex = 0;
        int[] array = slots.keySet().toArray(new int[0]);
        Arrays.parallelSort(array);
        for (int slot : array) {
            SlotWrapper slotWrapper = slots.get(slot);
            if (slotWrapper.slot.role == RecipeIngredientRole.INPUT) {
                if (inputIndex < inputs.size()) {
                    slotWrapper.slot.slot.clearEntries();
                    slotWrapper.slot.slot.entries(type.unwrapList(inputs.get(inputIndex++)));
                }
            } else if (slotWrapper.slot.role == RecipeIngredientRole.OUTPUT) {
                if (outputIndex < outputs.size()) {
                    slotWrapper.slot.slot.clearEntries();
                    slotWrapper.slot.slot.entries(type.unwrapList(outputs.get(outputIndex++)));
                }
            }
        }
    }
    
    @Override
    public void set(int slotIndex, @Nullable List<T> ingredients) {
        Slot slot = getSlot(slotIndex).slot.slot;
        slot.clearEntries();
        slot.entries(type.unwrapList(ingredients));
    }
    
    @Override
    public void set(int slotIndex, @Nullable T ingredient) {
        Slot slot = getSlot(slotIndex).slot.slot;
        slot.clearEntries();
        slot.entry(ingredient.unwrapStack(type));
    }
    
    @Override
    public void setBackground(int slotIndex, @NotNull IDrawable background) {
        SlotWrapper slot = getSlot(slotIndex);
        slot.slot.setBackground(background, 0, 0);
    }
    
    @Override
    public void addTooltipCallback(@NotNull ITooltipCallback<T> tooltipCallback) {
        this.tooltipCallbacks.add(Objects.requireNonNull(tooltipCallback, "tooltipCallback"));
    }
    
    @Override
    @NotNull
    public Int2ObjectMap<SlotWrapper> getGuiIngredients() {
        return slots;
    }
    
    @Override
    public void init(int slotIndex, boolean input, int xPosition, int yPosition) {
        SlotWrapper slot = getSlot(slotIndex);
        slot.slot.role = input ? RecipeIngredientRole.INPUT : RecipeIngredientRole.OUTPUT;
        slot.slot.slot.getBounds().setLocation(xPosition - 1, yPosition - 1);
    }
    
    @Override
    public void init(int slotIndex, boolean input, @NotNull IIngredientRenderer<T> ingredientRenderer, int xPosition, int yPosition, int width, int height, int xPadding, int yPadding) {
        SlotWrapper slot = getSlot(slotIndex);
        slot.slot.role = input ? RecipeIngredientRole.INPUT : RecipeIngredientRole.OUTPUT;
        slot.slot.slot.getBounds().setLocation(xPosition - 1, yPosition - 1);
        slot.slot.slot.getBounds().setSize(width + 2, height + 2);
        slot.slot.setCustomRenderer(type, ingredientRenderer);
    }
    
    @Override
    public void setOverrideDisplayFocus(@Nullable IFocus<T> focus) {
        throw WILL_NOT_BE_IMPLEMENTED();
    }
    
    public class SlotWrapper extends AbstractList<T> implements IGuiIngredient<T> {
        public JEIRecipeSlot slot;
        public final int index;
        
        public SlotWrapper(int index, JEIRecipeSlot slot) {
            this.index = index;
            this.slot = slot;
        }
        
        @Override
        @NotNull
        public IIngredientType<T> getIngredientType() {
            return type;
        }
        
        @Override
        @Nullable
        public T getDisplayedIngredient() {
            return slot.getDisplayedIngredient(type).orElse(null);
        }
        
        @Override
        @NotNull
        public List<T> getAllIngredients() {
            return this;
        }
        
        @Override
        public boolean isInput() {
            return slot.getRole() == RecipeIngredientRole.INPUT;
        }
        
        @Override
        public void drawHighlight(@NotNull PoseStack matrixStack, int color, int xOffset, int yOffset) {
            matrixStack.pushPose();
            matrixStack.translate(xOffset, yOffset, 0);
            slot.drawHighlight(matrixStack, color);
            matrixStack.popPose();
        }
        
        @Override
        public T get(int index) {
            return slot.slot.getEntries().get(index).<T>cast().jeiValueOrNull();
        }
        
        @Override
        public int size() {
            return slot.slot.getEntries().size();
        }
        
        @Override
        public void clear() {
            slot.slot.clearEntries();
        }
        
        @Override
        public void add(int index, T element) {
            slot.slot.entry(element.unwrapStack(type));
        }
    }
}
