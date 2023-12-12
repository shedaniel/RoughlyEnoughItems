/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020, 2021, 2022, 2023 shedaniel
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

package me.shedaniel.rei.impl.client.gui.screen.collapsible.selection;

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
import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import me.shedaniel.rei.api.client.registry.entry.EntryRegistry;
import me.shedaniel.rei.api.client.search.SearchFilter;
import me.shedaniel.rei.api.client.search.SearchProvider;
import me.shedaniel.rei.api.common.entry.EntrySerializer;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.impl.client.gui.ScreenOverlayImpl;
import me.shedaniel.rei.impl.client.gui.widget.BatchedEntryRendererManager;
import me.shedaniel.rei.impl.client.gui.widget.EntryWidget;
import me.shedaniel.rei.impl.client.gui.widget.search.OverlaySearchField;
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

import static me.shedaniel.rei.impl.client.gui.widget.entrylist.EntryListWidget.entrySize;

@ApiStatus.Internal
public class CustomCollapsibleEntrySelectionScreen extends Screen {
    private final List<EntryStack<?>> selectedStacks;
    protected List<EntryStack<?>> selected = Lists.newArrayList();
    protected final ScrollingContainer scrolling = new ScrollingContainer() {
        @Override
        public int getMaxScrollHeight() {
            return Mth.ceil(entryStacks.size() / (innerBounds.width / (float) entrySize())) * entrySize() + 28;
        }
        
        @Override
        public Rectangle getBounds() {
            return CustomCollapsibleEntrySelectionScreen.this.getBounds();
        }
        
        @Override
        public int getScrollBarX(int maxX) {
            return width - 7;
        }
    };
    
    public Screen parent;
    private Tooltip tooltip = null;
    private List<EntryStack<?>> entryStacks = null;
    private Rectangle innerBounds;
    private List<InnerStackEntry> entries = Collections.emptyList();
    private List<GuiEventListener> elements = Collections.emptyList();
    
    private record PointPair(Point firstPoint, @Nullable Point secondPoint) {}
    
    private final List<PointPair> points = new ArrayList<>();
    
    private final OverlaySearchField searchField;
    private final Button selectAllButton;
    private final Button selectNoneButton;
    private final Button addButton;
    private final Button removeButton;
    private final Button backButton;
    private Predicate<Rectangle> selectionCache;
    
    private SearchFilter lastFilter = SearchFilter.matchAll();
    
    public CustomCollapsibleEntrySelectionScreen(List<EntryStack<?>> selectedStacks) {
        super(new TranslatableComponent("text.rei.collapsible.entries.custom.title"));
        this.selectedStacks = selectedStacks;
        this.searchField = new OverlaySearchField(0, 0, 0, 0);
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
            Component addText = new TranslatableComponent("text.rei.collapsible.entries.custom.select.add");
            this.addButton = new Button(0, 0, Minecraft.getInstance().font.width(addText) + 10, 20, addText, button -> {
                for (int i = 0; i < entryStacks.size(); i++) {
                    EntryStack<?> stack = entryStacks.get(i);
                    InnerStackEntry entry = entries.get(i);
                    entry.getBounds().y = entry.backupY - scrolling.scrollAmountInt();
                    if (entry.isSelected() && !entry.isFiltered()) {
                        selectedStacks.add(stack);
                        entry.dirty = true;
                    }
                }
            });
        }
        {
            Component removeText = new TranslatableComponent("text.rei.collapsible.entries.custom.select.remove");
            this.removeButton = new Button(0, 0, Minecraft.getInstance().font.width(removeText) + 10, 20, removeText, button -> {
                for (int i = 0; i < entryStacks.size(); i++) {
                    EntryStack<?> stack = entryStacks.get(i);
                    InnerStackEntry entry = entries.get(i);
                    entry.getBounds().y = entry.backupY - scrolling.scrollAmountInt();
                    if (entry.isSelected() && selectedStacks.remove(stack)) {
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
        this.searchField.isMain = false;
    }
    
    @Override
    public void onClose() {
        this.minecraft.setScreen(parent);
        this.parent = null;
    }
    
    private static Rectangle updateInnerBounds(Rectangle bounds) {
        int width = Math.max(Mth.floor((bounds.width - 2 - 6) / (float) entrySize()), 1);
        return new Rectangle((int) (bounds.getCenterX() - width * entrySize() / 2f), bounds.y + 5, width * entrySize(), bounds.height);
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
        int searchFieldWidth = Math.max(bounds.width - (selectNoneButton.x + selectNoneButton.getWidth() + addButton.getWidth() + removeButton.getWidth() + 12), 100);
        this.searchField.getBounds().setBounds(selectNoneButton.x + selectNoneButton.getWidth() + 4, bounds.getMaxY() - 21, searchFieldWidth, 18);
        this.addButton.x = bounds.getMaxX() - addButton.getWidth() - removeButton.getWidth() - 4;
        this.addButton.y = bounds.getMaxY() - 22;
        this.removeButton.x = bounds.getMaxX() - removeButton.getWidth() - 2;
        this.removeButton.y = bounds.getMaxY() - 22;
        this.backButton.x = 4;
        this.backButton.y = 4;
        this.searchField.setResponder(this::updateSearch);
    }
    
    protected void renderHoleBackground(PoseStack poses, int y1, int y2, int tint, int alpha1, int alpha2) {
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();
        RenderSystem.setShaderTexture(0, BACKGROUND_LOCATION);
        Matrix4f matrix = poses.last().pose();
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
    public void render(PoseStack poses, int mouseX, int mouseY, float delta) {
        renderHoleBackground(poses, 0, height, 32, 255, 255);
        updateSelectionCache();
        Rectangle bounds = getBounds();
        tooltip = null;
        if (bounds.isEmpty())
            return;
        ScissorsHandler.INSTANCE.scissor(bounds);
        for (InnerStackEntry entry : entries)
            entry.clearStacks();
        int skip = Math.max(0, Mth.floor(scrolling.scrollAmount() / (float) entrySize()));
        int nextIndex = skip * innerBounds.width / entrySize();
        int i = nextIndex;
        BatchedEntryRendererManager manager = new BatchedEntryRendererManager();
        for (; i < entryStacks.size(); i++) {
            EntryStack<?> stack = entryStacks.get(i);
            InnerStackEntry entry = entries.get(nextIndex);
            entry.getBounds().y = entry.backupY - scrolling.scrollAmountInt();
            if (entry.getBounds().y > bounds.getMaxY())
                break;
            entry.entry(stack);
            manager.add(entry);
            nextIndex++;
        }
        manager.render(poses, mouseX, mouseY, delta);
        updatePosition(delta);
        scrolling.renderScrollBar(0, 1.0F, REIRuntime.getInstance().isDarkThemeEnabled() ? 0.8F : 1F);
        poses.pushPose();
        poses.translate(0, 0, 300);
        this.searchField.render(poses, mouseX, mouseY, delta);
        this.selectAllButton.render(poses, mouseX, mouseY, delta);
        this.selectNoneButton.render(poses, mouseX, mouseY, delta);
        this.addButton.render(poses, mouseX, mouseY, delta);
        this.removeButton.render(poses, mouseX, mouseY, delta);
        poses.popPose();
        
        ScissorsHandler.INSTANCE.removeLastScissor();
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(770, 771, 0, 1);
        RenderSystem.disableTexture();
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        Matrix4f matrix = poses.last().pose();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        buffer.vertex(matrix, 0, bounds.y + 4, 0.0F).uv(0.0F, 1.0F).color(0, 0, 0, 0).endVertex();
        buffer.vertex(matrix, width, bounds.y + 4, 0.0F).uv(1.0F, 1.0F).color(0, 0, 0, 0).endVertex();
        buffer.vertex(matrix, width, bounds.y, 0.0F).uv(1.0F, 0.0F).color(0, 0, 0, 255).endVertex();
        buffer.vertex(matrix, 0, bounds.y, 0.0F).uv(0.0F, 0.0F).color(0, 0, 0, 255).endVertex();
        tesselator.end();
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
        renderHoleBackground(poses, 0, bounds.y, 64, 255, 255);
        
        this.backButton.render(poses, mouseX, mouseY, delta);
        
        if (tooltip != null) {
            ScreenOverlayImpl.getInstance().renderTooltip(poses, tooltip);
        }
        
        this.font.drawShadow(poses, this.title.getVisualOrderText(), this.width / 2.0F - this.font.width(this.title) / 2.0F, 12.0F, -1);
        Component hint = new TranslatableComponent("config.roughlyenoughitems.filteringRulesScreen.hint").withStyle(ChatFormatting.YELLOW);
        this.font.drawShadow(poses, hint, this.width - this.font.width(hint) - 15, 12.0F, -1);
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
        int entrySize = entrySize();
        this.innerBounds = updateInnerBounds(getBounds());
        int width = innerBounds.width / entrySize;
        int pageHeight = innerBounds.height / entrySize;
        int slotsToPrepare = Math.max(entryStacks.size() * 3, width * pageHeight * 3);
        int currentX = 0;
        int currentY = 0;
        List<InnerStackEntry> entries = Lists.newArrayList();
        for (int i = 0; i < slotsToPrepare; i++) {
            int xPos = currentX * entrySize + innerBounds.x;
            int yPos = currentY * entrySize + innerBounds.y;
            entries.add(new InnerStackEntry(xPos, yPos, entrySize));
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
    public List<? extends GuiEventListener> children() {
        return elements;
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (scrolling.updateDraggingState(mouseX, mouseY, button))
            return true;
        
        if (getBounds().contains(mouseX, mouseY)) {
            if (searchField.mouseClicked(mouseX, mouseY, button)) {
                this.points.clear();
                return true;
            } else if (selectAllButton.mouseClicked(mouseX, mouseY, button)) {
                return true;
            } else if (selectNoneButton.mouseClicked(mouseX, mouseY, button)) {
                return true;
            } else if (addButton.mouseClicked(mouseX, mouseY, button)) {
                return true;
            } else if (removeButton.mouseClicked(mouseX, mouseY, button)) {
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
        for (GuiEventListener element : children()) {
            if (element.charTyped(chr, keyCode)) {
                return true;
            }
        }
        
        return super.charTyped(chr, keyCode);
    }
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        for (GuiEventListener element : children()) {
            if (element.keyPressed(keyCode, scanCode, modifiers)) {
                return true;
            }
        }
        
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
        if (searchTerm != null) {
            updateSearch(searchTerm);
        } else if (entryStacks == null) {
            updateSearch("");
        } else {
            updateEntriesPosition();
        }
    }
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (getBounds().contains(mouseX, mouseY)) {
            scrolling.offset(ClothConfigInitializer.getScrollStep() * -amount, true);
            return true;
        }
        
        super.mouseScrolled(mouseX, mouseY, amount);
        return true;
    }
    
    private class InnerStackEntry extends EntryWidget {
        private final int backupY;
        private boolean filtered = false;
        private boolean dirty = true;
        
        private InnerStackEntry(int x, int y, int entrySize) {
            super(new Point(x, y));
            this.backupY = y;
            getBounds().width = getBounds().height = entrySize;
            interactableFavorites(false);
            interactable(false);
            noHighlight();
        }
        
        @Override
        public boolean containsMouse(double mouseX, double mouseY) {
            return super.containsMouse(mouseX, mouseY) && CustomCollapsibleEntrySelectionScreen.this.getBounds().contains(mouseX, mouseY);
        }
        
        @Override
        protected void drawExtra(PoseStack matrices, int mouseX, int mouseY, float delta) {
            if (isSelected()) {
                Rectangle bounds = getBounds();
                RenderSystem.disableDepthTest();
                fillGradient(matrices, bounds.x, bounds.y, bounds.getMaxX(), bounds.getMaxY(), 0x896b70fa, 0x896b70fa);
                RenderSystem.enableDepthTest();
            }
        }
        
        public boolean isSelected() {
            return getSelection().test(getBounds());
        }
        
        public boolean isFiltered() {
            if (dirty) {
                filtered = selectedStacks.contains(getCurrentEntry());
                dirty = false;
            }
            return filtered;
        }
        
        @Override
        protected void drawBackground(PoseStack matrices, int mouseX, int mouseY, float delta) {
            if (isFiltered()) {
                Rectangle bounds = getBounds();
                RenderSystem.disableDepthTest();
                fillGradient(matrices, bounds.x, bounds.y, bounds.getMaxX(), bounds.getMaxY(), 0xff873e23, 0xff873e23);
                RenderSystem.enableDepthTest();
            }
        }
        
        @Override
        protected void queueTooltip(PoseStack matrices, int mouseX, int mouseY, float delta) {
            if (searchField.containsMouse(mouseX, mouseY))
                return;
            Tooltip tooltip = getCurrentTooltip(new Point(mouseX, mouseY));
            if (tooltip != null) {
                CustomCollapsibleEntrySelectionScreen.this.tooltip = tooltip;
            }
        }
    }
}
