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

package me.shedaniel.rei.gui.config.entry;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.systems.RenderSystem;
import me.shedaniel.clothconfig2.ClothConfigInitializer;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.gui.ClothConfigScreen;
import me.shedaniel.clothconfig2.gui.widget.DynamicNewSmoothScrollingEntryListWidget;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.math.impl.PointHelper;
import me.shedaniel.rei.api.ConfigObject;
import me.shedaniel.rei.api.EntryRegistry;
import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.api.widgets.Tooltip;
import me.shedaniel.rei.gui.OverlaySearchField;
import me.shedaniel.rei.gui.widget.EntryWidget;
import me.shedaniel.rei.gui.widget.ScrollingContainer;
import me.shedaniel.rei.impl.ScreenHelper;
import me.shedaniel.rei.impl.SearchArgument;
import me.shedaniel.rei.utils.CollectionUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import static me.shedaniel.rei.gui.widget.EntryListWidget.entrySize;

@ApiStatus.Internal
public class FilteringEntry extends AbstractConfigListEntry<List<EntryStack>> {
    protected List<EntryStack> selected = Lists.newArrayList();
    protected final ScrollingContainer scrolling = new ScrollingContainer() {
        @Override
        public int getMaxScrollHeight() {
            return MathHelper.ceil(entryStacks.size() / (innerBounds.width / (float) entrySize())) * entrySize() + 28;
        }
        
        @Override
        public Rectangle getBounds() {
            return FilteringEntry.this.getBounds();
        }
        
        @Override
        public int getScrollBarX() {
            return getParent().right - 7;
        }
    };
    private Consumer<List<EntryStack>> saveConsumer;
    private List<EntryStack> defaultValue;
    private List<EntryStack> configFiltered;
    private Tooltip tooltip = null;
    @SuppressWarnings("rawtypes") private ClothConfigScreen.ListWidget lastList = null;
    private List<EntryStack> entryStacks = null;
    private Rectangle innerBounds;
    private List<EntryListEntry> entries = Collections.emptyList();
    private List<Element> elements = Collections.emptyList();
    
    private Point selectionPoint = null;
    private Point secondPoint = null;
    
    private OverlaySearchField searchField;
    private ButtonWidget selectAllButton;
    private ButtonWidget selectNoneButton;
    private ButtonWidget hideButton;
    private ButtonWidget showButton;
    
    private List<SearchArgument.SearchArguments> lastSearchArguments = Collections.emptyList();
    
    public FilteringEntry(List<EntryStack> configFiltered, List<EntryStack> defaultValue, Consumer<List<EntryStack>> saveConsumer) {
        super("", false);
        this.configFiltered = configFiltered;
        this.defaultValue = defaultValue;
        this.saveConsumer = saveConsumer;
        this.searchField = new OverlaySearchField(0, 0, 0, 0);
        {
            String selectAllText = I18n.translate("config.roughlyenoughitems.filteredEntries.selectAll");
            this.selectAllButton = new ButtonWidget(0, 0, MinecraftClient.getInstance().textRenderer.getStringWidth(selectAllText) + 10, 20, selectAllText, button -> {
                this.selectionPoint = new Point(-Integer.MAX_VALUE / 2, -Integer.MAX_VALUE / 2);
                this.secondPoint = new Point(Integer.MAX_VALUE / 2, Integer.MAX_VALUE / 2);
            });
        }
        {
            String selectNoneText = I18n.translate("config.roughlyenoughitems.filteredEntries.selectNone");
            this.selectNoneButton = new ButtonWidget(0, 0, MinecraftClient.getInstance().textRenderer.getStringWidth(selectNoneText) + 10, 20, selectNoneText, button -> {
                this.selectionPoint = new Point(Integer.MAX_VALUE, Integer.MAX_VALUE);
                this.secondPoint = new Point(Integer.MAX_VALUE, Integer.MAX_VALUE);
            });
        }
        {
            String hideText = I18n.translate("config.roughlyenoughitems.filteredEntries.hide");
            this.hideButton = new ButtonWidget(0, 0, MinecraftClient.getInstance().textRenderer.getStringWidth(hideText) + 10, 20, hideText, button -> {
                for (int i = 0; i < entryStacks.size(); i++) {
                    EntryStack stack = entryStacks.get(i);
                    EntryListEntry entry = entries.get(i);
                    entry.getBounds().y = (int) (entry.backupY - scrolling.scrollAmount);
                    if (entry.isSelected() && !entry.isFiltered()) {
                        configFiltered.add(stack);
                        getScreen().setEdited(true, false);
                    }
                }
            });
        }
        {
            String showText = I18n.translate("config.roughlyenoughitems.filteredEntries.show");
            this.showButton = new ButtonWidget(0, 0, MinecraftClient.getInstance().textRenderer.getStringWidth(showText) + 10, 20, showText, button -> {
                for (int i = 0; i < entryStacks.size(); i++) {
                    EntryStack stack = entryStacks.get(i);
                    EntryListEntry entry = entries.get(i);
                    entry.getBounds().y = (int) (entry.backupY - scrolling.scrollAmount);
                    if (entry.isSelected() && configFiltered.remove(stack)) {
                        getScreen().setEdited(true, false);
                    }
                }
            });
        }
        this.searchField.isMain = false;
    }
    
    private static Rectangle updateInnerBounds(Rectangle bounds) {
        int width = Math.max(MathHelper.floor((bounds.width - 2 - 6) / (float) entrySize()), 1);
        return new Rectangle((int) (bounds.getCenterX() - width * entrySize() / 2f), bounds.y + 5, width * entrySize(), bounds.height);
    }
    
    @SuppressWarnings("rawtypes")
    public Rectangle getBounds() {
        ClothConfigScreen.ListWidget listWidget = getParent();
        return new Rectangle(listWidget.left, listWidget.top, listWidget.right - listWidget.left, listWidget.bottom - listWidget.top);
    }
    
    @Override
    public List<EntryStack> getValue() {
        return configFiltered;
    }
    
    @Override
    public Optional<List<EntryStack>> getDefaultValue() {
        return Optional.ofNullable(defaultValue);
    }
    
    @Override
    public void save() {
        saveConsumer.accept(getValue());
    }
    
    @SuppressWarnings("rawtypes")
    @Override
    public void render(int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean isSelected, float delta) {
        ClothConfigScreen.ListWidget parent = getParent();
        Rectangle bounds = getBounds();
        if (lastList != parent) {
            updateSearch(this.searchField.getText());
            lastList = parent;
            this.searchField.getBounds().setBounds(bounds.getCenterX() - 75, bounds.getMaxY() - 22, 150, 18);
            this.selectAllButton.x = 2;
            this.selectAllButton.y = bounds.getMaxY() - 22;
            this.selectNoneButton.x = 4 + selectAllButton.getWidth();
            this.selectNoneButton.y = bounds.getMaxY() - 22;
            this.hideButton.x = bounds.getMaxX() - hideButton.getWidth() - showButton.getWidth() - 4;
            this.hideButton.y = bounds.getMaxY() - 22;
            this.showButton.x = bounds.getMaxX() - showButton.getWidth() - 2;
            this.showButton.y = bounds.getMaxY() - 22;
            this.searchField.setChangedListener(this::updateSearch);
        }
        tooltip = null;
        if (bounds.isEmpty())
            return;
        for (EntryListEntry entry : entries)
            entry.clearStacks();
        int skip = Math.max(0, MathHelper.floor(scrolling.scrollAmount / (float) entrySize()));
        int nextIndex = skip * innerBounds.width / entrySize();
        int i = nextIndex;
        for (; i < entryStacks.size(); i++) {
            EntryStack stack = entryStacks.get(i);
            EntryListEntry entry = entries.get(nextIndex);
            entry.getBounds().y = (int) (entry.backupY - scrolling.scrollAmount);
            if (entry.getBounds().y > bounds.getMaxY())
                break;
            entry.entry(stack);
            entry.render(mouseX, mouseY, delta);
            nextIndex++;
        }
        updatePosition(delta);
        scrolling.renderScrollBar(0xff000000, 1);
        RenderSystem.translatef(0, 0, 300);
        this.searchField.laterRender(mouseX, mouseY, delta);
        this.selectAllButton.render(mouseX, mouseY, delta);
        this.selectNoneButton.render(mouseX, mouseY, delta);
        this.hideButton.render(mouseX, mouseY, delta);
        this.showButton.render(mouseX, mouseY, delta);
        RenderSystem.translatef(0, 0, -300);
        if (tooltip != null) {
            ScreenHelper.getLastOverlay().renderTooltip(tooltip);
        }
    }
    
    private Rectangle getSelection() {
        if (selectionPoint != null) {
            Point p = secondPoint;
            if (p == null) {
                p = PointHelper.ofMouse();
                p.translate(0, (int) scrolling.scrollAmount);
            }
            int left = Math.min(p.x, selectionPoint.x);
            int top = Math.min(p.y, selectionPoint.y);
            int right = Math.max(p.x, selectionPoint.x);
            int bottom = Math.max(p.y, selectionPoint.y);
            return new Rectangle(left, (int) (top - scrolling.scrollAmount), right - left, bottom - top);
        }
        return new Rectangle(0, 0, 0, 0);
    }
    
    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dx, double dy) {
        if (scrolling.mouseDragged(mouseX, mouseY, button, dx, dy, true))
            return true;
        return super.mouseDragged(mouseX, mouseY, button, dx, dy);
    }
    
    private void updatePosition(float delta) {
        if (ConfigObject.getInstance().doesSnapToRows() && scrolling.scrollTarget >= 0 && scrolling.scrollTarget <= scrolling.getMaxScroll()) {
            double nearestRow = Math.round(scrolling.scrollTarget / (double) entrySize()) * (double) entrySize();
            if (!DynamicNewSmoothScrollingEntryListWidget.Precision.almostEquals(scrolling.scrollTarget, nearestRow, DynamicNewSmoothScrollingEntryListWidget.Precision.FLOAT_EPSILON))
                scrolling.scrollTarget += (nearestRow - scrolling.scrollTarget) * Math.min(delta / 2.0, 1.0);
            else
                scrolling.scrollTarget = nearestRow;
        }
        scrolling.updatePosition(delta);
    }
    
    public void updateSearch(String searchTerm) {
        lastSearchArguments = SearchArgument.processSearchTerm(searchTerm);
        Set<EntryStack> list = Sets.newLinkedHashSet();
        for (EntryStack stack : EntryRegistry.getInstance().getStacksList()) {
            if (canLastSearchTermsBeAppliedTo(stack)) {
                list.add(stack.copy().setting(EntryStack.Settings.CHECK_AMOUNT, EntryStack.Settings.FALSE).setting(EntryStack.Settings.RENDER_COUNTS, EntryStack.Settings.FALSE).setting(EntryStack.Settings.CHECK_TAGS, EntryStack.Settings.TRUE));
            }
        }
        
        entryStacks = Lists.newArrayList(list);
        updateEntriesPosition();
    }
    
    public boolean canLastSearchTermsBeAppliedTo(EntryStack stack) {
        return lastSearchArguments.isEmpty() || SearchArgument.canSearchTermsBeAppliedTo(stack, lastSearchArguments);
    }
    
    public void updateEntriesPosition() {
        this.innerBounds = updateInnerBounds(getBounds());
        int width = innerBounds.width / entrySize();
        int pageHeight = innerBounds.height / entrySize();
        int slotsToPrepare = Math.max(entryStacks.size() * 3, width * pageHeight * 3);
        int currentX = 0;
        int currentY = 0;
        List<EntryListEntry> entries = Lists.newArrayList();
        for (int i = 0; i < slotsToPrepare; i++) {
            int xPos = currentX * entrySize() + innerBounds.x;
            int yPos = currentY * entrySize() + innerBounds.y;
            entries.add(new EntryListEntry(xPos, yPos));
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
    public List<? extends Element> children() {
        return elements;
    }
    
    @Override
    public boolean mouseClicked(double double_1, double double_2, int int_1) {
        if (scrolling.updateDraggingState(double_1, double_2, int_1))
            return true;
        
        if (getBounds().contains(double_1, double_2)) {
            if (searchField.mouseClicked(double_1, double_2, int_1)) {
                this.selectionPoint = null;
                this.secondPoint = null;
                return true;
            } else if (selectAllButton.mouseClicked(double_1, double_2, int_1)) {
                return true;
            } else if (selectNoneButton.mouseClicked(double_1, double_2, int_1)) {
                return true;
            } else if (hideButton.mouseClicked(double_1, double_2, int_1)) {
                return true;
            } else if (showButton.mouseClicked(double_1, double_2, int_1)) {
                return true;
            }
            if (int_1 == 0) {
                this.selectionPoint = new Point(double_1, double_2 + scrolling.scrollAmount);
                this.secondPoint = null;
                return true;
            }
        }
        return false;
    }
    
    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (selectionPoint != null && button == 0 && secondPoint == null) {
            this.secondPoint = new Point(mouseX, mouseY + scrolling.scrollAmount);
            if (secondPoint.equals(selectionPoint)) {
                secondPoint.translate(1, 1);
            }
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }
    
    @Override
    public boolean charTyped(char chr, int keyCode) {
        for (Element element : children())
            if (element.charTyped(chr, keyCode))
                return true;
        return super.charTyped(chr, keyCode);
    }
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        for (Element element : children())
            if (element.keyPressed(keyCode, scanCode, modifiers))
                return true;
        if (Screen.isSelectAll(keyCode)) {
            this.selectionPoint = new Point(0, 0);
            this.secondPoint = new Point(Integer.MAX_VALUE, Integer.MAX_VALUE);
            return true;
        }
        return false;
    }
    
    public void updateArea(@Nullable String searchTerm) {
        if (searchTerm != null)
            updateSearch(searchTerm);
        else if (entryStacks == null)
            updateSearch("");
        else
            updateEntriesPosition();
    }
    
    @Override
    public boolean mouseScrolled(double double_1, double double_2, double double_3) {
        if (getBounds().contains(double_1, double_2)) {
            scrolling.offset(ClothConfigInitializer.getScrollStep() * -double_3, true);
            return true;
        }
        super.mouseScrolled(double_1, double_2, double_3);
        return true;
    }
    
    private class EntryListEntry extends EntryWidget {
        private int backupY;
        
        private EntryListEntry(int x, int y) {
            super(new Point(x, y));
            this.backupY = y;
            getBounds().width = getBounds().height = entrySize();
            interactableFavorites(false);
            interactable(false);
            noHighlight();
        }
        
        @Override
        public boolean containsMouse(double mouseX, double mouseY) {
            return super.containsMouse(mouseX, mouseY) && FilteringEntry.this.getBounds().contains(mouseX, mouseY);
        }
        
        @Override
        protected void drawHighlighted(int mouseX, int mouseY, float delta) {
        
        }
        
        @Override
        public void render(int mouseX, int mouseY, float delta) {
            super.render(mouseX, mouseY, delta);
            if (isSelected()) {
                Rectangle bounds = getBounds();
                RenderSystem.disableDepthTest();
                fillGradient(bounds.x, bounds.y, bounds.getMaxX(), bounds.getMaxY(), 0x896b70fa, 0x896b70fa);
                RenderSystem.enableDepthTest();
            }
        }
        
        @Override
        public EntryStack getCurrentEntry() {
            return super.getCurrentEntry();
        }
        
        public boolean isSelected() {
            return getSelection().intersects(getBounds());
        }
        
        public boolean isFiltered() {
            return CollectionUtils.findFirstOrNullEqualsEntryIgnoreAmount(configFiltered, getCurrentEntry()) != null;
        }
        
        @Override
        protected void drawBackground(int mouseX, int mouseY, float delta) {
            if (isFiltered()) {
                Rectangle bounds = getBounds();
                RenderSystem.disableDepthTest();
                fillGradient(bounds.x, bounds.y, bounds.getMaxX(), bounds.getMaxY(), 0xffff0000, 0xffff0000);
                RenderSystem.enableDepthTest();
            }
        }
        
        @Override
        protected void queueTooltip(int mouseX, int mouseY, float delta) {
            if (searchField.containsMouse(mouseX, mouseY))
                return;
            Tooltip tooltip = getCurrentTooltip(new Point(mouseX, mouseY));
            if (tooltip != null) {
                FilteringEntry.this.tooltip = tooltip;
            }
        }
    }
}
