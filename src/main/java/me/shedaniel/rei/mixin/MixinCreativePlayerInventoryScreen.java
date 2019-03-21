package me.shedaniel.rei.mixin;

import me.shedaniel.cloth.api.ClientUtils;
import me.shedaniel.rei.client.ScreenHelper;
import net.minecraft.client.gui.ingame.AbstractPlayerInventoryScreen;
import net.minecraft.client.gui.ingame.CreativePlayerInventoryScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemGroup;
import net.minecraft.text.TextComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CreativePlayerInventoryScreen.class)
public abstract class MixinCreativePlayerInventoryScreen extends AbstractPlayerInventoryScreen<CreativePlayerInventoryScreen.CreativeContainer> {
    
    @Shadow
    private static int selectedTab;
    @Shadow
    private boolean field_2888;
    
    public MixinCreativePlayerInventoryScreen(CreativePlayerInventoryScreen.CreativeContainer container_1, PlayerInventory playerInventory_1, TextComponent textComponent_1) {
        super(container_1, playerInventory_1, textComponent_1);
    }
    
    @Shadow
    protected abstract boolean doRenderScrollBar();
    
    @Inject(method = "mouseScrolled", at = @At("HEAD"), cancellable = true, remap = false)
    public void mouseScrolled(double i, double j, double amount, CallbackInfoReturnable<Boolean> ci) {
        if (!doRenderScrollBar() && selectedTab == ItemGroup.INVENTORY.getIndex())
            if (ScreenHelper.isOverlayVisible() && ScreenHelper.getLastOverlay().getRectangle().contains(ClientUtils.getMouseLocation()) && ScreenHelper.getLastOverlay().mouseScrolled(i, j, amount)) {
                ci.setReturnValue(true);
                ci.cancel();
            }
    }
    
    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true, remap = false)
    public void keyPressed(int int_1, int int_2, int int_3, CallbackInfoReturnable<Boolean> ci) {
        if (selectedTab == ItemGroup.INVENTORY.getIndex())
            if (ScreenHelper.getLastOverlay().keyPressed(int_1, int_2, int_3)) {
                ci.setReturnValue(true);
                ci.cancel();
            }
    }
    
    @Inject(method = "charTyped", at = @At("HEAD"), cancellable = true, remap = false)
    public void charTyped(char char_1, int int_1, CallbackInfoReturnable<Boolean> ci) {
        if (!this.field_2888 && selectedTab == ItemGroup.INVENTORY.getIndex())
            if (ScreenHelper.isOverlayVisible() && ScreenHelper.getLastOverlay().charTyped(char_1, int_1)) {
                ci.setReturnValue(true);
                ci.cancel();
            }
    }
    
    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true, remap = false)
    public void mouseClicked(double i, double j, int k, CallbackInfoReturnable<Boolean> ci) {
        if (selectedTab == ItemGroup.INVENTORY.getIndex())
            if (ScreenHelper.isOverlayVisible() && ScreenHelper.getLastOverlay().mouseClicked(i, j, k)) {
                ci.setReturnValue(true);
                ci.cancel();
            }
    }
    
}
