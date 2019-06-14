/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.plugin.stonecutting;

import com.mojang.blaze3d.platform.GlStateManager;
import me.shedaniel.rei.api.DisplaySettings;
import me.shedaniel.rei.api.RecipeCategory;
import me.shedaniel.rei.api.Renderable;
import me.shedaniel.rei.api.Renderer;
import me.shedaniel.rei.gui.widget.RecipeBaseWidget;
import me.shedaniel.rei.gui.widget.SlotWidget;
import me.shedaniel.rei.gui.widget.Widget;
import me.shedaniel.rei.plugin.DefaultPlugin;
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
    
    @Override
    public Identifier getIdentifier() {
        return DefaultPlugin.STONE_CUTTING;
    }
    
    @Override
    public Renderer getIcon() {
        return Renderable.fromItemStack(new ItemStack(Blocks.STONECUTTER));
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
            public void render(int mouseX, int mouseY, float delta) {
                super.render(mouseX, mouseY, delta);
                GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
                GuiLighting.disable();
                MinecraftClient.getInstance().getTextureManager().bindTexture(DefaultPlugin.getDisplayTexture());
                blit(startPoint.x, startPoint.y, 0, 221, 82, 26);
            }
        }));
        widgets.add(new SlotWidget(startPoint.x + 4, startPoint.y + 5, recipeDisplaySupplier.get().getInput().get(0), true, true, true));
        widgets.add(new SlotWidget(startPoint.x + 61, startPoint.y + 5, recipeDisplaySupplier.get().getOutput(), false, true, true));
        return widgets;
    }
    
    @Override
    public DisplaySettings getDisplaySettings() {
        return new DisplaySettings<DefaultStoneCuttingDisplay>() {
            @Override
            public int getDisplayHeight(RecipeCategory category) {
                return 36;
            }
            
            @Override
            public int getDisplayWidth(RecipeCategory category, DefaultStoneCuttingDisplay display) {
                return 150;
            }
            
            @Override
            public int getMaximumRecipePerPage(RecipeCategory category) {
                return 99;
            }
        };
    }
    
}
