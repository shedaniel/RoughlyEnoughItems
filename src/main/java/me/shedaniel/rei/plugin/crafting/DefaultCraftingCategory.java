/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.plugin.crafting;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.ints.IntList;
import me.shedaniel.math.api.Point;
import me.shedaniel.math.api.Rectangle;
import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.api.TransferRecipeCategory;
import me.shedaniel.rei.gui.widget.EntryWidget;
import me.shedaniel.rei.gui.widget.RecipeBaseWidget;
import me.shedaniel.rei.gui.widget.Widget;
import me.shedaniel.rei.plugin.DefaultPlugin;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.util.Identifier;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

public class DefaultCraftingCategory implements TransferRecipeCategory<DefaultCraftingDisplay> {
    
    @Deprecated
    public static int getSlotWithSize(DefaultCraftingDisplay recipeDisplay, int num) {
        return getSlotWithSize(recipeDisplay, num, 3);
    }
    
    public static int getSlotWithSize(DefaultCraftingDisplay recipeDisplay, int num, int craftingGridWidth) {
        int x = num % recipeDisplay.getWidth();
        int y = (num - x) / recipeDisplay.getWidth();
        return craftingGridWidth * y + x;
    }
    
    @Override
    public Identifier getIdentifier() {
        return DefaultPlugin.CRAFTING;
    }
    
    @Override
    public EntryStack getLogo() {
        return EntryStack.create(Blocks.CRAFTING_TABLE);
    }
    
    @Override
    public String getCategoryName() {
        return I18n.translate("category.rei.crafting");
    }
    
    @Override
    public List<Widget> setupDisplay(Supplier<DefaultCraftingDisplay> recipeDisplaySupplier, Rectangle bounds) {
        Point startPoint = new Point(bounds.getCenterX() - 58, bounds.getCenterY() - 27);
        List<Widget> widgets = new LinkedList<>(Arrays.asList(new RecipeBaseWidget(bounds) {
            @Override
            public void render(int mouseX, int mouseY, float delta) {
                super.render(mouseX, mouseY, delta);
                RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
                DiffuseLighting.disable();
                MinecraftClient.getInstance().getTextureManager().bindTexture(DefaultPlugin.getDisplayTexture());
                blit(startPoint.x, startPoint.y, 0, 0, 116, 54);
            }
        }));
        List<List<EntryStack>> input = recipeDisplaySupplier.get().getInputEntries();
        List<EntryWidget> slots = Lists.newArrayList();
        for (int y = 0; y < 3; y++)
            for (int x = 0; x < 3; x++)
                slots.add(EntryWidget.create(startPoint.x + 1 + x * 18, startPoint.y + 1 + y * 18));
        for (int i = 0; i < input.size(); i++) {
            if (recipeDisplaySupplier.get() instanceof DefaultShapedDisplay) {
                if (!input.get(i).isEmpty())
                    slots.get(getSlotWithSize(recipeDisplaySupplier.get(), i, 3)).entries(input.get(i));
            } else if (!input.get(i).isEmpty())
                slots.get(i).entries(input.get(i));
        }
        widgets.addAll(slots);
        widgets.add(EntryWidget.create(startPoint.x + 95, startPoint.y + 19).entries(recipeDisplaySupplier.get().getOutputEntries()).noBackground());
        return widgets;
    }
    
    @Override
    public void renderRedSlots(List<Widget> widgets, Rectangle bounds, DefaultCraftingDisplay display, IntList redSlots) {
        Point startPoint = new Point(bounds.getCenterX() - 58, bounds.getCenterY() - 27);
        RenderSystem.translatef(0, 0, 400);
        for (Integer slot : redSlots) {
            int i = slot;
            int x = i % 3;
            int y = (i - x) / 3;
            DrawableHelper.fill(startPoint.x + 1 + x * 18, startPoint.y + 1 + y * 18, startPoint.x + 1 + x * 18 + 16, startPoint.y + 1 + y * 18 + 16, 0x60ff0000);
        }
        RenderSystem.translatef(0, 0, -400);
    }
}
