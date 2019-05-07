package me.shedaniel.rei.mixin;

import me.shedaniel.rei.listeners.RecipeBookGuiHooks;
import net.minecraft.client.gui.recipebook.GroupButtonWidget;
import net.minecraft.client.gui.recipebook.RecipeBookGui;
import net.minecraft.client.gui.widget.RecipeBookGhostSlots;
import net.minecraft.client.gui.widget.TextFieldWidget;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(RecipeBookGui.class)
public class MixinRecipeBookGui implements RecipeBookGuiHooks {
    
    @Shadow @Final protected RecipeBookGhostSlots ghostSlots;
    
    @Shadow private TextFieldWidget searchField;
    
    @Shadow @Final private List<GroupButtonWidget> tabButtons;
    
    public RecipeBookGhostSlots rei_getGhostSlots() {
        return ghostSlots;
    }
    
    @Override
    public TextFieldWidget rei_getSearchField() {
        return searchField;
    }
    
    @Override
    public List<GroupButtonWidget> rei_getTabButtons() {
        return tabButtons;
    }
    
    
}
