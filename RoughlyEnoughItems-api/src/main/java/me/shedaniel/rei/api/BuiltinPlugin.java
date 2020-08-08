package me.shedaniel.rei.api;

import me.shedaniel.rei.impl.Internals;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Collections;
import java.util.List;
import java.util.function.UnaryOperator;

public interface BuiltinPlugin {
    Identifier CRAFTING = new Identifier("minecraft", "plugins/crafting");
    Identifier SMELTING = new Identifier("minecraft", "plugins/smelting");
    Identifier SMOKING = new Identifier("minecraft", "plugins/smoking");
    Identifier BLASTING = new Identifier("minecraft", "plugins/blasting");
    Identifier CAMPFIRE = new Identifier("minecraft", "plugins/campfire");
    Identifier STONE_CUTTING = new Identifier("minecraft", "plugins/stone_cutting");
    Identifier STRIPPING = new Identifier("minecraft", "plugins/stripping");
    Identifier BREWING = new Identifier("minecraft", "plugins/brewing");
    Identifier PLUGIN = new Identifier("roughlyenoughitems", "default_plugin");
    Identifier COMPOSTING = new Identifier("minecraft", "plugins/composting");
    Identifier FUEL = new Identifier("minecraft", "plugins/fuel");
    Identifier SMITHING = new Identifier("minecraft", "plugins/smithing");
    Identifier BEACON = new Identifier("minecraft", "plugins/beacon");
    Identifier INFO = new Identifier("roughlyenoughitems", "plugins/information");
    
    static BuiltinPlugin getInstance() {
        return Internals.getBuiltinPlugin();
    }
    
    void registerBrewingRecipe(ItemStack input, Ingredient ingredient, ItemStack output);
    
    void registerInformation(List<EntryStack> entryStacks, Text name, UnaryOperator<List<Text>> textBuilder);
    
    default void registerInformation(EntryStack entryStack, Text name, UnaryOperator<List<Text>> textBuilder) {
        registerInformation(Collections.singletonList(entryStack), name, textBuilder);
    }
}
