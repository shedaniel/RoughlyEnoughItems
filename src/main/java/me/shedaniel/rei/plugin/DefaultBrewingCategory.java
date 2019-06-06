/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.plugin;

import com.mojang.blaze3d.platform.GlStateManager;
import me.shedaniel.rei.api.RecipeCategory;
import me.shedaniel.rei.api.Renderable;
import me.shedaniel.rei.api.Renderer;
import me.shedaniel.rei.gui.widget.RecipeBaseWidget;
import me.shedaniel.rei.gui.widget.SlotWidget;
import me.shedaniel.rei.gui.widget.Widget;
import net.minecraft.ChatFormat;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GuiLighting;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
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
        return Renderable.fromItemStack(new ItemStack(Blocks.BREWING_STAND));
    }
    
    @Override
    public String getCategoryName() {
        return I18n.translate("category.rei.brewing");
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
                GuiLighting.disable();
                MinecraftClient.getInstance().getTextureManager().bindTexture(DefaultPlugin.getDisplayTexture());
                blit(startPoint.x, startPoint.y, 0, 108, 103, 59);
                int width = MathHelper.ceil((System.currentTimeMillis() / 250 % 18d) / 1f);
                blit(startPoint.x + 44, startPoint.y + 28, 103, 163, width, 4);
            }
        }));
        widgets.add(new SlotWidget(startPoint.x + 1, startPoint.y + 1, Arrays.asList(new ItemStack(Items.BLAZE_POWDER)), false, true, true));
        widgets.add(new SlotWidget(startPoint.x + 63, startPoint.y + 1, recipeDisplay.getInput().get(0), false, true, true) {
            @Override
            protected List<String> getExtraToolTips(ItemStack stack) {
                return Collections.singletonList(ChatFormat.YELLOW.toString() + I18n.translate("category.rei.brewing.input"));
            }
        });
        widgets.add(new SlotWidget(startPoint.x + 40, startPoint.y + 1, recipeDisplay.getInput().get(1), false, true, true) {
            @Override
            protected List<String> getExtraToolTips(ItemStack stack) {
                return Collections.singletonList(ChatFormat.YELLOW.toString() + I18n.translate("category.rei.brewing.reactant"));
            }
        });
        widgets.add(new SlotWidget(startPoint.x + 40, startPoint.y + 35, recipeDisplay.getOutput(0), false, true, true) {
            @Override
            protected List<String> getExtraToolTips(ItemStack stack) {
                return Collections.singletonList(ChatFormat.YELLOW.toString() + I18n.translate("category.rei.brewing.result"));
            }
        });
        widgets.add(new SlotWidget(startPoint.x + 63, startPoint.y + 42, recipeDisplay.getOutput(1), false, true, true) {
            @Override
            protected List<String> getExtraToolTips(ItemStack stack) {
                return Collections.singletonList(ChatFormat.YELLOW.toString() + I18n.translate("category.rei.brewing.result"));
            }
        });
        widgets.add(new SlotWidget(startPoint.x + 86, startPoint.y + 35, recipeDisplay.getOutput(2), false, true, true) {
            @Override
            protected List<String> getExtraToolTips(ItemStack stack) {
                return Collections.singletonList(ChatFormat.YELLOW.toString() + I18n.translate("category.rei.brewing.result"));
            }
        });
        return widgets;
    }
    
    @Override
    public boolean checkTags() {
        return true;
    }
    
}
