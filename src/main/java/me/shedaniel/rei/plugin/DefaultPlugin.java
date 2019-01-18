package me.shedaniel.rei.plugin;

import com.google.common.collect.Lists;
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
import net.minecraft.recipe.cooking.BlastingRecipe;
import net.minecraft.recipe.cooking.SmeltingRecipe;
import net.minecraft.recipe.cooking.SmokingRecipe;
import net.minecraft.recipe.crafting.ShapedRecipe;
import net.minecraft.recipe.crafting.ShapelessRecipe;
import net.minecraft.util.Identifier;

import java.util.List;

public class DefaultPlugin implements IRecipePlugin {
    
    static final Identifier CRAFTING = new Identifier("roughlyenoughitems", "plugins/crafting");
    static final Identifier SMELTING = new Identifier("roughlyenoughitems", "plugins/smelting");
    static final Identifier SMOKING = new Identifier("roughlyenoughitems", "plugins/smoking");
    static final Identifier BLASTING = new Identifier("roughlyenoughitems", "plugins/blasting");
    static final Identifier BREWING = new Identifier("roughlyenoughitems", "plugins/brewing");
    
    static final List<DefaultBrewingDisplay> BREWING_DISPLAYS = Lists.newArrayList();
    
    public static void registerBrewingDisplay(DefaultBrewingDisplay display) {
        BREWING_DISPLAYS.add(display);
    }
    
    @Override
    public void registerPluginCategories() {
        RecipeHelper.registerCategory(new DefaultCraftingCategory());
        RecipeHelper.registerCategory(new DefaultSmeltingCategory());
        RecipeHelper.registerCategory(new DefaultSmokingCategory());
        RecipeHelper.registerCategory(new DefaultBlastingCategory());
        RecipeHelper.registerCategory(new DefaultBrewingCategory());
    }
    
    @Override
    public void registerRecipes() {
        for(Recipe value : RecipeHelper.getRecipeManager().values())
            if (value instanceof ShapelessRecipe)
                RecipeHelper.registerRecipe(CRAFTING, new DefaultShapelessDisplay((ShapelessRecipe) value));
            else if (value instanceof ShapedRecipe)
                RecipeHelper.registerRecipe(CRAFTING, new DefaultShapedDisplay((ShapedRecipe) value));
            else if (value instanceof SmeltingRecipe)
                RecipeHelper.registerRecipe(SMELTING, new DefaultSmeltingDisplay((SmeltingRecipe) value));
            else if (value instanceof SmokingRecipe)
                RecipeHelper.registerRecipe(SMOKING, new DefaultSmokingDisplay((SmokingRecipe) value));
            else if (value instanceof BlastingRecipe)
                RecipeHelper.registerRecipe(BLASTING, new DefaultBlastingDisplay((BlastingRecipe) value));
        BREWING_DISPLAYS.forEach(display -> RecipeHelper.registerRecipe(BREWING, display));
    }
    
    @Override
    public void registerSpeedCraft() {
        RecipeHelper.registerSpeedCraftButtonArea(DefaultPlugin.BREWING, null);
        RecipeHelper.registerSpeedCraftFunctional(DefaultPlugin.CRAFTING, new SpeedCraftFunctional<DefaultCraftingDisplay>() {
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
                else return false;
                MinecraftClient.getInstance().interactionManager.clickRecipe(MinecraftClient.getInstance().player.container.syncId, recipe.getRecipe(), Gui.isShiftPressed());
                return true;
            }
            
            @Override
            public boolean acceptRecipe(Gui gui, DefaultCraftingDisplay recipe) {
                return gui instanceof CraftingTableGui || (gui instanceof PlayerInventoryGui && recipe.getHeight() < 3 && recipe.getWidth() < 3);
            }
        });
        RecipeHelper.registerSpeedCraftFunctional(DefaultPlugin.SMELTING, new SpeedCraftFunctional<DefaultSmeltingDisplay>() {
            @Override
            public Class[] getFunctioningFor() {
                return new Class[]{FurnaceGui.class};
            }
            
            @Override
            public boolean performAutoCraft(Gui gui, DefaultSmeltingDisplay recipe) {
                if (gui instanceof FurnaceGui)
                    ((IMixinRecipeBookGui) (((FurnaceGui) gui).getRecipeBookGui())).getGhostSlots().reset();
                else return false;
                MinecraftClient.getInstance().interactionManager.clickRecipe(MinecraftClient.getInstance().player.container.syncId, recipe.getRecipe(), Gui.isShiftPressed());
                return true;
            }
            
            @Override
            public boolean acceptRecipe(Gui gui, DefaultSmeltingDisplay recipe) {
                return gui instanceof FurnaceGui;
            }
        });
        RecipeHelper.registerSpeedCraftFunctional(DefaultPlugin.SMOKING, new SpeedCraftFunctional<DefaultSmokingDisplay>() {
            @Override
            public Class[] getFunctioningFor() {
                return new Class[]{SmokerGui.class};
            }
            
            @Override
            public boolean performAutoCraft(Gui gui, DefaultSmokingDisplay recipe) {
                if (gui instanceof SmokerGui)
                    ((IMixinRecipeBookGui) (((SmokerGui) gui).getRecipeBookGui())).getGhostSlots().reset();
                else return false;
                MinecraftClient.getInstance().interactionManager.clickRecipe(MinecraftClient.getInstance().player.container.syncId, recipe.getRecipe(), Gui.isShiftPressed());
                return true;
            }
            
            @Override
            public boolean acceptRecipe(Gui gui, DefaultSmokingDisplay recipe) {
                return gui instanceof SmokerGui;
            }
        });
        RecipeHelper.registerSpeedCraftFunctional(DefaultPlugin.BLASTING, new SpeedCraftFunctional<DefaultBlastingDisplay>() {
            @Override
            public Class[] getFunctioningFor() {
                return new Class[]{BlastFurnaceGui.class};
            }
            
            @Override
            public boolean performAutoCraft(Gui gui, DefaultBlastingDisplay recipe) {
                if (gui instanceof BlastFurnaceGui)
                    ((IMixinRecipeBookGui) (((BlastFurnaceGui) gui).getRecipeBookGui())).getGhostSlots().reset();
                else return false;
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
