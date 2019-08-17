/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.plugin.crafting;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlStateManager;
import it.unimi.dsi.fastutil.ints.IntList;
import me.shedaniel.math.api.Point;
import me.shedaniel.math.api.Rectangle;
import me.shedaniel.rei.api.Renderer;
import me.shedaniel.rei.api.TransferRecipeCategory;
import me.shedaniel.rei.gui.widget.RecipeBaseWidget;
import me.shedaniel.rei.gui.widget.SlotWidget;
import me.shedaniel.rei.gui.widget.Widget;
import me.shedaniel.rei.plugin.DefaultPlugin;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.render.GuiLighting;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

public class DefaultCraftingCategory implements TransferRecipeCategory<DefaultCraftingDisplay> {
    
    public static int getSlotWithSize(DefaultCraftingDisplay recipeDisplay, int num) {
        if (recipeDisplay.getWidth() == 1) {
            if (num == 1)
                return 3;
            if (num == 2)
                return 6;
        }
        
        if (recipeDisplay.getWidth() == 2) {
            if (num == 2)
                return 3;
            if (num == 3)
                return 4;
            if (num == 4)
                return 6;
            if (num == 5)
                return 7;
        }
        return num;
    }
    
    @Override
    public Identifier getIdentifier() {
        return DefaultPlugin.CRAFTING;
    }
    
    @Override
    public Renderer getIcon() {
        return Renderer.fromItemStack(new ItemStack(Blocks.CRAFTING_TABLE));
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
                GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
                GuiLighting.disable();
                MinecraftClient.getInstance().getTextureManager().bindTexture(DefaultPlugin.getDisplayTexture());
                blit(startPoint.x, startPoint.y, 0, 0, 116, 54);
            }
        }));
        List<List<ItemStack>> input = recipeDisplaySupplier.get().getInput();
        List<SlotWidget> slots = Lists.newArrayList();
        for (int y = 0; y < 3; y++)
            for (int x = 0; x < 3; x++)
                slots.add(new SlotWidget(startPoint.x + 1 + x * 18, startPoint.y + 1 + y * 18, Lists.newArrayList(), true, true, true));
        for (int i = 0; i < input.size(); i++) {
            if (recipeDisplaySupplier.get() instanceof DefaultShapedDisplay) {
                if (!input.get(i).isEmpty())
                    slots.get(getSlotWithSize(recipeDisplaySupplier.get(), i)).setItemList(input.get(i));
            } else if (!input.get(i).isEmpty())
                slots.get(i).setItemList(input.get(i));
        }
        widgets.addAll(slots);
        widgets.add(new SlotWidget(startPoint.x + 95, startPoint.y + 19, Renderer.fromItemStacks(recipeDisplaySupplier.get().getOutput()), false, true, true));
        return widgets;
    }
    
    @Override
    public void renderRedSlots(List<Widget> widgets, Rectangle bounds, DefaultCraftingDisplay display, IntList redSlots) {
        Point startPoint = new Point(bounds.getCenterX() - 58, bounds.getCenterY() - 27);
        GlStateManager.translatef(0, 0, 400);
        for (Integer slot : redSlots) {
            int i = getSlotWithSize(display, slot);
            int x = i % 3;
            int y = (i - x) / 3;
            DrawableHelper.fill(startPoint.x + 1 + x * 18, startPoint.y + 1 + y * 18, startPoint.x + 1 + x * 18 + 16, startPoint.y + 1 + y * 18 + 16, 0x30ff0000);
        }
        GlStateManager.translatef(0, 0, -400);
    }
}
