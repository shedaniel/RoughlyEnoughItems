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

package me.shedaniel.rei.impl.client.gui;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector4f;
import dev.architectury.injectables.annotations.ExpectPlatform;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.math.impl.PointHelper;
import me.shedaniel.rei.api.client.ClientHelper;
import me.shedaniel.rei.api.client.REIRuntime;
import me.shedaniel.rei.api.client.config.ConfigManager;
import me.shedaniel.rei.api.client.config.ConfigObject;
import me.shedaniel.rei.api.client.favorites.FavoriteEntry;
import me.shedaniel.rei.api.client.gui.config.DisplayPanelLocation;
import me.shedaniel.rei.api.client.gui.config.SearchFieldLocation;
import me.shedaniel.rei.api.client.gui.config.SyntaxHighlightingMode;
import me.shedaniel.rei.api.client.gui.drag.DraggingContext;
import me.shedaniel.rei.api.client.gui.drag.component.DraggableComponentProvider;
import me.shedaniel.rei.api.client.gui.drag.component.DraggableComponentVisitor;
import me.shedaniel.rei.api.client.gui.screen.DisplayScreen;
import me.shedaniel.rei.api.client.gui.widgets.Button;
import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.overlay.OverlayListWidget;
import me.shedaniel.rei.api.client.overlay.ScreenOverlay;
import me.shedaniel.rei.api.client.registry.screen.ClickArea;
import me.shedaniel.rei.api.client.registry.screen.OverlayDecider;
import me.shedaniel.rei.api.client.registry.screen.ScreenRegistry;
import me.shedaniel.rei.api.client.view.ViewSearchBuilder;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.plugins.PluginManager;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.shedaniel.rei.impl.client.ClientHelperImpl;
import me.shedaniel.rei.impl.client.REIRuntimeImpl;
import me.shedaniel.rei.impl.client.config.ConfigManagerImpl;
import me.shedaniel.rei.impl.client.config.ConfigObjectImpl;
import me.shedaniel.rei.impl.client.gui.changelog.ChangelogLoader;
import me.shedaniel.rei.impl.client.gui.craftable.CraftableFilter;
import me.shedaniel.rei.impl.client.gui.dragging.CurrentDraggingStack;
import me.shedaniel.rei.impl.client.gui.modules.Menu;
import me.shedaniel.rei.impl.client.gui.modules.MenuEntry;
import me.shedaniel.rei.impl.client.gui.modules.entries.*;
import me.shedaniel.rei.impl.client.gui.widget.DefaultDisplayChoosePageWidget;
import me.shedaniel.rei.impl.client.gui.widget.InternalWidgets;
import me.shedaniel.rei.impl.client.gui.widget.LateRenderable;
import me.shedaniel.rei.impl.client.gui.widget.entrylist.EntryListSearchManager;
import me.shedaniel.rei.impl.client.gui.widget.entrylist.EntryListWidget;
import me.shedaniel.rei.impl.client.gui.widget.entrylist.PaginatedEntryListWidget;
import me.shedaniel.rei.impl.client.gui.widget.entrylist.ScrolledEntryListWidget;
import me.shedaniel.rei.impl.client.gui.widget.favorites.FavoritesListWidget;
import me.shedaniel.rei.impl.client.gui.widget.search.OverlaySearchField;
import me.shedaniel.rei.impl.common.util.Weather;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static me.shedaniel.rei.impl.client.gui.widget.entrylist.EntryListWidget.entrySize;

@ApiStatus.Internal
public class ScreenOverlayImpl extends ScreenOverlay {
    private static final ResourceLocation CHEST_GUI_TEXTURE = new ResourceLocation("roughlyenoughitems", "textures/gui/recipecontainer.png");
    public static final ResourceLocation ARROW_LEFT_TEXTURE = new ResourceLocation("roughlyenoughitems", "textures/gui/arrow_left.png");
    public static final ResourceLocation ARROW_RIGHT_TEXTURE = new ResourceLocation("roughlyenoughitems", "textures/gui/arrow_right.png");
    public static final ResourceLocation ARROW_LEFT_SMALL_TEXTURE = new ResourceLocation("roughlyenoughitems", "textures/gui/arrow_left_small.png");
    public static final ResourceLocation ARROW_RIGHT_SMALL_TEXTURE = new ResourceLocation("roughlyenoughitems", "textures/gui/arrow_right_small.png");
    private static final List<Tooltip> TOOLTIPS = Lists.newArrayList();
    private static final List<Runnable> AFTER_RENDER = Lists.newArrayList();
    private static EntryListWidget entryListWidget = null;
    private static FavoritesListWidget favoritesListWidget = null;
    private final List<Widget> widgets = Lists.newLinkedList();
    public boolean shouldReload = false;
    public boolean shouldReloadSearch = false;
    private Rectangle screenBounds;
    private Rectangle bounds;
    private Window window;
    private Button leftButton, rightButton;
    private Widget configButton;
    private CurrentDraggingStack draggingStack = new CurrentDraggingStack();
    @Nullable
    public DefaultDisplayChoosePageWidget choosePageWidget;
    
    @Nullable
    private ScreenOverlayImpl.OverlayMenu overlayMenu = null;
    
    
    public static EntryListWidget getEntryListWidget() {
        if (entryListWidget != null) {
            if (ConfigObject.getInstance().isEntryListWidgetScrolled() && entryListWidget instanceof ScrolledEntryListWidget) {
                return entryListWidget;
            } else if (!ConfigObject.getInstance().isEntryListWidgetScrolled() && entryListWidget instanceof PaginatedEntryListWidget) {
                return entryListWidget;
            }
        }
        
        if (ConfigObject.getInstance().isEntryListWidgetScrolled()) {
            entryListWidget = new ScrolledEntryListWidget();
        } else if (!ConfigObject.getInstance().isEntryListWidgetScrolled()) {
            entryListWidget = new PaginatedEntryListWidget();
        }
        
        Rectangle overlayBounds = ScreenOverlayImpl.getInstance().bounds;
        entryListWidget.updateArea(Objects.requireNonNullElse(overlayBounds, new Rectangle()), REIRuntimeImpl.getSearchField() == null ? "" : REIRuntimeImpl.getSearchField().getText());
        entryListWidget.updateEntriesPosition();
        
        return entryListWidget;
    }
    
    @Nullable
    public static FavoritesListWidget getFavoritesListWidget() {
        return favoritesListWidget;
    }
    
    public static ScreenOverlayImpl getInstance() {
        return (ScreenOverlayImpl) REIRuntime.getInstance().getOverlay().get();
    }
    
    public void tick() {
        if (REIRuntimeImpl.getSearchField() != null) {
            REIRuntimeImpl.getSearchField().tick();
            if (Minecraft.getInstance().player != null && !PluginManager.areAnyReloading() && Minecraft.getInstance().player.tickCount % 5 == 0) {
                CraftableFilter.INSTANCE.tick();
            }
        }
    }
    
    private static class OverlayMenu {
        private UUID uuid;
        private Menu menu;
        private Widget wrappedMenu;
        private Predicate<Point> inBounds;
        
        public OverlayMenu(UUID uuid, Menu menu, Widget wrappedMenu, Predicate<Point> or, Predicate<Point> and) {
            this.uuid = uuid;
            this.menu = menu;
            this.wrappedMenu = wrappedMenu;
            this.inBounds = or.or(menu::containsMouse).and(and);
        }
    }
    
    public boolean isMenuOpened(UUID uuid) {
        return overlayMenu != null && overlayMenu.uuid.equals(uuid);
    }
    
    public boolean isAnyMenuOpened() {
        return overlayMenu != null;
    }
    
    public boolean isMenuInBounds(UUID uuid) {
        return isMenuOpened(uuid) && overlayMenu.inBounds.test(PointHelper.ofMouse());
    }
    
    private void proceedOpenMenu(UUID uuid, Runnable runnable) {
        proceedOpenMenuOrElse(uuid, runnable, menu -> {});
    }
    
    private void proceedOpenMenuOrElse(UUID uuid, Runnable runnable, Consumer<OverlayMenu> orElse) {
        if (overlayMenu == null || !overlayMenu.uuid.equals(uuid)) {
            closeOverlayMenu();
            runnable.run();
        } else {
            orElse.accept(this.overlayMenu);
        }
    }
    
    public void openMenu(UUID uuid, Menu menu) {
        openMenu(uuid, menu, point -> false, point -> true);
    }
    
    public void openMenu(UUID uuid, Menu menu, Predicate<Point> or, Predicate<Point> and) {
        this.overlayMenu = new OverlayMenu(uuid, menu, Widgets.withTranslate(menu, 0, 0, 400), or, and);
    }
    
    @ApiStatus.Internal
    @Nullable
    public Menu getOverlayMenu() {
        if (isMenuOpened(Menu.SUBSETS))
            return this.overlayMenu.menu;
        throw new IllegalStateException("Subsets menu accessed when subsets are not opened!");
    }
    
    @ApiStatus.Internal
    @Override
    public void closeOverlayMenu() {
        OverlayMenu tmpOverlayMenu = this.overlayMenu;
        if (tmpOverlayMenu != null)
            AFTER_RENDER.add(() -> this.widgets.remove(tmpOverlayMenu.wrappedMenu));
        this.overlayMenu = null;
    }
    
    @Override
    public void queueReloadOverlay() {
        shouldReload = true;
    }
    
    public void queueReloadSearch() {
        shouldReloadSearch = true;
    }
    
    @Override
    public DraggingContext<?> getDraggingContext() {
        return draggingStack;
    }
    
    protected boolean hasSpace() {
        return !this.bounds.isEmpty();
    }
    
    public void init(boolean useless) {
        init();
    }
    
    public void init() {
        draggingStack.set(DraggableComponentProvider.from(ScreenRegistry.getInstance()::getDraggableComponentProviders),
                DraggableComponentVisitor.from(ScreenRegistry.getInstance()::getDraggableComponentVisitors));
        
        this.shouldReload = false;
        this.shouldReloadSearch = false;
        //Update Variables
        this.children().clear();
        this.window = Minecraft.getInstance().getWindow();
        this.screenBounds = ScreenRegistry.getInstance().getScreenBounds(Minecraft.getInstance().screen);
        this.bounds = calculateOverlayBounds();
        if (ConfigObject.getInstance().isFavoritesEnabled()) {
            if (favoritesListWidget == null) {
                favoritesListWidget = new FavoritesListWidget();
            }
            favoritesListWidget.favoritePanel.resetRows();
            widgets.add(favoritesListWidget);
        }
        getEntryListWidget().updateArea(this.bounds, REIRuntimeImpl.getSearchField() == null ? "" : REIRuntimeImpl.getSearchField().getText());
        widgets.add(getEntryListWidget());
        REIRuntimeImpl.getSearchField().getBounds().setBounds(getSearchFieldArea());
        this.widgets.add(REIRuntimeImpl.getSearchField());
        REIRuntimeImpl.getSearchField().setResponder(s -> getEntryListWidget().updateSearch(s, false));
        if (!ConfigObject.getInstance().isEntryListWidgetScrolled()) {
            widgets.add(leftButton = Widgets.createButton(new Rectangle(bounds.x, bounds.y + (ConfigObject.getInstance().getSearchFieldLocation() == SearchFieldLocation.TOP_SIDE ? 24 : 0) + 5, 16, 16), Component.literal(""))
                    .onClick(button -> {
                        getEntryListWidget().previousPage();
                        if (getEntryListWidget().getPage() < 0)
                            getEntryListWidget().setPage(getEntryListWidget().getTotalPages() - 1);
                        getEntryListWidget().updateEntriesPosition();
                    })
                    .containsMousePredicate((button, point) -> button.getBounds().contains(point) && isNotInExclusionZones(point.x, point.y))
                    .tooltipLine(Component.translatable("text.rei.previous_page"))
                    .focusable(false));
            widgets.add(Widgets.createDrawableWidget((helper, matrices, mouseX, mouseY, delta) -> {
                helper.setBlitOffset(helper.getBlitOffset() + 1);
                RenderSystem.setShaderTexture(0, ARROW_LEFT_TEXTURE);
                Rectangle bounds = leftButton.getBounds();
                matrices.pushPose();
                blit(matrices, bounds.x + 4, bounds.y + 4, 0, 0, 8, 8, 8, 8);
                matrices.popPose();
                helper.setBlitOffset(helper.getBlitOffset() - 1);
            }));
            Button changelogButton;
            widgets.add(changelogButton = Widgets.createButton(new Rectangle(bounds.x + bounds.width - 18 - 18, bounds.y + (ConfigObject.getInstance().getSearchFieldLocation() == SearchFieldLocation.TOP_SIDE ? 24 : 0) + 5, 16, 16), Component.translatable(""))
                    .onClick(button -> {
                        ChangelogLoader.show();
                    })
                    .containsMousePredicate((button, point) -> button.getBounds().contains(point) && isNotInExclusionZones(point.x, point.y))
                    .tooltipLine(Component.translatable("text.rei.changelog.title"))
                    .focusable(false));
            widgets.add(Widgets.createDrawableWidget((helper, matrices, mouseX, mouseY, delta) -> {
                helper.setBlitOffset(helper.getBlitOffset() + 1);
                RenderSystem.setShaderTexture(0, CHEST_GUI_TEXTURE);
                Rectangle bounds = changelogButton.getBounds();
                matrices.pushPose();
                matrices.translate(0.5f, 0, 0);
                helper.blit(matrices, bounds.x + 1, bounds.y + 2, !ChangelogLoader.hasVisited() ? 28 : 14, 0, 14, 14);
                matrices.popPose();
                helper.setBlitOffset(helper.getBlitOffset() - 1);
            }));
            widgets.add(rightButton = Widgets.createButton(new Rectangle(bounds.x + bounds.width - 18, bounds.y + (ConfigObject.getInstance().getSearchFieldLocation() == SearchFieldLocation.TOP_SIDE ? 24 : 0) + 5, 16, 16), Component.literal(""))
                    .onClick(button -> {
                        getEntryListWidget().nextPage();
                        if (getEntryListWidget().getPage() >= getEntryListWidget().getTotalPages())
                            getEntryListWidget().setPage(0);
                        getEntryListWidget().updateEntriesPosition();
                    })
                    .containsMousePredicate((button, point) -> button.getBounds().contains(point) && isNotInExclusionZones(point.x, point.y))
                    .tooltipLine(Component.translatable("text.rei.next_page"))
                    .focusable(false));
            widgets.add(Widgets.createDrawableWidget((helper, matrices, mouseX, mouseY, delta) -> {
                helper.setBlitOffset(helper.getBlitOffset() + 1);
                RenderSystem.setShaderTexture(0, ARROW_RIGHT_TEXTURE);
                Rectangle bounds = rightButton.getBounds();
                matrices.pushPose();
                blit(matrices, bounds.x + 4, bounds.y + 4, 0, 0, 8, 8, 8, 8);
                matrices.popPose();
                helper.setBlitOffset(helper.getBlitOffset() - 1);
            }));
        }
        
        final Rectangle configButtonArea = getConfigButtonArea();
        UUID configButtonUuid = UUID.fromString("4357bc36-0a4e-47d2-8e07-ddc220df4a0f");
        widgets.add(configButton = InternalWidgets.wrapLateRenderable(
                
                InternalWidgets.concatWidgets(
                        Widgets.createButton(configButtonArea, NarratorChatListener.NO_TITLE)
                                .onClick(button -> {
                                    if (Screen.hasShiftDown() || Screen.hasControlDown()) {
                                        ClientHelper.getInstance().setCheating(!ClientHelper.getInstance().isCheating());
                                        return;
                                    }
                                    ConfigManager.getInstance().openConfigScreen(REIRuntime.getInstance().getPreviousScreen());
                                })
                                .onRender((matrices, button) -> {
                                    if (ClientHelper.getInstance().isCheating() && !(Minecraft.getInstance().screen instanceof DisplayScreen) && ClientHelperImpl.getInstance().hasOperatorPermission()) {
                                        button.setTint(ClientHelperImpl.getInstance().hasPermissionToUsePackets() ? 721354752 : 1476440063);
                                    } else {
                                        button.removeTint();
                                    }
                                    
                                    boolean isOpened = isMenuOpened(configButtonUuid);
                                    if (isOpened || !isAnyMenuOpened()) {
                                        boolean inBounds = (isNotInExclusionZones(PointHelper.getMouseFloatingX(), PointHelper.getMouseFloatingY()) && button.containsMouse(PointHelper.ofMouse())) || isMenuInBounds(configButtonUuid);
                                        if (isOpened != inBounds) {
                                            if (inBounds) {
                                                Menu menu = new Menu(button.getBounds(), provideConfigButtonMenu(), false);
                                                openMenu(configButtonUuid, menu, button::containsMouse, point -> true);
                                            } else {
                                                closeOverlayMenu();
                                            }
                                        }
                                    }
                                })
                                .focusable(false)
                                .containsMousePredicate((button, point) -> button.getBounds().contains(point) && isNotInExclusionZones(point.x, point.y)),
                        Widgets.createDrawableWidget((helper, matrices, mouseX, mouseY, delta) -> {
                            helper.setBlitOffset(helper.getBlitOffset() + 1);
                            RenderSystem.setShaderTexture(0, CHEST_GUI_TEXTURE);
                            helper.blit(matrices, configButtonArea.x + 3, configButtonArea.y + 3, 0, 0, 14, 14);
                            helper.setBlitOffset(helper.getBlitOffset() - 1);
                        })
                )
        ));
        Rectangle subsetsButtonBounds = getSubsetsButtonBounds();
        if (ConfigObject.getInstance().isSubsetsEnabled()) {
            widgets.add(InternalWidgets.wrapLateRenderable(Widgets.createButton(subsetsButtonBounds, ClientHelperImpl.getInstance().isAprilFools.get() ? Component.translatable("text.rei.tiny_potato") : Component.translatable("text.rei.subsets"))
                    .onClick(button -> {
                        proceedOpenMenuOrElse(Menu.SUBSETS, () -> {
                            openMenu(Menu.SUBSETS, Menu.createSubsetsMenuFromRegistry(subsetsButtonBounds), point -> true, point -> ConfigObject.getInstance().isSubsetsEnabled());
                        }, menu -> {
                            closeOverlayMenu();
                        });
                    })));
        }
        if (!ConfigObject.getInstance().isEntryListWidgetScrolled()) {
            widgets.add(Widgets.createClickableLabel(new Point(bounds.x + ((bounds.width - 18) / 2), bounds.y + (ConfigObject.getInstance().getSearchFieldLocation() == SearchFieldLocation.TOP_SIDE ? 24 : 0) + 10), NarratorChatListener.NO_TITLE, label -> {
                if (!Screen.hasShiftDown()) {
                    getEntryListWidget().setPage(0);
                    getEntryListWidget().updateEntriesPosition();
                } else {
                    ScreenOverlayImpl.getInstance().choosePageWidget = new DefaultDisplayChoosePageWidget(page -> {
                        getEntryListWidget().setPage(page);
                        getEntryListWidget().updateEntriesPosition();
                    }, getEntryListWidget().getPage(), getEntryListWidget().getTotalPages());
                }
            }).tooltip(Component.translatable("text.rei.go_back_first_page"), Component.literal(" "), Component.translatable("text.rei.shift_click_to", Component.translatable("text.rei.choose_page")).withStyle(ChatFormatting.GRAY)).focusable(false).onRender((matrices, label) -> {
                label.setClickable(getEntryListWidget().getTotalPages() > 1);
                label.setMessage(Component.literal(String.format("%s/%s", getEntryListWidget().getPage() + 1, Math.max(getEntryListWidget().getTotalPages(), 1))));
            }).rainbow(new Random().nextFloat() < 1.0E-4D || ClientHelperImpl.getInstance().isAprilFools.get()));
        }
        if (ConfigObject.getInstance().isCraftableFilterEnabled()) {
            Rectangle area = getCraftableToggleArea();
            ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
            ItemStack icon = new ItemStack(Blocks.CRAFTING_TABLE);
            this.widgets.add(InternalWidgets.wrapLateRenderable(InternalWidgets.concatWidgets(
                    Widgets.createButton(area, NarratorChatListener.NO_TITLE)
                            .focusable(false)
                            .onClick(button -> {
                                ConfigManager.getInstance().toggleCraftableOnly();
                                getEntryListWidget().updateSearch(REIRuntimeImpl.getSearchField().getText(), true);
                            })
                            .onRender((matrices, button) -> button.setTint(ConfigManager.getInstance().isCraftableOnlyEnabled() ? 939579655 : 956235776))
                            .containsMousePredicate((button, point) -> button.getBounds().contains(point) && isNotInExclusionZones(point.x, point.y))
                            .tooltipLineSupplier(button -> Component.translatable(ConfigManager.getInstance().isCraftableOnlyEnabled() ? "text.rei.showing_craftable" : "text.rei.showing_all")),
                    Widgets.createDrawableWidget((helper, matrices, mouseX, mouseY, delta) -> {
                        Vector4f vector = new Vector4f(area.x + 2, area.y + 2, helper.getBlitOffset() - 10, 1.0F);
                        vector.transform(matrices.last().pose());
                        itemRenderer.blitOffset = vector.z();
                        itemRenderer.renderGuiItem(icon, (int) vector.x(), (int) vector.y());
                        itemRenderer.blitOffset = 0.0F;
                    }))
            ));
        }
        
        widgets.add(draggingStack);
    }
    
    private Collection<MenuEntry> provideConfigButtonMenu() {
        ConfigObjectImpl config = ConfigManagerImpl.getInstance().getConfig();
        return Arrays.asList(
                ToggleMenuEntry.of(Component.translatable("text.rei.cheating"),
                        config::isCheating,
                        config::setCheating
                ),
                new EmptyMenuEntry(4),
                new TextMenuEntry(() -> {
                    if (!ClientHelper.getInstance().isCheating())
                        return Component.translatable("text.rei.cheating_disabled");
                    else if (!ClientHelperImpl.getInstance().hasOperatorPermission()) {
                        if (minecraft.gameMode.hasInfiniteItems())
                            return Component.translatable("text.rei.cheating_limited_creative_enabled");
                        else return Component.translatable("text.rei.cheating_enabled_no_perms");
                    } else if (ClientHelperImpl.getInstance().hasPermissionToUsePackets())
                        return Component.translatable("text.rei.cheating_enabled");
                    else
                    return Component.translatable("text.rei.cheating_limited_enabled");
                }),
                new SeparatorMenuEntry(),
                ToggleMenuEntry.ofDeciding(Component.translatable("text.rei.config.menu.dark_theme"),
                        config::isUsingDarkTheme,
                        dark -> {
                            config.setUsingDarkTheme(dark);
                            return false;
                        }
                ),
                ToggleMenuEntry.of(Component.translatable("text.rei.config.menu.craftable_filter"),
                        config::isCraftableFilterEnabled,
                        config::setCraftableFilterEnabled
                ),
                new SubMenuEntry(Component.translatable("text.rei.config.menu.display"), Arrays.asList(
                        ToggleMenuEntry.of(Component.translatable("text.rei.config.menu.display.remove_recipe_book"),
                                config::doesDisableRecipeBook,
                                disableRecipeBook -> {
                                    config.setDisableRecipeBook(disableRecipeBook);
                                    Screen screen = Minecraft.getInstance().screen;
                                    
                                    if (screen != null) {
                                        screen.init(minecraft, screen.width, screen.height);
                                    }
                                }
                        ),
                        ToggleMenuEntry.of(Component.translatable("text.rei.config.menu.display.left_side_mob_effects"),
                                config::isLeftSideMobEffects,
                                disableRecipeBook -> {
                                    config.setLeftSideMobEffects(disableRecipeBook);
                                    Screen screen = Minecraft.getInstance().screen;
                                    
                                    if (screen != null) {
                                        screen.init(minecraft, screen.width, screen.height);
                                    }
                                }
                        ),
                        ToggleMenuEntry.of(Component.translatable("text.rei.config.menu.display.left_side_panel"),
                                config::isLeftHandSidePanel,
                                bool -> config.setDisplayPanelLocation(bool ? DisplayPanelLocation.LEFT : DisplayPanelLocation.RIGHT)
                        ),
                        ToggleMenuEntry.of(Component.translatable("text.rei.config.menu.display.scrolling_side_panel"),
                                config::isEntryListWidgetScrolled,
                                config::setEntryListWidgetScrolled
                        ),
                        new SeparatorMenuEntry(),
                        ToggleMenuEntry.of(Component.translatable("text.rei.config.menu.display.caching_entry_rendering"),
                                config::doesCacheEntryRendering,
                                config::setDoesCacheEntryRendering
                        ),
                        new SeparatorMenuEntry(),
                        ToggleMenuEntry.of(Component.translatable("text.rei.config.menu.display.side_search_field"),
                                () -> config.getSearchFieldLocation() != SearchFieldLocation.CENTER,
                                bool -> config.setSearchFieldLocation(bool ? SearchFieldLocation.BOTTOM_SIDE : SearchFieldLocation.CENTER)
                        ),
                        ToggleMenuEntry.of(Component.translatable("text.rei.config.menu.display.syntax_highlighting"),
                                () -> config.getSyntaxHighlightingMode() == SyntaxHighlightingMode.COLORFUL || config.getSyntaxHighlightingMode() == SyntaxHighlightingMode.COLORFUL_UNDERSCORED,
                                bool -> config.setSyntaxHighlightingMode(bool ? SyntaxHighlightingMode.COLORFUL : SyntaxHighlightingMode.PLAIN_UNDERSCORED)
                        )
                )),
                new SeparatorMenuEntry(),
                ToggleMenuEntry.ofDeciding(Component.translatable("text.rei.config.menu.config"),
                        () -> false,
                        $ -> {
                            ConfigManager.getInstance().openConfigScreen(REIRuntime.getInstance().getPreviousScreen());
                            return false;
                        })
        );
    }
    
    private Rectangle getSubsetsButtonBounds() {
        if (ConfigObject.getInstance().isSubsetsEnabled()) {
            ScreenRegistry registry = ScreenRegistry.getInstance();
            Rectangle screenBounds = registry.getScreenBounds(minecraft.screen);
            return new Rectangle(screenBounds.x, 3, screenBounds.width, 18);
        }
        return null;
    }
    
    private Weather getNextWeather() {
        try {
            Weather current = getCurrentWeather();
            int next = current.getId() + 1;
            if (next >= 3)
                next = 0;
            return Weather.byId(next);
        } catch (Exception e) {
            return Weather.CLEAR;
        }
    }
    
    private Weather getCurrentWeather() {
        ClientLevel world = Minecraft.getInstance().level;
        if (world.isThundering())
            return Weather.THUNDER;
        if (world.getLevelData().isRaining())
            return Weather.RAIN;
        return Weather.CLEAR;
    }
    
    private String getGameModeShortText(GameType gameMode) {
        return I18n.get("text.rei.short_gamemode." + gameMode.getName());
    }
    
    private String getGameModeText(GameType gameMode) {
        return I18n.get("selectWorld.gameMode." + gameMode.getName());
    }
    
    private GameType getCurrentGameMode() {
        PlayerInfo info = Minecraft.getInstance().getConnection().getPlayerInfo(Minecraft.getInstance().player.getGameProfile().getId());
        return info == null ? GameType.SURVIVAL : info.getGameMode();
    }
    
    private Rectangle getSearchFieldArea() {
        int widthRemoved = 1;
        if (ConfigObject.getInstance().isCraftableFilterEnabled()) widthRemoved += 22;
        if (ConfigObject.getInstance().isLowerConfigButton()) widthRemoved += 22;
        SearchFieldLocation searchFieldLocation = REIRuntime.getInstance().getContextualSearchFieldLocation();
        switch (searchFieldLocation) {
            case TOP_SIDE:
                return getTopSideSearchFieldArea(widthRemoved);
            case BOTTOM_SIDE:
                return getBottomSideSearchFieldArea(widthRemoved);
            default:
            case CENTER:
                return getCenterSearchFieldArea(widthRemoved);
        }
    }
    
    private Rectangle getTopSideSearchFieldArea(int widthRemoved) {
        return new Rectangle(bounds.x + 2, 4, bounds.width - 6 - widthRemoved, 18);
    }
    
    private Rectangle getBottomSideSearchFieldArea(int widthRemoved) {
        return new Rectangle(bounds.x + 2, window.getGuiScaledHeight() - 22, bounds.width - 6 - widthRemoved, 18);
    }
    
    private Rectangle getCenterSearchFieldArea(int widthRemoved) {
        Rectangle screenBounds = ScreenRegistry.getInstance().getScreenBounds(minecraft.screen);
        return getBottomCenterSearchFieldArea(screenBounds, widthRemoved);
    }
    
    private Rectangle getBottomCenterSearchFieldArea(Rectangle containerBounds, int widthRemoved) {
        return new Rectangle(containerBounds.x, window.getGuiScaledHeight() - 22, containerBounds.width - widthRemoved, 18);
    }
    
    private Rectangle getCraftableToggleArea() {
        Rectangle area = getSearchFieldArea();
        area.setLocation(area.x + area.width + 4, area.y - 1);
        area.setSize(20, 20);
        return area;
    }
    
    private Rectangle getConfigButtonArea() {
        if (ConfigObject.getInstance().isLowerConfigButton()) {
            Rectangle area = getSearchFieldArea();
            area.setLocation(area.x + area.width + (ConfigObject.getInstance().isCraftableFilterEnabled() ? 26 : 4), area.y - 1);
            area.setSize(20, 20);
            return area;
        }
        return new Rectangle(ConfigObject.getInstance().isLeftHandSidePanel() ? window.getGuiScaledWidth() - 30 : 10, 10, 20, 20);
    }
    
    private String getCheatModeText() {
        return I18n.get(String.format("%s%s", "text.rei.", ClientHelper.getInstance().isCheating() ? "cheat" : "nocheat"));
    }
    
    @Override
    public Rectangle getBounds() {
        return bounds;
    }
    
    public Rectangle getScreenBounds() {
        return screenBounds;
    }
    
    @Override
    public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
        if (shouldReload || !calculateOverlayBounds().equals(bounds)) {
            init();
            getEntryListWidget().updateSearch(REIRuntimeImpl.getSearchField().getText(), true);
        } else {
            for (OverlayDecider decider : ScreenRegistry.getInstance().getDeciders(minecraft.screen)) {
                if (decider != null && decider.shouldRecalculateArea(ConfigObject.getInstance().getDisplayPanelLocation(), bounds)) {
                    init();
                    break;
                }
            }
        }
        if (shouldReloadSearch || (ConfigManager.getInstance().isCraftableOnlyEnabled() && CraftableFilter.INSTANCE.wasDirty())) {
            shouldReloadSearch = false;
            getEntryListWidget().updateSearch(REIRuntimeImpl.getSearchField().getText(), true);
        }
        if (OverlaySearchField.isHighlighting) {
            RenderSystem.disableDepthTest();
            RenderSystem.colorMask(true, true, true, false);
            if (Minecraft.getInstance().screen instanceof AbstractContainerScreen<?> containerScreen) {
                int x = containerScreen.leftPos, y = containerScreen.topPos;
                for (Slot slot : containerScreen.getMenu().slots) {
                    if (!slot.hasItem() || !EntryListSearchManager.INSTANCE.matches(EntryStacks.of(slot.getItem()))) {
                        matrices.pushPose();
                        matrices.translate(0, 0, 500f);
                        fillGradient(matrices, x + slot.x, y + slot.y, x + slot.x + 16, y + slot.y + 16, 0xdc202020, 0xdc202020);
                        matrices.popPose();
                    } else {
                        matrices.pushPose();
                        matrices.translate(0, 0, 200f);
                        fillGradient(matrices, x + slot.x, y + slot.y, x + slot.x + 16, y + slot.y + 16, 0x345fff3b, 0x345fff3b);
                        
                        fillGradient(matrices, x + slot.x - 1, y + slot.y - 1, x + slot.x, y + slot.y + 16 + 1, 0xff5fff3b, 0xff5fff3b);
                        fillGradient(matrices, x + slot.x + 16, y + slot.y - 1, x + slot.x + 16 + 1, y + slot.y + 16 + 1, 0xff5fff3b, 0xff5fff3b);
                        fillGradient(matrices, x + slot.x - 1, y + slot.y - 1, x + slot.x + 16, y + slot.y, 0xff5fff3b, 0xff5fff3b);
                        fillGradient(matrices, x + slot.x - 1, y + slot.y + 16, x + slot.x + 16, y + slot.y + 16 + 1, 0xff5fff3b, 0xff5fff3b);
                        
                        matrices.popPose();
                    }
                }
            }
            RenderSystem.colorMask(true, true, true, true);
            RenderSystem.enableDepthTest();
        }
        if (!hasSpace()) return;
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        this.renderWidgets(matrices, mouseX, mouseY, delta);
        if (ConfigObject.getInstance().areClickableRecipeArrowsEnabled()) {
            Screen screen = Minecraft.getInstance().screen;
            ClickArea.ClickAreaContext<Screen> context = new ClickArea.ClickAreaContext<Screen>() {
                @Override
                public Screen getScreen() {
                    return screen;
                }
                
                @Override
                public Point getMousePosition() {
                    return new Point(mouseX, mouseY);
                }
            };
            List<Component> clickAreaTooltips = ScreenRegistry.getInstance().getClickAreaTooltips((Class<Screen>) screen.getClass(), context);
            if (clickAreaTooltips != null && !clickAreaTooltips.isEmpty()) {
                Tooltip.create(clickAreaTooltips).queue();
            }
        }
    }
    
    private static Rectangle calculateOverlayBounds() {
        Rectangle bounds = ScreenRegistry.getInstance().getOverlayBounds(ConfigObject.getInstance().getDisplayPanelLocation(), Minecraft.getInstance().screen);
        
        int widthReduction = (int) Math.round(bounds.width * (1 - ConfigObject.getInstance().getHorizontalEntriesBoundariesPercentage()));
        if (ConfigObject.getInstance().getDisplayPanelLocation() == DisplayPanelLocation.RIGHT)
            bounds.x += widthReduction;
        bounds.width -= widthReduction;
        int maxWidth = (int) Math.ceil(entrySize() * ConfigObject.getInstance().getHorizontalEntriesBoundariesColumns() + entrySize() * 0.75);
        if (bounds.width > maxWidth) {
            if (ConfigObject.getInstance().getDisplayPanelLocation() == DisplayPanelLocation.RIGHT)
                bounds.x += bounds.width - maxWidth;
            bounds.width = maxWidth;
        }
        
        return bounds;
    }
    
    public void lateRender(PoseStack matrices, int mouseX, int mouseY, float delta) {
        if (REIRuntime.getInstance().isOverlayVisible() && hasSpace()) {
            REIRuntimeImpl.getSearchField().laterRender(matrices, mouseX, mouseY, delta);
            for (Widget widget : widgets) {
                if (widget instanceof LateRenderable && (overlayMenu == null || overlayMenu.wrappedMenu != widget))
                    widget.render(matrices, mouseX, mouseY, delta);
            }
            if (overlayMenu != null) {
                if (!overlayMenu.inBounds.test(PointHelper.ofMouse())) {
                    closeOverlayMenu();
                } else {
                    if (overlayMenu.wrappedMenu.containsMouse(mouseX, mouseY)) {
                        TOOLTIPS.clear();
                    }
                    overlayMenu.wrappedMenu.render(matrices, mouseX, mouseY, delta);
                }
            }
            if (choosePageWidget != null) {
                setBlitOffset(500);
                this.fillGradient(matrices, 0, 0, window.getGuiScaledWidth(), window.getGuiScaledHeight(), -1072689136, -804253680);
                setBlitOffset(0);
                choosePageWidget.render(matrices, mouseX, mouseY, delta);
            }
        }
        Screen currentScreen = Minecraft.getInstance().screen;
        if (choosePageWidget == null) {
            TOOLTIPS.stream().filter(Objects::nonNull)
                    .reduce((tooltip, tooltip2) -> tooltip2)
                    .ifPresent(tooltip -> renderTooltip(matrices, tooltip));
        }
        TOOLTIPS.clear();
        if (REIRuntime.getInstance().isOverlayVisible()) {
            for (Runnable runnable : AFTER_RENDER) {
                runnable.run();
            }
            AFTER_RENDER.clear();
        }
    }
    
    public void renderTooltip(PoseStack matrices, Tooltip tooltip) {
        renderTooltipInner(minecraft.screen, matrices, tooltip, tooltip.getX(), tooltip.getY());
    }
    
    @ExpectPlatform
    public static void renderTooltipInner(Screen screen, PoseStack matrices, Tooltip tooltip, int mouseX, int mouseY) {
        throw new AssertionError();
    }
    
    public void addTooltip(@Nullable Tooltip tooltip) {
        if (tooltip != null)
            TOOLTIPS.add(tooltip);
    }
    
    public void clearTooltips() {
        TOOLTIPS.clear();
    }
    
    public void renderWidgets(PoseStack matrices, int mouseX, int mouseY, float delta) {
        if (!REIRuntime.getInstance().isOverlayVisible())
            return;
        if (!ConfigObject.getInstance().isEntryListWidgetScrolled()) {
            leftButton.setEnabled(getEntryListWidget().getTotalPages() > 1);
            rightButton.setEnabled(getEntryListWidget().getTotalPages() > 1);
        }
        for (Widget widget : widgets) {
            if (!(widget instanceof LateRenderable))
                widget.render(matrices, mouseX, mouseY, delta);
        }
    }
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (!REIRuntime.getInstance().isOverlayVisible())
            return false;
        if (overlayMenu != null && overlayMenu.wrappedMenu.mouseScrolled(mouseX, mouseY, amount))
            return true;
        if (isInside(PointHelper.ofMouse())) {
            if (getEntryListWidget().mouseScrolled(mouseX, mouseY, amount)) {
                return true;
            }
            if (!Screen.hasControlDown() && !ConfigObject.getInstance().isEntryListWidgetScrolled()) {
                if (amount > 0 && leftButton.isEnabled())
                    leftButton.onClick();
                else if (amount < 0 && rightButton.isEnabled())
                    rightButton.onClick();
                else
                    return false;
                return true;
            }
        }
        if (isNotInExclusionZones(PointHelper.getMouseX(), PointHelper.getMouseY())) {
            if (favoritesListWidget != null && favoritesListWidget.mouseScrolled(mouseX, mouseY, amount))
                return true;
        }
        for (Widget widget : widgets)
            if (widget != getEntryListWidget() && (favoritesListWidget == null || widget != favoritesListWidget)
                && (overlayMenu == null || widget != overlayMenu.wrappedMenu)
                && widget.mouseScrolled(mouseX, mouseY, amount))
                return true;
        return false;
    }
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!hasSpace()) return false;
        if (REIRuntime.getInstance().isOverlayVisible()) {
            if (keyCode == 256 && choosePageWidget != null) {
                choosePageWidget = null;
                return true;
            }
            if (choosePageWidget != null)
                return choosePageWidget.keyPressed(keyCode, scanCode, modifiers);
            if (REIRuntimeImpl.getSearchField().keyPressed(keyCode, scanCode, modifiers))
                return true;
            for (GuiEventListener listener : widgets)
                if (listener != REIRuntimeImpl.getSearchField() && listener.keyPressed(keyCode, scanCode, modifiers))
                    return true;
        }
        if (ConfigObject.getInstance().getHideKeybind().matchesKey(keyCode, scanCode)) {
            REIRuntime.getInstance().toggleOverlayVisible();
            return true;
        }
        EntryStack<?> stack = ScreenRegistry.getInstance().getFocusedStack(Minecraft.getInstance().screen, PointHelper.ofMouse());
        if (stack != null && !stack.isEmpty()) {
            stack = stack.copy();
            if (ConfigObject.getInstance().getRecipeKeybind().matchesKey(keyCode, scanCode)) {
                return ViewSearchBuilder.builder().addRecipesFor(stack).open();
            } else if (ConfigObject.getInstance().getUsageKeybind().matchesKey(keyCode, scanCode)) {
                return ViewSearchBuilder.builder().addUsagesFor(stack).open();
            } else if (ConfigObject.getInstance().getFavoriteKeyCode().matchesKey(keyCode, scanCode)) {
                FavoriteEntry favoriteEntry = FavoriteEntry.fromEntryStack(stack);
                ConfigObject.getInstance().getFavoriteEntries().add(favoriteEntry);
                return true;
            }
        }
        if (!REIRuntime.getInstance().isOverlayVisible())
            return false;
        if (ConfigObject.getInstance().getFocusSearchFieldKeybind().matchesKey(keyCode, scanCode)) {
            REIRuntimeImpl.getSearchField().setFocused(true);
            setFocused(REIRuntimeImpl.getSearchField());
            REIRuntimeImpl.getSearchField().keybindFocusTime = System.currentTimeMillis();
            REIRuntimeImpl.getSearchField().keybindFocusKey = keyCode;
            return true;
        }
        return false;
    }
    
    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (!hasSpace()) return false;
        if (REIRuntime.getInstance().isOverlayVisible()) {
            if (choosePageWidget == null) {
                if (REIRuntimeImpl.getSearchField().keyReleased(keyCode, scanCode, modifiers))
                    return true;
                for (GuiEventListener listener : widgets)
                    if (listener != REIRuntimeImpl.getSearchField() && listener == getFocused() && listener.keyPressed(keyCode, scanCode, modifiers))
                        return true;
            }
        }
        return false;
    }
    
    @Override
    public boolean charTyped(char character, int modifiers) {
        if (!REIRuntime.getInstance().isOverlayVisible())
            return false;
        if (!hasSpace()) return false;
        if (choosePageWidget != null) {
            return choosePageWidget.charTyped(character, modifiers);
        }
        if (REIRuntimeImpl.getSearchField().charTyped(character, modifiers))
            return true;
        for (GuiEventListener listener : widgets)
            if (listener != REIRuntimeImpl.getSearchField() && listener.charTyped(character, modifiers))
                return true;
        return false;
    }
    
    @Override
    public List<Widget> children() {
        return widgets;
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean visible = REIRuntime.getInstance().isOverlayVisible();
        if (choosePageWidget != null) {
            if (choosePageWidget.containsMouse(mouseX, mouseY)) {
                return choosePageWidget.mouseClicked(mouseX, mouseY, button);
            } else {
                choosePageWidget = null;
                init();
                return false;
            }
        }
        if (!hasSpace()) return false;
        if (visible && configButton.mouseClicked(mouseX, mouseY, button)) {
            this.setFocused(configButton);
            if (button == 0)
                this.setDragging(true);
            return true;
        }
        if (ConfigObject.getInstance().getHideKeybind().matchesMouse(button)) {
            REIRuntime.getInstance().toggleOverlayVisible();
            return REIRuntime.getInstance().isOverlayVisible();
        }
        EntryStack<?> stack = ScreenRegistry.getInstance().getFocusedStack(Minecraft.getInstance().screen, PointHelper.ofMouse());
        if (stack != null && !stack.isEmpty()) {
            stack = stack.copy();
            if (ConfigObject.getInstance().getRecipeKeybind().matchesMouse(button)) {
                return ViewSearchBuilder.builder().addRecipesFor(stack).open();
            } else if (ConfigObject.getInstance().getUsageKeybind().matchesMouse(button)) {
                return ViewSearchBuilder.builder().addUsagesFor(stack).open();
            } else if (visible && ConfigObject.getInstance().getFavoriteKeyCode().matchesMouse(button)) {
                FavoriteEntry favoriteEntry = FavoriteEntry.fromEntryStack(stack);
                ConfigObject.getInstance().getFavoriteEntries().add(favoriteEntry);
                return true;
            }
        }
        if (visible && overlayMenu != null) {
            if (overlayMenu.wrappedMenu.mouseClicked(mouseX, mouseY, button)) {
                if (overlayMenu != null) this.setFocused(overlayMenu.wrappedMenu);
                else this.setFocused(null);
                if (button == 0)
                    this.setDragging(true);
                REIRuntimeImpl.getSearchField().setFocused(false);
                return true;
            }
        }
        if (ConfigObject.getInstance().areClickableRecipeArrowsEnabled()) {
            Screen screen = Minecraft.getInstance().screen;
            ClickArea.ClickAreaContext<Screen> context = new ClickArea.ClickAreaContext<Screen>() {
                @Override
                public Screen getScreen() {
                    return screen;
                }
                
                @Override
                public Point getMousePosition() {
                    return new Point(mouseX, mouseY);
                }
            };
            if (ScreenRegistry.getInstance().executeClickArea((Class<Screen>) screen.getClass(), context)) {
                return true;
            }
        }
        if (!visible) {
            return false;
        }
        for (GuiEventListener element : widgets) {
            if (element != configButton && (overlayMenu == null || element != overlayMenu.wrappedMenu) && element.mouseClicked(mouseX, mouseY, button)) {
                this.setFocused(element);
                if (button == 0)
                    this.setDragging(true);
                if (!(element instanceof OverlaySearchField))
                    REIRuntimeImpl.getSearchField().setFocused(false);
                return true;
            }
        }
        if (ConfigObject.getInstance().getFocusSearchFieldKeybind().matchesMouse(button)) {
            REIRuntimeImpl.getSearchField().setFocused(true);
            setFocused(REIRuntimeImpl.getSearchField());
            REIRuntimeImpl.getSearchField().keybindFocusTime = -1;
            REIRuntimeImpl.getSearchField().keybindFocusKey = -1;
            return true;
        }
        return false;
    }
    
    @Override
    public boolean mouseDragged(double double_1, double double_2, int int_1, double double_3, double double_4) {
        if (!REIRuntime.getInstance().isOverlayVisible())
            return false;
        if (!hasSpace()) return false;
        if (choosePageWidget != null) {
            return choosePageWidget.mouseDragged(double_1, double_2, int_1, double_3, double_4);
        }
        return (this.getFocused() != null && this.isDragging() && int_1 == 0) && this.getFocused().mouseDragged(double_1, double_2, int_1, double_3, double_4);
    }
    
    @Override
    public GuiEventListener getFocused() {
        if (choosePageWidget != null)
            return choosePageWidget;
        return super.getFocused();
    }
    
    public boolean isInside(double mouseX, double mouseY) {
        return bounds.contains(mouseX, mouseY) && isNotInExclusionZones(mouseX, mouseY);
    }
    
    @Override
    public boolean isNotInExclusionZones(double mouseX, double mouseY) {
        for (OverlayDecider decider : ScreenRegistry.getInstance().getDeciders(Minecraft.getInstance().screen)) {
            InteractionResult in = decider.isInZone(mouseX, mouseY);
            if (in != InteractionResult.PASS)
                return in == InteractionResult.SUCCESS;
        }
        return true;
    }
    
    public boolean isInside(Point point) {
        return isInside(point.getX(), point.getY());
    }
    
    @Override
    public OverlayListWidget getEntryList() {
        return getEntryListWidget();
    }
    
    @Override
    public Optional<OverlayListWidget> getFavoritesList() {
        return Optional.ofNullable(getFavoritesListWidget());
    }
}
