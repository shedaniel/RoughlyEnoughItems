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

package me.shedaniel.rei.impl.client.gui.widget;

import com.google.common.base.Predicates;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.math.Matrix4f;
import me.shedaniel.clothconfig2.ClothConfigInitializer;
import me.shedaniel.clothconfig2.api.ScissorsHandler;
import me.shedaniel.clothconfig2.api.animator.NumberAnimator;
import me.shedaniel.clothconfig2.api.animator.ValueAnimator;
import me.shedaniel.clothconfig2.api.scroll.ScrollingContainer;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.math.impl.PointHelper;
import me.shedaniel.rei.RoughlyEnoughItemsCore;
import me.shedaniel.rei.api.client.ClientHelper;
import me.shedaniel.rei.api.client.REIRuntime;
import me.shedaniel.rei.api.client.config.ConfigManager;
import me.shedaniel.rei.api.client.config.ConfigObject;
import me.shedaniel.rei.api.client.entry.renderer.EntryRenderer;
import me.shedaniel.rei.api.client.gui.config.EntryPanelOrdering;
import me.shedaniel.rei.api.client.gui.drag.DraggableStack;
import me.shedaniel.rei.api.client.gui.drag.DraggableStackVisitorWidget;
import me.shedaniel.rei.api.client.gui.drag.DraggedAcceptorResult;
import me.shedaniel.rei.api.client.gui.drag.DraggingContext;
import me.shedaniel.rei.api.client.gui.screen.DisplayScreen;
import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.WidgetWithBounds;
import me.shedaniel.rei.api.client.overlay.OverlayListWidget;
import me.shedaniel.rei.api.client.overlay.ScreenOverlay;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.client.registry.screen.OverlayDecider;
import me.shedaniel.rei.api.client.registry.screen.ScreenRegistry;
import me.shedaniel.rei.api.client.registry.transfer.TransferHandler;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes;
import me.shedaniel.rei.api.common.plugins.PluginManager;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.shedaniel.rei.impl.client.ClientHelperImpl;
import me.shedaniel.rei.impl.client.config.ConfigManagerImpl;
import me.shedaniel.rei.impl.client.config.ConfigObjectImpl;
import me.shedaniel.rei.impl.client.gui.ScreenOverlayImpl;
import me.shedaniel.rei.impl.client.search.AsyncSearchManager;
import me.shedaniel.rei.impl.client.view.ViewsImpl;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.mutable.MutableLong;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ApiStatus.Internal
public class EntryListWidget extends WidgetWithBounds implements OverlayListWidget, DraggableStackVisitorWidget {
    static final Comparator<? super EntryStack<?>> ENTRY_NAME_COMPARER = Comparator.comparing(stack -> stack.asFormatStrippedText().getString());
    static final Comparator<? super EntryStack<?>> ENTRY_GROUP_COMPARER = Comparator.comparingInt(stack -> {
        if (stack.getType() == VanillaEntryTypes.ITEM) {
            CreativeModeTab group = ((ItemStack) stack.getValue()).getItem().getItemCategory();
            if (group != null)
                return group.getId();
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
            return Mth.ceil((allStacks.size() + blockedCount) / (innerBounds.width / (float) entrySize())) * entrySize();
        }
    };
    protected int blockedCount;
    private boolean debugTime;
    private double lastAverageDebugTime, averageDebugTime, lastTotalDebugTime, totalDebugTime, totalDebugTimeDelta;
    private Rectangle bounds, innerBounds;
    private List<EntryStack<?>> allStacks = null;
    private List<EntryListEntry> entries = Collections.emptyList();
    private List<Widget> renders = Collections.emptyList();
    private List<Widget> widgets = Collections.emptyList();
    private AsyncSearchManager searchManager = AsyncSearchManager.createDefault();
    
    public static int entrySize() {
        return Mth.ceil(SIZE * ConfigObject.getInstance().getEntrySize());
    }
    
    public static boolean notSteppingOnExclusionZones(int left, int top, int width, int height, Rectangle listArea) {
        Minecraft instance = Minecraft.getInstance();
        for (OverlayDecider decider : ScreenRegistry.getInstance().getDeciders(instance.screen)) {
            InteractionResult fit = canItemSlotWidgetFit(left, top, width, height, decider);
            if (fit != InteractionResult.PASS)
                return fit == InteractionResult.SUCCESS;
        }
        return true;
    }
    
    private static InteractionResult canItemSlotWidgetFit(int left, int top, int width, int height, OverlayDecider decider) {
        InteractionResult fit;
        fit = decider.isInZone(left, top);
        if (fit != InteractionResult.PASS)
            return fit;
        fit = decider.isInZone(left + width, top);
        if (fit != InteractionResult.PASS)
            return fit;
        fit = decider.isInZone(left, top + height);
        if (fit != InteractionResult.PASS)
            return fit;
        fit = decider.isInZone(left + width, top + height);
        return fit;
    }
    
    private boolean containsChecked(Point point, boolean inner) {
        return containsChecked(point.x, point.y, inner);
    }
    
    @Override
    public boolean containsMouse(double mouseX, double mouseY) {
        return hasSpace() && super.containsMouse(mouseX, mouseY);
    }
    
    public boolean innerContainsMouse(double mouseX, double mouseY) {
        return hasSpace() && innerBounds.contains(mouseX, mouseY);
    }
    
    private boolean containsChecked(double x, double y, boolean inner) {
        if (inner) {
            if (!innerContainsMouse(x, y)) return false;
        } else {
            if (!containsMouse(x, y)) return false;
        }
        Minecraft instance = Minecraft.getInstance();
        for (OverlayDecider decider : ScreenRegistry.getInstance().getDeciders(instance.screen)) {
            InteractionResult result = decider.isInZone(x, y);
            if (result != InteractionResult.PASS)
                return result == InteractionResult.SUCCESS;
        }
        return true;
    }
    
    private static Rectangle updateInnerBounds(Rectangle bounds) {
        bounds = bounds.clone();
        int heightReduction = (int) Math.round(bounds.height * (1 - ConfigObject.getInstance().getVerticalEntriesBoundariesPercentage()));
        bounds.y += heightReduction / 2;
        bounds.height -= heightReduction;
        int maxHeight = (int) Math.ceil(entrySize() * ConfigObject.getInstance().getVerticalEntriesBoundariesRows());
        if (bounds.height > maxHeight) {
            bounds.y += (bounds.height - maxHeight) / 2;
            bounds.height = maxHeight;
        }
        int entrySize = entrySize();
        if (ConfigObject.getInstance().isEntryListWidgetScrolled()) {
            int width = Math.max(Mth.floor((bounds.width - 2 - 6) / (float) entrySize), 1);
            if (ConfigObject.getInstance().isLeftHandSidePanel()) {
                return new Rectangle((int) (bounds.getCenterX() - width * (entrySize / 2f) + 3), bounds.y, width * entrySize, bounds.height);
            }
            return new Rectangle((int) (bounds.getCenterX() - width * (entrySize / 2f) - 3), bounds.y, width * entrySize, bounds.height);
        } else {
            int width = Math.max(Mth.floor((bounds.width - 2) / (float) entrySize), 1);
            int height = Math.max(Mth.floor((bounds.height - 2) / (float) entrySize), 1);
            return new Rectangle((int) (bounds.getCenterX() - width * (entrySize / 2f)), (int) (bounds.getCenterY() - height * (entrySize / 2f)), width * entrySize, height * entrySize);
        }
    }
    
    @Override
    public DraggedAcceptorResult acceptDraggedStack(DraggingContext<Screen> context, DraggableStack stack) {
        if (innerBounds.contains(context.getCurrentPosition())) {
            context.renderToVoid(stack);
            return DraggedAcceptorResult.CONSUMED;
        } else {
            return DraggedAcceptorResult.PASS;
        }
    }
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (containsChecked(mouseX, mouseY, false)) {
            if (Screen.hasControlDown()) {
                ConfigObjectImpl config = ConfigManagerImpl.getInstance().getConfig();
                if (config.setEntrySize(config.getEntrySize() + amount * 0.075)) {
                    ConfigManager.getInstance().saveConfig();
                    REIRuntime.getInstance().getOverlay().ifPresent(ScreenOverlay::queueReloadOverlay);
                    return true;
                }
            } else if (ConfigObject.getInstance().isEntryListWidgetScrolled()) {
                scrolling.offset(ClothConfigInitializer.getScrollStep() * -amount, true);
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, amount);
    }
    
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
        return Mth.ceil(allStacks.size() / (float) entries.size());
    }
    
    @Override
    public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
        if (!hasSpace()) return;
        
        MutableInt size = new MutableInt();
        MutableLong time = new MutableLong();
        long totalTimeStart = debugTime ? System.nanoTime() : 0;
        boolean fastEntryRendering = ConfigObject.getInstance().doesFastEntryRendering();
        if (ConfigObject.getInstance().isEntryListWidgetScrolled()) {
            ScissorsHandler.INSTANCE.scissor(bounds);
            
            int skip = Math.max(0, Mth.floor(scrolling.scrollAmount() / (float) entrySize()));
            int nextIndex = skip * innerBounds.width / entrySize();
            this.blockedCount = 0;
            BatchedEntryRendererManager helper = new BatchedEntryRendererManager();
            
            int i = nextIndex;
            for (int cont = nextIndex; cont < entries.size(); cont++) {
                EntryListEntry entry = entries.get(cont);
                Rectangle entryBounds = entry.getBounds();
                
                entryBounds.y = entry.backupY - scrolling.scrollAmountInt();
                if (entryBounds.y > this.bounds.getMaxY()) break;
                if (allStacks.size() <= i) break;
                if (notSteppingOnExclusionZones(entryBounds.x, entryBounds.y, entryBounds.width, entryBounds.height, innerBounds)) {
                    EntryStack<?> stack = allStacks.get(i++);
                    entry.clearStacks();
                    if (!stack.isEmpty()) {
                        entry.entry(stack);
                        helper.add(entry);
                    }
                } else {
                    blockedCount++;
                }
            }
            
            helper.render(debugTime, size, time, matrices, mouseX, mouseY, delta);
            
            updatePosition(delta);
            ScissorsHandler.INSTANCE.removeLastScissor();
            if (scrolling.getMaxScroll() > 0) {
                scrolling.renderScrollBar(0, 1, REIRuntime.getInstance().isDarkThemeEnabled() ? 0.8f : 1f);
            }
        } else {
            for (Widget widget : renders) {
                widget.render(matrices, mouseX, mouseY, delta);
            }
            if (ConfigObject.getInstance().doesCacheEntryRendering()) {
                for (EntryListEntry entry : entries) {
                    if (entry.our == null) {
                        CachedEntryListRender.Sprite sprite = CachedEntryListRender.get(entry.getCurrentEntry());
                        if (sprite != null) {
                            entry.our = entry.getCurrentEntry().copy().setting(EntryStack.Settings.RENDERER, stack -> new EntryRenderer<Object>() {
                                @Override
                                public void render(EntryStack<Object> entry, PoseStack matrices, Rectangle bounds, int mouseX, int mouseY, float delta) {
                                    RenderSystem.setShaderTexture(0, CachedEntryListRender.cachedTextureLocation);
                                    innerBlit(matrices.last().pose(), bounds.x, bounds.getMaxX(), bounds.y, bounds.getMaxY(), getBlitOffset(), sprite.u0, sprite.u1, sprite.v0, sprite.v1);
                                }
                                
                                @Override
                                @Nullable
                                public Tooltip getTooltip(EntryStack<Object> entry, Point mouse) {
                                    return stack.getDefinition().getRenderer().getTooltip(entry.cast(), mouse);
                                }
                            });
                        }
                    }
                }
                
                BatchedEntryRendererManager.renderSlow(debugTime, size, time, matrices, mouseX, mouseY, delta, entries);
            } else {
                new BatchedEntryRendererManager(entries).render(debugTime, size, time, matrices, mouseX, mouseY, delta);
            }
        }
        
        if (debugTime) {
            long totalTime = System.nanoTime() - totalTimeStart;
            averageDebugTime += (time.getValue() / size.doubleValue()) * delta;
            totalDebugTime += totalTime / 1000000d * delta;
            totalDebugTimeDelta += delta;
            if (totalDebugTimeDelta >= 20) {
                lastAverageDebugTime = averageDebugTime / totalDebugTimeDelta;
                lastTotalDebugTime = totalDebugTime / totalDebugTimeDelta;
                averageDebugTime = 0;
                totalDebugTime = 0;
                totalDebugTimeDelta = 0;
            } else if (lastAverageDebugTime == 0) {
                lastAverageDebugTime = time.getValue() / size.doubleValue();
                totalDebugTime = totalTime / 1000000d;
            }
            int z = getZ();
            setZ(500);
            Component debugText = Component.literal(String.format("%d entries, avg. %.0fns, ttl. %.2fms, %s fps", size.getValue(), lastAverageDebugTime, lastTotalDebugTime, minecraft.fpsString.split(" ")[0]));
            int stringWidth = font.width(debugText);
            fillGradient(matrices, Math.min(bounds.x, minecraft.screen.width - stringWidth - 2), bounds.y, bounds.x + stringWidth + 2, bounds.y + font.lineHeight + 2, -16777216, -16777216);
            MultiBufferSource.BufferSource immediate = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
            matrices.pushPose();
            matrices.translate(0.0D, 0.0D, getZ());
            Matrix4f matrix = matrices.last().pose();
            font.drawInBatch(debugText.getVisualOrderText(), Math.min(bounds.x + 2, minecraft.screen.width - stringWidth), bounds.y + 2, -1, false, matrix, immediate, false, 0, 15728880);
            immediate.endBatch();
            setZ(z);
            matrices.popPose();
        }
        
        if (containsChecked(mouseX, mouseY, false) && ClientHelper.getInstance().isCheating() && !(Minecraft.getInstance().screen instanceof DisplayScreen) && !minecraft.player.containerMenu.getCarried().isEmpty() && ClientHelperImpl.getInstance().canDeleteItems()) {
            EntryStack<?> stack = EntryStacks.of(minecraft.player.containerMenu.getCarried().copy());
            if (stack.getType() != VanillaEntryTypes.ITEM) {
                EntryStack<ItemStack> cheatsAs = stack.cheatsAs();
                stack = cheatsAs.isEmpty() ? stack : cheatsAs;
            }
            for (Widget child : children()) {
                if (child.containsMouse(mouseX, mouseY) && child instanceof EntryWidget widget) {
                    if (widget.cancelDeleteItems(stack)) {
                        return;
                    }
                }
            }
            Tooltip.create(Component.translatable("text.rei.delete_items")).queue();
        }
    }
    
    private int getScrollbarMinX() {
        if (ConfigObject.getInstance().isLeftHandSidePanel())
            return bounds.x + 1;
        return bounds.getMaxX() - 7;
    }
    
    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dx, double dy) {
        if (hasSpace() && scrolling.mouseDragged(mouseX, mouseY, button, dx, dy, ConfigObject.getInstance().doesSnapToRows(), entrySize()))
            return true;
        return super.mouseDragged(mouseX, mouseY, button, dx, dy);
    }
    
    private void updatePosition(float delta) {
        // TODO: Snap to rows
//        if (ConfigObject.getInstance().doesSnapToRows() && scrolling.scrollTarget() >= 0 && scrolling.scrollTarget() <= scrolling.getMaxScroll()) {
//            double nearestRow = Math.round(scrolling.scrollTarget() / (double) entrySize()) * (double) entrySize();
//            if (!DynamicNewSmoothScrollingEntryListWidget.Precision.almostEquals(scrolling.scrollTarget(), nearestRow, DynamicNewSmoothScrollingEntryListWidget.Precision.FLOAT_EPSILON))
//                scrolling.scrollTarget += (nearestRow - scrolling.scrollTarget()) * Math.min(delta / 2.0, 1.0);
//            else
//                scrolling.scrollTarget = nearestRow;
//        }
        scrolling.updatePosition(delta);
    }
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (containsChecked(PointHelper.ofMouse(), false))
            for (Widget widget : widgets)
                if (widget.keyPressed(keyCode, scanCode, modifiers))
                    return true;
        return false;
    }
    
    public void updateArea(Rectangle bounds, String searchTerm) {
        this.bounds = REIRuntime.getInstance().calculateEntryListArea(bounds);
        FavoritesListWidget favoritesListWidget = ScreenOverlayImpl.getFavoritesListWidget();
        if (favoritesListWidget != null) {
            favoritesListWidget.updateFavoritesBounds(searchTerm);
        }
        if (allStacks == null || (ConfigObject.getInstance().isFavoritesEnabled() && favoritesListWidget == null)) {
            updateSearch(searchTerm, true);
        } else {
            updateEntriesPosition();
        }
    }
    
    public boolean hasSpace() {
        int entrySize = entrySize();
        int width = innerBounds.width / entrySize;
        int height = innerBounds.height / entrySize;
        return width * height > 0;
    }
    
    public void updateEntriesPosition() {
        int entrySize = entrySize();
        boolean focusModeZoomed = ConfigObject.getInstance().isFocusModeZoomed();
        this.innerBounds = updateInnerBounds(bounds);
        if (!ConfigObject.getInstance().isEntryListWidgetScrolled()) {
            this.renders = Lists.newArrayList();
            page = Math.max(page, 0);
            List<EntryListEntry> entries = Lists.newArrayList();
            int width = innerBounds.width / entrySize;
            int height = innerBounds.height / entrySize;
            for (int currentY = 0; currentY < height; currentY++) {
                for (int currentX = 0; currentX < width; currentX++) {
                    int slotX = currentX * entrySize + innerBounds.x;
                    int slotY = currentY * entrySize + innerBounds.y;
                    if (notSteppingOnExclusionZones(slotX - 1, slotY - 1, entrySize, entrySize, innerBounds)) {
                        entries.add((EntryListEntry) new EntryListEntry(slotX, slotY, entrySize, focusModeZoomed).noBackground());
                    }
                }
            }
            page = Math.max(Math.min(page, getTotalPages() - 1), 0);
            List<EntryStack<?>> subList = allStacks.stream().skip(Math.max(0, page * entries.size())).limit(Math.max(0, entries.size() - Math.max(0, -page * entries.size()))).collect(Collectors.toList());
            for (int i = 0; i < subList.size(); i++) {
                EntryStack<?> stack = subList.get(i);
                entries.get(i + Math.max(0, -page * entries.size())).clearStacks().entry(stack);
            }
            this.entries = entries;
            this.widgets = Lists.newArrayList(renders);
            this.widgets.addAll(entries);
        } else {
            page = 0;
            int width = innerBounds.width / entrySize;
            int pageHeight = innerBounds.height / entrySize;
            int slotsToPrepare = Math.max(allStacks.size() * 3, width * pageHeight * 3);
            int currentX = 0;
            int currentY = 0;
            List<EntryListEntry> entries = Lists.newArrayList();
            for (int i = 0; i < slotsToPrepare; i++) {
                int xPos = currentX * entrySize + innerBounds.x;
                int yPos = currentY * entrySize + innerBounds.y;
                entries.add((EntryListEntry) new EntryListEntry(xPos, yPos, entrySize, focusModeZoomed).noBackground());
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
        FavoritesListWidget favoritesListWidget = ScreenOverlayImpl.getFavoritesListWidget();
        if (favoritesListWidget != null) {
            favoritesListWidget.getSystemRegion().updateEntriesPosition(entry -> true);
            favoritesListWidget.getRegion().updateEntriesPosition(entry -> true);
        }
    }
    
    @ApiStatus.Internal
    public List<EntryStack<?>> getAllStacks() {
        return allStacks;
    }
    
    public void updateSearch(String searchTerm, boolean ignoreLastSearch) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        if (ignoreLastSearch) searchManager.markDirty();
        searchManager.updateFilter(searchTerm);
        if (searchManager.isDirty()) {
            searchManager.getAsync(list -> {
                list = new ArrayList<>(list);
                EntryPanelOrdering ordering = ConfigObject.getInstance().getItemListOrdering();
                if (ordering == EntryPanelOrdering.NAME)
                    list.sort(ENTRY_NAME_COMPARER);
                if (ordering == EntryPanelOrdering.GROUPS)
                    list.sort(ENTRY_GROUP_COMPARER);
                if (!ConfigObject.getInstance().isItemListAscending()) {
                    Collections.reverse(list);
                }
                allStacks = list;
                
                if (ConfigObject.getInstance().doDebugSearchTimeRequired()) {
                    RoughlyEnoughItemsCore.LOGGER.info("Search Used: %s", stopwatch.stop().toString());
                }
                updateEntriesPosition();
            });
        }
        debugTime = ConfigObject.getInstance().doDebugRenderTimeRequired();
        FavoritesListWidget favorites = ScreenOverlayImpl.getFavoritesListWidget();
        if (favorites != null) {
            favorites.updateSearch();
        }
    }
    
    public boolean matches(EntryStack<?> stack) {
        return searchManager.matches(stack);
    }
    
    @Override
    public List<? extends Widget> children() {
        return widgets;
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!hasSpace()) return false;
        if (ConfigObject.getInstance().isEntryListWidgetScrolled()) {
            if (scrolling.updateDraggingState(mouseX, mouseY, button))
                return true;
        }
        for (Widget widget : children())
            if (widget.mouseClicked(mouseX, mouseY, button))
                return true;
        return false;
    }
    
    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (containsChecked(mouseX, mouseY, false)) {
            LocalPlayer player = minecraft.player;
            if (ClientHelper.getInstance().isCheating() && !(Minecraft.getInstance().screen instanceof DisplayScreen) && player != null && player.containerMenu != null && !player.containerMenu.getCarried().isEmpty() && ClientHelperImpl.getInstance().canDeleteItems()) {
                ClientHelper.getInstance().sendDeletePacket();
                return true;
            }
            for (Widget widget : children())
                if (widget.mouseReleased(mouseX, mouseY, button))
                    return true;
        }
        return false;
    }
    
    @Override
    public EntryStack<?> getFocusedStack() {
        Point mouse = PointHelper.ofMouse();
        if (containsChecked(mouse, false)) {
            for (EntryListEntry entry : entries) {
                EntryStack<?> currentEntry = entry.getCurrentEntry();
                if (!currentEntry.isEmpty() && entry.containsMouse(mouse)) {
                    return currentEntry.copy();
                }
            }
        }
        return EntryStack.empty();
    }
    
    @Override
    public Stream<EntryStack<?>> getEntries() {
        if (ConfigObject.getInstance().isEntryListWidgetScrolled()) {
            int skip = Math.max(0, Mth.floor(scrolling.scrollAmount() / (float) entrySize()));
            int nextIndex = skip * innerBounds.width / entrySize();
            return (Stream<EntryStack<?>>) (Stream<? extends EntryStack<?>>) entries.stream()
                    .skip(nextIndex)
                    .filter(entry -> entry.getBounds().y <= this.bounds.getMaxY())
                    .map(EntryWidget::getCurrentEntry)
                    .filter(Predicates.not(EntryStack::isEmpty));
        } else {
            return entries.stream().map(EntryWidget::getCurrentEntry);
        }
    }
    
    private class EntryListEntry extends EntryListEntryWidget {
        private long lastCheckTime = -1;
        private Display display;
        private EntryStack<?> our;
        private NumberAnimator<Double> size = null;
        
        private EntryListEntry(int x, int y, int entrySize, boolean zoomed) {
            super(new Point(x, y), entrySize);
            if (zoomed) {
                noHighlight();
                size = ValueAnimator.ofDouble(1f)
                        .withConvention(() -> {
                            double mouseX = PointHelper.getMouseFloatingX();
                            double mouseY = PointHelper.getMouseFloatingY();
                            int x1 = getBounds().getCenterX() - entrySize / 2;
                            int y1 = getBounds().getCenterY() - entrySize / 2;
                            boolean hovering = mouseX >= x1 && mouseX < x1 + entrySize && mouseY >= y1 && mouseY < y1 + entrySize;
                            return hovering ? 1.5 : 1.0;
                        }, 200);
            }
        }
        
        @Override
        protected void drawExtra(PoseStack matrices, int mouseX, int mouseY, float delta) {
            if (size != null) {
                size.update(delta);
                int centerX = getBounds().getCenterX();
                int centerY = getBounds().getCenterY();
                int entrySize = (int) Math.round(entrySize() * size.value());
                getBounds().setBounds(centerX - entrySize / 2, centerY - entrySize / 2, entrySize, entrySize);
            }
            super.drawExtra(matrices, mouseX, mouseY, delta);
        }
        
        @Override
        public EntryStack<?> getCurrentEntry() {
            if (our != null) {
                if (CachedEntryListRender.cachedTextureLocation != null) {
                    return our;
                }
            }
            
            return super.getCurrentEntry();
        }
        
        @Override
        public boolean containsMouse(double mouseX, double mouseY) {
            return super.containsMouse(mouseX, mouseY) && containsChecked(mouseX, mouseY, true);
        }
        
        public TransferHandler getTransferHandler() {
            if (PluginManager.areAnyReloading()) {
                return null;
            }
            
            if (display != null) {
                if (ViewsImpl.isRecipesFor(getEntries(), display)) {
                    AutoCraftingEvaluator.AutoCraftingResult result = AutoCraftingEvaluator.evaluateAutoCrafting(false, false, display, null);
                    if (result.successful) {
                        return result.successfulHandler;
                    }
                }
                
                display = null;
                lastCheckTime = -1;
            }
            
            if (lastCheckTime != -1 && Util.getMillis() - lastCheckTime < 2000) {
                return null;
            }
            
            return _getTransferHandler();
        }
        
        @Nullable
        private TransferHandler _getTransferHandler() {
            lastCheckTime = Util.getMillis();
            
            for (List<Display> displays : DisplayRegistry.getInstance().getAll().values()) {
                for (Display display : displays) {
                    if (ViewsImpl.isRecipesFor(getEntries(), display)) {
                        AutoCraftingEvaluator.AutoCraftingResult result = AutoCraftingEvaluator.evaluateAutoCrafting(false, false, display, null);
                        if (result.successful) {
                            this.display = display;
                            return result.successfulHandler;
                        }
                    }
                }
            }
            
            return null;
        }
        
        @Override
        @Nullable
        public Tooltip getCurrentTooltip(Point point) {
            Tooltip tooltip = super.getCurrentTooltip(point);
            
            if (tooltip != null && !ClientHelper.getInstance().isCheating() && getTransferHandler() != null) {
                tooltip.add(Component.translatable("text.auto_craft.move_items.tooltip").withStyle(ChatFormatting.YELLOW));
            }
            
            return tooltip;
        }
        
        @Override
        protected boolean doAction(double mouseX, double mouseY, int button) {
            if (!ClientHelper.getInstance().isCheating() && !(Minecraft.getInstance().screen instanceof DisplayScreen) && Screen.hasControlDown()) {
                try {
                    TransferHandler handler = getTransferHandler();
                    
                    if (handler != null) {
                        AbstractContainerScreen<?> containerScreen = REIRuntime.getInstance().getPreviousContainerScreen();
                        TransferHandler.Context context = TransferHandler.Context.create(true, Screen.hasShiftDown() || button == 1, containerScreen, display);
                        TransferHandler.Result transferResult = handler.handle(context);
                        
                        if (transferResult.isBlocking()) {
                            minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                            if (transferResult.isReturningToScreen() && Minecraft.getInstance().screen != containerScreen) {
                                Minecraft.getInstance().setScreen(containerScreen);
                                REIRuntime.getInstance().getOverlay().ifPresent(ScreenOverlay::queueReloadOverlay);
                            }
                            return true;
                        }
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
            return super.doAction(mouseX, mouseY, button);
        }
    }
}
