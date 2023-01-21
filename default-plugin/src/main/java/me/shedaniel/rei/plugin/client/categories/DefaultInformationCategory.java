/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020, 2021, 2022, 2023 shedaniel
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

package me.shedaniel.rei.plugin.client.categories;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import me.shedaniel.clothconfig2.ClothConfigInitializer;
import me.shedaniel.clothconfig2.api.scroll.ScrollingContainer;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.REIRuntime;
import me.shedaniel.rei.api.client.gui.AbstractRenderer;
import me.shedaniel.rei.api.client.gui.DisplayRenderer;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.CloseableScissors;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.WidgetWithBounds;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.plugin.common.BuiltinPlugin;
import me.shedaniel.rei.plugin.common.displays.DefaultInformationDisplay;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;
import org.joml.Matrix4f;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Environment(EnvType.CLIENT)
public class DefaultInformationCategory implements DisplayCategory<DefaultInformationDisplay> {
    protected static void innerBlit(Matrix4f matrix4f, int xStart, int xEnd, int yStart, int yEnd, int z, float uStart, float uEnd, float vStart, float vEnd) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferBuilder.vertex(matrix4f, xStart, yEnd, z).uv(uStart, vEnd).endVertex();
        bufferBuilder.vertex(matrix4f, xEnd, yEnd, z).uv(uEnd, vEnd).endVertex();
        bufferBuilder.vertex(matrix4f, xEnd, yStart, z).uv(uEnd, vStart).endVertex();
        bufferBuilder.vertex(matrix4f, xStart, yStart, z).uv(uStart, vStart).endVertex();
        BufferUploader.drawWithShader(bufferBuilder.end());
    }
    
    @Override
    public CategoryIdentifier<? extends DefaultInformationDisplay> getCategoryIdentifier() {
        return BuiltinPlugin.INFO;
    }
    
    @Override
    public Component getTitle() {
        return Component.translatable("category.rei.information");
    }
    
    @Override
    public DisplayRenderer getDisplayRenderer(DefaultInformationDisplay display) {
        FormattedCharSequence name = display.getName().getVisualOrderText();
        return new DisplayRenderer() {
            @Override
            public int getHeight() {
                return 10 + Minecraft.getInstance().font.lineHeight;
            }
            
            @Override
            public void render(PoseStack matrices, Rectangle rectangle, int mouseX, int mouseY, float delta) {
                Minecraft.getInstance().font.draw(matrices, name, rectangle.x + 5, rectangle.y + 6, -1);
            }
        };
    }
    
    @Override
    public Renderer getIcon() {
        return new AbstractRenderer() {
            @Override
            public void render(PoseStack matrices, Rectangle bounds, int mouseX, int mouseY, float delta) {
                RenderSystem.setShaderTexture(0, REIRuntime.getInstance().getDefaultDisplayTexture());
                matrices.pushPose();
                matrices.translate(-1.2f, -1, 0);
                Matrix4f matrix = matrices.last().pose();
                DefaultInformationCategory.innerBlit(matrix, bounds.getCenterX() - 8, bounds.getCenterX() + 8, bounds.getCenterY() - 8, bounds.getCenterY() + 8, 0, 116f / 256f, (116f + 16f) / 256f, 0f, 16f / 256f);
                matrices.popPose();
            }
        };
    }
    
    @Override
    public List<Widget> setupDisplay(DefaultInformationDisplay display, Rectangle bounds) {
        List<Widget> widgets = Lists.newArrayList();
        widgets.add(Widgets.createRecipeBase(bounds));
        widgets.add(Widgets.createSlot(new Point(bounds.getCenterX() - 8, bounds.y + 15 - 9)).entries(display.getEntryStacks()));
        Rectangle rectangle = new Rectangle(bounds.getCenterX() - (bounds.width * 0.95 / 2), bounds.y + 35 - 9, bounds.width * 0.95, bounds.height - 40 + 9);
        widgets.add(new ScrollableTextWidget(rectangle, display.getTexts()));
        return widgets;
    }
    
    @Override
    public int getDisplayHeight() {
        return 140;
    }
    
    @Override
    public int getFixedDisplaysPerPage() {
        return 1;
    }
    
    private static class ScrollableTextWidget extends WidgetWithBounds {
        private Rectangle bounds;
        private List<FormattedCharSequence> texts;
        private final ScrollingContainer scrolling = new ScrollingContainer() {
            @Override
            public Rectangle getBounds() {
                Rectangle bounds = ScrollableTextWidget.this.getBounds();
                return new Rectangle(bounds.x + 1, bounds.y + 1, bounds.width - 2, bounds.height - 2);
            }
            
            @Override
            public int getMaxScrollHeight() {
                int i = 2;
                for (FormattedCharSequence entry : texts) {
                    i += entry == null ? 4 : font.lineHeight;
                }
                return i;
            }
        };
        
        public ScrollableTextWidget(Rectangle bounds, List<Component> texts) {
            this.bounds = Objects.requireNonNull(bounds);
            this.texts = Lists.newArrayList();
            for (FormattedText text : texts) {
                if (!this.texts.isEmpty())
                    this.texts.add(null);
                this.texts.addAll(Minecraft.getInstance().font.split(text, bounds.width - 11));
            }
        }
        
        @Override
        public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
            if (containsMouse(mouseX, mouseY)) {
                scrolling.offset(ClothConfigInitializer.getScrollStep() * -amount, true);
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
        public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
            scrolling.updatePosition(delta);
            Rectangle innerBounds = scrolling.getScissorBounds();
            try (CloseableScissors scissors = scissor(matrices, innerBounds)) {
                int currentY = -scrolling.scrollAmountInt() + innerBounds.y;
                for (FormattedCharSequence text : texts) {
                    if (text != null && currentY + font.lineHeight >= innerBounds.y && currentY <= innerBounds.getMaxY()) {
                        font.draw(matrices, text, innerBounds.x + 2, currentY + 2, REIRuntime.getInstance().isDarkThemeEnabled() ? 0xFFBBBBBB : 0xFF090909);
                    }
                    currentY += text == null ? 4 : font.lineHeight;
                }
            }
            if (scrolling.hasScrollBar()) {
                if (scrolling.scrollAmount() > 8) {
                    fillGradient(matrices, innerBounds.x, innerBounds.y, innerBounds.getMaxX(), innerBounds.y + 16, 0xFFC6C6C6, 0x00C6C6C6);
                }
                if (scrolling.getMaxScroll() - scrolling.scrollAmount() > 8) {
                    fillGradient(matrices, innerBounds.x, innerBounds.getMaxY() - 16, innerBounds.getMaxX(), innerBounds.getMaxY(), 0x00C6C6C6, 0xFFC6C6C6);
                }
            }
            try (CloseableScissors scissors = scissor(matrices, scrolling.getBounds())) {
                scrolling.renderScrollBar(0, 1, 1f);
            }
        }
        
        @Override
        public List<? extends GuiEventListener> children() {
            return Collections.emptyList();
        }
    }
}
