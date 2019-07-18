package me.shedaniel.rei.mixin;

import me.shedaniel.rei.api.RecipeBookGuiHooks;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.recipebook.GhostRecipe;
import net.minecraft.client.gui.recipebook.GuiButtonRecipeTab;
import net.minecraft.client.gui.recipebook.GuiRecipeBook;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(GuiRecipeBook.class)
public class MixinGuiRecipeBook implements RecipeBookGuiHooks {
    @Shadow @Final protected GhostRecipe ghostRecipe;
    
    @Shadow private GuiTextField searchBar;
    
    @Shadow @Final private List<GuiButtonRecipeTab> recipeTabs;
    
    @Override
    public GhostRecipe rei_getGhostSlots() {
        return ghostRecipe;
    }
    
    @Override
    public GuiTextField rei_getSearchField() {
        return searchBar;
    }
    
    @Override
    public List<GuiButtonRecipeTab> rei_getTabButtons() {
        return recipeTabs;
    }
}
