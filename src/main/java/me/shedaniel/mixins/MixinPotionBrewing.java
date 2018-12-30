package me.shedaniel.mixins;

import me.shedaniel.listenerdefinitions.PotionCraftingAdder;
import net.minecraft.item.Item;
import net.minecraft.potion.PotionBrewing;
import net.minecraft.potion.PotionType;
import org.dimdev.riftloader.RiftLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PotionBrewing.class)
public class MixinPotionBrewing {
    
    @Inject(method = "addMix", at = @At("RETURN"))
    private static void addMix(PotionType inputPotion, Item reagent, PotionType outputPotion, CallbackInfo info) {
        RiftLoader.instance.getListeners(PotionCraftingAdder.class).forEach(potionCraftingAdder -> potionCraftingAdder.addPotionRecipe(inputPotion, reagent, outputPotion));
    }
    
}
