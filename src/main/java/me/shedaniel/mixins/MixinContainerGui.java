package me.shedaniel.mixins;

import me.shedaniel.Core;
import me.shedaniel.listenerdefinitions.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.ContainerGui;
import net.minecraft.client.gui.Gui;
import net.minecraft.container.Slot;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Created by James on 7/27/2018.
 */
@Mixin(ContainerGui.class)
public abstract class MixinContainerGui extends Gui implements IMixinContainerGui {
    @Shadow
    protected Slot focusedSlot;
    @Shadow
    private ItemStack field_2782; //draggedStack
    @Shadow
    protected int left;
    @Shadow
    protected int top;
    @Shadow
    protected int containerWidth;
    @Shadow
    protected int containerHeight;
    
    @Inject(method = "draw(IIF)V", at = @At("RETURN"))
    public void draw(int p_drawScreen_1_, int p_drawScreen_2_, float p_drawScreen_3_, CallbackInfo ci) {
        Core.getListeners(DrawContainer.class).forEach(drawContainer ->
                drawContainer.draw(p_drawScreen_1_, p_drawScreen_2_, p_drawScreen_3_, (ContainerGui) MinecraftClient.getInstance().currentGui));
    }
    
    @Inject(method = "mouseClicked(DDI)Z", at = @At("HEAD"), cancellable = true)
    private void mouseClick(double p_mouseClicked_1_, double p_mouseClicked_3_, int p_mouseClicked_5_, CallbackInfoReturnable<Boolean> ci) {
        boolean handled = false;
        for(GuiCickListener listener : Core.getListeners(GuiCickListener.class)) {
            if (listener.onClick((int) p_mouseClicked_1_, (int) p_mouseClicked_3_, p_mouseClicked_5_)) {
                ci.setReturnValue(true);
                handled = true;
            }
        }
        if (handled)
            ci.cancel();
    }
    
    @Inject(method = "keyPressed(III)Z", at = @At("HEAD"), cancellable = true)
    private void onKeyPressed(int p_keyPressed_1_, int p_keyPressed_2_, int p_keyPressed_3_, CallbackInfoReturnable<Boolean> ci) {
        boolean handled = false;
        for(GuiKeyDown listener : Core.getListeners(GuiKeyDown.class)) {
            if (listener.keyDown(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_))
                handled = true;
        }
        if (handled) {
            ci.setReturnValue(handled);
            ci.cancel();
        }
    }
    
    public boolean mouseScrolled(double p_mouseScrolled_1_) {
        for(MouseScrollListener listener : Core.getListeners(MouseScrollListener.class))
            if (listener.mouseScrolled(p_mouseScrolled_1_))
                return true;
        return super.mouseScrolled(p_mouseScrolled_1_);
    }
    
    @Override
    public ItemStack getDraggedStack() {
        return field_2782;
    }
    
    @Override
    public int getGuiLeft() {
        return left;
    }
    
    @Override
    public int getXSize() {
        return containerWidth;
    }
    
    @Override
    public Slot getHoveredSlot() {
        return focusedSlot;
    }
    
    @Override
    public int getContainerHeight() {
        return containerHeight;
    }
    
    @Override
    public int getContainerWidth() {
        return containerWidth;
    }
}
