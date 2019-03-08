package me.shedaniel.rei.plugin;

import com.google.common.collect.Lists;
import me.shedaniel.rei.RoughlyEnoughItemsCore;
import me.shedaniel.rei.api.*;
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
import net.minecraft.potion.PotionType;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IRegistryDelegate;

import java.util.*;

import static me.shedaniel.rei.utils.RecipeBookUtils.getGhostRecipe;

@IREIPlugin(identifier = "roughlyenoughitems:default_plugin")
public class DefaultPlugin implements REIPlugin {
    
    public static final ResourceLocation CRAFTING = new ResourceLocation("roughlyenoughitems", "plugins/crafting");
    public static final ResourceLocation SMELTING = new ResourceLocation("roughlyenoughitems", "plugins/smelting");
    public static final ResourceLocation BREWING = new ResourceLocation("roughlyenoughitems", "plugins/brewing");
    public static final ResourceLocation PLUGIN = new ResourceLocation("roughlyenoughitems", "default_plugin");
    
    @Override
    public void onFirstLoad(PluginDisabler pluginDisabler) {
        if (!RoughlyEnoughItemsCore.getConfigManager().getConfig().loadDefaultPlugin) {
            pluginDisabler.disablePluginFunction(PLUGIN, PluginFunction.REGISTER_ITEMS);
            pluginDisabler.disablePluginFunction(PLUGIN, PluginFunction.REGISTER_CATEGORIES);
            pluginDisabler.disablePluginFunction(PLUGIN, PluginFunction.REGISTER_RECIPE_DISPLAYS);
            pluginDisabler.disablePluginFunction(PLUGIN, PluginFunction.REGISTER_SPEED_CRAFT);
        }
    }
    
    @Override
    public void registerItems(ItemRegistry itemRegisterer) {
        ForgeRegistries.ITEMS.forEach(o -> {
            try {
                if (o instanceof Item) {
                    Item item = (Item) o;
                    if (item.equals(Items.ENCHANTED_BOOK)) {
                        itemRegisterer.registerItemStack(item.getDefaultInstance());
                    } else {
                        Optional<NonNullList<ItemStack>> optionalStacks = itemRegisterer.getAlterativeStacks(item);
                        if (optionalStacks.isPresent())
                            itemRegisterer.registerItemStack(optionalStacks.get().toArray(new ItemStack[0]));
                        else
                            itemRegisterer.registerItemStack(item.getDefaultInstance());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        ForgeRegistries.ENCHANTMENTS.forEach(o -> {
            if (o instanceof Enchantment) {
                Enchantment enchantment = (Enchantment) o;
                for(int i = enchantment.getMinLevel(); i < enchantment.getMaxLevel(); i++) {
                    Map<Enchantment, Integer> map = new HashMap<>();
                    map.put(enchantment, i);
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
        List<Ingredient> potionItems = PotionRecipeUtils.getPotionItems();
        PotionRecipeUtils.getPotionItemConversions().forEach(o -> {
            try {
                IRegistryDelegate<Item> input = PotionRecipeUtils.getInputFromMixPredicate(o, IRegistryDelegate.class);
                IRegistryDelegate<Item> output = PotionRecipeUtils.getOutputFromMixPredicate(o, IRegistryDelegate.class);
                Ingredient reagent = PotionRecipeUtils.getReagentFromMixPredicate(o);
                potionItemConversions.add(new BrewingRecipe(input.get(), reagent, output.get()));
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        });
        PotionRecipeUtils.getPotionTypeConversions().forEach(o -> {
            try {
                IRegistryDelegate<PotionType> input = PotionRecipeUtils.getInputFromMixPredicate(o, IRegistryDelegate.class);
                IRegistryDelegate<PotionType> output = PotionRecipeUtils.getOutputFromMixPredicate(o, IRegistryDelegate.class);
                Ingredient reagent = PotionRecipeUtils.getReagentFromMixPredicate(o);
                if (!registeredPotionTypes.contains(input.get()))
                    registerPotionType(recipeHelper, registeredPotionTypes, potionItemConversions, input.get());
                if (!registeredPotionTypes.contains(output.get()))
                    registerPotionType(recipeHelper, registeredPotionTypes, potionItemConversions, output.get());
                potionItems.stream().map(Ingredient::getMatchingStacks).forEach(itemStacks -> Arrays.stream(itemStacks).forEach(stack -> {
                    recipeHelper.registerDisplay(BREWING, new DefaultBrewingDisplay(PotionUtils.addPotionToItemStack(stack.copy(), input.get()), reagent, PotionUtils.addPotionToItemStack(stack.copy(), output.get())));
                }));
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        });
        List<ItemStack> arrowStack = Arrays.asList(Items.ARROW.getDefaultInstance());
        RoughlyEnoughItemsCore.getItemRegistry().getItemList().stream().filter(stack -> stack.getItem().equals(Items.LINGERING_POTION)).forEach(stack -> {
            List<List<ItemStack>> input = new ArrayList<>();
            for(int i = 0; i < 4; i++)
                input.add(arrowStack);
            input.add(Arrays.asList(stack));
            for(int i = 0; i < 4; i++)
                input.add(arrowStack);
            ItemStack outputStack = new ItemStack(Items.TIPPED_ARROW, 8);
            PotionUtils.addPotionToItemStack(outputStack, PotionUtils.getPotionFromItem(stack));
            PotionUtils.appendEffects(outputStack, PotionUtils.getFullEffectsFromItem(stack));
            List<ItemStack> output = Lists.newArrayList(outputStack);
            recipeHelper.registerDisplay(CRAFTING, new DefaultCustomDisplay(input, output));
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
        recipeHelper.registerSpeedCraftFunctional(DefaultPlugin.CRAFTING, new SpeedCraftFunctional<DefaultCraftingDisplay>() {
            @Override
            public Class[] getFunctioningFor() {
                return new Class[]{GuiInventory.class, GuiCrafting.class};
            }
            
            @Override
            public boolean performAutoCraft(Gui gui, DefaultCraftingDisplay recipe) {
                if (!recipe.getRecipe().isPresent())
                    return false;
                try {
                    if (gui.getClass().isAssignableFrom(GuiCrafting.class))
                        getGhostRecipe(((GuiCrafting) gui).func_194310_f()).clear();
                    else if (gui.getClass().isAssignableFrom(GuiInventory.class))
                        getGhostRecipe(((GuiInventory) gui).func_194310_f()).clear();
                    else
                        return false;
                    Minecraft.getInstance().playerController.func_203413_a(Minecraft.getInstance().player.openContainer.windowId, (IRecipe) recipe.getRecipe().get(), GuiScreen.isShiftKeyDown());
                    return true;
                } catch (Throwable e) {
                    return false;
                }
            }
            
            @Override
            public boolean acceptRecipe(Gui gui, DefaultCraftingDisplay recipe) {
                return gui instanceof GuiCrafting || (gui instanceof GuiInventory && recipe.getHeight() < 3 && recipe.getWidth() < 3);
            }
        });
        recipeHelper.registerSpeedCraftFunctional(DefaultPlugin.SMELTING, new SpeedCraftFunctional<DefaultSmeltingDisplay>() {
            @Override
            public Class[] getFunctioningFor() {
                return new Class[]{GuiFurnace.class};
            }
            
            @Override
            public boolean performAutoCraft(Gui gui, DefaultSmeltingDisplay recipe) {
                if (!recipe.getRecipe().isPresent())
                    return false;
                try {
                    if (gui instanceof GuiFurnace)
                        getGhostRecipe(((GuiFurnace) gui).func_194310_f()).clear();
                    else
                        return false;
                    Minecraft.getInstance().playerController.func_203413_a(Minecraft.getInstance().player.openContainer.windowId, recipe.getRecipe().get(), GuiScreen.isShiftKeyDown());
                    return true;
                } catch (Throwable e) {
                    return false;
                }
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
