package me.shedaniel.rei.mixin;

import me.shedaniel.rei.api.ContainerScreenHooks;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(GuiContainer.class)
public class MixinGuiContainer implements ContainerScreenHooks {
    @Shadow protected int guiLeft;
    
    @Shadow protected int guiTop;
    
    @Shadow protected int xSize;
    
    @Shadow protected int ySize;
    
    @Shadow protected Slot hoveredSlot;
    
    @Override
    public int rei_getContainerLeft() {
        return guiLeft;
    }
    
    @Override
    public int rei_getContainerTop() {
        return guiTop;
    }
    
    @Override
    public int rei_getContainerWidth() {
        return xSize;
    }
    
    @Override
    public int rei_getContainerHeight() {
        return ySize;
    }
    
    @Override
    public Slot rei_getHoveredSlot() {
        return hoveredSlot;
    }
}
