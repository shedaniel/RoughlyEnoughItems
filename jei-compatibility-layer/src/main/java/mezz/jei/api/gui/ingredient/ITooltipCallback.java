package mezz.jei.api.gui.ingredient;

import net.minecraft.network.chat.Component;

import java.util.List;

/**
 * Used to add tooltips to ingredients drawn on a recipe.
 * Implement a tooltip callback and set it with {@link IGuiIngredientGroup#addTooltipCallback(ITooltipCallback)}.
 * <p>
 * Note that this works on anything that implements {@link IGuiIngredientGroup},
 * like {@link IGuiItemStackGroup} and {@link IGuiFluidStackGroup}.
 *
 * @deprecated Use {@link IRecipeSlotTooltipCallback} instead.
 */
@Deprecated(forRemoval = true, since = "9.3.0")
@FunctionalInterface
public interface ITooltipCallback<T> {
    /**
     * Change the tooltip for an ingredient.
     *
     * @deprecated Use {@link IRecipeSlotTooltipCallback#onTooltip(IRecipeSlotView, List)} instead.
     */
    @Deprecated(forRemoval = true, since = "9.3.0")
    void onTooltip(int ingredientIndex, boolean input, T ingredient, List<Component> tooltip);
}
