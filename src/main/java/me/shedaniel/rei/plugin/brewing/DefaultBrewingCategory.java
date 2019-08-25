/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.plugin.brewing;

import me.shedaniel.math.api.Point;
import me.shedaniel.math.api.Rectangle;
import me.shedaniel.math.compat.RenderHelper;
import me.shedaniel.rei.api.RecipeCategory;
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
import net.minecraft.item.Items;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

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
        return I18n.translate("category.rei.brewing");
    }
    
    @Override
    public List<Widget> setupDisplay(Supplier<DefaultBrewingDisplay> recipeDisplaySupplier, Rectangle bounds) {
        final DefaultBrewingDisplay recipeDisplay = recipeDisplaySupplier.get();
        Point startPoint = new Point(bounds.getCenterX() - 52, bounds.getCenterY() - 29);
        List<Widget> widgets = new LinkedList<>(Arrays.asList(new RecipeBaseWidget(bounds) {
            @Override
            public void render(int mouseX, int mouseY, float delta) {
                super.render(mouseX, mouseY, delta);
                RenderHelper.color4f(1.0F, 1.0F, 1.0F, 1.0F);
                GuiLighting.disable();
                MinecraftClient.getInstance().getTextureManager().bindTexture(DefaultPlugin.getDisplayTexture());
                blit(startPoint.x, startPoint.y, 0, 108, 103, 59);
                int width = MathHelper.ceil((System.currentTimeMillis() / 250 % 18d) / 1f);
                blit(startPoint.x + 44, startPoint.y + 28, 103, 163, width, 4);
            }
        }));
        widgets.add(new SlotWidget(startPoint.x + 1, startPoint.y + 1, Renderer.fromItemStack(new ItemStack(Items.BLAZE_POWDER)), false, true, true));
        widgets.add(new SlotWidget(startPoint.x + 40, startPoint.y + 1, Renderer.fromItemStacks(recipeDisplay.getInput().get(0)), false, true, true) {
            @Override
            protected List<String> getExtraItemToolTips(ItemStack stack) {
                return Collections.singletonList(Formatting.YELLOW.toString() + I18n.translate("category.rei.brewing.input"));
            }
        });
        widgets.add(new SlotWidget(startPoint.x + 63, startPoint.y + 1, Renderer.fromItemStacks(recipeDisplay.getInput().get(1)), false, true, true) {
            @Override
            protected List<String> getExtraItemToolTips(ItemStack stack) {
                return Collections.singletonList(Formatting.YELLOW.toString() + I18n.translate("category.rei.brewing.reactant"));
            }
        });
        widgets.add(new SlotWidget(startPoint.x + 40, startPoint.y + 35, Renderer.fromItemStacks(recipeDisplay.getOutput(0)), false, true, true) {
            @Override
            protected List<String> getExtraItemToolTips(ItemStack stack) {
                return Collections.singletonList(Formatting.YELLOW.toString() + I18n.translate("category.rei.brewing.result"));
            }
        });
        widgets.add(new SlotWidget(startPoint.x + 63, startPoint.y + 42, Renderer.fromItemStacks(recipeDisplay.getOutput(1)), false, true, true) {
            @Override
            protected List<String> getExtraItemToolTips(ItemStack stack) {
                return Collections.singletonList(Formatting.YELLOW.toString() + I18n.translate("category.rei.brewing.result"));
            }
        });
        widgets.add(new SlotWidget(startPoint.x + 86, startPoint.y + 35, Renderer.fromItemStacks(recipeDisplay.getOutput(2)), false, true, true) {
            @Override
            protected List<String> getExtraItemToolTips(ItemStack stack) {
                return Collections.singletonList(Formatting.YELLOW.toString() + I18n.translate("category.rei.brewing.result"));
            }
        });
        return widgets;
    }
    
    @Override
    public boolean checkTags() {
        return true;
    }
    
}
