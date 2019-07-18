/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.plugin.brewing;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.shedaniel.rei.api.Identifier;
import me.shedaniel.rei.api.Identifiers;
import me.shedaniel.rei.api.RecipeCategory;
import me.shedaniel.rei.api.Renderer;
import me.shedaniel.rei.gui.widget.RecipeBaseWidget;
import me.shedaniel.rei.gui.widget.SlotWidget;
import me.shedaniel.rei.gui.widget.Widget;
import me.shedaniel.rei.plugin.DefaultPlugin;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;

import java.awt.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

public class DefaultBrewingCategory implements RecipeCategory<DefaultBrewingDisplay> {
    
    @Override
    public Identifier getIdentifier() {
        return DefaultPlugin.BREWING;
    }
    
    @Override
    public Renderer getIcon() {
        return Renderer.fromItemStack(new ItemStack(Blocks.BREWING_STAND));
    }
    
    @Override
    public String getCategoryName() {
        return I18n.format("category.rei.brewing");
    }
    
    @Override
    public List<Widget> setupDisplay(Supplier<DefaultBrewingDisplay> recipeDisplaySupplier, Rectangle bounds) {
        final DefaultBrewingDisplay recipeDisplay = recipeDisplaySupplier.get();
        Point startPoint = new Point((int) bounds.getCenterX() - 52, (int) bounds.getCenterY() - 29);
        List<Widget> widgets = new LinkedList<>(Arrays.asList(new RecipeBaseWidget(bounds) {
            @Override
            public void render(int mouseX, int mouseY, float delta) {
                super.render(mouseX, mouseY, delta);
                GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
                RenderHelper.disableStandardItemLighting();
                Minecraft.getInstance().getTextureManager().bindTexture(Identifiers.of(DefaultPlugin.getDisplayTexture()));
                drawTexturedModalRect(startPoint.x, startPoint.y, 0, 108, 103, 59);
                int width = MathHelper.ceil((System.currentTimeMillis() / 250 % 18d) / 1f);
                drawTexturedModalRect(startPoint.x + 44, startPoint.y + 28, 103, 163, width, 4);
            }
        }));
        widgets.add(new SlotWidget(startPoint.x + 1, startPoint.y + 1, Arrays.asList(new ItemStack(Items.BLAZE_POWDER)), false, true, true));
        widgets.add(new SlotWidget(startPoint.x + 63, startPoint.y + 1, recipeDisplay.getInput().get(0), false, true, true) {
            @Override
            protected List<String> getExtraToolTips(ItemStack stack) {
                return Collections.singletonList(ChatFormatting.YELLOW.toString() + I18n.format("category.rei.brewing.input"));
            }
        });
        widgets.add(new SlotWidget(startPoint.x + 40, startPoint.y + 1, recipeDisplay.getInput().get(1), false, true, true) {
            @Override
            protected List<String> getExtraToolTips(ItemStack stack) {
                return Collections.singletonList(ChatFormatting.YELLOW.toString() + I18n.format("category.rei.brewing.reactant"));
            }
        });
        widgets.add(new SlotWidget(startPoint.x + 40, startPoint.y + 35, recipeDisplay.getOutput(0), false, true, true) {
            @Override
            protected List<String> getExtraToolTips(ItemStack stack) {
                return Collections.singletonList(ChatFormatting.YELLOW.toString() + I18n.format("category.rei.brewing.result"));
            }
        });
        widgets.add(new SlotWidget(startPoint.x + 63, startPoint.y + 42, recipeDisplay.getOutput(1), false, true, true) {
            @Override
            protected List<String> getExtraToolTips(ItemStack stack) {
                return Collections.singletonList(ChatFormatting.YELLOW.toString() + I18n.format("category.rei.brewing.result"));
            }
        });
        widgets.add(new SlotWidget(startPoint.x + 86, startPoint.y + 35, recipeDisplay.getOutput(2), false, true, true) {
            @Override
            protected List<String> getExtraToolTips(ItemStack stack) {
                return Collections.singletonList(ChatFormatting.YELLOW.toString() + I18n.format("category.rei.brewing.result"));
            }
        });
        return widgets;
    }
    
    @Override
    public boolean checkTags() {
        return true;
    }
    
}
