/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.api;

import me.shedaniel.rei.gui.renderers.EmptyRenderer;
import me.shedaniel.rei.gui.renderers.FluidRenderer;
import me.shedaniel.rei.gui.renderers.ItemStackRenderer;
import me.shedaniel.rei.gui.renderers.SimpleRecipeRenderer;
import me.shedaniel.rei.gui.widget.QueuedTooltip;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class Renderer extends DrawableHelper {
    /**
     * Gets an item stack renderer by an item stack supplier
     *
     * @param supplier the supplier for getting the item stack
     * @return the item stack renderer
     */
    public static ItemStackRenderer fromItemStackSupplier(Supplier<ItemStack> supplier) {
        return fromItemStacks(() -> Collections.singletonList(supplier.get()), true, null);
    }
    
    /**
     * Gets an item stack renderer by an item stack supplier
     *
     * @param supplier the supplier for getting the item stack
     * @return the item stack renderer
     */
    public static ItemStackRenderer fromItemStackSupplierNoCounts(Supplier<ItemStack> supplier) {
        return fromItemStacks(() -> Collections.singletonList(supplier.get()), false, null);
    }
    
    /**
     * Gets an item stack renderer by an item stack
     *
     * @param stack the item stack to be displayed
     * @return the item stack renderer
     */
    public static ItemStackRenderer fromItemStack(ItemStack stack) {
        return fromItemStacks(() -> Collections.singletonList(stack), true, null);
    }
    
    public static FluidRenderer fromFluid(Fluid fluid) {
        return fromFluid(() -> fluid, null);
    }
    
    public static FluidRenderer fromFluid(Supplier<Fluid> fluidSupplier, @Nullable Function<Fluid, List<String>> extraTooltipSupplier) {
        return new FluidRenderer() {
            @Override
            public Fluid getFluid() {
                return fluidSupplier.get();
            }
            
            @Override
            protected List<String> getExtraToolTips(Fluid fluid) {
                if (extraTooltipSupplier == null)
                    return super.getExtraToolTips(fluid);
                List<String> apply = extraTooltipSupplier.apply(fluid);
                if (apply == null)
                    return super.getExtraToolTips(fluid);
                return apply;
            }
        };
    }
    
    /**
     * Gets an item stack renderer by an item stack
     *
     * @param stack the item stack to be displayed
     * @return the item stack renderer
     */
    public static ItemStackRenderer fromItemStackNoCounts(ItemStack stack) {
        return fromItemStacks(() -> Collections.singletonList(stack), false, null);
    }
    
    /**
     * Gets an empty renderer
     *
     * @return an empty renderer
     */
    public static EmptyRenderer empty() {
        return EmptyRenderer.INSTANCE;
    }
    
    /**
     * Gets a simple recipe renderer from inputs and outputs
     *
     * @param input  the list of input items
     * @param output the list of output items
     * @return the recipe renderer
     */
    public static SimpleRecipeRenderer fromRecipe(Supplier<List<List<ItemStack>>> input, Supplier<List<ItemStack>> output) {
        return new SimpleRecipeRenderer(input, output);
    }
    
    public static ItemStackRenderer fromItemStacks(List<ItemStack> stacks) {
        return fromItemStacks(() -> stacks, true, null);
    }
    
    public static ItemStackRenderer fromItemStacks(Supplier<List<ItemStack>> stacksSupplier, boolean renderCounts, @Nullable Function<ItemStack, List<String>> extraTooltipSupplier) {
        return fromItemStacks(stacksSupplier, stack -> renderCounts ? null : "", extraTooltipSupplier);
    }
    
    public static ItemStackRenderer fromItemStacks(Supplier<List<ItemStack>> stacksSupplier, Function<ItemStack, String> countsFunction, @Nullable Function<ItemStack, List<String>> extraTooltipSupplier) {
        return new ItemStackRenderer() {
            @Override
            public ItemStack getItemStack() {
                if (stacksSupplier.get().isEmpty())
                    return ItemStack.EMPTY;
                return stacksSupplier.get().get(MathHelper.floor((System.currentTimeMillis() / 500 % (double) stacksSupplier.get().size()) / 1f));
            }
            
            @Override
            protected String getCounts() {
                return countsFunction.apply(getItemStack());
            }
            
            @Override
            protected List<String> getExtraToolTips(ItemStack stack) {
                if (extraTooltipSupplier == null)
                    return super.getExtraToolTips(stack);
                List<String> apply = extraTooltipSupplier.apply(stack);
                if (apply == null)
                    return super.getExtraToolTips(stack);
                return apply;
            }
        };
    }
    
    public static ItemStackRenderer fromItemStacksNoCounts(List<ItemStack> stacks) {
        return fromItemStacks(() -> stacks, false, null);
    }
    
    /**
     * Gets the current blit offset
     *
     * @return the blit offset
     */
    public int getBlitOffset() {
        return this.blitOffset;
    }
    
    /**
     * Sets the current blit offset
     *
     * @param offset the new blit offset
     */
    public void setBlitOffset(int offset) {
        this.blitOffset = offset;
    }
    
    /**
     * Renders of the renderable
     *
     * @param x      the x coordinate of the renderable
     * @param y      the y coordinate of the renderable
     * @param mouseX the x coordinate of the mouse
     * @param mouseY the y coordinate of the mouse
     * @param delta  the delta
     */
    public abstract void render(int x, int y, double mouseX, double mouseY, float delta);
    
    @Nullable
    public QueuedTooltip getQueuedTooltip(float delta) {
        return null;
    }
    
}
