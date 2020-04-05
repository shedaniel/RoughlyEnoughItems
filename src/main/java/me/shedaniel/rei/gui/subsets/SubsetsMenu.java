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

package me.shedaniel.rei.gui.subsets;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import me.shedaniel.clothconfig2.ClothConfigInitializer;
import me.shedaniel.clothconfig2.api.ScissorsHandler;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.RoughlyEnoughItemsCore;
import me.shedaniel.rei.api.EntryRegistry;
import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.api.subsets.SubsetsRegistry;
import me.shedaniel.rei.gui.subsets.entries.EntryStackMenuEntry;
import me.shedaniel.rei.gui.subsets.entries.SubMenuEntry;
import me.shedaniel.rei.gui.widget.LateRenderable;
import me.shedaniel.rei.gui.widget.ScrollingContainer;
import me.shedaniel.rei.gui.widget.WidgetWithBounds;
import me.shedaniel.rei.impl.EntryRegistryImpl;
import me.shedaniel.rei.utils.CollectionUtils;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@ApiStatus.Experimental
@ApiStatus.Internal
public class SubsetsMenu extends WidgetWithBounds implements LateRenderable {
    public final Point menuStartPoint;
    private final List<SubsetsMenuEntry> entries = Lists.newArrayList();
    public final ScrollingContainer scrolling = new ScrollingContainer() {
        @Override
        public int getMaxScrollHeight() {
            int i = 0;
            for (SubsetsMenuEntry entry : children()) {
                i += entry.getEntryHeight();
            }
            return i;
        }
        
        @Override
        public Rectangle getBounds() {
            return SubsetsMenu.this.getInnerBounds();
        }
        
        @Override
        public boolean hasScrollBar() {
            return SubsetsMenu.this.hasScrollBar();
        }
    };
    
    public SubsetsMenu(Point menuStartPoint, Collection<SubsetsMenuEntry> entries) {
        this.menuStartPoint = menuStartPoint;
        buildEntries(entries);
    }
    
    public static SubsetsMenu createFromRegistry(Point menuStartPoint) {
        List<EntryStack> stacks = EntryRegistry.getInstance().getStacksList();
        Map<String, Object> entries = Maps.newHashMap();
        {
            // All Entries group
            Map<String, Object> allEntries = getOrCreateSubEntryInMap(entries, "roughlyenoughitems:all_entries");
            for (EntryStack stack : stacks) {
                putEntryInMap(allEntries, stack);
            }
        }
        {
            // Item Groups group
            Map<String, Object> itemGroups = getOrCreateSubEntryInMap(entries, "roughlyenoughitems:item_groups");
            for (Item item : Registry.ITEM) {
                ItemGroup group = item.getGroup();
                if (group == null)
                    continue;
                DefaultedList<ItemStack> list;
                try {
                    list = new EntryRegistryImpl.DefaultedLinkedList<>(Lists.newLinkedList(), null);
                    item.appendStacks(group, list);
                    if (list.isEmpty())
                        list.add(item.getStackForRender());
                    Map<String, Object> groupMenu = getOrCreateSubEntryInMap(itemGroups, "_item_group_" + group.getId());
                    for (ItemStack stack : list) {
                        putEntryInMap(groupMenu, EntryStack.create(stack));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        Set<String> paths = SubsetsRegistry.INSTANCE.getPaths();
        for (String path : paths) {
            Map<String, Object> lastMap = entries;
            String[] pathSegments = path.split("/");
            for (String pathSegment : pathSegments) {
                lastMap = getOrCreateSubEntryInMap(lastMap, pathSegment);
            }
            for (EntryStack entry : SubsetsRegistry.INSTANCE.getPathEntries(path)) {
                EntryStack firstStack = CollectionUtils.findFirstOrNullEqualsEntryIgnoreAmount(stacks, entry);
                if (firstStack != null)
                    putEntryInMap(lastMap, firstStack);
            }
        }
        return new SubsetsMenu(menuStartPoint, buildEntries(entries));
    }
    
    private static Map<String, Object> getOrCreateSubEntryInMap(Map<String, Object> parent, String pathSegment) {
        putEntryInMap(parent, pathSegment);
        return (Map<String, Object>) parent.get(pathSegment);
    }
    
    private static void putEntryInMap(Map<String, Object> parent, String pathSegment) {
        if (!parent.containsKey(pathSegment)) {
            parent.put(pathSegment, Maps.newHashMap());
        }
    }
    
    private static void putEntryInMap(Map<String, Object> parent, EntryStack stack) {
        Set<EntryStack> items = (Set<EntryStack>) parent.get("items");
        if (items == null) {
            items = Sets.newLinkedHashSet();
            parent.put("items", items);
        }
        items.add(stack);
    }
    
    private static List<SubsetsMenuEntry> buildEntries(Map<String, Object> map) {
        List<SubsetsMenuEntry> entries = Lists.newArrayList();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (entry.getKey().equals("items")) {
                Set<EntryStack> items = (Set<EntryStack>) entry.getValue();
                for (EntryStack item : items) {
                    entries.add(new EntryStackMenuEntry(item));
                }
            } else {
                Map<String, Object> entryMap = (Map<String, Object>) entry.getValue();
                if (entry.getKey().startsWith("_item_group_")) {
                    entries.add(new SubMenuEntry(I18n.translate(entry.getKey().replace("_item_group_", "itemGroup.")), buildEntries(entryMap)));
                } else {
                    String translationKey = "subsets.rei." + entry.getKey().replace(':', '.');
                    if (!I18n.hasTranslation(translationKey))
                        RoughlyEnoughItemsCore.LOGGER.warn("Subsets menu " + translationKey + " does not have a translation");
                    entries.add(new SubMenuEntry(I18n.translate(translationKey), buildEntries(entryMap)));
                }
            }
        }
        return entries;
    }
    
    @SuppressWarnings("deprecation")
    private void buildEntries(Collection<SubsetsMenuEntry> entries) {
        this.entries.clear();
        this.entries.addAll(entries);
        this.entries.sort(Comparator.comparing(entry -> entry instanceof SubMenuEntry ? 0 : 1).thenComparing(entry -> entry instanceof SubMenuEntry ? ((SubMenuEntry) entry).text : ""));
        for (SubsetsMenuEntry entry : this.entries) {
            entry.parent = this;
        }
    }
    
    @Override
    public @NotNull Rectangle getBounds() {
        return new Rectangle(menuStartPoint.x, menuStartPoint.y, getMaxEntryWidth() + 2 + (hasScrollBar() ? 6 : 0), getInnerHeight() + 2);
    }
    
    public Rectangle getInnerBounds() {
        return new Rectangle(menuStartPoint.x + 1, menuStartPoint.y + 1, getMaxEntryWidth() + (hasScrollBar() ? 6 : 0), getInnerHeight());
    }
    
    public boolean hasScrollBar() {
        return scrolling.getMaxScrollHeight() > getInnerHeight();
    }
    
    public int getInnerHeight() {
        return Math.min(scrolling.getMaxScrollHeight(), minecraft.currentScreen.height - 20 - menuStartPoint.y);
    }
    
    public int getMaxEntryWidth() {
        int i = 0;
        for (SubsetsMenuEntry entry : children()) {
            if (entry.getEntryWidth() > i)
                i = entry.getEntryWidth();
        }
        return Math.max(10, i);
    }
    
    @Override
    public void render(int mouseX, int mouseY, float delta) {
        Rectangle bounds = getBounds();
        Rectangle innerBounds = getInnerBounds();
        fill(bounds.x, bounds.y, bounds.getMaxX(), bounds.getMaxY(), -6250336);
        fill(innerBounds.x, innerBounds.y, innerBounds.getMaxX(), innerBounds.getMaxY(), -16777216);
        boolean contains = innerBounds.contains(mouseX, mouseY);
        SubsetsMenuEntry focused = getFocused() instanceof SubsetsMenuEntry ? (SubsetsMenuEntry) getFocused() : null;
        int currentY = (int) (innerBounds.y - scrolling.scrollAmount);
        for (SubsetsMenuEntry child : children()) {
            boolean containsMouse = contains && mouseY >= currentY && mouseY < currentY + child.getEntryHeight();
            if (containsMouse) {
                focused = child;
            }
            currentY += child.getEntryHeight();
        }
        currentY = (int) (innerBounds.y - scrolling.scrollAmount);
        ScissorsHandler.INSTANCE.scissor(scrolling.getScissorBounds());
        for (SubsetsMenuEntry child : children()) {
            boolean rendering = currentY + child.getEntryHeight() >= innerBounds.y && currentY <= innerBounds.getMaxY();
            boolean containsMouse = contains && mouseY >= currentY && mouseY < currentY + child.getEntryHeight();
            child.updateInformation(innerBounds.x, currentY, focused == child || containsMouse, containsMouse, rendering, getMaxEntryWidth());
            if (rendering)
                child.render(mouseX, mouseY, delta);
            currentY += child.getEntryHeight();
        }
        ScissorsHandler.INSTANCE.removeLastScissor();
        setFocused(focused);
        scrolling.renderScrollBar();
        scrolling.updatePosition(delta);
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (scrolling.updateDraggingState(mouseX, mouseY, button))
            return true;
        return super.mouseClicked(mouseX, mouseY, button);
    }
    
    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (scrolling.mouseDragged(mouseX, mouseY, button, deltaX, deltaY))
            return true;
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (getInnerBounds().contains(mouseX, mouseY)) {
            scrolling.offset(ClothConfigInitializer.getScrollStep() * -amount, true);
            return true;
        }
        for (SubsetsMenuEntry child : children()) {
            if (child instanceof SubMenuEntry) {
                if (child.mouseScrolled(mouseX, mouseY, amount))
                    return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, amount);
    }
    
    @Override
    public List<SubsetsMenuEntry> children() {
        return entries;
    }
}
