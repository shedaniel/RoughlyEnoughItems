package me.shedaniel.plugin.smoker;

import me.shedaniel.api.DisplayCategoryCraftable;
import me.shedaniel.api.IDisplayCategory;
import me.shedaniel.gui.RecipeGui;
import me.shedaniel.gui.widget.Control;
import me.shedaniel.gui.widget.REISlot;
import me.shedaniel.gui.widget.SmallButton;
import me.shedaniel.gui.widget.WidgetArrow;
import me.shedaniel.listenerdefinitions.IMixinRecipeBookGui;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.SmokerBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.container.FurnaceGui;
import net.minecraft.client.gui.container.SmokerGui;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class VanillaSmokerCategory implements DisplayCategoryCraftable<VanillaSmokerRecipe> {
    private List<VanillaSmokerRecipe> recipes;
    
    @Override
    public String getId() {
        return "smoker";
    }
    
    @Override
    public String getDisplayName() {
        return I18n.translate("category.rei.smoking");
    }
    
    @Override
    public void addRecipe(VanillaSmokerRecipe recipe) {
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
        fuelSlot.setExtraTooltip(I18n.translate("category.rei.smelting.fuel"));
        
        slots.add(inputSlot);
        slots.add(outputSlot);
        slots.add(fuelSlot);
        return slots;
    }
    
    @Override
    public boolean canDisplay(VanillaSmokerRecipe recipe) {
        return false;
    }
    
    @Override
    public void drawExtras() {
    
    }
    
    @Override
    public void addWidget(List<Control> controls, int number) {
        WidgetArrow wa = new WidgetArrow(75, 70 + number * 75, true, 10);
        controls.add(wa);
    }
    
    private List<ItemStack> getFuel() {
        return SmokerBlockEntity.createBurnableMap().keySet().stream().map(Item::getDefaultStack).collect(Collectors.toList());
    }
    
    @Override
    public ItemStack getCategoryIcon() {
        return new ItemStack(Blocks.SMOKER.getItem());
    }
    
    @Override
    public boolean canAutoCraftHere(Class<? extends Gui> guiClass, VanillaSmokerRecipe recipe) {
        return guiClass.isAssignableFrom(SmokerGui.class);
    }
    
    @Override
    public boolean performAutoCraft(Gui gui, VanillaSmokerRecipe recipe) {
        if (!gui.getClass().isAssignableFrom(SmokerGui.class))
            return false;
        ((IMixinRecipeBookGui) (((SmokerGui) gui).getRecipeBookGui())).getGhostSlots().reset();
        MinecraftClient.getInstance().interactionManager.clickRecipe(MinecraftClient.getInstance().player.container.syncId, recipe.getRecipe(), Gui.isShiftPressed());
        return false;
    }
    
    @Override
    public void registerAutoCraftButton(List<Control> control, RecipeGui recipeGui, Gui parentGui, VanillaSmokerRecipe recipe, int number) {
        SmallButton button = new SmallButton(128, 75 + 6 + 26 + number * 75, 10, 10, "+");
        button.setOnClick(mouse -> {
            recipeGui.close();
            MinecraftClient.getInstance().openGui(parentGui);
            return canAutoCraftHere(parentGui.getClass(), recipe) && performAutoCraft(parentGui, recipe);
        });
        button.setEnabled(canAutoCraftHere(parentGui.getClass(), recipe));
        control.add(button);
    }
    
}
