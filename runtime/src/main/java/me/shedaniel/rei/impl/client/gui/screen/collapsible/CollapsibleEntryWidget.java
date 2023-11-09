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

package me.shedaniel.rei.impl.client.gui.screen.collapsible;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import me.shedaniel.clothconfig2.ClothConfigInitializer;
import me.shedaniel.clothconfig2.api.animator.ProgressValueAnimator;
import me.shedaniel.clothconfig2.api.animator.ValueAnimator;
import me.shedaniel.clothconfig2.api.scroll.ScrollingContainer;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.ClientHelper;
import me.shedaniel.rei.api.client.config.ConfigObject;
import me.shedaniel.rei.api.client.gui.widgets.CloseableScissors;
import me.shedaniel.rei.api.client.gui.widgets.Slot;
import me.shedaniel.rei.api.client.gui.widgets.WidgetWithBounds;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.CollectionUtils;
import me.shedaniel.rei.impl.client.config.collapsible.CollapsibleConfigManager;
import me.shedaniel.rei.impl.client.gui.InternalTextures;
import me.shedaniel.rei.impl.client.gui.ScreenOverlayImpl;
import me.shedaniel.rei.impl.client.gui.text.TextTransformations;
import me.shedaniel.rei.impl.client.gui.widget.BatchedEntryRendererManager;
import me.shedaniel.rei.impl.client.gui.widget.EntryWidget;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

@SuppressWarnings("UnstableApiUsage")
public class CollapsibleEntryWidget extends WidgetWithBounds {
    private final boolean custom;
    private final ResourceLocation id;
    private final Component component;
    private final Collection<Slot> stacks;
    private final CollapsibleConfigManager.CollapsibleConfigObject configObject;
    private final ProgressValueAnimator<Boolean> idDrawer = ValueAnimator.ofBoolean();
    private final ProgressValueAnimator<Boolean> modIdDrawer = ValueAnimator.ofBoolean();
    private final Button toggleButton;
    @Nullable
    private final Button deleteButton;
    @Nullable
    private final Button configureButton;
    private final ScrollingContainer scroller = new ScrollingContainer() {
        @Override
        public Rectangle getBounds() {
            return new Rectangle(x + width / 2 - 8 * rowSize, y + 37, 16 * rowSize, height - 40);
        }
        
        @Override
        public int getMaxScrollHeight() {
            return Math.max(0, Mth.positiveCeilDiv(stacks.size(), rowSize) * 16) + 24;
        }
    };
    private int x;
    private int y;
    private int width;
    private int height;
    private int rowSize;
    
    public CollapsibleEntryWidget(boolean custom, ResourceLocation id, Component component, Collection<EntryStack<?>> stacks,
                                  CollapsibleConfigManager.CollapsibleConfigObject configObject, Runnable markDirty) {
        this.custom = custom;
        this.id = id;
        this.component = component;
        this.stacks = CollectionUtils.map(stacks, stack -> Widgets.createSlot(new Rectangle(0, 0, 16, 16))
                .entry(stack).disableBackground());
        this.configObject = configObject;
        this.toggleButton = new Button(0, 0, 20, 20, Component.translatable("text.rei.collapsible.entries.toggle"), button -> {
            if (this.configObject.disabledGroups.contains(this.id)) {
                this.configObject.disabledGroups.remove(this.id);
            } else {
                this.configObject.disabledGroups.add(this.id);
            }
        }, Supplier::get) {};
        this.toggleButton.setWidth(this.font.width(toggleButton.getMessage()) + 8);
        if (this.custom) {
            this.deleteButton = new Button(0, 0, 20, 20, Component.translatable("text.rei.collapsible.entries.delete"), button -> {
                this.configObject.customGroups.removeIf(customEntry -> customEntry.id.equals(this.id));
                markDirty.run();
            }, Supplier::get) {};
            this.deleteButton.setWidth(this.font.width(deleteButton.getMessage()) + 8);
            this.configureButton = new Button(0, 0, 20, 20, Component.nullToEmpty(null), button -> {
                CollapsibleEntriesScreen.setupCustom(this.id, this.component.getString(), new ArrayList<>(stacks), this.configObject, markDirty);
            }, Supplier::get) {
                @Override
                public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
                    RenderSystem.setShaderTexture(0, InternalTextures.CHEST_GUI_TEXTURE);
                    graphics.blit(InternalTextures.CHEST_GUI_TEXTURE, x + 3, y + 3, 0, 0, 14, 14);
                }
            };
        } else {
            this.deleteButton = null;
            this.configureButton = null;
        }
    }
    
    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }
    
    public void setWidth(int width) {
        if (this.width != width) {
            this.width = width;
            this.rowSize = Math.max(1, (width - 6) / 16);
            this.height = Math.min(42 + Math.max(Mth.positiveCeilDiv(this.stacks.size(), this.rowSize) * 16 + 24, 24), 170);
        }
    }
    
    public int getHeight() {
        return this.height;
    }
    
    @Override
    public Rectangle getBounds() {
        return new Rectangle(this.x, this.y, this.width, this.height);
    }
    
    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        this.scroller.updatePosition(delta);
        this.idDrawer.update(delta);
        this.modIdDrawer.update(delta);
        Rectangle bounds = this.getBounds();
        graphics.fillGradient(bounds.x, bounds.y, bounds.getMaxX(), bounds.getMaxY(), 0xFF777777, 0xFF777777);
        graphics.fillGradient(bounds.x + 1, bounds.y + 1, bounds.getMaxX() - 1, bounds.getMaxY() - 1, 0xFF000000, 0xFF000000);
        int y = bounds.y + 4;
        if (y + 9 >= 30 && y < minecraft.screen.height) {
            renderTextScrolling(graphics, this.component, bounds.x + 4, y, bounds.width - 8, 0xFFDDDDDD);
        }
        y += 13;
        if (y + 9 >= 30 && y < minecraft.screen.height) {
            Rectangle lineBounds = new Rectangle(bounds.x + 4, y, bounds.width - 8, 9);
            idDrawer.setTo(lineBounds.contains(mouseX, mouseY), ConfigObject.getInstance().isReducedMotion() ? 0 : 400);
            try (CloseableScissors scissors = scissor(graphics, lineBounds)) {
                graphics.pose().pushPose();
                graphics.pose().translate(0, -idDrawer.progress() * 10, 0);
                graphics.drawString(font, Component.translatable("text.rei.collapsible.entries.count", this.stacks.size() + ""), bounds.x + 4, y, 0xFFAAAAAA);
                boolean enabled = !this.configObject.disabledGroups.contains(this.id);
                Component sideText = Component.translatable("text.rei.collapsible.entries.enabled." + enabled);
                graphics.drawString(font, sideText, bounds.getMaxX() - 4 - font.width(sideText), y, enabled ? 0xDD55FF55 : 0xDDFF5555);
                renderTextScrolling(graphics, Component.literal(this.id.toString()), bounds.x + 4, y + 10, bounds.width - 8, 0xFF777777);
                graphics.pose().popPose();
            }
        }
        y += 10;
        if (y + 9 >= 30 && y < minecraft.screen.height) {
            Rectangle lineBounds = new Rectangle(bounds.x + 4, y, bounds.width - 8, 9);
            modIdDrawer.setTo(lineBounds.contains(mouseX, mouseY), ConfigObject.getInstance().isReducedMotion() ? 0 : 400);
            int xo = graphics.drawString(font, Component.translatable("text.rei.collapsible.entries.source").append(" "), bounds.x + 4, y, 0xFFAAAAAA);
            try (CloseableScissors scissors = scissor(graphics, lineBounds)) {
                graphics.pose().pushPose();
                if (this.custom) {
                    renderTextScrolling(graphics, TextTransformations.applyRainbow(Component.translatable("text.rei.collapsible.entries.source.custom").getVisualOrderText(), xo - 1, y), xo - 1, y, bounds.getWidth() - 8, 0xFFAAAAAA);
                } else {
                    graphics.pose().translate(0, -modIdDrawer.progress() * 10, 0);
                    renderTextScrolling(graphics, Component.literal(ClientHelper.getInstance().getModFromModId(this.id.getNamespace())), xo - 1, y, bounds.getMaxX() - 4 - (xo - 1), 0xFF777777);
                    renderTextScrolling(graphics, Component.literal(this.id.getNamespace().toString()), xo - 1, y + 10, bounds.getMaxX() - 4 - (xo - 1), 0xFF777777);
                }
                graphics.pose().popPose();
            }
        }
        renderStacks(graphics, mouseX, mouseY, delta, bounds, y);
        bounds.y = this.y;
        
        graphics.pose().pushPose();
        graphics.pose().translate(0, 0, 400);
        this.toggleButton.setX(bounds.getMaxX() - 4 - toggleButton.getWidth());
        this.toggleButton.setY(bounds.getMaxY() - 4 - toggleButton.getHeight());
        this.toggleButton.render(graphics, mouseX, mouseY, delta);
        if (this.toggleButton.isMouseOver(mouseX, mouseY)) {
            ScreenOverlayImpl.getInstance().clearTooltips();
        }
        if (this.custom) {
            this.deleteButton.setX(toggleButton.getX() - 2 - deleteButton.getWidth());
            this.deleteButton.setY(bounds.getMaxY() - 4 - deleteButton.getHeight());
            this.deleteButton.render(graphics, mouseX, mouseY, delta);
            if (this.deleteButton.isMouseOver(mouseX, mouseY)) {
                ScreenOverlayImpl.getInstance().clearTooltips();
            }
            this.configureButton.setX(deleteButton.getX() - 2 - configureButton.getWidth());
            this.configureButton.setY(bounds.getMaxY() - 4 - configureButton.getHeight());
            this.configureButton.render(graphics, mouseX, mouseY, delta);
            if (this.configureButton.isMouseOver(mouseX, mouseY)) {
                ScreenOverlayImpl.getInstance().clearTooltips();
            }
        }
        graphics.pose().popPose();
    }
    
    private void renderStacks(GuiGraphics graphics, int mouseX, int mouseY, float delta, Rectangle bounds, int y) {
        graphics.pose().pushPose();
        try (CloseableScissors outerScissors = scissor(graphics, new Rectangle(bounds.x, y, bounds.width, bounds.getMaxY() - 3 - y))) {
            y = bounds.y + 37 - this.scroller.scrollAmountInt();
            int x = bounds.getCenterX() - 8 * rowSize;
            int xIndex = 0;
            graphics.pose().translate(0, 0, 100);
            BatchedEntryRendererManager<EntryWidget> manager = new BatchedEntryRendererManager<>();
            for (Slot stack : this.stacks) {
                if (y + 16 >= 30 && y + 16 >= bounds.y + 37) {
                    stack.getBounds().setBounds(x + 16 * xIndex - 1, y - 1, 18, 18);
                    manager.add((EntryWidget) stack);
                }
                xIndex++;
                if (xIndex >= this.rowSize) {
                    y += 16;
                    xIndex = 0;
                    if (y >= bounds.getMaxY() || y >= minecraft.screen.height) {
                        break;
                    }
                }
            }
            try (CloseableScissors scissors = scissor(graphics, new Rectangle(x, bounds.y + 37, 16 * rowSize, bounds.getMaxY() - 4 - (bounds.y + 37)))) {
                manager.render(graphics, mouseX, mouseY, delta);
            }
            graphics.pose().translate(0, 0, 300);
            
            if (this.stacks.size() > rowSize * 3) {
                Tesselator tesselator = Tesselator.getInstance();
                BufferBuilder buffer = tesselator.getBuilder();
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                RenderSystem.setShader(GameRenderer::getPositionColorShader);
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                Matrix4f matrix = graphics.pose().last().pose();
                buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
                buffer.vertex(matrix, this.x + 1, this.y + this.height - 1, 0.0F).color(0xFF000000).endVertex();
                buffer.vertex(matrix, this.x + this.width - 1, this.y + this.height - 1, 0.0F).color(0xFF000000).endVertex();
                buffer.vertex(matrix, this.x + this.width - 1, this.y + this.height - 40, 0.0F).color(0x00000000).endVertex();
                buffer.vertex(matrix, this.x + 1, this.y + this.height - 40, 0.0F).color(0x00000000).endVertex();
                tesselator.end();
                RenderSystem.disableBlend();
            }
        }
        graphics.pose().popPose();
    }
    
    private void renderTextScrolling(GuiGraphics graphics, Component text, int x, int y, int width, int color) {
        this.renderTextScrolling(graphics, text.getVisualOrderText(), x, y, width, color);
    }
    
    private void renderTextScrolling(GuiGraphics graphics, FormattedCharSequence text, int x, int y, int width, int color) {
        try (CloseableScissors scissors = scissor(graphics, new Rectangle(x, y, width, y + 9))) {
            int textWidth = this.font.width(text);
            if (textWidth > width) {
                graphics.pose().pushPose();
                float textX = (System.currentTimeMillis() % ((textWidth + 10) * textWidth / 3)) / (float) textWidth * 3;
                graphics.pose().translate(-textX, 0, 0);
                graphics.drawString(font, text, x + width - textWidth - 10, y, color, false);
                graphics.drawString(font, text, x + width, y, color, false);
                graphics.pose().popPose();
            } else {
                graphics.drawString(font, text, x, y, color, false);
            }
        }
    }
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (this.scroller.getMaxScroll() > 0 && this.scroller.getBounds().contains(mouseX, mouseY)) {
            if ((amount < 0 || this.scroller.scrollAmountInt() != 0) && (amount > 0 || this.scroller.scrollAmountInt() != this.scroller.getMaxScroll())) {
                this.scroller.offset(ClothConfigInitializer.getScrollStep() * -amount, true);
                return true;
            }
        }
        
        return false;
    }
    
    @Override
    public List<? extends GuiEventListener> children() {
        if (this.custom) {
            return List.of(toggleButton, deleteButton, configureButton);
        } else {
            return List.of(toggleButton);
        }
    }
}
