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

package me.shedaniel.rei.impl.client.config.entries;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import me.shedaniel.clothconfig2.ClothConfigInitializer;
import me.shedaniel.clothconfig2.api.ScissorsHandler;
import me.shedaniel.clothconfig2.api.scroll.ScrollingContainer;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.math.impl.PointHelper;
import me.shedaniel.rei.api.client.REIRuntime;
import me.shedaniel.rei.api.client.config.ConfigObject;
import me.shedaniel.rei.api.client.gui.widgets.*;
import me.shedaniel.rei.api.client.registry.entry.EntryRegistry;
import me.shedaniel.rei.api.client.search.SearchFilter;
import me.shedaniel.rei.api.client.search.SearchProvider;
import me.shedaniel.rei.api.common.entry.EntrySerializer;
import me.shedaniel.rei.api.common.entry.EntryStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

@ApiStatus.Internal
public class FilteringScreen extends Screen {
    protected List<EntryStack<?>> selected = Lists.newArrayList();
    protected final ScrollingContainer scrolling = new ScrollingContainer() {
        @Override
        public int getMaxScrollHeight() {
            return Mth.ceil(entryStacks.size() / (innerBounds.width / 18.0F)) * 18 + 28;
        }
        
        @Override
        public Rectangle getBounds() {
            return FilteringScreen.this.getBounds();
        }
        
        @Override
        public int getScrollBarX(int maxX) {
            return width - 7;
        }
    };
    
    Screen parent;
    private FilteringEntry filteringEntry;
    private Tooltip tooltip = null;
    private List<EntryStack<?>> entryStacks = null;
    private Rectangle innerBounds;
    private List<FilteringListEntry> entries = Collections.emptyList();
    private List<GuiEventListener> elements = Collections.emptyList();
    
    private record PointPair(Point firstPoint, @Nullable Point secondPoint) {}
    
    private List<PointPair> points = new ArrayList<>();
    
    private TextField searchField;
    private Button selectAllButton;
    private Button selectNoneButton;
    private Button hideButton;
    private Button showButton;
    private Button backButton;
    private Predicate<Rectangle> selectionCache;
    
    private SearchFilter lastFilter = SearchFilter.matchAll();
    
    public FilteringScreen(FilteringEntry filteringEntry) {
        super(new TranslatableComponent("config.roughlyenoughitems.filteringScreen"));
        this.filteringEntry = filteringEntry;
        this.searchField = Widgets.createTextField(new Rectangle());
        {
            Component selectAllText = new TranslatableComponent("config.roughlyenoughitems.filteredEntries.selectAll");
            this.selectAllButton = new Button(0, 0, Minecraft.getInstance().font.width(selectAllText) + 10, 20, selectAllText, button -> {
                this.points.clear();
                this.points.add(new PointPair(new Point(-Integer.MAX_VALUE / 2, -Integer.MAX_VALUE / 2), new Point(Integer.MAX_VALUE / 2, Integer.MAX_VALUE / 2)));
            });
        }
        {
            Component selectNoneText = new TranslatableComponent("config.roughlyenoughitems.filteredEntries.selectNone");
            this.selectNoneButton = new Button(0, 0, Minecraft.getInstance().font.width(selectNoneText) + 10, 20, selectNoneText, button -> {
                this.points.clear();
            });
        }
        {
            Component hideText = new TranslatableComponent("config.roughlyenoughitems.filteredEntries.hide");
            this.hideButton = new Button(0, 0, Minecraft.getInstance().font.width(hideText) + 10, 20, hideText, button -> {
                for (int i = 0; i < entryStacks.size(); i++) {
                    EntryStack<?> stack = entryStacks.get(i);
                    FilteringListEntry entry = entries.get(i);
                    entry.getBounds().y = entry.backupY - scrolling.scrollAmountInt();
                    if (entry.isSelected() && !entry.isFiltered()) {
                        filteringEntry.configFiltered.add(stack);
                        filteringEntry.edited = true;
                        entry.dirty = true;
                    }
                }
            });
        }
        {
            Component showText = new TranslatableComponent("config.roughlyenoughitems.filteredEntries.show");
            this.showButton = new Button(0, 0, Minecraft.getInstance().font.width(showText) + 10, 20, showText, button -> {
                for (int i = 0; i < entryStacks.size(); i++) {
                    EntryStack<?> stack = entryStacks.get(i);
                    FilteringListEntry entry = entries.get(i);
                    entry.getBounds().y = entry.backupY - scrolling.scrollAmountInt();
                    if (entry.isSelected() && filteringEntry.configFiltered.remove(stack)) {
                        filteringEntry.edited = true;
                        entry.dirty = true;
                    }
                }
            });
        }
        {
            Component backText = new TextComponent("↩ ").append(new TranslatableComponent("gui.back"));
            this.backButton = new Button(0, 0, Minecraft.getInstance().font.width(backText) + 10, 20, backText, button -> {
                minecraft.setScreen(parent);
                this.parent = null;
            });
        }
    }
    
    private static Rectangle updateInnerBounds(Rectangle bounds) {
        int width = Math.max(Mth.floor((bounds.width - 2 - 6) / 18.0F), 1);
        return new Rectangle((int) (bounds.getCenterX() - width * 18 / 2f), bounds.y + 5, width * 18, bounds.height);
    }
    
    public Rectangle getBounds() {
        return new Rectangle(0, 30, width, this.height - 30);
    }
    
    @Override
    public void init() {
        super.init();
        Rectangle bounds = getBounds();
        updateSearch(this.searchField.getText());
        this.selectAllButton.x = 2;
        this.selectAllButton.y = bounds.getMaxY() - 22;
        this.selectNoneButton.x = 4 + selectAllButton.getWidth();
        this.selectNoneButton.y = bounds.getMaxY() - 22;
        int searchFieldWidth = Math.max(bounds.width - (selectNoneButton.x + selectNoneButton.getWidth() + hideButton.getWidth() + showButton.getWidth() + 12), 100);
        this.searchField.asWidget().getBounds().setBounds(selectNoneButton.x + selectNoneButton.getWidth() + 4, bounds.getMaxY() - 21, searchFieldWidth, 18);
        this.hideButton.x = bounds.getMaxX() - hideButton.getWidth() - showButton.getWidth() - 4;
        this.hideButton.y = bounds.getMaxY() - 22;
        this.showButton.x = bounds.getMaxX() - showButton.getWidth() - 2;
        this.showButton.y = bounds.getMaxY() - 22;
        this.backButton.x = 4;
        this.backButton.y = 4;
        this.searchField.setResponder(this::updateSearch);
    }
    
    protected void renderHoleBackground(PoseStack matrices, int y1, int y2, int tint, int alpha1, int alpha2) {
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();
        RenderSystem.setShaderTexture(0, BACKGROUND_LOCATION);
        Matrix4f matrix = matrices.last().pose();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        float float_1 = 32.0F;
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        buffer.vertex(matrix, 0, y2, 0.0F).uv(0.0F, y2 / 32.0F).color(tint, tint, tint, alpha2).endVertex();
        buffer.vertex(matrix, this.width, y2, 0.0F).uv(this.width / 32.0F, y2 / 32.0F).color(tint, tint, tint, alpha2).endVertex();
        buffer.vertex(matrix, this.width, y1, 0.0F).uv(this.width / 32.0F, y1 / 32.0F).color(tint, tint, tint, alpha1).endVertex();
        buffer.vertex(matrix, 0, y1, 0.0F).uv(0.0F, y1 / 32.0F).color(tint, tint, tint, alpha1).endVertex();
        tesselator.end();
    }
    
    @Override
    public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
        renderHoleBackground(matrices, 0, height, 32, 255, 255);
        updateSelectionCache();
        Rectangle bounds = getBounds();
        tooltip = null;
        if (bounds.isEmpty())
            return;
        ScissorsHandler.INSTANCE.scissor(bounds);
        for (FilteringListEntry entry : entries)
            entry.slot.clearEntries();
        int skip = Math.max(0, Mth.floor(scrolling.scrollAmount() / 18.0F));
        int nextIndex = skip * innerBounds.width / 18;
        int i = nextIndex;
        BatchedSlots slots = Widgets.createBatchedSlots();
        for (; i < entryStacks.size(); i++) {
            EntryStack<?> stack = entryStacks.get(i);
            FilteringListEntry entry = entries.get(nextIndex);
            entry.getBounds().y = entry.backupY - scrolling.scrollAmountInt();
            if (entry.getBounds().y > bounds.getMaxY())
                break;
            entry.slot.entry(stack);
            slots.add(entry.slot);
            nextIndex++;
        }
        slots.render(matrices, mouseX, mouseY, delta);
        updatePosition(delta);
        scrolling.renderScrollBar(0, 1.0F, ConfigObject.getInstance().isUsingDarkTheme() ? 0.8F : 1F);
        matrices.pushPose();
        matrices.translate(0, 0, 300);
        this.searchField.asWidget().render(matrices, mouseX, mouseY, delta);
        this.selectAllButton.render(matrices, mouseX, mouseY, delta);
        this.selectNoneButton.render(matrices, mouseX, mouseY, delta);
        this.hideButton.render(matrices, mouseX, mouseY, delta);
        this.showButton.render(matrices, mouseX, mouseY, delta);
        matrices.popPose();
        
        ScissorsHandler.INSTANCE.removeLastScissor();
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(770, 771, 0, 1);
        RenderSystem.disableTexture();
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        Matrix4f matrix = matrices.last().pose();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        buffer.vertex(matrix, 0, bounds.y + 4, 0.0F).uv(0.0F, 1.0F).color(0, 0, 0, 0).endVertex();
        buffer.vertex(matrix, width, bounds.y + 4, 0.0F).uv(1.0F, 1.0F).color(0, 0, 0, 0).endVertex();
        buffer.vertex(matrix, width, bounds.y, 0.0F).uv(1.0F, 0.0F).color(0, 0, 0, 255).endVertex();
        buffer.vertex(matrix, 0, bounds.y, 0.0F).uv(0.0F, 0.0F).color(0, 0, 0, 255).endVertex();
        tesselator.end();
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
        renderHoleBackground(matrices, 0, bounds.y, 64, 255, 255);
        
        this.backButton.render(matrices, mouseX, mouseY, delta);
        
        if (tooltip != null) {
            REIRuntime.getInstance().getOverlay().get().renderTooltip(matrices, tooltip);
        }
        
        this.font.drawShadow(matrices, this.title.getVisualOrderText(), this.width / 2.0F - this.font.width(this.title) / 2.0F, 12.0F, -1);
        Component hint = new TranslatableComponent("config.roughlyenoughitems.filteringRulesScreen.hint").withStyle(ChatFormatting.YELLOW);
        this.font.drawShadow(matrices, hint, this.width - this.font.width(hint) - 15, 12.0F, -1);
    }
    
    private Predicate<Rectangle> getSelection() {
        return selectionCache;
    }
    
    private void updateSelectionCache() {
        if (!points.isEmpty()) {
            Predicate<Rectangle> predicate = rect -> false;
            for (PointPair pair : points) {
                Point firstPoint = pair.firstPoint();
                Point secondPoint = pair.secondPoint();
                if (secondPoint == null) {
                    secondPoint = PointHelper.ofMouse();
                    secondPoint.translate(0, scrolling.scrollAmountInt());
                }
                int left = Math.min(firstPoint.x, secondPoint.x);
                int top = Math.min(firstPoint.y, secondPoint.y);
                int right = Math.max(firstPoint.x, secondPoint.x);
                int bottom = Math.max(firstPoint.y, secondPoint.y);
                Rectangle rectangle = new Rectangle(left, top - scrolling.scrollAmountInt(), Math.max(1, right - left), Math.max(1, bottom - top));
                predicate = predicate.or(rectangle::intersects);
            }
            selectionCache = predicate;
            return;
        }
        selectionCache = rect -> false;
    }
    
    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dx, double dy) {
        if (scrolling.mouseDragged(mouseX, mouseY, button, dx, dy))
            return true;
        return super.mouseDragged(mouseX, mouseY, button, dx, dy);
    }
    
    private void updatePosition(float delta) {
        scrolling.updatePosition(delta);
    }
    
    public void updateSearch(String searchTerm) {
        lastFilter = SearchProvider.getInstance().createFilter(searchTerm);
        Set<EntryStack<?>> list = Sets.newLinkedHashSet();
        EntryRegistry.getInstance().getEntryStacks().parallel().filter(this::matches).map(EntryStack::normalize).forEachOrdered(list::add);
        
        entryStacks = Lists.newArrayList(list);
        updateEntriesPosition();
    }
    
    public boolean matches(EntryStack<?> stack) {
        EntrySerializer<?> serializer = stack.getDefinition().getSerializer();
        if (serializer == null || !serializer.supportReading() || !serializer.supportSaving()) {
            return false;
        }
        return lastFilter.test(stack);
    }
    
    public void updateEntriesPosition() {
        this.innerBounds = updateInnerBounds(getBounds());
        int width = innerBounds.width / 18;
        int pageHeight = innerBounds.height / 18;
        int slotsToPrepare = Math.max(entryStacks.size() * 3, width * pageHeight * 3);
        int currentX = 0;
        int currentY = 0;
        List<FilteringListEntry> entries = Lists.newArrayList();
        for (int i = 0; i < slotsToPrepare; i++) {
            int xPos = currentX * 18 + innerBounds.x;
            int yPos = currentY * 18 + innerBounds.y;
            entries.add(new FilteringListEntry(xPos, yPos));
            currentX++;
            if (currentX >= width) {
                currentX = 0;
                currentY++;
            }
        }
        this.entries = entries;
        this.elements = Lists.newArrayList(entries);
        this.elements.add(searchField.asWidget());
    }
    
    @Override
    public List<? extends GuiEventListener> children() {
        return elements;
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (scrolling.updateDraggingState(mouseX, mouseY, button))
            return true;
        
        if (getBounds().contains(mouseX, mouseY)) {
            if (searchField.asWidget().mouseClicked(mouseX, mouseY, button)) {
                this.points.clear();
                return true;
            } else if (selectAllButton.mouseClicked(mouseX, mouseY, button)) {
                return true;
            } else if (selectNoneButton.mouseClicked(mouseX, mouseY, button)) {
                return true;
            } else if (hideButton.mouseClicked(mouseX, mouseY, button)) {
                return true;
            } else if (showButton.mouseClicked(mouseX, mouseY, button)) {
                return true;
            } else if (button == 0) {
                if (!Screen.hasShiftDown()) {
                    this.points.clear();
                }
                this.points.add(new PointPair(new Point(mouseX, mouseY + scrolling.scrollAmount()), null));
                return true;
            }
        }
        return backButton.mouseClicked(mouseX, mouseY, button);
    }
    
    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && !points.isEmpty()) {
            PointPair pair = this.points.get(points.size() - 1);
            if (pair.secondPoint() == null) {
                this.points.set(points.size() - 1, new PointPair(pair.firstPoint(), new Point(mouseX, mouseY + scrolling.scrollAmount())));
                return true;
            }
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }
    
    @Override
    public boolean charTyped(char chr, int keyCode) {
        for (GuiEventListener element : children())
            if (element.charTyped(chr, keyCode))
                return true;
        return super.charTyped(chr, keyCode);
    }
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        for (GuiEventListener element : children())
            if (element.keyPressed(keyCode, scanCode, modifiers))
                return true;
        if (Screen.isSelectAll(keyCode)) {
            this.points.clear();
            this.points.add(new PointPair(new Point(-Integer.MAX_VALUE / 2, -Integer.MAX_VALUE / 2), new Point(Integer.MAX_VALUE / 2, Integer.MAX_VALUE / 2)));
            return true;
        }
        if (keyCode == 256 && this.shouldCloseOnEsc()) {
            this.backButton.onPress();
            return true;
        } else if (keyCode == 258) {
            boolean bl = !hasShiftDown();
            if (!this.changeFocus(bl)) {
                this.changeFocus(bl);
            }
            
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
    
    private class FilteringListEntry extends DelegateWidget {
        private final Slot slot;
        private int backupY;
        private boolean filtered = false;
        private boolean dirty = true;
        
        private FilteringListEntry(int x, int y) {
            super(Widgets.createSlot(new Point(x, y))
                    .noFavoritesInteractable()
                    .noInteractable()
                    .noBackground()
                    .noTooltips()
                    .noHighlight());
            this.slot = (Slot) widget;
            this.backupY = y;
        }
        
        @Override
        public void render(PoseStack poseStack, int mouseX, int mouseY, float delta) {
            drawBackground(poseStack, mouseX, mouseY, delta);
            super.render(poseStack, mouseX, mouseY, delta);
            drawExtra(poseStack, mouseX, mouseY, delta);
            
            if (searchField.asWidget().containsMouse(mouseX, mouseY))
                return;
            if (containsMouse(mouseX, mouseY)) {
                Tooltip tooltip = slot.getCurrentTooltip(new Point(mouseX, mouseY));
                if (tooltip != null) {
                    FilteringScreen.this.tooltip = tooltip;
                }
            }
        }
        
        public boolean isSelected() {
            return getSelection().test(getBounds());
        }
        
        public boolean isFiltered() {
            if (dirty) {
                filtered = filteringEntry.configFiltered.contains(slot.getCurrentEntry());
                dirty = false;
            }
            return filtered;
        }
        
        private void drawExtra(PoseStack matrices, int mouseX, int mouseY, float delta) {
            if (isSelected()) {
                Rectangle bounds = getBounds();
                RenderSystem.disableDepthTest();
                fillGradient(matrices, bounds.x, bounds.y, bounds.getMaxX(), bounds.getMaxY(), 0x896b70fa, 0x896b70fa);
                RenderSystem.enableDepthTest();
            }
        }
        
        private void drawBackground(PoseStack matrices, int mouseX, int mouseY, float delta) {
            if (isFiltered()) {
                Rectangle bounds = getBounds();
                RenderSystem.disableDepthTest();
                fillGradient(matrices, bounds.x, bounds.y, bounds.getMaxX(), bounds.getMaxY(), 0xffff0000, 0xffff0000);
                RenderSystem.enableDepthTest();
            }
        }
    }
}
