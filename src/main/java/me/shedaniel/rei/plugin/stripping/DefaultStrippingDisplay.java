/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.plugin.stripping;

import me.shedaniel.rei.api.RecipeDisplay;
import me.shedaniel.rei.plugin.DefaultPlugin;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

import java.util.Collections;
import java.util.List;

public class DefaultStrippingDisplay implements RecipeDisplay {

    private ItemStack in, out;

    public DefaultStrippingDisplay(ItemStack in, ItemStack out) {
        this.in = in;
        this.out = out;
    }

    public final ItemStack getIn() {
        return in;
    }

    public final ItemStack getOut() {
        return out;
    }

    @Override
    public List<List<ItemStack>> getInput() {
        return Collections.singletonList(Collections.singletonList(in));
    }

    @Override
    public List<ItemStack> getOutput() {
        return Collections.singletonList(out);
    }

    @Override
    public Identifier getRecipeCategory() {
        return DefaultPlugin.STRIPPING;
    }

    @Override
    public List<List<ItemStack>> getRequiredItems() {
        return getInput();
    }

}
