/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.plugin;

import com.mojang.blaze3d.platform.GlStateManager;
import me.shedaniel.rei.api.RecipeCategory;
import me.shedaniel.rei.api.Renderable;
import me.shedaniel.rei.api.Renderer;
import me.shedaniel.rei.gui.renderables.RecipeRenderer;
import me.shedaniel.rei.gui.widget.RecipeBaseWidget;
import me.shedaniel.rei.gui.widget.SlotWidget;
import me.shedaniel.rei.gui.widget.Widget;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GuiLighting;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import java.awt.*;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

public class DefaultBlastingCategory implements RecipeCategory<DefaultBlastingDisplay> {
    
    private static final Identifier DISPLAY_TEXTURE = new Identifier("roughlyenoughitems", "textures/gui/display.png");
    
    @Override
    public Identifier getIdentifier() {
        return DefaultPlugin.BLASTING;
    }
    
    @Override
    public Renderer getIcon() {
        return Renderable.fromItemStack(new ItemStack(Blocks.BLAST_FURNACE));
    }
    
    @Override
    public String getCategoryName() {
        return I18n.translate("category.rei.blasting");
    }
    
    @Override
    public RecipeRenderer getSimpleRenderer(DefaultBlastingDisplay recipe) {
        return Renderable.fromRecipe(() -> Arrays.asList(recipe.getInput().get(0)), recipe::getOutput);
    }
    
    @Override
    public List<Widget> setupDisplay(Supplier<DefaultBlastingDisplay> recipeDisplaySupplier, Rectangle bounds) {
        final DefaultBlastingDisplay recipeDisplay = recipeDisplaySupplier.get();
        Point startPoint = new Point((int) bounds.getCenterX() - 41, (int) bounds.getCenterY() - 27);
        List<Widget> widgets = new LinkedList<>(Arrays.asList(new RecipeBaseWidget(bounds) {
            @Override
            public void render(int mouseX, int mouseY, float delta) {
                super.render(mouseX, mouseY, delta);
                GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
                GuiLighting.disable();
                MinecraftClient.getInstance().getTextureManager().bindTexture(DISPLAY_TEXTURE);
                blit(startPoint.x, startPoint.y, 0, 54, 82, 54);
                int height = MathHelper.ceil((System.currentTimeMillis() / 250 % 14d) / 1f);
                blit(startPoint.x + 2, startPoint.y + 21 + (14 - height), 82, 77 + (14 - height), 14, height);
                int width = MathHelper.ceil((System.currentTimeMillis() / 250 % 24d) / 1f);
                blit(startPoint.x + 24, startPoint.y + 18, 82, 91, width, 17);
            }
        }));
        List<List<ItemStack>> input = recipeDisplay.getInput();
        widgets.add(new SlotWidget(startPoint.x + 1, startPoint.y + 1, input.get(0), true, true, true));
        widgets.add(new SlotWidget(startPoint.x + 1, startPoint.y + 37, recipeDisplay.getFuel(), true, true, true) {
            @Override
            protected List<String> getExtraToolTips(ItemStack stack) {
                return Arrays.asList(I18n.translate("category.rei.smelting.fuel"));
            }
        });
        widgets.add(new SlotWidget(startPoint.x + 61, startPoint.y + 19, recipeDisplay.getOutput(), false, true, true));
        return widgets;
    }
    
}
