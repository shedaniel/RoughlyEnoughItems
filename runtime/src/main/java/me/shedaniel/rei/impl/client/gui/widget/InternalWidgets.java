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
import com.mojang.blaze3d.vertex.PoseStack;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.math.impl.PointHelper;
import me.shedaniel.rei.api.client.config.ConfigObject;
import me.shedaniel.rei.api.client.gui.DrawableConsumer;
import me.shedaniel.rei.api.client.gui.widgets.*;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.impl.ClientInternals;
import me.shedaniel.rei.impl.client.gui.toast.CopyRecipeIdentifierToast;
import me.shedaniel.rei.impl.client.gui.widget.basewidgets.*;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
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
            boolean didJustRender = false;
            
            @Override
            public void render(PoseStack poses, int mouseX, int mouseY, float delta) {
                didJustRender = false;
                
                AutoCraftingEvaluator.AutoCraftingResult result = AutoCraftingEvaluator.evaluateAutoCrafting(false, false, displaySupplier.get(), idsSupplier);
                
                autoCraftingButton.setEnabled(result.successful);
                autoCraftingButton.setTint(result.tint);
                
                if (result.hasApplicable && (containsMouse(mouseX, mouseY) || autoCraftingButton.isFocused()) && result.renderer != null) {
                    result.renderer.render(poses, mouseX, mouseY, delta, setupDisplay, displayBounds, displaySupplier.get());
                }
                
                renderIf(result.hasApplicable, poses, mouseX, mouseY, delta);
                
                if (didJustRender) {
                    if (!autoCraftingButton.isFocused() && containsMouse(mouseX, mouseY)) {
                        tryTooltip(result, new Point(mouseX, mouseY));
                    } else if (autoCraftingButton.isFocused()) {
                        Rectangle bounds = autoCraftingButton.getBounds();
                        tryTooltip(result, new Point(bounds.x + bounds.width / 2, bounds.y + bounds.height / 2));
                    }
                }
            }
            
            private void tryTooltip(AutoCraftingEvaluator.AutoCraftingResult result, Point point) {
                if (result.tooltipRenderer != null) {
                    result.tooltipRenderer.accept(point, Tooltip::queue);
                }
            }
            
            private void renderIf(boolean should, PoseStack poseStack, int mouseX, int mouseY, float delta) {
                if (should) {
                    didJustRender = true;
                    this.widget.render(poseStack, mouseX, mouseY, delta);
                } else if (Minecraft.getInstance().options.advancedItemTooltips) {
                    didJustRender = true;
                    this.widget.render(poseStack, mouseX, mouseY, delta);
                }
            }
            
            @Override
            public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
                if (didJustRender && displaySupplier.get().getDisplayLocation().isPresent() && ConfigObject.getInstance().getCopyRecipeIdentifierKeybind().matchesKey(keyCode, scanCode) && containsMouse(PointHelper.ofMouse())) {
                    minecraft.keyboardHandler.setClipboard(displaySupplier.get().getDisplayLocation().get().toString());
                    if (ConfigObject.getInstance().isToastDisplayedOnCopyIdentifier()) {
                        CopyRecipeIdentifierToast.addToast(I18n.get("msg.rei.copied_recipe_id"), I18n.get("msg.rei.recipe_id_details", displaySupplier.get().getDisplayLocation().get().toString()));
                    }
                    return true;
                }
                return super.keyPressed(keyCode, scanCode, modifiers);
            }
            
            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                if (didJustRender && displaySupplier.get().getDisplayLocation().isPresent() && ConfigObject.getInstance().getCopyRecipeIdentifierKeybind().matchesMouse(button) && containsMouse(PointHelper.ofMouse())) {
                    minecraft.keyboardHandler.setClipboard(displaySupplier.get().getDisplayLocation().get().toString());
                    if (ConfigObject.getInstance().isToastDisplayedOnCopyIdentifier()) {
                        CopyRecipeIdentifierToast.addToast(I18n.get("msg.rei.copied_recipe_id"), I18n.get("msg.rei.recipe_id_details", displaySupplier.get().getDisplayLocation().get().toString()));
                    }
                    return true;
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
    
    private static class MergedWidget extends Widget {
        private final List<Widget> widgets;
        
        public MergedWidget(Widget widget1, Widget widget2) {
            this.widgets = Lists.newArrayList(Objects.requireNonNull(widget1), Objects.requireNonNull(widget2));
        }
        
        @Override
        public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
            for (Widget widget : widgets) {
                widget.setZ(getZ());
                widget.render(matrices, mouseX, mouseY, delta);
            }
        }
        
        @Override
        public List<? extends GuiEventListener> children() {
            return widgets;
        }
        
        @Override
        public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
            for (Widget widget : this.widgets) {
                if (widget.mouseScrolled(mouseX, mouseY, amount))
                    return true;
            }
            return false;
        }
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
        public Widget createDrawableWidget(DrawableConsumer drawable) {
            return new DrawableWidget(drawable);
        }
        
        @Override
        public me.shedaniel.rei.api.client.gui.widgets.Slot createSlot(Point point) {
            return new EntryWidget(point);
        }
        
        @Override
        public me.shedaniel.rei.api.client.gui.widgets.Slot createSlot(Rectangle bounds) {
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
    }
}
