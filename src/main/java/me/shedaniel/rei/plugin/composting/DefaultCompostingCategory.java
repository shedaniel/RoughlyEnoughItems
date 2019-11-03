/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.plugin.composting;

import com.google.common.collect.Lists;
import me.shedaniel.math.api.Point;
import me.shedaniel.math.api.Rectangle;
import com.mojang.blaze3d.systems.RenderSystem;
import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.api.RecipeCategory;
import me.shedaniel.rei.gui.renderers.RecipeRenderer;
import me.shedaniel.rei.gui.widget.EntryWidget;
import me.shedaniel.rei.gui.widget.RecipeBaseWidget;
import me.shedaniel.rei.gui.widget.Widget;
import me.shedaniel.rei.plugin.DefaultPlugin;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GuiLighting;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.item.ItemConvertible;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class DefaultCompostingCategory implements RecipeCategory<DefaultCompostingDisplay> {
    
    @Override
    public Identifier getIdentifier() {
        return DefaultPlugin.COMPOSTING;
    }
    
    @Override
    public EntryStack getLogo() {
        return EntryStack.create(Blocks.COMPOSTER);
    }
    
    @Override
    public String getCategoryName() {
        return I18n.translate("category.rei.composting");
    }
    
    @Override
    public RecipeRenderer getSimpleRenderer(DefaultCompostingDisplay recipe) {
        return new RecipeRenderer() {
            @Override
            public int getHeight() {
                return 10 + MinecraftClient.getInstance().textRenderer.fontHeight;
            }
            
            @Override
            public void render(int x, int y, double mouseX, double mouseY, float delta) {
                MinecraftClient.getInstance().textRenderer.draw(I18n.translate("text.rei.composting.page", recipe.getPage() + 1), x + 5, y + 6, -1);
            }
        };
    }
    
    @Override
    public List<Widget> setupDisplay(Supplier<DefaultCompostingDisplay> recipeDisplaySupplier, Rectangle bounds) {
        List<Widget> widgets = Lists.newArrayList();
        Point startingPoint = new Point(bounds.x + bounds.width - 55, bounds.y + 110);
        widgets.add(new RecipeBaseWidget(bounds) {
            @Override
            public void render(int mouseX, int mouseY, float partialTicks) {
                RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
                GuiLighting.disable();
                MinecraftClient.getInstance().getTextureManager().bindTexture(DefaultPlugin.getDisplayTexture());
                this.blit(startingPoint.x, startingPoint.y, 28, 221, 55, 26);
            }
        });
        List<EntryStack> stacks = new LinkedList<>(recipeDisplaySupplier.get().getItemsByOrder());
        int i = 0;
        for (int y = 0; y < 6; y++)
            for (int x = 0; x < 8; x++) {
                int finalI = i;
                EntryStack entryStack = stacks.size() > i ? stacks.get(finalI) : EntryStack.empty();
                if (entryStack.getType() != EntryStack.Type.EMPTY)
                    for (Map.Entry<ItemConvertible, Float> entry : recipeDisplaySupplier.get().getInputMap().entrySet()) {
                        if (entry.getKey().asItem().equals(entryStack.getItem())) {
                            entryStack = entryStack.setting(EntryStack.Settings.TOOLTIP_APPEND_EXTRA, s -> Collections.singletonList(I18n.translate("text.rei.composting.chance", MathHelper.fastFloor(entry.getValue() * 100))));
                            break;
                        }
                    }
                widgets.add(EntryWidget.create(bounds.getCenterX() - 72 + x * 18, bounds.y + y * 18).entry(entryStack));
                i++;
            }
        widgets.add(EntryWidget.create(startingPoint.x + 34, startingPoint.y + 5).entries(recipeDisplaySupplier.get().getOutputEntries()).noBackground());
        return widgets;
    }
    
    @Override
    public int getDisplayHeight() {
        return 140;
    }
    
    @SuppressWarnings("deprecation")
    @Override
    public int getFixedRecipesPerPage() {
        return 1;
    }
}