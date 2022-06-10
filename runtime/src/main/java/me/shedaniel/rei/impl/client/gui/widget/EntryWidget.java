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

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import me.shedaniel.clothconfig2.api.ModifierKeyCode;
import me.shedaniel.clothconfig2.api.animator.NumberAnimator;
import me.shedaniel.clothconfig2.api.animator.ValueAnimator;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.REIRuntime;
import me.shedaniel.rei.api.client.config.ConfigManager;
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
import me.shedaniel.rei.api.client.overlay.ScreenOverlay;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.client.registry.transfer.TransferHandler;
import me.shedaniel.rei.api.client.view.ViewSearchBuilder;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.plugins.PluginManager;
import me.shedaniel.rei.impl.client.REIRuntimeImpl;
import me.shedaniel.rei.impl.client.gui.ScreenOverlayImpl;
import me.shedaniel.rei.impl.client.gui.widget.favorites.FavoritesEntriesManager;
import me.shedaniel.rei.impl.client.gui.widget.favorites.FavoritesListWidget;
import me.shedaniel.rei.impl.client.view.ViewsImpl;
import net.minecraft.ChatFormatting;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EntryWidget extends Slot implements DraggableStackProviderWidget {
    @ApiStatus.Internal
    public static long stackDisplayOffset = 0;
    protected static final ResourceLocation RECIPE_GUI = new ResourceLocation("roughlyenoughitems", "textures/gui/recipecontainer.png");
    protected static final ResourceLocation RECIPE_GUI_DARK = new ResourceLocation("roughlyenoughitems", "textures/gui/recipecontainer_dark.png");
    
    @ApiStatus.Internal
    private byte noticeMark = 0;
    protected boolean highlight = true;
    protected boolean tooltips = true;
    protected boolean background = true;
    protected boolean interactable = true;
    protected boolean interactableFavorites = true;
    protected boolean wasClicked = false;
    private Rectangle bounds;
    private List<EntryStack<?>> entryStacks;
    @Nullable
    private Set<UnaryOperator<Tooltip>> tooltipProcessors;
    public ResourceLocation tagMatch;
    public boolean removeTagMatch = true;
    
    private long lastCheckTime = -1;
    private Display display;
    
    public EntryWidget(Point point) {
        this(new Rectangle(point.x - 1, point.y - 1, 18, 18));
    }
    
    public EntryWidget(Rectangle bounds) {
        this.bounds = bounds;
        this.entryStacks = Collections.emptyList();
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
    
    public EntryWidget clearStacks() {
        entryStacks = Collections.emptyList();
        return this;
    }
    
    @Override
    public Slot clearEntries() {
        return clearStacks();
    }
    
    @Override
    public EntryWidget entry(EntryStack<?> stack) {
        Objects.requireNonNull(stack);
        if (entryStacks.isEmpty()) {
            entryStacks = Collections.singletonList(stack);
        } else {
            if (!(entryStacks instanceof ArrayList)) {
                entryStacks = new ArrayList<>(entryStacks);
            }
            entryStacks.add(stack);
        }
        if (removeTagMatch) tagMatch = null;
        return this;
    }
    
    @Override
    public EntryWidget entries(Collection<? extends EntryStack<?>> stacks) {
        if (!stacks.isEmpty()) {
            if (!(entryStacks instanceof ArrayList)) {
                entryStacks = new ArrayList<>(entryStacks);
            }
            entryStacks.addAll(stacks);
            if (removeTagMatch) tagMatch = null;
        }
        return this;
    }
    
    @Override
    public EntryStack<?> getCurrentEntry() {
        int size = entryStacks.size();
        if (size == 0)
            return EntryStack.empty();
        if (size == 1)
            return entryStacks.get(0);
        return entryStacks.get(Mth.floor(((System.currentTimeMillis() + stackDisplayOffset) / 1000 % (double) size)));
    }
    
    @Override
    public List<EntryStack<?>> getEntries() {
        return entryStacks;
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
    
    private TransferHandler getTransferHandler() {
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
    
    @Override
    public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
        drawBackground(matrices, mouseX, mouseY, delta);
        drawCurrentEntry(matrices, mouseX, mouseY, delta);
        
        boolean highlighted = containsMouse(mouseX, mouseY);
        if (hasTooltips() && highlighted) {
            queueTooltip(matrices, mouseX, mouseY, delta);
        }
        if (hasHighlight() && highlighted) {
            drawHighlighted(matrices, mouseX, mouseY, delta);
        }
        drawExtra(matrices, mouseX, mouseY, delta);
    }
    
    public final boolean hasTooltips() {
        return isTooltipsEnabled();
    }
    
    public final boolean hasHighlight() {
        return isHighlightEnabled();
    }
    
    private final NumberAnimator<Float> darkBackgroundAlpha = ValueAnimator.ofFloat()
            .withConvention(() -> REIRuntime.getInstance().isDarkThemeEnabled() ? 1.0F : 0.0F, ValueAnimator.typicalTransitionTime())
            .asFloat();
    
    protected void drawBackground(PoseStack matrices, int mouseX, int mouseY, float delta) {
        if (background) {
            darkBackgroundAlpha.update(delta);
            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(770, 771, 1, 0);
            RenderSystem.blendFunc(770, 771);
            RenderSystem.setShaderTexture(0, RECIPE_GUI);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            blit(matrices, bounds.x, bounds.y, 0, 222, bounds.width, bounds.height);
            if (darkBackgroundAlpha.value() > 0.0F) {
                RenderSystem.setShaderTexture(0, RECIPE_GUI_DARK);
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, darkBackgroundAlpha.value());
                blit(matrices, bounds.x, bounds.y, 0, 222, bounds.width, bounds.height);
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            }
        }
    }
    
    protected void drawCurrentEntry(PoseStack matrices, int mouseX, int mouseY, float delta) {
        EntryStack<?> entry = getCurrentEntry();
        entry.setZ(100);
        entry.render(matrices, getInnerBounds(), mouseX, mouseY, delta);
    }
    
    protected void queueTooltip(PoseStack matrices, int mouseX, int mouseY, float delta) {
        Tooltip tooltip = getCurrentTooltip(TooltipContext.of(new Point(mouseX, mouseY)));
        if (tooltip != null) {
            tooltip.queue();
        }
    }
    
    protected void drawExtra(PoseStack matrices, int mouseX, int mouseY, float delta) {}
    
    @Override
    @Nullable
    public Tooltip getCurrentTooltip(TooltipContext context) {
        Tooltip tooltip = getCurrentEntry().getTooltip(context);
        
        if (tooltip != null && getTransferHandler() != null
            && !(Minecraft.getInstance().screen instanceof DisplayScreen)) {
            tooltip.add(Component.translatable("text.auto_craft.move_items.tooltip").withStyle(ChatFormatting.YELLOW));
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
                            .map(TextComponent::new).collect(Collectors.toList()));
                else
                    tooltip.addAllTexts(Stream.of(I18n.get("text.rei.favorites_tooltip", name).split("\n"))
                            .map(TextComponent::new).collect(Collectors.toList()));
            }
            
            if (tooltipProcessors != null) {
                for (UnaryOperator<Tooltip> processor : tooltipProcessors) {
                    tooltip = processor.apply(tooltip);
                }
            }
        }
        
        return tooltip;
    }
    
    private final NumberAnimator<Float> darkHighlightedAlpha = ValueAnimator.ofFloat()
            .withConvention(() -> REIRuntime.getInstance().isDarkThemeEnabled() ? 1.0F : 0.0F, ValueAnimator.typicalTransitionTime())
            .asFloat();
    
    protected void drawHighlighted(PoseStack matrices, int mouseX, int mouseY, float delta) {
        darkHighlightedAlpha.update(delta);
        RenderSystem.disableDepthTest();
        RenderSystem.colorMask(true, true, true, false);
        Rectangle bounds = getInnerBounds();
        fillGradient(matrices, bounds.x, bounds.y, bounds.getMaxX(), bounds.getMaxY(), 0x80ffffff, 0x80ffffff);
        int darkColor = 0x111111 | ((int) (90 * darkHighlightedAlpha.value()) << 24);
        fillGradient(matrices, bounds.x, bounds.y, bounds.getMaxX(), bounds.getMaxY(), darkColor, darkColor);
        RenderSystem.colorMask(true, true, true, true);
        RenderSystem.enableDepthTest();
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
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (containsMouse(mouseX, mouseY))
            this.wasClicked = true;
        return super.mouseClicked(mouseX, mouseY, button);
    }
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (REIRuntimeImpl.isWithinRecipeViewingScreen && entryStacks.size() > 1 && containsMouse(mouseX, mouseY)) {
            if (amount < 0) {
                EntryWidget.stackDisplayOffset = ((System.currentTimeMillis() + stackDisplayOffset) / 1000 - 1) * 1000;
                return true;
            } else if (amount > 0) {
                EntryWidget.stackDisplayOffset = ((System.currentTimeMillis() + stackDisplayOffset) / 1000 + 1) * 1000;
                return true;
            }
        }
        return false;
    }
    
    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (!interactable)
            return false;
        if (wasClicked() && containsMouse(mouseX, mouseY)) {
            if (doAction(mouseX, mouseY, button)) {
                return true;
            }
        }
        return false;
    }
    
    protected boolean doAction(double mouseX, double mouseY, int button) {
        if (interactableFavorites && ConfigObject.getInstance().isFavoritesEnabled() && !getCurrentEntry().isEmpty()) {
            ModifierKeyCode keyCode = ConfigObject.getInstance().getFavoriteKeyCode();
            if (keyCode.matchesMouse(button)) {
                FavoriteEntry favoriteEntry = asFavoriteEntry();
                if (favoriteEntry != null) {
                    if (reverseFavoritesAction()) {
                        FavoritesEntriesManager.INSTANCE.remove(favoriteEntry);
                    } else {
                        FavoritesEntriesManager.INSTANCE.add(favoriteEntry);
                    }
                    ConfigManager.getInstance().saveConfig();
                    FavoritesListWidget favoritesListWidget = ScreenOverlayImpl.getFavoritesListWidget();
                    if (favoritesListWidget != null)
                        favoritesListWidget.updateSearch();
                    return true;
                }
            }
        }
        
        if (!(Minecraft.getInstance().screen instanceof DisplayScreen) && Screen.hasControlDown()) {
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
        
        if ((ConfigObject.getInstance().getRecipeKeybind().getType() != InputConstants.Type.MOUSE && button == 0) || ConfigObject.getInstance().getRecipeKeybind().matchesMouse(button))
            return ViewSearchBuilder.builder().addRecipesFor(getCurrentEntry()).open();
        else if ((ConfigObject.getInstance().getUsageKeybind().getType() != InputConstants.Type.MOUSE && button == 1) || ConfigObject.getInstance().getUsageKeybind().matchesMouse(button))
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
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (containsMouse(mouse())) {
            return keyPressedIgnoreContains(keyCode, scanCode, modifiers);
        }
        
        return false;
    }
    
    public boolean keyPressedIgnoreContains(int keyCode, int scanCode, int modifiers) {
        if (!interactable) return false;
        
        if (interactableFavorites && ConfigObject.getInstance().isFavoritesEnabled() && !getCurrentEntry().isEmpty()) {
            if (ConfigObject.getInstance().getFavoriteKeyCode().matchesKey(keyCode, scanCode)) {
                FavoriteEntry favoriteEntry = asFavoriteEntry();
                if (favoriteEntry != null) {
                    if (reverseFavoritesAction()) {
                        FavoritesEntriesManager.INSTANCE.remove(favoriteEntry);
                    } else {
                        FavoritesEntriesManager.INSTANCE.add(favoriteEntry);
                    }
                    ConfigManager.getInstance().saveConfig();
                    FavoritesListWidget favoritesListWidget = ScreenOverlayImpl.getFavoritesListWidget();
                    if (favoritesListWidget != null)
                        favoritesListWidget.updateSearch();
                    return true;
                }
            }
        }
        if (ConfigObject.getInstance().getRecipeKeybind().matchesKey(keyCode, scanCode))
            return ViewSearchBuilder.builder().addRecipesFor(getCurrentEntry()).open();
        else if (ConfigObject.getInstance().getUsageKeybind().matchesKey(keyCode, scanCode))
            return ViewSearchBuilder.builder().addUsagesFor(getCurrentEntry()).open();
        return false;
    }
    
    @Override
    @Nullable
    public DraggableStack getHoveredStack(DraggingContext<Screen> context, double mouseX, double mouseY) {
        if (!getCurrentEntry().isEmpty() && containsMouse(mouseX, mouseY)) {
            return new DraggableStack() {
                EntryStack<?> stack = getCurrentEntry().copy()
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
    public void render(PoseStack matrices, Rectangle bounds, int mouseX, int mouseY, float delta) {
        Rectangle clone = getBounds().clone();
        getBounds().setBounds(bounds.x - 1, bounds.y - 1, bounds.width + 2, bounds.height + 2);
        render(matrices, mouseX, mouseY, delta);
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
        category.setDetail("Entries count", () -> String.valueOf(entryStacks.size()));
        EntryStack<?> currentEntry = getCurrentEntry();
        
        CrashReportCategory entryCategory = report.addCategory("Current Rendering Entry");
        currentEntry.fillCrashReport(report, entryCategory);
    }
}
