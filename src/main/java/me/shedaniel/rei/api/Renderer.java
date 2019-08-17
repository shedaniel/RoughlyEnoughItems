/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.api;

import me.shedaniel.rei.gui.renderers.EmptyRenderer;
import me.shedaniel.rei.gui.renderers.FluidRenderer;
import me.shedaniel.rei.gui.renderers.ItemStackRenderer;
import me.shedaniel.rei.gui.renderers.SimpleRecipeRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;

import java.util.List;
import java.util.function.Supplier;

public abstract class Renderer extends DrawableHelper {
    /**
     * Gets an item stack renderer by an item stack supplier
     *
     * @param supplier the supplier for getting the item stack
     * @return the item stack renderer
     */
    public static ItemStackRenderer fromItemStackSupplier(Supplier<ItemStack> supplier) {
        return new ItemStackRenderer() {
            @Override
            public ItemStack getItemStack() {
                return supplier.get();
            }
        };
    }
    
    /**
     * Gets an item stack renderer by an item stack supplier
     *
     * @param supplier the supplier for getting the item stack
     * @return the item stack renderer
     */
    public static ItemStackRenderer fromItemStackSupplierNoCounts(Supplier<ItemStack> supplier) {
        return new ItemStackRenderer() {
            @Override
            public ItemStack getItemStack() {
                return supplier.get();
            }
            
            @Override
            protected boolean renderCounts() {
                return false;
            }
        };
    }
    
    /**
     * Gets an item stack renderer by an item stack
     *
     * @param stack the item stack to be displayed
     * @return the item stack renderer
     */
    public static ItemStackRenderer fromItemStack(ItemStack stack) {
        return fromItemStackSupplier(() -> stack);
    }
    
    public static FluidRenderer fromFluid(Fluid fluid) {
        return new FluidRenderer() {
            @Override
            public Fluid getFluid() {
                return fluid;
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
        return fromItemStackSupplierNoCounts(() -> stack);
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
        return new ItemStackRenderer() {
            @Override
            public ItemStack getItemStack() {
                if (stacks.isEmpty())
                    return ItemStack.EMPTY;
                return stacks.get(MathHelper.floor((System.currentTimeMillis() / 500 % (double) stacks.size()) / 1f));
            }
        };
    }
    
    public static ItemStackRenderer fromItemStacksNoCounts(List<ItemStack> stacks) {
        return new ItemStackRenderer() {
            @Override
            public ItemStack getItemStack() {
                if (stacks.isEmpty())
                    return ItemStack.EMPTY;
                return stacks.get(MathHelper.floor((System.currentTimeMillis() / 500 % (double) stacks.size()) / 1f));
            }
            
            @Override
            protected boolean renderCounts() {
                return false;
            }
        };
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
    
}
