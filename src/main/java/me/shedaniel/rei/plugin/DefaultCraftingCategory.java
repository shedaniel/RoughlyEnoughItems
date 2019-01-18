package me.shedaniel.rei.plugin;

import com.google.common.collect.Lists;
import me.shedaniel.rei.api.IRecipeCategory;
import me.shedaniel.rei.gui.widget.IWidget;
import me.shedaniel.rei.gui.widget.ItemSlotWidget;
import me.shedaniel.rei.gui.widget.RecipeBaseWidget;
import me.shedaniel.rei.listeners.IMixinGuiContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.awt.*;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class DefaultCraftingCategory implements IRecipeCategory<DefaultCraftingDisplay> {
    
    private static final ResourceLocation DISPLAY_TEXTURE = new ResourceLocation("roughlyenoughitems", "textures/gui/display.png");
    
    @Override
    public ResourceLocation getResourceLocation() {
        return DefaultPlugin.CRAFTING;
    }
    
    @Override
    public ItemStack getCategoryIcon() {
        return new ItemStack(Blocks.CRAFTING_TABLE.asItem());
    }
    
    @Override
    public String getCategoryName() {
        return I18n.format("category.rei.crafting");
    }
    
    @Override
    public List<IWidget> setupDisplay(IMixinGuiContainer containerGui, DefaultCraftingDisplay recipeDisplay, Rectangle bounds) {
        Point startPoint = new Point((int) bounds.getCenterX() - 58, (int) bounds.getCenterY() - 27);
        List<IWidget> widgets = new LinkedList<>(Arrays.asList(new RecipeBaseWidget(bounds) {
            @Override
            public void draw(int mouseX, int mouseY, float partialTicks) {
                super.draw(mouseX, mouseY, partialTicks);
                GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
                RenderHelper.disableStandardItemLighting();
                Minecraft.getInstance().getTextureManager().bindTexture(DISPLAY_TEXTURE);
                drawTexturedModalRect(startPoint.x, startPoint.y, 0, 0, 116, 54);
            }
        }));
        List<List<ItemStack>> input = recipeDisplay.getInput();
        List<ItemSlotWidget> slots = Lists.newArrayList();
        for(int y = 0; y < 3; y++)
            for(int x = 0; x < 3; x++)
                slots.add(new ItemSlotWidget(startPoint.x + 1 + x * 18, startPoint.y + 1 + y * 18, Lists.newArrayList(), true, true, containerGui, true));
        for(int i = 0; i < input.size(); i++) {
            if (recipeDisplay instanceof DefaultShapedDisplay) {
                if (!input.get(i).isEmpty())
                    slots.get(getSlotWithSize(recipeDisplay, i)).setItemList(input.get(i));
            } else if (!input.get(i).isEmpty())
                slots.get(i).setItemList(input.get(i));
        }
        widgets.addAll(slots);
        widgets.add(new ItemSlotWidget(startPoint.x + 95, startPoint.y + 19, recipeDisplay.getOutput(), false, true, containerGui, true) {
            @Override
            protected String getItemCountOverlay(ItemStack currentStack) {
                if (currentStack.getCount() == 1)
                    return "";
                if (currentStack.getCount() < 1)
                    return "§c" + currentStack.getCount();
                return currentStack.getCount() + "";
            }
        });
        return widgets;
    }
    
    private int getSlotWithSize(DefaultCraftingDisplay recipeDisplay, int num) {
        if (recipeDisplay.getWidth() == 1) {
            if (num == 1)
                return 3;
            if (num == 2)
                return 6;
        }
        
        if (recipeDisplay.getWidth() == 2) {
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
    
}
