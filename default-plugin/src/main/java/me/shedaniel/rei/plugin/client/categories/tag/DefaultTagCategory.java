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

package me.shedaniel.rei.plugin.client.categories.tag;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import me.shedaniel.clothconfig2.api.animator.ValueAnimator;
import me.shedaniel.math.FloatingRectangle;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.REIRuntime;
import me.shedaniel.rei.api.client.gui.AbstractRenderer;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.*;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.client.util.ClientEntryStacks;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.CollectionUtils;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.shedaniel.rei.plugin.common.BuiltinPlugin;
import me.shedaniel.rei.plugin.common.displays.tag.DefaultTagDisplay;
import me.shedaniel.rei.plugin.common.displays.tag.TagNode;
import me.shedaniel.rei.plugin.common.displays.tag.TagNodes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class DefaultTagCategory implements DisplayCategory<DefaultTagDisplay<?, ?>> {
    @Override
    public CategoryIdentifier<? extends DefaultTagDisplay<?, ?>> getCategoryIdentifier() {
        return BuiltinPlugin.TAG;
    }
    
    @Override
    public Component getTitle() {
        return Component.translatable("category.rei.tag");
    }
    
    @Override
    public Renderer getIcon() {
        return EntryStacks.of(Items.NAME_TAG);
    }
    
    @Override
    public List<Widget> setupDisplay(DefaultTagDisplay<?, ?> display, Rectangle bounds) {
        List<Widget> widgets = new ArrayList<>();
        
        Window window = Minecraft.getInstance().getWindow();
        Rectangle boundsBig = new Rectangle(window.getGuiScaledWidth() * 0.05, window.getGuiScaledHeight() * 0.1, window.getGuiScaledWidth() * 0.9, window.getGuiScaledHeight() * 0.8);
        Rectangle recipeBounds = bounds.clone();
        
        boolean[] expanded = {false};
        
        Rectangle innerBounds = new Rectangle(bounds.x + 6 + 14, bounds.y + 6, bounds.width - 12 - 14, bounds.height - 12);
        Rectangle overflowBounds = new Rectangle(innerBounds.x + 1, innerBounds.y + 1, innerBounds.width - 2, innerBounds.height - 2);
        
        Rectangle expandButtonBounds = new Rectangle(bounds.x + 5, bounds.y + 6, 13, 13);
        Rectangle expandOverlayBounds = new Rectangle(bounds.x + 5 + 2, bounds.y + 6 + 2, 13 - 4, 13 - 4);
        Rectangle copyButtonBounds = new Rectangle(bounds.x + 5, bounds.getMaxY() - 6 - 13, 13, 13);
        Rectangle copyOverlayBounds = new Rectangle(bounds.x + 5 + 2, bounds.getMaxY() - 6 - 13 + 2, 13 - 4, 13 - 4);
        
        ValueAnimator<FloatingRectangle> boundsAnimator = ValueAnimator.ofFloatingRectangle(bounds.getFloatingBounds())
                .withConvention(() -> {
                    if (expanded[0]) {
                        return boundsBig.getFloatingBounds();
                    } else {
                        return bounds.getFloatingBounds();
                    }
                }, 1400);
        
        widgets.add(Widgets.createDrawableWidget((helper, matrices, mouseX, mouseY, delta) -> {
            innerBounds.setBounds(recipeBounds.x + 6 + 14, recipeBounds.y + 6, recipeBounds.width - 12 - 14, recipeBounds.height - 12);
            overflowBounds.setBounds(innerBounds.x + 1, innerBounds.y + 1, innerBounds.width - 2, innerBounds.height - 2);
            expandButtonBounds.setBounds(recipeBounds.x + 5, recipeBounds.y + 6, 13, 13);
            copyButtonBounds.setBounds(recipeBounds.x + 5, recipeBounds.getMaxY() - 6 - 13, 13, 13);
            expandOverlayBounds.setBounds(recipeBounds.x + 5 + 2, recipeBounds.y + 6 + 2, 13 - 4, 13 - 4);
            copyOverlayBounds.setBounds(recipeBounds.x + 5 + 2, recipeBounds.getMaxY() - 6 - 13 + 2, 13 - 4, 13 - 4);
            recipeBounds.setBounds(boundsAnimator.value());
            boundsAnimator.update(delta);
            
            if (overflowBounds.contains(mouseX, mouseY)) {
                REIRuntime.getInstance().clearTooltips();
            }
        }));
        
        widgets.add(Widgets.createRecipeBase(recipeBounds));
        widgets.add(Widgets.createSlotBase(innerBounds));
        
        WidgetWithBounds[] delegate = new WidgetWithBounds[]{Widgets.noOp()};
        TagNode<?>[] tagNode = new TagNode[]{null};
        widgets.add(Widgets.withTranslate(Widgets.delegateWithBounds(() -> delegate[0]), 0, 0, 20));
        
        TagNodes.create(display.getKey(), dataResult -> {
            if (dataResult.error().isPresent()) {
                delegate[0] = Widgets.withBounds(Widgets.concat(
                        Widgets.createLabel(new Point(innerBounds.getCenterX(), innerBounds.getCenterY() - 8), Component.literal("Failed to resolve tags!")),
                        Widgets.createLabel(new Point(innerBounds.getCenterX(), innerBounds.getCenterY() + 1), Component.literal(dataResult.error().get().message()))
                ), overflowBounds);
            } else {
                tagNode[0] = dataResult.result().get();
                //noinspection rawtypes
                Function<? extends Holder<?>, ? extends EntryStack<?>> displayMapper = display.getMapper();
                Function<Holder<?>, EntryStack<?>> mapper = holder -> {
                    EntryStack<?> stack = ((Function<Holder<?>, EntryStack<?>>) displayMapper).apply(holder);
                    if (stack.isEmpty()) {
                        return ClientEntryStacks.of(new AbstractRenderer() {
                            @Override
                            public void render(PoseStack matrices, Rectangle bounds, int mouseX, int mouseY, float delta) {
                                Minecraft instance = Minecraft.getInstance();
                                Font font = instance.font;
                                String text = "?";
                                int width = font.width(text);
                                font.draw(matrices, text, bounds.getCenterX() - width / 2f + 0.2f, bounds.getCenterY() - font.lineHeight / 2f + 1f, REIRuntime.getInstance().isDarkThemeEnabled() ? -4473925 : -12566464);
                            }
                            
                            @Override
                            @Nullable
                            public Tooltip getTooltip(TooltipContext context) {
                                return Tooltip.create(context.getPoint(), Component.literal(holder.unwrapKey().map(key -> key.location().toString()).orElse("null")));
                            }
                        });
                    }
                    return stack;
                };
                delegate[0] = Widgets.overflowed(overflowBounds, Widgets.padded(16, new TagTreeWidget(tagNode[0], mapper, overflowBounds)));
            }
        });
        
        widgets.add(Widgets.createButton(expandButtonBounds, Component.literal(""))
                .onRender((poseStack, button) -> {
                    button.setEnabled(tagNode[0] != null);
                })
                .onClick(button -> {
                    expanded[0] = !expanded[0];
                })
                .tooltipSupplier(button -> new Component[]{Component.translatable(!expanded[0] ? "text.rei.expand.view" : "text.rei.expand.view.close")}));
        widgets.add(Widgets.createButton(copyButtonBounds, Component.literal(""))
                .onRender((poseStack, button) -> {
                    button.setEnabled(tagNode[0] != null);
                })
                .onClick(button -> {
                    TagNode<?> node = tagNode[0];
                    
                    if (node != null) {
                        Minecraft.getInstance().keyboardHandler.setClipboard(node.asTree());
                    }
                })
                .tooltipLine(Component.translatable("text.rei.tag.copy.clipboard")));
        widgets.add(Widgets.withTranslate(new DelegateWidget(Widgets.noOp()) {
            @Override
            protected Widget delegate() {
                ResourceLocation expandTexture = !expanded[0] ? new ResourceLocation("roughlyenoughitems", "textures/gui/expand.png")
                        : new ResourceLocation("roughlyenoughitems", "textures/gui/shrink.png");
                return Widgets.concat(
                        Widgets.createTexturedWidget(expandTexture,
                                new Rectangle(recipeBounds.x + 5 + 2, recipeBounds.y + 6 + 2, 13 - 4, 13 - 4), 0, 0, 9, 9),
                        Widgets.createTexturedWidget(new ResourceLocation("roughlyenoughitems", "textures/gui/clipboard.png"),
                                new Rectangle(recipeBounds.x + 5 + 2, recipeBounds.getMaxY() - 6 - 13 + 2, 13 - 4, 13 - 4), 0, 0, 9, 9)
                );
            }
        }, 0, 0, 10));
        
        Matrix4f translateMatrix = new Matrix4f().translate(0, 0, 200);
        Matrix4f identity = new Matrix4f();
        identity.identity();
        return CollectionUtils.map(widgets, widget -> Widgets.withTranslate(widget, () ->
                expanded[0] || !boundsAnimator.value().equals(boundsAnimator.target()) ? translateMatrix : identity));
    }
}
