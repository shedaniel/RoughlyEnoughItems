package me.shedaniel.rei.api;

import me.shedaniel.rei.gui.renderables.ItemStackRenderable;
import net.minecraft.item.ItemStack;

import java.util.function.Supplier;

public interface Renderable {
    
    static ItemStackRenderable fromItemStackSupplier(Supplier<ItemStack> supplier) {
        return new ItemStackRenderable() {
            @Override
            protected ItemStack getItemStack() {
                return supplier.get();
            }
        };
    }
    
    static ItemStackRenderable fromItemStack(ItemStack stack) {
        return new ItemStackRenderable() {
            @Override
            protected ItemStack getItemStack() {
                return stack;
            }
        };
    }
    
    void render(int x, int y, double mouseX, double mouseY, float delta);
}
