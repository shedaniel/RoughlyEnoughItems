package me.shedaniel.rei.plugin;

import com.google.common.collect.Lists;
import me.shedaniel.rei.RoughlyEnoughItemsCore;
import me.shedaniel.rei.api.IRecipePlugin;
import me.shedaniel.rei.api.SpeedCraftFunctional;
import me.shedaniel.rei.client.RecipeHelper;
import me.shedaniel.rei.listeners.IMixinRecipeBookGui;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.container.BlastFurnaceGui;
import net.minecraft.client.gui.container.CraftingTableGui;
import net.minecraft.client.gui.container.FurnaceGui;
import net.minecraft.client.gui.container.SmokerGui;
import net.minecraft.client.gui.ingame.PlayerInventoryGui;
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
                return new Class[]{PlayerInventoryGui.class, CraftingTableGui.class};
            }
            
            @Override
            public boolean performAutoCraft(Gui gui, DefaultCraftingDisplay recipe) {
                if (gui.getClass().isAssignableFrom(CraftingTableGui.class))
                    ((IMixinRecipeBookGui) (((CraftingTableGui) gui).getRecipeBookGui())).getGhostSlots().reset();
                else if (gui.getClass().isAssignableFrom(PlayerInventoryGui.class))
                    ((IMixinRecipeBookGui) (((PlayerInventoryGui) gui).getRecipeBookGui())).getGhostSlots().reset();
                else
                    return false;
                MinecraftClient.getInstance().interactionManager.clickRecipe(MinecraftClient.getInstance().player.container.syncId, recipe.getRecipe(), Gui.isShiftPressed());
                return true;
            }
            
            @Override
            public boolean acceptRecipe(Gui gui, DefaultCraftingDisplay recipe) {
                return gui instanceof CraftingTableGui || (gui instanceof PlayerInventoryGui && recipe.getHeight() < 3 && recipe.getWidth() < 3);
            }
        });
        RoughlyEnoughItemsCore.getRecipeHelper().registerSpeedCraftFunctional(DefaultPlugin.SMELTING, new SpeedCraftFunctional<DefaultSmeltingDisplay>() {
            @Override
            public Class[] getFunctioningFor() {
                return new Class[]{FurnaceGui.class};
            }
            
            @Override
            public boolean performAutoCraft(Gui gui, DefaultSmeltingDisplay recipe) {
                if (gui instanceof FurnaceGui)
                    ((IMixinRecipeBookGui) (((FurnaceGui) gui).getRecipeBookGui())).getGhostSlots().reset();
                else
                    return false;
                MinecraftClient.getInstance().interactionManager.clickRecipe(MinecraftClient.getInstance().player.container.syncId, recipe.getRecipe(), Gui.isShiftPressed());
                return true;
            }
            
            @Override
            public boolean acceptRecipe(Gui gui, DefaultSmeltingDisplay recipe) {
                return gui instanceof FurnaceGui;
            }
        });
        RoughlyEnoughItemsCore.getRecipeHelper().registerSpeedCraftFunctional(DefaultPlugin.SMOKING, new SpeedCraftFunctional<DefaultSmokingDisplay>() {
            @Override
            public Class[] getFunctioningFor() {
                return new Class[]{SmokerGui.class};
            }
            
            @Override
            public boolean performAutoCraft(Gui gui, DefaultSmokingDisplay recipe) {
                if (gui instanceof SmokerGui)
                    ((IMixinRecipeBookGui) (((SmokerGui) gui).getRecipeBookGui())).getGhostSlots().reset();
                else
                    return false;
                MinecraftClient.getInstance().interactionManager.clickRecipe(MinecraftClient.getInstance().player.container.syncId, recipe.getRecipe(), Gui.isShiftPressed());
                return true;
            }
            
            @Override
            public boolean acceptRecipe(Gui gui, DefaultSmokingDisplay recipe) {
                return gui instanceof SmokerGui;
            }
        });
        RoughlyEnoughItemsCore.getRecipeHelper().registerSpeedCraftFunctional(DefaultPlugin.BLASTING, new SpeedCraftFunctional<DefaultBlastingDisplay>() {
            @Override
            public Class[] getFunctioningFor() {
                return new Class[]{BlastFurnaceGui.class};
            }
            
            @Override
            public boolean performAutoCraft(Gui gui, DefaultBlastingDisplay recipe) {
                if (gui instanceof BlastFurnaceGui)
                    ((IMixinRecipeBookGui) (((BlastFurnaceGui) gui).getRecipeBookGui())).getGhostSlots().reset();
                else
                    return false;
                MinecraftClient.getInstance().interactionManager.clickRecipe(MinecraftClient.getInstance().player.container.syncId, recipe.getRecipe(), Gui.isShiftPressed());
                return true;
            }
            
            @Override
            public boolean acceptRecipe(Gui gui, DefaultBlastingDisplay recipe) {
                return gui instanceof BlastFurnaceGui;
            }
        });
    }
    
    @Override
    public int getPriority() {
        return -1;
    }
    
}
