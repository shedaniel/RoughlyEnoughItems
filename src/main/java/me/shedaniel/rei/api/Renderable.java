package me.shedaniel.rei.api;

import me.shedaniel.rei.gui.renderables.EmptyRenderer;
import me.shedaniel.rei.gui.renderables.ItemStackRenderer;
import net.minecraft.item.ItemStack;

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
    
    void render(int x, int y, double mouseX, double mouseY, float delta);
}
