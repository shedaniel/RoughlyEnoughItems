package mezz.jei.api.constants;

import lombok.experimental.ExtensionMethod;
import me.shedaniel.rei.jeicompat.JEIPluginDetector;
import me.shedaniel.rei.plugin.client.BuiltinClientPlugin;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.vanilla.*;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;

/**
 * List of all the built-in {@link RecipeType}s that are added by JEI.
 *
 * @since 9.5.0
 */
@ExtensionMethod(JEIPluginDetector.class)
public final class RecipeTypes {
    /**
     * The crafting recipe type.
     * <p>
     * Automatically includes all recipes in the {@link net.minecraft.world.item.crafting.RecipeManager}.
     *
     * @since 9.5.0
     */
    public static final RecipeType<CraftingRecipe> CRAFTING =
            BuiltinClientPlugin.CRAFTING.asRecipeType(CraftingRecipe.class);
    
    /**
     * The stonecutting recipe type.
     * <p>
     * Automatically includes every {@link StonecutterRecipe}.
     *
     * @since 9.5.0
     */
    public static final RecipeType<StonecutterRecipe> STONECUTTING =
            BuiltinClientPlugin.STONE_CUTTING.asRecipeType(StonecutterRecipe.class);
    
    /**
     * The smelting recipe type.
     * <p>
     * Automatically includes every {@link SmeltingRecipe}.
     *
     * @since 9.5.0
     */
    public static final RecipeType<SmeltingRecipe> SMELTING =
            BuiltinClientPlugin.SMELTING.asRecipeType(SmeltingRecipe.class);
    
    /**
     * The smoking recipe type.
     * <p>
     * Automatically includes every {@link SmokingRecipe}.
     *
     * @since 9.5.0
     */
    public static final RecipeType<SmokingRecipe> SMOKING =
            BuiltinClientPlugin.SMOKING.asRecipeType(SmokingRecipe.class);
    
    /**
     * The blasting recipe type.
     * <p>
     * Automatically includes every {@link BlastingRecipe}.
     *
     * @since 9.5.0
     */
    public static final RecipeType<BlastingRecipe> BLASTING =
            BuiltinClientPlugin.BLASTING.asRecipeType(BlastingRecipe.class);
    
    /**
     * The campfire cooking recipe type.
     * <p>
     * Automatically includes every {@link CampfireCookingRecipe}.
     *
     * @since 9.5.0
     */
    public static final RecipeType<CampfireCookingRecipe> CAMPFIRE_COOKING =
            BuiltinClientPlugin.CAMPFIRE.asRecipeType(CampfireCookingRecipe.class);
    
    /**
     * The fueling recipe type.
     * <p>
     * JEI automatically creates a fuel recipe for anything that has a burn time.
     *
     * @see Item#getBurnTime
     * @since 9.5.0
     */
    public static final RecipeType<IJeiFuelingRecipe> FUELING =
            BuiltinClientPlugin.FUEL.asRecipeType(IJeiFuelingRecipe.class);
    
    /**
     * The brewing recipe type.
     * <p>
     * JEI automatically tries to generate all potion variations from the basic ingredients,
     * and also automatically adds modded potions from {@link BrewingRecipeRegistry#getRecipes()}.
     *
     * @see IVanillaRecipeFactory#createBrewingRecipe to create new brewing recipes in JEI.
     * @since 9.5.0
     */
    public static final RecipeType<IJeiBrewingRecipe> BREWING =
            BuiltinClientPlugin.BREWING.asRecipeType(IJeiBrewingRecipe.class);
    
    /**
     * The anvil recipe type.
     *
     * @see IVanillaRecipeFactory#createAnvilRecipe to create new anvil recipes in JEI.
     * @since 9.5.0
     */
    public static final RecipeType<IJeiAnvilRecipe> ANVIL =
            BuiltinClientPlugin.ANVIL.asRecipeType(IJeiAnvilRecipe.class);
    
    /**
     * The smithing recipe type.
     * Automatically includes every {@link UpgradeRecipe}.
     *
     * @since 9.5.0
     */
    public static final RecipeType<UpgradeRecipe> SMITHING =
            BuiltinClientPlugin.SMELTING.asRecipeType(UpgradeRecipe.class);
    
    /**
     * The composting recipe type.
     * Automatically includes every item added to {@link ComposterBlock#COMPOSTABLES}.
     *
     * @since 9.5.0
     */
    public static final RecipeType<IJeiCompostingRecipe> COMPOSTING =
            BuiltinClientPlugin.COMPOSTING.asRecipeType(IJeiCompostingRecipe.class);
    
    /**
     * The JEI info recipe type.
     *
     * @see IRecipeRegistration#addIngredientInfo to create this type of recipe.
     * @since 9.5.0
     */
    public static final RecipeType<IJeiIngredientInfoRecipe> INFORMATION =
            BuiltinClientPlugin.INFO.asRecipeType(IJeiIngredientInfoRecipe.class);
    
    private RecipeTypes() {}
}