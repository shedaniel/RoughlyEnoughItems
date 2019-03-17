package me.shedaniel.rei.plugin;

import com.google.common.collect.Lists;
import me.shedaniel.rei.RoughlyEnoughItemsCore;
import me.shedaniel.rei.api.*;
import me.shedaniel.rei.mixin.GhostSlotsHook;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiCrafting;
import net.minecraft.client.gui.inventory.GuiFurnace;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipe;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapedRecipe;
import net.minecraft.item.crafting.ShapelessRecipe;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.registry.IRegistry;

import java.util.*;

public class DefaultPlugin implements REIPlugin {
    
    public static final Identifier CRAFTING = new Identifier("roughlyenoughitems", "plugins/crafting");
    public static final Identifier SMELTING = new Identifier("roughlyenoughitems", "plugins/smelting");
    public static final Identifier BREWING = new Identifier("roughlyenoughitems", "plugins/brewing");
    public static final Identifier PLUGIN = new Identifier("roughlyenoughitems", "default_plugin");
    
    private static final List<DefaultBrewingDisplay> BREWING_DISPLAYS = Lists.newArrayList();
    
    public static void registerBrewingDisplay(DefaultBrewingDisplay display) {
        BREWING_DISPLAYS.add(display);
    }
    
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
    public void registerItems(ItemRegistry itemRegistry) {
        IRegistry.ITEM.stream().forEach(item -> {
            itemRegistry.registerItemStack(item.getDefaultInstance());
            try {
                itemRegistry.registerItemStack(itemRegistry.getAllStacksFromItem(item));
            } catch (Exception e) {
            }
        });
        IRegistry.ENCHANTMENT.forEach(enchantment -> {
            for(int i = enchantment.getMinLevel(); i < enchantment.getMaxLevel(); i++) {
                Map<Enchantment, Integer> map = new HashMap<>();
                map.put(enchantment, i);
                ItemStack itemStack = new ItemStack(Items.ENCHANTED_BOOK);
                EnchantmentHelper.setEnchantments(map, itemStack);
                itemRegistry.registerItemStack(Items.ENCHANTED_BOOK, itemStack);
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
        for(IRecipe recipe : recipeHelper.getRecipeManager().getRecipes())
            if (recipe instanceof ShapelessRecipe)
                recipeHelper.registerDisplay(CRAFTING, new DefaultShapelessDisplay((ShapelessRecipe) recipe));
            else if (recipe instanceof ShapedRecipe)
                recipeHelper.registerDisplay(CRAFTING, new DefaultShapedDisplay((ShapedRecipe) recipe));
            else if (recipe instanceof FurnaceRecipe)
                recipeHelper.registerDisplay(SMELTING, new DefaultSmeltingDisplay((FurnaceRecipe) recipe));
        BREWING_DISPLAYS.stream().forEachOrdered(display -> recipeHelper.registerDisplay(BREWING, display));
        List<ItemStack> arrowStack = Arrays.asList(Items.ARROW.getDefaultInstance());
        RoughlyEnoughItemsCore.getItemRegisterer().getItemList().stream().filter(stack -> stack.getItem().equals(Items.LINGERING_POTION)).forEach(stack -> {
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
    
    @Override
    public void registerSpeedCraft(RecipeHelper recipeHelper) {
        recipeHelper.registerSpeedCraftButtonArea(DefaultPlugin.BREWING, null);
        recipeHelper.registerSpeedCraftFunctional(DefaultPlugin.CRAFTING, new SpeedCraftFunctional<DefaultCraftingDisplay>() {
            @Override
            public Class[] getFunctioningFor() {
                return new Class[]{GuiInventory.class, GuiCrafting.class};
            }
            
            @Override
            public boolean performAutoCraft(GuiScreen screen, DefaultCraftingDisplay recipe) {
                if (!recipe.getRecipe().isPresent())
                    return false;
                if (screen.getClass().isAssignableFrom(GuiCrafting.class))
                    ((GhostSlotsHook) (((GuiCrafting) screen).func_194310_f())).rei_getGhostRecipe().clear();
                else if (screen.getClass().isAssignableFrom(GuiInventory.class))
                    ((GhostSlotsHook) (((GuiInventory) screen).func_194310_f())).rei_getGhostRecipe().clear();
                else
                    return false;
                Minecraft.getInstance().playerController.func_203413_a(Minecraft.getInstance().player.openContainer.windowId, (IRecipe) recipe.getRecipe().get(), GuiScreen.isShiftKeyDown());
                return true;
            }
            
            @Override
            public boolean acceptRecipe(GuiScreen screen, DefaultCraftingDisplay recipe) {
                return screen instanceof GuiCrafting || (screen instanceof GuiInventory && recipe.getHeight() < 3 && recipe.getWidth() < 3);
            }
        });
        recipeHelper.registerSpeedCraftFunctional(DefaultPlugin.SMELTING, new SpeedCraftFunctional<DefaultSmeltingDisplay>() {
            @Override
            public Class[] getFunctioningFor() {
                return new Class[]{GuiFurnace.class};
            }
            
            @Override
            public boolean performAutoCraft(GuiScreen screen, DefaultSmeltingDisplay recipe) {
                if (!recipe.getRecipe().isPresent())
                    return false;
                if (screen instanceof GuiFurnace)
                    ((GhostSlotsHook) (((GuiFurnace) screen).func_194310_f())).rei_getGhostRecipe().clear();
                else
                    return false;
                Minecraft.getInstance().playerController.func_203413_a(Minecraft.getInstance().player.openContainer.windowId, recipe.getRecipe().get(), GuiScreen.isShiftKeyDown());
                return true;
            }
            
            @Override
            public boolean acceptRecipe(GuiScreen screen, DefaultSmeltingDisplay recipe) {
                return screen instanceof GuiFurnace;
            }
        });
    }
    
    @Override
    public int getPriority() {
        return -1;
    }
    
}
