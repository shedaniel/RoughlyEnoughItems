package me.shedaniel.rei.mixin;

import me.shedaniel.rei.api.TabGetter;
import me.shedaniel.rei.client.ScreenHelper;
import me.shedaniel.rei.listeners.ContainerScreenHooks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.ContainerScreen;
import net.minecraft.client.gui.Screen;
import net.minecraft.client.gui.ingame.CreativePlayerInventoryScreen;
import net.minecraft.container.Slot;
import net.minecraft.item.ItemGroup;
import net.minecraft.text.TextComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
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
    
    protected MixinContainerScreen(TextComponent textComponent_1) {
        super(textComponent_1);
    }
    
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
    
    @Override
    public Slot rei_getHoveredSlot() {
        return focusedSlot;
    }
    
    @Inject(method = "keyPressed(III)Z", at = @At("HEAD"), cancellable = true, remap = false)
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
    
    // TODO: Make this use an event when Cloth mixin issues are fixed
    @Override
    public boolean charTyped(char char_1, int int_1) {
        if (!(MinecraftClient.getInstance().currentScreen instanceof CreativePlayerInventoryScreen)) {
            if (ScreenHelper.getLastOverlay().charTyped(char_1, int_1)) {
                return true;
            }
        }
        return super.charTyped(char_1, int_1);
    }
    
}
