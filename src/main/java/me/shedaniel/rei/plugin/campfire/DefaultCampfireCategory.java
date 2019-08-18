/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.plugin.campfire;

import com.mojang.blaze3d.platform.GlStateManager;
import me.shedaniel.math.api.Point;
import me.shedaniel.math.api.Rectangle;
import me.shedaniel.rei.api.RecipeCategory;
import me.shedaniel.rei.api.Renderer;
import me.shedaniel.rei.gui.widget.RecipeArrowWidget;
import me.shedaniel.rei.gui.widget.RecipeBaseWidget;
import me.shedaniel.rei.gui.widget.SlotWidget;
import me.shedaniel.rei.gui.widget.Widget;
import me.shedaniel.rei.impl.ScreenHelper;
import me.shedaniel.rei.plugin.DefaultPlugin;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GuiLighting;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

public class DefaultCampfireCategory implements RecipeCategory<DefaultCampfireDisplay> {
    
    @Override
    public Identifier getIdentifier() {
        return DefaultPlugin.CAMPFIRE;
    }
    
    @Override
    public Renderer getIcon() {
        return Renderer.fromItemStack(new ItemStack(Blocks.CAMPFIRE));
    }
    
    @Override
    public String getCategoryName() {
        return I18n.translate("category.rei.campfire");
    }
    
    @Override
    public List<Widget> setupDisplay(Supplier<DefaultCampfireDisplay> recipeDisplaySupplier, Rectangle bounds) {
        Point startPoint = new Point(bounds.getCenterX() - 41, bounds.getCenterY() - 27);
        List<Widget> widgets = new LinkedList<>(Arrays.asList(new RecipeBaseWidget(bounds) {
            @Override
            public void render(int mouseX, int mouseY, float delta) {
                super.render(mouseX, mouseY, delta);
                GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
                GuiLighting.disable();
                MinecraftClient.getInstance().getTextureManager().bindTexture(DefaultPlugin.getDisplayTexture());
                blit(startPoint.x, startPoint.y, 0, 167, 82, 54);
                int height = MathHelper.ceil((System.currentTimeMillis() / 250 % 14d) / 1f);
                blit(startPoint.x + 2, startPoint.y + 31 + (14 - height), 82, 77 + (14 - height), 14, height);
                String text = I18n.translate("category.rei.campfire.time", MathHelper.floor(recipeDisplaySupplier.get().getCookTime() / 20d));
                int length = MinecraftClient.getInstance().textRenderer.getStringWidth(text);
                MinecraftClient.getInstance().textRenderer.draw(text, bounds.x + bounds.width - length - 5, startPoint.y + 54 - 8, ScreenHelper.isDarkModeEnabled() ? 0xFFBBBBBB : 0xFF404040);
            }
        }));
        widgets.add(new RecipeArrowWidget(startPoint.x + 24, startPoint.y + 18, true));
        widgets.add(new SlotWidget(startPoint.x + 1, startPoint.y + 11, Renderer.fromItemStacks(recipeDisplaySupplier.get().getInput().get(0)), true, true, true));
        widgets.add(new SlotWidget(startPoint.x + 61, startPoint.y + 19, Renderer.fromItemStacks(recipeDisplaySupplier.get().getOutput()), false, true, true));
        return widgets;
    }
    
}
