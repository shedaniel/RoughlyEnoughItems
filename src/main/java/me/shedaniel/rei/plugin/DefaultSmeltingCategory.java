package me.shedaniel.rei.plugin;

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
import net.minecraft.util.math.MathHelper;

import java.awt.*;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class DefaultSmeltingCategory implements IRecipeCategory<DefaultSmeltingDisplay> {
    
    private static final ResourceLocation DISPLAY_TEXTURE = new ResourceLocation("roughlyenoughitems", "textures/gui/display.png");
    
    @Override
    public ResourceLocation getResourceLocation() {
        return DefaultPlugin.SMELTING;
    }
    
    @Override
    public ItemStack getCategoryIcon() {
        return new ItemStack(Blocks.FURNACE.asItem());
    }
    
    @Override
    public String getCategoryName() {
        return I18n.format("category.rei.smelting");
    }
    
    @Override
    public List<IWidget> setupDisplay(IMixinGuiContainer containerGui, DefaultSmeltingDisplay recipeDisplay, Rectangle bounds) {
        Point startPoint = new Point((int) bounds.getCenterX() - 41, (int) bounds.getCenterY() - 27);
        List<IWidget> widgets = new LinkedList<>(Arrays.asList(new RecipeBaseWidget(bounds) {
            @Override
            public void draw(int mouseX, int mouseY, float partialTicks) {
                super.draw(mouseX, mouseY, partialTicks);
                GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
                RenderHelper.disableStandardItemLighting();
                Minecraft.getInstance().getTextureManager().bindTexture(DISPLAY_TEXTURE);
                drawTexturedModalRect(startPoint.x, startPoint.y, 0, 54, 82, 54);
                int height = MathHelper.ceil((System.currentTimeMillis() / 250 % 14d) / 1f);
                drawTexturedModalRect(startPoint.x + 2, startPoint.y + 21 + (14 - height), 82, 77 + (14 - height), 14, height);
                int width = MathHelper.ceil((System.currentTimeMillis() / 250 % 24d) / 1f);
                drawTexturedModalRect(startPoint.x + 24, startPoint.y + 19, 82, 92, width, 17);
            }
        }));
        List<List<ItemStack>> input = recipeDisplay.getInput();
        widgets.add(new ItemSlotWidget(startPoint.x + 1, startPoint.y + 1, input.get(0), true, true, containerGui, true));
        widgets.add(new ItemSlotWidget(startPoint.x + 1, startPoint.y + 37, recipeDisplay.getFuel(), true, true, containerGui, true) {
            @Override
            protected List<String> getExtraToolTips(ItemStack stack) {
                return Arrays.asList(I18n.format("category.rei.smelting.fuel"));
            }
        });
        widgets.add(new ItemSlotWidget(startPoint.x + 61, startPoint.y + 19, recipeDisplay.getOutput(), false, true, containerGui, true));
        return widgets;
    }
    
}
