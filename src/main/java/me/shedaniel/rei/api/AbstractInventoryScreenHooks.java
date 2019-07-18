/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.api;

import net.minecraft.client.renderer.InventoryEffectRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

public interface AbstractInventoryScreenHooks {
    
    boolean rei_doesOffsetGuiForEffects();
    
}
