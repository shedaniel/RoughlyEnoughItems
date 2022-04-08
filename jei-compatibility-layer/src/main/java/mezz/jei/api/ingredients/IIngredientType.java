package mezz.jei.api.ingredients;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.registration.IModIngredientRegistration;

import java.util.Collection;

/**
 * A type of ingredient (i.e. ItemStack, FluidStack, etc) handled by JEI.
 * Register new types with {@link IModIngredientRegistration#register(IIngredientType, Collection, IIngredientHelper, IIngredientRenderer)}
 *
 * @see VanillaTypes for the built-in vanilla types {@link VanillaTypes#ITEM} and {@link VanillaTypes#FLUID}
 */
@FunctionalInterface
public interface IIngredientType<T> {
    /**
     * @return The class of the ingredient for this type.
     */
    Class<? extends T> getIngredientClass();
    
    default <R> IIngredientType<R> cast() {
        return (IIngredientType<R>) this;
    }
}
