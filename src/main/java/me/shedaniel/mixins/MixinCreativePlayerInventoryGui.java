package me.shedaniel.mixins;

import me.shedaniel.Core;
import me.shedaniel.listenerdefinitions.GuiKeyDown;
import net.minecraft.client.gui.ingame.AbstractPlayerInventoryGui;
import net.minecraft.client.gui.ingame.CreativePlayerInventoryGui;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.container.Container;
import net.minecraft.container.Slot;
import net.minecraft.item.ItemGroup;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CreativePlayerInventoryGui.class)
public abstract class MixinCreativePlayerInventoryGui extends AbstractPlayerInventoryGui {
    
    @Shadow
    public abstract int method_2469();
    
    @Shadow
    protected abstract boolean doRenderScrollBar();
    
    public MixinCreativePlayerInventoryGui(Container container_1) {
        super(container_1);
    }
    
    @Inject(method = "keyPressed(III)Z", at = @At("HEAD"), cancellable = true)
    public void keyPressed(int p_keyPressed_1_, int p_keyPressed_2_, int p_keyPressed_3_, CallbackInfoReturnable<Boolean> ci) {
        boolean handled = false;
        if (method_2469() != ItemGroup.SEARCH.getId()) {
            if (method_2469() == ItemGroup.INVENTORY.getId()) {
                for(GuiKeyDown listener : Core.getListeners(GuiKeyDown.class))
                    if (listener.keyDown(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_)) {
                        ci.setReturnValue(true);
                        handled = true;
                    }
            }
        }
        if (handled)
            ci.cancel();
    }
    
    @Inject(method = "mouseScrolled(D)Z", at = @At("HEAD"), cancellable = true)
    public void mouseScrolled(double p_mouseScrolled_1_, CallbackInfoReturnable<Boolean> ci) {
        if (!this.doRenderScrollBar()) {
            ci.setReturnValue(super.mouseScrolled(p_mouseScrolled_1_));
            ci.cancel();
        }
    }
    
}
