package me.shedaniel.rei.mixin;

import com.google.common.collect.Lists;
import me.shedaniel.rei.plugin.BrewingRecipe;
import me.shedaniel.rei.plugin.DefaultBrewingDisplay;
import me.shedaniel.rei.plugin.DefaultPlugin;
import net.minecraft.item.Item;
import net.minecraft.item.ItemProvider;
import net.minecraft.item.PotionItem;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.recipe.BrewingRecipeRegistry;
import net.minecraft.recipe.Ingredient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;
import java.util.List;

@Mixin(BrewingRecipeRegistry.class)
public class MixinBrewingRecipeRegistry {
    
    private static final List<BrewingRecipe> SELF_ITEM_RECIPES = Lists.newArrayList();
    private static final List<Potion> REGISTERED_POTION_TYPES = Lists.newArrayList();
    private static final List<Ingredient> SELF_POTION_TYPES = Lists.newArrayList();
    
    @Inject(method = "method_8080", at = @At("RETURN"))
    private static void method_8080(Item item_1, CallbackInfo ci) {
        if (item_1 instanceof PotionItem)
            SELF_POTION_TYPES.add(Ingredient.ofItems(new ItemProvider[]{item_1}));
    }
    
    @Inject(method = "method_8071", at = @At("RETURN"))
    private static void method_8071(Item item_1, Item item_2, Item item_3, CallbackInfo ci) {
        if (item_1 instanceof PotionItem && item_3 instanceof PotionItem)
            SELF_ITEM_RECIPES.add(new BrewingRecipe(item_1, Ingredient.ofItems(new ItemProvider[]{item_2}), item_3));
    }
    
    @Inject(method = "registerPotionRecipe", at = @At("RETURN"))
    private static void registerPotionRecipe(Potion potion_1, Item item_1, Potion potion_2, CallbackInfo ci) {
        if (!REGISTERED_POTION_TYPES.contains(potion_1))
            registerPotionType(potion_1);
        if (!REGISTERED_POTION_TYPES.contains(potion_2))
            registerPotionType(potion_2);
        SELF_POTION_TYPES.stream().map(Ingredient::getStackArray).forEach(itemStacks -> Arrays.stream(itemStacks).forEach(stack -> {
            DefaultPlugin.registerBrewingDisplay(new DefaultBrewingDisplay(PotionUtil.setPotion(stack.copy(), potion_1), Ingredient.ofItems(new ItemProvider[]{item_1}), PotionUtil.setPotion(stack.copy(), potion_2)));
        }));
    }
    
    private static void registerPotionType(Potion potion) {
        REGISTERED_POTION_TYPES.add(potion);
        SELF_ITEM_RECIPES.forEach(recipe -> {
            DefaultPlugin.registerBrewingDisplay(new DefaultBrewingDisplay(PotionUtil.setPotion(recipe.input.getDefaultStack(), potion), recipe.ingredient, PotionUtil.setPotion(recipe.output.getDefaultStack(), potion)));
        });
    }
    
}
