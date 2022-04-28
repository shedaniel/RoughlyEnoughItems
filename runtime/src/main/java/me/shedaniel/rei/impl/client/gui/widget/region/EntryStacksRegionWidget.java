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

package me.shedaniel.rei.impl.client.gui.widget.region;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.ints.*;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import me.shedaniel.clothconfig2.ClothConfigInitializer;
import me.shedaniel.clothconfig2.api.ScissorsHandler;
import me.shedaniel.clothconfig2.api.scroll.ScrollingContainer;
import me.shedaniel.math.FloatingPoint;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.math.impl.PointHelper;
import me.shedaniel.rei.api.client.REIRuntime;
import me.shedaniel.rei.api.client.config.ConfigObject;
import me.shedaniel.rei.api.client.entry.region.RegionEntry;
import me.shedaniel.rei.api.client.gui.drag.*;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.WidgetWithBounds;
import me.shedaniel.rei.api.common.entry.EntrySerializer;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.CollectionUtils;
import me.shedaniel.rei.impl.client.gui.widget.BatchedEntryRendererManager;
import me.shedaniel.rei.impl.client.gui.widget.EntryWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.util.Mth;
import net.minecraft.util.Tuple;
import net.minecraft.util.Unit;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static me.shedaniel.rei.impl.client.gui.widget.entrylist.EntryListWidget.entrySize;

public class EntryStacksRegionWidget<T extends RegionEntry<T>> extends WidgetWithBounds implements DraggableStackProviderWidget, DraggableStackVisitorWidget {
    public final RegionListener<T> listener;
    protected int blockedCount;
    private Rectangle bounds = new Rectangle(), innerBounds;
    public final ScrollingContainer scrolling = new ScrollingContainer() {
        @Override
        public Rectangle getBounds() {
            return EntryStacksRegionWidget.this.getBounds();
        }
        
        @Override
        public int getMaxScrollHeight() {
            if (innerBounds.width == 0) return 0;
            return Mth.ceil((entries.size() + blockedCount) / (innerBounds.width / (float) entrySize())) * entrySize();
        }
        
        @Override
        public int getScrollBarX(int maxX) {
            if (!ConfigObject.getInstance().isLeftHandSidePanel())
                return bounds.x + 1;
            return maxX - 7;
        }
    };
    private final Int2ObjectMap<RealRegionEntry<T>> entries = new Int2ObjectLinkedOpenHashMap<>();
    private final Int2ObjectMap<RealRegionEntry<T>> removedEntries = new Int2ObjectLinkedOpenHashMap<>();
    private List<RegionEntryWidget<T>> entriesList = Lists.newArrayList();
    private List<Widget> children = Lists.newArrayList();
    
    public EntryStacksRegionWidget(RegionListener<T> listener) {
        this.listener = listener;
    }
    
    @Override
    public Rectangle getBounds() {
        return bounds;
    }
    
    @Override
    public void render(PoseStack poses, int mouseX, int mouseY, float delta) {
        if (bounds.isEmpty()) return;
        
        int entrySize = entrySize();
        boolean fastEntryRendering = ConfigObject.getInstance().doesFastEntryRendering();
        updateEntriesPosition(entry -> true);
        for (RealRegionEntry<T> entry : entries.values()) {
            entry.update(delta);
        }
        ObjectIterator<RealRegionEntry<T>> removedEntriesIterator = removedEntries.values().iterator();
        while (removedEntriesIterator.hasNext()) {
            RealRegionEntry<T> removedEntry = removedEntriesIterator.next();
            removedEntry.update(delta);
            
            if (removedEntry.size.doubleValue() <= 300) {
                removedEntriesIterator.remove();
                this.entriesList.remove(removedEntry.getWidget());
                this.children.remove(removedEntry.getWidget());
            }
        }
        
        ScissorsHandler.INSTANCE.scissor(bounds);
        
        Stream<RegionEntryWidget<T>> entryStream = this.entriesList.stream()
                .filter(entry -> entry.getBounds().getMaxY() >= this.bounds.getY() && entry.getBounds().y <= this.bounds.getMaxY());
        
        new BatchedEntryRendererManager(entryStream.collect(Collectors.toList()))
                .render(poses, mouseX, mouseY, delta);
        
        updatePosition(delta);
        scrolling.renderScrollBar(0, 1, REIRuntime.getInstance().isDarkThemeEnabled() ? 0.8f : 1f);
        ScissorsHandler.INSTANCE.removeLastScissor();
    }
    
    @Override
    public List<Widget> children() {
        return children;
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (scrolling.updateDraggingState(mouseX, mouseY, button)) {
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (containsMouse(mouseX, mouseY)) {
            scrolling.offset(ClothConfigInitializer.getScrollStep() * -amount, true);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, amount);
    }
    
    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (scrolling.mouseDragged(mouseX, mouseY, button, deltaX, deltaY))
            return true;
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }
    
    private void updatePosition(float delta) {
        scrolling.updatePosition(delta);
    }
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (containsMouse(PointHelper.ofMouse()))
            for (Widget widget : children())
                if (widget.keyPressed(keyCode, scanCode, modifiers))
                    return true;
        return false;
    }
    
    @Override
    @Nullable
    public DraggableStack getHoveredStack(DraggingContext<Screen> context, double mouseX, double mouseY) {
        if (innerBounds.contains(mouseX, mouseY)) {
            for (RealRegionEntry<T> entry : entries.values()) {
                if (entry.getWidget().containsMouse(mouseX, mouseY) && listener.canBeDragged(entry)) {
                    return new RegionDraggableStack<>(entry, null);
                }
            }
        }
        return null;
    }
    
    public EntryStack<?> getFocusedStack() {
        Point mouse = PointHelper.ofMouse();
        if (innerBounds.contains(mouse)) {
            for (RealRegionEntry<T> entry : entries.values()) {
                if (entry.getWidget().containsMouse(mouse)) {
                    return entry.getWidget().getCurrentEntry().copy();
                }
            }
        }
        return EntryStack.empty();
    }
    
    public Stream<EntryStack<?>> getEntries() {
        return (Stream<EntryStack<?>>) (Stream<? extends EntryStack<?>>) entriesList.stream()
                .filter(entry -> entry.getBounds().getMaxY() >= this.bounds.getY() && entry.getBounds().y <= this.bounds.getMaxY())
                .map(EntryWidget::getCurrentEntry)
                .filter(entry -> !entry.isEmpty());
    }
    
    @Override
    public DraggedAcceptorResult acceptDraggedStack(DraggingContext<Screen> context, DraggableStack stack) {
        return checkDraggedStacks(context, stack)
                .filter(entry -> innerBounds.contains(context.getCurrentPosition()))
                .flatMap(entry -> {
                    if (stack instanceof RegionDraggableStack && ((RegionDraggableStack<?>) stack).getEntry().region == this && ((RegionDraggableStack<?>) stack).getShowcaseWidget() == null) {
                        return Optional.empty();
                    }
                    if (!drop(entry)) {
                        return Optional.empty();
                    }
                    return Optional.of(Unit.INSTANCE);
                }).isPresent() ? DraggedAcceptorResult.CONSUMED : DraggedAcceptorResult.PASS;
    }
    
    public Optional<RealRegionEntry<T>> checkDraggedStacks(DraggingContext<Screen> context, DraggableStack stack) {
        EntrySerializer<?> serializer = stack.getStack().getDefinition().getSerializer();
        if (serializer != null && (stack instanceof RegionDraggableStack || (serializer.supportReading() && serializer.supportSaving()))) {
            try {
                T regionEntry = stack instanceof RegionDraggableStack ? ((RegionDraggableStack<T>) stack).getEntry().getEntry().copy()
                        : listener.convertDraggableStack(context, stack);
                if (regionEntry == null) return Optional.empty();
                RealRegionEntry<T> entry = new RealRegionEntry<>(this, regionEntry, entrySize());
                entry.size.setAs(entrySize() * 100);
                return Optional.of(entry);
            } catch (Throwable ignored) {
            }
        }
        return Optional.empty();
    }
    
    public enum RemovalMode {
        THROW_EXCEPTION,
        DISAPPEAR,
        MIGRATED,
        ;
    }
    
    public void setEntries(List<T> newEntries, RemovalMode removalMode) {
        newEntries = Lists.newArrayList(newEntries);
        newEntries.removeIf(entry -> entry == null || entry.isEntryInvalid());
        
        int entrySize = entrySize();
        IntSet newFavoritesHash = new IntOpenHashSet(CollectionUtils.mapToInt(newEntries, T::hashCode));
        List<RealRegionEntry<T>> removedEntries = Lists.newArrayList(this.entries.values());
        removedEntries.removeIf(entry -> newFavoritesHash.contains(entry.hashIgnoreAmount()));
        
        if (!removedEntries.isEmpty() && removalMode == RemovalMode.THROW_EXCEPTION) {
            throw new IllegalStateException("Cannot remove entries from region " + this + ": " + removedEntries);
        } else if (removalMode == RemovalMode.DISAPPEAR) {
            for (RealRegionEntry<T> removedEntry : removedEntries) {
                removedEntry.remove();
                this.removedEntries.put(removedEntry.hashIgnoreAmount(), removedEntry);
            }
        }
        
        List<RealRegionEntry<T>> addedEntries = new ArrayList<>();
        Int2ObjectMap<RealRegionEntry<T>> prevEntries = new Int2ObjectOpenHashMap<>(entries);
        this.entries.clear();
        
        for (T regionEntry : newEntries) {
            RealRegionEntry<T> realEntry = prevEntries.get(regionEntry.hashCode());
            
            if (realEntry == null) {
                realEntry = new RealRegionEntry<>(this, regionEntry, entrySize);
                addedEntries.add(realEntry);
            }
            
            if (!ConfigObject.getInstance().isFavoritesAnimated()) realEntry.size.setAs(entrySize * 100);
            else realEntry.size.setTo(entrySize * 100, 300);
            entries.put(realEntry.hashIgnoreAmount(), realEntry);
        }
        
        applyNewEntriesList();
        updateEntriesPosition(entry -> prevEntries.containsKey(entry.hashIgnoreAmount()));
        
        for (RealRegionEntry<T> removedEntry : removedEntries) {
            this.listener.onRemove(removedEntry);
        }
        
        for (RealRegionEntry<T> addedEntry : addedEntries) {
            this.listener.onAdd(addedEntry);
        }
        
        this.listener.onSetNewEntries(entriesList);
        this.listener.onSetNewEntries(entriesList.stream()
                .map(RegionEntryWidget::getEntry)
                .map(RealRegionEntry::getEntry));
    }
    
    public boolean isEmpty() {
        return entries.isEmpty();
    }
    
    public void applyNewEntriesList() {
        this.entriesList = Stream.concat(entries.values().stream().map(RealRegionEntry::getWidget), removedEntries.values().stream().map(RealRegionEntry::getWidget)).collect(Collectors.toList());
        this.children = Stream.<Stream<Widget>>of(
                entries.values().stream().map(RealRegionEntry::getWidget),
                removedEntries.values().stream().map(RealRegionEntry::getWidget)
        ).flatMap(Function.identity()).collect(Collectors.toList());
    }
    
    public void updateEntriesPosition(Predicate<RealRegionEntry<T>> animated) {
        int entrySize = entrySize();
        this.blockedCount = 0;
        this.innerBounds = updateInnerBounds(bounds);
        int width = innerBounds.width / entrySize;
        int currentX = 0;
        int currentY = 0;
        int releaseIndex = getReleaseIndex(null);
        
        int slotIndex = 0;
        for (RealRegionEntry<T> entry : this.entries.values()) {
            while (true) {
                int xPos = currentX * entrySize + innerBounds.x;
                int yPos = currentY * entrySize + innerBounds.y;
                
                currentX++;
                if (currentX >= width) {
                    currentX = 0;
                    currentY++;
                }
                
                if (listener.notSteppingOnExclusionZones(xPos, yPos - scrolling.scrollAmountInt(), entrySize, entrySize)) {
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
    
    private int getReleaseIndex(@Nullable Point position) {
        DraggingContext<?> context = DraggingContext.getInstance();
        if (position == null) position = context.getCurrentPosition();
        if (context.isDraggingStack() && bounds.contains(position) && checkDraggedStacks(context.cast(), context.getCurrentStack()).isPresent()) {
            int entrySize = entrySize();
            int width = innerBounds.width / entrySize;
            int currentX = 0;
            int currentY = 0;
            List<Tuple<RealRegionEntry<T>, Point>> entriesPoints = Lists.newArrayList();
            for (RealRegionEntry<T> entry : this.entries.values()) {
                while (true) {
                    int xPos = currentX * entrySize + innerBounds.x;
                    int yPos = currentY * entrySize + innerBounds.y;
                    
                    currentX++;
                    if (currentX >= width) {
                        currentX = 0;
                        currentY++;
                    }
                    
                    if (listener.notSteppingOnExclusionZones(xPos, yPos - scrolling.scrollAmountInt(), entrySize, entrySize)) {
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
                
                if (listener.notSteppingOnExclusionZones(xPos, yPos - scrolling.scrollAmountInt(), entrySize, entrySize)) {
                    entriesPoints.add(new Tuple<>(null, new Point(xPos, yPos)));
                }
            }
            
            double x = position.x - 8;
            double y = position.y + scrolling.scrollAmount() - 8;
            
            return Mth.clamp(entriesPoints.stream()
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
        
        return -2;
    }
    
    private static Rectangle updateInnerBounds(Rectangle bounds) {
        int entrySize = entrySize();
        int width = Math.max(Mth.floor((bounds.width - 2 - 6) / (float) entrySize), 1);
        if (!ConfigObject.getInstance().isLeftHandSidePanel())
            return new Rectangle((int) (bounds.getCenterX() - width * (entrySize / 2f) + 3), bounds.y, width * entrySize, bounds.height);
        return new Rectangle((int) (bounds.getCenterX() - width * (entrySize / 2f) - 3), bounds.y, width * entrySize, bounds.height);
    }
    
    public boolean drop(RealRegionEntry<T> entry) {
        DraggingContext<?> context = DraggingContext.getInstance();
        double x = context.getCurrentPosition().x;
        double y = context.getCurrentPosition().y + scrolling.scrollAmount();
        return drop(entry, x, y);
    }
    
    public boolean drop(RealRegionEntry<T> entry, double x, double y) {
        boolean contains = bounds.contains(x, y);
        int newIndex = contains ? getReleaseIndex(new Point(x, y)) : Math.max(-1, Iterables.indexOf(entries.values(), e -> e == entry));
        return drop(entry, x, y, newIndex < 0 ? entries.size() : newIndex);
    }
    
    public boolean drop(RealRegionEntry<T> entry, double x, double y, int newIndex) {
        if (newIndex < 0) return drop(entry, x, y);
        if (!listener.canAcceptDrop(entry)) {
            return false;
        }
        
        entry.pos.setAs(new FloatingPoint(x - 8, y - 8));
        
        if (entries.size() <= newIndex) {
            RealRegionEntry<T> remove = this.entries.remove(entry.hashIgnoreAmount());
            if (remove != null) {
                remove.remove();
                this.removedEntries.put(remove.hashIgnoreAmount(), remove);
            }
            this.entries.put(entry.hashIgnoreAmount(), entry);
        } else {
            Int2ObjectMap<RealRegionEntry<T>> prevEntries = new Int2ObjectLinkedOpenHashMap<>(entries);
            this.entries.clear();
            
            int index = 0;
            for (Int2ObjectMap.Entry<RealRegionEntry<T>> entryEntry : prevEntries.int2ObjectEntrySet()) {
                if (index == newIndex) {
                    this.entries.put(entry.hashIgnoreAmount(), entry);
                }
                if (entryEntry.getIntKey() != entry.hashIgnoreAmount()) {
                    this.entries.put(entryEntry.getIntKey(), entryEntry.getValue());
                    index++;
                }
            }
        }
        
        applyNewEntriesList();
        
        listener.onDrop(this.entries.values().stream()
                .map(RealRegionEntry::getEntry));
        
        setEntries(this.entries.values().stream()
                .map(RealRegionEntry::getEntry)
                .collect(Collectors.toList()), RemovalMode.THROW_EXCEPTION);
        return true;
    }
    
    public int indexOf(RealRegionEntry<T> entry) {
        return entriesList.indexOf(entry.getWidget());
    }
    
    public void remove(RealRegionEntry<T> entry, RemovalMode mode) {
        RealRegionEntry<T> currentEntry = entries.get(entry.hashIgnoreAmount());
        if (currentEntry != null) {
            List<T> newEntries = CollectionUtils.map(entries.values(), RealRegionEntry::getEntry);
            newEntries.remove(currentEntry.getEntry());
            setEntries(newEntries, mode);
        }
    }
    
    public double getScrollAmount() {
        return scrolling.scrollAmount();
    }
    
    public boolean has(RealRegionEntry<T> entry) {
        return has(entry.getEntry());
    }
    
    public boolean has(T entry) {
        int hash = entry.hashCode();
        return entries.containsKey(hash) && !removedEntries.containsKey(hash);
    }
    
    public Rectangle getInnerBounds() {
        return innerBounds;
    }
}
