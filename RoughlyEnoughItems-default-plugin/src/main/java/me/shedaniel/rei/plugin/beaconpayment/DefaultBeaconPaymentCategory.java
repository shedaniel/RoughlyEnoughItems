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

package me.shedaniel.rei.plugin.beaconpayment;

import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import me.shedaniel.clothconfig2.ClothConfigInitializer;
import me.shedaniel.clothconfig2.api.ScissorsHandler;
import me.shedaniel.clothconfig2.api.ScrollingContainer;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.api.REIHelper;
import me.shedaniel.rei.api.RecipeCategory;
import me.shedaniel.rei.api.widgets.Slot;
import me.shedaniel.rei.api.widgets.Widgets;
import me.shedaniel.rei.gui.entries.RecipeEntry;
import me.shedaniel.rei.gui.widget.Widget;
import me.shedaniel.rei.gui.widget.WidgetWithBounds;
import me.shedaniel.rei.plugin.DefaultPlugin;
import me.shedaniel.rei.utils.CollectionUtils;
import org.jetbrains.annotations.NotNull;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Objects;

public class DefaultBeaconPaymentCategory implements RecipeCategory<DefaultBeaconPaymentDisplay> {
    @Override
    public Identifier getIdentifier() {
        return DefaultPlugin.BEACON_PAYMENT;
    }
    
    @Override
    public String getCategoryName() {
        return I18n.translate("category.rei.beacon_payment");
    }
    
    @Override
    public EntryStack getLogo() {
        return EntryStack.create(Blocks.BEACON);
    }
    
    @Override
    public RecipeEntry getSimpleRenderer(DefaultBeaconPaymentDisplay recipe) {
        String name = getCategoryName();
        return new RecipeEntry() {
            @Override
            public int getHeight() {
                return 10 + MinecraftClient.getInstance().textRenderer.fontHeight;
            }
            
            @Override
            public void render(MatrixStack matrices, Rectangle rectangle, int mouseX, int mouseY, float delta) {
                MinecraftClient.getInstance().textRenderer.draw(matrices, name, rectangle.x + 5, rectangle.y + 6, -1);
            }
        };
    }
    
    @Override
    public List<Widget> setupDisplay(DefaultBeaconPaymentDisplay display, Rectangle bounds) {
        List<Widget> widgets = Lists.newArrayList();
        widgets.add(Widgets.createSlot(new Point(bounds.getCenterX() - 8, bounds.y + 3)).entry(getLogo()));
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
    public int getFixedRecipesPerPage() {
        return 1;
    }
    
    private static class ScrollableSlotsWidget extends WidgetWithBounds {
        private Rectangle bounds;
        private List<Slot> widgets;
        private final ScrollingContainer scrolling = new ScrollingContainer() {
            @Override
            public Rectangle getBounds() {
                Rectangle bounds = DefaultBeaconPaymentCategory.ScrollableSlotsWidget.this.getBounds();
                return new Rectangle(bounds.x + 1, bounds.y + 1, bounds.width - 2, bounds.height - 2);
            }
            
            @Override
            public int getMaxScrollHeight() {
                return MathHelper.ceil(widgets.size() / 8f) * 18;
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
        
        @NotNull
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
        public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
            scrolling.updatePosition(delta);
            Rectangle innerBounds = scrolling.getScissorBounds();
            ScissorsHandler.INSTANCE.scissor(innerBounds);
            for (int y = 0; y < MathHelper.ceil(widgets.size() / 8f); y++) {
                for (int x = 0; x < 8; x++) {
                    int index = y * 8 + x;
                    if (widgets.size() <= index)
                        break;
                    Slot widget = widgets.get(index);
                    widget.getBounds().setLocation(bounds.x + 1 + x * 18, (int) (bounds.y + 1 + y * 18 - scrolling.scrollAmount));
                    widget.render(matrices, mouseX, mouseY, delta);
                }
            }
            ScissorsHandler.INSTANCE.removeLastScissor();
            ScissorsHandler.INSTANCE.scissor(scrolling.getBounds());
            scrolling.renderScrollBar(0xff000000, 1, REIHelper.getInstance().isDarkThemeEnabled() ? 0.8f : 1f);
            ScissorsHandler.INSTANCE.removeLastScissor();
        }
        
        @Override
        public List<? extends Element> children() {
            return widgets;
        }
    }
}
