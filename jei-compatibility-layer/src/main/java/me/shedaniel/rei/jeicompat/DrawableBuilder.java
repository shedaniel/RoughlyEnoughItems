package me.shedaniel.rei.jeicompat;

import com.mojang.blaze3d.vertex.PoseStack;
import me.shedaniel.clothconfig2.forge.api.PointHelper;
import me.shedaniel.rei.api.gui.widgets.Widget;
import me.shedaniel.rei.api.gui.widgets.Widgets;
import mezz.jei.api.gui.ITickTimer;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.drawable.IDrawableBuilder;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import static me.shedaniel.rei.jeicompat.JEIPluginDetector.TODO;

public class DrawableBuilder implements IDrawableBuilder {
    private ResourceLocation texture;
    private int u;
    private int v;
    private int width;
    private int height;
    private int textureWidth = 256;
    private int textureHeight = 256;
    private int paddingTop;
    private int paddingBottom;
    private int paddingLeft;
    private int paddingRight;
    
    public DrawableBuilder(ResourceLocation texture, int u, int v, int width, int height) {
        this.texture = texture;
        this.u = u;
        this.v = v;
        this.width = width;
        this.height = height;
    }
    
    @Override
    @NotNull
    public IDrawableBuilder setTextureSize(int width, int height) {
        this.textureWidth = width;
        this.textureHeight = height;
        return this;
    }
    
    @Override
    @NotNull
    public IDrawableBuilder addPadding(int paddingTop, int paddingBottom, int paddingLeft, int paddingRight) {
        this.paddingTop = paddingTop;
        this.paddingBottom = paddingBottom;
        this.paddingLeft = paddingLeft;
        this.paddingRight = paddingRight;
        return this;
    }
    
    @Override
    @NotNull
    public IDrawableBuilder trim(int trimTop, int trimBottom, int trimLeft, int trimRight) {
        this.u += trimLeft;
        this.v += trimTop;
        this.width -= trimLeft + trimRight;
        this.height -= trimTop + trimBottom;
        return this;
    }
    
    @Override
    @NotNull
    public IDrawableStatic build() {
        int actualWidth = width + paddingLeft + paddingRight;
        int actualHeight = height + paddingTop + paddingBottom;
        Widget widget = Widgets.createTexturedWidget(texture, 0, 0, u, v, width, height, textureWidth, textureHeight);
        return new IDrawableStatic() {
            @Override
            public void draw(@NotNull PoseStack matrixStack, int xOffset, int yOffset, int maskTop, int maskBottom, int maskLeft, int maskRight) {
                matrixStack.pushPose();
                matrixStack.translate(xOffset + paddingLeft, yOffset + paddingTop, 0);
                widget.render(matrixStack, PointHelper.getMouseX(), PointHelper.getMouseY(), 0);
                matrixStack.popPose();
            }
            
            @Override
            public void draw(@NotNull PoseStack matrixStack, int xOffset, int yOffset) {
                draw(matrixStack, xOffset, yOffset, 0, 0, 0, 0);
            }
            
            @Override
            public int getWidth() {
                return actualWidth;
            }
            
            @Override
            public int getHeight() {
                return actualHeight;
            }
        };
    }
    
    @Override
    @NotNull
    public IDrawableAnimated buildAnimated(int ticksPerCycle, @NotNull IDrawableAnimated.StartDirection startDirection, boolean inverted) {
        return JEIGuiHelper.INSTANCE.createAnimatedDrawable(build(), ticksPerCycle, startDirection, inverted);
    }
    
    @Override
    @NotNull
    public IDrawableAnimated buildAnimated(@NotNull ITickTimer tickTimer, @NotNull IDrawableAnimated.StartDirection startDirection) {
        throw TODO();
    }
}
