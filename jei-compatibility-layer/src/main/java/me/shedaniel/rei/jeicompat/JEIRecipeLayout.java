package me.shedaniel.rei.jeicompat;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ingredient.IGuiFluidStackGroup;
import mezz.jei.api.gui.ingredient.IGuiIngredientGroup;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.category.IRecipeCategory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

import static me.shedaniel.rei.jeicompat.JEIPluginDetector.TODO;

public class JEIRecipeLayout<T> implements IRecipeLayout {
    private final JEIWrappedCategory<T> category;
    private final JEIWrappedDisplay<T> display;
    private final Map<IIngredientType<?>, IGuiIngredientGroup<?>> groups = new HashMap<>();
    
    public JEIRecipeLayout(JEIWrappedCategory<T> category, JEIWrappedDisplay<T> display) {
        this.category = category;
        this.display = display;
    }
    
    @Override
    @NotNull
    public IGuiItemStackGroup getItemStacks() {
        return (IGuiItemStackGroup) getIngredientsGroup(VanillaTypes.ITEM);
    }
    
    @Override
    @NotNull
    public IGuiFluidStackGroup getFluidStacks() {
        return (IGuiFluidStackGroup) getIngredientsGroup(VanillaTypes.FLUID);
    }
    
    @Override
    @NotNull
    public <T> IGuiIngredientGroup<T> getIngredientsGroup(@NotNull IIngredientType<T> ingredientType) {
        return (IGuiIngredientGroup<T>) groups.computeIfAbsent(ingredientType, JEIGuiIngredientGroup::new);
    }
    
    @Override
    public @Nullable IFocus<?> getFocus() {
        throw TODO();
    }
    
    @Override
    public @Nullable <V> IFocus<V> getFocus(@NotNull IIngredientType<V> ingredientType) {
        throw TODO();
    }
    
    @Override
    @NotNull
    public IRecipeCategory<?> getRecipeCategory() {
        return category.getBackingCategory();
    }
    
    @Override
    public void moveRecipeTransferButton(int posX, int posY) {
        throw TODO();
    }
    
    @Override
    public void setShapeless() {
        throw TODO();
    }
}
