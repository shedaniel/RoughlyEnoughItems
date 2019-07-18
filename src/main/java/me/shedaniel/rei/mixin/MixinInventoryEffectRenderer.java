package me.shedaniel.rei.mixin;

import me.shedaniel.rei.api.AbstractInventoryScreenHooks;
import net.minecraft.client.renderer.InventoryEffectRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(InventoryEffectRenderer.class)
public class MixinInventoryEffectRenderer implements AbstractInventoryScreenHooks {
    @Shadow protected boolean hasActivePotionEffects;
    
    @Override
    public boolean rei_doesOffsetGuiForEffects() {
        return hasActivePotionEffects;
    }
}
