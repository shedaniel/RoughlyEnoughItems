package mezz.jei.api.constants;

import me.shedaniel.rei.plugin.client.BuiltinClientPlugin;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICraftingCategoryExtension;
import mezz.jei.api.recipe.vanilla.IVanillaRecipeFactory;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.block.ComposterBlock;

import java.util.List;

/**
 * List of built-in recipe category UIDs, so that plugins with their own recipe handlers can use them.
 */
public final class VanillaRecipeCategoryUid {
    /**
     * The crafting recipe category.
     * <p>
     * Automatically includes all vanilla and Forge recipes.
     * <p>
     * To add a shaped recipe extension to this category, it must implement
     * {@link ICraftingCategoryExtension#getWidth()} and {@link ICraftingCategoryExtension#getHeight()}.
     */
    public static final ResourceLocation CRAFTING = BuiltinClientPlugin.CRAFTING.getIdentifier();
    
    /**
     * The stonecutting recipe category
     * <p>
     * Automatically includes every {@link StonecutterRecipe}
     */
    public static final ResourceLocation STONECUTTING = BuiltinClientPlugin.STONE_CUTTING.getIdentifier();
    
    /**
     * The furnace recipe category.
     * <p>
     * Automatically includes every {@link SmeltingRecipe}
     */
    public static final ResourceLocation FURNACE = BuiltinClientPlugin.SMELTING.getIdentifier();
    
    /**
     * The smoking recipe category.
     * <p>
     * Automatically includes every {@link SmokingRecipe}
     */
    public static final ResourceLocation SMOKING = BuiltinClientPlugin.SMOKING.getIdentifier();
    
    /**
     * The blasting recipe category.
     * <p>
     * Automatically includes every {@link BlastingRecipe}
     */
    public static final ResourceLocation BLASTING = BuiltinClientPlugin.BLASTING.getIdentifier();
    
    /**
     * The campfire furnace recipe category.
     * <p>
     * Automatically includes every {@link CampfireCookingRecipe}
     */
    public static final ResourceLocation CAMPFIRE = BuiltinClientPlugin.CAMPFIRE.getIdentifier();
    
    /**
     * The fuel recipe category.
     * <p>
     * Automatically includes everything that has a burn time.
     */
    public static final ResourceLocation FUEL = BuiltinClientPlugin.FUEL.getIdentifier();
    
    /**
     * The brewing recipe category.
     * <p>
     * Automatically tries to generate all potion variations from the basic ingredients.
     * <p>
     * JEI can only understand modded potion recipes that are built into vanilla or Forge.
     */
    public static final ResourceLocation BREWING = BuiltinClientPlugin.BREWING.getIdentifier();
    
    /**
     * The anvil recipe category.
     * <p>
     * This is a built-in category, you can create new recipes with {@link IVanillaRecipeFactory#createAnvilRecipe(ItemStack, List, List)}
     */
    public static final ResourceLocation ANVIL = BuiltinClientPlugin.ANVIL.getIdentifier();
    
    /**
     * The smithing recipe category.
     * <p>
     * Automatically includes every {@link UpgradeRecipe}.
     *
     * @since 7.3.1
     */
    public static final ResourceLocation SMITHING = BuiltinClientPlugin.SMITHING.getIdentifier();
    
    /**
     * The sompostable recipe category.
     * <p>
     * Automatically includes every item added to {@link ComposterBlock#COMPOSTABLES}.
     *
     * @since 8.1.0
     */
    public static final ResourceLocation COMPOSTABLE = BuiltinClientPlugin.COMPOSTING.getIdentifier();
    
    /**
     * The JEI info recipe category shows extra information about ingredients.
     * <p>
     * This is a built-in category, you can add new recipes with
     * {@link IRecipeRegistration#addIngredientInfo(Object, IIngredientType, net.minecraft.network.chat.Component...)} or
     * {@link IRecipeRegistration#addIngredientInfo(List, IIngredientType, net.minecraft.network.chat.Component...)}
     */
    public static final ResourceLocation INFORMATION = BuiltinClientPlugin.INFO.getIdentifier();
    
    private VanillaRecipeCategoryUid() {
        
    }
}
