/*
 * Copyright (c) 2018, 2019, 2020 shedaniel
 * Licensed under the MIT License (the "License").
 */

package me.shedaniel.rei.gui.config.entry;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import me.shedaniel.clothconfig2.ClothConfigInitializer;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.gui.ClothConfigScreen;
import me.shedaniel.clothconfig2.gui.widget.DynamicNewSmoothScrollingEntryListWidget;
import me.shedaniel.math.api.Point;
import me.shedaniel.math.api.Rectangle;
import me.shedaniel.math.impl.PointHelper;
import me.shedaniel.rei.api.ConfigObject;
import me.shedaniel.rei.api.EntryRegistry;
import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.gui.OverlaySearchField;
import me.shedaniel.rei.gui.widget.EntryWidget;
import me.shedaniel.rei.gui.widget.QueuedTooltip;
import me.shedaniel.rei.impl.ScreenHelper;
import me.shedaniel.rei.impl.SearchArgument;
import me.shedaniel.rei.utils.CollectionUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static me.shedaniel.rei.gui.widget.EntryListWidget.entrySize;

@ApiStatus.Internal
public class FilteringEntry extends AbstractConfigListEntry<List<EntryStack>> {
    private Consumer<List<EntryStack>> saveConsumer;
    private List<EntryStack> defaultValue;
    private List<EntryStack> configFiltered;
    
    private QueuedTooltip tooltip = null;
    
    @SuppressWarnings("rawtypes") private ClothConfigScreen.ListWidget lastList = null;
    
    protected List<EntryStack> selected = Lists.newArrayList();
    
    protected double target;
    protected double scroll;
    protected long start;
    protected long duration;
    private List<EntryStack> entryStacks = null;
    private Rectangle innerBounds;
    private List<EntryListEntry> entries = Collections.emptyList();
    private List<Element> elements = Collections.emptyList();
    private boolean draggingScrollBar = false;
    
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
                    entry.getBounds().y = (int) (entry.backupY - scroll);
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
                    entry.getBounds().y = (int) (entry.backupY - scroll);
                    if (entry.isSelected() && configFiltered.remove(stack)) {
                        getScreen().setEdited(true, false);
                    }
                }
            });
        }
        this.searchField.isMain = false;
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
        int skip = Math.max(0, MathHelper.floor(scroll / (float) entrySize()));
        int nextIndex = skip * innerBounds.width / entrySize();
        int i = nextIndex;
        for (; i < entryStacks.size(); i++) {
            EntryStack stack = entryStacks.get(i);
            EntryListEntry entry = entries.get(nextIndex);
            entry.getBounds().y = (int) (entry.backupY - scroll);
            if (entry.getBounds().y > bounds.getMaxY())
                break;
            entry.entry(stack);
            entry.render(mouseX, mouseY, delta);
            nextIndex++;
        }
        updatePosition(delta);
        renderScrollbar();
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
                p = PointHelper.fromMouse();
                p.translate(0, (int) scroll);
            }
            int left = Math.min(p.x, selectionPoint.x);
            int top = Math.min(p.y, selectionPoint.y);
            int right = Math.max(p.x, selectionPoint.x);
            int bottom = Math.max(p.y, selectionPoint.y);
            return new Rectangle(left, (int) (top - scroll), right - left, bottom - top);
        }
        return new Rectangle(0, 0, 0, 0);
    }
    
    private int getScrollbarMinX() {
        return getParent().right - 7;
    }
    
    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int int_1, double double_3, double double_4) {
        if (int_1 == 0 && draggingScrollBar) {
            float height = getMaxScrollPosition();
            int actualHeight = innerBounds.height;
            if (height > actualHeight && mouseY >= innerBounds.y && mouseY <= innerBounds.getMaxY()) {
                double double_5 = Math.max(1, this.getMaxScroll());
                int int_2 = innerBounds.height;
                int int_3 = MathHelper.clamp((int) ((float) (int_2 * int_2) / (float) getMaxScrollPosition()), 32, int_2 - 8);
                double double_6 = Math.max(1.0D, double_5 / (double) (int_2 - int_3));
                float to = MathHelper.clamp((float) (scroll + double_4 * double_6), 0, height - innerBounds.height);
                if (ConfigObject.getInstance().doesSnapToRows()) {
                    double nearestRow = Math.round(to / (double) entrySize()) * (double) entrySize();
                    scrollTo(nearestRow, false);
                } else
                    scrollTo(to, false);
            }
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, int_1, double_3, double_4);
    }
    
    private void renderScrollbar() {
        int maxScroll = getMaxScroll();
        if (maxScroll > 0) {
            int height = innerBounds.height * innerBounds.height / getMaxScrollPosition();
            height = MathHelper.clamp(height, 32, innerBounds.height - 8);
            height -= Math.min((scroll < 0 ? (int) -scroll : scroll > maxScroll ? (int) scroll - maxScroll : 0), height * .95);
            height = Math.max(10, height);
            int minY = Math.min(Math.max((int) scroll * (innerBounds.height - height) / maxScroll + innerBounds.y, innerBounds.y), innerBounds.getMaxY() - height);
            
            int scrollbarPositionMinX = getScrollbarMinX();
            int scrollbarPositionMaxX = scrollbarPositionMinX + 6;
            boolean hovered = (new Rectangle(scrollbarPositionMinX, minY, scrollbarPositionMaxX - scrollbarPositionMinX, height)).contains(PointHelper.fromMouse());
            float bottomC = (hovered ? .67f : .5f) * (ScreenHelper.isDarkModeEnabled() ? 0.8f : 1f);
            float topC = (hovered ? .87f : .67f) * (ScreenHelper.isDarkModeEnabled() ? 0.8f : 1f);
            
            RenderSystem.disableTexture();
            RenderSystem.enableBlend();
            RenderSystem.disableAlphaTest();
            RenderSystem.blendFuncSeparate(770, 771, 1, 0);
            RenderSystem.shadeModel(7425);
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();
            buffer.begin(7, VertexFormats.POSITION_COLOR);
            buffer.vertex(scrollbarPositionMinX, minY + height, 0.0D).color(bottomC, bottomC, bottomC, 1).next();
            buffer.vertex(scrollbarPositionMaxX, minY + height, 0.0D).color(bottomC, bottomC, bottomC, 1).next();
            buffer.vertex(scrollbarPositionMaxX, minY, 0.0D).color(bottomC, bottomC, bottomC, 1).next();
            buffer.vertex(scrollbarPositionMinX, minY, 0.0D).color(bottomC, bottomC, bottomC, 1).next();
            tessellator.draw();
            buffer.begin(7, VertexFormats.POSITION_COLOR);
            buffer.vertex(scrollbarPositionMinX, (minY + height - 1), 0.0D).color(topC, topC, topC, 1).next();
            buffer.vertex((scrollbarPositionMaxX - 1), (minY + height - 1), 0.0D).color(topC, topC, topC, 1).next();
            buffer.vertex((scrollbarPositionMaxX - 1), minY, 0.0D).color(topC, topC, topC, 1).next();
            buffer.vertex(scrollbarPositionMinX, minY, 0.0D).color(topC, topC, topC, 1).next();
            tessellator.draw();
            RenderSystem.shadeModel(7424);
            RenderSystem.disableBlend();
            RenderSystem.enableAlphaTest();
            RenderSystem.enableTexture();
        }
    }
    
    private void updatePosition(float delta) {
        if (ConfigObject.getInstance().doesSnapToRows() && target >= 0 && target <= getMaxScroll()) {
            double nearestRow = Math.round(target / (double) entrySize()) * (double) entrySize();
            if (!DynamicNewSmoothScrollingEntryListWidget.Precision.almostEquals(target, nearestRow, DynamicNewSmoothScrollingEntryListWidget.Precision.FLOAT_EPSILON))
                target += (nearestRow - target) * Math.min(delta / 2.0, 1.0);
            else
                target = nearestRow;
        }
        double[] targetD = new double[]{this.target};
        this.scroll = ClothConfigInitializer.handleScrollingPosition(targetD, this.scroll, this.getMaxScroll(), delta, this.start, this.duration);
        this.target = targetD[0];
    }
    
    public void updateSearch(String searchTerm) {
        lastSearchArguments = SearchArgument.processSearchTerm(searchTerm);
        List<EntryStack> list = Lists.newArrayList();
        for (EntryStack stack : EntryRegistry.getInstance().getStacksList()) {
            if (canLastSearchTermsBeAppliedTo(stack)) {
                list.add(stack.copy().setting(EntryStack.Settings.RENDER_COUNTS, EntryStack.Settings.FALSE).setting(EntryStack.Settings.CHECK_TAGS, EntryStack.Settings.TRUE));
            }
        }
        entryStacks = list;
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
        double height = getMaxScroll();
        Rectangle bounds = getBounds();
        int actualHeight = bounds.height;
        if (height > actualHeight && double_2 >= bounds.y && double_2 <= bounds.getMaxY()) {
            double scrollbarPositionMinX = getScrollbarMinX();
            if (double_1 >= scrollbarPositionMinX - 1 & double_1 <= scrollbarPositionMinX + 8) {
                this.draggingScrollBar = true;
                return true;
            }
        }
        this.draggingScrollBar = false;
        
        if (bounds.contains(double_1, double_2)) {
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
                this.selectionPoint = new Point(double_1, double_2 + scroll);
                this.secondPoint = null;
                return true;
            }
        }
        return false;
    }
    
    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (selectionPoint != null && button == 0 && secondPoint == null) {
            this.secondPoint = new Point(mouseX, mouseY + scroll);
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
    
    private static Rectangle updateInnerBounds(Rectangle bounds) {
        int width = Math.max(MathHelper.floor((bounds.width - 2 - 6) / (float) entrySize()), 1);
        return new Rectangle((int) (bounds.getCenterX() - width * entrySize() / 2f), bounds.y + 5, width * entrySize(), bounds.height);
    }
    
    protected final int getMaxScrollPosition() {
        return MathHelper.ceil(entryStacks.size() / (innerBounds.width / (float) entrySize())) * entrySize() + 28;
    }
    
    protected final int getMaxScroll() {
        return Math.max(0, this.getMaxScrollPosition() - innerBounds.height);
    }
    
    protected final double clamp(double v) {
        return this.clamp(v, 200.0D);
    }
    
    protected final double clamp(double v, double clampExtension) {
        return MathHelper.clamp(v, -clampExtension, (double) this.getMaxScroll() + clampExtension);
    }
    
    protected final void offset(double value, boolean animated) {
        scrollTo(target + value, animated);
    }
    
    protected final void scrollTo(double value, boolean animated) {
        scrollTo(value, animated, ClothConfigInitializer.getScrollDuration());
    }
    
    protected final void scrollTo(double value, boolean animated, long duration) {
        target = clamp(value);
        
        if (animated) {
            start = System.currentTimeMillis();
            this.duration = duration;
        } else
            scroll = target;
    }
    
    @Override
    public boolean mouseScrolled(double double_1, double double_2, double double_3) {
        if (getBounds().contains(double_1, double_2)) {
            offset(ClothConfigInitializer.getScrollStep() * -double_3, true);
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
            return CollectionUtils.findFirstOrNullEquals(configFiltered, getCurrentEntry()) != null;
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
            QueuedTooltip tooltip = getCurrentTooltip(mouseX, mouseY);
            if (tooltip != null) {
                FilteringEntry.this.tooltip = tooltip;
            }
        }
    }
}
