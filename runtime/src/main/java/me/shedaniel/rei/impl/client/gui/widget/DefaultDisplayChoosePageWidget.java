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

package me.shedaniel.rei.impl.client.gui.widget;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.REIRuntime;
import me.shedaniel.rei.api.client.gui.widgets.Button;
import me.shedaniel.rei.api.client.gui.widgets.Panel;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.impl.client.gui.ScreenOverlayImpl;
import me.shedaniel.rei.impl.client.gui.widget.basewidgets.TextFieldWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.ApiStatus;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.IntConsumer;

@ApiStatus.Internal
public class DefaultDisplayChoosePageWidget extends DraggableWidget {
    
    private int currentPage;
    private int maxPage;
    private Rectangle bounds, grabBounds, dragBounds;
    private List<Widget> widgets;
    private IntConsumer callback;
    private TextFieldWidget textFieldWidget;
    private Panel base1, base2;
    private Button btnDone;
    
    public DefaultDisplayChoosePageWidget(IntConsumer callback, int currentPage, int maxPage) {
        super(getPointFromConfig());
        this.callback = callback;
        this.currentPage = currentPage;
        this.maxPage = maxPage;
        initWidgets(getMidPoint());
    }
    
    private static Point getPointFromConfig() {
        Window window = Minecraft.getInstance().getWindow();
        return new Point(window.getGuiScaledWidth() * .5, window.getGuiScaledHeight() * .5);
    }
    
    @Override
    public Rectangle getBounds() {
        return bounds;
    }
    
    @Override
    public Rectangle getGrabBounds() {
        return grabBounds;
    }
    
    @Override
    public Rectangle getDragBounds() {
        return dragBounds;
    }
    
    @Override
    public boolean containsMouse(double mouseX, double mouseY) {
        return getBounds().contains(mouseX, mouseY) || new Rectangle(bounds.x + bounds.width - 50, bounds.y + bounds.height - 3, 50, 36).contains(mouseX, mouseY);
    }
    
    @Override
    public void updateWidgets(Point midPoint) {
        this.bounds = new Rectangle(midPoint.x - 50, midPoint.y - 20, 100, 40);
        this.grabBounds = new Rectangle(midPoint.x - 50, midPoint.y - 20, 100, 16);
        this.dragBounds = new Rectangle(midPoint.x - 50, midPoint.y - 20, 100, 70);
        base1.getBounds().setLocation(bounds.x + bounds.width - 50, bounds.y + bounds.height - 6);
        base2.getBounds().setBounds(bounds);
        textFieldWidget.getBounds().setLocation(bounds.x + 7, bounds.y + 16);
        btnDone.getBounds().setLocation(bounds.x + bounds.width - 45, bounds.y + bounds.height + 3);
    }
    
    @Override
    protected void initWidgets(Point midPoint) {
        this.bounds = new Rectangle(midPoint.x - 50, midPoint.y - 20, 100, 40);
        this.grabBounds = new Rectangle(midPoint.x - 50, midPoint.y - 20, 100, 16);
        this.dragBounds = new Rectangle(midPoint.x - 50, midPoint.y - 20, 100, 70);
        this.widgets = Lists.newArrayList();
        this.widgets.add(base1 = Widgets.createCategoryBase(new Rectangle(bounds.x + bounds.width - 50, bounds.y + bounds.height - 6, 50, 36)));
        this.widgets.add(base2 = Widgets.createCategoryBase(bounds));
        this.widgets.add(new Widget() {
            
            private Component text = Component.translatable("text.rei.choose_page");
            
            @Override
            public List<Widget> children() {
                return Collections.emptyList();
            }
            
            @Override
            public void render(PoseStack matrices, int i, int i1, float v) {
                font.draw(matrices, text.getVisualOrderText(), bounds.x + 5, bounds.y + 5, REIRuntime.getInstance().isDarkThemeEnabled() ? 0xFFBBBBBB : 0xFF404040);
                String endString = String.format(" /%d", maxPage);
                int width = font.width(endString);
                font.draw(matrices, endString, bounds.x + bounds.width - 5 - width, bounds.y + 22, REIRuntime.getInstance().isDarkThemeEnabled() ? 0xFFBBBBBB : 0xFF404040);
            }
        });
        String endString = String.format(" /%d", maxPage);
        int width = font.width(endString);
        this.widgets.add(textFieldWidget = new TextFieldWidget(bounds.x + 7, bounds.y + 16, bounds.width - width - 12, 18));
        textFieldWidget.setMaxLength(10000);
        textFieldWidget.stripInvalid = s -> {
            StringBuilder stringBuilder_1 = new StringBuilder();
            char[] var2 = s.toCharArray();
            int var3 = var2.length;
            
            for (char char_1 : var2) {
                if (Character.isDigit(char_1))
                    stringBuilder_1.append(char_1);
            }
            
            return stringBuilder_1.toString();
        };
        textFieldWidget.setText(String.valueOf(currentPage + 1));
        widgets.add(btnDone = Widgets.createButton(new Rectangle(bounds.x + bounds.width - 45, bounds.y + bounds.height + 3, 40, 20), Component.translatable("gui.done"))
                .onClick(button -> {
                    callback.accept(Mth.clamp(getIntFromString(textFieldWidget.getText()).orElse(0) - 1, 0, maxPage - 1));
                    ScreenOverlayImpl.getInstance().choosePageWidget = null;
                }));
        textFieldWidget.setFocused(true);
    }
    
    @Override
    public Point processMidPoint(Point midPoint, Point mouse, Point startPoint, Window window, int relateX, int relateY) {
        return new Point(Mth.clamp(mouse.x - relateX, getDragBounds().width / 2, window.getGuiScaledWidth() - getDragBounds().width / 2), Mth.clamp(mouse.y - relateY, 20, window.getGuiScaledHeight() - 50));
    }
    
    @Override
    public List<Widget> children() {
        return widgets;
    }
    
    @Override
    public void render(PoseStack matrices, int i, int i1, float v) {
        matrices.pushPose();
        matrices.translate(0, 0, 800);
        for (Widget widget : widgets) {
            widget.render(matrices, i, i1, v);
        }
        matrices.popPose();
    }
    
    @Override
    public boolean charTyped(char char_1, int int_1) {
        for (Widget widget : widgets)
            if (widget.charTyped(char_1, int_1))
                return true;
        return false;
    }
    
    @Override
    public boolean keyPressed(int int_1, int int_2, int int_3) {
        if (int_1 == 335 || int_1 == 257) {
            callback.accept(Mth.clamp(getIntFromString(textFieldWidget.getText()).orElse(0) - 1, 0, maxPage - 1));
            ScreenOverlayImpl.getInstance().choosePageWidget = null;
            return true;
        }
        for (Widget widget : widgets)
            if (widget.keyPressed(int_1, int_2, int_3))
                return true;
        return false;
    }
    
    public Optional<Integer> getIntFromString(String s) {
        try {
            return Optional.of(Integer.valueOf(s));
        } catch (Exception ignored) {
        }
        return Optional.empty();
    }
    
    @Override
    public void onMouseReleaseMidPoint(Point midPoint) {
    }
    
}
