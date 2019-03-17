package me.shedaniel.rei.mixin;

import me.shedaniel.rei.RoughlyEnoughItemsCore;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiButtonImage;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GuiScreen.class)
public class MixinScreen {
    
    @Inject(method = "addButton", at = @At("HEAD"), cancellable = true)
    protected void addButton(GuiButton buttonWidget, CallbackInfoReturnable info) {
        if (RoughlyEnoughItemsCore.getConfigManager().getConfig().disableRecipeBook && ((GuiScreen) (Object) this) instanceof GuiContainer && buttonWidget instanceof GuiButtonImage && buttonWidget.id == 10)
            info.cancel();
    }
    
}
