/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020 shedaniel
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package me.shedaniel.rei.plugin.beacon;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import me.shedaniel.clothconfig2.ClothConfigInitializer;
import me.shedaniel.clothconfig2.api.ScissorsHandler;
import me.shedaniel.clothconfig2.gui.widget.DynamicEntryListWidget;
import me.shedaniel.math.api.Rectangle;
import me.shedaniel.math.impl.PointHelper;
import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.api.RecipeCategory;
import me.shedaniel.rei.gui.entries.RecipeEntry;
import me.shedaniel.rei.gui.widget.*;
import me.shedaniel.rei.plugin.DefaultPlugin;
import me.shedaniel.rei.utils.CollectionUtils;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

public class DefaultBeaconBaseCategory implements RecipeCategory<DefaultBeaconBaseDisplay> {
    @Override
    public Identifier getIdentifier() {
        return DefaultPlugin.BEACON;
    }
    
    @Override
    public String getCategoryName() {
        return I18n.translate("category.rei.beacon_base");
    }
    
    @Override
    public EntryStack getLogo() {
        return EntryStack.create(Blocks.BEACON);
    }
    
    @Override
    public RecipeEntry getSimpleRenderer(DefaultBeaconBaseDisplay recipe) {
        String name = getCategoryName();
        return new RecipeEntry() {
            @Override
            public int getHeight() {
                return 10 + MinecraftClient.getInstance().textRenderer.fontHeight;
            }
            
            @Nullable
            @Override
            public QueuedTooltip getTooltip(int mouseX, int mouseY) {
                return null;
            }
            
            @Override
            public void render(Rectangle rectangle, int mouseX, int mouseY, float delta) {
                MinecraftClient.getInstance().textRenderer.draw(name, rectangle.x + 5, rectangle.y + 6, -1);
            }
        };
    }
    
    @Override
    public List<Widget> setupDisplay(Supplier<DefaultBeaconBaseDisplay> recipeDisplaySupplier, Rectangle bounds) {
        DefaultBeaconBaseDisplay display = recipeDisplaySupplier.get();
        List<Widget> widgets = Lists.newArrayList();
        widgets.add(EntryWidget.create(bounds.getCenterX() - 8, bounds.y + 3).entry(getLogo()));
        Rectangle rectangle = new Rectangle(bounds.getCenterX() - (bounds.width / 2) - 1, bounds.y + 23, bounds.width + 2, bounds.height - 28);
        widgets.add(new SlotBaseWidget(rectangle));
        widgets.add(new ScrollableSlotsWidget(rectangle, CollectionUtils.map(display.getEntries(), t -> EntryWidget.create(0, 0).noBackground().entry(t))));
        return widgets;
    }
    
    @Override
    public int getDisplayHeight() {
        return 140;
    }
    
    @Override
    public int getFixedRecipesPerPage() {
        return 1;
    }
    
    private static class ScrollableSlotsWidget extends WidgetWithBounds {
        private Rectangle bounds;
        private List<EntryWidget> widgets;
        private double target;
        private double scroll;
        private long start;
        private long duration;
        
        public ScrollableSlotsWidget(Rectangle bounds, List<EntryWidget> widgets) {
            this.bounds = bounds;
            this.widgets = Lists.newArrayList(widgets);
        }
        
        @Override
        public boolean mouseScrolled(double double_1, double double_2, double double_3) {
            if (containsMouse(double_1, double_2)) {
                offset(ClothConfigInitializer.getScrollStep() * -double_3, true);
                return true;
            }
            return false;
        }
        
        public void offset(double value, boolean animated) {
            scrollTo(target + value, animated);
        }
        
        public void scrollTo(double value, boolean animated) {
            scrollTo(value, animated, ClothConfigInitializer.getScrollDuration());
        }
        
        public void scrollTo(double value, boolean animated, long duration) {
            target = clamp(value);
            
            if (animated) {
                start = System.currentTimeMillis();
                this.duration = duration;
            } else
                scroll = target;
        }
        
        public final double clamp(double v) {
            return clamp(v, DynamicEntryListWidget.SmoothScrollingSettings.CLAMP_EXTENSION);
        }
        
        public final double clamp(double v, double clampExtension) {
            return MathHelper.clamp(v, -clampExtension, getMaxScroll() + clampExtension);
        }
        
        protected int getMaxScroll() {
            return Math.max(0, this.getMaxScrollPosition() - this.getBounds().height + 1);
        }
        
        protected int getMaxScrollPosition() {
            return MathHelper.ceil(widgets.size() / 8f) * 18;
        }
        
        @Override
        public Rectangle getBounds() {
            return bounds;
        }
        
        @Override
        public void render(int mouseX, int mouseY, float delta) {
            updatePosition(delta);
            Rectangle innerBounds = new Rectangle(bounds.x + 1, bounds.y + 1, bounds.width - 7, bounds.height - 2);
            ScissorsHandler.INSTANCE.scissor(innerBounds);
            for (int y = 0; y < MathHelper.ceil(widgets.size() / 8f); y++) {
                for (int x = 0; x < 8; x++) {
                    int index = y * 8 + x;
                    if (widgets.size() <= index)
                        break;
                    EntryWidget widget = widgets.get(index);
                    widget.getBounds().setLocation(bounds.x + 1 + x * 18, (int) (bounds.y + 1 + y * 18 - scroll));
                    widget.render(mouseX, mouseY, delta);
                }
            }
            ScissorsHandler.INSTANCE.removeLastScissor();
            ScissorsHandler.INSTANCE.scissor(bounds);
            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(770, 771, 0, 1);
            RenderSystem.disableAlphaTest();
            RenderSystem.shadeModel(7425);
            RenderSystem.disableTexture();
            renderScrollBar();
            RenderSystem.enableTexture();
            RenderSystem.shadeModel(7424);
            RenderSystem.enableAlphaTest();
            RenderSystem.disableBlend();
            ScissorsHandler.INSTANCE.removeLastScissor();
        }
        
        @SuppressWarnings("deprecation")
        private void renderScrollBar() {
            int maxScroll = getMaxScroll();
            int scrollbarPositionMinX = getBounds().getMaxX() - 7;
            int scrollbarPositionMaxX = scrollbarPositionMinX + 6;
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();
            if (maxScroll > 0) {
                int height = (int) (((this.getBounds().height - 2f) * (this.getBounds().height - 2f)) / this.getMaxScrollPosition());
                height = MathHelper.clamp(height, 32, this.getBounds().height - 2);
                height -= Math.min((scroll < 0 ? (int) -scroll : scroll > maxScroll ? (int) scroll - maxScroll : 0), height * .95);
                height = Math.max(10, height);
                int minY = Math.min(Math.max((int) scroll * (this.getBounds().height - 2 - height) / maxScroll + getBounds().y + 1, getBounds().y + 1), getBounds().getMaxY() - 1 - height);
                
                boolean hovered = new Rectangle(scrollbarPositionMinX, minY, scrollbarPositionMaxX - scrollbarPositionMinX, height).contains(PointHelper.fromMouse());
                int bottomC = hovered ? 168 : 128;
                int topC = hovered ? 222 : 172;
                
                // Black Bar
                buffer.begin(7, VertexFormats.POSITION_TEXTURE_COLOR);
                buffer.vertex(scrollbarPositionMinX, this.getBounds().y + 1, 0.0D).texture(0, 1).color(0, 0, 0, 255).next();
                buffer.vertex(scrollbarPositionMaxX, this.getBounds().y + 1, 0.0D).texture(1, 1).color(0, 0, 0, 255).next();
                buffer.vertex(scrollbarPositionMaxX, getBounds().getMaxY() - 1, 0.0D).texture(1, 0).color(0, 0, 0, 255).next();
                buffer.vertex(scrollbarPositionMinX, getBounds().getMaxY() - 1, 0.0D).texture(0, 0).color(0, 0, 0, 255).next();
                tessellator.draw();
                
                // Bottom
                buffer.begin(7, VertexFormats.POSITION_TEXTURE_COLOR);
                buffer.vertex(scrollbarPositionMinX, minY + height, 0.0D).texture(0, 1).color(bottomC, bottomC, bottomC, 255).next();
                buffer.vertex(scrollbarPositionMaxX, minY + height, 0.0D).texture(1, 1).color(bottomC, bottomC, bottomC, 255).next();
                buffer.vertex(scrollbarPositionMaxX, minY, 0.0D).texture(1, 0).color(bottomC, bottomC, bottomC, 255).next();
                buffer.vertex(scrollbarPositionMinX, minY, 0.0D).texture(0, 0).color(bottomC, bottomC, bottomC, 255).next();
                tessellator.draw();
                
                // Top
                buffer.begin(7, VertexFormats.POSITION_TEXTURE_COLOR);
                buffer.vertex(scrollbarPositionMinX, (minY + height - 1), 0.0D).texture(0, 1).color(topC, topC, topC, 255).next();
                buffer.vertex((scrollbarPositionMaxX - 1), (minY + height - 1), 0.0D).texture(1, 1).color(topC, topC, topC, 255).next();
                buffer.vertex((scrollbarPositionMaxX - 1), minY, 0.0D).texture(1, 0).color(topC, topC, topC, 255).next();
                buffer.vertex(scrollbarPositionMinX, minY, 0.0D).texture(0, 0).color(topC, topC, topC, 255).next();
                tessellator.draw();
            }
        }
        
        private void updatePosition(float delta) {
            double[] target = new double[]{this.target};
            this.scroll = ClothConfigInitializer.handleScrollingPosition(target, this.scroll, this.getMaxScroll(), delta, this.start, this.duration);
            this.target = target[0];
        }
        
        @Override
        public List<? extends Element> children() {
            return widgets;
        }
    }
}
