package me.shedaniel.gui.widget;

import com.mojang.blaze3d.platform.GlStateManager;
import me.shedaniel.api.TriBooleanProducer;
import me.shedaniel.gui.Drawable;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;

import java.awt.*;
import java.util.function.BiConsumer;
import java.util.function.IntFunction;

/**
 * Created by James on 7/29/2018.
 */
public abstract class Control extends Drawable {
    private boolean enabled = true;
    public IntFunction<Boolean> onClick;
    public TriBooleanProducer onKeyDown;
    public BiConsumer<Character, Integer> charPressed;
    
    public Control(int x, int y, int width, int height) {
        super(x, y, width, height);
    }
    
    public Control(Rectangle rect) {
        super(rect);
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public void move(int x, int y) {
        rect.x += x;
        rect.y += y;
        //rect.move(x+rect.x,rect.y+y);//Why the fuck?
    }
    
    protected static void drawRect(int p_drawRect_0_, int p_drawRect_1_, int p_drawRect_2_, int p_drawRect_3_, int p_drawRect_4_) {
        int lvt_5_3_;
        if (p_drawRect_0_ < p_drawRect_2_) {
            lvt_5_3_ = p_drawRect_0_;
            p_drawRect_0_ = p_drawRect_2_;
            p_drawRect_2_ = lvt_5_3_;
        }
        
        if (p_drawRect_1_ < p_drawRect_3_) {
            lvt_5_3_ = p_drawRect_1_;
            p_drawRect_1_ = p_drawRect_3_;
            p_drawRect_3_ = lvt_5_3_;
        }
        
        float lvt_5_3_1 = (float) (p_drawRect_4_ >> 24 & 255) / 255.0F;
        float lvt_6_1_ = (float) (p_drawRect_4_ >> 16 & 255) / 255.0F;
        float lvt_7_1_ = (float) (p_drawRect_4_ >> 8 & 255) / 255.0F;
        float lvt_8_1_ = (float) (p_drawRect_4_ & 255) / 255.0F;
        Tessellator lvt_9_1_ = Tessellator.getInstance();
        BufferBuilder lvt_10_1_ = lvt_9_1_.getBufferBuilder();
        GlStateManager.enableAlphaTest();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture();
        GlStateManager.blendFuncSeparate(GlStateManager.SrcBlendFactor.SRC_ALPHA, GlStateManager.DstBlendFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SrcBlendFactor.ONE, GlStateManager.DstBlendFactor.ZERO);
        GlStateManager.color4f(lvt_6_1_, lvt_7_1_, lvt_8_1_, lvt_5_3_1);
        lvt_10_1_.begin(7, VertexFormats.POSITION);
        lvt_10_1_.vertex((double) p_drawRect_0_, (double) p_drawRect_3_, 0.0D).next();
        lvt_10_1_.vertex((double) p_drawRect_2_, (double) p_drawRect_3_, 0.0D).next();
        lvt_10_1_.vertex((double) p_drawRect_2_, (double) p_drawRect_1_, 0.0D).next();
        lvt_10_1_.vertex((double) p_drawRect_0_, (double) p_drawRect_1_, 0.0D).next();
        lvt_9_1_.draw();
        GlStateManager.enableTexture();
        GlStateManager.disableBlend();
        GlStateManager.disableAlphaTest();
    }
    
    protected void drawTexturedModalRect(int x, int y, int u, int v, int width, int height) {
        float lvt_7_1_ = 0.00390625F;
        float lvt_8_1_ = 0.00390625F;
        Tessellator lvt_9_1_ = Tessellator.getInstance();
        BufferBuilder lvt_10_1_ = lvt_9_1_.getBufferBuilder();
        lvt_10_1_.begin(7, VertexFormats.POSITION_UV);
        lvt_10_1_.vertex((double) (x + 0), (double) (y + height), (double) 200).texture((double) ((float) (u + 0) * 0.00390625F), (double) ((float) (v + height) * 0.00390625F)).next();
        lvt_10_1_.vertex((double) (x + width), (double) (y + height), (double) 200).texture((double) ((float) (u + width) * 0.00390625F), (double) ((float) (v + height) * 0.00390625F)).next();
        lvt_10_1_.vertex((double) (x + width), (double) (y + 0), (double) 200).texture((double) ((float) (u + width) * 0.00390625F), (double) ((float) (v + 0) * 0.00390625F)).next();
        lvt_10_1_.vertex((double) (x + 0), (double) (y + 0), (double) 200).texture((double) ((float) (u + 0) * 0.00390625F), (double) ((float) (v + 0) * 0.00390625F)).next();
        lvt_9_1_.draw();
    }
    
    protected void drawTexturedModalRect(int x, int y, int u, int v, int width, int height, float zLevel) {
        float lvt_7_1_ = 0.00390625F;
        float lvt_8_1_ = 0.00390625F;
        Tessellator lvt_9_1_ = Tessellator.getInstance();
        BufferBuilder lvt_10_1_ = lvt_9_1_.getBufferBuilder();
        lvt_10_1_.begin(7, VertexFormats.POSITION_UV);
        lvt_10_1_.vertex((double) (x + 0), (double) (y + height), zLevel).texture((double) ((float) (u + 0) * 0.00390625F), (double) ((float) (v + height) * 0.00390625F)).next();
        lvt_10_1_.vertex((double) (x + width), (double) (y + height), zLevel).texture((double) ((float) (u + width) * 0.00390625F), (double) ((float) (v + height) * 0.00390625F)).next();
        lvt_10_1_.vertex((double) (x + width), (double) (y + 0), zLevel).texture((double) ((float) (u + width) * 0.00390625F), (double) ((float) (v + 0) * 0.00390625F)).next();
        lvt_10_1_.vertex((double) (x + 0), (double) (y + 0), zLevel).texture((double) ((float) (u + 0) * 0.00390625F), (double) ((float) (v + 0) * 0.00390625F)).next();
        lvt_9_1_.draw();
    }
    
    
    public void tick() {
    }
    
    public void setOnClick(IntFunction<Boolean> onClick) {
        this.onClick = onClick;
    }
    
}
