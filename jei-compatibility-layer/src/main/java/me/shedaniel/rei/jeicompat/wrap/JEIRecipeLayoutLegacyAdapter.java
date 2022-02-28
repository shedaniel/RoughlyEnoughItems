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

import com.mojang.blaze3d.vertex.PoseStack;
import lombok.experimental.ExtensionMethod;
import me.shedaniel.rei.api.common.entry.type.EntryType;
import me.shedaniel.rei.jeicompat.JEIPluginDetector;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.*;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@ExtensionMethod(JEIPluginDetector.class)
public class JEIRecipeLayoutLegacyAdapter implements IRecipeLayout {
    private final Map<EntryType<?>, GroupView<?>> groups = new HashMap<>();
    private final JEIDisplaySetup.Result view;
    
    public JEIRecipeLayoutLegacyAdapter(JEIDisplaySetup.Result view) {
        this.view = view;
    }
    
    @Override
    @NotNull
    public IGuiItemStackGroup getItemStacks() {
        return (IGuiItemStackGroup) getIngredientsGroup(VanillaTypes.ITEM);
    }
    
    @Override
    @NotNull
    public IGuiFluidStackGroup getFluidStacks() {
        return (IGuiFluidStackGroup) getIngredientsGroup(VanillaTypes.FLUID);
    }
    
    @Override
    @NotNull
    public <T> IGuiIngredientGroup<T> getIngredientsGroup(@NotNull IIngredientType<T> ingredientType) {
        return (IGuiIngredientGroup<T>) groups.computeIfAbsent(ingredientType.unwrapType(), type -> {
            if (Objects.equals(ingredientType.getIngredientClass(), ItemStack.class))
                return new GroupViewItem(ingredientType.cast());
            if (Objects.equals(ingredientType.getIngredientClass(), FluidStack.class))
                return new GroupViewFluid(ingredientType.cast());
            return new GroupView<>(ingredientType);
        });
    }
    
    @Nullable
    public IFocus<?> getFocus() {
        List<IFocus<?>> foci = JEIWrappedDisplay.getFoci();
        if (foci.isEmpty()) return null;
        return foci.get(0);
    }
    
    @Override
    @Nullable
    public <V> IFocus<V> getFocus(@NotNull IIngredientType<V> ingredientType) {
        IFocus<?> focus = getFocus();
        if (focus == null) return null;
        ITypedIngredient<V> typedIngredient = (ITypedIngredient<V>) focus.getTypedValue();
        return new JEIFocus<>(focus.getRole(), new JEITypedIngredient<>(typedIngredient.getType(), typedIngredient.getIngredient()));
    }
    
    @Override
    public void moveRecipeTransferButton(int posX, int posY) {
        throw new UnsupportedOperationException("This method is not supported during transfer");
    }
    
    @Override
    public void setShapeless() {
        throw new UnsupportedOperationException("This method is not supported during transfer");
    }
    
    public class GroupView<T> implements IGuiIngredientGroup<T> {
        private final IIngredientType<T> type;
        
        public GroupView(IIngredientType<T> type) {
            this.type = type;
        }
        
        @Override
        public void set(IIngredients ingredients) {
            throw new UnsupportedOperationException("This method is not supported during transfer");
        }
        
        @Override
        public void set(int ingredientIndex, @Nullable List<T> ingredients) {
            throw new UnsupportedOperationException("This method is not supported during transfer");
        }
        
        @Override
        public void set(int ingredientIndex, @Nullable T ingredient) {
            throw new UnsupportedOperationException("This method is not supported during transfer");
        }
        
        @Override
        public void setBackground(int ingredientIndex, IDrawable background) {
            throw new UnsupportedOperationException("This method is not supported during transfer");
        }
        
        @Override
        public void addTooltipCallback(ITooltipCallback<T> tooltipCallback) {
            throw new UnsupportedOperationException("This method is not supported during transfer");
        }
        
        @Override
        public Map<Integer, ? extends IGuiIngredient<T>> getGuiIngredients() {
            Map<Integer, IGuiIngredient<T>> ingredients = new HashMap<>();
            for (JEIRecipeSlot slot : view.slots) {
                if (slot.getIndex() >= 0) {
                    GuiIngredientView view = new GuiIngredientView(slot);
                    if (view.getAllIngredients().isEmpty()) continue;
                    ingredients.put(slot.getIndex(), view);
                }
            }
            return ingredients;
        }
        
        public class GuiIngredientView implements IGuiIngredient<T> {
            public final JEIRecipeSlot slot;
            
            public GuiIngredientView(JEIRecipeSlot slot) {
                this.slot = slot;
            }
            
            @Override
            public IIngredientType<T> getIngredientType() {
                return type;
            }
            
            @Override
            @Nullable
            public T getDisplayedIngredient() {
                return slot.getDisplayedIngredient(type).orElse(null);
            }
            
            @Override
            public List<T> getAllIngredients() {
                return slot.getIngredients(type).collect(Collectors.toList());
            }
            
            @Override
            public boolean isInput() {
                return slot.role == RecipeIngredientRole.INPUT || slot.role == RecipeIngredientRole.CATALYST;
            }
            
            @Override
            public void drawHighlight(PoseStack stack, int color, int xOffset, int yOffset) {
                stack.pushPose();
                stack.translate(xOffset, yOffset, 0);
                slot.drawHighlight(stack, color);
                stack.popPose();
            }
        }
        
        @Override
        public void init(int ingredientIndex, boolean input, int xPosition, int yPosition) {
            throw new UnsupportedOperationException("This method is not supported during transfer");
        }
        
        @Override
        public void init(int ingredientIndex, boolean input, IIngredientRenderer<T> ingredientRenderer, int xPosition, int yPosition, int width, int height, int xInset, int yInset) {
            throw new UnsupportedOperationException("This method is not supported during transfer");
        }
        
        @Override
        public void setOverrideDisplayFocus(@Nullable IFocus<T> focus) {
            throw new UnsupportedOperationException("This method is not supported during transfer");
        }
    }
    
    public class GroupViewItem extends GroupView<ItemStack> implements IGuiItemStackGroup {
        public GroupViewItem(IIngredientType<ItemStack> type) {
            super(type);
        }
    }
    
    public class GroupViewFluid extends GroupView<FluidStack> implements IGuiFluidStackGroup {
        public GroupViewFluid(IIngredientType<FluidStack> type) {
            super(type);
        }
        
        @Override
        public void init(int ingredientIndex, boolean input, int xPosition, int yPosition, int width, int height, int capacityMb, boolean showCapacity, @Nullable IDrawable overlay) {
            throw new UnsupportedOperationException("This method is not supported during transfer");
        }
    }
}
