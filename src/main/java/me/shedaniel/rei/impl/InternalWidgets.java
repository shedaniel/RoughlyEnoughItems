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

package me.shedaniel.rei.impl;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.ints.IntList;
import me.shedaniel.math.Rectangle;
import me.shedaniel.math.impl.PointHelper;
import me.shedaniel.rei.api.*;
import me.shedaniel.rei.api.widgets.Button;
import me.shedaniel.rei.api.widgets.Widgets;
import me.shedaniel.rei.gui.toast.CopyRecipeIdentifierToast;
import me.shedaniel.rei.gui.widget.LateRenderable;
import me.shedaniel.rei.gui.widget.Widget;
import me.shedaniel.rei.gui.widget.WidgetWithBounds;
import me.shedaniel.rei.utils.CollectionUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.ingame.ContainerScreen;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

@ApiStatus.Internal
@Environment(EnvType.CLIENT)
public final class InternalWidgets {
    private InternalWidgets() {}
    
    public static Widget createAutoCraftingButtonWidget(Rectangle displayBounds, Rectangle rectangle, Text text, Supplier<RecipeDisplay> displaySupplier, List<Widget> setupDisplay, RecipeCategory<?> category) {
        ContainerScreen<?> containerScreen = REIHelper.getInstance().getPreviousContainerScreen();
        boolean[] visible = {false};
        List<String>[] errorTooltip = new List[]{null};
        Button autoCraftingButton = Widgets.createButton(rectangle, text)
                .focusable(false)
                .onClick(button -> {
                    AutoTransferHandler.Context context = AutoTransferHandler.Context.create(true, containerScreen, displaySupplier.get());
                    for (AutoTransferHandler autoTransferHandler : RecipeHelper.getInstance().getSortedAutoCraftingHandler())
                        try {
                            AutoTransferHandler.Result result = autoTransferHandler.handle(context);
                            if (result.isSuccessful()) {
                                if (result.isReturningToScreen()) {
                                    break; // Same as failing, but doesn't ask other handlers
                                }
                                return;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    MinecraftClient.getInstance().openScreen(containerScreen);
                    ScreenHelper.getLastOverlay().init();
                })
                .onRender((matrices, button) -> {
                    button.setEnabled(false);
                    List<String> error = null;
                    int color = 0;
                    visible[0] = false;
                    IntList redSlots = null;
                    AutoTransferHandler.Context context = AutoTransferHandler.Context.create(false, containerScreen, displaySupplier.get());
                    for (AutoTransferHandler autoTransferHandler : RecipeHelper.getInstance().getSortedAutoCraftingHandler()) {
                        try {
                            AutoTransferHandler.Result result = autoTransferHandler.handle(context);
                            if (result.isApplicable())
                                visible[0] = true;
                            if (result.isSuccessful()) {
                                button.setEnabled(true);
                                error = null;
                                color = 0;
                                redSlots = null;
                                break;
                            } else if (result.isApplicable()) {
                                if (error == null) {
                                    error = Lists.newArrayList();
                                }
                                error.add(result.getErrorKey());
                                color = result.getColor();
                                if (result.getIntegers() != null && !result.getIntegers().isEmpty())
                                    redSlots = result.getIntegers();
                            }
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
                        error.add("error.rei.no.handlers.applicable");
                    }
                    if ((button.containsMouse(PointHelper.ofMouse()) || button.isFocused()) && category instanceof TransferRecipeCategory && redSlots != null) {
                        ((TransferRecipeCategory<RecipeDisplay>) category).renderRedSlots(matrices, setupDisplay, displayBounds, displaySupplier.get(), redSlots);
                    }
                    errorTooltip[0] = error == null || error.isEmpty() ? null : Lists.newArrayList();
                    if (errorTooltip[0] != null) {
                        for (String s : error) {
                            if (errorTooltip[0].stream().noneMatch(ss -> ss.equalsIgnoreCase(s)))
                                errorTooltip[0].add(s);
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
                    String str = "";
                    if (errorTooltip[0] == null) {
                        if (((ClientHelperImpl) ClientHelper.getInstance()).isYog.get())
                            str += I18n.translate("text.auto_craft.move_items.yog");
                        else
                            str += I18n.translate("text.auto_craft.move_items");
                    } else {
                        if (errorTooltip[0].size() > 1)
                            str += Formatting.RED.toString() + I18n.translate("error.rei.multi.errors") + "\n";
                        str += CollectionUtils.mapAndJoinToString(errorTooltip[0], s -> Formatting.RED.toString() + (errorTooltip[0].size() > 1 ? "- " : "") + I18n.translate(s), "\n");
                    }
                    if (MinecraftClient.getInstance().options.advancedItemTooltips) {
                        str += displaySupplier.get().getRecipeLocation().isPresent() ? I18n.translate("text.rei.recipe_id", Formatting.GRAY.toString(), displaySupplier.get().getRecipeLocation().get().toString()) : "";
                    }
                    return str;
                });
        return new WidgetWithBounds() {
            @Override
            public @NotNull Rectangle getBounds() {
                return autoCraftingButton.getBounds();
            }
            
            @Override
            public List<? extends Element> children() {
                return Collections.singletonList(autoCraftingButton);
            }
            
            @Override
            public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
                autoCraftingButton.render(matrices, mouseX, mouseY, delta);
            }
            
            @Override
            public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
                if (displaySupplier.get().getRecipeLocation().isPresent() && ConfigObject.getInstance().getCopyRecipeIdentifierKeybind().matchesKey(keyCode, scanCode) && containsMouse(PointHelper.ofMouse())) {
                    minecraft.keyboard.setClipboard(displaySupplier.get().getRecipeLocation().get().toString());
                    if (ConfigObject.getInstance().isToastDisplayedOnCopyIdentifier()) {
                        CopyRecipeIdentifierToast.addToast(I18n.translate("msg.rei.copied_recipe_id"), I18n.translate("msg.rei.recipe_id_details", displaySupplier.get().getRecipeLocation().get().toString()));
                    }
                    return true;
                }
                return super.keyPressed(keyCode, scanCode, modifiers);
            }
            
            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                if (displaySupplier.get().getRecipeLocation().isPresent() && ConfigObject.getInstance().getCopyRecipeIdentifierKeybind().matchesMouse(button) && containsMouse(PointHelper.ofMouse())) {
                    minecraft.keyboard.setClipboard(displaySupplier.get().getRecipeLocation().get().toString());
                    if (ConfigObject.getInstance().isToastDisplayedOnCopyIdentifier()) {
                        CopyRecipeIdentifierToast.addToast(I18n.translate("msg.rei.copied_recipe_id"), I18n.translate("msg.rei.recipe_id_details", displaySupplier.get().getRecipeLocation().get().toString()));
                    }
                    return true;
                }
                return super.mouseClicked(mouseX, mouseY, button);
            }
        };
    }
    
    public static WidgetWithBounds wrapLateRenderable(WidgetWithBounds widget) {
        return new LateRenderableWidgetWithBounds(widget);
    }
    
    public static WidgetWithBounds wrapTranslate(WidgetWithBounds widget, float x, float y, float z) {
        return new WidgetWithBoundsWithTranslate(widget, x, y, z);
    }
    
    public static Widget wrapLateRenderable(Widget widget) {
        return new LateRenderableWidget(widget);
    }
    
    public static Widget mergeWidgets(Widget widget1, Widget widget2) {
        return new MergedWidget(widget2, widget1);
    }
    
    private static class MergedWidget extends Widget {
        private final List<Widget> widgets;
        
        public MergedWidget(Widget widget1, Widget widget2) {
            this.widgets = Lists.newArrayList(Objects.requireNonNull(widget1), Objects.requireNonNull(widget2));
        }
        
        @Override
        public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
            for (Widget widget : widgets) {
                widget.setZ(getZ());
                widget.render(matrices, mouseX, mouseY, delta);
            }
        }
        
        @Override
        public List<? extends Element> children() {
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
    
    private static class LateRenderableWidget extends Widget implements LateRenderable {
        private final Widget widget;
        
        private LateRenderableWidget(Widget widget) {
            this.widget = widget;
        }
        
        @Override
        public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
            this.widget.setZ(getZ());
            this.widget.render(matrices, mouseX, mouseY, delta);
        }
        
        @Override
        public List<? extends Element> children() {
            return Collections.singletonList(this.widget);
        }
        
        @Override
        public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
            return this.widget.mouseScrolled(mouseX, mouseY, amount);
        }
    }
    
    private static class LateRenderableWidgetWithBounds extends WidgetWithBounds implements LateRenderable {
        private final WidgetWithBounds widget;
        
        private LateRenderableWidgetWithBounds(WidgetWithBounds widget) {
            this.widget = widget;
        }
        
        @Override
        public @NotNull Rectangle getBounds() {
            return this.widget.getBounds();
        }
        
        @Override
        public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
            this.widget.setZ(getZ());
            this.widget.render(matrices, mouseX, mouseY, delta);
        }
        
        @Override
        public boolean containsMouse(double mouseX, double mouseY) {
            return this.widget.containsMouse(mouseX, mouseY);
        }
        
        @Override
        public List<? extends Element> children() {
            return Collections.singletonList(this.widget);
        }
        
        @Override
        public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
            return this.widget.mouseScrolled(mouseX, mouseY, amount);
        }
    }
    
    private static class WidgetWithBoundsWithTranslate extends WidgetWithBounds implements LateRenderable {
        private final WidgetWithBounds widget;
        private final float x, y, z;
        
        public WidgetWithBoundsWithTranslate(WidgetWithBounds widget, float x, float y, float z) {
            this.widget = widget;
            this.x = x;
            this.y = y;
            this.z = z;
        }
        
        @Override
        public @NotNull Rectangle getBounds() {
            return this.widget.getBounds();
        }
        
        @Override
        public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
            matrices.push();
            matrices.translate(x, y, z);
            this.widget.setZ(getZ());
            this.widget.render(matrices, mouseX, mouseY, delta);
            matrices.pop();
        }
        
        @Override
        public boolean containsMouse(double mouseX, double mouseY) {
            return this.widget.containsMouse(mouseX, mouseY);
        }
        
        @Override
        public List<? extends Element> children() {
            return Collections.singletonList(this.widget);
        }
        
        @Override
        public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
            return this.widget.mouseScrolled(mouseX, mouseY, amount);
        }
    }
}
