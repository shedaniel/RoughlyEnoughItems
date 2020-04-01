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

package me.shedaniel.rei.plugin.information;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import me.shedaniel.clothconfig2.ClothConfigInitializer;
import me.shedaniel.clothconfig2.api.ScissorsHandler;
import me.shedaniel.math.api.Point;
import me.shedaniel.math.api.Rectangle;
import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.api.RecipeCategory;
import me.shedaniel.rei.gui.entries.RecipeEntry;
import me.shedaniel.rei.gui.widget.*;
import me.shedaniel.rei.impl.RenderingEntry;
import me.shedaniel.rei.impl.ScreenHelper;
import me.shedaniel.rei.plugin.DefaultPlugin;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.Texts;
import net.minecraft.client.util.math.Matrix4f;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public class DefaultInformationCategory implements RecipeCategory<DefaultInformationDisplay> {
    protected static void innerBlit(Matrix4f matrix4f, int xStart, int xEnd, int yStart, int yEnd, int z, float uStart, float uEnd, float vStart, float vEnd) {
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        bufferBuilder.begin(7, VertexFormats.POSITION_TEXTURE);
        bufferBuilder.vertex(matrix4f, xStart, yEnd, z).texture(uStart, vEnd).next();
        bufferBuilder.vertex(matrix4f, xEnd, yEnd, z).texture(uEnd, vEnd).next();
        bufferBuilder.vertex(matrix4f, xEnd, yStart, z).texture(uEnd, vStart).next();
        bufferBuilder.vertex(matrix4f, xStart, yStart, z).texture(uStart, vStart).next();
        bufferBuilder.end();
        RenderSystem.enableAlphaTest();
        BufferRenderer.draw(bufferBuilder);
    }
    
    @Override
    public Identifier getIdentifier() {
        return DefaultPlugin.INFO;
    }
    
    @Override
    public String getCategoryName() {
        return I18n.translate("category.rei.information");
    }
    
    @Override
    public RecipeEntry getSimpleRenderer(DefaultInformationDisplay recipe) {
        Text name = recipe.getName();
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
                MinecraftClient.getInstance().textRenderer.draw(name.asFormattedString(), rectangle.x + 5, rectangle.y + 6, -1);
            }
        };
    }
    
    @Override
    public EntryStack getLogo() {
        return new RenderingEntry() {
            @Override
            public void render(Rectangle bounds, int mouseX, int mouseY, float delta) {
                MinecraftClient.getInstance().getTextureManager().bindTexture(DefaultPlugin.getDisplayTexture());
                Matrix4f matrix4f = Matrix4f.translate(-1.2f, -1, 0);
                DefaultInformationCategory.innerBlit(matrix4f, bounds.getCenterX() - 8, bounds.getCenterX() + 8, bounds.getCenterY() - 8, bounds.getCenterY() + 8, 0, 116f / 256f, (116f + 16f) / 256f, 0f, 16f / 256f);
            }
        };
    }
    
    @Override
    public List<Widget> setupDisplay(Supplier<DefaultInformationDisplay> recipeDisplaySupplier, Rectangle bounds) {
        DefaultInformationDisplay display = recipeDisplaySupplier.get();
        List<Widget> widgets = Lists.newArrayList();
        widgets.add(LabelWidget.create(new Point(bounds.getCenterX(), bounds.y + 3), display.getName().asFormattedString()).noShadow().color(ScreenHelper.isDarkModeEnabled() ? 0xFFBBBBBB : 0xFF404040));
        widgets.add(EntryWidget.create(bounds.getCenterX() - 8, bounds.y + 15).entries(display.getEntryStacks()).markIsInput());
        Rectangle rectangle = new Rectangle(bounds.getCenterX() - (bounds.width / 2), bounds.y + 35, bounds.width, bounds.height - 40);
        widgets.add(new SlotBaseWidget(rectangle));
        widgets.add(new ScrollableTextWidget(rectangle, display.getTexts()));
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
    
    private static class ScrollableTextWidget extends WidgetWithBounds {
        private Rectangle bounds;
        private List<Text> texts;
        private final ScrollingContainer scrolling = new ScrollingContainer() {
            @Override
            public Rectangle getBounds() {
                Rectangle bounds = ScrollableTextWidget.this.getBounds();
                return new Rectangle(bounds.x + 1, bounds.y + 1, bounds.width - 2, bounds.height - 2);
            }
            
            @Override
            public int getMaxScrollHeight() {
                int i = 2;
                for (Text entry : texts) {
                    i += entry == null ? 4 : font.fontHeight;
                }
                return i;
            }
        };
        
        public ScrollableTextWidget(Rectangle bounds, List<Text> texts) {
            this.bounds = Objects.requireNonNull(bounds);
            this.texts = Lists.newArrayList();
            for (Text text : texts) {
                if (!this.texts.isEmpty())
                    this.texts.add(null);
                this.texts.addAll(Texts.wrapLines(text, bounds.width - 11, MinecraftClient.getInstance().textRenderer, true, false));
            }
        }
        
        @Override
        public boolean mouseScrolled(double double_1, double double_2, double double_3) {
            if (containsMouse(double_1, double_2)) {
                scrolling.offset(ClothConfigInitializer.getScrollStep() * -double_3, true);
                return true;
            }
            return false;
        }
        
        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (scrolling.updateDraggingState(mouseX, mouseY, button))
                return true;
            return super.mouseClicked(mouseX, mouseY, button);
        }
        
        @Override
        public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
            if (scrolling.mouseDragged(mouseX, mouseY, button, deltaX, deltaY))
                return true;
            return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
        }
        
        @Override
        public Rectangle getBounds() {
            return bounds;
        }
        
        @Override
        public void render(int mouseX, int mouseY, float delta) {
            scrolling.updatePosition(delta);
            Rectangle innerBounds = scrolling.getScissorBounds();
            ScissorsHandler.INSTANCE.scissor(innerBounds);
            int currentY = (int) -scrolling.scrollAmount + innerBounds.y;
            for (Text text : texts) {
                if (text != null && currentY + font.fontHeight >= innerBounds.y && currentY <= innerBounds.getMaxY()) {
                    font.draw(text.asFormattedString(), innerBounds.x + 2, currentY + 2, ScreenHelper.isDarkModeEnabled() ? 0xFFBBBBBB : 0xFF090909);
                }
                currentY += text == null ? 4 : font.fontHeight;
            }
            ScissorsHandler.INSTANCE.removeLastScissor();
            ScissorsHandler.INSTANCE.scissor(scrolling.getBounds());
            scrolling.renderScrollBar(0xff000000, 1);
            ScissorsHandler.INSTANCE.removeLastScissor();
        }
        
        @Override
        public List<? extends Element> children() {
            return Collections.emptyList();
        }
    }
}
