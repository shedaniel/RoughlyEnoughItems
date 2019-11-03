/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.plugin.brewing;

import com.google.common.collect.Lists;
import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.api.RecipeDisplay;
import me.shedaniel.rei.plugin.DefaultPlugin;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PotionItem;
import net.minecraft.recipe.Ingredient;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DefaultBrewingDisplay implements RecipeDisplay {
    
    private EntryStack input, output;
    private List<EntryStack> reactant;
    
    public DefaultBrewingDisplay(ItemStack input, Ingredient reactant, ItemStack output) {
        this.input = EntryStack.create(input).setting(EntryStack.Settings.TOOLTIP_APPEND_EXTRA, stack -> Collections.singletonList(Formatting.YELLOW.toString() + I18n.translate("category.rei.brewing.input")));
        if (this.input.getItem() instanceof PotionItem)
            this.input = this.input.setting(EntryStack.Settings.CHECK_TAGS, EntryStack.Settings.TRUE);
        this.reactant = new ArrayList<>();
        for (ItemStack stack : reactant.getStackArray()) {
            EntryStack entryStack = EntryStack.create(stack);
            if (stack.getItem() instanceof PotionItem)
                entryStack.setting(EntryStack.Settings.CHECK_TAGS, EntryStack.Settings.TRUE);
            entryStack.setting(EntryStack.Settings.TOOLTIP_APPEND_EXTRA, s -> Collections.singletonList(Formatting.YELLOW.toString() + I18n.translate("category.rei.brewing.reactant")));
            this.reactant.add(entryStack);
        }
        this.output = EntryStack.create(output).setting(EntryStack.Settings.TOOLTIP_APPEND_EXTRA, stack -> Collections.singletonList(Formatting.YELLOW.toString() + I18n.translate("category.rei.brewing.result")));
        if (this.output.getItem() instanceof PotionItem)
            this.output = this.output.setting(EntryStack.Settings.CHECK_TAGS, EntryStack.Settings.TRUE);
    }
    
    @Override
    public List<List<EntryStack>> getInputEntries() {
        return Lists.newArrayList(Collections.singletonList(input), reactant);
    }
    
    @Override
    public List<EntryStack> getOutputEntries() {
        return Collections.singletonList(output);
    }
    
    @Override
    public Identifier getRecipeCategory() {
        return DefaultPlugin.BREWING;
    }
    
    public List<EntryStack> getOutput(int slot) {
        List<EntryStack> stack = new ArrayList<>();
        for (int i = 0; i < slot * 2; i++)
            stack.add(EntryStack.empty());
        for (int i = 0; i < 6 - slot * 2; i++)
            stack.addAll(getOutputEntries());
        return stack;
    }
    
    @Override
    public List<List<EntryStack>> getRequiredEntries() {
        return getInputEntries();
    }
}
