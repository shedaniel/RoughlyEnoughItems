package me.shedaniel.reiclothconfig2.gui.widget;

import me.shedaniel.reiclothconfig2.api.RunSixtyTimesEverySec;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

public abstract class DynamicSmoothScrollingEntryListWidget<E extends DynamicEntryListWidget.Entry<E>> extends DynamicEntryListWidget<E> {
    
    protected double scrollVelocity;
    protected boolean smoothScrolling = true;
    protected RunSixtyTimesEverySec scroller = () -> {
        if (scrollVelocity == 0 && scroll >= 0 && scroll <= getMaxScroll())
            scrollerUnregisterTick();
        else {
            // Basic scrolling
            double change = scrollVelocity * 0.3d;
            if (scrollVelocity != 0) {
                scroll += change;
                scrollVelocity -= scrollVelocity * (scroll >= 0 && scroll <= getMaxScroll() ? 0.2d : .4d);
                if (Math.abs(scrollVelocity) < .1)
                    scrollVelocity = 0d;
            }
            
            // Scrolling back aka bounce
            if (scroll < 0d && scrollVelocity == 0d) {
                scroll = Math.min(scroll + (0 - scroll) * 0.2d, 0);
                if (scroll > -0.1d && scroll < 0d)
                    scroll = 0d;
            } else if (scroll > getMaxScroll() && scrollVelocity == 0d) {
                scroll = Math.max(scroll - (scroll - getMaxScroll()) * 0.2d, getMaxScroll());
                if (scroll > getMaxScroll() && scroll < getMaxScroll() + 0.1d)
                    scroll = getMaxScroll();
            }
        }
    };
    
    public DynamicSmoothScrollingEntryListWidget(Minecraft client, int width, int height, int top, int bottom, ResourceLocation backgroundLocation) {
        super(client, width, height, top, bottom, backgroundLocation);
    }
    
    protected void scrollerUnregisterTick() {
        scroller.unregisterTick();
    }
    
    public double getScrollVelocity() {
        return scrollVelocity;
    }
    
    public void setScrollVelocity(double scrollVelocity) {
        this.scrollVelocity = scrollVelocity;
    }
    
    public boolean isSmoothScrolling() {
        return smoothScrolling;
    }
    
    public void setSmoothScrolling(boolean smoothScrolling) {
        this.smoothScrolling = smoothScrolling;
    }
    
    @Override
    public void capYPosition(double double_1) {
        if (smoothScrolling)
            this.scroll = double_1;
        else
            this.scroll = MathHelper.clamp(double_1, 0.0D, (double) this.getMaxScroll());
    }
    
    @Override
    public boolean mouseDragged(double double_1, double double_2, int int_1, double double_3, double double_4) {
        if (!smoothScrolling)
            return super.mouseDragged(double_1, double_2, int_1, double_3, double_4);
        //        if (this.getFocused() != null && this.isDragging() && int_1 == 0 ? this.getFocused().mouseDragged(double_1, double_2, int_1, double_3, double_4) : false) {
        //            return true;
        //        } else
        if (super.mouseDragged(double_1, double_2, int_1, double_3, double_4)) {
            return true;
        } else if (int_1 == 0 && this.scrolling) {
            if (double_2 < (double) this.top) {
                this.capYPosition(0.0D);
            } else if (double_2 > (double) this.bottom) {
                this.capYPosition((double) this.getMaxScroll());
            } else {
                double double_5 = (double) Math.max(1, this.getMaxScroll());
                int int_2 = this.bottom - this.top;
                int int_3 = MathHelper.clamp((int) ((float) (int_2 * int_2) / (float) this.getMaxScrollPosition()), 32, int_2 - 8);
                double double_6 = Math.max(1.0D, double_5 / (double) (int_2 - int_3));
                this.capYPosition(MathHelper.clamp(this.getScroll() + double_4 * double_6, 0, getMaxScroll()));
            }
            return true;
        }
        return false;
    }
    
    @Override
    protected void scroll(int int_1) {
        super.scroll(int_1);
        this.scrollVelocity = 0d;
    }
    
    @Override
    public boolean mouseScrolled(double double_3) {
        if (!smoothScrolling) {
            this.scrollVelocity = 0d;
            if (double_3 < 0)
                scroll += 16;
            if (double_3 > 0)
                scroll -= 16;
            this.scroll = MathHelper.clamp(scroll, 0.0D, (double) this.getMaxScroll());
            return true;
        }
        if (scroll <= getMaxScroll() && double_3 < 0)
            scrollVelocity += 16;
        if (scroll >= 0 && double_3 > 0)
            scrollVelocity -= 16;
        if (!scroller.isRegistered())
            scroller.registerTick();
        return true;
    }
    
    @Override
    protected void renderScrollBar(Tessellator tessellator, BufferBuilder buffer, int maxScroll, int scrollbarPositionMinX, int scrollbarPositionMaxX) {
        if (!smoothScrolling)
            super.renderScrollBar(tessellator, buffer, maxScroll, scrollbarPositionMinX, scrollbarPositionMaxX);
        else if (maxScroll > 0) {
            int height = (int) (((this.bottom - this.top) * (this.bottom - this.top)) / this.getMaxScrollPosition());
            height = MathHelper.clamp(height, 32, this.bottom - this.top - 8);
            height -= Math.min((scroll < 0 ? (int) -scroll : scroll > getMaxScroll() ? (int) scroll - getMaxScroll() : 0), height * .75);
            int minY = Math.min(Math.max((int) this.getScroll() * (this.bottom - this.top - height) / maxScroll + this.top, this.top), this.bottom - height);
            
            // Black Bar
            buffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
            buffer.pos(scrollbarPositionMinX, this.bottom, 0.0D).tex(0.0D, 1.0D).color(0, 0, 0, 255).endVertex();
            buffer.pos(scrollbarPositionMaxX, this.bottom, 0.0D).tex(1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
            buffer.pos(scrollbarPositionMaxX, this.top, 0.0D).tex(1.0D, 0.0D).color(0, 0, 0, 255).endVertex();
            buffer.pos(scrollbarPositionMinX, this.top, 0.0D).tex(0.0D, 0.0D).color(0, 0, 0, 255).endVertex();
            tessellator.draw();
            
            // Top
            buffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
            buffer.pos(scrollbarPositionMinX, minY + height, 0.0D).tex(0.0D, 1.0D).color(128, 128, 128, 255).endVertex();
            buffer.pos(scrollbarPositionMaxX, minY + height, 0.0D).tex(1.0D, 1.0D).color(128, 128, 128, 255).endVertex();
            buffer.pos(scrollbarPositionMaxX, minY, 0.0D).tex(1.0D, 0.0D).color(128, 128, 128, 255).endVertex();
            buffer.pos(scrollbarPositionMinX, minY, 0.0D).tex(0.0D, 0.0D).color(128, 128, 128, 255).endVertex();
            tessellator.draw();
            
            // Bottom
            buffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
            buffer.pos(scrollbarPositionMinX, (minY + height - 1), 0.0D).tex(0.0D, 1.0D).color(192, 192, 192, 255).endVertex();
            buffer.pos((scrollbarPositionMaxX - 1), (minY + height - 1), 0.0D).tex(1.0D, 1.0D).color(192, 192, 192, 255).endVertex();
            buffer.pos((scrollbarPositionMaxX - 1), minY, 0.0D).tex(1.0D, 0.0D).color(192, 192, 192, 255).endVertex();
            buffer.pos(scrollbarPositionMinX, minY, 0.0D).tex(0.0D, 0.0D).color(192, 192, 192, 255).endVertex();
            tessellator.draw();
        }
    }
    
}
