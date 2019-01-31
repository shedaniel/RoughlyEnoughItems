package me.shedaniel.rei.plugin;

import com.google.common.collect.Lists;
import me.shedaniel.rei.api.IRecipePlugin;
import me.shedaniel.rei.api.SpeedCraftFunctional;
import me.shedaniel.rei.client.RecipeHelper;
import me.shedaniel.rei.listeners.IMixinRecipeBookGui;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiCrafting;
import net.minecraft.client.gui.inventory.GuiFurnace;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.item.crafting.FurnaceRecipe;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapedRecipe;
import net.minecraft.item.crafting.ShapelessRecipe;
import net.minecraft.util.ResourceLocation;

import java.util.List;

public class DefaultPlugin implements IRecipePlugin {
    
    static final ResourceLocation CRAFTING = new ResourceLocation("roughlyenoughitems", "plugins/crafting");
    static final ResourceLocation SMELTING = new ResourceLocation("roughlyenoughitems", "plugins/smelting");
    static final ResourceLocation BREWING = new ResourceLocation("roughlyenoughitems", "plugins/brewing");
    
    static final List<DefaultBrewingDisplay> BREWING_DISPLAYS = Lists.newArrayList();
    
    public static void registerBrewingDisplay(DefaultBrewingDisplay display) {
        BREWING_DISPLAYS.add(display);
    }
    
    @Override
    public void registerPluginCategories() {
        RecipeHelper.getInstance().registerCategory(new DefaultCraftingCategory());
        RecipeHelper.getInstance().registerCategory(new DefaultSmeltingCategory());
        RecipeHelper.getInstance().registerCategory(new DefaultBrewingCategory());
    }
    
    @Override
    public void registerRecipes() {
        for(IRecipe value : RecipeHelper.getInstance().getRecipeManager().getRecipes())
            if (value instanceof ShapelessRecipe)
                RecipeHelper.getInstance().registerRecipe(CRAFTING, new DefaultShapelessDisplay((ShapelessRecipe) value));
            else if (value instanceof ShapedRecipe)
                RecipeHelper.getInstance().registerRecipe(CRAFTING, new DefaultShapedDisplay((ShapedRecipe) value));
            else if (value instanceof FurnaceRecipe)
                RecipeHelper.getInstance().registerRecipe(SMELTING, new DefaultSmeltingDisplay((FurnaceRecipe) value));
        BREWING_DISPLAYS.forEach(display -> RecipeHelper.getInstance().registerRecipe(BREWING, display));
    }
    
    @Override
    public void registerSpeedCraft() {
        RecipeHelper.getInstance().registerSpeedCraftButtonArea(DefaultPlugin.BREWING, null);
        RecipeHelper.getInstance().registerSpeedCraftFunctional(DefaultPlugin.CRAFTING, new SpeedCraftFunctional<DefaultCraftingDisplay>() {
            @Override
            public Class[] getFunctioningFor() {
                return new Class[]{GuiInventory.class, GuiCrafting.class};
            }
            
            @Override
            public boolean performAutoCraft(Gui gui, DefaultCraftingDisplay recipe) {
                if (gui.getClass().isAssignableFrom(GuiCrafting.class))
                    ((IMixinRecipeBookGui) (((GuiCrafting) gui).func_194310_f())).getGhostRecipe().clear();
                else if (gui.getClass().isAssignableFrom(GuiInventory.class))
                    ((IMixinRecipeBookGui) (((GuiInventory) gui).func_194310_f())).getGhostRecipe().clear();
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
        RecipeHelper.getInstance().registerSpeedCraftFunctional(DefaultPlugin.SMELTING, new SpeedCraftFunctional<DefaultSmeltingDisplay>() {
            @Override
            public Class[] getFunctioningFor() {
                return new Class[]{GuiFurnace.class};
            }
            
            @Override
            public boolean performAutoCraft(Gui gui, DefaultSmeltingDisplay recipe) {
                if (gui instanceof GuiFurnace)
                    ((IMixinRecipeBookGui) (((GuiFurnace) gui).func_194310_f())).getGhostRecipe().clear();
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
