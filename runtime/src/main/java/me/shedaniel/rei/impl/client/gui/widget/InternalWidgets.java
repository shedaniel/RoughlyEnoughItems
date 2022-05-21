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
import com.mojang.math.Matrix4f;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.math.impl.PointHelper;
import me.shedaniel.rei.api.client.config.ConfigObject;
import me.shedaniel.rei.api.client.gui.DrawableConsumer;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.*;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.impl.ClientInternals;
import me.shedaniel.rei.impl.client.gui.ScreenOverlayImpl;
import me.shedaniel.rei.impl.client.gui.toast.CopyRecipeIdentifierToast;
import me.shedaniel.rei.impl.client.gui.widget.basewidgets.*;
import me.shedaniel.rei.impl.client.gui.widget.favorites.FavoritesListWidget;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;

import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

@ApiStatus.Internal
@Environment(EnvType.CLIENT)
public final class InternalWidgets {
    private InternalWidgets() {}
    
    public static Widget createAutoCraftingButtonWidget(Rectangle displayBounds, Rectangle rectangle, Component text, Supplier<Display> displaySupplier, Supplier<Collection<ResourceLocation>> idsSupplier, List<Widget> setupDisplay, DisplayCategory<?> category) {
        Button autoCraftingButton = Widgets.createButton(rectangle, text)
                .focusable(false)
                .onClick(button -> {
                    AutoCraftingEvaluator.evaluateAutoCrafting(true, Screen.hasShiftDown(), displaySupplier.get(), idsSupplier);
                });
        return new DelegateWidget(autoCraftingButton) {
            @Override
            public void render(PoseStack poses, int mouseX, int mouseY, float delta) {
                AutoCraftingEvaluator.AutoCraftingResult result = AutoCraftingEvaluator.evaluateAutoCrafting(false, false, displaySupplier.get(), idsSupplier);
                
                autoCraftingButton.setEnabled(result.successful);
                autoCraftingButton.setTint(result.tint);
                
                if (result.hasApplicable) {
                    autoCraftingButton.setText(text);
                } else {
                    autoCraftingButton.setText(new TextComponent("?"));
                }
                
                if (result.hasApplicable && (containsMouse(mouseX, mouseY) || autoCraftingButton.isFocused()) && result.renderer != null) {
                    result.renderer.render(poses, mouseX, mouseY, delta, setupDisplay, displayBounds, displaySupplier.get());
                }
                
                this.widget.render(poses, mouseX, mouseY, delta);
                
                if (!autoCraftingButton.isFocused() && containsMouse(mouseX, mouseY)) {
                    tryTooltip(result, new Point(mouseX, mouseY));
                } else if (autoCraftingButton.isFocused()) {
                    Rectangle bounds = autoCraftingButton.getBounds();
                    tryTooltip(result, new Point(bounds.x + bounds.width / 2, bounds.y + bounds.height / 2));
                }
            }
            
            private void tryTooltip(AutoCraftingEvaluator.AutoCraftingResult result, Point point) {
                if (result.tooltipRenderer != null) {
                    result.tooltipRenderer.accept(point, Tooltip::queue);
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
    
    public static WidgetWithBounds wrapLateRenderable(Widget widget) {
        return new LateRenderableWidget(widget);
    }
    
    public static Widget concatWidgets(Widget widget1, Widget widget2) {
        return new MergedWidget(widget2, widget1);
    }
    
    public static Widget concatWidgets(List<Widget> widgets) {
        return new MergedWidget(widgets);
    }
    
    private static class LateRenderableWidget extends DelegateWidget implements LateRenderable {
        private LateRenderableWidget(Widget widget) {
            super(widget);
        }
    }
    
    public static void attach() {
        ClientInternals.attachInstance(new WidgetsProvider(), ClientInternals.WidgetsProvider.class);
    }
    
    private static class WidgetsProvider implements ClientInternals.WidgetsProvider {
        @Override
        public boolean isRenderingPanel(Panel panel) {
            return PanelWidget.isRendering(panel);
        }
        
        @Override
        public Widget wrapVanillaWidget(GuiEventListener element) {
            if (element instanceof Widget) return (Widget) element;
            return new VanillaWrappedWidget(element);
        }
        
        @Override
        public WidgetWithBounds wrapRenderer(Supplier<Rectangle> bounds, Renderer renderer) {
            return new RendererWrappedWidget(renderer, bounds);
        }
        
        @Override
        public WidgetWithBounds withTranslate(WidgetWithBounds widget, Supplier<Matrix4f> translate) {
            return new DelegateWidgetWithTranslate(widget, translate);
        }
        
        @Override
        public Widget createDrawableWidget(DrawableConsumer drawable) {
            return new DrawableWidget(drawable);
        }
        
        @Override
        public Slot createSlot(Point point) {
            return new EntryWidget(point);
        }
        
        @Override
        public Slot createSlot(Rectangle bounds) {
            return new EntryWidget(bounds);
        }
        
        @Override
        public Button createButton(Rectangle bounds, Component text) {
            return new ButtonWidget(bounds, text);
        }
        
        @Override
        public Panel createPanelWidget(Rectangle bounds) {
            return new PanelWidget(bounds);
        }
        
        @Override
        public Label createLabel(Point point, FormattedText text) {
            return new LabelWidget(point, text);
        }
        
        @Override
        public Arrow createArrow(Rectangle rectangle) {
            return new ArrowWidget(rectangle);
        }
        
        @Override
        public BurningFire createBurningFire(Rectangle rectangle) {
            return new BurningFireWidget(rectangle);
        }
        
        @Override
        public DrawableConsumer createTexturedConsumer(ResourceLocation texture, int x, int y, int width, int height, float u, float v, int uWidth, int vHeight, int textureWidth, int textureHeight) {
            return new TexturedDrawableConsumer(texture, x, y, width, height, u, v, uWidth, vHeight, textureWidth, textureHeight);
        }
        
        @Override
        public DrawableConsumer createFillRectangleConsumer(Rectangle rectangle, int color) {
            return new FillRectangleDrawableConsumer(rectangle, color);
        }
        
        @Override
        public Widget createShapelessIcon(Point point) {
            int magnification;
            double scale = Minecraft.getInstance().getWindow().getGuiScale();
            if (scale >= 1 && scale <= 4 && scale == Math.floor(scale)) {
                magnification = (int) scale;
            } else if (scale > 4 && scale == Math.floor(scale)) {
                magnification = 1;
                for (int i = 4; i >= 1; i--) {
                    if (scale % i == 0) {
                        magnification = i;
                        break;
                    }
                }
            } else {
                magnification = 4;
            }
            Rectangle bounds = new Rectangle(point.getX() - 9, point.getY() + 1, 8, 8);
            Widget widget = Widgets.createTexturedWidget(new ResourceLocation("roughlyenoughitems:textures/gui/shapeless_icon_" + magnification + "x.png"), bounds.getX(), bounds.getY(), 0, 0, bounds.getWidth(), bounds.getHeight(), 1, 1, 1, 1);
            return Widgets.withTooltip(Widgets.withBounds(widget, bounds),
                    Component.translatable("text.rei.shapeless"));
        }
        
        @Override
        public Widget concatWidgets(List<Widget> widgets) {
            return InternalWidgets.concatWidgets(widgets);
        }
    }
}
