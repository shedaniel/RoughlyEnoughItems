package me.shedaniel.plugin.crafting;

import me.shedaniel.api.IDisplayCategoryCraftable;
import me.shedaniel.gui.RecipeGui;
import me.shedaniel.gui.widget.*;
import me.shedaniel.listenerdefinitions.IMixinRecipeBookGui;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.container.CraftingTableGui;
import net.minecraft.client.gui.ingame.PlayerInventoryGui;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.Window;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class VanillaCraftingCategory implements IDisplayCategoryCraftable<VanillaCraftingRecipe> {
    Window mainWindow = MinecraftClient.getInstance().window;
    private List<VanillaCraftingRecipe> recipes;
    
    @Override
    public String getId() {
        return "vanilla";
    }
    
    @Override
    public String getDisplayName() {
        return I18n.translate("category.rei.crafting");
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
                if (stack.getAmount() == 1)
                    return "";
                return stack.getAmount() + "";
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
        return new ItemStack(Blocks.CRAFTING_TABLE.getItem());
    }
    
    @Override
    public boolean canAutoCraftHere(Class<? extends Gui> guiClass, VanillaCraftingRecipe recipe) {
        return guiClass.isAssignableFrom(CraftingTableGui.class) || (guiClass.isAssignableFrom(PlayerInventoryGui.class) && recipe.getHeight() < 3 && recipe.getWidth() < 3);
    }
    
    @Override
    public boolean performAutoCraft(Gui gui, VanillaCraftingRecipe recipe) {
        if (gui.getClass().isAssignableFrom(CraftingTableGui.class))
            ((IMixinRecipeBookGui) (((CraftingTableGui) gui).getRecipeBookGui())).getGhostSlots().reset();
        else if (gui.getClass().isAssignableFrom(PlayerInventoryGui.class))
            ((IMixinRecipeBookGui) (((PlayerInventoryGui) gui).getRecipeBookGui())).getGhostSlots().reset();
        else return false;
        MinecraftClient.getInstance().interactionManager.clickRecipe(MinecraftClient.getInstance().player.container.syncId, recipe.getRecipe(), Gui.isShiftPressed());
        return true;
    }
    
    @Override
    public void registerAutoCraftButton(List<Control> control, RecipeGui recipeGui, Gui parentGui, VanillaCraftingRecipe recipe, int number) {
        SmallButton button = new SmallButton(78, 75 + 6 + 36 + number * 75, 10, 10, "+", enabled -> {
            if (!(parentGui instanceof CraftingTableGui || parentGui instanceof PlayerInventoryGui))
                return I18n.translate("text.auto_craft.wrong_gui");
            if (parentGui instanceof PlayerInventoryGui && !(recipe.getHeight() < 3 && recipe.getWidth() < 3))
                return I18n.translate("text.auto_craft.crafting.too_small");
            return "";
        });
        button.setOnClick(mouse -> {
            recipeGui.close();
            MinecraftClient.getInstance().openGui(parentGui);
            return canAutoCraftHere(parentGui.getClass(), recipe) && performAutoCraft(parentGui, recipe);
        });
        button.setEnabled(canAutoCraftHere(parentGui.getClass(), recipe));
        control.add(button);
    }
    
}
