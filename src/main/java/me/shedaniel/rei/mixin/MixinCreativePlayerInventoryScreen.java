package me.shedaniel.rei.mixin;

import me.shedaniel.rei.client.ClientHelper;
import me.shedaniel.rei.client.ScreenHelper;
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
public abstract class MixinCreativePlayerInventoryScreen extends InventoryEffectRenderer {
    
    @Shadow
    private static int selectedTabIndex;
    @Shadow
    private boolean field_195377_F;
    
    public MixinCreativePlayerInventoryScreen(Container inventorySlotsIn) {
        super(inventorySlotsIn);
    }
    
    @Shadow
    protected abstract boolean needsScrollBars();
    
    @Inject(method = "mouseScrolled", at = @At("HEAD"), cancellable = true)
    public void mouseScrolled(double amount, CallbackInfoReturnable<Boolean> ci) {
        if (!needsScrollBars() && selectedTabIndex == ItemGroup.INVENTORY.getIndex())
            if (ScreenHelper.isOverlayVisible() && ScreenHelper.getLastOverlay().getRectangle().contains(ClientHelper.getMouseLocation()) && ScreenHelper.getLastOverlay().mouseScrolled(amount)) {
                ci.setReturnValue(true);
                ci.cancel();
            }
    }
    
    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    public void keyPressed(int int_1, int int_2, int int_3, CallbackInfoReturnable<Boolean> ci) {
        if (selectedTabIndex == ItemGroup.INVENTORY.getIndex())
            if (ScreenHelper.getLastOverlay().keyPressed(int_1, int_2, int_3)) {
                ci.setReturnValue(true);
                ci.cancel();
            }
    }
    
    @Inject(method = "charTyped", at = @At("HEAD"), cancellable = true)
    public void charTyped(char char_1, int int_1, CallbackInfoReturnable<Boolean> ci) {
        if (!this.field_195377_F && selectedTabIndex == ItemGroup.INVENTORY.getIndex())
            if (ScreenHelper.isOverlayVisible() && ScreenHelper.getLastOverlay().charTyped(char_1, int_1)) {
                ci.setReturnValue(true);
                ci.cancel();
            }
    }
    
    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    public void mouseClicked(double i, double j, int k, CallbackInfoReturnable<Boolean> ci) {
        if (selectedTabIndex == ItemGroup.INVENTORY.getIndex())
            if (ScreenHelper.isOverlayVisible() && ScreenHelper.getLastOverlay().mouseClicked(i, j, k)) {
                ci.setReturnValue(true);
                ci.cancel();
            }
    }
    
}
