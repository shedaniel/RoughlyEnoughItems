package me.shedaniel.rei.gui;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.platform.GlStateManager;
import me.shedaniel.rei.api.RecipeCategory;
import me.shedaniel.rei.api.RecipeDisplay;
import me.shedaniel.rei.api.RecipeHelper;
import me.shedaniel.rei.client.ScreenHelper;
import me.shedaniel.rei.gui.widget.CategoryBaseWidget;
import me.shedaniel.rei.gui.widget.RecipeBaseWidget;
import me.shedaniel.rei.gui.widget.SlotBaseWidget;
import me.shedaniel.rei.gui.widget.Widget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Screen;
import net.minecraft.client.render.GuiLighting;
import net.minecraft.text.StringTextComponent;
import net.minecraft.util.math.MathHelper;

import java.awt.*;
import java.util.List;
import java.util.Map;

public class VillagerRecipeViewingScreen extends Screen {
    
    private final Map<RecipeCategory, List<RecipeDisplay>> categoryMap;
    private final List<RecipeCategory> categories;
    private final List<Widget> widgets;
    public Rectangle bounds, scrollListBounds;
    private int selectedCategoryIndex, selectedRecipeIndex;
    
    public VillagerRecipeViewingScreen(Map<RecipeCategory, List<RecipeDisplay>> map) {
        super(new StringTextComponent(""));
        this.widgets = Lists.newArrayList();
        this.categoryMap = Maps.newLinkedHashMap();
        this.selectedCategoryIndex = 0;
        this.selectedRecipeIndex = 0;
        this.categories = Lists.newArrayList();
        RecipeHelper.getInstance().getAllCategories().forEach(category -> {
            if (map.containsKey(category)) {
                categories.add(category);
                categoryMap.put(category, map.get(category));
            }
        });
    }
    
    @Override
    protected void init() {
        super.init();
        this.children.clear();
        this.widgets.clear();
        int largestWidth = width - 100;
        int largestHeight = height - 40;
        RecipeCategory category = categories.get(selectedCategoryIndex);
        RecipeDisplay display = categoryMap.get(category).get(selectedRecipeIndex);
        int guiWidth = MathHelper.clamp(category.getDisplayWidth(display) + 30, 0, largestWidth) + 100;
        int guiHeight = MathHelper.clamp(category.getDisplayHeight() + 40, 166, largestHeight);
        this.bounds = new Rectangle(width / 2 - guiWidth / 2, height / 2 - guiHeight / 2, guiWidth, guiHeight);
        this.widgets.add(new CategoryBaseWidget(bounds));
        this.scrollListBounds = new Rectangle(bounds.x + 4, bounds.y + 17, 97, guiHeight - 17 - 7);
        this.widgets.add(new SlotBaseWidget(scrollListBounds));
        Rectangle recipeBounds = new Rectangle(bounds.x + 100 + (guiWidth - 100) / 2 - category.getDisplayWidth(display) / 2, bounds.y + bounds.height / 2 - category.getDisplayHeight() / 2, category.getDisplayWidth(display), category.getDisplayHeight());
        this.widgets.addAll(category.setupDisplay(() -> display, recipeBounds));
        this.children.addAll(widgets);
        this.children.add(ScreenHelper.getLastOverlay(true, false));
    }
    
    @Override
    public void render(int mouseX, int mouseY, float delta) {
        this.fillGradient(0, 0, this.width, this.height, -1072689136, -804253680);
        this.widgets.forEach(widget -> {
            GuiLighting.disable();
            widget.render(mouseX, mouseY, delta);
        });
        GuiLighting.disable();
        ScreenHelper.getLastOverlay().render(mouseX, mouseY, delta);
        GlStateManager.pushMatrix();
        GlStateManager.translatef((float) bounds.x, (float) bounds.y, 0.0F);
        GuiLighting.disable();
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        String categoryName = categories.get(selectedCategoryIndex).getCategoryName();
        font.draw(categoryName, 4 + scrollListBounds.width / 2 - font.getStringWidth(categoryName) / 2, 6, 4210752);
        GlStateManager.popMatrix();
        GuiLighting.disable();
        ScreenHelper.getLastOverlay().lateRender(mouseX, mouseY, delta);
    }
    
    @Override
    public boolean keyPressed(int int_1, int int_2, int int_3) {
        if ((int_1 == 256 || this.minecraft.options.keyInventory.matchesKey(int_1, int_2)) && this.shouldCloseOnEsc()) {
            MinecraftClient.getInstance().openScreen(ScreenHelper.getLastContainerScreen());
            ScreenHelper.getLastOverlay().init();
            return true;
        }
        return super.keyPressed(int_1, int_2, int_3);
    }
    
}
