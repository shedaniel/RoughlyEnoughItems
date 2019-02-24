package me.shedaniel.rei.utils;

import com.google.common.collect.Lists;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.potion.PotionBrewing;
import net.minecraftforge.registries.IRegistryDelegate;

import java.util.List;

public class PotionRecipeUtils {
    
    public static <T> IRegistryDelegate<T> getInputFromMixPredicate(Object o, Class<IRegistryDelegate> T) throws Throwable {
        return ReflectionUtils.getField(o, T, 0).orElseThrow(ReflectionUtils.ReflectionException::new);
    }
    
    public static <T> IRegistryDelegate<T> getOutputFromMixPredicate(Object o, Class<IRegistryDelegate> T) throws Throwable {
        return ReflectionUtils.getField(o, T, 2).orElseThrow(ReflectionUtils.ReflectionException::new);
    }
    
    public static Ingredient getReagentFromMixPredicate(Object o) throws Throwable {
        return ReflectionUtils.getField(o, Ingredient.class, 1).orElseThrow(ReflectionUtils.ReflectionException::new);
    }
    
    public static List getPotionItemConversions() {
        return ReflectionUtils.getStaticField(PotionBrewing.class, List.class, 1).orElse(Lists.newArrayList());
    }
    
    public static List getPotionTypeConversions() {
        return ReflectionUtils.getStaticField(PotionBrewing.class, List.class, 0).orElse(Lists.newArrayList());
    }
    
    public static List<Ingredient> getPotionItems() {
        return ReflectionUtils.getStaticField(PotionBrewing.class, List.class, 2).orElse(Lists.newArrayList());
    }
    
}
