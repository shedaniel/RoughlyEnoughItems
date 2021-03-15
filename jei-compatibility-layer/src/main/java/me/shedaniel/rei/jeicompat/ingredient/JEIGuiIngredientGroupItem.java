package me.shedaniel.rei.jeicompat.ingredient;

import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.ingredients.IIngredientType;
import net.minecraft.world.item.ItemStack;

public class JEIGuiIngredientGroupItem extends JEIGuiIngredientGroup<ItemStack> implements IGuiItemStackGroup {
    public JEIGuiIngredientGroupItem(IIngredientType<ItemStack> type) {
        super(type);
    }
}
