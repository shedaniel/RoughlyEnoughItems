package me.shedaniel.plugin.potion;

import com.mojang.blaze3d.platform.GlStateManager;
import me.shedaniel.api.IDisplayCategory;
import me.shedaniel.gui.REIRenderHelper;
import me.shedaniel.gui.widget.Control;
import me.shedaniel.gui.widget.REISlot;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;

import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class VanillaPotionCategory implements IDisplayCategory<VanillaPotionRecipe> {
    private List<VanillaPotionRecipe> recipes = new ArrayList<>();
    
    @Override
    public String getId() {
        return "potion";
    }
    
    @Override
    public String getDisplayName() {
        return I18n.translate("category.rei.brewing");
    }
    
    @Override
    public void addRecipe(VanillaPotionRecipe recipe) {
        recipes.add(recipe);
    }
    
    @Override
    public void resetRecipes() {
        recipes = new ArrayList<>();
    }
    
    @Override
    public List<REISlot> setupDisplay(int number) {
        List<REISlot> list = new LinkedList<>();
        REISlot blazePowderSlot = new REISlot(32, 62 + number * 75);
        blazePowderSlot.setDrawBackground(false);
        blazePowderSlot.setStack(new ItemStack(Items.BLAZE_POWDER));
        list.add(blazePowderSlot);
        REISlot inputSlot = new REISlot(30 + 41, 62 + number * 75);
        inputSlot.setDrawBackground(true);
        inputSlot.setStackList(recipes.get(number).getInput().get(0));
        list.add(inputSlot);
        REISlot reactWithSlot = new REISlot(30 + 64, 62 + number * 75);
        reactWithSlot.setDrawBackground(false);
        reactWithSlot.setStackList(recipes.get(number).getInput().get(1));
        list.add(reactWithSlot);
        REISlot outputSlotOne = new REISlot(30 + 41, 62 + 34 + number * 75);
        outputSlotOne.setDrawBackground(false);
        outputSlotOne.setStackList(recipes.get(number).getOutput(0));
        list.add(outputSlotOne);
        REISlot outputSlotTwo = new REISlot(30 + 64, 62 + 41 + number * 75);
        outputSlotTwo.setDrawBackground(false);
        outputSlotTwo.setStackList(recipes.get(number).getOutput(1));
        list.add(outputSlotTwo);
        return new LinkedList<>();
    }
    
    @Override
    public boolean canDisplay(VanillaPotionRecipe recipe) {
        return false;
    }
    
    @Override
    public void drawExtras() {
    
    }
    
    private static final Identifier RECIPE_GUI = new Identifier("textures/gui/container/brewing_stand.png");
    
    @Override
    public void addWidget(List<Control> controls, int number) {
        controls.add(new PotionScreen(30, 60 + number * 75));
        PotionSlot blazePowderSlot = new PotionSlot(32, 62 + number * 75);
        blazePowderSlot.setDrawBackground(false);
        blazePowderSlot.setExtraTooltip(getTooltip(SlotType.BLAZE_POWDER));
        blazePowderSlot.setStack(new ItemStack(Items.BLAZE_POWDER));
        controls.add(blazePowderSlot);
        PotionSlot inputSlot = new PotionSlot(30 + 41, 62 + number * 75);
        inputSlot.setDrawBackground(true);
        inputSlot.setExtraTooltip(getTooltip(SlotType.INPUT));
        inputSlot.setStackList(recipes.get(number).getInput().get(0));
        controls.add(inputSlot);
        PotionSlot reactWithSlot = new PotionSlot(30 + 63, 62 + number * 75);
        reactWithSlot.setDrawBackground(false);
        reactWithSlot.setExtraTooltip(getTooltip(SlotType.REACT));
        reactWithSlot.setStackList(recipes.get(number).getInput().get(1));
        controls.add(reactWithSlot);
        PotionSlot outputSlotOne = new PotionSlot(30 + 40, 62 + 34 + number * 75);
        outputSlotOne.setDrawBackground(false);
        outputSlotOne.setExtraTooltip(getTooltip(SlotType.OUTPUT));
        outputSlotOne.setDrawMiniBackground(true);
        outputSlotOne.setStackList(recipes.get(number).getOutput(0));
        controls.add(outputSlotOne);
        PotionSlot outputSlotTwo = new PotionSlot(30 + 63, 62 + 41 + number * 75);
        outputSlotTwo.setDrawBackground(false);
        outputSlotTwo.setExtraTooltip(getTooltip(SlotType.OUTPUT));
        outputSlotTwo.setDrawMiniBackground(true);
        outputSlotTwo.setStackList(recipes.get(number).getOutput(1));
        controls.add(outputSlotTwo);
        PotionSlot outputSlotThree = new PotionSlot(30 + 86, 62 + 34 + number * 75);
        outputSlotThree.setDrawBackground(false);
        outputSlotThree.setExtraTooltip(getTooltip(SlotType.OUTPUT));
        outputSlotThree.setDrawMiniBackground(true);
        outputSlotThree.setStackList(recipes.get(number).getOutput(2));
        controls.add(outputSlotThree);
    }
    
    @Override
    public ItemStack getCategoryIcon() {
        return new ItemStack(Blocks.BREWING_STAND.getItem());
    }
    
    private class PotionScreen extends Control {
        
        public PotionScreen(int x, int y) {
            super(x, y, 103, 60);
        }
        
        @Override
        public void draw() {
            GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.disableLighting();
            MinecraftClient.getInstance().getTextureManager().bindTexture(RECIPE_GUI);
            this.drawTexturedModalRect(rect.x, rect.y, 16, 15, 103, 60, 0);
            this.drawTexturedModalRect(rect.x + 97 - 16, rect.y + 16 - 15, 176, 0, 9, (int) (((double) System.currentTimeMillis() % 2800d / 2800d) * 28), 0);
            this.drawTexturedModalRect(rect.x + 45, rect.y, 110, 15, 15, 27, 0);
            this.drawTexturedModalRect(rect.x + 44, rect.y + 29, 176, 29, (int) (((double) System.currentTimeMillis() % 2800d / 2800d) * 18), 4, 0);
        }
    }
    
    private class PotionSlot extends REISlot {
        
        protected boolean drawMiniBackground = false;
        
        public PotionSlot(int x, int y) {
            super(x, y);
        }
        
        public void setDrawMiniBackground(boolean drawMiniBackground) {
            this.drawMiniBackground = drawMiniBackground;
        }
        
        @Override
        public void draw() {
            if (getStack().isEmpty())
                return;
            if (drawMiniBackground) {
                MinecraftClient.getInstance().getTextureManager().bindTexture(RECIPE_GUI);
                drawTexturedModalRect(rect.x + 1, rect.y + 1, 0 + 2, 222 + 2, rect.width - 4, rect.height - 4);
            }
            super.draw();
        }
        
        @Override
        protected void drawTooltip() {
            List<String> toolTip = getTooltip();
            toolTip.add(I18n.translate("text.rei.mod", getMod()));
            Point mouse = REIRenderHelper.getMouseLoc();
            MinecraftClient.getInstance().currentGui.drawTooltip(toolTip, mouse.x, mouse.y);
        }
    }
    
    public static String getTooltip(SlotType slotType) {
        switch (slotType) {
            case INPUT:
                return I18n.translate("category.rei.brewing.input");
            case REACT:
                return I18n.translate("category.rei.brewing.reactant");
            case OUTPUT:
                return I18n.translate("category.rei.brewing.result");
        }
        return null;
    }
    
    public enum SlotType {
        INPUT, REACT, OUTPUT, BLAZE_POWDER;
    }
}
