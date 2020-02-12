/*
 * Copyright (c) 2018, 2019, 2020 shedaniel
 * Licensed under the MIT License (the "License").
 */

package me.shedaniel.rei.gui.widget;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import me.shedaniel.clothconfig2.ClothConfigInitializer;
import me.shedaniel.clothconfig2.api.ScissorsHandler;
import me.shedaniel.clothconfig2.gui.widget.DynamicNewSmoothScrollingEntryListWidget;
import me.shedaniel.math.api.Point;
import me.shedaniel.math.api.Rectangle;
import me.shedaniel.math.impl.PointHelper;
import me.shedaniel.rei.api.*;
import me.shedaniel.rei.gui.config.ItemListOrdering;
import me.shedaniel.rei.impl.ScreenHelper;
import me.shedaniel.rei.utils.CollectionUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

import static me.shedaniel.rei.gui.widget.EntryListWidget.*;

@ApiStatus.Internal
public class FavoritesListWidget extends WidgetWithBounds {
    protected double target;
    protected double scroll;
    protected long start;
    protected long duration;
    protected int blockedCount;
    List<EntryStack> favorites = null;
    private Rectangle bounds, innerBounds;
    private List<EntryListEntry> entries = Collections.emptyList();
    private boolean draggingScrollBar = false;
    
    private static Rectangle updateInnerBounds(Rectangle bounds) {
        int width = Math.max(MathHelper.floor((bounds.width - 2 - 6) / (float) entrySize()), 1);
        if (!ConfigObject.getInstance().isLeftHandSidePanel())
            return new Rectangle((int) (bounds.getCenterX() - width * (entrySize() / 2f) + 3), bounds.y, width * entrySize(), bounds.height);
        return new Rectangle((int) (bounds.getCenterX() - width * (entrySize() / 2f) - 3), bounds.y, width * entrySize(), bounds.height);
    }
    
    protected final int getMaxScrollPosition() {
        return MathHelper.ceil((favorites.size() + blockedCount) / (innerBounds.width / (float) entrySize())) * entrySize();
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
        if (bounds.contains(double_1, double_2)) {
            offset(ClothConfigInitializer.getScrollStep() * -double_3, true);
            return true;
        }
        return super.mouseScrolled(double_1, double_2, double_3);
    }
    
    @Override
    public Rectangle getBounds() {
        return bounds;
    }
    
    @Override
    public void render(int mouseX, int mouseY, float delta) {
        if (bounds.isEmpty())
            return;
        for (EntryListEntry entry : entries)
            entry.clearStacks();
        ScissorsHandler.INSTANCE.scissor(bounds);
        int skip = Math.max(0, MathHelper.floor(scroll / (float) entrySize()));
        int nextIndex = skip * innerBounds.width / entrySize();
        int i = nextIndex;
        blockedCount = 0;
        back:
        for (; i < favorites.size(); i++) {
            EntryStack stack = favorites.get(i);
            while (true) {
                EntryListEntry entry = entries.get(nextIndex);
                entry.getBounds().y = (int) (entry.backupY - scroll);
                if (entry.getBounds().y > bounds.getMaxY())
                    break back;
                if (notSteppingOnExclusionZones(entry.getBounds().x, entry.getBounds().y, innerBounds)) {
                    entry.entry(stack);
                    entry.render(mouseX, mouseY, delta);
                    nextIndex++;
                    break;
                } else {
                    blockedCount++;
                    nextIndex++;
                }
            }
        }
        updatePosition(delta);
        ScissorsHandler.INSTANCE.removeLastScissor();
        renderScrollbar();
    }
    
    private int getScrollbarMinX() {
        if (!ConfigObject.getInstance().isLeftHandSidePanel())
            return bounds.x + 1;
        return bounds.getMaxX() - 7;
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
    
    @Override
    public boolean keyPressed(int int_1, int int_2, int int_3) {
        if (containsMouse(PointHelper.fromMouse()))
            for (Widget widget : children())
                if (widget.keyPressed(int_1, int_2, int_3))
                    return true;
        return false;
    }
    
    @SuppressWarnings("rawtypes")
    public void updateFavoritesBounds(DisplayHelper.DisplayBoundsHandler boundsHandler, @Nullable String searchTerm) {
        this.bounds = boundsHandler.getFavoritesListArea(!ConfigObject.getInstance().isLeftHandSidePanel() ? boundsHandler.getLeftBounds(MinecraftClient.getInstance().currentScreen) : boundsHandler.getRightBounds(MinecraftClient.getInstance().currentScreen));
    }
    
    public void updateSearch(EntryListWidget listWidget, String searchTerm) {
        if (ConfigObject.getInstance().isFavoritesEnabled() && ConfigObject.getInstance().doDisplayFavoritesOnTheLeft()) {
            if (ConfigObject.getInstance().doSearchFavorites()) {
                List<EntryStack> list = Lists.newArrayList();
                boolean checkCraftable = ConfigManager.getInstance().isCraftableOnlyEnabled() && !ScreenHelper.inventoryStacks.isEmpty();
                List<EntryStack> workingItems = checkCraftable ? RecipeHelper.getInstance().findCraftableEntriesByItems(CollectionUtils.map(ScreenHelper.inventoryStacks, EntryStack::create)) : null;
                for (EntryStack stack : ConfigManager.getInstance().getFavorites()) {
                    if (listWidget.canLastSearchTermsBeAppliedTo(stack)) {
                        if (checkCraftable && CollectionUtils.findFirstOrNullEquals(workingItems, stack) == null)
                            continue;
                        list.add(stack.copy().setting(EntryStack.Settings.RENDER_COUNTS, EntryStack.Settings.FALSE).setting(EntryStack.Settings.Item.RENDER_ENCHANTMENT_GLINT, RENDER_ENCHANTMENT_GLINT));
                    }
                }
                ItemListOrdering ordering = ConfigObject.getInstance().getItemListOrdering();
                if (ordering == ItemListOrdering.name)
                    list.sort(ENTRY_NAME_COMPARER);
                if (ordering == ItemListOrdering.item_groups)
                    list.sort(ENTRY_GROUP_COMPARER);
                if (!ConfigObject.getInstance().isItemListAscending())
                    Collections.reverse(list);
                favorites = list;
            } else {
                List<EntryStack> list = Lists.newArrayList();
                boolean checkCraftable = ConfigManager.getInstance().isCraftableOnlyEnabled() && !ScreenHelper.inventoryStacks.isEmpty();
                List<EntryStack> workingItems = checkCraftable ? RecipeHelper.getInstance().findCraftableEntriesByItems(CollectionUtils.map(ScreenHelper.inventoryStacks, EntryStack::create)) : null;
                for (EntryStack stack : ConfigManager.getInstance().getFavorites()) {
                    if (checkCraftable && CollectionUtils.findFirstOrNullEquals(workingItems, stack) == null)
                        continue;
                    list.add(stack.copy().setting(EntryStack.Settings.RENDER_COUNTS, EntryStack.Settings.FALSE).setting(EntryStack.Settings.Item.RENDER_ENCHANTMENT_GLINT, RENDER_ENCHANTMENT_GLINT));
                }
                ItemListOrdering ordering = ConfigObject.getInstance().getItemListOrdering();
                if (ordering == ItemListOrdering.name)
                    list.sort(ENTRY_NAME_COMPARER);
                if (ordering == ItemListOrdering.item_groups)
                    list.sort(ENTRY_GROUP_COMPARER);
                if (!ConfigObject.getInstance().isItemListAscending())
                    Collections.reverse(list);
                favorites = list;
            }
        } else
            favorites = Collections.emptyList();
    }
    
    public void updateEntriesPosition() {
        this.innerBounds = updateInnerBounds(bounds);
        int width = innerBounds.width / entrySize();
        int pageHeight = innerBounds.height / entrySize();
        int slotsToPrepare = Math.max(favorites.size() * 3, width * pageHeight * 3);
        int currentX = 0;
        int currentY = 0;
        List<EntryListEntry> entries = Lists.newArrayList();
        for (int i = 0; i < slotsToPrepare; i++) {
            int xPos = currentX * entrySize() + innerBounds.x;
            int yPos = currentY * entrySize() + innerBounds.y;
            entries.add((EntryListEntry) new EntryListEntry(xPos, yPos).noBackground());
            currentX++;
            if (currentX >= width) {
                currentX = 0;
                currentY++;
            }
        }
        this.entries = entries;
    }
    
    @Override
    public List<? extends Widget> children() {
        return entries;
    }
    
    @Override
    public boolean mouseClicked(double double_1, double double_2, int int_1) {
        double height = getMaxScroll();
        int actualHeight = bounds.height;
        if (height > actualHeight && double_2 >= bounds.y && double_2 <= bounds.getMaxY()) {
            double scrollbarPositionMinX = getScrollbarMinX();
            if (double_1 >= scrollbarPositionMinX - 1 & double_1 <= scrollbarPositionMinX + 8) {
                this.draggingScrollBar = true;
                return true;
            }
        }
        this.draggingScrollBar = false;
        
        if (containsMouse(double_1, double_2)) {
            for (Widget widget : children())
                if (widget.mouseClicked(double_1, double_2, int_1))
                    return true;
        }
        return false;
    }
    
    private class EntryListEntry extends EntryWidget {
        private int backupY;
        
        private EntryListEntry(int x, int y) {
            super(new Point(x, y));
            this.backupY = y;
            getBounds().width = getBounds().height = entrySize();
        }
        
        @Override
        public boolean containsMouse(double mouseX, double mouseY) {
            return super.containsMouse(mouseX, mouseY) && bounds.contains(mouseX, mouseY);
        }
        
        @Override
        protected void drawHighlighted(int mouseX, int mouseY, float delta) {
            if (!getCurrentEntry().isEmpty())
                super.drawHighlighted(mouseX, mouseY, delta);
        }
        
        @Override
        protected boolean reverseFavoritesAction() {
            return true;
        }
    }
}
