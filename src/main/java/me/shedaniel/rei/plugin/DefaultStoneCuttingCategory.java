package me.shedaniel.rei.plugin;

import com.mojang.blaze3d.platform.GlStateManager;
import me.shedaniel.rei.api.RecipeCategory;
import me.shedaniel.rei.gui.widget.ItemSlotWidget;
import me.shedaniel.rei.gui.widget.RecipeBaseWidget;
import me.shedaniel.rei.gui.widget.Widget;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GuiLighting;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

import java.awt.*;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

public class DefaultStoneCuttingCategory implements RecipeCategory<DefaultStoneCuttingDisplay> {
    
    private static final Identifier DISPLAY_TEXTURE = new Identifier("roughlyenoughitems", "textures/gui/display.png");
    
    @Override
    public Identifier getIdentifier() {
        return DefaultPlugin.STONE_CUTTING;
    }
    
    @Override
    public ItemStack getCategoryIcon() {
        return new ItemStack(Blocks.STONECUTTER);
    }
    
    @Override
    public String getCategoryName() {
        return I18n.translate("category.rei.stone_cutting");
    }
    
    @Override
    public List<Widget> setupDisplay(Supplier<DefaultStoneCuttingDisplay> recipeDisplaySupplier, Rectangle bounds) {
        Point startPoint = new Point((int) bounds.getCenterX() - 41, (int) bounds.getCenterY() - 13);
        List<Widget> widgets = new LinkedList<>(Arrays.asList(new RecipeBaseWidget(bounds) {
            @Override
            public void draw(int mouseX, int mouseY, float partialTicks) {
                super.draw(mouseX, mouseY, partialTicks);
                GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
                GuiLighting.disable();
                MinecraftClient.getInstance().getTextureManager().bindTexture(DISPLAY_TEXTURE);
                drawTexturedRect(startPoint.x, startPoint.y, 0, 221, 82, 26);
            }
        }));
        widgets.add(new ItemSlotWidget(startPoint.x + 4, startPoint.y + 5, recipeDisplaySupplier.get().getInput().get(0), true, true, true));
        widgets.add(new ItemSlotWidget(startPoint.x + 61, startPoint.y + 5, recipeDisplaySupplier.get().getOutput(), false, true, true));
        return widgets;
    }
    
}
