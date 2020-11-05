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

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import it.unimi.dsi.fastutil.ints.*;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import me.shedaniel.clothconfig2.forge.ClothConfigInitializer;
import me.shedaniel.clothconfig2.forge.api.PointHelper;
import me.shedaniel.clothconfig2.forge.api.ScissorsHandler;
import me.shedaniel.clothconfig2.forge.api.ScrollingContainer;
import me.shedaniel.clothconfig2.forge.gui.widget.DynamicNewSmoothScrollingEntryListWidget;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.RoughlyEnoughItemsCore;
import me.shedaniel.rei.api.ConfigManager;
import me.shedaniel.rei.api.ConfigObject;
import me.shedaniel.rei.api.DisplayHelper;
import me.shedaniel.rei.api.REIHelper;
import me.shedaniel.rei.api.favorites.FavoriteEntry;
import me.shedaniel.rei.api.favorites.FavoriteMenuEntry;
import me.shedaniel.rei.gui.ContainerScreenOverlay;
import me.shedaniel.rei.gui.modules.Menu;
import me.shedaniel.rei.gui.modules.MenuEntry;
import me.shedaniel.rei.impl.Animator;
import me.shedaniel.rei.impl.OptimalEntryStack;
import me.shedaniel.rei.impl.ScreenHelper;
import me.shedaniel.rei.utils.CollectionUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static me.shedaniel.rei.gui.widget.EntryListWidget.*;

@ApiStatus.Internal
public class FavoritesListWidget extends WidgetWithBounds {
    protected final ScrollingContainer scrolling = new ScrollingContainer() {
        @Override
        public Rectangle getBounds() {
            return currentBounds;
        }
        
        @Override
        public int getMaxScrollHeight() {
            return MathHelper.ceil((entries.size() + blockedCount) / (innerBounds.width / (float) entrySize())) * entrySize();
        }
        
        @Override
        public int getScrollBarX() {
            if (!ConfigObject.getInstance().isLeftHandSidePanel())
                return fullBounds.x + 1;
            return fullBounds.getMaxX() - 7;
        }
    };
    protected int blockedCount;
    private Rectangle fullBounds, currentBounds = new Rectangle(), innerBounds;
    private final Int2ObjectMap<Entry> entries = new Int2ObjectLinkedOpenHashMap<>();
    private final Int2ObjectMap<Entry> removedEntries = new Int2ObjectLinkedOpenHashMap<>();
    private List<EntryListEntry> entriesList = Lists.newArrayList();
    private List<Widget> children = Lists.newArrayList();
    private Entry lastTouchedEntry = null;

//    public final AddFavoritePanel favoritePanel = new AddFavoritePanel(this);
//    public final ToggleAddFavoritePanelButton favoritePanelButton = new ToggleAddFavoritePanelButton(this);
    
    private static Rectangle updateInnerBounds(Rectangle bounds) {
        int entrySize = entrySize();
        int width = Math.max(MathHelper.floor((bounds.width - 2 - 6) / (float) entrySize), 1);
        if (!ConfigObject.getInstance().isLeftHandSidePanel())
            return new Rectangle((int) (bounds.getCenterX() - width * (entrySize / 2f) + 3), bounds.y, width * entrySize, bounds.height);
        return new Rectangle((int) (bounds.getCenterX() - width * (entrySize / 2f) - 3), bounds.y, width * entrySize, bounds.height);
    }
    
    @Override
    public boolean mouseScrolled(double double_1, double double_2, double double_3) {
        if (currentBounds.contains(double_1, double_2)) {
//            if (favoritePanel.mouseScrolled(double_1, double_2, double_3)) {
//                return true;
//            }
            scrolling.offset(ClothConfigInitializer.getScrollStep() * -double_3, true);
            return true;
        }
        return super.mouseScrolled(double_1, double_2, double_3);
    }
    
    @NotNull
    @Override
    public Rectangle getBounds() {
        return fullBounds;
    }
    
    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        if (fullBounds.isEmpty() || currentBounds.isEmpty())
            return;
        int entrySize = entrySize();
        boolean fastEntryRendering = ConfigObject.getInstance().doesFastEntryRendering();
        updateEntriesPosition(entry -> true);
        if (lastTouchedEntry != null && lastTouchedEntry.dragged) {
            lastTouchedEntry.x.setAs(mouseX - entrySize / 2);
            lastTouchedEntry.y.setAs(mouseY - entrySize / 2 + (int) scrolling.scrollAmount);
            
            if (!RoughlyEnoughItemsCore.isLeftModePressed)
                resetDraggedEntry();
        }
        for (Entry entry : entries.values()) {
            entry.update(delta);
        }
        if (lastTouchedEntry != null && lastTouchedEntry.madeUp) {
            lastTouchedEntry.update(delta);
        }
        ObjectIterator<Entry> removedEntriesIterator = removedEntries.values().iterator();
        while (removedEntriesIterator.hasNext()) {
            Entry removedEntry = removedEntriesIterator.next();
            removedEntry.update(delta);
            
            if (removedEntry.size.doubleValue() <= 300) {
                removedEntriesIterator.remove();
                this.entriesList.remove(removedEntry.getWidget());
                this.children.remove(removedEntry.getWidget());
            }
        }
        ScissorsHandler.INSTANCE.scissor(currentBounds);
        
        Stream<EntryListEntry> entryStream = this.entriesList.stream()
                .filter(entry -> lastTouchedEntry == null || entry != lastTouchedEntry.getWidget())
                .filter(entry -> entry.getBounds().getMaxY() >= this.currentBounds.getY() && entry.getBounds().y <= this.currentBounds.getMaxY());
        
        if (fastEntryRendering) {
            for (List<EntryListEntry> entries : entryStream.collect(Collectors.groupingBy(entryListEntry -> OptimalEntryStack.groupingHashFrom(entryListEntry.getCurrentEntry()))).values()) {
                renderEntries(true, matrices, mouseX, mouseY, delta, entries);
            }
        } else {
            List<EntryListEntry> widgets = entryStream.collect(Collectors.toList());
            renderEntries(false, matrices, mouseX, mouseY, delta, widgets);
        }
        
        updatePosition(delta);
        scrolling.renderScrollBar(0, 1, REIHelper.getInstance().isDarkThemeEnabled() ? 0.8f : 1f);
        ScissorsHandler.INSTANCE.removeLastScissor();
        
        renderAddFavorite(matrices, mouseX, mouseY, delta);
        
        if (lastTouchedEntry != null) {
            matrices.pushPose();
            matrices.translate(0, 0, 600);
            lastTouchedEntry.widget.render(matrices, mouseX, mouseY, delta);
            matrices.popPose();
        }
    }
    
    private void renderAddFavorite(MatrixStack matrices, int mouseX, int mouseY, float delta) {
//        this.favoritePanel.render(matrices, mouseX, mouseY, delta);
//        this.favoritePanelButton.render(matrices, mouseX, mouseY, delta);
    }
    
    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int int_1, double double_3, double double_4) {
        if (lastTouchedEntry != null && !lastTouchedEntry.dragged) {
            Point startPoint = lastTouchedEntry.startedDraggingPosition;
            double xDistance = Math.abs(startPoint.x - mouseX);
            double yDistance = Math.abs(startPoint.y - mouseY);
            double requiredDistance = entrySize() * .4;
            if (xDistance * xDistance + yDistance * yDistance > requiredDistance * requiredDistance) {
                lastTouchedEntry.dragged = true;
            }
        }
        if (scrolling.handleMouseDrag(mouseX, mouseY, int_1, double_3, double_4, ConfigObject.getInstance().doesSnapToRows(), entrySize()))
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
        this.fullBounds = ScreenHelper.getFavoritesListArea(DisplayHelper.getInstance().getOverlayBounds(ConfigObject.getInstance().getDisplayPanelLocation().mirror(), Minecraft.getInstance().screen));
    }
    
    public void updateSearch(EntryListWidget listWidget, String searchTerm) {
        if (ConfigObject.getInstance().isFavoritesEnabled()) {
            applyNewFavorites(CollectionUtils.map(ConfigObject.getInstance().getFavoriteEntries(), FavoriteEntry::copy));
        } else applyNewFavorites(Collections.emptyList());
    }
    
    public void applyNewFavorites(List<FavoriteEntry> newFavorites) {
        newFavorites = Lists.newArrayList(newFavorites);
        newFavorites.removeIf(FavoriteEntry::isEntryInvalid);
        
        int entrySize = entrySize();
        IntSet newFavoritesHash = new IntOpenHashSet(CollectionUtils.mapToInt(newFavorites, FavoriteEntry::hashIgnoreAmount));
        List<Entry> removedEntries = Lists.newArrayList(this.entries.values());
        removedEntries.removeIf(entry -> newFavoritesHash.contains(entry.hashIgnoreAmount()));
        
        for (Entry removedEntry : removedEntries) {
            removedEntry.remove();
            this.removedEntries.put(removedEntry.hashIgnoreAmount(), removedEntry);
        }
        
        Int2ObjectMap<Entry> prevEntries = new Int2ObjectOpenHashMap<>(entries);
        this.entries.clear();
        
        for (FavoriteEntry favorite : newFavorites) {
            Entry entry = prevEntries.get(favorite.hashIgnoreAmount());
            
            if (entry == null) {
                entry = new Entry(favorite, entrySize);
            }
            
            if (ConfigObject.getInstance().isReducedMotion()) entry.size.setAs(entrySize * 100);
            else entry.size.setTo(entrySize * 100, 300);
            entries.put(entry.hashIgnoreAmount(), entry);
        }
        
        applyNewEntriesList();
        updateEntriesPosition(entry -> prevEntries.containsKey(entry.hashIgnoreAmount()));
    }
    
    public void applyNewEntriesList() {
        this.entriesList = Stream.concat(entries.values().stream().map(Entry::getWidget), removedEntries.values().stream().map(Entry::getWidget)).collect(Collectors.toList());
        this.children = Stream.<Stream<Widget>>of(
                entries.values().stream().map(Entry::getWidget),
                removedEntries.values().stream().map(Entry::getWidget)
//                Stream.of(favoritePanelButton, favoritePanel)
        ).flatMap(Function.identity()).collect(Collectors.toList());
    }
    
    public void updateEntriesPosition(Predicate<Entry> animated) {
        int entrySize = entrySize();
        this.blockedCount = 0;
//        if (favoritePanel.getBounds().height > 20)
//            this.currentBounds.setBounds(this.fullBounds.x, this.fullBounds.y, this.fullBounds.width, this.fullBounds.height - (this.fullBounds.getMaxY() - this.favoritePanel.bounds.y) - 4);
//        else this.currentBounds.setBounds(this.fullBounds);
        this.currentBounds.setBounds(this.fullBounds);
        this.innerBounds = updateInnerBounds(currentBounds);
        int width = innerBounds.width / entrySize;
        int currentX = 0;
        int currentY = 0;
        int releaseIndex = lastTouchedEntry != null && lastTouchedEntry.dragged ? getReleaseIndex() : -2;
        
        int slotIndex = 0;
        for (Entry entry : this.entries.values()) {
            if (entry.dragged) continue;
            while (true) {
                int xPos = currentX * entrySize + innerBounds.x;
                int yPos = currentY * entrySize + innerBounds.y;
                
                currentX++;
                if (currentX >= width) {
                    currentX = 0;
                    currentY++;
                }
                
                if (notSteppingOnExclusionZones(xPos, yPos - (int) scrolling.scrollAmount, entrySize, entrySize, innerBounds)) {
                    if (slotIndex++ == releaseIndex) {
                        continue;
                    }
                    
                    entry.moveTo(animated.test(entry), xPos, yPos);
                    break;
                } else {
                    blockedCount++;
                }
            }
        }
    }
    
    @Override
    public List<? extends Widget> children() {
        return children;
    }
    
    public int getReleaseIndex() {
        if (lastTouchedEntry != null && lastTouchedEntry.dragged && currentBounds.contains(PointHelper.ofMouse())) {
            int entrySize = entrySize();
            int width = innerBounds.width / entrySize;
            int currentX = 0;
            int currentY = 0;
            List<Tuple<Entry, Point>> entriesPoints = Lists.newArrayList();
            for (Entry entry : this.entries.values()) {
                if (entry.dragged) continue;
                while (true) {
                    int xPos = currentX * entrySize + innerBounds.x;
                    int yPos = currentY * entrySize + innerBounds.y;
                    
                    currentX++;
                    if (currentX >= width) {
                        currentX = 0;
                        currentY++;
                    }
                    
                    if (notSteppingOnExclusionZones(xPos, yPos - (int) scrolling.scrollAmount, entrySize, entrySize, innerBounds)) {
                        entriesPoints.add(new Tuple<>(entry, new Point(xPos, yPos)));
                        break;
                    } else {
                        blockedCount++;
                    }
                }
            }
            
            int maxSize = entriesPoints.size();
            if (currentX != 0) {
                int xPos = currentX * entrySize + innerBounds.x;
                int yPos = currentY * entrySize + innerBounds.y;
                
                if (notSteppingOnExclusionZones(xPos, yPos - (int) scrolling.scrollAmount, entrySize, entrySize, innerBounds)) {
                    entriesPoints.add(new Tuple<>(null, new Point(xPos, yPos)));
                }
            }
            
            double x = lastTouchedEntry.x.doubleValue();
            double y = lastTouchedEntry.y.doubleValue();
            
            return MathHelper.clamp(entriesPoints.stream()
                            .filter(value -> {
                                double otherY = value.getB().y;
                                
                                return otherY <= y + entrySize / 2 && otherY + entrySize > y + entrySize / 2;
                            })
                            .min(Comparator.comparingDouble(value -> {
                                double otherX = value.getB().x;
                                double otherY = value.getB().y;
                                
                                return (x - otherX) * (x - otherX) + (y - otherY) * (y - otherY);
                            }))
                            .map(entriesPoints::indexOf)
                            .orElse(maxSize),
                    0, entriesPoints.size());
        }
        
        return entries.size();
    }
    
    public void resetDraggedEntry() {
        if (lastTouchedEntry != null && lastTouchedEntry.dragged) {
            Entry entry = lastTouchedEntry;
            double x = entry.x.doubleValue();
            double y = entry.y.doubleValue();
            entry.startedDraggingPosition = null;
            
            boolean contains = currentBounds.contains(PointHelper.ofMouse());
            if (contains || !entry.madeUp) {
                int newIndex = contains ? getReleaseIndex() : Iterables.indexOf(entries.values(), e -> e == entry);
                entry.dragged = false;
                
                if (entries.size() - 1 <= newIndex) {
                    this.entries.remove(entry.hashIgnoreAmount());
                    this.entries.put(entry.hashIgnoreAmount(), entry);
                } else {
                    Int2ObjectMap<Entry> prevEntries = new Int2ObjectLinkedOpenHashMap<>(entries);
                    this.entries.clear();
                    
                    int index = 0;
                    for (Int2ObjectMap.Entry<Entry> entryEntry : prevEntries.int2ObjectEntrySet()) {
                        if (index == newIndex)
                            this.entries.put(entry.hashIgnoreAmount(), entry);
                        if (entryEntry.getIntKey() != entry.hashIgnoreAmount()) {
                            this.entries.put(entryEntry.getIntKey(), entryEntry.getValue());
                            
                            index++;
                        }
                    }
                }
                
                applyNewEntriesList();
                
                if (ConfigObject.getInstance().isFavoritesEnabled()) {
                    List<FavoriteEntry> favorites = ConfigObject.getInstance().getFavoriteEntries();
                    favorites.clear();
                    for (Entry value : this.entries.values()) {
                        favorites.add(value.entry.copy());
                    }
                    
                    ConfigManager.getInstance().saveConfig();
                }
                
                if (entry.madeUp) {
                    applyNewFavorites(this.entries.values().stream()
                            .map(Entry::getEntry)
                            .collect(Collectors.toList()));
                }
                
                entry.madeUp = false;
            }
        }
        
        lastTouchedEntry = null;
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int int_1) {
        resetDraggedEntry();
        
        if (scrolling.updateDraggingState(mouseX, mouseY, int_1))
            return true;
        if (innerBounds.contains(mouseX, mouseY)) {
            for (Entry entry : entries.values()) {
                if (entry.getWidget().containsMouse(mouseX, mouseY)) {
                    lastTouchedEntry = entry;
                    lastTouchedEntry.startedDraggingPosition = new Point(mouseX, mouseY);
                    break;
                }
            }
        } /*else if (favoritePanel.bounds.contains(mouseX, mouseY)) {
            back:
            for (AddFavoritePanel.Row row : favoritePanel.rows.get()) {
                if (row instanceof AddFavoritePanel.SectionEntriesRow) {
                    for (AddFavoritePanel.SectionEntriesRow.SectionFavoriteWidget widget : ((AddFavoritePanel.SectionEntriesRow) row).widgets) {
                        if (widget.containsMouse(mouseX, mouseY)) {
                            lastTouchedEntry = new Entry(widget.entry.copy(), entrySize());
                            lastTouchedEntry.madeUp = true;
                            lastTouchedEntry.dragged = true;
                            lastTouchedEntry.size.setAs(entrySize() * 100);
                            applyNewFavorites(this.entries.values().stream()
                                    .map(Entry::getEntry)
                                    .map(FavoriteEntry::copy)
                                    .filter(entry -> !entry.equals(widget.entry))
                                    .collect(Collectors.toList()));
                            break back;
                        }
                    }
                }
            }
        }*/
        for (Widget widget : children())
            if (widget.mouseClicked(mouseX, mouseY, int_1))
                return true;
        return lastTouchedEntry != null;
    }
    
    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (lastTouchedEntry != null && lastTouchedEntry.dragged) {
            resetDraggedEntry();
            return true;
        }
        
        lastTouchedEntry = null;
        
        if (containsMouse(mouseX, mouseY)) {
            for (Widget widget : children())
                if (widget.mouseReleased(mouseX, mouseY, button))
                    return true;
        }
        return false;
    }
    
    public class Entry {
        private FavoriteEntry entry;
        private final EntryListEntry widget;
        private boolean hidden;
        private Point startedDraggingPosition;
        private boolean dragged;
        private boolean madeUp;
        private Animator x = new Animator();
        private Animator y = new Animator();
        private Animator size = new Animator();
        
        public Entry(FavoriteEntry entry, int entrySize) {
            this.entry = entry;
            this.widget = (EntryListEntry) new EntryListEntry(this, 0, 0, entrySize, entry).noBackground();
        }
        
        public void remove() {
            if (!hidden) {
                this.hidden = true;
                if (ConfigObject.getInstance().isReducedMotion()) this.size.setAs(0);
                else this.size.setTo(0, 300);
            }
        }
        
        public void update(double delta) {
            this.size.update(delta);
            this.x.update(delta);
            this.y.update(delta);
            this.getWidget().getBounds().width = this.getWidget().getBounds().height = (int) Math.round(this.size.doubleValue() / 100);
            double offsetSize = (entrySize() - this.size.doubleValue() / 100) / 2;
            this.getWidget().getBounds().x = (int) Math.round(x.doubleValue() + offsetSize);
            this.getWidget().getBounds().y = (int) Math.round(y.doubleValue() + offsetSize) - (int) scrolling.scrollAmount;
        }
        
        public EntryListEntry getWidget() {
            return widget;
        }
        
        public boolean isHidden() {
            return hidden;
        }
        
        public int hashIgnoreAmount() {
            return entry.hashIgnoreAmount();
        }
        
        public FavoriteEntry getEntry() {
            return entry;
        }
        
        public void moveTo(boolean animated, int xPos, int yPos) {
            if (animated && !ConfigObject.getInstance().isReducedMotion()) {
                x.setTo(xPos, 200);
                y.setTo(yPos, 200);
            } else {
                x.setAs(xPos);
                y.setAs(yPos);
            }
        }
    }
    
    private class EntryListEntry extends EntryListEntryWidget {
        private final Entry entry;
        private final FavoriteEntry favoriteEntry;
        
        private EntryListEntry(Entry entry, int x, int y, int entrySize, FavoriteEntry favoriteEntry) {
            super(new Point(x, y), entrySize);
            this.entry = entry;
            this.favoriteEntry = favoriteEntry;
            this.clearEntries().entry(this.favoriteEntry.getWidget(false));
        }
        
        @Override
        protected FavoriteEntry asFavoriteEntry() {
            return favoriteEntry.copy();
        }
        
        @Override
        public boolean containsMouse(double mouseX, double mouseY) {
            return super.containsMouse(mouseX, mouseY) && currentBounds.contains(mouseX, mouseY);
        }
        
        @Override
        protected boolean reverseFavoritesAction() {
            return true;
        }
        
        @Override
        public void queueTooltip(MatrixStack matrices, int mouseX, int mouseY, float delta) {
            if (lastTouchedEntry == null || !lastTouchedEntry.dragged)
                super.queueTooltip(matrices, mouseX, mouseY, delta);
        }
        
        @Override
        protected void drawHighlighted(MatrixStack matrices, int mouseX, int mouseY, float delta) {
            if (lastTouchedEntry == null || !lastTouchedEntry.dragged)
                super.drawHighlighted(matrices, mouseX, mouseY, delta);
        }
        
        @Override
        public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
            Optional<ContainerScreenOverlay> overlayOptional = ScreenHelper.getOptionalOverlay();
            Optional<Supplier<Collection<@NotNull FavoriteMenuEntry>>> menuEntries = favoriteEntry.getMenuEntries();
            if (Math.abs(entry.x.doubleValue() - entry.x.target()) < 1 && Math.abs(entry.y.doubleValue() - entry.y.target()) < 1 && overlayOptional.isPresent() && menuEntries.isPresent()) {
                ContainerScreenOverlay overlay = overlayOptional.get();
                UUID uuid = favoriteEntry.getUuid();
                
                boolean isOpened = overlay.isMenuOpened(uuid);
                if (entry.dragged || entry.madeUp) {
                    if (isOpened) {
                        overlay.removeOverlayMenu();
                    }
                } else if (isOpened || !overlay.isAnyMenuOpened()) {
                    boolean inBounds = containsMouse(mouseX, mouseY) || overlay.isMenuInBounds(uuid);
                    if (isOpened != inBounds) {
                        if (inBounds) {
                            Menu menu = new Menu(new Point(getBounds().x, getBounds().getMaxY()),
                                    CollectionUtils.map(menuEntries.get().get(), entry -> new MenuEntry() {
                                        @Override
                                        public List<? extends IGuiEventListener> children() {
                                            return Collections.singletonList(entry);
                                        }
                                        
                                        @Override
                                        public void render(MatrixStack poseStack, int i, int j, float f) {
                                            entry.render(poseStack, i, j, f);
                                        }
                                        
                                        @Override
                                        public int getEntryWidth() {
                                            return entry.getEntryWidth();
                                        }
                                        
                                        @Override
                                        public int getEntryHeight() {
                                            return entry.getEntryHeight();
                                        }
                                        
                                        @Override
                                        public void updateInformation(int xPos, int yPos, boolean selected, boolean containsMouse, boolean rendering, int width) {
                                            entry.closeMenu = overlay::removeOverlayMenu;
                                            entry.updateInformation(xPos, yPos, selected, containsMouse, rendering, width);
                                        }
                                        
                                        @Override
                                        public int getZ() {
                                            return entry.getZ();
                                        }
                                        
                                        @Override
                                        public void setZ(int z) {
                                            entry.setZ(z);
                                        }
                                    }));
                            if (ConfigObject.getInstance().isLeftHandSidePanel())
                                menu.menuStartPoint.x -= menu.getBounds().width - getBounds().width;
                            overlay.openMenu(uuid, menu, this::containsMouse);
                        } else {
                            overlay.removeOverlayMenu();
                        }
                    }
                }
            }
            super.render(matrices, mouseX, mouseY, delta);
        }
        
        @Override
        protected boolean doAction(double mouseX, double mouseY, int button) {
            return favoriteEntry.doAction(button);
        }
    }
    
    /*public static class ToggleAddFavoritePanelButton extends WidgetWithBounds {
        private final FavoritesListWidget widget;
        public boolean wasClicked = false;
        public final Animator alpha = new Animator(0);
        
        public final Rectangle bounds = new Rectangle();
        
        public ToggleAddFavoritePanelButton(FavoritesListWidget widget) {
            this.widget = widget;
        }
        
        @Override
        public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
            float expendProgress = widget.favoritePanel.expendState.floatValue();
            this.bounds.setBounds(updateAddFavoriteButtonArea(widget.fullBounds));
            
            boolean isHoveringAddFavoriteButton = containsMouse(mouseX, mouseY);
            this.alpha.setTo(isHoveringAddFavoriteButton ? 1f : widget.fullBounds.contains(mouseX, mouseY) || expendProgress > .1f ? 0.3f : 0f, 260);
            this.alpha.update(delta);
            int buttonColor = 0xFFFFFF | (Math.round(0x34 * alpha.floatValue()) << 24);
            fillGradient(matrices, bounds.x, bounds.y, bounds.getMaxX(), bounds.getMaxY(), buttonColor, buttonColor);
            if (isVisible()) {
                MultiBufferSource.BufferSource bufferSource = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
                if (expendProgress < .9f) {
                    int textColor = 0xFFFFFF | (Math.round(0xef * alpha.floatValue() * (1 - expendProgress)) << 24);
                    font.drawInBatch("+", bounds.getCenterX() - 2.5f, bounds.getCenterY() - 3, textColor, false, matrices.last().pose(), bufferSource, false, 0, 15728880);
                }
                if (expendProgress > .1f) {
                    int textColor = 0xFFFFFF | (Math.round(0xef * alpha.floatValue() * expendProgress) << 24);
                    font.drawInBatch("-", bounds.getCenterX() - 2.5f, bounds.getCenterY() - 3, textColor, false, matrices.last().pose(), bufferSource, false, 0, 15728880);
                }
                bufferSource.endBatch();
            }
            if (isHoveringAddFavoriteButton) {
                Tooltip.create(new TranslatableComponent("text.rei.add_favorite_widget")).queue();
            }
        }
        
        @NotNull
        @Override
        public Rectangle getBounds() {
            return bounds;
        }
        
        public boolean isVisible() {
            return Math.round(0x12 * alpha.floatValue()) > 0;
        }
        
        private Rectangle updateAddFavoriteButtonArea(Rectangle fullArea) {
            return new Rectangle(fullArea.x + 4, fullArea.getMaxY() - 16 - 4, 16, 16);
        }
        
        protected boolean wasClicked() {
            boolean b = this.wasClicked;
            this.wasClicked = false;
            return b;
        }
        
        @Override
        public List<? extends GuiEventListener> children() {
            return Collections.emptyList();
        }
        
        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (isVisible() && containsMouse(mouseX, mouseY))
                this.wasClicked = true;
            return false;
        }
        
        @Override
        public boolean mouseReleased(double mouseX, double mouseY, int button) {
            if (wasClicked() && isVisible() && containsMouse(mouseX, mouseY)) {
                widget.favoritePanel.expendState.setTo(widget.favoritePanel.expendState.target() == 1 ? 0 : 1, 1500);
                widget.favoritePanel.resetRows();
                return true;
            }
            return false;
        }
    }
    
    public static class AddFavoritePanel extends WidgetWithBounds {
        private final FavoritesListWidget widget;
        public final Animator expendState = new Animator(0);
        private final Rectangle bounds = new Rectangle();
        private final Rectangle scrollBounds = new Rectangle();
        private final LazyResettable<List<Row>> rows = new LazyResettable<>(() -> {
            List<Row> rows = new ArrayList<>();
            for (FavoriteEntryType.Section section : FavoriteEntryType.registry().sections()) {
                rows.add(new SectionRow(section.getText().copy().withStyle(style -> style.withBold(true))));
                rows.add(new SectionEntriesRow(CollectionUtils.map(section.getEntries(), FavoriteEntry::copy)));
            }
            return rows;
        });
        private final ScrollingContainer scroller = new ScrollingContainer() {
            @Override
            public Rectangle getBounds() {
                return scrollBounds;
            }
            
            @Override
            public int getMaxScrollHeight() {
                return rows.get().stream().mapToInt(Row::getRowHeight).sum();
            }
        };
        
        public AddFavoritePanel(FavoritesListWidget widget) {
            this.widget = widget;
        }
        
        public void resetRows() {
            this.rows.reset();
        }
        
        @Override
        public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
            this.bounds.setBounds(updatePanelArea(widget.fullBounds));
            this.scrollBounds.setBounds(bounds.x + 4, bounds.y + 4, bounds.width - 8, bounds.height - 20);
            this.expendState.update(delta);
            int buttonColor = 0xFFFFFF | (Math.round(0x34 * Math.min(expendState.floatValue() * 2, 1)) << 24);
            fillGradient(matrices, bounds.x, bounds.y, bounds.getMaxX(), bounds.getMaxY(), buttonColor, buttonColor);
            scroller.updatePosition(delta);
            
            if (expendState.floatValue() > 0.1f) {
                ScissorsHandler.INSTANCE.scissor(scrollBounds);
                int y = scrollBounds.y - (int) scroller.scrollAmount;
                for (Row row : rows.get()) {
                    row.render(matrices, scrollBounds.x, y, scrollBounds.width, row.getRowHeight(), mouseX, mouseY, delta);
                    y += row.getRowHeight();
                }
                scroller.renderScrollBar(0, 1, 1f);
                ScissorsHandler.INSTANCE.removeLastScissor();
            }
        }
        
        private Rectangle updatePanelArea(Rectangle fullArea) {
            int currentWidth = 16 + Math.round(Math.min(expendState.floatValue(), 1) * (fullArea.getWidth() - 16 - 12));
            int currentHeight = 16 + Math.round(expendState.floatValue() * (fullArea.getHeight() * 0.4f - 16 - 8));
            return new Rectangle(fullArea.x + 4, fullArea.getMaxY() - currentHeight - 4, currentWidth, currentHeight);
        }
        
        @Override
        public boolean mouseScrolled(double d, double e, double f) {
            if (scrollBounds.contains(d, e)) {
                scroller.offset(ClothConfigInitializer.getScrollStep() * -f, true);
                return true;
            }
            return super.mouseScrolled(d, e, f);
        }
        
        @Override
        public List<? extends GuiEventListener> children() {
            return rows.get();
        }
        
        @NotNull
        @Override
        public Rectangle getBounds() {
            return bounds;
        }
        
        private static abstract class Row extends AbstractContainerEventHandler {
            public abstract int getRowHeight();
            
            public abstract void render(PoseStack matrices, int x, int y, int rowWidth, int rowHeight, int mouseX, int mouseY, float delta);
        }
        
        private static class SectionRow extends Row {
            private final Component text;
            
            public SectionRow(Component text) {
                this.text = text;
            }
            
            @Override
            public int getRowHeight() {
                return 11;
            }
            
            @Override
            public void render(PoseStack matrices, int x, int y, int rowWidth, int rowHeight, int mouseX, int mouseY, float delta) {
                Minecraft.getInstance().font.draw(matrices, text, x, y + 1, 0xFFFFFFFF);
            }
            
            @Override
            public List<? extends GuiEventListener> children() {
                return Collections.emptyList();
            }
        }
        
        private class SectionEntriesRow extends Row {
            private final List<FavoriteEntry> entries;
            private final List<SectionFavoriteWidget> widgets;
            private int blockedCount;
            private int lastY;
            
            public SectionEntriesRow(List<FavoriteEntry> entries) {
                this.entries = entries;
                int entrySize = entrySize();
                this.widgets = CollectionUtils.map(this.entries, entry -> new SectionFavoriteWidget(new Point(0, 0), entrySize, entry));
                
                for (SectionFavoriteWidget widget : this.widgets) {
                    widget.size.setTo(entrySize * 100, 300);
                }
                
                this.lastY = scrollBounds.y;
                
                updateEntriesPosition(widget -> false);
            }
            
            @Override
            public int getRowHeight() {
                return Mth.ceil((entries.size() + blockedCount) / (scrollBounds.width / (float) entrySize())) * entrySize();
            }
            
            @Override
            public void render(PoseStack matrices, int x, int y, int rowWidth, int rowHeight, int mouseX, int mouseY, float delta) {
                this.lastY = y;
                int entrySize = entrySize();
                boolean fastEntryRendering = ConfigObject.getInstance().doesFastEntryRendering();
                updateEntriesPosition(entry -> true);
                for (SectionFavoriteWidget widget : widgets) {
                    widget.update(delta);
                    
                    if (widget.getBounds().getMaxY() > lastY && widget.getBounds().getY() <= lastY + rowHeight) {
                        if (widget.getCurrentEntry().isEmpty())
                            continue;
                        widget.render(matrices, mouseX, mouseY, delta);
                    }
                }
            }
            
            @Override
            public List<? extends GuiEventListener> children() {
                return widgets;
            }
            
            private class SectionFavoriteWidget extends EntryListEntryWidget {
                private Animator x = new Animator();
                private Animator y = new Animator();
                private Animator size = new Animator();
                private FavoriteEntry entry;
                
                protected SectionFavoriteWidget(Point point, int entrySize, FavoriteEntry entry) {
                    super(point, entrySize);
                    this.entry = entry;
                    entry(entry.getWidget(true));
                    noBackground();
                }
                
                public void moveTo(boolean animated, int xPos, int yPos) {
                    if (animated) {
                        x.setTo(xPos, 200);
                        y.setTo(yPos, 200);
                    } else {
                        x.setAs(xPos);
                        y.setAs(yPos);
                    }
                }
                
                public void update(float delta) {
                    this.size.update(delta);
                    this.x.update(delta);
                    this.y.update(delta);
                    this.getBounds().width = this.getBounds().height = (int) Math.round(this.size.doubleValue() / 100);
                    double offsetSize = (entrySize() - this.size.doubleValue() / 100) / 2;
                    this.getBounds().x = (int) Math.round(x.doubleValue() + offsetSize);
                    this.getBounds().y = (int) Math.round(y.doubleValue() + offsetSize) - (int) scroller.scrollAmount;
                }
            }
            
            public void updateEntriesPosition(Predicate<SectionFavoriteWidget> animated) {
                int entrySize = entrySize();
                this.blockedCount = 0;
                int width = scrollBounds.width / entrySize;
                int currentX = 0;
                int currentY = 0;
                
                int slotIndex = 0;
                for (SectionFavoriteWidget widget : this.widgets) {
                    while (true) {
                        int xPos = currentX * entrySize + scrollBounds.x;
                        int yPos = currentY * entrySize + lastY + (int) scroller.scrollAmount;
                        
                        currentX++;
                        if (currentX >= width) {
                            currentX = 0;
                            currentY++;
                        }
                        
                        if (notSteppingOnExclusionZones(xPos, yPos, entrySize, entrySize, scrollBounds)) {
                            widget.moveTo(animated.test(widget), xPos, yPos);
                            break;
                        } else {
                            blockedCount++;
                        }
                    }
                }
            }
        }
    }*/
}
