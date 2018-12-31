package me.shedaniel.plugin;

import me.shedaniel.api.IDisplayCategory;
import me.shedaniel.gui.widget.Control;
import me.shedaniel.gui.widget.REISlot;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class TestRandomCategory implements IDisplayCategory<RandomRecipe> {
    
    private String id;
    private List<RandomRecipe> recipes;
    private ItemStack item;
    
    public TestRandomCategory(String id, ItemStack item) {
        this.id = id;
        this.item = item;
    }
    
    @Override
    public String getId() {
        return id;
    }
    
    @Override
    public String getDisplayName() {
        return id;
    }
    
    @Override
    public void addRecipe(RandomRecipe recipe) {
        if (this.recipes == null)
            this.recipes = new ArrayList<>();
        this.recipes.add(recipe);
    }
    
    @Override
    public void resetRecipes() {
        this.recipes = new ArrayList<>();
    }
    
    @Override
    public List<REISlot> setupDisplay(int number) {
        return new LinkedList<>();
    }
    
    @Override
    public boolean canDisplay(RandomRecipe recipe) {
        return false;
    }
    
    @Override
    public void drawExtras() {
    
    }
    
    @Override
    public void addWidget(List<Control> controls, int number) {
    
    }
    
    @Override
    public ItemStack getCategoryIcon() {
        return item;
    }
}
