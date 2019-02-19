package me.shedaniel.rei.utils;

import net.minecraft.item.crafting.Ingredient;
import net.minecraftforge.registries.IRegistryDelegate;

public class PotionRecipeUtils {
    
    public static <T> IRegistryDelegate<T> getInputFromMixPredicate(Object o, Class<IRegistryDelegate> T) throws Throwable {
        return ReflectionUtils.getField(o, T, "input", "field_185198_a").orElseThrow(ReflectionException::new);
    }
    
    public static <T> IRegistryDelegate<T> getOutputFromMixPredicate(Object o, Class<IRegistryDelegate> T) throws Throwable {
        return ReflectionUtils.getField(o, T, "output", "field_185200_c").orElseThrow(ReflectionException::new);
    }
    
    public static Ingredient getReagentFromMixPredicate(Object o) throws Throwable {
        return ReflectionUtils.getField(o, Ingredient.class, "reagent", "field_185199_b").orElseThrow(ReflectionException::new);
    }
    
    public static class ReflectionException extends Exception {}
    
}
