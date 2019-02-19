package me.shedaniel.rei.plugin;

import com.google.common.collect.Lists;
import me.shedaniel.rei.api.*;
import me.shedaniel.rei.client.ConfigHelper;
import me.shedaniel.rei.client.RecipeHelper;
import me.shedaniel.rei.utils.PotionRecipeUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiCrafting;
import net.minecraft.client.gui.inventory.GuiFurnace;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.*;
import net.minecraft.potion.PotionBrewing;
import net.minecraft.potion.PotionType;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.util.registry.IRegistry;
import net.minecraftforge.registries.IRegistryDelegate;

import java.util.*;

@IREIPlugin(identifier = "roughlyenoughitems:default_plugin")
public class DefaultPlugin implements IRecipePlugin {
    
    static final Identifier CRAFTING = new Identifier("roughlyenoughitems", "plugins/crafting");
    static final Identifier SMELTING = new Identifier("roughlyenoughitems", "plugins/smelting");
    static final Identifier BREWING = new Identifier("roughlyenoughitems", "plugins/brewing");
    
    @Override
    public void onFirstLoad(IPluginDisabler pluginDisabler) {
        if (!ConfigHelper.getInstance().isLoadingDefaultPlugin()) {
            pluginDisabler.disablePluginFunction(new Identifier("roughlyenoughitems", "default_plugin"), PluginFunction.REGISTER_ITEMS);
            pluginDisabler.disablePluginFunction(new Identifier("roughlyenoughitems", "default_plugin"), PluginFunction.REGISTER_CATEGORIES);
            pluginDisabler.disablePluginFunction(new Identifier("roughlyenoughitems", "default_plugin"), PluginFunction.REGISTER_RECIPE_DISPLAYS);
            pluginDisabler.disablePluginFunction(new Identifier("roughlyenoughitems", "default_plugin"), PluginFunction.REGISTER_SPEED_CRAFT);
        }
    }
    
    @Override
    public void registerItems(IItemRegisterer itemRegisterer) {
        IRegistry.field_212630_s.forEach(o -> {
            try {
                if (o instanceof Item) {
                    Item item = (Item) o;
                    Optional<NonNullList<ItemStack>> optionalStacks = itemRegisterer.getAlterativeStacks(item);
                    if (optionalStacks.isPresent())
                        itemRegisterer.registerItemStack(optionalStacks.get().toArray(new ItemStack[0]));
                    else
                        itemRegisterer.registerItemStack(item.getDefaultInstance());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        IRegistry.field_212628_q.forEach(o -> {
            if (o instanceof Enchantment) {
                Enchantment ench = (Enchantment) o;
                for(int i = ench.getMinLevel(); i < ench.getMaxLevel(); i++) {
                    Map<Enchantment, Integer> map = new HashMap<>();
                    map.put(ench, i);
                    ItemStack itemStack = new ItemStack(Items.ENCHANTED_BOOK);
                    EnchantmentHelper.setEnchantments(map, itemStack);
                    itemRegisterer.registerItemStack(Items.ENCHANTED_BOOK, itemStack);
                }
            }
        });
    }
    
    @Override
    public void registerPluginCategories(RecipeHelper recipeHelper) {
        recipeHelper.registerCategory(new DefaultCraftingCategory());
        recipeHelper.registerCategory(new DefaultSmeltingCategory());
        recipeHelper.registerCategory(new DefaultBrewingCategory());
    }
    
    // TODO Register Potion
    @Override
    public void registerRecipeDisplays(RecipeHelper recipeHelper) {
        for(IRecipe value : recipeHelper.getRecipeManager().getRecipes())
            if (value instanceof ShapelessRecipe)
                recipeHelper.registerDisplay(CRAFTING, new DefaultShapelessDisplay((ShapelessRecipe) value));
            else if (value instanceof ShapedRecipe)
                recipeHelper.registerDisplay(CRAFTING, new DefaultShapedDisplay((ShapedRecipe) value));
            else if (value instanceof FurnaceRecipe)
                recipeHelper.registerDisplay(SMELTING, new DefaultSmeltingDisplay((FurnaceRecipe) value));
        List<PotionType> registeredPotionTypes = Lists.newArrayList();
        List<BrewingRecipe> potionItemConversions = Lists.newArrayList();
        ((List) PotionBrewing.POTION_ITEM_CONVERSIONS).forEach(o -> {
            try {
                IRegistryDelegate<Item> input = PotionRecipeUtils.getInputFromMixPredicate(o, IRegistryDelegate.class);
                IRegistryDelegate<Item> output = PotionRecipeUtils.getOutputFromMixPredicate(o, IRegistryDelegate.class);
                Ingredient reagent = PotionRecipeUtils.getReagentFromMixPredicate(o);
                potionItemConversions.add(new BrewingRecipe(input.get(), reagent, output.get()));
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        });
        ((List) PotionBrewing.POTION_TYPE_CONVERSIONS).forEach(o -> {
            try {
                IRegistryDelegate<PotionType> input = PotionRecipeUtils.getInputFromMixPredicate(o, IRegistryDelegate.class);
                IRegistryDelegate<PotionType> output = PotionRecipeUtils.getOutputFromMixPredicate(o, IRegistryDelegate.class);
                Ingredient reagent = PotionRecipeUtils.getReagentFromMixPredicate(o);
                if (!registeredPotionTypes.contains(input.get()))
                    registerPotionType(recipeHelper, registeredPotionTypes, potionItemConversions, input.get());
                if (!registeredPotionTypes.contains(output.get()))
                    registerPotionType(recipeHelper, registeredPotionTypes, potionItemConversions, output.get());
                PotionBrewing.POTION_ITEMS.stream().map(Ingredient::getMatchingStacks).forEach(itemStacks -> Arrays.stream(itemStacks).forEach(stack -> {
                    recipeHelper.registerDisplay(BREWING, new DefaultBrewingDisplay(PotionUtils.addPotionToItemStack(stack.copy(), input.get()), reagent, PotionUtils.addPotionToItemStack(stack.copy(), output.get())));
                }));
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        });
    }
    
    private void registerPotionType(RecipeHelper recipeHelper, List<PotionType> list, List<BrewingRecipe> potionItemConversions, PotionType potion) {
        list.add(potion);
        potionItemConversions.forEach(recipe -> {
            recipeHelper.registerDisplay(BREWING, new DefaultBrewingDisplay(PotionUtils.addPotionToItemStack(recipe.input.getDefaultInstance(), potion), recipe.ingredient, PotionUtils.addPotionToItemStack(recipe.output.getDefaultInstance(), potion)));
        });
    }
    
    @Override
    public void registerSpeedCraft(RecipeHelper recipeHelper) {
        recipeHelper.registerSpeedCraftButtonArea(DefaultPlugin.BREWING, null);
        recipeHelper.registerSpeedCraftFunctional(DefaultPlugin.CRAFTING, new ISpeedCraftFunctional<DefaultCraftingDisplay>() {
            @Override
            public Class[] getFunctioningFor() {
                return new Class[]{GuiInventory.class, GuiCrafting.class};
            }
            
            @Override
            public boolean performAutoCraft(Gui gui, DefaultCraftingDisplay recipe) {
                if (gui.getClass().isAssignableFrom(GuiCrafting.class))
                    ((GuiCrafting) gui).func_194310_f().ghostRecipe.clear();
                else if (gui.getClass().isAssignableFrom(GuiInventory.class))
                    ((GuiInventory) gui).func_194310_f().ghostRecipe.clear();
                else
                    return false;
                Minecraft.getInstance().playerController.func_203413_a(Minecraft.getInstance().player.openContainer.windowId, recipe.getRecipe(), GuiScreen.isShiftKeyDown());
                return true;
            }
            
            @Override
            public boolean acceptRecipe(Gui gui, DefaultCraftingDisplay recipe) {
                return gui instanceof GuiCrafting || (gui instanceof GuiInventory && recipe.getHeight() < 3 && recipe.getWidth() < 3);
            }
        });
        recipeHelper.registerSpeedCraftFunctional(DefaultPlugin.SMELTING, new ISpeedCraftFunctional<DefaultSmeltingDisplay>() {
            @Override
            public Class[] getFunctioningFor() {
                return new Class[]{GuiFurnace.class};
            }
            
            @Override
            public boolean performAutoCraft(Gui gui, DefaultSmeltingDisplay recipe) {
                if (gui instanceof GuiFurnace)
                    ((GuiFurnace) gui).func_194310_f().ghostRecipe.clear();
                else
                    return false;
                Minecraft.getInstance().playerController.func_203413_a(Minecraft.getInstance().player.openContainer.windowId, recipe.getRecipe(), GuiScreen.isShiftKeyDown());
                return true;
            }
            
            @Override
            public boolean acceptRecipe(Gui gui, DefaultSmeltingDisplay recipe) {
                return gui instanceof GuiFurnace;
            }
        });
    }
    
    @Override
    public int getPriority() {
        return -1;
    }
    
}
