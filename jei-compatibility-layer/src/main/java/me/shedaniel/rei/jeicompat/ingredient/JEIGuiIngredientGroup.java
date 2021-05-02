/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020, 2021 shedaniel
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

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import me.shedaniel.architectury.utils.Value;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.widgets.Slot;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.jeicompat.JEIPluginDetector;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IGuiIngredient;
import mezz.jei.api.gui.ingredient.IGuiIngredientGroup;
import mezz.jei.api.gui.ingredient.ITooltipCallback;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IFocus;
import net.minecraft.client.gui.GuiComponent;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

import static me.shedaniel.rei.jeicompat.JEIPluginDetector.*;

public class JEIGuiIngredientGroup<T> implements IGuiIngredientGroup<T> {
    private static final Method func_238468_a_ = ObfuscationReflectionHelper.findMethod(GuiComponent.class, "func_238468_a_",
            PoseStack.class, Integer.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE);
    private final IIngredientType<T> type;
    private final Int2ObjectMap<SlotWrapper> slots = new Int2ObjectOpenHashMap<>();
    public final List<ITooltipCallback<T>> tooltipCallbacks = new ArrayList<>();
    public final Value<IDrawable> background;
    
    public JEIGuiIngredientGroup(IIngredientType<T> type, Value<IDrawable> background) {
        this.type = type;
        this.background = background;
    }
    
    protected SlotWrapper getSlot(int slotIndex) {
        return slots.computeIfAbsent(slotIndex, i -> new SlotWrapper(Widgets.createSlot(new Point(0, 0))
                .disableBackground()));
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
            if (slotWrapper.slot.getNoticeMark() == Slot.INPUT) {
                if (inputIndex < inputs.size()) {
                    slotWrapper.slot.clearEntries();
                    slotWrapper.slot.entries(JEIPluginDetector.wrapList(type, inputs.get(inputIndex++)));
                }
            } else if (slotWrapper.slot.getNoticeMark() == Slot.OUTPUT) {
                if (outputIndex < outputs.size()) {
                    slotWrapper.slot.clearEntries();
                    slotWrapper.slot.entries(JEIPluginDetector.wrapList(type, outputs.get(outputIndex++)));
                }
            }
        }
    }
    
    @Override
    public void set(int slotIndex, @Nullable List<T> ingredients) {
        Slot slot = getSlot(slotIndex).slot;
        slot.clearEntries();
        slot.entries(JEIPluginDetector.wrapList(type, ingredients));
    }
    
    @Override
    public void set(int slotIndex, @Nullable T ingredient) {
        Slot slot = getSlot(slotIndex).slot;
        slot.clearEntries();
        slot.entry(wrap(type, ingredient));
    }
    
    @Override
    public void setBackground(int slotIndex, @NotNull IDrawable background) {
        SlotWrapper slot = getSlot(slotIndex);
        slot.background = background;
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
        slot.slot.setNoticeMark(input ? Slot.INPUT : Slot.OUTPUT);
        slot.slot.getBounds().setLocation(xPosition, yPosition);
    }
    
    @Override
    public void init(int slotIndex, boolean input, @NotNull IIngredientRenderer<T> ingredientRenderer, int xPosition, int yPosition, int width, int height, int xPadding, int yPadding) {
        init(slotIndex, input, xPosition - 1, yPosition - 1);
        SlotWrapper slot = getSlot(slotIndex);
        slot.slot.getBounds().setSize(width + 2, height + 2);
        slot.renderer = ingredientRenderer;
    }
    
    @Override
    public void setOverrideDisplayFocus(@Nullable IFocus<T> focus) {
        throw WILL_NOT_BE_IMPLEMENTED();
    }
    
    public class SlotWrapper extends AbstractList<T> implements IGuiIngredient<T> {
        public final Slot slot;
        public float fluidCapacity = Float.NaN;
        @Nullable
        public IIngredientRenderer<T> renderer;
        @Nullable
        public IDrawable background;
        @Nullable
        public IDrawable overlay;
        
        public SlotWrapper(Slot slot) {
            this.slot = slot;
        }
        
        @Override
        @Nullable
        public T getDisplayedIngredient() {
            return unwrap(slot.getCurrentEntry().cast());
        }
        
        @Override
        @NotNull
        public List<T> getAllIngredients() {
            return this;
        }
        
        @Override
        public boolean isInput() {
            return slot.getNoticeMark() == Slot.INPUT;
        }
        
        @Override
        public void drawHighlight(@NotNull PoseStack matrixStack, int color, int xOffset, int yOffset) {
            RenderSystem.disableDepthTest();
            RenderSystem.colorMask(true, true, true, false);
            slot.setZ(300);
            Rectangle bounds = slot.getInnerBounds();
            try {
                func_238468_a_.invoke(slot, matrixStack, bounds.x, bounds.y, bounds.getMaxX(), bounds.getMaxY(), color, color);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
            slot.setZ(0);
            RenderSystem.colorMask(true, true, true, true);
            RenderSystem.enableDepthTest();
        }
        
        @Override
        public T get(int index) {
            return unwrap(slot.getEntries().get(index).cast());
        }
        
        @Override
        public int size() {
            return slot.getEntries().size();
        }
        
        @Override
        public void clear() {
            slot.clearEntries();
        }
        
        @Override
        public void add(int index, T element) {
            slot.entry(wrap(type, element));
        }
    }
}
