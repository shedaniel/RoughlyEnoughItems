package me.shedaniel.rei.api;

import me.shedaniel.rei.gui.renderables.EmptyRenderer;
import me.shedaniel.rei.gui.renderables.ItemStackRenderer;
import me.shedaniel.rei.gui.renderables.SimpleRecipeRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;

import java.util.List;
import java.util.function.Supplier;

public interface Renderable {
    
    static ItemStackRenderer fromItemStackSupplier(Supplier<ItemStack> supplier) {
        return new ItemStackRenderer() {
            @Override
            public ItemStack getItemStack() {
                return supplier.get();
            }
        };
    }
    
    static ItemStackRenderer fromItemStack(ItemStack stack) {
        return new ItemStackRenderer() {
            @Override
            public ItemStack getItemStack() {
                return stack;
            }
        };
    }
    
    static EmptyRenderer empty() {
        return EmptyRenderer.INSTANCE;
    }
    
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
    
    void render(int x, int y, double mouseX, double mouseY, float delta);
}
