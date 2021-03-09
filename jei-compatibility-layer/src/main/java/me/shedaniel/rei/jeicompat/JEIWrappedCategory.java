package me.shedaniel.rei.jeicompat;

import com.mojang.blaze3d.vertex.PoseStack;
import me.shedaniel.math.Dimension;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.gui.Renderer;
import me.shedaniel.rei.api.gui.widgets.Tooltip;
import me.shedaniel.rei.api.gui.widgets.Widget;
import me.shedaniel.rei.api.gui.widgets.Widgets;
import me.shedaniel.rei.api.registry.display.DisplayCategory;
import me.shedaniel.rei.api.util.ImmutableLiteralText;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.LazyLoadedValue;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static me.shedaniel.rei.jeicompat.JEIPluginDetector.wrapDrawable;

public class JEIWrappedCategory<T> implements DisplayCategory<JEIWrappedDisplay<T>> {
    private final IRecipeCategory<T> backingCategory;
    private final LazyLoadedValue<IDrawable> background;
    
    public JEIWrappedCategory(IRecipeCategory<T> backingCategory) {
        this.backingCategory = backingCategory;
        this.background = new LazyLoadedValue<>(backingCategory::getBackground);
    }
    
    public Class<? extends T> getRecipeClass() {
        return backingCategory.getRecipeClass();
    }
    
    public boolean handlesRecipe(T recipe) {
        return backingCategory.isHandled(recipe);
    }
    
    @Override
    public Renderer getIcon() {
        return wrapDrawable(backingCategory.getIcon());
    }
    
    @Override
    public Component getTitle() {
        return new ImmutableLiteralText(backingCategory.getTitle());
    }
    
    @Override
    public ResourceLocation getIdentifier() {
        return backingCategory.getUid();
    }
    
    @Override
    public int getDisplayWidth(JEIWrappedDisplay<T> display) {
        return this.background.get().getWidth() + 8;
    }
    
    @Override
    public int getDisplayHeight() {
        return this.background.get().getHeight() + 8;
    }
    
    public IRecipeCategory<T> getBackingCategory() {
        return backingCategory;
    }
    
    @Override
    public List<Widget> setupDisplay(JEIWrappedDisplay<T> display, Rectangle bounds) {
        List<Widget> widgets = new ArrayList<>();
        widgets.add(Widgets.createRecipeBase(bounds));
        widgets.add(Widgets.createDrawableWidget((helper, matrices, mouseX, mouseY, delta) -> {
            this.background.get().draw(matrices, bounds.x + 4, bounds.y + 4);
        }));
        backingCategory.setRecipe(new JEIRecipeLayout<>(this, display), display.getBackingRecipe(), display.getIngredients());
        widgets.add(new Widget() {
            @Override
            public void render(PoseStack arg, int i, int j, float f) {
                arg.pushPose();
                arg.translate(bounds.x, bounds.y, 0);
                backingCategory.draw(display.getBackingRecipe(), arg, i, j);
                arg.popPose();
            }
            
            @Override
            public @Nullable Tooltip getTooltip(Point mouse) {
                List<Component> strings = backingCategory.getTooltipStrings(display.getBackingRecipe(), mouse.x - bounds.x, mouse.y - bounds.y);
                if (strings.isEmpty()) {
                    return null;
                }
                return Tooltip.create(mouse, strings);
            }
            
            @Override
            public List<? extends GuiEventListener> children() {
                return Collections.emptyList();
            }
            
            @Override
            public boolean mouseClicked(double d, double e, int i) {
                return backingCategory.handleClick(display.getBackingRecipe(), d - bounds.x, e - bounds.y, i) || super.mouseClicked(d, e, i);
            }
        });
        return widgets;
    }
}
