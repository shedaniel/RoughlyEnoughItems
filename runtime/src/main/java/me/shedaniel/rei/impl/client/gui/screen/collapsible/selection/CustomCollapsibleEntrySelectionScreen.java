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

package me.shedaniel.rei.impl.client.gui.screen.collapsible.selection;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import me.shedaniel.clothconfig2.ClothConfigInitializer;
import me.shedaniel.clothconfig2.api.scroll.ScrollingContainer;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.math.impl.PointHelper;
import me.shedaniel.rei.api.client.REIRuntime;
import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import me.shedaniel.rei.api.client.gui.widgets.TooltipContext;
import me.shedaniel.rei.api.client.registry.entry.EntryRegistry;
import me.shedaniel.rei.api.client.search.SearchFilter;
import me.shedaniel.rei.api.client.search.SearchProvider;
import me.shedaniel.rei.api.common.entry.EntrySerializer;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.impl.client.gui.ScreenOverlayImpl;
import me.shedaniel.rei.impl.client.gui.widget.EntryWidget;
import me.shedaniel.rei.impl.client.gui.widget.UpdatedListWidget;
import me.shedaniel.rei.impl.client.gui.widget.search.OverlaySearchField;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import me.shedaniel.rei.api.client.gui.compat.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static me.shedaniel.rei.impl.client.gui.widget.entrylist.EntryListWidget.entrySize;

@ApiStatus.Internal
public class CustomCollapsibleEntrySelectionScreen extends me.shedaniel.rei.impl.client.gui.screen.REIScreen {
    private final List<EntryStack<?>> selectedStacks;
    protected List<EntryStack<?>> selected = Lists.newArrayList();
    protected final ScrollingContainer scrolling = new ScrollingContainer() {
        @Override
        public int getMaxScrollHeight() {
            return Mth.ceil(entryStacks.size() / (innerBounds.width / (float) entrySize())) * entrySize() + 28;
        }
        
        @Override
        public Rectangle getBounds() {
            return CustomCollapsibleEntrySelectionScreen.this.getBounds();
        }
        
        @Override
        public int getScrollBarX(int maxX) {
            return width - 7;
        }
    };
    
    public Screen parent;
    private Tooltip tooltip = null;
    private List<EntryStack<?>> entryStacks = null;
    private Rectangle innerBounds;
    private List<InnerStackEntry> entries = Collections.emptyList();
    private List<GuiEventListener> elements = Collections.emptyList();
    
    private record PointPair(Point firstPoint, @Nullable Point secondPoint) {
    }
    
    private final List<PointPair> points = new ArrayList<>();
    
    private final OverlaySearchField searchField;
    private final Button selectAllButton;
    private final Button selectNoneButton;
    private final Button addButton;
    private final Button removeButton;
    private final Button backButton;
    private Predicate<Rectangle> selectionCache;
    
    private SearchFilter lastFilter = SearchFilter.matchAll();
    
    public CustomCollapsibleEntrySelectionScreen(List<EntryStack<?>> selectedStacks) {
        super(Component.translatable("text.rei.collapsible.entries.custom.title"));
        this.selectedStacks = selectedStacks;
        this.searchField = new OverlaySearchField(0, 0, 0, 0);
        {
            Component selectAllText = Component.translatable("config.roughlyenoughitems.filteredEntries.selectAll");
            this.selectAllButton = new Button.Plain(0, 0, Minecraft.getInstance().font.width(selectAllText) + 10, 20, selectAllText, button -> {
                this.points.clear();
                this.points.add(new PointPair(new Point(-Integer.MAX_VALUE / 2, -Integer.MAX_VALUE / 2), new Point(Integer.MAX_VALUE / 2, Integer.MAX_VALUE / 2)));
            }, Supplier::get) {};
        }
        {
            Component selectNoneText = Component.translatable("config.roughlyenoughitems.filteredEntries.selectNone");
            this.selectNoneButton = new Button.Plain(0, 0, Minecraft.getInstance().font.width(selectNoneText) + 10, 20, selectNoneText, button -> {
                this.points.clear();
            }, Supplier::get) {};
        }
        {
            Component addText = Component.translatable("text.rei.collapsible.entries.custom.select.add");
            this.addButton = new Button.Plain(0, 0, Minecraft.getInstance().font.width(addText) + 10, 20, addText, button -> {
                for (int i = 0; i < entryStacks.size(); i++) {
                    EntryStack<?> stack = entryStacks.get(i);
                    InnerStackEntry entry = entries.get(i);
                    entry.getBounds().y = entry.backupY - scrolling.scrollAmountInt();
                    if (entry.isSelected() && !entry.isFiltered()) {
                        selectedStacks.add(stack);
                        entry.dirty = true;
                    }
                }
            }, Supplier::get) {};
        }
        {
            Component removeText = Component.translatable("text.rei.collapsible.entries.custom.select.remove");
            this.removeButton = new Button.Plain(0, 0, Minecraft.getInstance().font.width(removeText) + 10, 20, removeText, button -> {
                for (int i = 0; i < entryStacks.size(); i++) {
                    EntryStack<?> stack = entryStacks.get(i);
                    InnerStackEntry entry = entries.get(i);
                    entry.getBounds().y = entry.backupY - scrolling.scrollAmountInt();
                    if (entry.isSelected() && selectedStacks.remove(stack)) {
                        entry.dirty = true;
                    }
                }
            }, Supplier::get) {};
        }
        {
            Component backText = Component.literal("↩ ").append(Component.translatable("gui.back"));
            this.backButton = new Button.Plain(0, 0, Minecraft.getInstance().font.width(backText) + 10, 20, backText, button -> {
                minecraft.setScreen(parent);
                this.parent = null;
            }, Supplier::get) {};
        }
        this.searchField.isMain = false;
    }
    
    @Override
    public void onClose() {
        this.minecraft.setScreen(parent);
        this.parent = null;
    }
    
    private static Rectangle updateInnerBounds(Rectangle bounds) {
        int width = Math.max(Mth.floor((bounds.width - 2 - 6) / (float) entrySize()), 1);
        return new Rectangle((int) (bounds.getCenterX() - width * entrySize() / 2f), bounds.y + 5, width * entrySize(), bounds.height);
    }
    
    public Rectangle getBounds() {
        return new Rectangle(0, 30, width, this.height - 30);
    }
    
    @Override
    public void init() {
        super.init();
        Rectangle bounds = getBounds();
        updateSearch(this.searchField.getText());
        this.selectAllButton.setX(2);
        this.selectAllButton.setY(bounds.getMaxY() - 22);
        this.selectNoneButton.setX(4 + selectAllButton.getWidth());
        this.selectNoneButton.setY(bounds.getMaxY() - 22);
        int searchFieldWidth = Math.max(bounds.width - (selectNoneButton.getX() + selectNoneButton.getWidth() + addButton.getWidth() + removeButton.getWidth() + 12), 100);
        this.searchField.getBounds().setBounds(selectNoneButton.getX() + selectNoneButton.getWidth() + 4, bounds.getMaxY() - 21, searchFieldWidth, 18);
        this.addButton.setX(bounds.getMaxX() - addButton.getWidth() - removeButton.getWidth() - 4);
        this.addButton.setY(bounds.getMaxY() - 22);
        this.removeButton.setX(bounds.getMaxX() - removeButton.getWidth() - 2);
        this.removeButton.setY(bounds.getMaxY() - 22);
        this.backButton.setX(4);
        this.backButton.setY(4);
        this.searchField.setResponder(this::updateSearch);
    }
    
    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        super.render(graphics, mouseX, mouseY, delta);
        updateSelectionCache();
        Rectangle bounds = getBounds();
        tooltip = null;
        UpdatedListWidget.renderAs(minecraft, width, height, bounds.y, height, graphics, delta);
        if (bounds.isEmpty())
            return;
        graphics.enableScissor(bounds.x, bounds.y, bounds.getMaxX(), bounds.getMaxY());
        for (InnerStackEntry entry : entries)
            entry.clearStacks();
        int skip = Math.max(0, Mth.floor(scrolling.scrollAmount() / (float) entrySize()));
        int nextIndex = skip * innerBounds.width / entrySize();
        int i = nextIndex;
        for (; i < entryStacks.size(); i++) {
            EntryStack<?> stack = entryStacks.get(i);
            InnerStackEntry entry = entries.get(nextIndex);
            entry.getBounds().y = entry.backupY - scrolling.scrollAmountInt();
            if (entry.getBounds().y > bounds.getMaxY())
                break;
            entry.entry(stack);
            entry.render(graphics, mouseX, mouseY, delta);
            nextIndex++;
        }
        updatePosition(delta);
        scrolling.renderScrollBar(graphics, 0, REIRuntime.getInstance().isDarkThemeEnabled() ? 0.8F : 1F);
        this.searchField.extractRenderState(graphics, mouseX, mouseY, delta);
        this.selectAllButton.extractRenderState(graphics, mouseX, mouseY, delta);
        this.selectNoneButton.extractRenderState(graphics, mouseX, mouseY, delta);
        this.addButton.extractRenderState(graphics, mouseX, mouseY, delta);
        this.removeButton.extractRenderState(graphics, mouseX, mouseY, delta);
        
        graphics.disableScissor();
        graphics.fillGradient(0, bounds.y, width, bounds.y + 4, 0xFF000000, 0x00000000);
        
        this.backButton.extractRenderState(graphics, mouseX, mouseY, delta);
        
        if (tooltip != null) {
            ScreenOverlayImpl.getInstance().renderTooltip(graphics, tooltip);
        }
        
        graphics.drawString(this.font, this.title.getVisualOrderText(), this.width / 2 - this.font.width(this.title) / 2, 12, -1);
        Component hint = Component.translatable("config.roughlyenoughitems.filteringRulesScreen.hint").withStyle(ChatFormatting.YELLOW);
        graphics.drawString(this.font, hint, this.width - this.font.width(hint) - 15, 12, -1);
    }
    
    private Predicate<Rectangle> getSelection() {
        return selectionCache;
    }
    
    private void updateSelectionCache() {
        if (!points.isEmpty()) {
            Predicate<Rectangle> predicate = rect -> false;
            for (PointPair pair : points) {
                Point firstPoint = pair.firstPoint();
                Point secondPoint = pair.secondPoint();
                if (secondPoint == null) {
                    secondPoint = PointHelper.ofMouse();
                    secondPoint.translate(0, scrolling.scrollAmountInt());
                }
                int left = Math.min(firstPoint.x, secondPoint.x);
                int top = Math.min(firstPoint.y, secondPoint.y);
                int right = Math.max(firstPoint.x, secondPoint.x);
                int bottom = Math.max(firstPoint.y, secondPoint.y);
                Rectangle rectangle = new Rectangle(left, top - scrolling.scrollAmountInt(), Math.max(1, right - left), Math.max(1, bottom - top));
                predicate = predicate.or(rectangle::intersects);
            }
            selectionCache = predicate;
            return;
        }
        selectionCache = rect -> false;
    }
    
    @Override
    public boolean mouseDragged(MouseButtonEvent event, double deltaX, double deltaY) {
        if (scrolling.mouseDragged(event.x(), event.y(), event.button(), deltaX, deltaY))
            return true;
        return super.mouseDragged(event, deltaX, deltaY);
    }
    
    private void updatePosition(float delta) {
        scrolling.updatePosition(delta);
    }
    
    public void updateSearch(String searchTerm) {
        lastFilter = SearchProvider.getInstance().createFilter(searchTerm);
        Set<EntryStack<?>> list = Sets.newLinkedHashSet();
        EntryRegistry.getInstance().getEntryStacks().parallel().filter(this::matches).map(EntryStack::normalize).forEachOrdered(list::add);
        
        entryStacks = Lists.newArrayList(list);
        updateEntriesPosition();
    }
    
    public boolean matches(EntryStack<?> stack) {
        EntrySerializer<?> serializer = stack.getDefinition().getSerializer();
        if (serializer == null) {
            return false;
        }
        return lastFilter.test(stack);
    }
    
    public void updateEntriesPosition() {
        int entrySize = entrySize();
        this.innerBounds = updateInnerBounds(getBounds());
        int width = innerBounds.width / entrySize;
        int pageHeight = innerBounds.height / entrySize;
        int slotsToPrepare = Math.max(entryStacks.size() * 3, width * pageHeight * 3);
        int currentX = 0;
        int currentY = 0;
        List<InnerStackEntry> entries = Lists.newArrayList();
        for (int i = 0; i < slotsToPrepare; i++) {
            int xPos = currentX * entrySize + innerBounds.x;
            int yPos = currentY * entrySize + innerBounds.y;
            entries.add(new InnerStackEntry(xPos, yPos, entrySize));
            currentX++;
            if (currentX >= width) {
                currentX = 0;
                currentY++;
            }
        }
        this.entries = entries;
        this.elements = Lists.newArrayList(entries);
        this.elements.add(searchField);
    }
    
    @Override
    public List<? extends GuiEventListener> children() {
        return elements;
    }
    
    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (scrolling.updateDraggingState(event.x(), event.y(), event.button()))
            return true;
        
        if (getBounds().contains(event.x(), event.y())) {
            if (searchField.mouseClicked(event, doubleClick)) {
                this.points.clear();
                return true;
            } else if (selectAllButton.mouseClicked(event, doubleClick)) {
                return true;
            } else if (selectNoneButton.mouseClicked(event, doubleClick)) {
                return true;
            } else if (addButton.mouseClicked(event, doubleClick)) {
                return true;
            } else if (removeButton.mouseClicked(event, doubleClick)) {
                return true;
            } else if (event.button() == 0) {
                if (!event.hasShiftDown()) {
                    this.points.clear();
                }
                this.points.add(new PointPair(new Point(event.x(), event.y() + scrolling.scrollAmount()), null));
                return true;
            }
        }
        
        return backButton.mouseClicked(event, doubleClick);
    }
    
    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (event.button() == 0 && !points.isEmpty()) {
            PointPair pair = this.points.get(points.size() - 1);
            if (pair.secondPoint() == null) {
                this.points.set(points.size() - 1, new PointPair(pair.firstPoint(), new Point(event.x(), event.y() + scrolling.scrollAmount())));
                return true;
            }
        }
        
        return super.mouseReleased(event);
    }
    
    @Override
    public boolean charTyped(CharacterEvent event) {
        for (GuiEventListener element : children()) {
            if (element.charTyped(event)) {
                return true;
            }
        }
        
        return super.charTyped(event);
    }
    
    @Override
    public boolean keyPressed(KeyEvent event) {
        for (GuiEventListener element : children()) {
            if (element.keyPressed(event)) {
                return true;
            }
        }
        
        if (event.isSelectAll()) {
            this.points.clear();
            this.points.add(new PointPair(new Point(-Integer.MAX_VALUE / 2, -Integer.MAX_VALUE / 2), new Point(Integer.MAX_VALUE / 2, Integer.MAX_VALUE / 2)));
            return true;
        }
        
        if (event.isEscape() && this.shouldCloseOnEsc()) {
            this.backButton.onPress(event);
            return true;
        }
        return false;
    }
    
    public void updateArea(@Nullable String searchTerm) {
        if (searchTerm != null) {
            updateSearch(searchTerm);
        } else if (entryStacks == null) {
            updateSearch("");
        } else {
            updateEntriesPosition();
        }
    }
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amountX, double amountY) {
        if (getBounds().contains(mouseX, mouseY) && amountY != 0) {
            scrolling.offset(ClothConfigInitializer.getScrollStep() * -amountY, true);
            return true;
        }
        
        super.mouseScrolled(mouseX, mouseY, amountX, amountY);
        return true;
    }
    
    private class InnerStackEntry extends EntryWidget {
        private final int backupY;
        private boolean filtered = false;
        private boolean dirty = true;
        
        private InnerStackEntry(int x, int y, int entrySize) {
            super(new Point(x, y));
            this.backupY = y;
            getBounds().width = getBounds().height = entrySize;
            interactableFavorites(false);
            interactable(false);
            noHighlight();
        }
        
        @Override
        public boolean containsMouse(double mouseX, double mouseY) {
            return super.containsMouse(mouseX, mouseY) && CustomCollapsibleEntrySelectionScreen.this.getBounds().contains(mouseX, mouseY);
        }
        
        @Override
        protected void drawExtra(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
            if (isSelected()) {
                boolean filtered = isFiltered();
                Rectangle bounds = getBounds();
                graphics.fillGradient(bounds.x, bounds.y, bounds.getMaxX(), bounds.getMaxY(), filtered ? 0x70ffffff : 0x55ffffff, filtered ? 0x70ffffff : 0x55ffffff);
            }
        }
        
        public boolean isSelected() {
            return getSelection().test(getBounds());
        }
        
        public boolean isFiltered() {
            if (dirty) {
                filtered = selectedStacks.contains(getCurrentEntry());
                dirty = false;
            }
            return filtered;
        }
        
        @Override
        protected void drawBackground(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
            if (isFiltered()) {
                Rectangle bounds = getBounds();
                graphics.fillGradient(bounds.x, bounds.y, bounds.getMaxX(), bounds.getMaxY(), 0xff873e23, 0xff873e23);
            }
        }
        
        @Override
        protected void queueTooltip(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
            if (searchField.containsMouse(mouseX, mouseY))
                return;
            Tooltip tooltip = getCurrentTooltip(TooltipContext.of(new Point(mouseX, mouseY), Item.TooltipContext.of(minecraft.level)));
            if (tooltip != null) {
                CustomCollapsibleEntrySelectionScreen.this.tooltip = tooltip;
            }
        }
    }
}
