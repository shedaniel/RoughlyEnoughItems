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

package me.shedaniel.rei.impl.client.gui.widget.entrylist;

import com.google.common.base.Predicates;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import me.shedaniel.clothconfig2.ClothConfigInitializer;
import me.shedaniel.clothconfig2.api.ScissorsHandler;
import me.shedaniel.clothconfig2.api.animator.NumberAnimator;
import me.shedaniel.clothconfig2.api.animator.ValueAnimator;
import me.shedaniel.clothconfig2.api.scroll.ScrollingContainer;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.math.impl.PointHelper;
import me.shedaniel.rei.api.client.ClientHelper;
import me.shedaniel.rei.api.client.REIRuntime;
import me.shedaniel.rei.api.client.config.ConfigManager;
import me.shedaniel.rei.api.client.config.ConfigObject;
import me.shedaniel.rei.api.client.entry.renderer.EntryRenderer;
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
import me.shedaniel.rei.api.client.registry.screen.OverlayDecider;
import me.shedaniel.rei.api.client.registry.screen.ScreenRegistry;
import me.shedaniel.rei.api.client.util.ClientEntryStacks;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.shedaniel.rei.impl.client.ClientHelperImpl;
import me.shedaniel.rei.impl.client.config.ConfigManagerImpl;
import me.shedaniel.rei.impl.client.config.ConfigObjectImpl;
import me.shedaniel.rei.impl.client.gui.ScreenOverlayImpl;
import me.shedaniel.rei.impl.client.gui.widget.BatchedEntryRendererManager;
import me.shedaniel.rei.impl.client.gui.widget.CachedEntryListRender;
import me.shedaniel.rei.impl.client.gui.widget.EntryWidget;
import me.shedaniel.rei.impl.client.gui.widget.favorites.FavoritesListWidget;
import me.shedaniel.rei.impl.client.gui.widget.region.RegionRenderingDebugger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ApiStatus.Internal
public class EntryListWidget extends WidgetWithBounds implements OverlayListWidget, DraggableStackVisitorWidget {
    private static final int SIZE = 18;
    private int page;
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
    private final RegionRenderingDebugger debugger = new RegionRenderingDebugger();
    private Rectangle bounds, innerBounds;
    private List<EntryStack<?>> allStacks = null;
    private List<EntryListStackEntry> entries = Collections.emptyList();
    private List<Widget> renders = Collections.emptyList();
    private List<Widget> children = Collections.emptyList();
    public final NumberAnimator<Double> scaleIndicator = ValueAnimator.ofDouble(0.0D)
            .withConvention(() -> 0.0D, 8000);
    
    public static int entrySize() {
        return Mth.ceil(SIZE * ConfigObject.getInstance().getEntrySize());
    }
    
    public static boolean notSteppingOnExclusionZones(int left, int top, int width, int height) {
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
    
    public boolean containsChecked(double x, double y, boolean inner) {
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
                scaleIndicator.setAs(10.0D);
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
        this.page = page;
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
        
        boolean fastEntryRendering = ConfigObject.getInstance().doesFastEntryRendering();
        if (ConfigObject.getInstance().isEntryListWidgetScrolled()) {
            ScissorsHandler.INSTANCE.scissor(bounds);
            
            int skip = Math.max(0, Mth.floor(scrolling.scrollAmount() / (float) entrySize()));
            int nextIndex = skip * innerBounds.width / entrySize();
            this.blockedCount = 0;
            BatchedEntryRendererManager helper = new BatchedEntryRendererManager();
            
            int i = nextIndex;
            for (int cont = nextIndex; cont < entries.size(); cont++) {
                EntryListStackEntry entry = entries.get(cont);
                Rectangle entryBounds = entry.getBounds();
                
                entryBounds.y = entry.backupY - scrolling.scrollAmountInt();
                if (entryBounds.y > this.bounds.getMaxY()) break;
                if (allStacks.size() <= i) break;
                if (notSteppingOnExclusionZones(entryBounds.x, entryBounds.y, entryBounds.width, entryBounds.height)) {
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
            
            helper.render(debugger.debugTime, debugger.size, debugger.time, matrices, mouseX, mouseY, delta);
            
            scrolling.updatePosition(delta);
            ScissorsHandler.INSTANCE.removeLastScissor();
            if (scrolling.getMaxScroll() > 0) {
                scrolling.renderScrollBar(0, 1, REIRuntime.getInstance().isDarkThemeEnabled() ? 0.8f : 1f);
            }
        } else {
            for (Widget widget : renders) {
                widget.render(matrices, mouseX, mouseY, delta);
            }
            if (ConfigObject.getInstance().doesCacheEntryRendering()) {
                for (EntryListStackEntry entry : entries) {
                    if (entry.our == null) {
                        CachedEntryListRender.Sprite sprite = CachedEntryListRender.get(entry.getCurrentEntry());
                        if (sprite != null) {
                            entry.our = ClientEntryStacks.setRenderer(entry.getCurrentEntry().copy().cast(), stack -> new EntryRenderer<Object>() {
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
                
                BatchedEntryRendererManager.renderSlow(debugger.debugTime, debugger.size, debugger.time, matrices, mouseX, mouseY, delta, entries);
            } else {
                new BatchedEntryRendererManager(entries).render(debugger.debugTime, debugger.size, debugger.time, matrices, mouseX, mouseY, delta);
            }
        }
        
        debugger.render(matrices, bounds.x, bounds.y, delta);
        
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
        
        scaleIndicator.update(delta);
        if (scaleIndicator.value() > 0.04) {
            TextComponent component = new TextComponent(Math.round(ConfigObject.getInstance().getEntrySize() * 100) + "%");
            int width = font.width(component);
            int backgroundColor = ((int) Math.round(0xa0 * Mth.clamp(scaleIndicator.value(), 0.0, 1.0))) << 24;
            int textColor = ((int) Math.round(0xdd * Mth.clamp(scaleIndicator.value(), 0.0, 1.0))) << 24;
            fillGradient(matrices, bounds.getCenterX() - width / 2 - 2, bounds.getCenterY() - 6, bounds.getCenterX() + width / 2 + 2, bounds.getCenterY() + 6, backgroundColor, backgroundColor);
            font.draw(matrices, component, bounds.getCenterX() - width / 2, bounds.getCenterY() - 4, 0xFFFFFF | textColor);
        }
    }
    
    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dx, double dy) {
        if (hasSpace() && scrolling.mouseDragged(mouseX, mouseY, button, dx, dy))
            return true;
        return super.mouseDragged(mouseX, mouseY, button, dx, dy);
    }
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (containsChecked(PointHelper.ofMouse(), false))
            for (Widget widget : children)
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
        boolean zoomed = ConfigObject.getInstance().isFocusModeZoomed();
        this.innerBounds = updateInnerBounds(bounds);
        if (ConfigObject.getInstance().isEntryListWidgetScrolled()) {
            updateScrolledEntries(entrySize, zoomed);
        } else {
            updatePaginatedEntries(entrySize, zoomed);
        }
        FavoritesListWidget favoritesListWidget = ScreenOverlayImpl.getFavoritesListWidget();
        if (favoritesListWidget != null) {
            favoritesListWidget.getSystemRegion().updateEntriesPosition(entry -> true);
            favoritesListWidget.getRegion().updateEntriesPosition(entry -> true);
        }
    }
    
    private void updateScrolledEntries(int entrySize, boolean zoomed) {
        page = 0;
        int width = innerBounds.width / entrySize;
        int pageHeight = innerBounds.height / entrySize;
        int slotsToPrepare = Math.max(allStacks.size() * 3, width * pageHeight * 3);
        int currentX = 0;
        int currentY = 0;
        List<EntryListStackEntry> entries = Lists.newArrayList();
        for (int i = 0; i < slotsToPrepare; i++) {
            int xPos = currentX * entrySize + innerBounds.x;
            int yPos = currentY * entrySize + innerBounds.y;
            entries.add((EntryListStackEntry) new EntryListStackEntry(this, xPos, yPos, entrySize, zoomed).noBackground());
            currentX++;
            if (currentX >= width) {
                currentX = 0;
                currentY++;
            }
        }
        this.entries = entries;
        this.children = Lists.newArrayList(renders);
        this.children.addAll(entries);
    }
    
    private void updatePaginatedEntries(int entrySize, boolean zoomed) {
        this.renders = Lists.newArrayList();
        page = Math.max(page, 0);
        List<EntryListStackEntry> entries = Lists.newArrayList();
        int width = innerBounds.width / entrySize;
        int height = innerBounds.height / entrySize;
        for (int currentY = 0; currentY < height; currentY++) {
            for (int currentX = 0; currentX < width; currentX++) {
                int slotX = currentX * entrySize + innerBounds.x;
                int slotY = currentY * entrySize + innerBounds.y;
                if (notSteppingOnExclusionZones(slotX - 1, slotY - 1, entrySize, entrySize)) {
                    entries.add((EntryListStackEntry) new EntryListStackEntry(this, slotX, slotY, entrySize, zoomed).noBackground());
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
        this.children = Lists.newArrayList(renders);
        this.children.addAll(entries);
    }
    
    @ApiStatus.Internal
    public List<EntryStack<?>> getAllStacks() {
        return allStacks;
    }
    
    public void updateSearch(String searchTerm, boolean ignoreLastSearch) {
        EntryListSearchManager.INSTANCE.update(searchTerm, ignoreLastSearch, stacks -> {
            allStacks = stacks;
            updateEntriesPosition();
        });
        debugger.debugTime = ConfigObject.getInstance().doDebugRenderTimeRequired();
        FavoritesListWidget favorites = ScreenOverlayImpl.getFavoritesListWidget();
        if (favorites != null) {
            favorites.updateSearch();
        }
    }
    
    @Override
    public List<? extends Widget> children() {
        return children;
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
            for (EntryListStackEntry entry : entries) {
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
}
