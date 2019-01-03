package me.shedaniel.mixins;

import me.shedaniel.listenerdefinitions.GuiCickListener;
import me.shedaniel.listenerdefinitions.GuiKeyDown;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.client.renderer.InventoryEffectRenderer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemGroup;
import org.dimdev.riftloader.RiftLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GuiContainerCreative.class)
public abstract class MixinGuiContainerCreative extends InventoryEffectRenderer {
    
    @Shadow
    public abstract int getSelectedTabIndex();
    
    @Shadow
    protected abstract boolean needsScrollBars();
    
    public MixinGuiContainerCreative(Container container_1) {
        super(container_1);
    }
    
    @Inject(method = "keyPressed(III)Z", at = @At("HEAD"), cancellable = true)
    public void keyPressed(int p_keyPressed_1_, int p_keyPressed_2_, int p_keyPressed_3_, CallbackInfoReturnable<Boolean> ci) {
        boolean handled = false;
        if (getSelectedTabIndex() == ItemGroup.INVENTORY.getIndex()) {
            for(GuiKeyDown listener : RiftLoader.instance.getListeners(GuiKeyDown.class))
                if (listener.keyDown(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_)) {
                    ci.setReturnValue(true);
                    handled = true;
                }
        }
        if (handled)
            ci.cancel();
    }
    
    @Inject(method = "mouseScrolled(D)Z", at = @At("HEAD"), cancellable = true)
    public void mouseScrolled(double p_mouseScrolled_1_, CallbackInfoReturnable<Boolean> ci) {
        if (!this.needsScrollBars()) {
            ci.setReturnValue(super.mouseScrolled(p_mouseScrolled_1_));
            ci.cancel();
        }
    }
    
    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void onMouseClicked(double p_mouseClicked_1_, double p_mouseClicked_3_, int p_mouseClicked_5_, CallbackInfoReturnable<Boolean> ci) {
        if (getSelectedTabIndex() != ItemGroup.INVENTORY.getIndex())
            return;
        boolean handled = false;
        for(GuiCickListener listener : RiftLoader.instance.getListeners(GuiCickListener.class)) {
            if (listener.onClick((int) p_mouseClicked_1_, (int) p_mouseClicked_3_, p_mouseClicked_5_)) {
                ci.setReturnValue(true);
                handled = true;
            }
        }
        if (handled)
            ci.cancel();
    }
    
}
