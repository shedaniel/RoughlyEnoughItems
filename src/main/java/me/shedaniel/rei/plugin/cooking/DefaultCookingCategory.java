package me.shedaniel.rei.plugin.cooking;

import it.unimi.dsi.fastutil.ints.IntList;
import me.shedaniel.math.api.Point;
import me.shedaniel.math.api.Rectangle;
import me.shedaniel.math.compat.RenderHelper;
import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.api.Renderer;
import me.shedaniel.rei.api.TransferRecipeCategory;
import me.shedaniel.rei.gui.renderers.RecipeRenderer;
import me.shedaniel.rei.gui.widget.EntryWidget;
import me.shedaniel.rei.gui.widget.RecipeArrowWidget;
import me.shedaniel.rei.gui.widget.RecipeBaseWidget;
import me.shedaniel.rei.gui.widget.Widget;
import me.shedaniel.rei.plugin.DefaultPlugin;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.render.GuiLighting;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

public class DefaultCookingCategory implements TransferRecipeCategory<DefaultCookingDisplay> {
    private Identifier identifier;
    private EntryStack logo;
    private String categoryName;
    
    public DefaultCookingCategory(Identifier identifier, EntryStack logo, String categoryName) {
        this.identifier = identifier;
        this.logo = logo;
        this.categoryName = categoryName;
    }
    
    @Override
    public void renderRedSlots(List<Widget> widgets, Rectangle bounds, DefaultCookingDisplay display, IntList redSlots) {
        Point startPoint = new Point(bounds.getCenterX() - 41, bounds.getCenterY() - 27);
        RenderHelper.translatef(0, 0, 400);
        if (redSlots.contains(0)) {
            DrawableHelper.fill(startPoint.x + 1, startPoint.y + 1, startPoint.x + 1 + 16, startPoint.y + 1 + 16, 822018048);
        }
        RenderHelper.translatef(0, 0, -400);
    }
    
    @Override
    public List<Widget> setupDisplay(Supplier<DefaultCookingDisplay> recipeDisplaySupplier, Rectangle bounds) {
        Point startPoint = new Point(bounds.getCenterX() - 41, bounds.getCenterY() - 27);
        List<Widget> widgets = new LinkedList<>(Arrays.asList(new RecipeBaseWidget(bounds) {
            @Override
            public void render(int mouseX, int mouseY, float delta) {
                super.render(mouseX, mouseY, delta);
                RenderHelper.color4f(1.0F, 1.0F, 1.0F, 1.0F);
                GuiLighting.disable();
                MinecraftClient.getInstance().getTextureManager().bindTexture(DefaultPlugin.getDisplayTexture());
                blit(startPoint.x, startPoint.y, 0, 54, 82, 54);
                int height = MathHelper.ceil((System.currentTimeMillis() / 250 % 14d) / 1f);
                blit(startPoint.x + 2, startPoint.y + 21 + (14 - height), 82, 77 + (14 - height), 14, height);
            }
        }));
        widgets.add(new RecipeArrowWidget(startPoint.x + 24, startPoint.y + 18, true));
        List<List<EntryStack>> input = recipeDisplaySupplier.get().getInputEntries();
        widgets.add(EntryWidget.create(startPoint.x + 1, startPoint.y + 1).entries(input.get(0)));
        widgets.add(EntryWidget.create(startPoint.x + 1, startPoint.y + 37).entries(input.get(1)));
        widgets.add(EntryWidget.create(startPoint.x + 61, startPoint.y + 19).entries(recipeDisplaySupplier.get().getOutputEntries()).noBackground());
        return widgets;
    }
    
    @Override
    public RecipeRenderer getSimpleRenderer(DefaultCookingDisplay recipe) {
        return Renderer.fromRecipeEntries(() -> Arrays.asList(recipe.getInputEntries().get(0)), recipe::getOutputEntries);
    }
    
    @Override
    public Identifier getIdentifier() {
        return identifier;
    }
    
    @Override
    public EntryStack getLogo() {
        return logo;
    }
    
    @Override
    public String getCategoryName() {
        return I18n.translate(categoryName);
    }
}
