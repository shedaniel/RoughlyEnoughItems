package me.shedaniel.rei.mixin;

import me.shedaniel.rei.api.TabGetter;
import me.shedaniel.rei.client.ClientHelper;
import me.shedaniel.rei.client.ScreenHelper;
import me.shedaniel.rei.gui.ContainerScreenOverlay;
import me.shedaniel.rei.listeners.ContainerScreenHooks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.ContainerScreen;
import net.minecraft.client.gui.Screen;
import net.minecraft.client.gui.ingame.CreativePlayerInventoryScreen;
import net.minecraft.container.Slot;
import net.minecraft.item.ItemGroup;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ContainerScreen.class)
public class MixinContainerScreen extends Screen implements ContainerScreenHooks {
    
    @Shadow
    protected int left;
    @Shadow
    protected int top;
    @Shadow
    protected int width;
    @Shadow
    protected int height;
    @Shadow
    protected Slot focusedSlot;
    
    @Override
    public int rei_getContainerLeft() {
        return left;
    }
    
    @Override
    public int rei_getContainerTop() {
        return top;
    }
    
    @Override
    public int rei_getContainerWidth() {
        return width;
    }
    
    @Override
    public int rei_getContainerHeight() {
        return height;
    }
    
    @Inject(method = "onInitialized()V", at = @At("RETURN"))
    protected void onInitialized(CallbackInfo info) {
        if (MinecraftClient.getInstance().currentScreen instanceof CreativePlayerInventoryScreen) {
            TabGetter tabGetter = (TabGetter) MinecraftClient.getInstance().currentScreen;
            if (tabGetter.rei_getSelectedTab() != ItemGroup.INVENTORY.getIndex())
                return;
        }
        ScreenHelper.setLastContainerScreen((ContainerScreen) (Object) this);
        this.listeners.add(ScreenHelper.getLastOverlay(true));
    }
    
    @Inject(method = "draw(IIF)V", at = @At("RETURN"))
    public void draw(int int_1, int int_2, float float_1, CallbackInfo info) {
        if (MinecraftClient.getInstance().currentScreen instanceof CreativePlayerInventoryScreen) {
            TabGetter tabGetter = (TabGetter) MinecraftClient.getInstance().currentScreen;
            if (tabGetter.rei_getSelectedTab() != ItemGroup.INVENTORY.getIndex())
                return;
        }
        ScreenHelper.getLastOverlay().drawOverlay(int_1, int_2, float_1);
    }
    
    @Override
    public Slot rei_getHoveredSlot() {
        return focusedSlot;
    }
    
    @Override
    public boolean mouseScrolled(double double_1) {
        if (MinecraftClient.getInstance().currentScreen instanceof CreativePlayerInventoryScreen) {
            TabGetter tabGetter = (TabGetter) MinecraftClient.getInstance().currentScreen;
            if (tabGetter.rei_getSelectedTab() != ItemGroup.INVENTORY.getIndex())
                return super.mouseScrolled(double_1);
        }
        ContainerScreenOverlay overlay = ScreenHelper.getLastOverlay();
        if (ScreenHelper.isOverlayVisible() && overlay.getRectangle().contains(ClientHelper.getMouseLocation()))
            if (overlay.mouseScrolled(double_1))
                return true;
        return super.mouseScrolled(double_1);
    }
    
    @Inject(method = "keyPressed(III)Z", at = @At("HEAD"), cancellable = true)
    public void keyPressed(int int_1, int int_2, int int_3, CallbackInfoReturnable<Boolean> ci) {
        if (MinecraftClient.getInstance().currentScreen instanceof CreativePlayerInventoryScreen) {
            TabGetter tabGetter = (TabGetter) MinecraftClient.getInstance().currentScreen;
            if (tabGetter.rei_getSelectedTab() != ItemGroup.INVENTORY.getIndex())
                return;
        }
        if (ScreenHelper.getLastOverlay().keyPressed(int_1, int_2, int_3)) {
            ci.setReturnValue(true);
            ci.cancel();
        }
    }
    
}
