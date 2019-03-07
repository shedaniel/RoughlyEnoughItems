package me.shedaniel.rei.mixin;

import me.shedaniel.rei.RoughlyEnoughItemsCore;
import net.minecraft.client.gui.ContainerScreen;
import net.minecraft.client.gui.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.RecipeBookButtonWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Screen.class)
public class MixinScreen {
    
    @Inject(method = "addButton", at = @At("HEAD"), cancellable = true)
    protected void addButton(ButtonWidget buttonWidget, CallbackInfoReturnable info) {
        if (RoughlyEnoughItemsCore.getConfigManager().getConfig().disableRecipeBook && ((Screen) (Object) this) instanceof ContainerScreen && buttonWidget instanceof RecipeBookButtonWidget)
            info.cancel();
    }
    
}
