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

import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.DelegateWidget;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.WidgetWithBounds;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.shedaniel.rei.plugin.common.BuiltinPlugin;
import me.shedaniel.rei.plugin.common.displays.tag.DefaultTagDisplay;
import me.shedaniel.rei.plugin.common.displays.tag.TagNode;
import me.shedaniel.rei.plugin.common.displays.tag.TagNodes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

public class DefaultTagCategory implements DisplayCategory<DefaultTagDisplay<?, ?>> {
    @Override
    public CategoryIdentifier<? extends DefaultTagDisplay<?, ?>> getCategoryIdentifier() {
        return BuiltinPlugin.TAG;
    }
    
    @Override
    public Component getTitle() {
        return new TranslatableComponent("category.rei.tag");
    }
    
    @Override
    public Renderer getIcon() {
        return EntryStacks.of(Items.NAME_TAG);
    }
    
    @Override
    public List<Widget> setupDisplay(DefaultTagDisplay<?, ?> display, Rectangle bounds) {
        List<Widget> widgets = new ArrayList<>();
        
        widgets.add(Widgets.createRecipeBase(bounds));
        Rectangle innerBounds = new Rectangle(bounds.x + 6 + 14, bounds.y + 6, bounds.width - 12 - 14, bounds.height - 12);
        widgets.add(Widgets.createSlotBase(innerBounds));
        
        Widget[] delegate = new Widget[]{Widgets.noOp()};
        TagNode<?>[] tagNode = new TagNode[]{null};
        Rectangle overflowBounds = new Rectangle(innerBounds.x + 1, innerBounds.y + 1, innerBounds.width - 2, innerBounds.height - 2);
        WidgetWithBounds inner = Widgets.withBounds(new DelegateWidget(Widgets.noOp()) {
            @Override
            protected Widget delegate() {
                return delegate[0];
            }
        }, overflowBounds);
        widgets.add(Widgets.withTranslate(Widgets.overflowed(overflowBounds, Widgets.withBounds(Widgets.createDrawableWidget((helper, matrices, mouseX, mouseY, delta) -> {
            new GuiComponent() {
                {
                    fillGradient(matrices, 0, 0, 1000, 1000, 0xff3489eb, 0xffc41868);
                    for (int x = 0; x < 20; x++) {
                        for (int y = 0; y < 20; y++) {
                            Widgets.createSlot(new Point(500 - 9 * 20 + x * 18, 500 - 9 * 20 + y * 18))
                                    .entry(EntryStacks.of(Registry.ITEM.byId(x + y * 10 + 1)))
                                    .disableBackground()
                                    .render(matrices, mouseX, mouseY, delta);
                        }
                    }
                }
            };
        }), new Rectangle(0, 0, 1000, 1000))), 0, 0, 20));
        
        TagNodes.create(display.getKey(), dataResult -> {
            if (dataResult.error().isPresent()) {
                delegate[0] = Widgets.concat(
                        Widgets.createLabel(new Point(innerBounds.getCenterX(), innerBounds.getCenterY() - 8), new TextComponent("Failed to resolve tags!")),
                        Widgets.createLabel(new Point(innerBounds.getCenterX(), innerBounds.getCenterY() - 8), new TextComponent(dataResult.error().get().message()))
                );
            } else {
                tagNode[0] = dataResult.result().get();
            }
        });
        
        widgets.add(Widgets.createButton(new Rectangle(bounds.x + 5, bounds.y + 6, 13, 13), new TextComponent(""))
                .onRender((poseStack, button) -> {
                    button.setEnabled(tagNode[0] != null);
                })
                .onClick(button -> {
                })
                .tooltipLine(new TranslatableComponent("text.rei.expand.view")));
        widgets.add(Widgets.createButton(new Rectangle(bounds.x + 5, bounds.getMaxY() - 6 - 13, 13, 13), new TextComponent(""))
                .onRender((poseStack, button) -> {
                    button.setEnabled(tagNode[0] != null);
                })
                .onClick(button -> {
                    TagNode<?> node = tagNode[0];
                    
                    if (node != null) {
                        Minecraft.getInstance().keyboardHandler.setClipboard(node.asTree());
                    }
                })
                .tooltipLine(new TranslatableComponent("text.rei.tag.copy.clipboard")));
        widgets.add(Widgets.withTranslate(Widgets.createTexturedWidget(new ResourceLocation("roughlyenoughitems", "textures/gui/expand.png"),
                new Rectangle(bounds.x + 5 + 2, bounds.y + 6 + 2, 13 - 4, 13 - 4), 0, 0, 9, 9), 0, 0, 10));
        widgets.add(Widgets.withTranslate(Widgets.createTexturedWidget(new ResourceLocation("roughlyenoughitems", "textures/gui/clipboard.png"),
                new Rectangle(bounds.x + 5 + 2, bounds.getMaxY() - 6 - 13 + 2, 13 - 4, 13 - 4), 0, 0, 9, 9), 0, 0.5, 10));
        
        return widgets;
    }
}
