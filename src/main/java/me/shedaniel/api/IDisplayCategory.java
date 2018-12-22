package me.shedaniel.api;

import me.shedaniel.gui.widget.AEISlot;
import me.shedaniel.gui.widget.Control;

import java.util.List;

/**
 * Created by James on 8/7/2018.
 */
public interface IDisplayCategory<T extends IRecipe> {
    public String getId();
    
    public String getDisplayName();
    
    public void addRecipe(T recipe);
    
    public void resetRecipes();
    
    public List<AEISlot> setupDisplay(int number);
    
    public boolean canDisplay(T recipe);
    
    public void drawExtras();
    
    public void addWidget(List<Control> controls, int number);
}
