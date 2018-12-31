package me.shedaniel.plugin.crafting;

import me.shedaniel.api.IDisplayCategory;
import me.shedaniel.gui.widget.REISlot;
import me.shedaniel.gui.widget.Control;
import me.shedaniel.gui.widget.WidgetArrow;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class VanillaCraftingCategory implements IDisplayCategory<VanillaCraftingRecipe> {
    MainWindow mainWindow = Minecraft.getInstance().mainWindow;
    private List<VanillaCraftingRecipe> recipes;
    
    @Override
    public String getId() {
        return "vanilla";
    }
    
    @Override
    public String getDisplayName() {
        return I18n.format("category.rei.crafting");
    }
    
    @Override
    public void addRecipe(VanillaCraftingRecipe recipe) {
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
        List<REISlot> slots = new LinkedList<>();
        int count = 0;
        List<List<ItemStack>> input = recipes.get(number).getInput();
        for(int y = 0; y < 3; y++) {
            for(int x = 0; x < 3; x++) {
                REISlot slot = new REISlot(20 + x * 18, 75 + y * 18 + number * 75);
                slot.setDrawBackground(true);
                slots.add(slot);
                count++;
            }
        }
        for(int i = 0; i < input.size(); i++) {
            if (recipes.get(number) instanceof VanillaShapedCraftingRecipe) {
                if (!input.get(i).isEmpty())
                    slots.get(getSlotWithSize(number, i)).setStackList(input.get(i));
            } else if (!input.get(i).isEmpty())
                slots.get(i).setStackList(input.get(i));
        }
        REISlot slot = new REISlot(130, 75 + 18 + number * 75) {
            @Override
            public String getTextOverlay(ItemStack stack) {
                if (stack.getCount() == 1)
                    return "";
                return stack.getCount() + "";
            }
        };
        
        slot.setDrawBackground(true);
        slot.setStack(recipes.get(number).getOutput().get(0).copy());
        slots.add(slot);
        return slots;
    }
    
    @Override
    public boolean canDisplay(VanillaCraftingRecipe recipe) {
        return false;
    }
    
    @Override
    public void drawExtras() {
    
    }
    
    @Override
    public void addWidget(List<Control> controls, int number) {
        WidgetArrow wa = new WidgetArrow(90, 70 + 22 + number * 75, false);
        controls.add(wa);
    }
    
    private int getSlotWithSize(int number, int num) {
        if (recipes.get(number).getWidth() == 1) {
            if (num == 1)
                return 3;
            if (num == 2)
                return 6;
        }
        
        if (recipes.get(number).getWidth() == 2) {
            if (num == 2)
                return 3;
            if (num == 3)
                return 4;
            if (num == 4)
                return 6;
            if (num == 5)
                return 7;
            
        }
        return num;
    }
    
    @Override
    public ItemStack getCategoryIcon() {
        return new ItemStack(Blocks.CRAFTING_TABLE.asItem());
    }
    
}
