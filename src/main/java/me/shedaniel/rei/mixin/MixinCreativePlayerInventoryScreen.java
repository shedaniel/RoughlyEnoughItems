package me.shedaniel.rei.mixin;

import me.shedaniel.rei.client.ScreenHelper;
import me.shedaniel.rei.listeners.CreativePlayerInventoryScreenHooks;
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
public abstract class MixinCreativePlayerInventoryScreen extends AbstractPlayerInventoryScreen<CreativePlayerInventoryScreen.CreativeContainer> implements CreativePlayerInventoryScreenHooks {
    
    @Shadow
    private static int selectedTab;
    @Shadow
    private boolean field_2888;
    
    public MixinCreativePlayerInventoryScreen(CreativePlayerInventoryScreen.CreativeContainer container_1, PlayerInventory playerInventory_1, TextComponent textComponent_1) {
        super(container_1, playerInventory_1, textComponent_1);
    }
    
    @Shadow
    protected abstract boolean doRenderScrollBar();
    
    @Override
    public int rei_getSelectedTab() {
        return selectedTab;
    }
    
    @Override
    public boolean rei_doRenderScrollBar() {
        return doRenderScrollBar();
    }
    
    @Override
    public boolean rei_getField2888() {
        return field_2888;
    }
    
    // Inject to fix pressing T to switch tab
    // TODO: Make into Cloth events
    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    public void keyPressed(int int_1, int int_2, int int_3, CallbackInfoReturnable<Boolean> ci) {
        if (selectedTab == ItemGroup.INVENTORY.getIndex())
            if (ScreenHelper.getLastOverlay().keyPressed(int_1, int_2, int_3)) {
                ci.setReturnValue(true);
                ci.cancel();
            }
    }
    
}
