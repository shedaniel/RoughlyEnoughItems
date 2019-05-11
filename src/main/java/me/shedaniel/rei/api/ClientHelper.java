/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.api;

import me.shedaniel.rei.client.ClientHelperImpl;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.FabricKeyBinding;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Map;

public interface ClientHelper extends ClientModInitializer {
    /**
     * @return the api instance of {@link ClientHelperImpl}
     */
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
     * {@link me.shedaniel.rei.gui.PreRecipeViewingScreen} if not set
     * {@link me.shedaniel.rei.gui.RecipeViewingScreen} if set to default
     * {@link me.shedaniel.rei.gui.VillagerRecipeViewingScreen} if set to villager
     *
     * @param map the map of recipes
     */
    void openRecipeViewingScreen(Map<RecipeCategory, List<RecipeDisplay>> map);
    
    /**
     * Registers REI's keybinds using Fabric API.
     */
    void registerFabricKeyBinds();
    
    /**
     * Tries to cheat items using either packets or commands.
     *
     * @param stack the stack to cheat in
     * @return whether it failed
     */
    boolean tryCheatingStack(ItemStack stack);
    
    /**
     * Finds recipe for the item and opens the recipe screen.
     *
     * @param stack the stack to find recipe for
     * @return whether the stack has any recipes to show
     */
    boolean executeRecipeKeyBind(ItemStack stack);
    
    /**
     * Finds usage for the item and opens the recipe screen.
     *
     * @param stack the stack to find usage for
     * @return whether the stack has any usages to show
     */
    boolean executeUsageKeyBind(ItemStack stack);
    
    /**
     * Gets the mod from an item
     *
     * @param item
     * @return the mod name
     */
    String getModFromItem(Item item);
    
    /**
     * Tries to delete the player's cursor item
     *
     * @return whether it failed
     */
    void sendDeletePacket();
    
    /**
     * Gets the formatted mod from an item
     *
     * @param item
     * @return the mod name with blue and italic formatting
     */
    String getFormattedModFromItem(Item item);
    
    /**
     * Gets the formatted mod from an identifier
     *
     * @param identifier
     * @return the mod name with blue and italic formatting
     */
    String getFormattedModFromIdentifier(Identifier identifier);
    
    /**
     * Gets the mod from an identifier
     *
     * @param identifier
     * @return the mod name
     */
    String getModFromIdentifier(Identifier identifier);
    
    FabricKeyBinding getRecipeKeyBinding();
    
    FabricKeyBinding getUsageKeyBinding();
    
    FabricKeyBinding getHideKeyBinding();
    
    FabricKeyBinding getPreviousPageKeyBinding();
    
    FabricKeyBinding getNextPageKeyBinding();
    
    /**
     * Finds all recipes and open them in a recipe screen.
     *
     * @return whether there are any recipes to show
     */
    boolean executeViewAllRecipesKeyBind();
}
