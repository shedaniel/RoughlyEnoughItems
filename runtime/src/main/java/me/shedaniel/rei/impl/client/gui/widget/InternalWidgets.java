/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020, 2021 shedaniel
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
import me.shedaniel.rei.api.client.REIRuntime;
import me.shedaniel.rei.api.client.config.ConfigObject;
import me.shedaniel.rei.api.client.gui.DrawableConsumer;
import me.shedaniel.rei.api.client.gui.widgets.*;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.client.registry.transfer.TransferHandler;
import me.shedaniel.rei.api.client.registry.transfer.TransferHandlerErrorRenderer;
import me.shedaniel.rei.api.client.registry.transfer.TransferHandlerRegistry;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.util.CollectionUtils;
import me.shedaniel.rei.impl.ClientInternals;
import me.shedaniel.rei.impl.client.gui.toast.CopyRecipeIdentifierToast;
import me.shedaniel.rei.impl.client.gui.widget.basewidgets.*;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

@ApiStatus.Internal
@Environment(EnvType.CLIENT)
public final class InternalWidgets {
    private InternalWidgets() {}
    
    public static Widget createAutoCraftingButtonWidget(Rectangle displayBounds, Rectangle rectangle, Component text, Supplier<Display> displaySupplier, Supplier<Collection<ResourceLocation>> idsSupplier, List<Widget> setupDisplay, DisplayCategory<?> category) {
        AbstractContainerScreen<?> containerScreen = REIRuntime.getInstance().getPreviousContainerScreen();
        Mutable<List<Component>> errorTooltip = new MutableObject<>(new ArrayList<>());
        Button autoCraftingButton = Widgets.createButton(rectangle, text)
                .focusable(false)
                .onClick(button -> {
                    TransferHandler.Context context = TransferHandler.Context.create(true, containerScreen, displaySupplier.get());
                    for (TransferHandler autoTransferHandler : TransferHandlerRegistry.getInstance())
                        try {
                            TransferHandler.Result result = autoTransferHandler.handle(context);
                            if (result.isBlocking()) {
                                if (result.isReturningToScreen()) {
                                    break;
                                }
                                return;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    Minecraft.getInstance().setScreen(containerScreen);
                    REIRuntime.getInstance().getOverlay().get().queueReloadOverlay();
                })
                .tooltipSupplier(button -> {
                    List<Component> str = new ArrayList<>(errorTooltip.getValue());
                    
                    if (Minecraft.getInstance().options.advancedItemTooltips) {
                        Collection<ResourceLocation> locations = idsSupplier.get();
                        if (!locations.isEmpty()) {
                            str.add(new TextComponent(" "));
                            for (ResourceLocation location : locations) {
                                String t = I18n.get("text.rei.recipe_id", "", location.toString());
                                if (t.startsWith("\n")) {
                                    t = t.substring("\n".length());
                                }
                                str.add(new TextComponent(t).withStyle(ChatFormatting.GRAY));
                            }
                        }
                    }
                    return str.toArray(new Component[0]);
                });
        return new DelegateWidget(autoCraftingButton) {
            boolean didJustRender = false;
            @Override
            public void render(PoseStack poses, int mouseX, int mouseY, float delta) {
                didJustRender = false;
                autoCraftingButton.setEnabled(false);
                autoCraftingButton.setTint(0);
                
                if (containerScreen == null) {
                    errorTooltip.setValue(Lists.newArrayList(new TranslatableComponent("error.rei.not.supported.move.items").withStyle(ChatFormatting.RED)));
                    renderIf(false, poses, mouseX, mouseY, delta);
                    return;
                }
                
                List<TransferHandler.Result> errors = new ArrayList<>();
                boolean hasApplicable = false;
                TransferHandlerErrorRenderer errorRenderer = null;
                TransferHandler.Context context = TransferHandler.Context.create(false, containerScreen, displaySupplier.get());
                for (TransferHandler transferHandler : TransferHandlerRegistry.getInstance()) {
                    try {
                        TransferHandler.Result result = transferHandler.handle(context);
                        if (result.isApplicable()) {
                            hasApplicable = true;
                            autoCraftingButton.setTint(result.getColor());
                            
                            if (result.isSuccessful()) {
                                errors.clear();
                                autoCraftingButton.setEnabled(true);
                                errorRenderer = null;
                                break;
                            }
                            
                            errors.add(result);
                            TransferHandlerErrorRenderer transferHandlerErrorRenderer = result.getErrorRenderer(transferHandler, context);
                            if (transferHandlerErrorRenderer != null) {
                                errorRenderer = transferHandlerErrorRenderer;
                            }
                            
                            if (result.isBlocking()) {
                                break;
                            }
                        }
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }
                
                if (!hasApplicable) {
                    errorTooltip.setValue(Lists.newArrayList(new TranslatableComponent("error.rei.not.supported.move.items").withStyle(ChatFormatting.RED)));
                    renderIf(false, poses, mouseX, mouseY, delta);
                    return;
                }
                
                if ((containsMouse(mouseX, mouseY) || autoCraftingButton.isFocused()) && errorRenderer != null) {
                    errorRenderer.render(poses, mouseX, mouseY, delta, setupDisplay, displayBounds, displaySupplier.get());
                }
                if (errors.isEmpty()) {
                    errorTooltip.setValue(Lists.newArrayList(new TranslatableComponent("text.auto_craft.move_items")));
                } else {
                    errorTooltip.setValue(Lists.newArrayList());
                    List<Component> tooltipsFilled = new ArrayList<>();
                    for (TransferHandler.Result error : errors) {
                        error.fillTooltip(tooltipsFilled);
                    }
                    
                    if (errors.size() == 1) {
                        for (Component tooltipFilled : tooltipsFilled) {
                            MutableComponent colored = tooltipFilled.copy().withStyle(ChatFormatting.RED);
                            if (!CollectionUtils.anyMatch(errorTooltip.getValue(), ss -> ss.getString().equalsIgnoreCase(tooltipFilled.getString()))) {
                                errorTooltip.getValue().add(colored);
                            }
                        }
                    } else {
                        errorTooltip.getValue().add(new TranslatableComponent("error.rei.multi.errors").withStyle(ChatFormatting.RED));
                        for (Component tooltipFilled : tooltipsFilled) {
                            MutableComponent colored = new TextComponent("- ").withStyle(ChatFormatting.RED)
                                    .append(tooltipFilled.copy().withStyle(ChatFormatting.RED));
                            if (!CollectionUtils.anyMatch(errorTooltip.getValue(), ss -> ss.getString().equalsIgnoreCase(colored.getString()))) {
                                errorTooltip.getValue().add(colored);
                            }
                        }
                    }
                }
                renderIf(true, poses, mouseX, mouseY, delta);
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
