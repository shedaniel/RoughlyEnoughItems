/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020, 2021, 2022, 2023 shedaniel
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package me.shedaniel.rei.mixin.fabric;

import me.shedaniel.rei.api.client.config.ConfigObject;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.EffectsInInventory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(EffectsInInventory.class)
public abstract class MixinEffectsInInventory {
    @Shadow
    @Final
    private AbstractContainerScreen<?> screen;
    
    @Unique
    private boolean leftSideEffects() {
        return ConfigObject.getInstance().isLeftSideMobEffects();
    }

    @ModifyVariable(
            method = "renderEffects",
            at = @At("HEAD"),
            argsOnly = true,
            ordinal = 4 // 'k' is the 5th parameter (0-based: i=0, j=1, k=2, l=3, m=4?) adjust if needed
    )
    private int modifyKParam(int k) {
        if (!leftSideEffects()) return k;
        return this.screen.leftPos >= 120 ? this.screen.leftPos - 120 - 4 : this.screen.leftPos - 32 - 4;
    }

    @ModifyArg(
            method = "renderEffects",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/screens/inventory/EffectsInInventory;renderBackground(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/Component;IIZI)I"
            ),
            index = 6
    )
    private boolean modifyRenderBackgroundBl(boolean bl) {
        if (!leftSideEffects()) return bl;
        return this.screen.leftPos >= 120;
    }
}
