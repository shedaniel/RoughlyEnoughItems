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
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

import java.awt.*;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

public class DefaultBrewingCategory implements IRecipeCategory<DefaultBrewingDisplay> {
    
    private static final ResourceLocation DISPLAY_TEXTURE = new ResourceLocation("roughlyenoughitems", "textures/gui/display.png");
    
    @Override
    public ResourceLocation getResourceLocation() {
        return DefaultPlugin.BREWING;
    }
    
    @Override
    public ItemStack getCategoryIcon() {
        return new ItemStack(Blocks.BREWING_STAND);
    }
    
    @Override
    public String getCategoryName() {
        return I18n.format("category.rei.brewing");
    }
    
    @Override
    public List<IWidget> setupDisplay(Supplier<DefaultBrewingDisplay> recipeDisplaySupplier, Rectangle bounds) {
        Point startPoint = new Point((int) bounds.getCenterX() - 52, (int) bounds.getCenterY() - 29);
        List<IWidget> widgets = new LinkedList<>(Arrays.asList(new RecipeBaseWidget(bounds) {
            @Override
            public void draw(int mouseX, int mouseY, float partialTicks) {
                super.draw(mouseX, mouseY, partialTicks);
                GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
                RenderHelper.disableStandardItemLighting();
                Minecraft.getInstance().getTextureManager().bindTexture(DISPLAY_TEXTURE);
                drawTexturedModalRect(startPoint.x, startPoint.y, 0, 108, 103, 59);
                int width = MathHelper.ceil((System.currentTimeMillis() / 250 % 18d) / 1f);
                drawTexturedModalRect(startPoint.x + 44, startPoint.y + 28, 103, 163, width, 4);
            }
        }));
        widgets.add(new ItemSlotWidget(startPoint.x + 1, startPoint.y + 1, Arrays.asList(new ItemStack(Items.BLAZE_POWDER)), false, true, true));
        widgets.add(new ItemSlotWidget(startPoint.x + 63, startPoint.y + 1, recipeDisplaySupplier.get().getInput().get(0), false, true, true) {
            @Override
            protected List<String> getExtraToolTips(ItemStack stack) {
                return Arrays.asList(I18n.format("category.rei.brewing.input"));
            }
        });
        widgets.add(new ItemSlotWidget(startPoint.x + 40, startPoint.y + 1, recipeDisplaySupplier.get().getInput().get(1), false, true, true) {
            @Override
            protected List<String> getExtraToolTips(ItemStack stack) {
                return Arrays.asList(I18n.format("category.rei.brewing.reactant"));
            }
        });
        widgets.add(new ItemSlotWidget(startPoint.x + 40, startPoint.y + 35, recipeDisplaySupplier.get().getOutput(0), false, true, true) {
            @Override
            protected List<String> getExtraToolTips(ItemStack stack) {
                return Arrays.asList(I18n.format("category.rei.brewing.result"));
            }
        });
        widgets.add(new ItemSlotWidget(startPoint.x + 63, startPoint.y + 42, recipeDisplaySupplier.get().getOutput(1), false, true, true) {
            @Override
            protected List<String> getExtraToolTips(ItemStack stack) {
                return Arrays.asList(I18n.format("category.rei.brewing.result"));
            }
        });
        widgets.add(new ItemSlotWidget(startPoint.x + 86, startPoint.y + 35, recipeDisplaySupplier.get().getOutput(2), false, true, true) {
            @Override
            protected List<String> getExtraToolTips(ItemStack stack) {
                return Arrays.asList(I18n.format("category.rei.brewing.result"));
            }
        });
        return widgets;
    }
    
}
