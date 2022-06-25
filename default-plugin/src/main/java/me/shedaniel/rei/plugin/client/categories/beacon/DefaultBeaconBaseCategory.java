/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020, 2021, 2022 shedaniel
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

package me.shedaniel.rei.plugin.client.categories.beacon;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import me.shedaniel.clothconfig2.ClothConfigInitializer;
import me.shedaniel.clothconfig2.api.ScissorsHandler;
import me.shedaniel.clothconfig2.api.scroll.ScrollingContainer;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.REIRuntime;
import me.shedaniel.rei.api.client.gui.DisplayRenderer;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.*;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.util.CollectionUtils;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.shedaniel.rei.plugin.common.BuiltinPlugin;
import me.shedaniel.rei.plugin.common.displays.beacon.DefaultBeaconBaseDisplay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;

import java.util.List;
import java.util.Objects;

public class DefaultBeaconBaseCategory implements DisplayCategory<DefaultBeaconBaseDisplay> {
    @Override
    public CategoryIdentifier<? extends DefaultBeaconBaseDisplay> getCategoryIdentifier() {
        return BuiltinPlugin.BEACON_BASE;
    }
    
    @Override
    public Component getTitle() {
        return Component.translatable("category.rei.beacon_base");
    }
    
    @Override
    public Renderer getIcon() {
        return EntryStacks.of(Blocks.BEACON);
    }
    
    @Override
    public DisplayRenderer getDisplayRenderer(DefaultBeaconBaseDisplay display) {
        Component name = getTitle();
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
    public List<Widget> setupDisplay(DefaultBeaconBaseDisplay display, Rectangle bounds) {
        List<Widget> widgets = Lists.newArrayList();
        widgets.add(Widgets.createSlot(new Point(bounds.getCenterX() - 8, bounds.y + 3)).entry(EntryStacks.of(Blocks.BEACON)));
        Rectangle rectangle = new Rectangle(bounds.getCenterX() - (bounds.width / 2) - 1, bounds.y + 23, bounds.width + 2, bounds.height - 28);
        widgets.add(Widgets.createSlotBase(rectangle));
        widgets.add(new ScrollableSlotsWidget(rectangle, CollectionUtils.map(display.getEntries(), t -> Widgets.createSlot(new Point(0, 0)).disableBackground().entry(t))));
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
    
    private static class ScrollableSlotsWidget extends WidgetWithBounds {
        private Rectangle bounds;
        private List<Slot> widgets;
        private final ScrollingContainer scrolling = new ScrollingContainer() {
            @Override
            public Rectangle getBounds() {
                Rectangle bounds = ScrollableSlotsWidget.this.getBounds();
                return new Rectangle(bounds.x + 1, bounds.y + 1, bounds.width - 2, bounds.height - 2);
            }
            
            @Override
            public int getMaxScrollHeight() {
                return Mth.ceil(widgets.size() / 8f) * 18;
            }
        };
        
        public ScrollableSlotsWidget(Rectangle bounds, List<Slot> widgets) {
            this.bounds = Objects.requireNonNull(bounds);
            this.widgets = Lists.newArrayList(widgets);
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
        public Rectangle getBounds() {
            return bounds;
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
        public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
            scrolling.updatePosition(delta);
            Rectangle innerBounds = scrolling.getScissorBounds();
            try (CloseableScissors scissors = scissor(matrices, innerBounds)) {
                for (int y = 0; y < Mth.ceil(widgets.size() / 8f); y++) {
                    for (int x = 0; x < 8; x++) {
                        int index = y * 8 + x;
                        if (widgets.size() <= index)
                            break;
                        Slot widget = widgets.get(index);
                        widget.getBounds().setLocation(bounds.x + 1 + x * 18, bounds.y + 1 + y * 18 - scrolling.scrollAmountInt());
                        widget.render(matrices, mouseX, mouseY, delta);
                    }
                }
            }
            try (CloseableScissors scissors = scissor(matrices, scrolling.getBounds())) {
                scrolling.renderScrollBar(0xff000000, 1, REIRuntime.getInstance().isDarkThemeEnabled() ? 0.8f : 1f);
            }
        }
        
        @Override
        public List<? extends GuiEventListener> children() {
            return widgets;
        }
    }
}
