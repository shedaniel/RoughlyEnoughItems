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

import com.mojang.blaze3d.vertex.PoseStack;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.math.impl.PointHelper;
import me.shedaniel.rei.api.client.config.ConfigObject;
import me.shedaniel.rei.api.client.gui.widgets.*;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.impl.client.ClientInternals;
import me.shedaniel.rei.impl.client.gui.ScreenOverlayImpl;
import me.shedaniel.rei.impl.client.gui.toast.CopyRecipeIdentifierToast;
import me.shedaniel.rei.impl.client.gui.widget.favorites.FavoritesListWidget;
import me.shedaniel.rei.impl.client.provider.AutoCraftingEvaluator;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

public class AutoCraftingButtonWidget {
    public static Widget create(Rectangle displayBounds, Rectangle rectangle, Component text,
            Supplier<Display> displaySupplier, Supplier<Collection<ResourceLocation>> idsSupplier, List<Widget> setupDisplay, DisplayCategory<?> category) {
        Button autoCraftingButton = Widgets.createButton(rectangle, text)
                .focusable(false)
                .onClick(button -> {
                    ClientInternals.getAutoCraftingEvaluator(displaySupplier.get())
                            .actuallyCraft()
                            .stacked(Screen.hasShiftDown())
                            .get();
                });
        return new DelegateWidget(autoCraftingButton) {
            @Override
            public void render(PoseStack poses, int mouseX, int mouseY, float delta) {
                AutoCraftingEvaluator.Result result = ClientInternals.getAutoCraftingEvaluator(displaySupplier.get())
                        .buildRenderer()
                        .buildTooltipRenderer(autoCraftingButton.isFocused() || containsMouse(mouseX, mouseY))
                        .ids(idsSupplier == null ? null : idsSupplier.get())
                        .get();
                
                autoCraftingButton.setEnabled(result.isSuccessful());
                autoCraftingButton.setTint(result.getTint());
                
                if (result.isApplicable()) {
                    autoCraftingButton.setText(text);
                } else {
                    autoCraftingButton.setText(new TextComponent("!"));
                }
                
                if (result.isApplicable() && (containsMouse(mouseX, mouseY) || autoCraftingButton.isFocused()) && result.getRenderer() != null) {
                    result.getRenderer().render(poses, mouseX, mouseY, delta, setupDisplay, displayBounds, displaySupplier.get());
                }
                
                this.widget.render(poses, mouseX, mouseY, delta);
                
                if (!autoCraftingButton.isFocused() && containsMouse(mouseX, mouseY)) {
                    tryTooltip(result, new Point(mouseX, mouseY));
                } else if (autoCraftingButton.isFocused()) {
                    Rectangle bounds = autoCraftingButton.getBounds();
                    tryTooltip(result, new Point(bounds.x + bounds.width / 2, bounds.y + bounds.height / 2));
                }
            }
            
            private void tryTooltip(AutoCraftingEvaluator.Result result, Point point) {
                if (result.getTooltipRenderer() != null) {
                    result.getTooltipRenderer().accept(point, Tooltip::queue);
                }
            }
            
            @Override
            public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
                if (displaySupplier.get().getDisplayLocation().isPresent() && ConfigObject.getInstance().getCopyRecipeIdentifierKeybind().matchesKey(keyCode, scanCode) && containsMouse(PointHelper.ofMouse())) {
                    minecraft.keyboardHandler.setClipboard(displaySupplier.get().getDisplayLocation().get().toString());
                    if (ConfigObject.getInstance().isToastDisplayedOnCopyIdentifier()) {
                        CopyRecipeIdentifierToast.addToast(I18n.get("msg.rei.copied_recipe_id"), I18n.get("msg.rei.recipe_id_details", displaySupplier.get().getDisplayLocation().get().toString()));
                    }
                    return true;
                } else if (ConfigObject.getInstance().isFavoritesEnabled() && containsMouse(PointHelper.ofMouse())) {
                    if (ConfigObject.getInstance().getFavoriteKeyCode().matchesKey(keyCode, scanCode)) {
                        FavoritesListWidget favoritesListWidget = ScreenOverlayImpl.getFavoritesListWidget();
                        
                        if (favoritesListWidget != null) {
                            favoritesListWidget.displayHistory.addDisplay(displayBounds.clone(), displaySupplier.get());
                            return true;
                        }
                    }
                }
                
                return super.keyPressed(keyCode, scanCode, modifiers);
            }
            
            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                if (displaySupplier.get().getDisplayLocation().isPresent() && ConfigObject.getInstance().getCopyRecipeIdentifierKeybind().matchesMouse(button) && containsMouse(PointHelper.ofMouse())) {
                    minecraft.keyboardHandler.setClipboard(displaySupplier.get().getDisplayLocation().get().toString());
                    if (ConfigObject.getInstance().isToastDisplayedOnCopyIdentifier()) {
                        CopyRecipeIdentifierToast.addToast(I18n.get("msg.rei.copied_recipe_id"), I18n.get("msg.rei.recipe_id_details", displaySupplier.get().getDisplayLocation().get().toString()));
                    }
                    return true;
                } else if (ConfigObject.getInstance().isFavoritesEnabled() && containsMouse(PointHelper.ofMouse())) {
                    if (ConfigObject.getInstance().getFavoriteKeyCode().matchesMouse(button)) {
                        FavoritesListWidget favoritesListWidget = ScreenOverlayImpl.getFavoritesListWidget();
                        
                        if (favoritesListWidget != null) {
                            favoritesListWidget.displayHistory.addDisplay(displayBounds.clone(), displaySupplier.get());
                            return true;
                        }
                    }
                }
                
                return super.mouseClicked(mouseX, mouseY, button);
            }
        };
    }
}
