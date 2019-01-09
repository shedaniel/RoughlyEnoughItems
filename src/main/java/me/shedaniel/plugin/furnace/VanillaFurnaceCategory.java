package me.shedaniel.plugin.furnace;

import me.shedaniel.api.IDisplayCategoryCraftable;
import me.shedaniel.gui.RecipeGui;
import me.shedaniel.gui.widget.Control;
import me.shedaniel.gui.widget.REISlot;
import me.shedaniel.gui.widget.SmallButton;
import me.shedaniel.gui.widget.WidgetArrow;
import me.shedaniel.listenerdefinitions.IMixinGuiRecipeBook;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiFurnace;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityFurnace;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class VanillaFurnaceCategory implements IDisplayCategoryCraftable<VanillaFurnaceRecipe> {
    
    private List<VanillaFurnaceRecipe> recipes;
    
    @Override
    public String getId() {
        return "furnace";
    }
    
    @Override
    public String getDisplayName() {
        return I18n.format("category.rei.smelting");
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
    public List<REISlot> setupDisplay(int number) {
        List<REISlot> slots = new LinkedList<>();
        REISlot inputSlot = new REISlot(50, 70 + number * 75);
        inputSlot.setStackList(recipes.get(number).getInput().get(0));
        inputSlot.setDrawBackground(true);
        
        REISlot outputSlot = new REISlot(110, 70 + number * 75);
        outputSlot.setStackList(recipes.get(number).getOutput());
        outputSlot.setDrawBackground(true);
        
        REISlot fuelSlot = new REISlot(80, 100 + number * 75);
        fuelSlot.setStackList(getFuel());
        fuelSlot.setDrawBackground(true);
        fuelSlot.setExtraTooltip(I18n.format("category.rei.smelting.fuel"));
        
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
    
    @Override
    public ItemStack getCategoryIcon() {
        return new ItemStack(Blocks.FURNACE.asItem());
    }
    
    @Override
    public boolean canAutoCraftHere(Class<? extends GuiScreen> guiClass, VanillaFurnaceRecipe recipe) {
        return guiClass.isAssignableFrom(GuiFurnace.class);
    }
    
    @Override
    public boolean performAutoCraft(GuiScreen gui, VanillaFurnaceRecipe recipe) {
        if (!gui.getClass().isAssignableFrom(GuiFurnace.class))
            return false;
        ((IMixinGuiRecipeBook) (((GuiFurnace) gui).recipeBook)).getGhostSlots().clear();
        Minecraft.getInstance().playerController.func_203413_a(Minecraft.getInstance().player.openContainer.windowId, recipe.getRecipe(), GuiScreen.isShiftKeyDown());
        return true;
    }
    
    @Override
    public void registerAutoCraftButton(List<Control> control, RecipeGui recipeGui, GuiScreen parentGui, VanillaFurnaceRecipe recipe, int number) {
        SmallButton button = new SmallButton(128, 75 + 6 + 26 + number * 75, 10, 10, "+", enabled -> {
            if (!(parentGui instanceof GuiFurnace))
                return I18n.format("text.auto_craft.wrong_gui");
            return "";
        });
        button.setOnClick(mouse -> {
            recipeGui.close();
            Minecraft.getInstance().displayGuiScreen(parentGui);
            return canAutoCraftHere(parentGui.getClass(), recipe) && performAutoCraft(parentGui, recipe);
        });
        button.setEnabled(canAutoCraftHere(parentGui.getClass(), recipe));
        control.add(button);
    }
    
}
