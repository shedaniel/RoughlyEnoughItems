package me.shedaniel.rei.plugin;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlStateManager;
import me.shedaniel.rei.api.IRecipeCategory;
import me.shedaniel.rei.api.IRecipeCategoryCraftable;
import me.shedaniel.rei.client.ClientHelper;
import me.shedaniel.rei.client.GuiHelper;
import me.shedaniel.rei.gui.widget.*;
import me.shedaniel.rei.listeners.IMixinContainerGui;
import me.shedaniel.rei.listeners.IMixinRecipeBookGui;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.ContainerGui;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.container.BlastFurnaceGui;
import net.minecraft.client.gui.container.FurnaceGui;
import net.minecraft.client.render.GuiLighting;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import java.awt.*;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class DefaultBlastingCategory implements IRecipeCategory<DefaultBlastingDisplay> {
    
    private static final Identifier DISPLAY_TEXTURE = new Identifier("roughlyenoughitems", "textures/gui/display.png");
    
    @Override
    public Identifier getIdentifier() {
        return DefaultPlugin.BLASTING;
    }
    
    @Override
    public ItemStack getCategoryIcon() {
        return new ItemStack(Blocks.BLAST_FURNACE.getItem());
    }
    
    @Override
    public String getCategoryName() {
        return I18n.translate("category.rei.blasting");
    }
    
    @Override
    public List<IWidget> setupDisplay(IMixinContainerGui containerGui, DefaultBlastingDisplay recipeDisplay, Rectangle bounds) {
        Point startPoint = new Point((int) bounds.getCenterX() - 41, (int) bounds.getCenterY() - 27);
        List<IWidget> widgets = new LinkedList<>(Arrays.asList(new RecipeBaseWidget(bounds) {
            @Override
            public void draw(int mouseX, int mouseY, float partialTicks) {
                super.draw(mouseX, mouseY, partialTicks);
                GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
                GuiLighting.disable();
                MinecraftClient.getInstance().getTextureManager().bindTexture(DISPLAY_TEXTURE);
                drawTexturedRect(startPoint.x, startPoint.y, 0, 54, 82, 54);
                int height = MathHelper.ceil((System.currentTimeMillis() / 250 % 14d) / 1f);
                drawTexturedRect(startPoint.x + 2, startPoint.y + 21 + (14 - height), 82, 77 + (14 - height), 14, height);
                int width = MathHelper.ceil((System.currentTimeMillis() / 250 % 24d) / 1f);
                drawTexturedRect(startPoint.x + 24, startPoint.y + 19, 82, 92, width, 17);
            }
        }));
        List<List<ItemStack>> input = recipeDisplay.getInput();
        widgets.add(new ItemSlotWidget(startPoint.x + 1, startPoint.y + 1, input.get(0), true, true, containerGui, true));
        widgets.add(new ItemSlotWidget(startPoint.x + 1, startPoint.y + 37, recipeDisplay.getFuel(), true, true, containerGui, true) {
            @Override
            protected List<String> getExtraToolTips(ItemStack stack) {
                return Arrays.asList(I18n.translate("category.rei.smelting.fuel"));
            }
        });
        widgets.add(new ItemSlotWidget(startPoint.x + 61, startPoint.y + 19, recipeDisplay.getOutput(), false, true, containerGui, true));
        return widgets;
    }
    
}
