package mezz.jei.api.ingredients.subtypes;

import net.minecraft.world.item.ItemStack;

import java.util.function.Function;

@FunctionalInterface
public interface ISubtypeInterpreter extends Function<ItemStack, String> {
    String NONE = "";
    
    /**
     * Get the data from an itemStack that is relevant to telling subtypes apart.
     * This should account for nbt, and anything else that's relevant.
     * Return {@link #NONE} if there is no data used for subtypes.
     */
    @Override
    String apply(ItemStack itemStack);
    
    /**
     * Get the data from an itemStack that is relevant to telling subtypes apart in the given context.
     * This should account for nbt, and anything else that's relevant.
     * Return {@link #NONE} if there is no data used for subtypes.
     *
     * @since JEI 7.3.0
     */
    default String apply(ItemStack itemStack, UidContext context) {
        return apply(itemStack);
    }
}
