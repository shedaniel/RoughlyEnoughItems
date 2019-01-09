package me.shedaniel.mixins;

import me.shedaniel.listenerdefinitions.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IGuiEventListenerDeferred;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import org.dimdev.riftloader.RiftLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Created by James on 7/27/2018.
 */
@Mixin(GuiContainer.class)
public abstract class MixinGuiContainer implements IGuiEventListenerDeferred, IMixinGuiContainer {
    @Shadow
    protected Slot hoveredSlot;
    @Shadow
    private ItemStack draggedStack;
    @Shadow
    protected int guiLeft;
    @Shadow
    protected int guiTop;
    @Shadow
    protected int xSize;
    @Shadow
    protected int ySize;
    
    @Inject(method = "render", at = @At("RETURN"))
    private void onRender(int p_drawScreen_1_, int p_drawScreen_2_, float p_drawScreen_3_, CallbackInfo ci) {
        for(DrawContainer listener : RiftLoader.instance.getListeners(DrawContainer.class)) {
            listener.draw(p_drawScreen_1_, p_drawScreen_2_, p_drawScreen_3_, (GuiContainer) Minecraft.getInstance().currentScreen);
        }
    }
    
    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void onMouseClicked(double p_mouseClicked_1_, double p_mouseClicked_3_, int p_mouseClicked_5_, CallbackInfoReturnable<Boolean> ci) {
        boolean handled = false;
        for(GuiClick listener : RiftLoader.instance.getListeners(GuiClick.class)) {
            if (listener.onClick((int) p_mouseClicked_1_, (int) p_mouseClicked_3_, p_mouseClicked_5_)) {
                ci.setReturnValue(true);
                handled = true;
            }
        }
        if (handled)
            ci.cancel();
    }
    
    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void onKeyPressed(int p_keyPressed_1_, int p_keyPressed_2_, int p_keyPressed_3_, CallbackInfoReturnable<Boolean> ci) {
        boolean handled = false;
        for(GuiKeyDown listener : RiftLoader.instance.getListeners(GuiKeyDown.class)) {
            if (listener.keyDown(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_))
                handled = true;
        }
        if (handled) {
            ci.setReturnValue(handled);
            ci.cancel();
        }
    }
    
    public boolean mouseScrolled(double p_mouseScrolled_1_) {
        boolean handled = false;
        for(GuiMouseScroll listener : RiftLoader.instance.getListeners(GuiMouseScroll.class)) {
            if (listener.mouseScrolled(p_mouseScrolled_1_)) {
                handled = true;
            }
        }
        return handled;
    }
    
    @Override
    public ItemStack getDraggedStack() {
        return draggedStack;
    }
    
    @Override
    public int getGuiLeft() {
        return guiLeft;
    }
    
    @Override
    public int getXSize() {
        return xSize;
    }
    
    @Override
    public Slot getHoveredSlot() {
        return hoveredSlot;
    }
    
    @Override
    public int getContainerHeight() {
        return ySize;
    }
    
    @Override
    public int getContainerWidth() {
        return xSize;
    }
}
