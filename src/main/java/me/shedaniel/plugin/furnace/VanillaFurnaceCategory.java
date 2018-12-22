package me.shedaniel.plugin.furnace;

import me.shedaniel.api.IDisplayCategory;
import me.shedaniel.gui.widget.AEISlot;
import me.shedaniel.gui.widget.Control;
import me.shedaniel.gui.widget.WidgetArrow;
import me.shedaniel.plugin.crafting.VanillaCraftingRecipe;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityFurnace;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class VanillaFurnaceCategory implements IDisplayCategory<VanillaFurnaceRecipe> {
    private List<VanillaFurnaceRecipe> recipes;
    
    @Override
    public String getId() {
        return "furnace";
    }
    
    @Override
    public String getDisplayName() {
        return "Smelting";
    }
    
    @Override
    public void addRecipe(VanillaFurnaceRecipe recipe) {
        if (this.recipes == null)
            this.recipes = new ArrayList<>();
        this.recipes.add(recipe);
    }
    
    @Override
    public void resetRecipes() {
        this.recipes = new ArrayList<>();
    }
    
    @Override
    public List<AEISlot> setupDisplay(int number) {
        List<AEISlot> slots = new LinkedList<>();
        AEISlot inputSlot = new AEISlot(50, 70 + number * 75);
        inputSlot.setStackList(recipes.get(number).getInput().get(0));
        inputSlot.setDrawBackground(true);
        
        AEISlot outputSlot = new AEISlot(110, 70 + number * 75);
        outputSlot.setStackList(recipes.get(number).getOutput());
        outputSlot.setDrawBackground(true);
        
        AEISlot fuelSlot = new AEISlot(80, 100 + number * 75);
        fuelSlot.setStackList(getFuel());
        fuelSlot.setDrawBackground(true);
        fuelSlot.setExtraTooltip("Fuel");
        
        slots.add(inputSlot);
        slots.add(outputSlot);
        slots.add(fuelSlot);
        return slots;
    }
    
    @Override
    public boolean canDisplay(VanillaFurnaceRecipe recipe) {
        return false;
    }
    
    @Override
    public void drawExtras() {
    
    }
    
    @Override
    public void addWidget(List<Control> controls, int number) {
        WidgetArrow wa = new WidgetArrow(75, 70 + number * 75, true);
        controls.add(wa);
    }
    
    private List<ItemStack> getFuel() {
        return TileEntityFurnace.getBurnTimes().keySet().stream().map(Item::getDefaultInstance).collect(Collectors.toList());
    }
}
