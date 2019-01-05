package me.shedaniel.mixins;

import me.shedaniel.Core;
import me.shedaniel.listenerdefinitions.PotionCraftingAdder;
import net.minecraft.item.Item;
import net.minecraft.potion.Potion;
import net.minecraft.recipe.BrewingRecipeRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BrewingRecipeRegistry.class)
public class MixinBrewingRecipeRegistry {
    
    @Inject(method = "registerPotionRecipe", at = @At("HEAD"))
    private static void registerPotionRecipe(Potion potion_1, Item item_1, Potion potion_2, CallbackInfo info) {
        Core.getListeners(PotionCraftingAdder.class).forEach(potionCraftingAdder -> potionCraftingAdder.addPotionRecipe(potion_1, item_1, potion_2));
    }
    
}
