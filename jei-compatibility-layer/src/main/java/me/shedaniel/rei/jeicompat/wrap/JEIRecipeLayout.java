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

package me.shedaniel.rei.jeicompat.wrap;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import dev.architectury.utils.Value;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.screen.DisplayScreen;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.util.ClientEntryStacks;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes;
import me.shedaniel.rei.jeicompat.ingredient.JEIGuiIngredientGroup;
import me.shedaniel.rei.jeicompat.ingredient.JEIGuiIngredientGroupFluid;
import me.shedaniel.rei.jeicompat.ingredient.JEIGuiIngredientGroupItem;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IGuiFluidStackGroup;
import mezz.jei.api.gui.ingredient.IGuiIngredientGroup;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.gui.ingredient.ITooltipCallback;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static me.shedaniel.rei.jeicompat.JEIPluginDetector.*;

public class JEIRecipeLayout<T> implements IRecipeLayout {
    private final JEIWrappedCategory<T> category;
    private final JEIWrappedDisplay<T> display;
    private final Map<IIngredientType<?>, IGuiIngredientGroup<?>> groups = new HashMap<>();
    public final Value<IDrawable> background;
    
    public JEIRecipeLayout(JEIWrappedCategory<T> category, JEIWrappedDisplay<T> display, Value<IDrawable> background) {
        this.category = category;
        this.display = display;
        this.background = background;
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
        return (IGuiIngredientGroup<T>) groups.computeIfAbsent(ingredientType, type -> {
            if (Objects.equals(type.getIngredientClass(), ItemStack.class))
                return new JEIGuiIngredientGroupItem(type.cast(), background);
            if (Objects.equals(type.getIngredientClass(), FluidStack.class))
                return new JEIGuiIngredientGroupFluid(type.cast(), background);
            return new JEIGuiIngredientGroup<>(type, background);
        });
    }
    
    @Override
    @Nullable
    public IFocus<?> getFocus() {
        DisplayScreen screen = (DisplayScreen) Minecraft.getInstance().screen;
        List<EntryStack<?>> notice = screen.getIngredientsToNotice();
        if (!notice.isEmpty()) {
            return new JEIFocus<>(IFocus.Mode.INPUT, unwrap(notice.get(0).cast())).wrap();
        }
        notice = screen.getResultsToNotice();
        if (!notice.isEmpty()) {
            return new JEIFocus<>(IFocus.Mode.OUTPUT, unwrap(notice.get(0).cast())).wrap();
        }
        return null;
    }
    
    @Override
    @Nullable
    public <V> IFocus<V> getFocus(@NotNull IIngredientType<V> ingredientType) {
        return JEIFocus.cast(getFocus(), ingredientType);
    }
    
    @Override
    @NotNull
    public IRecipeCategory<?> getRecipeCategory() {
        return category.getBackingCategory();
    }
    
    @Override
    public void moveRecipeTransferButton(int posX, int posY) {
        throw TODO();
    }
    
    @Override
    public void setShapeless() {
        throw TODO();
    }
    
    public Map<IIngredientType<?>, IGuiIngredientGroup<?>> getGroups() {
        return groups;
    }
    
    public void addTo(List<Widget> widgets, Rectangle bounds) {
        for (Map.Entry<IIngredientType<?>, IGuiIngredientGroup<?>> groupEntry : getGroups().entrySet()) {
            JEIGuiIngredientGroup<?> group = (JEIGuiIngredientGroup<?>) groupEntry.getValue();
            Int2ObjectMap<? extends JEIGuiIngredientGroup<?>.SlotWrapper> guiIngredients = group.getGuiIngredients();
            IntArrayList integers = new IntArrayList(guiIngredients.keySet());
            integers.sort(Comparator.naturalOrder());
            for (int integer : integers) {
                JEIGuiIngredientGroup<?>.SlotWrapper wrapper = guiIngredients.get(integer);
                wrapper.slot.getBounds().translate(bounds.x + 4, bounds.y + 4);
                wrapper.slot.highlightEnabled(!wrapper.isEmpty());
                
                if (wrapper.background != null) {
                    widgets.add(Widgets.wrapRenderer(wrapper.slot.getInnerBounds().clone(), wrapDrawable(wrapper.background)));
                }
                
                widgets.add(Widgets.withTranslate(wrapper.slot, 0, 0, 10));
                
                if (wrapper.renderer != null) {
                    JEIEntryDefinition.Renderer<?> renderer = new JEIEntryDefinition.Renderer<>(wrapper.renderer);
                    for (EntryStack<?> entry : wrapper.slot.getEntries()) {
                        ClientEntryStacks.setRenderer(entry, renderer);
                    }
                } else if (wrapper.fluidCapacity == wrapper.fluidCapacity) {
                    for (EntryStack<?> entry : wrapper.slot.getEntries()) {
                        if (entry.getType() == VanillaEntryTypes.FLUID) {
                            ClientEntryStacks.setFluidRenderRatio(entry.cast(),
                                    entry.<dev.architectury.fluid.FluidStack>cast().getValue().getAmount().floatValue() / wrapper.fluidCapacity);
                        }
                    }
                }
                
                if (wrapper.overlay != null) {
                    widgets.add(Widgets.withTranslate(Widgets.wrapRenderer(wrapper.slot.getInnerBounds().clone(), wrapDrawable(wrapper.overlay)), 0, 251, 0));
                }
                
                List<ITooltipCallback<Object>> tooltipCallbacks = (List<ITooltipCallback<Object>>) (List) group.tooltipCallbacks;
                for (EntryStack<?> entry : wrapper.slot.getEntries()) {
                    ClientEntryStacks.setTooltipProcessor(entry, (stack, tooltip) -> {
                        Object ingredient = null;
                        for (ITooltipCallback<Object> callback : tooltipCallbacks) {
                            if (ingredient == null) {
                                ingredient = unwrap(stack);
                            }
                            callback.onTooltip(integer, wrapper.isInput(), ingredient, tooltip.getText());
                        }
                        return tooltip;
                    });
                }
            }
        }
    }
}
