package me.shedaniel.rei.mixin;

import me.shedaniel.rei.api.GuiEventHandlerHooks;
import me.shedaniel.rei.client.ScreenHelper;
import me.shedaniel.reiclothconfig2.api.MouseUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHelper;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHelper.class)
public class MixinMouseHelper {
    
    @Shadow @Final private Minecraft minecraft;
    
    @Inject(method = "mouseButtonCallback", at = @At(value = "INVOKE",
                                                     target = "Lnet/minecraft/client/gui/GuiScreen;runOrMakeCrashReport(Ljava/lang/Runnable;Ljava/lang/String;Ljava/lang/String;)V",
                                                     ordinal = 0), cancellable = true)
    public void mouseClick(long long_1, int int_1, int int_2, int int_3, CallbackInfo info) {
        double x = MouseUtils.getMouseX(), y = MouseUtils.getMouseY();
        GuiScreen screen = minecraft.currentScreen;
        if (!info.isCancelled()) {
            if (screen instanceof GuiContainerCreative)
                if (ScreenHelper.isOverlayVisible() && ScreenHelper.getLastOverlay().mouseClicked(x, y, int_1)) {
                    ((GuiEventHandlerHooks) screen).rei_setFocused(ScreenHelper.getLastOverlay());
                    if (int_1 == 0)
                        ((GuiEventHandlerHooks) screen).rei_setDragging(true);
                    info.cancel();
                }
        }
    }
    
    @Inject(method = "scrollCallback",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiScreen;mouseScrolled(D)Z", ordinal = 0,
                     shift = At.Shift.BEFORE), cancellable = true)
    public void mouseScrolled(long handle, double xoffset, double yoffset, CallbackInfo info) {
        double d0 = yoffset * this.minecraft.gameSettings.mouseWheelSensitivity;
        GuiScreen screen = minecraft.currentScreen;
        if (!info.isCancelled()) {
            if (screen instanceof GuiContainer)
                if (ScreenHelper.isOverlayVisible() && ScreenHelper.getLastOverlay().isInside(MouseUtils.getMouseLocation()) && ScreenHelper.getLastOverlay().mouseScrolled(d0))
                    info.cancel();
        }
    }
    
}
