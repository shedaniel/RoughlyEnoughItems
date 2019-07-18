package me.shedaniel.rei.mixin;

import me.shedaniel.rei.client.ScreenHelper;
import me.shedaniel.rei.api.RecipeBookGuiHooks;
import net.minecraft.client.KeyboardListener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.recipebook.GuiRecipeBook;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardListener.class)
public class MixinKeyboardListener {
    
    @Shadow @Final private Minecraft mc;
    
    @Shadow private boolean repeatEventsEnabled;
    
    @Inject(method = "onCharEvent", at = @At(value = "INVOKE",
                                             target = "Lnet/minecraft/client/gui/GuiScreen;runOrMakeCrashReport(Ljava/lang/Runnable;Ljava/lang/String;Ljava/lang/String;)V",
                                             ordinal = 0), cancellable = true)
    public void onCharFirst(long long_1, int int_1, int int_2, CallbackInfo info) {
        if (!info.isCancelled()) {
            GuiScreen screen = mc.currentScreen;
            if (screen instanceof GuiContainer)
                if (ScreenHelper.getLastOverlay().charTyped((char) int_1, int_2))
                    info.cancel();
        }
    }
    
    @Inject(method = "onCharEvent", at = @At(value = "INVOKE",
                                             target = "Lnet/minecraft/client/gui/GuiScreen;runOrMakeCrashReport(Ljava/lang/Runnable;Ljava/lang/String;Ljava/lang/String;)V",
                                             ordinal = 1), cancellable = true)
    public void onCharSecond(long long_1, int int_1, int int_2, CallbackInfo info) {
        if (!info.isCancelled()) {
            GuiScreen screen = mc.currentScreen;
            if (screen instanceof GuiContainer)
                if (ScreenHelper.getLastOverlay().charTyped((char) int_1, int_2))
                    info.cancel();
        }
    }
    
    @Inject(method = "onKeyEvent", at = @At(value = "INVOKE",
                                            target = "Lnet/minecraft/client/gui/GuiScreen;runOrMakeCrashReport(Ljava/lang/Runnable;Ljava/lang/String;Ljava/lang/String;)V",
                                            ordinal = 0), cancellable = true)
    public void onKey(long long_1, int int_1, int int_2, int int_3, int int_4, CallbackInfo info) {
        if (!info.isCancelled()) {
            if (int_3 != 1 && (int_3 != 2 || !this.repeatEventsEnabled)) {
            
            } else {
                if (rei_runKey(mc.currentScreen, int_1, int_2, int_4))
                    info.cancel();
            }
        }
    }
    
    public boolean rei_runKey(GuiScreen screen, int i, int j, int k) {
        if (screen.getFocused() != null && screen.getFocused() instanceof GuiTextField || (screen.getFocused() instanceof GuiRecipeBook && ((RecipeBookGuiHooks) screen.getFocused()).rei_getSearchField() != null && ((RecipeBookGuiHooks) screen.getFocused()).rei_getSearchField().isFocused()))
            return false;
        if (screen instanceof GuiContainer)
            if (ScreenHelper.getLastOverlay().keyPressed(i, j, k))
                return true;
        return false;
    }
    
}
