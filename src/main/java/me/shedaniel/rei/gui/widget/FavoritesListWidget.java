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

package me.shedaniel.rei.gui.widget;

import com.google.common.collect.Lists;
import me.shedaniel.clothconfig2.ClothConfigInitializer;
import me.shedaniel.clothconfig2.api.ScissorsHandler;
import me.shedaniel.clothconfig2.api.ScrollingContainer;
import me.shedaniel.clothconfig2.gui.widget.DynamicNewSmoothScrollingEntryListWidget;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.math.impl.PointHelper;
import me.shedaniel.rei.RoughlyEnoughItemsCore;
import me.shedaniel.rei.api.*;
import me.shedaniel.rei.api.widgets.Tooltip;
import me.shedaniel.rei.gui.config.EntryPanelOrdering;
import me.shedaniel.rei.impl.ScreenHelper;
import me.shedaniel.rei.utils.CollectionUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static me.shedaniel.rei.gui.widget.EntryListWidget.*;

@ApiStatus.Internal
public class FavoritesListWidget extends WidgetWithBounds {
    protected final ScrollingContainer scrolling = new ScrollingContainer() {
        @Override
        public Rectangle getBounds() {
            return bounds;
        }
        
        @Override
        public int getMaxScrollHeight() {
            return MathHelper.ceil((favorites.size() + blockedCount) / (innerBounds.width / (float) entrySize())) * entrySize();
        }
        
        @Override
        public int getScrollBarX() {
            if (!ConfigObject.getInstance().isLeftHandSidePanel())
                return bounds.x + 1;
            return bounds.getMaxX() - 7;
        }
    };
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
    
    @Override
    public boolean mouseScrolled(double double_1, double double_2, double double_3) {
        if (bounds.contains(double_1, double_2)) {
            scrolling.offset(ClothConfigInitializer.getScrollStep() * -double_3, true);
            return true;
        }
        return super.mouseScrolled(double_1, double_2, double_3);
    }
    
    @NotNull
    @Override
    public Rectangle getBounds() {
        return bounds;
    }
    
    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        if (bounds.isEmpty())
            return;
        for (EntryListEntry entry : entries)
            entry.clearStacks();
        ScissorsHandler.INSTANCE.scissor(bounds);
        int skip = Math.max(0, MathHelper.floor(scrolling.scrollAmount / (float) entrySize()));
        int nextIndex = skip * innerBounds.width / entrySize();
        int i = nextIndex;
        blockedCount = 0;
        back:
        for (; i < favorites.size(); i++) {
            EntryStack stack = favorites.get(i);
            while (true) {
                EntryListEntry entry = entries.get(nextIndex);
                entry.getBounds().y = (int) (entry.backupY - scrolling.scrollAmount);
                if (entry.getBounds().y > bounds.getMaxY())
                    break back;
                if (notSteppingOnExclusionZones(entry.getBounds().x, entry.getBounds().y, innerBounds)) {
                    entry.entry(stack);
                    entry.render(matrices, mouseX, mouseY, delta);
                    nextIndex++;
                    break;
                } else {
                    blockedCount++;
                    nextIndex++;
                }
            }
        }
        updatePosition(delta);
        scrolling.renderScrollBar(0, 1, REIHelper.getInstance().isDarkThemeEnabled() ? 0.8f : 1f);
        ScissorsHandler.INSTANCE.removeLastScissor();
        if (containsMouse(mouseX, mouseY) && ClientHelper.getInstance().isCheating() && !minecraft.player.inventory.getCursorStack().isEmpty() && RoughlyEnoughItemsCore.canDeleteItems()) {
            EntryStack stack = EntryStack.create(minecraft.player.inventory.getCursorStack().copy());
            if (stack.getType() == EntryStack.Type.FLUID) {
                Item bucketItem = stack.getFluid().getBucketItem();
                if (bucketItem != null) {
                    stack = EntryStack.create(bucketItem);
                }
            }
            for (Widget child : children()) {
                if (child.containsMouse(mouseX, mouseY) && child instanceof EntryWidget) {
                    if (((EntryWidget) child).cancelDeleteItems(stack)) {
                        return;
                    }
                }
            }
            Tooltip.create(new TranslatableText("text.rei.delete_items")).queue();
        }
    }
    
    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int int_1, double double_3, double double_4) {
        if (scrolling.mouseDragged(mouseX, mouseY, int_1, double_3, double_4, ConfigObject.getInstance().doesSnapToRows(), entrySize()))
            return true;
        return super.mouseDragged(mouseX, mouseY, int_1, double_3, double_4);
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
    
    @Override
    public boolean keyPressed(int int_1, int int_2, int int_3) {
        if (containsMouse(PointHelper.ofMouse()))
            for (Widget widget : children())
                if (widget.keyPressed(int_1, int_2, int_3))
                    return true;
        return false;
    }
    
    public void updateFavoritesBounds(@Nullable String searchTerm) {
        this.bounds = ScreenHelper.getFavoritesListArea(DisplayHelper.getInstance().getOverlayBounds(ConfigObject.getInstance().getDisplayPanelLocation().mirror(), MinecraftClient.getInstance().currentScreen));
    }
    
    public void updateSearch(EntryListWidget listWidget, String searchTerm) {
        if (ConfigObject.getInstance().isFavoritesEnabled()) {
            if (ConfigObject.getInstance().doSearchFavorites()) {
                List<EntryStack> list = Lists.newArrayList();
                boolean checkCraftable = ConfigManager.getInstance().isCraftableOnlyEnabled() && !ScreenHelper.inventoryStacks.isEmpty();
                Set<EntryStack> workingItems = checkCraftable ? new TreeSet<>(Comparator.comparing(EntryStack::hashIgnoreAmount)) : null;
                if (checkCraftable)
                    workingItems.addAll(RecipeHelper.getInstance().findCraftableEntriesByItems(CollectionUtils.map(ScreenHelper.inventoryStacks, EntryStack::create)));
                for (EntryStack stack : ConfigObject.getInstance().getFavorites()) {
                    if (listWidget.canLastSearchTermsBeAppliedTo(stack)) {
                        if (checkCraftable && workingItems.contains(stack))
                            continue;
                        list.add(stack.copy().setting(EntryStack.Settings.RENDER_COUNTS, EntryStack.Settings.FALSE).setting(EntryStack.Settings.Item.RENDER_ENCHANTMENT_GLINT, RENDER_ENCHANTMENT_GLINT));
                    }
                }
                EntryPanelOrdering ordering = ConfigObject.getInstance().getItemListOrdering();
                if (ordering == EntryPanelOrdering.NAME)
                    list.sort(ENTRY_NAME_COMPARER);
                if (ordering == EntryPanelOrdering.GROUPS)
                    list.sort(ENTRY_GROUP_COMPARER);
                if (!ConfigObject.getInstance().isItemListAscending())
                    Collections.reverse(list);
                favorites = list;
            } else {
                List<EntryStack> list = Lists.newArrayList();
                boolean checkCraftable = ConfigManager.getInstance().isCraftableOnlyEnabled() && !ScreenHelper.inventoryStacks.isEmpty();
                Set<EntryStack> workingItems = checkCraftable ? new TreeSet<>(Comparator.comparing(EntryStack::hashIgnoreAmount)) : null;
                if (checkCraftable)
                    workingItems.addAll(RecipeHelper.getInstance().findCraftableEntriesByItems(CollectionUtils.map(ScreenHelper.inventoryStacks, EntryStack::create)));
                for (EntryStack stack : ConfigObject.getInstance().getFavorites()) {
                    if (checkCraftable && workingItems.contains(stack))
                        continue;
                    list.add(stack.copy().setting(EntryStack.Settings.RENDER_COUNTS, EntryStack.Settings.FALSE).setting(EntryStack.Settings.Item.RENDER_ENCHANTMENT_GLINT, RENDER_ENCHANTMENT_GLINT));
                }
                EntryPanelOrdering ordering = ConfigObject.getInstance().getItemListOrdering();
                if (ordering == EntryPanelOrdering.NAME)
                    list.sort(ENTRY_NAME_COMPARER);
                if (ordering == EntryPanelOrdering.GROUPS)
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
        if (scrolling.updateDraggingState(double_1, double_2, int_1))
            return true;
        for (Widget widget : children())
            if (widget.mouseClicked(double_1, double_2, int_1))
                return true;
        return false;
    }
    
    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (containsMouse(mouseX, mouseY)) {
            for (Widget widget : children())
                if (widget.mouseReleased(mouseX, mouseY, button))
                    return true;
            ClientPlayerEntity player = minecraft.player;
            if (ClientHelper.getInstance().isCheating() && player != null && player.inventory != null && !player.inventory.getCursorStack().isEmpty() && RoughlyEnoughItemsCore.canDeleteItems()) {
                ClientHelper.getInstance().sendDeletePacket();
                return true;
            }
            if (player != null && player.inventory != null && !player.inventory.getCursorStack().isEmpty() && RoughlyEnoughItemsCore.hasPermissionToUsePackets())
                return false;
        }
        return false;
    }
    
    private class EntryListEntry extends EntryListEntryWidget {
        private EntryListEntry(int x, int y) {
            super(new Point(x, y));
        }
        
        @Override
        public boolean containsMouse(double mouseX, double mouseY) {
            return super.containsMouse(mouseX, mouseY) && bounds.contains(mouseX, mouseY);
        }
        
        @Override
        protected boolean reverseFavoritesAction() {
            return true;
        }
    }
}
