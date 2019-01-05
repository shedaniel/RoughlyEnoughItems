package me.shedaniel.api;

import me.shedaniel.gui.widget.Control;
import me.shedaniel.gui.widget.REISlot;
import net.minecraft.client.gui.Gui;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.List;

/**
 * Created by James on 8/7/2018.
 */
public abstract class IDisplayCategory<T extends IRecipe> {
    public abstract String getId();
    
    public abstract String getDisplayName();
    
    public abstract void addRecipe(T recipe);
    
    public abstract void resetRecipes();
    
    public abstract List<REISlot> setupDisplay(int number);
    
    public abstract boolean canDisplay(T recipe);
    
    public abstract void drawExtras();
    
    public abstract void addWidget(List<Control> controls, int number);
    
    public abstract ItemStack getCategoryIcon();
    
    public boolean canAutoCraft(Class<? extends Gui> guiClass, T recipe) {
        return false;
    }
    
    public boolean performAutoCraft(Gui guiClass, T recipe) {
    
    }
    
}
