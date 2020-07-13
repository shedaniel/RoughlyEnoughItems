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
import me.shedaniel.rei.gui.ContainerScreenOverlay;
import me.shedaniel.rei.gui.config.EntryPanelOrdering;
import me.shedaniel.rei.impl.ScreenHelper;
import me.shedaniel.rei.impl.SearchArgument;
import me.shedaniel.rei.utils.CollectionUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Matrix4f;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@ApiStatus.Internal
public class EntryListWidget extends WidgetWithBounds {
    
    static final Supplier<Boolean> RENDER_ENCHANTMENT_GLINT = ConfigObject.getInstance()::doesRenderEntryEnchantmentGlint;
    static final Comparator<? super EntryStack> ENTRY_NAME_COMPARER = Comparator.comparing(stack -> stack.asFormatStrippedText().getString());
    static final Comparator<? super EntryStack> ENTRY_GROUP_COMPARER = Comparator.comparingInt(stack -> {
        if (stack.getType() == EntryStack.Type.ITEM) {
            ItemGroup group = stack.getItem().getGroup();
            if (group != null)
                return group.getIndex();
        }
        return Integer.MAX_VALUE;
    });
    private static final int SIZE = 18;
    private static final boolean LAZY = true;
    private static int page;
    protected final ScrollingContainer scrolling = new ScrollingContainer() {
        @Override
        public Rectangle getBounds() {
            return EntryListWidget.this.getBounds();
        }
        
        @Override
        public int getMaxScrollHeight() {
            return MathHelper.ceil((allStacks.size() + blockedCount) / (innerBounds.width / (float) entrySize())) * entrySize();
        }
    };
    protected int blockedCount;
    private boolean debugTime;
    private Rectangle bounds, innerBounds;
    private List<EntryStack> allStacks = null;
    private List<EntryListEntry> entries = Collections.emptyList();
    private List<Widget> renders = Collections.emptyList();
    private List<Widget> widgets = Collections.emptyList();
    private List<SearchArgument.SearchArguments> lastSearchArguments = Collections.emptyList();
    private String lastSearchTerm = null;
    
    public static int entrySize() {
        return MathHelper.ceil(SIZE * ConfigObject.getInstance().getEntrySize());
    }
    
    static boolean notSteppingOnExclusionZones(int left, int top, Rectangle listArea) {
        MinecraftClient instance = MinecraftClient.getInstance();
        for (OverlayDecider decider : DisplayHelper.getInstance().getSortedOverlayDeciders(instance.currentScreen.getClass())) {
            ActionResult fit = decider.isInZone(left, top);
            if (fit == ActionResult.FAIL)
                return fit == ActionResult.SUCCESS;
            ActionResult fit2 = decider.isInZone(left + 18, top + 18);
            if (fit2 == ActionResult.FAIL)
                return fit == ActionResult.SUCCESS;
            if (fit == ActionResult.SUCCESS && fit2 == ActionResult.SUCCESS)
                return true;
        }
        return true;
    }
    
    private static Rectangle updateInnerBounds(Rectangle bounds) {
        if (ConfigObject.getInstance().isEntryListWidgetScrolled()) {
            int width = Math.max(MathHelper.floor((bounds.width - 2 - 6) / (float) entrySize()), 1);
            if (ConfigObject.getInstance().isLeftHandSidePanel())
                return new Rectangle((int) (bounds.getCenterX() - width * (entrySize() / 2f) + 3), bounds.y, width * entrySize(), bounds.height);
            return new Rectangle((int) (bounds.getCenterX() - width * (entrySize() / 2f) - 3), bounds.y, width * entrySize(), bounds.height);
        }
        int width = Math.max(MathHelper.floor((bounds.width - 2) / (float) entrySize()), 1);
        int height = Math.max(MathHelper.floor((bounds.height - 2) / (float) entrySize()), 1);
        return new Rectangle((int) (bounds.getCenterX() - width * (entrySize() / 2f)), (int) (bounds.getCenterY() - height * (entrySize() / 2f)), width * entrySize(), height * entrySize());
    }
    
    @Override
    public boolean mouseScrolled(double double_1, double double_2, double double_3) {
        if (ConfigObject.getInstance().isEntryListWidgetScrolled() && bounds.contains(double_1, double_2)) {
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
    
    public int getPage() {
        return page;
    }
    
    public void setPage(int page) {
        EntryListWidget.page = page;
    }
    
    public void previousPage() {
        page--;
    }
    
    public void nextPage() {
        page++;
    }
    
    public int getTotalPages() {
        if (ConfigObject.getInstance().isEntryListWidgetScrolled())
            return 1;
        return MathHelper.ceil(allStacks.size() / (float) entries.size());
    }
    
    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        if (ConfigObject.getInstance().isEntryListWidgetScrolled()) {
            for (EntryListEntry entry : entries)
                entry.clearStacks();
            ScissorsHandler.INSTANCE.scissor(bounds);
            int skip = Math.max(0, MathHelper.floor(scrolling.scrollAmount / (float) entrySize()));
            int nextIndex = skip * innerBounds.width / entrySize();
            int i = nextIndex;
            blockedCount = 0;
            if (debugTime) {
                long totalTimeStart = System.nanoTime();
                int size = 0;
                long time = 0;
                back:
                for (; i < allStacks.size(); i++) {
                    EntryStack stack = allStacks.get(i);
                    while (true) {
                        EntryListEntry entry = entries.get(nextIndex);
                        entry.getBounds().y = (int) (entry.backupY - scrolling.scrollAmount);
                        if (entry.getBounds().y > bounds.getMaxY())
                            break back;
                        if (notSteppingOnExclusionZones(entry.getBounds().x, entry.getBounds().y, innerBounds)) {
                            entry.entry(stack);
                            if (!entry.getCurrentEntry().isEmpty()) {
                                size++;
                                long l = System.nanoTime();
                                entry.render(matrices, mouseX, mouseY, delta);
                                time += (System.nanoTime() - l);
                            }
                            nextIndex++;
                            break;
                        } else {
                            blockedCount++;
                            nextIndex++;
                        }
                    }
                }
                long totalTime = System.nanoTime() - totalTimeStart;
                int z = getZ();
                setZ(500);
                Text debugText = new LiteralText(String.format("%d entries, avg. %.0fns, ttl. %.0fms, %s fps", size, time / (double) size, totalTime / 1000000d, minecraft.fpsDebugString.split(" ")[0]));
                fillGradient(matrices, bounds.x, bounds.y, bounds.x + font.getWidth(debugText) + 2, bounds.y + font.fontHeight + 2, -16777216, -16777216);
                VertexConsumerProvider.Immediate immediate = VertexConsumerProvider.immediate(Tessellator.getInstance().getBuffer());
                matrices.push();
                matrices.translate(0.0D, 0.0D, getZ());
                Matrix4f matrix = matrices.peek().getModel();
                font.draw(debugText, bounds.x + 2, bounds.y + 2, -1, false, matrix, immediate, false, 0, 15728880);
                immediate.draw();
                setZ(z);
                matrices.pop();
            } else {
                back:
                for (; i < allStacks.size(); i++) {
                    EntryStack stack = allStacks.get(i);
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
            }
            updatePosition(delta);
            ScissorsHandler.INSTANCE.removeLastScissor();
            scrolling.renderScrollBar(0, 1, REIHelper.getInstance().isDarkThemeEnabled() ? 0.8f : 1f);
        } else {
            if (debugTime) {
                int size = 0;
                long time = 0;
                for (Widget widget : renders) {
                    widget.render(matrices, mouseX, mouseY, delta);
                }
                long totalTimeStart = System.nanoTime();
                if (ConfigObject.getInstance().doesFastEntryRendering()) {
                    for (Map.Entry<? extends Class<? extends EntryStack>, List<EntryListEntry>> entry : entries.stream().collect(Collectors.groupingBy(entryListEntry -> entryListEntry.getCurrentEntry().getClass())).entrySet()) {
                        List<EntryListEntry> list = entry.getValue();
                        if (list.isEmpty())
                            continue;
                        EntryListEntry firstWidget = list.get(0);
                        EntryStack first = firstWidget.getCurrentEntry();
                        if (first instanceof OptimalEntryStack) {
                            OptimalEntryStack firstStack = (OptimalEntryStack) first;
                            firstStack.optimisedRenderStart(matrices, delta);
                            long l = System.nanoTime();
                            for (EntryListEntry listEntry : list) {
                                EntryStack currentEntry = listEntry.getCurrentEntry();
                                currentEntry.setZ(100);
                                listEntry.drawBackground(matrices, mouseX, mouseY, delta);
                                ((OptimalEntryStack) currentEntry).optimisedRenderBase(matrices, listEntry.getInnerBounds(), mouseX, mouseY, delta);
                                if (!currentEntry.isEmpty())
                                    size++;
                            }
                            for (EntryListEntry listEntry : list) {
                                EntryStack currentEntry = listEntry.getCurrentEntry();
                                ((OptimalEntryStack) currentEntry).optimisedRenderOverlay(matrices, listEntry.getInnerBounds(), mouseX, mouseY, delta);
                                if (listEntry.containsMouse(mouseX, mouseY)) {
                                    listEntry.queueTooltip(matrices, mouseX, mouseY, delta);
                                    listEntry.drawHighlighted(matrices, mouseX, mouseY, delta);
                                }
                            }
                            time += (System.nanoTime() - l);
                            firstStack.optimisedRenderEnd(matrices, delta);
                        } else {
                            for (EntryListEntry listEntry : list) {
                                if (listEntry.getCurrentEntry().isEmpty())
                                    continue;
                                size++;
                                long l = System.nanoTime();
                                listEntry.render(matrices, mouseX, mouseY, delta);
                                time += (System.nanoTime() - l);
                            }
                        }
                    }
                } else {
                    for (EntryListEntry entry : entries) {
                        if (entry.getCurrentEntry().isEmpty())
                            continue;
                        size++;
                        long l = System.nanoTime();
                        entry.render(matrices, mouseX, mouseY, delta);
                        time += (System.nanoTime() - l);
                    }
                }
                long totalTime = System.nanoTime() - totalTimeStart;
                int z = getZ();
                setZ(500);
                Text debugText = new LiteralText(String.format("%d entries, avg. %.0fns, ttl. %.0fms, %s fps", size, time / (double) size, totalTime / 1000000d, minecraft.fpsDebugString.split(" ")[0]));
                int stringWidth = font.getWidth(debugText);
                fillGradient(matrices, Math.min(bounds.x, minecraft.currentScreen.width - stringWidth - 2), bounds.y, bounds.x + stringWidth + 2, bounds.y + font.fontHeight + 2, -16777216, -16777216);
                VertexConsumerProvider.Immediate immediate = VertexConsumerProvider.immediate(Tessellator.getInstance().getBuffer());
                matrices.push();
                matrices.translate(0.0D, 0.0D, getZ());
                Matrix4f matrix = matrices.peek().getModel();
                font.draw(debugText, Math.min(bounds.x + 2, minecraft.currentScreen.width - stringWidth), bounds.y + 2, -1, false, matrix, immediate, false, 0, 15728880);
                immediate.draw();
                setZ(z);
                matrices.pop();
            } else {
                for (Widget widget : renders) {
                    widget.render(matrices, mouseX, mouseY, delta);
                }
                if (ConfigObject.getInstance().doesFastEntryRendering()) {
                    for (Map.Entry<? extends Class<? extends EntryStack>, List<EntryListEntry>> entry : entries.stream().collect(Collectors.groupingBy(entryListEntry -> entryListEntry.getCurrentEntry().getClass())).entrySet()) {
                        List<EntryListEntry> list = entry.getValue();
                        if (list.isEmpty())
                            continue;
                        EntryListEntry firstWidget = list.get(0);
                        EntryStack first = firstWidget.getCurrentEntry();
                        if (first instanceof OptimalEntryStack) {
                            OptimalEntryStack firstStack = (OptimalEntryStack) first;
                            firstStack.optimisedRenderStart(matrices, delta);
                            for (EntryListEntry listEntry : list) {
                                EntryStack currentEntry = listEntry.getCurrentEntry();
                                currentEntry.setZ(100);
                                listEntry.drawBackground(matrices, mouseX, mouseY, delta);
                                ((OptimalEntryStack) currentEntry).optimisedRenderBase(matrices, listEntry.getInnerBounds(), mouseX, mouseY, delta);
                            }
                            for (EntryListEntry listEntry : list) {
                                EntryStack currentEntry = listEntry.getCurrentEntry();
                                ((OptimalEntryStack) currentEntry).optimisedRenderOverlay(matrices, listEntry.getInnerBounds(), mouseX, mouseY, delta);
                                if (listEntry.containsMouse(mouseX, mouseY)) {
                                    listEntry.queueTooltip(matrices, mouseX, mouseY, delta);
                                    listEntry.drawHighlighted(matrices, mouseX, mouseY, delta);
                                }
                            }
                            firstStack.optimisedRenderEnd(matrices, delta);
                        } else {
                            for (EntryListEntry listEntry : list) {
                                if (listEntry.getCurrentEntry().isEmpty())
                                    continue;
                                listEntry.render(matrices, mouseX, mouseY, delta);
                            }
                        }
                    }
                } else {
                    for (EntryListEntry listEntry : entries) {
                        if (listEntry.getCurrentEntry().isEmpty())
                            continue;
                        listEntry.render(matrices, mouseX, mouseY, delta);
                    }
                }
            }
        }
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
    
    private int getScrollbarMinX() {
        if (ConfigObject.getInstance().isLeftHandSidePanel())
            return bounds.x + 1;
        return bounds.getMaxX() - 7;
    }
    
    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dx, double dy) {
        if (scrolling.mouseDragged(mouseX, mouseY, button, dx, dy, ConfigObject.getInstance().doesSnapToRows(), entrySize()))
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
    
    @Override
    public boolean keyPressed(int int_1, int int_2, int int_3) {
        if (containsMouse(PointHelper.ofMouse()))
            for (Widget widget : widgets)
                if (widget.keyPressed(int_1, int_2, int_3))
                    return true;
        return false;
    }
    
    public void updateArea(@Nullable String searchTerm) {
        this.bounds = ScreenHelper.getItemListArea(ScreenHelper.getLastOverlay().getBounds());
        FavoritesListWidget favoritesListWidget = ContainerScreenOverlay.getFavoritesListWidget();
        if (favoritesListWidget != null)
            favoritesListWidget.updateFavoritesBounds(searchTerm);
        if (searchTerm != null)
            updateSearch(searchTerm, true);
        else if (allStacks == null || (favoritesListWidget != null && favoritesListWidget.favorites == null))
            updateSearch("", true);
        else
            updateEntriesPosition();
    }
    
    public void updateEntriesPosition() {
        this.innerBounds = updateInnerBounds(bounds);
        if (!ConfigObject.getInstance().isEntryListWidgetScrolled()) {
            this.renders = Lists.newArrayList();
            page = Math.max(page, 0);
            List<EntryListEntry> entries = Lists.newArrayList();
            int width = innerBounds.width / entrySize();
            int height = innerBounds.height / entrySize();
            for (int currentY = 0; currentY < height; currentY++) {
                for (int currentX = 0; currentX < width; currentX++) {
                    if (notSteppingOnExclusionZones(currentX * entrySize() + innerBounds.x, currentY * entrySize() + innerBounds.y, innerBounds)) {
                        entries.add((EntryListEntry) new EntryListEntry(currentX * entrySize() + innerBounds.x, currentY * entrySize() + innerBounds.y).noBackground());
                    }
                }
            }
            page = Math.max(Math.min(page, getTotalPages() - 1), 0);
            List<EntryStack> subList = allStacks.stream().skip(Math.max(0, page * entries.size())).limit(Math.max(0, entries.size() - Math.max(0, -page * entries.size()))).collect(Collectors.toList());
            for (int i = 0; i < subList.size(); i++) {
                EntryStack stack = subList.get(i);
                entries.get(i + Math.max(0, -page * entries.size())).clearStacks().entry(stack);
            }
            this.entries = entries;
            this.widgets = Lists.newArrayList(renders);
            this.widgets.addAll(entries);
        } else {
            page = 0;
            int width = innerBounds.width / entrySize();
            int pageHeight = innerBounds.height / entrySize();
            int slotsToPrepare = Math.max(allStacks.size() * 3, width * pageHeight * 3);
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
            this.widgets = Lists.newArrayList(renders);
            this.widgets.addAll(entries);
        }
        FavoritesListWidget favoritesListWidget = ContainerScreenOverlay.getFavoritesListWidget();
        if (favoritesListWidget != null)
            favoritesListWidget.updateEntriesPosition();
    }
    
    @ApiStatus.Internal
    public List<EntryStack> getAllStacks() {
        return allStacks;
    }
    
    public void updateSearch(String searchTerm) {
        updateSearch(searchTerm, true);
    }
    
    public void updateSearch(String searchTerm, boolean ignoreLastSearch) {
        long started = System.nanoTime();
        if (ignoreLastSearch || this.lastSearchTerm == null || !this.lastSearchTerm.equals(searchTerm)) {
            this.lastSearchTerm = searchTerm;
            this.lastSearchArguments = SearchArgument.processSearchTerm(searchTerm);
            List<EntryStack> list = Lists.newArrayList();
            boolean checkCraftable = ConfigManager.getInstance().isCraftableOnlyEnabled() && !ScreenHelper.inventoryStacks.isEmpty();
            Set<EntryStack> workingItems = checkCraftable ? new TreeSet<>(Comparator.comparing(EntryStack::hashIgnoreAmount)) : null;
            if (checkCraftable)
                workingItems.addAll(RecipeHelper.getInstance().findCraftableEntriesByItems(CollectionUtils.map(ScreenHelper.inventoryStacks, EntryStack::create)));
            List<EntryStack> stacks = EntryRegistry.getInstance().getPreFilteredList();
            if (stacks instanceof CopyOnWriteArrayList && !stacks.isEmpty()) {
                if (ConfigObject.getInstance().shouldAsyncSearch()) {
                    int size = ConfigObject.getInstance().getNumberAsyncSearch();
                    List<CompletableFuture<List<EntryStack>>> completableFutures = Lists.newArrayList();
                    for (int i = 0; i < stacks.size(); i += size) {
                        int[] start = {i};
                        completableFutures.add(CompletableFuture.supplyAsync(() -> {
                            int end = Math.min(stacks.size(), start[0] + size);
                            List<EntryStack> filtered = Lists.newArrayList();
                            for (; start[0] < end; start[0]++) {
                                EntryStack stack = stacks.get(start[0]);
                                if (canLastSearchTermsBeAppliedTo(stack)) {
                                    if (workingItems != null && workingItems.contains(stack))
                                        continue;
                                    filtered.add(stack.copy().setting(EntryStack.Settings.RENDER_COUNTS, EntryStack.Settings.FALSE).setting(EntryStack.Settings.Item.RENDER_ENCHANTMENT_GLINT, RENDER_ENCHANTMENT_GLINT));
                                }
                            }
                            return filtered;
                        }));
                    }
                    try {
                        CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[0])).get(10, TimeUnit.SECONDS);
                    } catch (InterruptedException | ExecutionException | TimeoutException e) {
                        e.printStackTrace();
                    }
                    for (CompletableFuture<List<EntryStack>> future : completableFutures) {
                        List<EntryStack> now = future.getNow(null);
                        if (now != null)
                            list.addAll(now);
                    }
                } else {
                    for (EntryStack stack : stacks) {
                        if (canLastSearchTermsBeAppliedTo(stack)) {
                            if (workingItems != null && workingItems.contains(stack))
                                continue;
                            list.add(stack.copy().setting(EntryStack.Settings.RENDER_COUNTS, EntryStack.Settings.FALSE).setting(EntryStack.Settings.Item.RENDER_ENCHANTMENT_GLINT, RENDER_ENCHANTMENT_GLINT));
                        }
                    }
                }
            }
            EntryPanelOrdering ordering = ConfigObject.getInstance().getItemListOrdering();
            if (ordering == EntryPanelOrdering.NAME)
                list.sort(ENTRY_NAME_COMPARER);
            if (ordering == EntryPanelOrdering.GROUPS)
                list.sort(ENTRY_GROUP_COMPARER);
            if (!ConfigObject.getInstance().isItemListAscending())
                Collections.reverse(list);
            allStacks = list;
        }
        debugTime = ConfigObject.getInstance().doDebugRenderTimeRequired();
        FavoritesListWidget favoritesListWidget = ContainerScreenOverlay.getFavoritesListWidget();
        if (favoritesListWidget != null)
            favoritesListWidget.updateSearch(this, searchTerm);
        long ended = System.nanoTime();
        long time = ended - started;
        if (ConfigObject.getInstance().doDebugSearchTimeRequired())
            RoughlyEnoughItemsCore.LOGGER.info("Search Used: %.2fms", time * 1e-6);
        updateEntriesPosition();
    }
    
    public boolean canLastSearchTermsBeAppliedTo(EntryStack stack) {
        return lastSearchArguments.isEmpty() || SearchArgument.canSearchTermsBeAppliedTo(stack, lastSearchArguments);
    }
    
    @Override
    public List<? extends Widget> children() {
        return widgets;
    }
    
    @Override
    public boolean mouseClicked(double double_1, double double_2, int int_1) {
        if (ConfigObject.getInstance().isEntryListWidgetScrolled()) {
            if (scrolling.updateDraggingState(double_1, double_2, int_1))
                return true;
        }
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
    }
}
