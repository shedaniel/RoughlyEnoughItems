/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020 shedaniel
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package me.shedaniel.rei.api;

import me.shedaniel.rei.impl.ClientHelperImpl;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;
import java.util.Map;

public interface ClientHelper {
    /**
     * @return the api instance of {@link ClientHelperImpl}
     */
    @SuppressWarnings("deprecation")
    static ClientHelper getInstance() {
        return ClientHelperImpl.instance;
    }
    
    /**
     * Checks if cheating is enabled
     *
     * @return whether cheating is enabled
     */
    boolean isCheating();
    
    /**
     * Sets current cheating mode
     * Should save the config in {@link ConfigManager}.
     *
     * @param cheating the new cheating mode
     */
    void setCheating(boolean cheating);
    
    List<ItemStack> getInventoryItemsTypes();
    
    /**
     * Opens a recipe viewing screen:
     * Opens {@link me.shedaniel.rei.gui.PreRecipeViewingScreen} if not set
     * Opens {@link me.shedaniel.rei.gui.RecipeViewingScreen} if set to default
     * Opens {@link me.shedaniel.rei.gui.VillagerRecipeViewingScreen} if set to villager
     *
     * @param map the map of recipes
     */
    void openRecipeViewingScreen(Map<RecipeCategory<?>, List<RecipeDisplay>> map);
    
    /**
     * Registers REI's keybinds using Fabric API.
     */
    @ApiStatus.Internal
    void registerFabricKeyBinds();
    
    /**
     * Tries to cheat stack using either packets or commands.
     *
     * @param stack the stack to cheat in
     * @return whether it failed
     */
    boolean tryCheatingEntry(EntryStack stack);
    
    default boolean tryCheatingStack(ItemStack stack) {
        return tryCheatingEntry(EntryStack.create(stack));
    }
    
    /**
     * Finds recipe for the stack and opens the recipe screen.
     *
     * @param stack the stack to find recipe for
     * @return whether the stack has any recipes to show
     */
    boolean executeRecipeKeyBind(EntryStack stack);
    
    default boolean executeRecipeKeyBind(ItemStack stack) {
        return executeRecipeKeyBind(EntryStack.create(stack));
    }
    
    /**
     * Finds usage for the stack and opens the recipe screen.
     *
     * @param stack the stack to find usage for
     * @return whether the stack has any usages to show
     */
    boolean executeUsageKeyBind(EntryStack stack);
    
    default boolean executeUsageKeyBind(ItemStack stack) {
        return executeUsageKeyBind(EntryStack.create(stack));
    }
    
    /**
     * Gets the mod from an item
     *
     * @param item the item to find
     * @return the mod name
     */
    String getModFromItem(Item item);
    
    /**
     * Tries to delete the player's cursor item
     */
    void sendDeletePacket();
    
    /**
     * Gets the formatted mod from an item
     *
     * @param item the item to find
     * @return the mod name with blue and italic formatting
     */
    String getFormattedModFromItem(Item item);
    
    /**
     * Gets the formatted mod from an identifier
     *
     * @param identifier the identifier to find
     * @return the mod name with blue and italic formatting
     */
    String getFormattedModFromIdentifier(Identifier identifier);
    
    /**
     * Gets the mod from an identifier
     *
     * @param identifier the identifier to find
     * @return the mod name
     */
    default String getModFromIdentifier(Identifier identifier) {
        if (identifier == null)
            return "";
        return getModFromModId(identifier.getNamespace());
    }
    
    /**
     * Gets the mod from a modid
     *
     * @param modid the modid of the mod
     * @return the mod name
     */
    String getModFromModId(String modid);
    
    /**
     * Finds all recipes and open them in a recipe screen.
     *
     * @return whether there are any recipes to show
     */
    boolean executeViewAllRecipesKeyBind();
    
    boolean executeViewAllRecipesFromCategory(Identifier category);
    
    boolean executeViewAllRecipesFromCategories(List<Identifier> categories);
}
