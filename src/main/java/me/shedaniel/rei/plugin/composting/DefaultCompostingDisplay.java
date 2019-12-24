/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.plugin.composting;

import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.api.RecipeDisplay;
import me.shedaniel.rei.plugin.DefaultPlugin;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

import java.util.*;
import java.util.stream.Collectors;

public class DefaultCompostingDisplay implements RecipeDisplay {

    private List<EntryStack> order, allItems;
    private Map<ItemConvertible, Float> inputMap;
    private List<EntryStack> output;
    private int page;

    public DefaultCompostingDisplay(int page, List<ItemConvertible> order, Map<ItemConvertible, Float> inputMap, List<ItemConvertible> allItems, ItemStack[] output) {
        this.page = page;
        this.order = order.stream().map(EntryStack::create).collect(Collectors.toList());
        this.inputMap = inputMap;
        this.output = Arrays.asList(output).stream().map(EntryStack::create).collect(Collectors.toList());
        this.allItems = allItems.stream().map(EntryStack::create).collect(Collectors.toList());
    }

    public int getPage() {
        return page;
    }

    @Override
    public List<List<EntryStack>> getInputEntries() {
        List<List<EntryStack>> lists = new ArrayList<>();
        for (EntryStack allItem : allItems) {
            lists.add(Collections.singletonList(allItem));
        }
        return lists;
    }

    public Map<ItemConvertible, Float> getInputMap() {
        return inputMap;
    }

    @Override
    public List<EntryStack> getOutputEntries() {
        return output;
    }

    @Override
    public Identifier getRecipeCategory() {
        return DefaultPlugin.COMPOSTING;
    }

    @Override
    public List<List<EntryStack>> getRequiredEntries() {
        return Collections.singletonList(allItems);
    }

    public List<EntryStack> getItemsByOrder() {
        return order;
    }

}
