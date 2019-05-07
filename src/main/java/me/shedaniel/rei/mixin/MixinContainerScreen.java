package me.shedaniel.rei.mixin;

import me.shedaniel.rei.listeners.ContainerScreenHooks;
import net.minecraft.client.gui.ContainerScreen;
import net.minecraft.container.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ContainerScreen.class)
public class MixinContainerScreen implements ContainerScreenHooks {
    
    @Shadow protected int left;
    @Shadow protected int top;
    @Shadow protected int containerWidth;
    @Shadow protected int containerHeight;
    @Shadow protected Slot focusedSlot;
    
    @Override
    public int rei_getContainerLeft() {
        return left;
    }
    
    @Override
    public int rei_getContainerTop() {
        return top;
    }
    
    @Override
    public int rei_getContainerWidth() {
        return containerWidth;
    }
    
    @Override
    public int rei_getContainerHeight() {
        return containerHeight;
    }
    
    @Override
    public Slot rei_getHoveredSlot() {
        return focusedSlot;
    }
    
}
