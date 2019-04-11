package me.shedaniel.rei.plugin;

import com.google.common.collect.Lists;
import me.shedaniel.rei.RoughlyEnoughItemsCore;
import me.shedaniel.rei.api.*;
import me.shedaniel.rei.client.ScreenHelper;
import me.shedaniel.rei.gui.RecipeViewingScreen;
import me.shedaniel.rei.listeners.GhostSlotsHooks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.ContainerScreen;
import net.minecraft.client.gui.Screen;
import net.minecraft.client.gui.container.BlastFurnaceScreen;
import net.minecraft.client.gui.container.CraftingTableScreen;
import net.minecraft.client.gui.container.FurnaceScreen;
import net.minecraft.client.gui.container.SmokerScreen;
import net.minecraft.client.gui.ingame.CreativePlayerInventoryScreen;
import net.minecraft.client.gui.ingame.PlayerInventoryScreen;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.PotionUtil;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.StonecuttingRecipe;
import net.minecraft.recipe.cooking.BlastingRecipe;
import net.minecraft.recipe.cooking.CampfireCookingRecipe;
import net.minecraft.recipe.cooking.SmeltingRecipe;
import net.minecraft.recipe.cooking.SmokingRecipe;
import net.minecraft.recipe.crafting.ShapedRecipe;
import net.minecraft.recipe.crafting.ShapelessRecipe;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.awt.*;
import java.util.*;
import java.util.List;

public class DefaultPlugin implements REIPlugin {
    
    public static final Identifier CRAFTING = new Identifier("roughlyenoughitems", "plugins/crafting");
    public static final Identifier SMELTING = new Identifier("roughlyenoughitems", "plugins/smelting");
    public static final Identifier SMOKING = new Identifier("roughlyenoughitems", "plugins/smoking");
    public static final Identifier BLASTING = new Identifier("roughlyenoughitems", "plugins/blasting");
    public static final Identifier CAMPFIRE = new Identifier("roughlyenoughitems", "plugins/campfire");
    public static final Identifier STONE_CUTTING = new Identifier("roughlyenoughitems", "plugins/stone_cutting");
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
            pluginDisabler.disablePluginFunction(PLUGIN, PluginFunction.REGISTER_OTHERS);
        }
    }
    
    @Override
    public void registerItems(ItemRegistry itemRegistry) {
        Registry.ITEM.stream().forEach(item -> {
            itemRegistry.registerItemStack(item.getDefaultStack());
            try {
                itemRegistry.registerItemStack(itemRegistry.getAllStacksFromItem(item));
            } catch (Exception e) {
            }
        });
        Registry.ENCHANTMENT.forEach(enchantment -> {
            for(int i = enchantment.getMinimumLevel(); i < enchantment.getMaximumLevel(); i++) {
                Map<Enchantment, Integer> map = new HashMap<>();
                map.put(enchantment, i);
                ItemStack itemStack = new ItemStack(Items.ENCHANTED_BOOK);
                EnchantmentHelper.set(map, itemStack);
                itemRegistry.registerItemStack(Items.ENCHANTED_BOOK, itemStack);
            }
        });
    }
    
    @Override
    public void registerPluginCategories(RecipeHelper recipeHelper) {
        recipeHelper.registerCategory(new DefaultCraftingCategory());
        recipeHelper.registerCategory(new DefaultSmeltingCategory());
        recipeHelper.registerCategory(new DefaultSmokingCategory());
        recipeHelper.registerCategory(new DefaultBlastingCategory());
        recipeHelper.registerCategory(new DefaultCampfireCategory());
        recipeHelper.registerCategory(new DefaultStoneCuttingCategory());
        recipeHelper.registerCategory(new DefaultBrewingCategory());
    }
    
    @Override
    public void registerRecipeDisplays(RecipeHelper recipeHelper) {
        for(Recipe recipe : recipeHelper.getRecipeManager().values())
            if (recipe instanceof ShapelessRecipe)
                recipeHelper.registerDisplay(CRAFTING, new DefaultShapelessDisplay((ShapelessRecipe) recipe));
            else if (recipe instanceof ShapedRecipe)
                recipeHelper.registerDisplay(CRAFTING, new DefaultShapedDisplay((ShapedRecipe) recipe));
            else if (recipe instanceof SmeltingRecipe)
                recipeHelper.registerDisplay(SMELTING, new DefaultSmeltingDisplay((SmeltingRecipe) recipe));
            else if (recipe instanceof SmokingRecipe)
                recipeHelper.registerDisplay(SMOKING, new DefaultSmokingDisplay((SmokingRecipe) recipe));
            else if (recipe instanceof BlastingRecipe)
                recipeHelper.registerDisplay(BLASTING, new DefaultBlastingDisplay((BlastingRecipe) recipe));
            else if (recipe instanceof CampfireCookingRecipe)
                recipeHelper.registerDisplay(CAMPFIRE, new DefaultCampfireDisplay((CampfireCookingRecipe) recipe));
            else if (recipe instanceof StonecuttingRecipe)
                recipeHelper.registerDisplay(STONE_CUTTING, new DefaultStoneCuttingDisplay((StonecuttingRecipe) recipe));
        BREWING_DISPLAYS.stream().forEachOrdered(display -> recipeHelper.registerDisplay(BREWING, display));
        List<ItemStack> arrowStack = Arrays.asList(Items.ARROW.getDefaultStack());
        RoughlyEnoughItemsCore.getItemRegisterer().getItemList().stream().filter(stack -> stack.getItem().equals(Items.LINGERING_POTION)).forEach(stack -> {
            List<List<ItemStack>> input = new ArrayList<>();
            for(int i = 0; i < 4; i++)
                input.add(arrowStack);
            input.add(Arrays.asList(stack));
            for(int i = 0; i < 4; i++)
                input.add(arrowStack);
            ItemStack outputStack = new ItemStack(Items.TIPPED_ARROW, 8);
            PotionUtil.setPotion(outputStack, PotionUtil.getPotion(stack));
            PotionUtil.setCustomPotionEffects(outputStack, PotionUtil.getCustomPotionEffects(stack));
            List<ItemStack> output = Lists.newArrayList(outputStack);
            recipeHelper.registerDisplay(CRAFTING, new DefaultCustomDisplay(input, output));
        });
    }
    
    @Override
    public void registerBounds(DisplayHelper displayHelper) {
        displayHelper.registerBoundsHandler(new DisplayHelper.DisplayBoundsHandler() {
            @Override
            public Class getBaseSupportedClass() {
                return ContainerScreen.class;
            }
            
            @Override
            public Rectangle getLeftBounds(Object screen) {
                if (MinecraftClient.getInstance().player.getRecipeBook().isGuiOpen())
                    return new Rectangle(2, 0, ScreenHelper.getLastContainerScreenHooks().rei_getContainerLeft() - 4 - 147 - 30, MinecraftClient.getInstance().window.getScaledHeight());
                return new Rectangle(2, 0, ScreenHelper.getLastContainerScreenHooks().rei_getContainerLeft() - 4, MinecraftClient.getInstance().window.getScaledHeight());
            }
            
            @Override
            public Rectangle getRightBounds(Object screen) {
                int startX = ScreenHelper.getLastContainerScreenHooks().rei_getContainerLeft() + ScreenHelper.getLastContainerScreenHooks().rei_getContainerWidth() + 2;
                return new Rectangle(startX, 0, MinecraftClient.getInstance().window.getScaledWidth() - startX - 2, MinecraftClient.getInstance().window.getScaledHeight());
            }
            
            @Override
            public float getPriority() {
                return -1.0f;
            }
        });
        displayHelper.registerBoundsHandler(new DisplayHelper.DisplayBoundsHandler() {
            @Override
            public Class getBaseSupportedClass() {
                return RecipeViewingScreen.class;
            }
            
            @Override
            public Rectangle getLeftBounds(Object screen) {
                return new Rectangle(2, 0, ((RecipeViewingScreen) screen).getBounds().x - 4, MinecraftClient.getInstance().window.getScaledHeight());
            }
            
            @Override
            public Rectangle getRightBounds(Object screen) {
                int startX = ((RecipeViewingScreen) screen).getBounds().x + ((RecipeViewingScreen) screen).getBounds().width + 2;
                return new Rectangle(startX, 0, MinecraftClient.getInstance().window.getScaledWidth() - startX - 2, MinecraftClient.getInstance().window.getScaledHeight());
            }
            
            @Override
            public float getPriority() {
                return -1.0f;
            }
        });
        displayHelper.registerBoundsHandler(new DisplayHelper.DisplayBoundsHandler() {
            @Override
            public Class getBaseSupportedClass() {
                return CreativePlayerInventoryScreen.class;
            }
            
            @Override
            public Rectangle getLeftBounds(Object screen) {
                return new Rectangle(2, 0, ScreenHelper.getLastContainerScreenHooks().rei_getContainerLeft() - 2, MinecraftClient.getInstance().window.getScaledHeight());
            }
            
            @Override
            public Rectangle getRightBounds(Object screen) {
                int startX = ScreenHelper.getLastContainerScreenHooks().rei_getContainerLeft() + ScreenHelper.getLastContainerScreenHooks().rei_getContainerWidth();
                return new Rectangle(startX, 0, MinecraftClient.getInstance().window.getScaledWidth() - startX - 2, MinecraftClient.getInstance().window.getScaledHeight());
            }
            
            @Override
            public Rectangle getItemListArea(Rectangle rectangle) {
                return new Rectangle(rectangle.x + 1, rectangle.y + 24, rectangle.width - 2, rectangle.height - (RoughlyEnoughItemsCore.getConfigManager().getConfig().sideSearchField ? 27 + 22 : 27));
            }
            
            @Override
            public float getPriority() {
                return -0.9f;
            }
        });
    }
    
    @Override
    public void registerOthers(RecipeHelper recipeHelper) {
        recipeHelper.registerRecipeVisibilityHandler(new DisplayVisibilityHandler() {
            @Override
            public DisplayVisibility handleDisplay(RecipeCategory category, RecipeDisplay display) {
                return DisplayVisibility.ALWAYS_VISIBLE;
            }
            
            @Override
            public float getPriority() {
                return -1f;
            }
        });
        recipeHelper.registerDefaultSpeedCraftButtonArea(DefaultPlugin.CRAFTING);
        recipeHelper.registerDefaultSpeedCraftButtonArea(DefaultPlugin.SMELTING);
        recipeHelper.registerDefaultSpeedCraftButtonArea(DefaultPlugin.SMOKING);
        recipeHelper.registerDefaultSpeedCraftButtonArea(DefaultPlugin.BLASTING);
        recipeHelper.registerSpeedCraftFunctional(DefaultPlugin.CRAFTING, new SpeedCraftFunctional<DefaultCraftingDisplay>() {
            @Override
            public Class[] getFunctioningFor() {
                return new Class[]{PlayerInventoryScreen.class, CraftingTableScreen.class};
            }
            
            @Override
            public boolean performAutoCraft(Screen screen, DefaultCraftingDisplay recipe) {
                if (!recipe.getRecipe().isPresent())
                    return false;
                if (screen.getClass().isAssignableFrom(CraftingTableScreen.class))
                    ((GhostSlotsHooks) (((CraftingTableScreen) screen).getRecipeBookGui())).rei_getGhostSlots().reset();
                else if (screen.getClass().isAssignableFrom(PlayerInventoryScreen.class))
                    ((GhostSlotsHooks) (((PlayerInventoryScreen) screen).getRecipeBookGui())).rei_getGhostSlots().reset();
                else
                    return false;
                MinecraftClient.getInstance().interactionManager.clickRecipe(MinecraftClient.getInstance().player.container.syncId, (Recipe) recipe.getRecipe().get(), Screen.hasShiftDown());
                return true;
            }
            
            @Override
            public boolean acceptRecipe(Screen screen, DefaultCraftingDisplay recipe) {
                return screen instanceof CraftingTableScreen || (screen instanceof PlayerInventoryScreen && recipe.getHeight() < 3 && recipe.getWidth() < 3);
            }
        });
        recipeHelper.registerSpeedCraftFunctional(DefaultPlugin.SMELTING, new SpeedCraftFunctional<DefaultSmeltingDisplay>() {
            @Override
            public Class[] getFunctioningFor() {
                return new Class[]{FurnaceScreen.class};
            }
            
            @Override
            public boolean performAutoCraft(Screen screen, DefaultSmeltingDisplay recipe) {
                if (!recipe.getRecipe().isPresent())
                    return false;
                if (screen instanceof FurnaceScreen)
                    ((GhostSlotsHooks) (((FurnaceScreen) screen).getRecipeBookGui())).rei_getGhostSlots().reset();
                else
                    return false;
                MinecraftClient.getInstance().interactionManager.clickRecipe(MinecraftClient.getInstance().player.container.syncId, (Recipe) recipe.getRecipe().get(), Screen.hasShiftDown());
                return true;
            }
            
            @Override
            public boolean acceptRecipe(Screen screen, DefaultSmeltingDisplay recipe) {
                return screen instanceof FurnaceScreen;
            }
        });
        recipeHelper.registerSpeedCraftFunctional(DefaultPlugin.SMOKING, new SpeedCraftFunctional<DefaultSmokingDisplay>() {
            @Override
            public Class[] getFunctioningFor() {
                return new Class[]{SmokerScreen.class};
            }
            
            @Override
            public boolean performAutoCraft(Screen screen, DefaultSmokingDisplay recipe) {
                if (!recipe.getRecipe().isPresent())
                    return false;
                if (screen instanceof SmokerScreen)
                    ((GhostSlotsHooks) (((SmokerScreen) screen).getRecipeBookGui())).rei_getGhostSlots().reset();
                else
                    return false;
                MinecraftClient.getInstance().interactionManager.clickRecipe(MinecraftClient.getInstance().player.container.syncId, (Recipe) recipe.getRecipe().get(), Screen.hasShiftDown());
                return true;
            }
            
            @Override
            public boolean acceptRecipe(Screen screen, DefaultSmokingDisplay recipe) {
                return screen instanceof SmokerScreen;
            }
        });
        recipeHelper.registerSpeedCraftFunctional(DefaultPlugin.BLASTING, new SpeedCraftFunctional<DefaultBlastingDisplay>() {
            @Override
            public Class[] getFunctioningFor() {
                return new Class[]{BlastFurnaceScreen.class};
            }
            
            @Override
            public boolean acceptRecipe(Screen screen, DefaultBlastingDisplay recipe) {
                return screen instanceof BlastFurnaceScreen;
            }
            
            @Override
            public boolean performAutoCraft(Screen screen, DefaultBlastingDisplay recipe) {
                if (!recipe.getRecipe().isPresent())
                    return false;
                if (screen instanceof BlastFurnaceScreen)
                    ((GhostSlotsHooks) (((BlastFurnaceScreen) screen).getRecipeBookGui())).rei_getGhostSlots().reset();
                else
                    return false;
                MinecraftClient.getInstance().interactionManager.clickRecipe(MinecraftClient.getInstance().player.container.syncId, (Recipe) recipe.getRecipe().get(), Screen.hasShiftDown());
                return true;
            }
        });
    }
    
    @Override
    public int getPriority() {
        return -1;
    }
    
}
