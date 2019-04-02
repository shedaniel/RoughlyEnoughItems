package me.shedaniel.rei.mixin;

import me.shedaniel.cloth.api.ClientUtils;
import me.shedaniel.rei.client.ScreenHelper;
import me.shedaniel.rei.listeners.CreativePlayerInventoryScreenHooks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.ContainerScreen;
import net.minecraft.client.gui.ingame.CreativePlayerInventoryScreen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.item.ItemGroup;
import net.minecraft.resource.SynchronousResourceReloadListener;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class MixinGameRenderer implements AutoCloseable, SynchronousResourceReloadListener {
    
    @Shadow
    @Final
    private MinecraftClient client;
    
    // TODO: Move to Cloth next snapshot
    @Inject(method = "render(FJZ)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Screen;render(IIF)V", shift = At.Shift.AFTER, ordinal = 0))
    public void renderScreen(float float_1, long long_1, boolean boolean_1, CallbackInfo ci) {
        if (client.currentScreen instanceof ContainerScreen) {
            if (client.currentScreen instanceof CreativePlayerInventoryScreen) {
                CreativePlayerInventoryScreenHooks creativePlayerInventoryScreenHooks = (CreativePlayerInventoryScreenHooks) client.currentScreen;
                if (creativePlayerInventoryScreenHooks.rei_getSelectedTab() != ItemGroup.INVENTORY.getIndex())
                    return;
            }
            ScreenHelper.getLastOverlay().lateRender((int) ClientUtils.getInstance().getMouseX(), (int) ClientUtils.getInstance().getMouseY(), client.getLastFrameDuration());
        }
    }
}
