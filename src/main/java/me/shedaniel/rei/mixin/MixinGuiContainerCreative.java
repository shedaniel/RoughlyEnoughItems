package me.shedaniel.rei.mixin;

import me.shedaniel.rei.listeners.IMixinTabGetter;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.client.renderer.InventoryEffectRenderer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemGroup;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GuiContainerCreative.class)
public abstract class MixinGuiContainerCreative extends InventoryEffectRenderer implements IMixinTabGetter {
    
    @Shadow
    private static int selectedTabIndex;
    @Shadow
    private boolean field_195377_F;
    
    public MixinGuiContainerCreative(Container inventorySlotsIn) {
        super(inventorySlotsIn);
    }
    
    @Shadow
    protected abstract boolean needsScrollBars();
    
    @Override
    public int getSelectedTab() {
        return selectedTabIndex;
    }
    
    @Inject(method = "mouseScrolled", at = @At("HEAD"), cancellable = true)
    public void mouseScrolled(double amount, CallbackInfoReturnable<Boolean> ci) {
        if (!needsScrollBars())
            if (super.mouseScrolled(amount)) {
                ci.setReturnValue(true);
                ci.cancel();
            }
    }
    
    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    public void keyPressed(int int_1, int int_2, int int_3, CallbackInfoReturnable<Boolean> ci) {
        if (selectedTabIndex == ItemGroup.INVENTORY.getIndex())
            if (super.keyPressed(int_1, int_2, int_3)) {
                ci.setReturnValue(true);
                ci.cancel();
            }
    }
    
    @Inject(method = "charTyped", at = @At("HEAD"), cancellable = true)
    public void charTyped(char char_1, int int_1, CallbackInfoReturnable<Boolean> ci) {
        if (!this.field_195377_F && selectedTabIndex == ItemGroup.INVENTORY.getIndex())
            if (super.charTyped(char_1, int_1)) {
                ci.setReturnValue(true);
                ci.cancel();
            }
    }
    
}
