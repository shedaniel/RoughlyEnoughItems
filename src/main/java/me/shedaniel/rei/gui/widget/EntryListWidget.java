/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.gui.widget;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import me.shedaniel.clothconfig2.ClothConfigInitializer;
import me.shedaniel.clothconfig2.api.ModifierKeyCode;
import me.shedaniel.clothconfig2.api.ScissorsHandler;
import me.shedaniel.clothconfig2.gui.widget.DynamicNewSmoothScrollingEntryListWidget;
import me.shedaniel.math.api.Point;
import me.shedaniel.math.api.Rectangle;
import me.shedaniel.math.impl.PointHelper;
import me.shedaniel.rei.RoughlyEnoughItemsCore;
import me.shedaniel.rei.api.*;
import me.shedaniel.rei.gui.ContainerScreenOverlay;
import me.shedaniel.rei.gui.config.ItemCheatingMode;
import me.shedaniel.rei.gui.config.ItemListOrdering;
import me.shedaniel.rei.impl.ScreenHelper;
import me.shedaniel.rei.impl.SearchArgument;
import me.shedaniel.rei.utils.CollectionUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.Matrix4f;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemGroup;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class EntryListWidget extends WidgetWithBounds {
    
    static final Supplier<Boolean> RENDER_ENCHANTMENT_GLINT = ConfigObject.getInstance()::doesRenderEntryEnchantmentGlint;
    @SuppressWarnings("deprecation")
    static final Comparator<? super EntryStack> ENTRY_NAME_COMPARER = Comparator.comparing(SearchArgument::tryGetEntryStackName);
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
    private static final String SPACE = " ", EMPTY = "";
    private static int page;
    protected double target;
    protected double scroll;
    protected long start;
    protected long duration;
    protected int blockedCount;
    private boolean debugTime;
    private Rectangle bounds, innerBounds;
    private List<EntryStack> allStacks = null;
    private List<EntryStack> favorites = null;
    private List<EntryListEntry> entries = Collections.emptyList();
    private List<Widget> widgets = Collections.emptyList();
    @SuppressWarnings("deprecation") private List<SearchArgument.SearchArguments> lastSearchArguments = Collections.emptyList();
    private boolean draggingScrollBar = false;
    
    public static int entrySize() {
        return MathHelper.ceil(SIZE * ConfigObject.getInstance().getEntrySize());
    }
    
    @SuppressWarnings("rawtypes")
    static boolean notSteppingOnExclusionZones(int left, int top, Rectangle listArea) {
        MinecraftClient instance = MinecraftClient.getInstance();
        for (DisplayHelper.DisplayBoundsHandler sortedBoundsHandler : DisplayHelper.getInstance().getSortedBoundsHandlers(instance.currentScreen.getClass())) {
            ActionResult fit = sortedBoundsHandler.canItemSlotWidgetFit(left, top, instance.currentScreen, listArea);
            if (fit != ActionResult.PASS)
                return fit == ActionResult.SUCCESS;
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
    
    protected final int getSlotsHeightNumberForFavorites() {
        if (favorites.isEmpty())
            return 0;
        if (ConfigObject.getInstance().isEntryListWidgetScrolled())
            return MathHelper.ceil(2 + favorites.size() / (innerBounds.width / (float) entrySize()));
        int height = MathHelper.ceil(favorites.size() / (innerBounds.width / (float) entrySize()));
        int pagesToFit = MathHelper.ceil(height / (innerBounds.height / (float) entrySize() - 1));
        if (height > (innerBounds.height / entrySize() - 1) && (height) % (innerBounds.height / entrySize()) == (innerBounds.height / entrySize()) - 2)
            height--;
        return height + pagesToFit + 1;
    }
    
    protected final int getScrollNumberForFavorites() {
        if (favorites.isEmpty())
            return 0;
        return (innerBounds.width / entrySize()) * getSlotsHeightNumberForFavorites();
    }
    
    protected final int getMaxScrollPosition() {
        if (favorites.isEmpty())
            return MathHelper.ceil((allStacks.size() + blockedCount) / (innerBounds.width / (float) entrySize())) * entrySize();
        return MathHelper.ceil((allStacks.size() + blockedCount + getScrollNumberForFavorites()) / (innerBounds.width / (float) entrySize())) * entrySize() - 12;
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
        if (ConfigObject.getInstance().isEntryListWidgetScrolled() && bounds.contains(double_1, double_2)) {
            offset(ClothConfigInitializer.getScrollStep() * -double_3, true);
            return true;
        }
        return super.mouseScrolled(double_1, double_2, double_3);
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
        return MathHelper.ceil((allStacks.size() + getScrollNumberForFavorites()) / (float) entries.size());
    }
    
    @Override
    public void render(int mouseX, int mouseY, float delta) {
        if (ConfigObject.getInstance().isEntryListWidgetScrolled()) {
            for (EntryListEntry entry : entries)
                entry.clearStacks();
            ScissorsHandler.INSTANCE.scissor(bounds);
            int sizeForFavorites = getSlotsHeightNumberForFavorites();
            int skip = Math.max(0, MathHelper.floor(scroll / (float) entrySize()) - sizeForFavorites);
            int nextIndex = skip * innerBounds.width / entrySize();
            int i = nextIndex;
            blockedCount = 0;
            if (debugTime) {
                int size = 0;
                long time = 0;
                if (sizeForFavorites > 0) {
                    drawString(font, I18n.translate("text.rei.favorites"), innerBounds.x + 2, (int) (innerBounds.y + 8 - scroll), -1);
                    nextIndex += innerBounds.width / entrySize();
                    back1:
                    for (EntryStack stack : favorites) {
                        while (true) {
                            EntryListEntry entry = entries.get(nextIndex);
                            entry.getBounds().y = (int) (entry.backupY - scroll);
                            if (entry.getBounds().y > bounds.getMaxY())
                                break back1;
                            if (notSteppingOnExclusionZones(entry.getBounds().x, entry.getBounds().y, innerBounds)) {
                                entry.entry(stack);
                                entry.isFavorites = true;
                                size++;
                                long l = System.currentTimeMillis();
                                entry.render(mouseX, mouseY, delta);
                                time += (System.currentTimeMillis() - l);
                                nextIndex++;
                                break;
                            } else {
                                blockedCount++;
                                nextIndex++;
                            }
                        }
                    }
                    nextIndex += innerBounds.width / -entrySize() + getScrollNumberForFavorites() - favorites.size();
                }
                int offset = sizeForFavorites > 0 ? -12 : 0;
                back:
                for (; i < allStacks.size(); i++) {
                    EntryStack stack = allStacks.get(i);
                    while (true) {
                        EntryListEntry entry = entries.get(nextIndex);
                        entry.getBounds().y = (int) (entry.backupY - scroll + offset);
                        if (entry.getBounds().y > bounds.getMaxY())
                            break back;
                        if (notSteppingOnExclusionZones(entry.getBounds().x, entry.getBounds().y, innerBounds)) {
                            entry.entry(stack);
                            entry.isFavorites = false;
                            size++;
                            long l = System.nanoTime();
                            entry.render(mouseX, mouseY, delta);
                            time += (System.nanoTime() - l);
                            nextIndex++;
                            break;
                        } else {
                            blockedCount++;
                            nextIndex++;
                        }
                    }
                }
                int z = getZ();
                setZ(500);
                String str = String.format("%d entries, avg. %.0fns, %s fps", size, time / (double) size, minecraft.fpsDebugString.split(" ")[0]);
                fillGradient(bounds.x, bounds.y, bounds.x + font.getStringWidth(str) + 2, bounds.y + font.fontHeight + 2, -16777216, -16777216);
                MatrixStack matrixStack_1 = new MatrixStack();
                VertexConsumerProvider.Immediate immediate = VertexConsumerProvider.immediate(Tessellator.getInstance().getBuffer());
                matrixStack_1.translate(0.0D, 0.0D, getZ());
                Matrix4f matrix4f_1 = matrixStack_1.peek().getModel();
                font.draw(str, bounds.x + 2, bounds.y + 2, -1, false, matrix4f_1, immediate, false, 0, 15728880);
                immediate.draw();
                setZ(z);
            } else {
                if (sizeForFavorites > 0) {
                    drawString(font, I18n.translate("text.rei.favorites"), innerBounds.x + 2, (int) (innerBounds.y + 8 - scroll), -1);
                    nextIndex += innerBounds.width / entrySize();
                    back1:
                    for (EntryStack stack : favorites) {
                        while (true) {
                            EntryListEntry entry = entries.get(nextIndex);
                            entry.getBounds().y = (int) (entry.backupY - scroll);
                            if (entry.getBounds().y > bounds.getMaxY())
                                break back1;
                            if (notSteppingOnExclusionZones(entry.getBounds().x, entry.getBounds().y, innerBounds)) {
                                entry.entry(stack);
                                entry.isFavorites = true;
                                entry.render(mouseX, mouseY, delta);
                                nextIndex++;
                                break;
                            } else {
                                blockedCount++;
                                nextIndex++;
                            }
                        }
                    }
                    nextIndex += innerBounds.width / -entrySize() + getScrollNumberForFavorites() - favorites.size();
                }
                int offset = sizeForFavorites > 0 ? -12 : 0;
                back:
                for (; i < allStacks.size(); i++) {
                    EntryStack stack = allStacks.get(i);
                    while (true) {
                        EntryListEntry entry = entries.get(nextIndex);
                        entry.getBounds().y = (int) (entry.backupY - scroll + offset);
                        if (entry.getBounds().y > bounds.getMaxY())
                            break back;
                        if (notSteppingOnExclusionZones(entry.getBounds().x, entry.getBounds().y, innerBounds)) {
                            entry.entry(stack);
                            entry.isFavorites = false;
                            entry.render(mouseX, mouseY, delta);
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
            renderScrollbar();
        } else {
            if (debugTime) {
                int size = 0;
                long time = 0;
                for (Widget widget : widgets) {
                    if (widget instanceof EntryListEntry) {
                        size++;
                        long l = System.nanoTime();
                        widget.render(mouseX, mouseY, delta);
                        time += (System.nanoTime() - l);
                    } else
                        widget.render(mouseX, mouseY, delta);
                }
                int z = getZ();
                setZ(500);
                String str = String.format("%d entries, avg. %.0fns, %s fps", size, time / (double) size, minecraft.fpsDebugString.split(" ")[0]);
                fillGradient(bounds.x, bounds.y, bounds.x + font.getStringWidth(str) + 2, bounds.y + font.fontHeight + 2, -16777216, -16777216);
                MatrixStack matrixStack_1 = new MatrixStack();
                VertexConsumerProvider.Immediate immediate = VertexConsumerProvider.immediate(Tessellator.getInstance().getBuffer());
                matrixStack_1.translate(0.0D, 0.0D, getZ());
                Matrix4f matrix4f_1 = matrixStack_1.peek().getModel();
                font.draw(str, bounds.x + 2, bounds.y + 2, -1, false, matrix4f_1, immediate, false, 0, 15728880);
                immediate.draw();
                setZ(z);
            } else {
                for (Widget widget : widgets) {
                    widget.render(mouseX, mouseY, delta);
                }
            }
        }
    }
    
    private int getScrollbarMinX() {
        if (ConfigObject.getInstance().isLeftHandSidePanel())
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
        target = clamp(target);
        if (target < 0) {
            target -= target * (1 - ClothConfigInitializer.getBounceBackMultiplier()) * delta / 3;
        } else if (target > getMaxScroll()) {
            target = (target - getMaxScroll()) * (1 - (1 - ClothConfigInitializer.getBounceBackMultiplier()) * delta / 3) + getMaxScroll();
        } else if (ConfigObject.getInstance().doesSnapToRows()) {
            double nearestRow = Math.round(target / (double) entrySize()) * (double) entrySize();
            if (!DynamicNewSmoothScrollingEntryListWidget.Precision.almostEquals(target, nearestRow, DynamicNewSmoothScrollingEntryListWidget.Precision.FLOAT_EPSILON))
                target += (nearestRow - target) * Math.min(delta / 2.0, 1.0);
            else
                target = nearestRow;
        }
        if (!DynamicNewSmoothScrollingEntryListWidget.Precision.almostEquals(scroll, target, DynamicNewSmoothScrollingEntryListWidget.Precision.FLOAT_EPSILON))
            scroll = (float) DynamicNewSmoothScrollingEntryListWidget.Interpolation.expoEase(scroll, target, Math.min((System.currentTimeMillis() - start) / ((double) duration), 1));
        else
            scroll = target;
    }
    
    @Override
    public boolean keyPressed(int int_1, int int_2, int int_3) {
        if (containsMouse(PointHelper.fromMouse()))
            for (Widget widget : widgets)
                if (widget.keyPressed(int_1, int_2, int_3))
                    return true;
        return false;
    }
    
    public void updateArea(DisplayHelper.DisplayBoundsHandler<?> boundsHandler, @Nullable String searchTerm) {
        this.bounds = boundsHandler.getItemListArea(ScreenHelper.getLastOverlay().getBounds());
        FavoritesListWidget favoritesListWidget = ContainerScreenOverlay.getFavoritesListWidget();
        if (favoritesListWidget != null)
            favoritesListWidget.updateFavoritesBounds(boundsHandler, searchTerm);
        if (searchTerm != null)
            updateSearch(searchTerm);
        else if (allStacks == null || favorites == null || (favoritesListWidget != null && favoritesListWidget.favorites == null))
            updateSearch("");
        else
            updateEntriesPosition();
    }
    
    public void updateEntriesPosition() {
        this.innerBounds = updateInnerBounds(bounds);
        if (!ConfigObject.getInstance().isEntryListWidgetScrolled()) {
            page = Math.max(page, 0);
            List<EntryListEntry> entries = Lists.newLinkedList();
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
            int numberForFavorites = getScrollNumberForFavorites();
            List<EntryStack> subList = allStacks.stream().skip(Math.max(0, page * entries.size() - numberForFavorites)).limit(Math.max(0, entries.size() - Math.max(0, numberForFavorites - page * entries.size()))).collect(Collectors.toList());
            for (int i = 0; i < subList.size(); i++) {
                EntryStack stack = subList.get(i);
                entries.get(i + Math.max(0, numberForFavorites - page * entries.size())).clearStacks().entry(stack);
                entries.get(i + Math.max(0, numberForFavorites - page * entries.size())).isFavorites = false;
            }
            this.entries = entries;
            this.widgets = Lists.newLinkedList(entries);
            if (numberForFavorites > 0) {
                int skippedFavorites = page * (entries.size() - width);
                int j = 0;
                if (skippedFavorites < favorites.size()) {
                    widgets.add(new LabelWidget(new Point(innerBounds.x + 2, innerBounds.y + 6), I18n.translate("text.rei.favorites")).leftAligned());
                    j += width;
                }
                List<EntryStack> subFavoritesList = favorites.stream().skip(skippedFavorites).limit(Math.max(0, entries.size() - width)).collect(Collectors.toList());
                for (EntryStack stack : subFavoritesList) {
                    entries.get(j).clearStacks().entry(stack);
                    entries.get(j).isFavorites = true;
                    j++;
                }
            }
        } else {
            page = 0;
            int width = innerBounds.width / entrySize();
            int pageHeight = innerBounds.height / entrySize();
            int sizeForFavorites = getScrollNumberForFavorites();
            int slotsToPrepare = Math.max(allStacks.size() * 3 + sizeForFavorites * 3, width * pageHeight * 3);
            int currentX = 0;
            int currentY = 0;
            List<EntryListEntry> entries = Lists.newLinkedList();
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
            this.widgets = Collections.unmodifiableList(entries);
        }
        FavoritesListWidget favoritesListWidget = ContainerScreenOverlay.getFavoritesListWidget();
        if (favoritesListWidget != null)
            favoritesListWidget.updateEntriesPosition();
    }
    
    @Deprecated
    public List<EntryStack> getAllStacks() {
        return allStacks;
    }
    
    public void updateSearch(String searchTerm) {
        lastSearchArguments = processSearchTerm(searchTerm);
        {
            List<EntryStack> list = Lists.newLinkedList();
            boolean checkCraftable = ConfigManager.getInstance().isCraftableOnlyEnabled() && !ScreenHelper.inventoryStacks.isEmpty();
            List<EntryStack> workingItems = checkCraftable ? RecipeHelper.getInstance().findCraftableEntriesByItems(CollectionUtils.map(ScreenHelper.inventoryStacks, EntryStack::create)) : null;
            List<EntryStack> stacks = EntryRegistry.getInstance().getStacksList();
            if (stacks instanceof CopyOnWriteArrayList) {
                for (EntryStack stack : stacks) {
                    if (canLastSearchTermsBeAppliedTo(stack)) {
                        if (workingItems != null && CollectionUtils.findFirstOrNullEquals(workingItems, stack) == null)
                            continue;
                        list.add(stack.copy().setting(EntryStack.Settings.RENDER_COUNTS, EntryStack.Settings.FALSE).setting(EntryStack.Settings.Item.RENDER_ENCHANTMENT_GLINT, RENDER_ENCHANTMENT_GLINT));
                    }
                }
            }
            ItemListOrdering ordering = ConfigObject.getInstance().getItemListOrdering();
            if (ordering == ItemListOrdering.name)
                list.sort(ENTRY_NAME_COMPARER);
            if (ordering == ItemListOrdering.item_groups)
                list.sort(ENTRY_GROUP_COMPARER);
            if (!ConfigObject.getInstance().isItemListAscending())
                Collections.reverse(list);
            allStacks = list;
        }
        if (ConfigObject.getInstance().isFavoritesEnabled() && !ConfigObject.getInstance().doDisplayFavoritesOnTheLeft()) {
            List<EntryStack> list = Lists.newLinkedList();
            boolean checkCraftable = ConfigManager.getInstance().isCraftableOnlyEnabled() && !ScreenHelper.inventoryStacks.isEmpty();
            List<EntryStack> workingItems = checkCraftable ? RecipeHelper.getInstance().findCraftableEntriesByItems(CollectionUtils.map(ScreenHelper.inventoryStacks, EntryStack::create)) : null;
            for (EntryStack stack : ConfigManager.getInstance().getFavorites()) {
                if (canLastSearchTermsBeAppliedTo(stack)) {
                    if (workingItems != null && CollectionUtils.findFirstOrNullEquals(workingItems, stack) == null)
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
        } else
            favorites = Collections.emptyList();
        debugTime = ConfigObject.getInstance().doDebugRenderTimeRequired();
        FavoritesListWidget favoritesListWidget = ContainerScreenOverlay.getFavoritesListWidget();
        if (favoritesListWidget != null)
            favoritesListWidget.updateSearch(this, searchTerm);
        updateEntriesPosition();
    }
    
    public boolean canLastSearchTermsBeAppliedTo(EntryStack stack) {
        return lastSearchArguments.isEmpty() || canSearchTermsBeAppliedTo(stack, lastSearchArguments);
    }
    
    @SuppressWarnings("deprecation")
    private boolean canSearchTermsBeAppliedTo(EntryStack stack, List<SearchArgument.SearchArguments> searchArguments) {
        if (searchArguments.isEmpty())
            return true;
        String mod = null;
        String name = null;
        String tooltip = null;
        String[] tags = null;
        for (SearchArgument.SearchArguments arguments : searchArguments) {
            boolean applicable = true;
            for (SearchArgument argument : arguments.getArguments()) {
                if (argument.getArgumentType() == SearchArgument.ArgumentType.ALWAYS)
                    return true;
                else if (argument.getArgumentType() == SearchArgument.ArgumentType.MOD) {
                    if (mod == null)
                        mod = stack.getIdentifier().map(Identifier::getNamespace).orElse("").replace(SPACE, EMPTY).toLowerCase(Locale.ROOT);
                    if (mod != null && !mod.isEmpty() && argument.getFunction(!argument.isInclude()).apply(mod)) {
                        applicable = false;
                        break;
                    }
                } else if (argument.getArgumentType() == SearchArgument.ArgumentType.TEXT) {
                    if (name == null)
                        name = SearchArgument.tryGetEntryStackName(stack).replace(SPACE, EMPTY).toLowerCase(Locale.ROOT);
                    if (name != null && !name.isEmpty() && argument.getFunction(!argument.isInclude()).apply(name)) {
                        applicable = false;
                        break;
                    }
                } else if (argument.getArgumentType() == SearchArgument.ArgumentType.TOOLTIP) {
                    if (name == null)
                        name = SearchArgument.tryGetEntryStackTooltip(stack).replace(SPACE, EMPTY).toLowerCase(Locale.ROOT);
                    if (name != null && !name.isEmpty() && argument.getFunction(!argument.isInclude()).apply(name)) {
                        applicable = false;
                        break;
                    }
                } else if (argument.getArgumentType() == SearchArgument.ArgumentType.TAG) {
                    if (tags == null) {
                        if (stack.getType() == EntryStack.Type.ITEM) {
                            Identifier[] tagsFor = minecraft.getNetworkHandler().getTagManager().items().getTagsFor(stack.getItem()).toArray(new Identifier[0]);
                            tags = new String[tagsFor.length];
                            for (int i = 0; i < tagsFor.length; i++)
                                tags[i] = tagsFor[i].toString();
                        } else if (stack.getType() == EntryStack.Type.FLUID) {
                            Identifier[] tagsFor = minecraft.getNetworkHandler().getTagManager().fluids().getTagsFor(stack.getFluid()).toArray(new Identifier[0]);
                            tags = new String[tagsFor.length];
                            for (int i = 0; i < tagsFor.length; i++)
                                tags[i] = tagsFor[i].toString();
                        } else
                            tags = new String[0];
                    }
                    if (tags != null && tags.length > 0) {
                        boolean a = false;
                        for (String tag : tags)
                            if (argument.getFunction(argument.isInclude()).apply(tag))
                                a = true;
                        if (!a) {
                            applicable = false;
                            break;
                        }
                    } else {
                        applicable = false;
                        break;
                    }
                }
            }
            if (applicable)
                return true;
        }
        return false;
    }
    
    @SuppressWarnings("deprecation")
    private List<SearchArgument.SearchArguments> processSearchTerm(String searchTerm) {
        List<SearchArgument.SearchArguments> searchArguments = Lists.newArrayList();
        for (String split : StringUtils.splitByWholeSeparatorPreserveAllTokens(searchTerm.toLowerCase(Locale.ROOT), "|")) {
            String[] terms = StringUtils.split(split);
            if (terms.length == 0)
                searchArguments.add(SearchArgument.SearchArguments.ALWAYS);
            else {
                SearchArgument[] arguments = new SearchArgument[terms.length];
                for (int i = 0; i < terms.length; i++) {
                    String term = terms[i];
                    if (term.startsWith("-@") || term.startsWith("@-")) {
                        arguments[i] = new SearchArgument(SearchArgument.ArgumentType.MOD, term.substring(2), false);
                    } else if (term.startsWith("@")) {
                        arguments[i] = new SearchArgument(SearchArgument.ArgumentType.MOD, term.substring(1), true);
                    } else if (term.startsWith("-$") || term.startsWith("$-")) {
                        arguments[i] = new SearchArgument(SearchArgument.ArgumentType.TAG, term.substring(2), false);
                    } else if (term.startsWith("$")) {
                        arguments[i] = new SearchArgument(SearchArgument.ArgumentType.TAG, term.substring(1), true);
                    } else if (term.startsWith("-#") || term.startsWith("#-")) {
                        arguments[i] = new SearchArgument(SearchArgument.ArgumentType.TOOLTIP, term.substring(2), false);
                    } else if (term.startsWith("#")) {
                        arguments[i] = new SearchArgument(SearchArgument.ArgumentType.TOOLTIP, term.substring(1), true);
                    } else if (term.startsWith("-")) {
                        arguments[i] = new SearchArgument(SearchArgument.ArgumentType.TEXT, term.substring(1), false);
                    } else {
                        arguments[i] = new SearchArgument(SearchArgument.ArgumentType.TEXT, term, true);
                    }
                }
                searchArguments.add(new SearchArgument.SearchArguments(arguments));
            }
        }
        return searchArguments;
    }
    
    @Override
    public List<? extends Widget> children() {
        return widgets;
    }
    
    @Override
    public boolean mouseClicked(double double_1, double double_2, int int_1) {
        if (ConfigObject.getInstance().isEntryListWidgetScrolled()) {
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
        }
        
        if (containsMouse(double_1, double_2)) {
            ClientPlayerEntity player = minecraft.player;
            if (ClientHelper.getInstance().isCheating() && !player.inventory.getCursorStack().isEmpty() && RoughlyEnoughItemsCore.hasPermissionToUsePackets()) {
                ClientHelper.getInstance().sendDeletePacket();
                return false;
            }
            if (!player.inventory.getCursorStack().isEmpty() && RoughlyEnoughItemsCore.hasPermissionToUsePackets())
                return false;
            for (Widget widget : children())
                if (widget.mouseClicked(double_1, double_2, int_1))
                    return true;
        }
        return false;
    }
    
    private class EntryListEntry extends EntryWidget {
        private int backupY;
        private boolean isFavorites;
        
        private EntryListEntry(int x, int y) {
            super(x, y);
            this.backupY = y;
            getBounds().width = getBounds().height = entrySize();
        }
        
        @Override
        public boolean containsMouse(double mouseX, double mouseY) {
            return super.containsMouse(mouseX, mouseY) && bounds.contains(mouseX, mouseY);
        }
        
        @Override
        protected void drawHighlighted(int mouseX, int mouseY, float delta) {
            if (getCurrentEntry().getType() != EntryStack.Type.EMPTY)
                super.drawHighlighted(mouseX, mouseY, delta);
        }
        
        private String getLocalizedName(InputUtil.KeyCode value) {
            String string_1 = value.getName();
            int int_1 = value.getKeyCode();
            String string_2 = null;
            switch (value.getCategory()) {
                case KEYSYM:
                    string_2 = InputUtil.getKeycodeName(int_1);
                    break;
                case SCANCODE:
                    string_2 = InputUtil.getScancodeName(int_1);
                    break;
                case MOUSE:
                    String string_3 = I18n.translate(string_1);
                    string_2 = Objects.equals(string_3, string_1) ? I18n.translate(InputUtil.Type.MOUSE.getName(), int_1 + 1) : string_3;
            }
            
            return string_2 == null ? I18n.translate(string_1) : string_2;
        }
        
        @Override
        protected void queueTooltip(int mouseX, int mouseY, float delta) {
            if (!ClientHelper.getInstance().isCheating() || minecraft.player.inventory.getCursorStack().isEmpty()) {
                QueuedTooltip tooltip = getCurrentTooltip(mouseX, mouseY);
                if (tooltip != null) {
                    if (ConfigObject.getInstance().doDisplayFavoritesTooltip() && !ConfigObject.getInstance().getFavoriteKeyCode().isUnknown()) {
                        String name = ConfigObject.getInstance().getFavoriteKeyCode().getLocalizedName();
                        if (!isFavorites)
                            tooltip.getText().addAll(Arrays.asList(I18n.translate("text.rei.favorites_tooltip", name).split("\n")));
                        else
                            tooltip.getText().addAll(Arrays.asList(I18n.translate("text.rei.remove_favorites_tooltip", name).split("\n")));
                    }
                    ScreenHelper.getLastOverlay().addTooltip(tooltip);
                }
            }
        }
        
        @Override
        public boolean keyPressed(int int_1, int int_2, int int_3) {
            if (interactable && ConfigObject.getInstance().isFavoritesEnabled() && containsMouse(PointHelper.fromMouse()) && !getCurrentEntry().isEmpty()) {
                ModifierKeyCode keyCode = ConfigObject.getInstance().getFavoriteKeyCode();
                if (keyCode.matchesKey(int_1, int_2)) {
                    if (!isFavorites) {
                        if (!CollectionUtils.anyMatchEqualsAll(ConfigManager.getInstance().getFavorites(), getCurrentEntry()))
                            ConfigManager.getInstance().getFavorites().add(getCurrentEntry().copy());
                    } else {
                        ConfigManager.getInstance().getFavorites().remove(getCurrentEntry());
                    }
                    ContainerScreenOverlay.getEntryListWidget().updateSearch(ScreenHelper.getSearchField().getText());
                    ConfigManager.getInstance().saveConfig();
                    minecraft.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                    return true;
                }
            }
            return super.keyPressed(int_1, int_2, int_3);
        }
        
        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (!interactable)
                return super.mouseClicked(mouseX, mouseY, button);
            if (containsMouse(mouseX, mouseY) && ClientHelper.getInstance().isCheating()) {
                EntryStack entry = getCurrentEntry().copy();
                if (!entry.isEmpty()) {
                    if (entry.getType() == EntryStack.Type.ITEM) {
                        if (ConfigObject.getInstance().getItemCheatingMode() == ItemCheatingMode.REI_LIKE)
                            entry.setAmount(button != 1 ? 1 : entry.getItemStack().getMaxCount());
                        else if (ConfigObject.getInstance().getItemCheatingMode() == ItemCheatingMode.JEI_LIKE)
                            entry.setAmount(button != 0 ? 1 : entry.getItemStack().getMaxCount());
                        else
                            entry.setAmount(1);
                    }
                    ClientHelper.getInstance().tryCheatingEntry(entry);
                    return true;
                }
            }
            return super.mouseClicked(mouseX, mouseY, button);
        }
    }
}
