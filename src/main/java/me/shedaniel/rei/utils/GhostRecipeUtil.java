package me.shedaniel.rei.utils;

import net.minecraft.client.gui.inventory.GuiCrafting;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.gui.recipebook.GhostRecipe;
import net.minecraft.client.gui.recipebook.GuiRecipeBook;

public class GhostRecipeUtil {
    
    public static GhostRecipe fromGuiCrafting(GuiCrafting gui) {
        return ReflectionUtils.getField(gui.func_194310_f(), GhostRecipe.class, "ghostRecipe", "field_191915_z").orElseThrow(NullPointerException::new);
    }
    
    public static GhostRecipe fromGuiInventory(GuiInventory gui) {
        return ReflectionUtils.getField(gui.func_194310_f(), GhostRecipe.class, "ghostRecipe", "field_191915_z").orElseThrow(NullPointerException::new);
    }
    
    public static GhostRecipe fromGuiRecipeBook(GuiRecipeBook gui) {
        return ReflectionUtils.getField(gui, GhostRecipe.class, "ghostRecipe", "field_191915_z").orElseThrow(NullPointerException::new);
    }
    
}
