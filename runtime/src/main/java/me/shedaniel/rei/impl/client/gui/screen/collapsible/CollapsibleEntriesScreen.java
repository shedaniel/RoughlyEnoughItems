/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020, 2021, 2022, 2023 shedaniel
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

package me.shedaniel.rei.impl.client.gui.screen.collapsible;

import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import me.shedaniel.clothconfig2.ClothConfigInitializer;
import me.shedaniel.clothconfig2.api.scroll.ScrollingContainer;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.config.ConfigObject;
import me.shedaniel.rei.api.client.config.entry.EntryStackProvider;
import me.shedaniel.rei.api.client.gui.widgets.CloseableScissors;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.registry.entry.CollapsibleEntryRegistry;
import me.shedaniel.rei.api.client.registry.entry.EntryRegistry;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.CollectionUtils;
import me.shedaniel.rei.impl.client.config.collapsible.CollapsibleConfigManager;
import me.shedaniel.rei.impl.client.gui.ScreenOverlayImpl;
import me.shedaniel.rei.impl.client.gui.screen.collapsible.selection.CustomCollapsibleEntrySelectionScreen;
import me.shedaniel.rei.impl.client.gui.screen.generic.OptionEntriesScreen;
import me.shedaniel.rei.impl.client.gui.widget.UpdatedListWidget;
import me.shedaniel.rei.impl.common.entry.type.EntryRegistryImpl;
import me.shedaniel.rei.impl.common.entry.type.collapsed.CollapsibleEntryRegistryImpl;
import me.shedaniel.rei.impl.common.util.HashedEntryStackWrapper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import me.shedaniel.rei.api.client.gui.compat.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class CollapsibleEntriesScreen extends me.shedaniel.rei.impl.client.gui.screen.REIScreen {
    private final Runnable onClose;
    private final CollapsibleConfigManager.CollapsibleConfigObject configObject;
    private final List<CollapsibleEntryWidget> widgets = new ArrayList<>();
    private ListWidget listWidget;
    private boolean dirty = true;
    
    public CollapsibleEntriesScreen(Runnable onClose, CollapsibleConfigManager.CollapsibleConfigObject configObject) {
        super(Component.translatable("text.rei.collapsible.entries.title"));
        this.onClose = onClose;
        this.configObject = configObject;
        this.prepareWidgets(configObject);
    }
    
    public void prepareWidgets(CollapsibleConfigManager.CollapsibleConfigObject configObject) {
        this.widgets.clear();
        
        for (CollapsibleConfigManager.CustomGroup customEntry : configObject.customGroups) {
            this.widgets.add(new CollapsibleEntryWidget(true, customEntry.id, Component.literal(customEntry.name),
                    CollectionUtils.filterAndMap(customEntry.stacks, EntryStackProvider::isValid, EntryStackProvider::provide), configObject,
                    () -> {
                        this.prepareWidgets(configObject);
                        this.dirty = true;
                    }));
        }
        
        CollapsibleEntryRegistryImpl collapsibleRegistry = (CollapsibleEntryRegistryImpl) CollapsibleEntryRegistry.getInstance();
        Multimap<Identifier, EntryStack<?>> entries = Multimaps.newListMultimap(new HashMap<>(), ArrayList::new);
        for (HashedEntryStackWrapper wrapper : ((EntryRegistryImpl) EntryRegistry.getInstance()).getFilteredList().getList()) {
            for (CollapsibleEntryRegistryImpl.Entry entry : collapsibleRegistry.getEntries()) {
                if (entry.getMatcher().matches(wrapper.unwrap(), wrapper.hashExact())) {
                    entries.put(entry.getId(), wrapper.unwrap());
                }
            }
        }
        
        for (CollapsibleEntryRegistryImpl.Entry entry : collapsibleRegistry.getEntries()) {
            this.widgets.add(new CollapsibleEntryWidget(false, entry.getId(), entry.getName(), entries.get(entry.getId()), configObject,
                    () -> {
                        this.prepareWidgets(configObject);
                        this.dirty = true;
                    }));
        }
    }
    
    @Override
    public void init() {
        super.init();
        {
            Component backText = Component.literal("↩ ").append(Component.translatable("gui.back"));
            addRenderableWidget(new Button.Plain(4, 4, font.width(backText) + 10, 20, backText,
                    button -> this.onClose(), Supplier::get) {});
        }
        {
            Component addText = Component.literal(" + ");
            addRenderableWidget(new Button.Plain(width - 4 - 20, 4, 20, 20, addText, $ -> {
                setupCustom(Identifier.parse("custom:" + UUID.randomUUID()), "", new ArrayList<>(), this.configObject, () -> {
                    this.prepareWidgets(configObject);
                    this.dirty = true;
                });
            }, Supplier::get) {});
        }
        
        this.listWidget = new ListWidget(width, height, 30);
        ((List<GuiEventListener>) this.children()).add(this.listWidget);
        this.dirty = true;
    }
    
    public static void setupCustom(Identifier id, String name, List<EntryStack<?>> stacks, CollapsibleConfigManager.CollapsibleConfigObject configObject, Runnable markDirty) {
        Minecraft.getInstance().setScreen(new OptionEntriesScreen(Component.translatable("text.rei.collapsible.entries.custom.title"), Minecraft.getInstance().screen) {
            private TextFieldListEntry entry;
            
            @Override
            public void addEntries(Consumer<ListEntry> entryConsumer) {
                addEmpty(entryConsumer, 10);
                addText(entryConsumer, Component.translatable("text.rei.collapsible.entries.custom.id").withStyle(ChatFormatting.GRAY)
                        .append(Component.literal(" " + id).withStyle(ChatFormatting.DARK_GRAY)));
                addEmpty(entryConsumer, 10);
                addText(entryConsumer, Component.translatable("text.rei.collapsible.entries.custom.name").withStyle(ChatFormatting.GRAY));
                entryConsumer.accept(this.entry = new TextFieldListEntry(width - 36, widget -> {
                    widget.setMaxLength(40);
                    if (this.entry != null) widget.setValue(this.entry.getWidget().getValue());
                    else widget.setValue(name);
                }));
                addEmpty(entryConsumer, 10);
                entryConsumer.accept(new ButtonListEntry(width - 36, $ -> Component.translatable("text.rei.collapsible.entries.custom.select"), ($, button) -> {
                    CustomCollapsibleEntrySelectionScreen screen = new CustomCollapsibleEntrySelectionScreen(stacks);
                    screen.parent = this.minecraft.screen;
                    this.minecraft.setScreen(screen);
                }));
            }
            
            @Override
            public void save() {
                configObject.customGroups.removeIf(customGroup -> customGroup.id.equals(id));
                configObject.customGroups.add(new CollapsibleConfigManager.CustomGroup(id, this.entry.getWidget().getValue(),
                        CollectionUtils.map(stacks, EntryStackProvider::ofStack)));
                markDirty.run();
            }
        });
    }
    
    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        if (this.dirty) {
            this.listWidget.clear();
            
            for (CollapsibleEntryWidget widget : this.widgets) {
                this.listWidget.add(widget);
            }
            
            this.dirty = false;
        }
        
        super.render(graphics, mouseX, mouseY, delta);
        this.listWidget.render(graphics, mouseX, mouseY, delta);
        graphics.drawString(this.font, this.title, this.width / 2 - this.font.width(this.title) / 2, 12, -1);
        
        if (ConfigObject.getInstance().doDebugRenderTimeRequired()) {
            Component debugText = Component.literal(String.format("%s fps", minecraft.getFps()));
            int stringWidth = font.width(debugText);
            graphics.fillGradient(minecraft.screen.width - stringWidth - 2, 32, minecraft.screen.width, 32 + font.lineHeight + 2, -16777216, -16777216);
            graphics.pose().pushMatrix();
            graphics.drawString(font, debugText.getVisualOrderText(), minecraft.screen.width - stringWidth, 32 + 2, -1, false);
            graphics.pose().popMatrix();
        }
    }
    
    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        super.renderBackground(graphics, mouseX, mouseY, delta);
        UpdatedListWidget.renderAs(minecraft, this.width, this.height, this.listWidget.top, this.height, graphics, delta);
    }
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amountX, double amountY) {
        return this.listWidget.mouseScrolled(mouseX, mouseY, amountX, amountY) || super.mouseScrolled(mouseX, mouseY, amountX, amountY);
    }
    
    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        return this.listWidget.mouseClicked(event, doubleClick) || super.mouseClicked(event, doubleClick);
    }
    
    @Override
    public boolean mouseDragged(MouseButtonEvent event, double deltaX, double deltaY) {
        return this.listWidget.mouseDragged(event, deltaX, deltaY) || super.mouseDragged(event, deltaX, deltaY);
    }
    
    @Override
    public void onClose() {
        this.onClose.run();
    }
    
    private static class ListWidget extends Widget {
        private static final int PADDING = 6;
        private final int width;
        private final int height;
        private final int top;
        private final ScrollingContainer scroller = new ScrollingContainer() {
            @Override
            public Rectangle getBounds() {
                return new Rectangle(0, top, width, height - top);
            }
            
            @Override
            public int getMaxScrollHeight() {
                return getMaxScrollDist();
            }
        };
        private final List<CollapsibleEntryWidget>[] columns;
        private final List<CollapsibleEntryWidget> children = new ArrayList<>();
        
        public ListWidget(int width, int height, int top) {
            this.width = width;
            this.height = height;
            this.top = top;
            this.columns = new List[Math.max(1, (width - 12 - PADDING) / (130 + PADDING))];
            for (int i = 0; i < columns.length; i++) {
                columns[i] = new ArrayList<>();
            }
        }
        
        @Override
        public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
            this.scroller.updatePosition(delta);
            
            try (CloseableScissors scissors = scissor(graphics, new Rectangle(0, this.top, this.width - 6, this.height - this.top))) {
                int entryWidth = (this.width - 12 - 6 - PADDING) / this.columns.length - PADDING;
                for (int i = 0; i < this.columns.length; i++) {
                    int x = 6 + PADDING + i * (entryWidth + PADDING);
                    int y = this.top + PADDING - scroller.scrollAmountInt();
                    for (CollapsibleEntryWidget widget : this.columns[i]) {
                        widget.setPosition(x, y);
                        widget.setWidth(entryWidth);
                        widget.render(graphics, mouseX, mouseY, delta);
                        y += widget.getHeight() + PADDING;
                    }
                }
            }
            
            this.scroller.renderScrollBar(graphics);
            
            ScreenOverlayImpl.getInstance().lateRender(graphics, mouseX, mouseY, delta);
        }
        
        private int getMaxScrollDist() {
            return Arrays.stream(this.columns).mapToInt(ListWidget::getHeightOf)
                    .max()
                    .orElse(0)
                    + PADDING * 2;
        }
        
        @Override
        public List<? extends GuiEventListener> children() {
            return children;
        }
        
        @Override
        public boolean mouseScrolled(double mouseX, double mouseY, double amountX, double amountY) {
            for (CollapsibleEntryWidget widget : children) {
                if (widget.mouseScrolled(mouseX, mouseY, amountX, amountY)) {
                    return true;
                }
            }
            if (mouseY > this.top && amountY != 0) {
                this.scroller.offset(ClothConfigInitializer.getScrollStep() * -amountY, true);
                return true;
            } else {
                return false;
            }
        }
        
        @Override
        public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
            return this.scroller.updateDraggingState(event.x(), event.y(), event.button()) || super.mouseClicked(event, doubleClick);
        }
        
        @Override
        public boolean mouseDragged(MouseButtonEvent event, double deltaX, double deltaY) {
            return this.scroller.mouseDragged(event.x(), event.y(), event.button(), deltaX, deltaY) || super.mouseDragged(event, deltaX, deltaY);
        }
        
        public void clear() {
            this.children.clear();
            for (List<CollapsibleEntryWidget> column : columns) {
                column.clear();
            }
        }
        
        public void add(CollapsibleEntryWidget widget) {
            Arrays.stream(columns)
                    .min(Comparator.comparingInt(ListWidget::getHeightOf))
                    .ifPresent(widgets -> widgets.add(widget));
            this.children.add(widget);
        }
        
        private static int getHeightOf(List<CollapsibleEntryWidget> widgets) {
            int height = 0;
            for (CollapsibleEntryWidget w : widgets) {
                height += w.getHeight() + PADDING;
            }
            return Math.max(0, height - PADDING);
        }
    }
}
