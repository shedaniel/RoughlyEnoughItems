package me.shedaniel.rei.jeicompat;

import com.mojang.blaze3d.vertex.PoseStack;
import me.shedaniel.clothconfig2.forge.api.PointHelper;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.gui.widgets.Panel;
import me.shedaniel.rei.api.gui.widgets.Widget;
import me.shedaniel.rei.api.gui.widgets.Widgets;
import me.shedaniel.rei.api.ingredient.EntryStack;
import mezz.jei.api.gui.ITickTimer;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.drawable.IDrawableBuilder;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.ICraftingGridHelper;
import mezz.jei.api.helpers.IGuiHelper;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import static me.shedaniel.rei.jeicompat.JEIPluginDetector.TODO;
import static me.shedaniel.rei.jeicompat.JEIPluginDetector.wrap;

public enum JEIGuiHelper implements IGuiHelper {
    INSTANCE;
    
    @Override
    @NotNull
    public IDrawableBuilder drawableBuilder(ResourceLocation resourceLocation, int u, int v, int width, int height) {
        return new IDrawableBuilder() {
            private int textureWidth = width;
            private int textureHeight = height;
            private int trimTop;
            private int trimBottom;
            private int trimLeft;
            private int trimRight;
            private int paddingTop;
            private int paddingBottom;
            private int paddingLeft;
            private int paddingRight;
            
            @Override
            public IDrawableBuilder setTextureSize(int width, int height) {
                this.textureWidth = width;
                this.textureHeight = height;
                return this;
            }
            
            @Override
            public IDrawableBuilder addPadding(int paddingTop, int paddingBottom, int paddingLeft, int paddingRight) {
                this.paddingTop = paddingTop;
                this.paddingBottom = paddingBottom;
                this.paddingLeft = paddingLeft;
                this.paddingRight = paddingRight;
                return this;
            }
            
            @Override
            public IDrawableBuilder trim(int trimTop, int trimBottom, int trimLeft, int trimRight) {
                this.trimTop = trimTop;
                this.trimBottom = trimBottom;
                this.trimLeft = trimLeft;
                this.trimRight = trimRight;
                return this;
            }
            
            @Override
            public IDrawableStatic build() {
                Widget widget = Widgets.createTexturedWidget(resourceLocation, 0, 0, u, v, width, height, textureWidth, textureHeight);
                return new IDrawableStatic() {
                    @Override
                    public void draw(PoseStack matrixStack, int xOffset, int yOffset, int maskTop, int maskBottom, int maskLeft, int maskRight) {
                        matrixStack.pushPose();
                        matrixStack.translate(xOffset, yOffset, 0);
                        widget.render(matrixStack, PointHelper.getMouseX(), PointHelper.getMouseY(), 0);
                        matrixStack.popPose();
                    }
    
                    @Override
                    public void draw(PoseStack matrixStack, int xOffset, int yOffset) {
                        matrixStack.pushPose();
                        matrixStack.translate(xOffset, yOffset, 0);
                        widget.render(matrixStack, PointHelper.getMouseX(), PointHelper.getMouseY(), 0);
                        matrixStack.popPose();
                    }
    
                    @Override
                    public int getWidth() {
                        return textureWidth + paddingLeft + paddingRight - trimLeft - trimRight;
                    }
                    
                    @Override
                    public int getHeight() {
                        return textureHeight + paddingTop + paddingBottom - trimTop - trimBottom;
                    }
                };
            }
            
            @Override
            public IDrawableAnimated buildAnimated(int ticksPerCycle, IDrawableAnimated.StartDirection startDirection, boolean inverted) {
                return createAnimatedDrawable(build(), ticksPerCycle, startDirection, inverted);
            }
            
            @Override
            public IDrawableAnimated buildAnimated(ITickTimer tickTimer, IDrawableAnimated.StartDirection startDirection) {
                throw TODO();
            }
        };
    }
    
    @Override
    @NotNull
    public IDrawableAnimated createAnimatedDrawable(IDrawableStatic drawable, int ticksPerCycle, IDrawableAnimated.StartDirection startDirection, boolean inverted) {
        // TODO Implement Animation
        return new IDrawableAnimated() {
            @Override
            public int getWidth() {
                return drawable.getWidth();
            }
            
            @Override
            public int getHeight() {
                return drawable.getHeight();
            }
            
            @Override
            public void draw(PoseStack matrixStack, int xOffset, int yOffset) {
                drawable.draw(matrixStack, xOffset, yOffset);
            }
        };
    }
    
    @Override
    @NotNull
    public IDrawableStatic getSlotDrawable() {
        Panel base = Widgets.createSlotBase(new Rectangle(0, 0, 18, 18));
        return new IDrawableStatic() {
            @Override
            public void draw(PoseStack matrixStack, int xOffset, int yOffset, int maskTop, int maskBottom, int maskLeft, int maskRight) {
                throw TODO();
            }
            
            @Override
            public void draw(PoseStack matrixStack, int xOffset, int yOffset) {
                base.getBounds().setLocation(xOffset, yOffset);
                base.render(matrixStack, PointHelper.getMouseX(), PointHelper.getMouseY(), 0);
            }
            
            @Override
            public int getWidth() {
                return base.getBounds().getWidth();
            }
            
            @Override
            public int getHeight() {
                return base.getBounds().getHeight();
            }
        };
    }
    
    @Override
    @NotNull
    public IDrawableStatic createBlankDrawable(int width, int height) {
        return new IDrawableStatic() {
            @Override
            public void draw(PoseStack matrixStack, int xOffset, int yOffset, int maskTop, int maskBottom, int maskLeft, int maskRight) {
                
            }
            
            @Override
            public void draw(PoseStack matrixStack, int xOffset, int yOffset) {
                
            }
            
            @Override
            public int getWidth() {
                return width;
            }
            
            @Override
            public int getHeight() {
                return height;
            }
        };
    }
    
    @Override
    @NotNull
    public <V> IDrawable createDrawableIngredient(V ingredient) {
        EntryStack<?> stack = wrap(ingredient);
        return new IDrawable() {
            @Override
            public int getWidth() {
                return 18;
            }
            
            @Override
            public int getHeight() {
                return 18;
            }
            
            @Override
            public void draw(PoseStack matrixStack, int xOffset, int yOffset) {
                stack.render(matrixStack, new Rectangle(xOffset, yOffset, getWidth(), getHeight()), PointHelper.getMouseX(), PointHelper.getMouseY(), 0);
            }
        };
    }
    
    @Override
    @NotNull
    public ICraftingGridHelper createCraftingGridHelper(int craftInputSlot1) {
        throw TODO();
    }
    
    @Override
    @NotNull
    public ITickTimer createTickTimer(int ticksPerCycle, int maxValue, boolean countDown) {
        throw TODO();
    }
}
