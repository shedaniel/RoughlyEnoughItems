/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.api;

import me.shedaniel.rei.gui.renderables.EmptyRenderer;
import me.shedaniel.rei.gui.renderables.ItemStackRenderer;
import me.shedaniel.rei.gui.renderables.SimpleRecipeRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;

import java.util.List;
import java.util.function.Supplier;

/**
 * The base class for renderables
 */
public interface Renderable {
    
    /**
     * Gets an item stack renderer by an item stack supplier
     *
     * @param supplier the supplier for getting the item stack
     * @return the item stack renderer
     */
    static ItemStackRenderer fromItemStackSupplier(Supplier<ItemStack> supplier) {
        return new ItemStackRenderer() {
            @Override
            public ItemStack getItemStack() {
                return supplier.get();
            }
        };
    }
    
    /**
     * Gets an item stack renderer by an item stack
     *
     * @param stack the item stack to be displayed
     * @return the item stack renderer
     */
    static ItemStackRenderer fromItemStack(ItemStack stack) {
        return fromItemStackSupplier(() -> stack);
    }
    
    /**
     * Gets an empty renderer
     *
     * @return an empty renderer
     */
    static EmptyRenderer empty() {
        return EmptyRenderer.INSTANCE;
    }
    
    /**
     * Gets a simple recipe renderer from inputs and outputs
     *
     * @param input  the list of input items
     * @param output the list of output items
     * @return the recipe renderer
     */
    static SimpleRecipeRenderer fromRecipe(Supplier<List<List<ItemStack>>> input, Supplier<List<ItemStack>> output) {
        return new SimpleRecipeRenderer(input, output);
    }
    
    static ItemStackRenderer fromItemStacks(List<ItemStack> stacks) {
        return new ItemStackRenderer() {
            @Override
            public ItemStack getItemStack() {
                if (stacks.isEmpty())
                    return ItemStack.EMPTY;
                return stacks.get(MathHelper.floor((System.currentTimeMillis() / 500 % (double) stacks.size()) / 1f));
            }
        };
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
    void render(int x, int y, double mouseX, double mouseY, float delta);
}
