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

package me.shedaniel.rei.impl.client.gui.widget;

import com.google.common.base.Suppliers;
import com.mojang.blaze3d.platform.InputConstants;
import me.shedaniel.clothconfig2.api.ModifierKeyCode;
import me.shedaniel.clothconfig2.api.animator.NumberAnimator;
import me.shedaniel.clothconfig2.api.animator.ValueAnimator;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.REIRuntime;
import me.shedaniel.rei.api.client.config.ConfigObject;
import me.shedaniel.rei.api.client.favorites.FavoriteEntry;
import me.shedaniel.rei.api.client.gui.drag.DraggableStack;
import me.shedaniel.rei.api.client.gui.drag.DraggableStackProviderWidget;
import me.shedaniel.rei.api.client.gui.drag.DraggedAcceptorResult;
import me.shedaniel.rei.api.client.gui.drag.DraggingContext;
import me.shedaniel.rei.api.client.gui.screen.DisplayScreen;
import me.shedaniel.rei.api.client.gui.widgets.Slot;
import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import me.shedaniel.rei.api.client.gui.widgets.TooltipContext;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.overlay.ScreenOverlay;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.client.registry.transfer.TransferHandler;
import me.shedaniel.rei.api.client.search.method.InputMethod;
import me.shedaniel.rei.api.client.view.ViewSearchBuilder;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.plugins.PluginManager;
import me.shedaniel.rei.api.common.util.FormattingUtils;
import me.shedaniel.rei.impl.client.REIRuntimeImpl;
import me.shedaniel.rei.impl.client.gui.InternalTextures;
import me.shedaniel.rei.impl.client.gui.dragging.CurrentDraggingStack;
import me.shedaniel.rei.impl.client.registry.display.DisplayCache;
import me.shedaniel.rei.impl.client.registry.display.DisplayRegistryImpl;
import me.shedaniel.rei.impl.client.util.CyclingList;
import me.shedaniel.rei.impl.client.util.OriginalRetainingCyclingList;
import me.shedaniel.rei.impl.client.view.ViewsImpl;
import net.minecraft.ChatFormatting;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.util.*;
import net.minecraft.client.Minecraft;
import me.shedaniel.rei.api.client.gui.compat.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.ARGB;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("UnstableApiUsage")
public class EntryWidget extends Slot implements DraggableStackProviderWidget {
    @ApiStatus.Internal
    private byte noticeMark = 0;
    protected boolean highlight = true;
    protected boolean tooltips = true;
    protected boolean background = true;
    protected boolean interactable = true;
    protected boolean interactableFavorites = true;
    protected boolean wasClicked = false;
    private final Rectangle bounds;
    private final OriginalRetainingCyclingList<EntryStack<?>> stacks = new OriginalRetainingCyclingList<>(EntryStack::empty);
    private long lastCycleTime = -1;
    @Nullable
    private Set<UnaryOperator<Tooltip>> tooltipProcessors;
    public Identifier tagMatch;
    public boolean removeTagMatch = true;
    
    private long lastCheckTime = -1;
    private long lastCheckedTime = -1;
    private Display display;
    private Supplier<DisplayTooltipComponent> displayTooltipComponent;
    
    public EntryWidget(Point point) {
        this(new Rectangle(point.x - 1, point.y - 1, 18, 18));
    }
    
    public EntryWidget(Rectangle bounds) {
        this.bounds = bounds;
    }
    
    @Override
    public EntryWidget unmarkInputOrOutput() {
        noticeMark = 0;
        return this;
    }
    
    public EntryWidget markIsInput() {
        noticeMark = 1;
        return this;
    }
    
    public EntryWidget markIsOutput() {
        noticeMark = 2;
        return this;
    }
    
    @Override
    public byte getNoticeMark() {
        return noticeMark;
    }
    
    @Override
    public void setNoticeMark(byte noticeMark) {
        this.noticeMark = noticeMark;
    }
    
    @Override
    public void setInteractable(boolean interactable) {
        interactable(interactable);
    }
    
    @Override
    public boolean isInteractable() {
        return this.interactable;
    }
    
    @Override
    public void setInteractableFavorites(boolean interactableFavorites) {
        interactableFavorites(interactableFavorites);
    }
    
    @Override
    public boolean isInteractableFavorites() {
        return interactableFavorites;
    }
    
    public EntryWidget disableInteractions() {
        return interactable(false);
    }
    
    @Override
    public EntryWidget interactable(boolean b) {
        interactable = b;
        interactableFavorites = interactableFavorites && interactable;
        return this;
    }
    
    public EntryWidget disableFavoritesInteractions() {
        return interactableFavorites(false);
    }
    
    @Override
    public EntryWidget interactableFavorites(boolean b) {
        interactableFavorites = b && interactable;
        return this;
    }
    
    public EntryWidget noHighlight() {
        return highlight(false);
    }
    
    public EntryWidget highlight(boolean b) {
        highlight = b;
        return this;
    }
    
    @Override
    public boolean isHighlightEnabled() {
        return highlight;
    }
    
    @Override
    public void setHighlightEnabled(boolean highlights) {
        highlight(highlights);
    }
    
    public EntryWidget noTooltips() {
        return tooltips(false);
    }
    
    public EntryWidget tooltips(boolean b) {
        tooltips = b;
        return this;
    }
    
    @Override
    public void setTooltipsEnabled(boolean tooltipsEnabled) {
        tooltips(tooltipsEnabled);
    }
    
    @Override
    public boolean isTooltipsEnabled() {
        return tooltips;
    }
    
    public EntryWidget noBackground() {
        return background(false);
    }
    
    public EntryWidget background(boolean b) {
        background = b;
        return this;
    }
    
    @Override
    public void setBackgroundEnabled(boolean backgroundEnabled) {
        background(backgroundEnabled);
    }
    
    @Override
    public boolean isBackgroundEnabled() {
        return background;
    }
    
    @Override
    public Slot clearEntries() {
        this.getCyclingEntries().clear();
        if (removeTagMatch) tagMatch = null;
        return this;
    }
    
    public EntryWidget clearStacks() {
        return (EntryWidget) this.clearEntries();
    }
    
    @Override
    public EntryWidget entry(EntryStack<?> stack) {
        Objects.requireNonNull(stack);
        this.getCyclingEntries().add(stack);
        if (removeTagMatch) tagMatch = null;
        return this;
    }
    
    @Override
    public EntryWidget entries(Collection<? extends EntryStack<?>> stacks) {
        Objects.requireNonNull(stacks);
        this.getCyclingEntries().addAll(stacks);
        if (removeTagMatch) tagMatch = null;
        return this;
    }
    
    @Override
    public Slot withEntriesListener(Consumer<Slot> listener) {
        this.getCyclingEntries().addListener($ -> listener.accept(this));
        return this;
    }
    
    public Slot entries(CyclingList<EntryStack<?>> stacks) {
        this.getCyclingEntries().setBacking(stacks);
        if (removeTagMatch) tagMatch = null;
        return this;
    }
    
    public CyclingList<EntryStack<?>> getBackingCyclingEntries() {
        return this.stacks.getBacking();
    }
    
    public OriginalRetainingCyclingList<EntryStack<?>> getCyclingEntries() {
        return this.stacks;
    }
    
    @Override
    public EntryStack<?> getCurrentEntry() {
        if (this.lastCycleTime == -1) this.lastCycleTime = System.currentTimeMillis();
        if (System.currentTimeMillis() > this.lastCycleTime + getCyclingInterval()) {
            this.lastCycleTime = System.currentTimeMillis();
            this.getCyclingEntries().next();
        }
        return this.getCyclingEntries().peek();
    }
    
    @Override
    public List<EntryStack<?>> getEntries() {
        return getCyclingEntries().get();
    }
    
    protected long getCyclingInterval() {
        return 1000;
    }
    
    @Override
    public Rectangle getBounds() {
        return bounds;
    }
    
    @Override
    public Rectangle getInnerBounds() {
        return new Rectangle(bounds.x + 1, bounds.y + 1, bounds.width - 2, bounds.height - 2);
    }
    
    @Nullable
    private TransferHandler _getTransferHandler() {
        lastCheckTime = Util.getMillis();
        
        if (PluginManager.areAnyReloading()) {
            return null;
        }
        
        try {
            DisplayRegistry displayRegistry = DisplayRegistry.getInstance();
            DisplayCache displayCache = ((DisplayRegistryImpl) displayRegistry).cache();
            CategoryRegistry categoryRegistry = CategoryRegistry.getInstance();
            Map<CategoryIdentifier<?>, Boolean> filteringQuickCraftCategories = ConfigObject.getInstance().getFilteringQuickCraftCategories();
            boolean shouldFilterDisplays = ConfigObject.getInstance().shouldFilterDisplays();
            
            for (Display display : displayCache.getAllDisplaysByOutputs(getEntries())) {
                CategoryIdentifier<?> categoryIdentifier = display.getCategoryIdentifier();
                Optional<? extends CategoryRegistry.CategoryConfiguration<?>> configuration;
                if ((configuration = categoryRegistry.tryGet(categoryIdentifier)).isEmpty()
                        || categoryRegistry.isCategoryInvisible(configuration.get().getCategory())) continue;
                if (!filteringQuickCraftCategories.getOrDefault(categoryIdentifier, configuration.get().isQuickCraftingEnabledByDefault()))
                    
                    continue;
                if ((!shouldFilterDisplays || displayRegistry.isDisplayVisible(display))
                ) {
                    AutoCraftingEvaluator.AutoCraftingResult result = AutoCraftingEvaluator.evaluateAutoCrafting(false, false, display, null);
                    if (result.successful) {
                        this.display = display;
                        this.displayTooltipComponent = Suppliers.memoize(() -> new DisplayTooltipComponent(display));
                        return result.successfulHandler;
                    }
                }
            }
        } catch (ConcurrentModificationException ignored) {
            display = null;
            displayTooltipComponent = null;
            lastCheckTime = -1;
        }
        
        return null;
    }
    
    private TransferHandler getTransferHandler(boolean query) {
        if (PluginManager.areAnyReloading()) {
            return null;
        }
        
        if (display != null) {
            if (ViewsImpl.isRecipesFor(null, getEntries(), display)) {
                AutoCraftingEvaluator.AutoCraftingResult result = AutoCraftingEvaluator.evaluateAutoCrafting(false, false, display, null);
                if (result.successful) {
                    return result.successfulHandler;
                }
            }
            
            display = null;
            displayTooltipComponent = null;
            lastCheckTime = -1;
        }
        
        if (lastCheckTime != -1 && Util.getMillis() - lastCheckTime < 2000) {
            return null;
        }
        
        return query ? _getTransferHandler() : null;
    }
    
    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        drawBackground(graphics, mouseX, mouseY, delta);
        drawCurrentEntry(graphics, mouseX, mouseY, delta);
        
        boolean highlighted = containsMouse(mouseX, mouseY);
        if (hasTooltips() && highlighted) {
            queueTooltip(graphics, mouseX, mouseY, delta);
        }
        if (hasHighlight() && highlighted) {
            drawHighlighted(graphics, mouseX, mouseY, delta);
        }
        drawExtra(graphics, mouseX, mouseY, delta);
    }
    
    public final boolean hasTooltips() {
        return isTooltipsEnabled();
    }
    
    public final boolean hasHighlight() {
        return isHighlightEnabled();
    }
    
    private NumberAnimator<Float> darkBackgroundAlpha = ValueAnimator.ofFloat()
            .withConvention(() -> REIRuntime.getInstance().isDarkThemeEnabled() ? 1.0F : 0.0F, ValueAnimator.typicalTransitionTime())
            .asFloat();
    
    @ApiStatus.Internal
    public void setDarkBackgroundAlpha(NumberAnimator<Float> darkBackgroundAlpha) {
        this.darkBackgroundAlpha = darkBackgroundAlpha;
    }
    
    protected void drawBackground(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        if (background) {
            darkBackgroundAlpha.update(delta);
            graphics.blit(RenderPipelines.GUI_TEXTURED, InternalTextures.CHEST_GUI_TEXTURE, bounds.x, bounds.y, 0, 222, bounds.width, bounds.height, 256, 256);
            if (darkBackgroundAlpha.value() > 0.0F) {
                graphics.blit(RenderPipelines.GUI_TEXTURED, InternalTextures.CHEST_GUI_TEXTURE_DARK, bounds.x, bounds.y, 0, 222, bounds.width, bounds.height, 256, 256, ARGB.white(darkBackgroundAlpha.value()));
            }
        }
    }
    
    protected void drawCurrentEntry(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        EntryStack<?> entry = getCurrentEntry();
        entry.render(graphics, getInnerBounds(), mouseX, mouseY, delta);
    }
    
    protected void queueTooltip(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        Tooltip tooltip = getCurrentTooltip(TooltipContext.ofMouse(Item.TooltipContext.of(minecraft.level)));
        if (tooltip != null) {
            tooltip.queue();
        }
    }
    
    protected void drawExtra(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
    }
    
    @Override
    @Nullable
    public Tooltip getCurrentTooltip(TooltipContext context) {
        Tooltip tooltip = getCurrentEntry().getTooltip(context);
        
        if (tooltip != null && !(Minecraft.getInstance().screen instanceof DisplayScreen)) {
            boolean exists = getTransferHandler(false) != null;
            
            if (!exists) {
                if (lastCheckedTime == -1 || Util.getMillis() - lastCheckedTime > 400) {
                    lastCheckedTime = Util.getMillis();
                }
                
                if (Util.getMillis() - lastCheckedTime > 200) {
                    lastCheckedTime = -1;
                    exists = getTransferHandler(true) != null;
                }
            } else {
                lastCheckedTime = -1;
            }
            
            if (exists) {
                tooltip.add(Component.translatable("text.auto_craft.move_items.tooltip").withStyle(ChatFormatting.YELLOW));
                tooltip.add(displayTooltipComponent.get());
            }
        }
        
        if (tooltip != null) {
            if (interactableFavorites && ConfigObject.getInstance().doDisplayFavoritesTooltip() && !ConfigObject.getInstance().getFavoriteKeyCode().isUnknown()) {
                String name = ConfigObject.getInstance().getFavoriteKeyCode().getLocalizedName().getString();
                if (reverseFavoritesAction())
                    tooltip.addAllTexts(Stream.of(I18n.get("text.rei.remove_favorites_tooltip", name).split("\n"))
                            .map(Component::literal).collect(Collectors.toList()));
                else
                    tooltip.addAllTexts(Stream.of(I18n.get("text.rei.favorites_tooltip", name).split("\n"))
                            .map(Component::literal).collect(Collectors.toList()));
            }
            
            if (tooltipProcessors != null) {
                for (UnaryOperator<Tooltip> processor : tooltipProcessors) {
                    tooltip = processor.apply(tooltip);
                }
            }
        }
        
        if (tooltip != null) {
            if (interactableFavorites && ConfigObject.getInstance().doDisplayFavoritesTooltip() && !ConfigObject.getInstance().getFavoriteKeyCode().isUnknown()) {
                String name = ConfigObject.getInstance().getFavoriteKeyCode().getLocalizedName().getString();
                if (reverseFavoritesAction())
                    tooltip.addAllTexts(Stream.of(I18n.get("text.rei.remove_favorites_tooltip", name).split("\n"))
                            .map(Component::literal).collect(Collectors.toList()));
                else
                    tooltip.addAllTexts(Stream.of(I18n.get("text.rei.favorites_tooltip", name).split("\n"))
                            .map(Component::literal).collect(Collectors.toList()));
            }
            
            if (tooltipProcessors != null) {
                for (UnaryOperator<Tooltip> processor : tooltipProcessors) {
                    tooltip = processor.apply(tooltip);
                }
            }
            
            if (!tooltip.entries().isEmpty() && ConfigObject.getInstance().doDisplayIMEHints()) {
                Tooltip.Entry entry = tooltip.entries().get(0);
                
                if (entry.isText()) {
                    String name = FormattingUtils.stripFormatting(entry.getAsText().getString());
                    InputMethod<?> active = InputMethod.active();
                    String suggested = active.suggestInputString(name);
                    if (suggested != null) {
                        tooltip.entries().add(1, Tooltip.entry(Component.literal(suggested).withStyle(ChatFormatting.GRAY)));
                    }
                }
            }
        }
        
        return tooltip;
    }
    
    private final NumberAnimator<Float> darkHighlightedAlpha = ValueAnimator.ofFloat()
            .withConvention(() -> REIRuntime.getInstance().isDarkThemeEnabled() ? 1.0F : 0.0F, ValueAnimator.typicalTransitionTime())
            .asFloat();
    
    protected void drawHighlighted(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        darkHighlightedAlpha.update(delta);
        Rectangle bounds = getInnerBounds();
        graphics.fillGradient(bounds.x, bounds.y, bounds.getMaxX(), bounds.getMaxY(), 0x80ffffff, 0x80ffffff);
        int darkColor = 0x111111 | ((int) (90 * darkHighlightedAlpha.value()) << 24);
        graphics.fillGradient(bounds.x, bounds.y, bounds.getMaxX(), bounds.getMaxY(), darkColor, darkColor);
    }
    
    @Override
    public List<? extends GuiEventListener> children() {
        return Collections.emptyList();
    }
    
    protected boolean wasClicked() {
        boolean b = this.wasClicked;
        this.wasClicked = false;
        return b;
    }
    
    public void tooltipProcessor(UnaryOperator<Tooltip> operator) {
        if (tooltipProcessors == null) {
            tooltipProcessors = Collections.singleton(operator);
        } else {
            if (!(tooltipProcessors instanceof LinkedHashSet)) {
                tooltipProcessors = new LinkedHashSet<>(tooltipProcessors);
            }
            tooltipProcessors.add(operator);
        }
    }
    
    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (containsMouse(event.x(), event.y())) {
            this.wasClicked = true;
            return true;
        }
        
        return super.mouseClicked(event, doubleClick);
    }
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amountX, double amountY) {
        if (REIRuntimeImpl.isWithinRecipeViewingScreen && this.getCyclingEntries().get().size() > 1 && containsMouse(mouseX, mouseY)) {
            if (amountY < 0) {
                for (EntryWidget slot : Widgets.<EntryWidget>walk(minecraft.screen.children(), EntryWidget.class::isInstance)) {
                    slot.getCyclingEntries().previous();
                    slot.lastCycleTime = System.currentTimeMillis();
                }
                return true;
            } else if (amountY > 0) {
                for (EntryWidget slot : Widgets.<EntryWidget>walk(minecraft.screen.children(), EntryWidget.class::isInstance)) {
                    slot.getCyclingEntries().next();
                    slot.lastCycleTime = System.currentTimeMillis();
                }
                return true;
            }
        }
        return false;
    }
    
    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (!interactable)
            return false;
        if (wasClicked() && containsMouse(event.x(), event.y())) {
            if (doAction(event)) {
                ((CurrentDraggingStack) DraggingContext.getInstance()).drop();
                return true;
            }
        }
        return false;
    }
    
    protected boolean doAction(MouseButtonEvent event) {
        if (interactableFavorites && ConfigObject.getInstance().isFavoritesEnabled() && !getCurrentEntry().isEmpty()) {
            ModifierKeyCode keyCode = ConfigObject.getInstance().getFavoriteKeyCode();
            if (keyCode.matchesMouse(event.button())) {
                FavoriteEntry favoriteEntry = asFavoriteEntry();
                if (favoriteEntry != null) {
                    if (reverseFavoritesAction()) {
                        ConfigObject.getInstance().getFavoriteEntries().remove(favoriteEntry);
                    } else {
                        ConfigObject.getInstance().getFavoriteEntries().add(favoriteEntry);
                    }
                    return true;
                }
            }
        }
        
        if (!(Minecraft.getInstance().screen instanceof DisplayScreen) && Minecraft.getInstance().hasControlDown()) {
            try {
                TransferHandler handler = getTransferHandler(true);
                
                if (handler != null) {
                    AbstractContainerScreen<?> containerScreen = REIRuntime.getInstance().getPreviousContainerScreen();
                    TransferHandler.Context context = TransferHandler.Context.create(true, Minecraft.getInstance().hasShiftDown() || event.button() == 1, containerScreen, display);
                    TransferHandler.ApplicabilityResult applicabilityResult = handler.checkApplicable(context);
                    if (!applicabilityResult.isApplicable()) return false;
                    TransferHandler.Result transferResult;
                    
                    if (applicabilityResult.isSuccessful()) {
                        transferResult = handler.handle(context);
                    } else {
                        transferResult = applicabilityResult.getError();
                    }
                    
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
        
        if ((ConfigObject.getInstance().getRecipeKeybind().getType() != InputConstants.Type.MOUSE && event.button() == 0) || ConfigObject.getInstance().getRecipeKeybind().matchesMouse(event.button()))
            return ViewSearchBuilder.builder().addRecipesFor(getCurrentEntry()).open();
        else if ((ConfigObject.getInstance().getUsageKeybind().getType() != InputConstants.Type.MOUSE && event.button() == 1) || ConfigObject.getInstance().getUsageKeybind().matchesMouse(event.button()))
            return ViewSearchBuilder.builder().addUsagesFor(getCurrentEntry()).open();
        
        return false;
    }
    
    @ApiStatus.Internal
    @Nullable
    protected FavoriteEntry asFavoriteEntry() {
        FavoriteEntry entry = FavoriteEntry.fromEntryStack(getCurrentEntry().normalize());
        return entry.isInvalid() ? null : entry;
    }
    
    @ApiStatus.Internal
    public boolean cancelDeleteItems(EntryStack<?> stack) {
        return false;
    }
    
    protected boolean reverseFavoritesAction() {
        return false;
    }
    
    @Override
    public boolean containsMouse(double mouseX, double mouseY) {
        return mouseX >= bounds.x + 1 && mouseY >= bounds.y + 1 && mouseX <= bounds.getMaxX() - 1 && mouseY <= bounds.getMaxY() - 1;
    }
    
    @Override
    public boolean keyPressed(KeyEvent event) {
        if (containsMouse(mouse())) {
            return keyPressedIgnoreContains(event);
        }
        
        return false;
    }
    
    public boolean keyPressedIgnoreContains(KeyEvent event) {
        if (!interactable) return false;
        
        if (interactableFavorites && ConfigObject.getInstance().isFavoritesEnabled() && !getCurrentEntry().isEmpty()) {
            if (ConfigObject.getInstance().getFavoriteKeyCode().matchesKey(event.key(), event.scancode())) {
                FavoriteEntry favoriteEntry = asFavoriteEntry();
                if (favoriteEntry != null) {
                    if (reverseFavoritesAction()) {
                        ConfigObject.getInstance().getFavoriteEntries().remove(favoriteEntry);
                    } else {
                        ConfigObject.getInstance().getFavoriteEntries().add(favoriteEntry);
                    }
                    return true;
                }
            }
        }
        if (ConfigObject.getInstance().getRecipeKeybind().matchesKey(event.key(), event.scancode()))
            return ViewSearchBuilder.builder().addRecipesFor(getCurrentEntry()).open();
        else if (ConfigObject.getInstance().getUsageKeybind().matchesKey(event.key(), event.scancode()))
            return ViewSearchBuilder.builder().addUsagesFor(getCurrentEntry()).open();
        return false;
    }
    
    @Override
    @Nullable
    public DraggableStack getHoveredStack(DraggingContext<Screen> context, double mouseX, double mouseY) {
        if (!getCurrentEntry().isEmpty() && containsMouse(mouseX, mouseY)) {
            return new DraggableStack() {
                final EntryStack<?> stack = getCurrentEntry().copy()
                        .removeSetting(EntryStack.Settings.RENDERER)
                        .removeSetting(EntryStack.Settings.FLUID_RENDER_RATIO);
                
                @Override
                public EntryStack<?> getStack() {
                    return stack;
                }
                
                @Override
                public void drag() {
                }
                
                @Override
                public void release(DraggedAcceptorResult result) {
                    if (result == DraggedAcceptorResult.PASS) {
                        context.renderBackToPosition(this, DraggingContext.getInstance().getCurrentPosition(), () -> new Point(getBounds().x, getBounds().y));
                    }
                }
            };
        }
        return null;
    }
    
    @Override
    @Deprecated
    public void render(GuiGraphics graphics, Rectangle bounds, int mouseX, int mouseY, float delta) {
        Rectangle clone = getBounds().clone();
        getBounds().setBounds(bounds.x - 1, bounds.y - 1, bounds.width + 2, bounds.height + 2);
        render(graphics, mouseX, mouseY, delta);
        getBounds().setBounds(clone);
    }
    
    @Override
    public void fillCrashReport(CrashReport report, CrashReportCategory category) {
        super.fillCrashReport(report, category);
        category.setDetail("Notice mark", () -> String.valueOf(getNoticeMark()));
        category.setDetail("Interactable", () -> String.valueOf(isInteractable()));
        category.setDetail("Interactable favorites", () -> String.valueOf(isInteractableFavorites()));
        category.setDetail("Highlight enabled", () -> String.valueOf(isHighlightEnabled()));
        category.setDetail("Tooltip enabled", () -> String.valueOf(isTooltipsEnabled()));
        category.setDetail("Background enabled", () -> String.valueOf(isBackgroundEnabled()));
        category.setDetail("Entries count", () -> String.valueOf(getEntries().size()));
        EntryStack<?> currentEntry = getCurrentEntry();
        
        CrashReportCategory entryCategory = report.addCategory("Current Rendering Entry");
        currentEntry.fillCrashReport(report, entryCategory);
    }
}
