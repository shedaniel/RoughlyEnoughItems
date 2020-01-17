/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.listeners;

import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import org.jetbrains.annotations.ApiStatus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@ApiStatus.Internal
@Mixin(AbstractInventoryScreen.class)
public interface AbstractInventoryScreenHooks {
    @Accessor("offsetGuiForEffects")
    boolean rei_doesOffsetGuiForEffects();
}
