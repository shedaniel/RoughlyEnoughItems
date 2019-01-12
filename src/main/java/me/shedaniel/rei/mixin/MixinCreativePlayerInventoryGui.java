package me.shedaniel.rei.mixin;

import me.shedaniel.rei.listeners.IMixinTabGetter;
import net.minecraft.client.gui.ingame.AbstractPlayerInventoryGui;
import net.minecraft.client.gui.ingame.CreativePlayerInventoryGui;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemGroup;
import net.minecraft.text.TextComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CreativePlayerInventoryGui.class)
public abstract class MixinCreativePlayerInventoryGui extends AbstractPlayerInventoryGui<CreativePlayerInventoryGui.CreativeContainer> implements IMixinTabGetter {
    
    @Shadow
    private static int selectedTab;
    @Shadow
    private boolean field_2888;
    
    public MixinCreativePlayerInventoryGui(CreativePlayerInventoryGui.CreativeContainer container_1, PlayerInventory playerInventory_1, TextComponent textComponent_1) {
        super(container_1, playerInventory_1, textComponent_1);
    }
    
    @Shadow
    protected abstract boolean doRenderScrollBar();
    
    @Override
    public int getSelectedTab() {
        return selectedTab;
    }
    
    @Inject(method = "mouseScrolled", at = @At("HEAD"), cancellable = true)
    public void mouseScrolled(double amount, CallbackInfoReturnable<Boolean> ci) {
        if (!doRenderScrollBar())
            if (super.mouseScrolled(amount)) {
                ci.setReturnValue(true);
                ci.cancel();
            }
    }
    
    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    public void keyPressed(int int_1, int int_2, int int_3, CallbackInfoReturnable<Boolean> ci) {
        if (selectedTab == ItemGroup.INVENTORY.getId())
            if (super.keyPressed(int_1, int_2, int_3)) {
                ci.setReturnValue(true);
                ci.cancel();
            }
    }
    
    @Inject(method = "charTyped", at = @At("HEAD"), cancellable = true)
    public void charTyped(char char_1, int int_1, CallbackInfoReturnable<Boolean> ci) {
        if (!this.field_2888 && selectedTab == ItemGroup.INVENTORY.getId())
            if (super.charTyped(char_1, int_1)) {
                ci.setReturnValue(true);
                ci.cancel();
            }
    }
    
}
