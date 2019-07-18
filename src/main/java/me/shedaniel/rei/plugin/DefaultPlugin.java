/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.plugin;

import com.google.common.collect.Lists;
import me.shedaniel.rei.RoughlyEnoughItemsClient;
import me.shedaniel.rei.RoughlyEnoughItemsCore;
import me.shedaniel.rei.api.*;
import me.shedaniel.rei.client.ScreenHelper;
import me.shedaniel.rei.gui.RecipeViewingScreen;
import me.shedaniel.rei.gui.VillagerRecipeViewingScreen;
import me.shedaniel.rei.plugin.brewing.DefaultBrewingCategory;
import me.shedaniel.rei.plugin.brewing.DefaultBrewingDisplay;
import me.shedaniel.rei.plugin.crafting.DefaultCraftingCategory;
import me.shedaniel.rei.plugin.crafting.DefaultCustomDisplay;
import me.shedaniel.rei.plugin.crafting.DefaultShapedDisplay;
import me.shedaniel.rei.plugin.crafting.DefaultShapelessDisplay;
import me.shedaniel.rei.plugin.smelting.DefaultSmeltingCategory;
import me.shedaniel.rei.plugin.smelting.DefaultSmeltingDisplay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.*;
import net.minecraft.client.gui.recipebook.IRecipeShownListener;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipe;
import net.minecraft.item.crafting.ShapedRecipe;
import net.minecraft.item.crafting.ShapelessRecipe;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.registry.IRegistry;

import java.awt.*;
import java.util.List;
import java.util.*;

public class DefaultPlugin implements REIPluginEntry {
    
    public static final Identifier CRAFTING = new Identifier("minecraft", "plugins/crafting");
    public static final Identifier SMELTING = new Identifier("minecraft", "plugins/smelting");
    public static final Identifier BREWING = new Identifier("minecraft", "plugins/brewing");
    public static final Identifier PLUGIN = new Identifier("roughlyenoughitems", "default_plugin");
    private static final Identifier DISPLAY_TEXTURE = new Identifier("roughlyenoughitems", "textures/gui/display.png");
    private static final Identifier DISPLAY_TEXTURE_DARK = new Identifier("roughlyenoughitems", "textures/gui/display_dark.png");
    private static final List<DefaultBrewingDisplay> BREWING_DISPLAYS = Lists.newArrayList();
    
    public static Identifier getDisplayTexture() {
        return ScreenHelper.isDarkModeEnabled() ? DISPLAY_TEXTURE_DARK : DISPLAY_TEXTURE;
    }
    
    public static void registerBrewingDisplay(DefaultBrewingDisplay display) {
        BREWING_DISPLAYS.add(display);
    }
    
    @Override
    public Identifier getPluginIdentifier() {
        return PLUGIN;
    }
    
    @Override
    public void onFirstLoad(PluginDisabler pluginDisabler) {
        if (!RoughlyEnoughItemsClient.getConfigManager().getConfig().loadDefaultPlugin) {
            pluginDisabler.disablePluginFunction(PLUGIN, PluginFunction.REGISTER_ITEMS);
            pluginDisabler.disablePluginFunction(PLUGIN, PluginFunction.REGISTER_CATEGORIES);
            pluginDisabler.disablePluginFunction(PLUGIN, PluginFunction.REGISTER_RECIPE_DISPLAYS);
            pluginDisabler.disablePluginFunction(PLUGIN, PluginFunction.REGISTER_OTHERS);
        }
    }
    
    @Override
    public void registerItems(ItemRegistry itemRegistry) {
        IRegistry.ITEM.stream().forEach(item -> {
            if (item != Items.ENCHANTED_BOOK) {
                itemRegistry.registerItemStack(item.getDefaultInstance());
                try {
                    itemRegistry.registerItemStack(itemRegistry.getAllStacksFromItem(item));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        IRegistry.ENCHANTMENT.forEach(enchantment -> {
            for(int i = enchantment.getMinLevel(); i <= enchantment.getMaxLevel(); i++) {
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
        recipeHelper.registerRecipes(CRAFTING, ShapelessRecipe.class, DefaultShapelessDisplay::new);
        recipeHelper.registerRecipes(CRAFTING, ShapedRecipe.class, DefaultShapedDisplay::new);
        recipeHelper.registerRecipes(SMELTING, FurnaceRecipe.class, DefaultSmeltingDisplay::new);
        BREWING_DISPLAYS.stream().forEachOrdered(display -> recipeHelper.registerDisplay(BREWING, display));
        List<ItemStack> arrowStack = Collections.singletonList(Items.ARROW.getDefaultInstance());
        RoughlyEnoughItemsCore.getItemRegisterer().getItemList().stream().filter(stack -> stack.getItem().equals(Items.LINGERING_POTION)).forEach(stack -> {
            List<List<ItemStack>> input = new ArrayList<>();
            for(int i = 0; i < 4; i++)
                input.add(arrowStack);
            input.add(Collections.singletonList(stack));
            for(int i = 0; i < 4; i++)
                input.add(arrowStack);
            ItemStack outputStack = new ItemStack(Items.TIPPED_ARROW, 8);
            PotionUtils.addPotionToItemStack(outputStack, PotionUtils.getPotionFromItem(stack));
            PotionUtils.appendEffects(outputStack, PotionUtils.getFullEffectsFromItem(stack));
            List<ItemStack> output = Collections.singletonList(outputStack);
            recipeHelper.registerDisplay(CRAFTING, new DefaultCustomDisplay(input, output));
        });
    }
    
    @Override
    public void registerBounds(DisplayHelper displayHelper) {
        displayHelper.getBaseBoundsHandler().registerExclusionZones(GuiContainer.class, new DefaultPotionEffectExclusionZones());
        displayHelper.getBaseBoundsHandler().registerExclusionZones(IRecipeShownListener.class, new DefaultRecipeBookExclusionZones());
        displayHelper.registerBoundsHandler(new DisplayHelper.DisplayBoundsHandler<GuiContainer>() {
            @Override
            public Class<?> getBaseSupportedClass() {
                return GuiContainer.class;
            }
            
            @Override
            public Rectangle getLeftBounds(GuiContainer screen) {
                return new Rectangle(2, 0, ScreenHelper.getLastContainerScreenHooks().rei_getContainerLeft() - 4, Minecraft.getInstance().mainWindow.getScaledHeight());
            }
            
            @Override
            public Rectangle getRightBounds(GuiContainer screen) {
                int startX = ScreenHelper.getLastContainerScreenHooks().rei_getContainerLeft() + ScreenHelper.getLastContainerScreenHooks().rei_getContainerWidth() + 2;
                return new Rectangle(startX, 0, Minecraft.getInstance().mainWindow.getScaledWidth() - startX - 2, Minecraft.getInstance().mainWindow.getScaledHeight());
            }
            
            @Override
            public float getPriority() {
                return -1.0f;
            }
        });
        displayHelper.registerBoundsHandler(new DisplayHelper.DisplayBoundsHandler<RecipeViewingScreen>() {
            @Override
            public Class<?> getBaseSupportedClass() {
                return RecipeViewingScreen.class;
            }
            
            @Override
            public Rectangle getLeftBounds(RecipeViewingScreen screen) {
                return new Rectangle(2, 0, ((RecipeViewingScreen) screen).getBounds().x - 4, Minecraft.getInstance().mainWindow.getScaledHeight());
            }
            
            @Override
            public Rectangle getRightBounds(RecipeViewingScreen screen) {
                int startX = ((RecipeViewingScreen) screen).getBounds().x + ((RecipeViewingScreen) screen).getBounds().width + 2;
                return new Rectangle(startX, 0, Minecraft.getInstance().mainWindow.getScaledWidth() - startX - 2, Minecraft.getInstance().mainWindow.getScaledHeight());
            }
            
            @Override
            public float getPriority() {
                return -1.0f;
            }
        });
        displayHelper.registerBoundsHandler(new DisplayHelper.DisplayBoundsHandler<VillagerRecipeViewingScreen>() {
            @Override
            public Class<?> getBaseSupportedClass() {
                return VillagerRecipeViewingScreen.class;
            }
            
            @Override
            public Rectangle getLeftBounds(VillagerRecipeViewingScreen screen) {
                return new Rectangle(2, 0, ((VillagerRecipeViewingScreen) screen).bounds.x - 4, Minecraft.getInstance().mainWindow.getScaledHeight());
            }
            
            @Override
            public Rectangle getRightBounds(VillagerRecipeViewingScreen screen) {
                int startX = ((VillagerRecipeViewingScreen) screen).bounds.x + ((VillagerRecipeViewingScreen) screen).bounds.width + 2;
                return new Rectangle(startX, 0, Minecraft.getInstance().mainWindow.getScaledWidth() - startX - 2, Minecraft.getInstance().mainWindow.getScaledHeight());
            }
            
            @Override
            public float getPriority() {
                return -1.0f;
            }
        });
        displayHelper.registerBoundsHandler(new DisplayHelper.DisplayBoundsHandler<GuiContainerCreative>() {
            @Override
            public Class<?> getBaseSupportedClass() {
                return GuiContainerCreative.class;
            }
            
            @Override
            public Rectangle getLeftBounds(GuiContainerCreative screen) {
                return new Rectangle(2, 0, ScreenHelper.getLastContainerScreenHooks().rei_getContainerLeft() - 2, Minecraft.getInstance().mainWindow.getScaledHeight());
            }
            
            @Override
            public Rectangle getRightBounds(GuiContainerCreative screen) {
                int startX = ScreenHelper.getLastContainerScreenHooks().rei_getContainerLeft() + ScreenHelper.getLastContainerScreenHooks().rei_getContainerWidth();
                return new Rectangle(startX, 0, Minecraft.getInstance().mainWindow.getScaledWidth() - startX - 2, Minecraft.getInstance().mainWindow.getScaledHeight());
            }
            
            @Override
            public Rectangle getItemListArea(Rectangle rectangle) {
                return new Rectangle(rectangle.x + 1, rectangle.y + 24, rectangle.width - 2, rectangle.height - (RoughlyEnoughItemsClient.getConfigManager().getConfig().sideSearchField ? 27 + 22 : 27));
            }
            
            @Override
            public float getPriority() {
                return -0.9f;
            }
        });
    }
    
    @Override
    public void registerOthers(RecipeHelper recipeHelper) {
        recipeHelper.registerWorkingStations(CRAFTING, new ItemStack(Blocks.CRAFTING_TABLE));
        recipeHelper.registerWorkingStations(SMELTING, new ItemStack(Blocks.FURNACE));
        recipeHelper.registerWorkingStations(BREWING, new ItemStack(Blocks.BREWING_STAND));
        recipeHelper.registerScreenClickArea(new Rectangle(88, 32, 28, 23), GuiCrafting.class, CRAFTING);
        recipeHelper.registerScreenClickArea(new Rectangle(137, 29, 10, 13), GuiInventory.class, CRAFTING);
        recipeHelper.registerScreenClickArea(new Rectangle(97, 16, 14, 30), GuiBrewingStand.class, BREWING);
        recipeHelper.registerScreenClickArea(new Rectangle(78, 32, 28, 23), GuiFurnace.class, SMELTING);
    }
    
    @Override
    public int getPriority() {
        return -1;
    }
    
}
