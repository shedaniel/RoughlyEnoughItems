/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.mixin;

import me.shedaniel.rei.api.ItemStackHook;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public class MixinItemStack implements ItemStackHook {
    private boolean rei_dontRenderOverlay = false;

    @Override
    public void rei_setRenderEnchantmentGlint(boolean b) {
        rei_dontRenderOverlay = !b;
    }

    @Inject(method = "hasEnchantmentGlint", at = @At("HEAD"), cancellable = true)
    public void hasEnchantmentGlint(CallbackInfoReturnable<Boolean> callbackInfo) {
        if (rei_dontRenderOverlay)
            callbackInfo.setReturnValue(false);
    }

}
