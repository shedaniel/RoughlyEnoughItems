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

package me.shedaniel.rei.gui.widget;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import me.shedaniel.math.api.Point;
import me.shedaniel.math.api.Rectangle;
import me.shedaniel.rei.api.REIHelper;
import me.shedaniel.rei.api.widgets.Button;
import me.shedaniel.rei.api.widgets.Panel;
import me.shedaniel.rei.api.widgets.Widgets;
import me.shedaniel.rei.gui.RecipeViewingScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.Window;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@ApiStatus.Internal
public class RecipeChoosePageWidget extends DraggableWidget {
    
    private int currentPage;
    private int maxPage;
    private Rectangle bounds, grabBounds, dragBounds;
    private List<Widget> widgets;
    private RecipeViewingScreen recipeViewingScreen;
    private TextFieldWidget textFieldWidget;
    private Panel base1, base2;
    private Button btnDone;
    
    public RecipeChoosePageWidget(RecipeViewingScreen recipeViewingScreen, int currentPage, int maxPage) {
        super(getPointFromConfig());
        this.recipeViewingScreen = recipeViewingScreen;
        this.currentPage = currentPage;
        this.maxPage = maxPage;
        initWidgets(getMidPoint());
    }
    
    private static Point getPointFromConfig() {
        Window window = MinecraftClient.getInstance().getWindow();
        return new Point(window.getScaledWidth() * .5, window.getScaledHeight() * .5);
    }
    
    @NotNull
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
            @Override
            public List<Widget> children() {
                return Collections.emptyList();
            }
            
            @Override
            public void render(int i, int i1, float v) {
                font.draw(I18n.translate("text.rei.choose_page"), bounds.x + 5, bounds.y + 5, REIHelper.getInstance().isDarkThemeEnabled() ? 0xFFBBBBBB : 0xFF404040);
                String endString = String.format(" /%d", maxPage);
                int width = font.getStringWidth(endString);
                font.draw(endString, bounds.x + bounds.width - 5 - width, bounds.y + 22, REIHelper.getInstance().isDarkThemeEnabled() ? 0xFFBBBBBB : 0xFF404040);
            }
        });
        String endString = String.format(" /%d", maxPage);
        int width = font.getStringWidth(endString);
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
        widgets.add(btnDone = Widgets.createButton(new Rectangle(bounds.x + bounds.width - 45, bounds.y + bounds.height + 3, 40, 20), new TranslatableText("gui.done"))
                .onClick(button -> {
                    recipeViewingScreen.page = MathHelper.clamp(getIntFromString(textFieldWidget.getText()).orElse(0) - 1, 0, recipeViewingScreen.getTotalPages(recipeViewingScreen.getSelectedCategory()) - 1);
                    recipeViewingScreen.choosePageActivated = false;
                    recipeViewingScreen.init();
                }));
        textFieldWidget.setFocused(true);
    }
    
    @Override
    public Point processMidPoint(Point midPoint, Point mouse, Point startPoint, Window window, int relateX, int relateY) {
        return new Point(MathHelper.clamp(mouse.x - relateX, getDragBounds().width / 2, window.getScaledWidth() - getDragBounds().width / 2), MathHelper.clamp(mouse.y - relateY, 20, window.getScaledHeight() - 50));
    }
    
    @Override
    public List<Widget> children() {
        return widgets;
    }
    
    @Override
    public void render(int i, int i1, float v) {
        RenderSystem.translatef(0, 0, 800);
        for (Widget widget : widgets) {
            widget.render(i, i1, v);
        }
        RenderSystem.translatef(0, 0, -800);
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
            recipeViewingScreen.page = MathHelper.clamp(getIntFromString(textFieldWidget.getText()).orElse(0) - 1, 0, recipeViewingScreen.getTotalPages(recipeViewingScreen.getSelectedCategory()) - 1);
            recipeViewingScreen.choosePageActivated = false;
            recipeViewingScreen.init();
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
