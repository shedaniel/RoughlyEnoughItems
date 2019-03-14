package net.minecraft.recipe;

import com.google.common.collect.Lists;
import me.shedaniel.cloth.api.ReflectionUtils;
import net.minecraft.item.Item;
import net.minecraft.potion.Potion;

import java.util.List;

public class REIPotionRecipeUtils {
    
    public static Object getInputFromRecipe(BrewingRecipeRegistry.Recipe o) throws Throwable {
        return ReflectionUtils.getField(o, Object.class, 0).orElseThrow(ReflectionUtils.ReflectionException::new);
    }
    
    public static Object getOutputFromRecipe(Object o) throws Throwable {
        return ReflectionUtils.getField(o, Object.class, 2).orElseThrow(ReflectionUtils.ReflectionException::new);
    }
    
    public static Ingredient getIngredientFromRecipe(Object o) throws Throwable {
        return ReflectionUtils.getField(o, Ingredient.class, 1).orElseThrow(ReflectionUtils.ReflectionException::new);
    }
    
    public static List<BrewingRecipeRegistry.Recipe<Item>> getItemRecipes() {
        return ReflectionUtils.getStaticField(BrewingRecipeRegistry.class, List.class, 1).orElse(Lists.newArrayList());
    }
    
    public static List<BrewingRecipeRegistry.Recipe<Potion>> getPotionRecipes() {
        return ReflectionUtils.getStaticField(BrewingRecipeRegistry.class, List.class, 0).orElse(Lists.newArrayList());
    }
    
    public static List<Ingredient> getPotionTypes() {
        return ReflectionUtils.getStaticField(BrewingRecipeRegistry.class, List.class, 2).orElse(Lists.newArrayList());
    }
    
}
