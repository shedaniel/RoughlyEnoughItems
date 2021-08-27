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
import it.unimi.dsi.fastutil.ints.IntList;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.math.impl.PointHelper;
import me.shedaniel.rei.api.client.REIRuntime;
import me.shedaniel.rei.api.client.config.ConfigObject;
import me.shedaniel.rei.api.client.gui.DrawableConsumer;
import me.shedaniel.rei.api.client.gui.widgets.*;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.client.registry.display.TransferDisplayCategory;
import me.shedaniel.rei.api.client.registry.transfer.TransferHandler;
import me.shedaniel.rei.api.client.registry.transfer.TransferHandlerRegistry;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.util.CollectionUtils;
import me.shedaniel.rei.impl.ClientInternals;
import me.shedaniel.rei.impl.client.ClientHelperImpl;
import me.shedaniel.rei.impl.client.gui.toast.CopyRecipeIdentifierToast;
import me.shedaniel.rei.impl.client.gui.widget.basewidgets.*;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

@ApiStatus.Internal
@Environment(EnvType.CLIENT)
public final class InternalWidgets {
    private InternalWidgets() {}
    
    public static Widget createAutoCraftingButtonWidget(Rectangle displayBounds, Rectangle rectangle, Component text, Supplier<Display> displaySupplier, List<Widget> setupDisplay, DisplayCategory<?> category) {
        AbstractContainerScreen<?> containerScreen = REIRuntime.getInstance().getPreviousContainerScreen();
        boolean[] visible = {false};
        List<Component>[] errorTooltip = new List[]{null};
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
                .onRender((matrices, button) -> {
                    button.setEnabled(false);
                    if (containerScreen == null) {
                        button.setTint(0);
                        return;
                    }
                    List<Component> error = null;
                    int color = 0;
                    visible[0] = false;
                    IntList redSlots = null;
                    TransferHandler.Context context = TransferHandler.Context.create(false, containerScreen, displaySupplier.get());
                    for (TransferHandler transferHandler : TransferHandlerRegistry.getInstance()) {
                        try {
                            TransferHandler.Result result = transferHandler.handle(context);
                            if (result.isApplicable()) {
                                visible[0] = true;
                            }
                            if (result.isSuccessful()) {
                                button.setEnabled(true);
                                error = null;
                                color = 0;
                                redSlots = null;
                            } else if (result.isApplicable()) {
                                if (error == null) {
                                    error = Lists.newArrayList();
                                }
                                error.add(result.getError());
                                color = result.getColor();
                                if (result.getIntegers() != null && !result.getIntegers().isEmpty())
                                    redSlots = result.getIntegers();
                            }
                            
                            if (result.isBlocking()) break;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    if (!visible[0]) {
                        button.setEnabled(false);
                        if (error == null) {
                            error = Lists.newArrayList();
                        } else {
                            error.clear();
                        }
                        error.add(new TranslatableComponent("error.rei.no.handlers.applicable"));
                    }
                    if ((button.containsMouse(PointHelper.ofMouse()) || button.isFocused()) && category instanceof TransferDisplayCategory && redSlots != null) {
                        ((TransferDisplayCategory<Display>) category).renderRedSlots(matrices, setupDisplay, displayBounds, displaySupplier.get(), redSlots);
                    }
                    errorTooltip[0] = error == null || error.isEmpty() ? null : Lists.newArrayList();
                    if (errorTooltip[0] != null) {
                        for (Component s : error) {
                            if (!CollectionUtils.anyMatch(errorTooltip[0], ss -> ss.getString().equalsIgnoreCase(s.getString()))) {
                                errorTooltip[0].add(s);
                            }
                        }
                    }
                    button.setTint(color);
                })
                .textColor((button, mouse) -> {
                    if (!visible[0]) {
                        return 10526880;
                    } else if (button.isEnabled() && (button.containsMouse(mouse) || button.isFocused())) {
                        return 16777120;
                    }
                    return 14737632;
                })
                .textureId((button, mouse) -> !visible[0] ? 0 : (button.containsMouse(mouse) || button.isFocused()) && button.isEnabled() ? 4 : 1)
                .tooltipSupplier(button -> {
                    List<Component> str = new ArrayList<>();
                    if (errorTooltip[0] == null) {
                        if (ClientHelperImpl.getInstance().isYog.get()) {
                            str.add(new TranslatableComponent("text.auto_craft.move_items.yog"));
                        } else {
                            str.add(new TranslatableComponent("text.auto_craft.move_items"));
                        }
                    } else {
                        if (errorTooltip[0].size() > 1) {
                            str.add(new TranslatableComponent("error.rei.multi.errors").withStyle(ChatFormatting.RED));
                            for (Component component : errorTooltip[0]) {
                                str.add(new TranslatableComponent("- ").withStyle(ChatFormatting.RED).append(component.copy().withStyle(ChatFormatting.RED)));
                            }
                        } else if (errorTooltip[0].size() == 1) {
                            str.add(errorTooltip[0].get(0).copy().withStyle(ChatFormatting.RED));
                        }
                    }
                    if (Minecraft.getInstance().options.advancedItemTooltips && displaySupplier.get().getDisplayLocation().isPresent()) {
                        str.add(new TranslatableComponent("text.rei.recipe_id", "", new TextComponent(displaySupplier.get().getDisplayLocation().get().toString()).withStyle(ChatFormatting.GRAY)).withStyle(ChatFormatting.GRAY));
                    }
                    return str.toArray(new Component[0]);
                });
        return new DelegateWidget(autoCraftingButton) {
            @Override
            public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
                if (displaySupplier.get().getDisplayLocation().isPresent() && ConfigObject.getInstance().getCopyRecipeIdentifierKeybind().matchesKey(keyCode, scanCode) && containsMouse(PointHelper.ofMouse())) {
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
                if (displaySupplier.get().getDisplayLocation().isPresent() && ConfigObject.getInstance().getCopyRecipeIdentifierKeybind().matchesMouse(button) && containsMouse(PointHelper.ofMouse())) {
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
