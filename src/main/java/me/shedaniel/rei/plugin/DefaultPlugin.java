package me.shedaniel.rei.plugin;

import com.google.common.collect.Lists;
import me.shedaniel.rei.RoughlyEnoughItemsCore;
import me.shedaniel.rei.api.IRecipePlugin;
import me.shedaniel.rei.api.SpeedCraftFunctional;
import me.shedaniel.rei.listeners.IMixinRecipeBookGui;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Screen;
import net.minecraft.client.gui.container.BlastFurnaceScreen;
import net.minecraft.client.gui.container.CraftingTableScreen;
import net.minecraft.client.gui.container.FurnaceScreen;
import net.minecraft.client.gui.container.SmokerScreen;
import net.minecraft.client.gui.ingame.PlayerInventoryScreen;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.StonecuttingRecipe;
import net.minecraft.recipe.cooking.BlastingRecipe;
import net.minecraft.recipe.cooking.CampfireCookingRecipe;
import net.minecraft.recipe.cooking.SmeltingRecipe;
import net.minecraft.recipe.cooking.SmokingRecipe;
import net.minecraft.recipe.crafting.ShapedRecipe;
import net.minecraft.recipe.crafting.ShapelessRecipe;
import net.minecraft.util.Identifier;

import java.util.List;

public class DefaultPlugin implements IRecipePlugin {
    
    public static final Identifier CRAFTING = new Identifier("roughlyenoughitems", "plugins/crafting");
    public static final Identifier SMELTING = new Identifier("roughlyenoughitems", "plugins/smelting");
    public static final Identifier SMOKING = new Identifier("roughlyenoughitems", "plugins/smoking");
    public static final Identifier BLASTING = new Identifier("roughlyenoughitems", "plugins/blasting");
    public static final Identifier CAMPFIRE = new Identifier("roughlyenoughitems", "plugins/campfire");
    public static final Identifier STONE_CUTTING = new Identifier("roughlyenoughitems", "plugins/stone_cutting");
    public static final Identifier BREWING = new Identifier("roughlyenoughitems", "plugins/brewing");
    
    private static final List<DefaultBrewingDisplay> BREWING_DISPLAYS = Lists.newArrayList();
    
    public static void registerBrewingDisplay(DefaultBrewingDisplay display) {
        BREWING_DISPLAYS.add(display);
    }
    
    @Override
    public void registerPluginCategories() {
        RoughlyEnoughItemsCore.getRecipeHelper().registerCategory(new DefaultCraftingCategory());
        RoughlyEnoughItemsCore.getRecipeHelper().registerCategory(new DefaultSmeltingCategory());
        RoughlyEnoughItemsCore.getRecipeHelper().registerCategory(new DefaultSmokingCategory());
        RoughlyEnoughItemsCore.getRecipeHelper().registerCategory(new DefaultBlastingCategory());
        RoughlyEnoughItemsCore.getRecipeHelper().registerCategory(new DefaultCampfireCategory());
        RoughlyEnoughItemsCore.getRecipeHelper().registerCategory(new DefaultStoneCuttingCategory());
        RoughlyEnoughItemsCore.getRecipeHelper().registerCategory(new DefaultBrewingCategory());
    }
    
    @Override
    public void registerRecipes() {
        for(Recipe recipe : RoughlyEnoughItemsCore.getRecipeHelper().getRecipeManager().values())
            if (recipe instanceof ShapelessRecipe)
                RoughlyEnoughItemsCore.getRecipeHelper().registerRecipe(CRAFTING, new DefaultShapelessDisplay((ShapelessRecipe) recipe));
            else if (recipe instanceof ShapedRecipe)
                RoughlyEnoughItemsCore.getRecipeHelper().registerRecipe(CRAFTING, new DefaultShapedDisplay((ShapedRecipe) recipe));
            else if (recipe instanceof SmeltingRecipe)
                RoughlyEnoughItemsCore.getRecipeHelper().registerRecipe(SMELTING, new DefaultSmeltingDisplay((SmeltingRecipe) recipe));
            else if (recipe instanceof SmokingRecipe)
                RoughlyEnoughItemsCore.getRecipeHelper().registerRecipe(SMOKING, new DefaultSmokingDisplay((SmokingRecipe) recipe));
            else if (recipe instanceof BlastingRecipe)
                RoughlyEnoughItemsCore.getRecipeHelper().registerRecipe(BLASTING, new DefaultBlastingDisplay((BlastingRecipe) recipe));
            else if (recipe instanceof CampfireCookingRecipe)
                RoughlyEnoughItemsCore.getRecipeHelper().registerRecipe(CAMPFIRE, new DefaultCampfireDisplay((CampfireCookingRecipe) recipe));
            else if (recipe instanceof StonecuttingRecipe)
                RoughlyEnoughItemsCore.getRecipeHelper().registerRecipe(STONE_CUTTING, new DefaultStoneCuttingDisplay((StonecuttingRecipe) recipe));
        BREWING_DISPLAYS.stream().forEachOrdered(display -> RoughlyEnoughItemsCore.getRecipeHelper().registerRecipe(BREWING, display));
    }
    
    @Override
    public void registerSpeedCraft() {
        RoughlyEnoughItemsCore.getRecipeHelper().registerSpeedCraftButtonArea(DefaultPlugin.CAMPFIRE, null);
        RoughlyEnoughItemsCore.getRecipeHelper().registerSpeedCraftButtonArea(DefaultPlugin.STONE_CUTTING, null);
        RoughlyEnoughItemsCore.getRecipeHelper().registerSpeedCraftButtonArea(DefaultPlugin.BREWING, null);
        RoughlyEnoughItemsCore.getRecipeHelper().registerSpeedCraftFunctional(DefaultPlugin.CRAFTING, new SpeedCraftFunctional<DefaultCraftingDisplay>() {
            @Override
            public Class[] getFunctioningFor() {
                return new Class[]{PlayerInventoryScreen.class, CraftingTableScreen.class};
            }
            
            @Override
            public boolean performAutoCraft(Screen screen, DefaultCraftingDisplay recipe) {
                if (screen.getClass().isAssignableFrom(CraftingTableScreen.class))
                    ((IMixinRecipeBookGui) (((CraftingTableScreen) screen).getRecipeBookGui())).getGhostSlots().reset();
                else if (screen.getClass().isAssignableFrom(PlayerInventoryScreen.class))
                    ((IMixinRecipeBookGui) (((PlayerInventoryScreen) screen).getRecipeBookGui())).getGhostSlots().reset();
                else
                    return false;
                MinecraftClient.getInstance().interactionManager.clickRecipe(MinecraftClient.getInstance().player.container.syncId, recipe.getRecipe(), Screen.isShiftPressed());
                return true;
            }
            
            @Override
            public boolean acceptRecipe(Screen screen, DefaultCraftingDisplay recipe) {
                return screen instanceof CraftingTableScreen || (screen instanceof PlayerInventoryScreen && recipe.getHeight() < 3 && recipe.getWidth() < 3);
            }
        });
        RoughlyEnoughItemsCore.getRecipeHelper().registerSpeedCraftFunctional(DefaultPlugin.SMELTING, new SpeedCraftFunctional<DefaultSmeltingDisplay>() {
            @Override
            public Class[] getFunctioningFor() {
                return new Class[]{FurnaceScreen.class};
            }
            
            @Override
            public boolean performAutoCraft(Screen screen, DefaultSmeltingDisplay recipe) {
                if (screen instanceof FurnaceScreen)
                    ((IMixinRecipeBookGui) (((FurnaceScreen) screen).getRecipeBookGui())).getGhostSlots().reset();
                else
                    return false;
                MinecraftClient.getInstance().interactionManager.clickRecipe(MinecraftClient.getInstance().player.container.syncId, recipe.getRecipe(), Screen.isShiftPressed());
                return true;
            }
            
            @Override
            public boolean acceptRecipe(Screen screen, DefaultSmeltingDisplay recipe) {
                return screen instanceof FurnaceScreen;
            }
        });
        RoughlyEnoughItemsCore.getRecipeHelper().registerSpeedCraftFunctional(DefaultPlugin.SMOKING, new SpeedCraftFunctional<DefaultSmokingDisplay>() {
            @Override
            public Class[] getFunctioningFor() {
                return new Class[]{SmokerScreen.class};
            }
            
            @Override
            public boolean performAutoCraft(Screen screen, DefaultSmokingDisplay recipe) {
                if (screen instanceof SmokerScreen)
                    ((IMixinRecipeBookGui) (((SmokerScreen) screen).getRecipeBookGui())).getGhostSlots().reset();
                else
                    return false;
                MinecraftClient.getInstance().interactionManager.clickRecipe(MinecraftClient.getInstance().player.container.syncId, recipe.getRecipe(), Screen.isShiftPressed());
                return true;
            }
            
            @Override
            public boolean acceptRecipe(Screen screen, DefaultSmokingDisplay recipe) {
                return screen instanceof SmokerScreen;
            }
        });
        RoughlyEnoughItemsCore.getRecipeHelper().registerSpeedCraftFunctional(DefaultPlugin.BLASTING, new SpeedCraftFunctional<DefaultBlastingDisplay>() {
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
                if (screen instanceof BlastFurnaceScreen)
                    ((IMixinRecipeBookGui) (((BlastFurnaceScreen) screen).getRecipeBookGui())).getGhostSlots().reset();
                else
                    return false;
                MinecraftClient.getInstance().interactionManager.clickRecipe(MinecraftClient.getInstance().player.container.syncId, recipe.getRecipe(), Screen.isShiftPressed());
                return true;
            }
        });
    }
    
    @Override
    public int getPriority() {
        return -1;
    }
    
}
