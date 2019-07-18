package me.shedaniel.rei.mixin;

import me.shedaniel.rei.client.ScreenHelper;
import me.shedaniel.reiclothconfig2.api.MouseUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class MixinGameRenderer {
    
    @Shadow @Final private Minecraft mc;
    
    @Inject(method = "updateCameraAndRender(FJZ)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiScreen;render(IIF)V",
                     shift = At.Shift.AFTER, ordinal = 0))
    public void renderScreen(float float_1, long long_1, boolean boolean_1, CallbackInfo ci) {
        if (!ScreenHelper.isOverlayVisible())
            return;
        if (mc.currentScreen instanceof GuiContainer)
            ScreenHelper.getLastOverlay().lateRender((int) MouseUtils.getMouseX(), (int) MouseUtils.getMouseY(), this.mc.getTickLength());
    }
    
}
