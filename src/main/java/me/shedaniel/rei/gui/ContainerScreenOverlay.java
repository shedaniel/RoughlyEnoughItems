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

package me.shedaniel.rei.gui;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.math.impl.PointHelper;
import me.shedaniel.rei.RoughlyEnoughItemsCore;
import me.shedaniel.rei.api.*;
import me.shedaniel.rei.api.widgets.Button;
import me.shedaniel.rei.api.widgets.Tooltip;
import me.shedaniel.rei.api.widgets.Widgets;
import me.shedaniel.rei.gui.config.SearchFieldLocation;
import me.shedaniel.rei.gui.modules.Menu;
import me.shedaniel.rei.gui.modules.entries.GameModeMenuEntry;
import me.shedaniel.rei.gui.modules.entries.WeatherMenuEntry;
import me.shedaniel.rei.gui.widget.*;
import me.shedaniel.rei.impl.ClientHelperImpl;
import me.shedaniel.rei.impl.InternalWidgets;
import me.shedaniel.rei.impl.ScreenHelper;
import me.shedaniel.rei.impl.Weather;
import me.shedaniel.rei.utils.CollectionUtils;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.ContainerScreen;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector4f;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.world.GameMode;
import org.apache.logging.log4j.util.TriConsumer;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

@ApiStatus.Internal
public class ContainerScreenOverlay extends WidgetWithBounds implements REIOverlay {
    
    private static final Identifier CHEST_GUI_TEXTURE = new Identifier("roughlyenoughitems", "textures/gui/recipecontainer.png");
    private static final List<Tooltip> TOOLTIPS = Lists.newArrayList();
    private static final List<Runnable> AFTER_RENDER = Lists.newArrayList();
    private static final EntryListWidget ENTRY_LIST_WIDGET = new EntryListWidget();
    private static FavoritesListWidget favoritesListWidget = null;
    private final List<Widget> widgets = Lists.newLinkedList();
    public boolean shouldReInit = false;
    private int tooltipWidth;
    private int tooltipHeight;
    private List<Text> tooltipLines;
    public final TriConsumer<MatrixStack, Point, Float> renderTooltipCallback = (matrices, mouse, aFloat) -> {
        RenderSystem.disableRescaleNormal();
        RenderSystem.disableDepthTest();
        matrices.push();
        matrices.translate(0, 0, 999);
        int x = mouse.x;
        int y = mouse.y;
        this.fillGradient(matrices, x - 3, y - 4, x + tooltipWidth + 3, y - 3, -267386864, -267386864);
        this.fillGradient(matrices, x - 3, y + tooltipHeight + 3, x + tooltipWidth + 3, y + tooltipHeight + 4, -267386864, -267386864);
        this.fillGradient(matrices, x - 3, y - 3, x + tooltipWidth + 3, y + tooltipHeight + 3, -267386864, -267386864);
        this.fillGradient(matrices, x - 4, y - 3, x - 3, y + tooltipHeight + 3, -267386864, -267386864);
        this.fillGradient(matrices, x + tooltipWidth + 3, y - 3, x + tooltipWidth + 4, y + tooltipHeight + 3, -267386864, -267386864);
        this.fillGradient(matrices, x - 3, y - 3 + 1, x - 3 + 1, y + tooltipHeight + 3 - 1, 1347420415, 1344798847);
        this.fillGradient(matrices, x + tooltipWidth + 2, y - 3 + 1, x + tooltipWidth + 3, y + tooltipHeight + 3 - 1, 1347420415, 1344798847);
        this.fillGradient(matrices, x - 3, y - 3, x + tooltipWidth + 3, y - 3 + 1, 1347420415, 1347420415);
        this.fillGradient(matrices, x - 3, y + tooltipHeight + 2, x + tooltipWidth + 3, y + tooltipHeight + 3, 1344798847, 1344798847);
        int currentY = y;
        VertexConsumerProvider.Immediate immediate = VertexConsumerProvider.immediate(Tessellator.getInstance().getBuffer());
        Matrix4f matrix = matrices.peek().getModel();
        for (int lineIndex = 0; lineIndex < tooltipLines.size(); lineIndex++) {
            font.draw(tooltipLines.get(lineIndex), x, currentY, -1, true, matrix, immediate, false, 0, 15728880);
            currentY += lineIndex == 0 ? 12 : 10;
        }
        immediate.draw();
        matrices.pop();
        RenderSystem.enableDepthTest();
        RenderSystem.enableRescaleNormal();
    };
    private Rectangle bounds;
    private Window window;
    private Button leftButton, rightButton;
    @ApiStatus.Experimental
    private Rectangle subsetsButtonBounds;
    @ApiStatus.Experimental
    @Nullable
    private Menu subsetsMenu = null;
    private Widget wrappedSubsetsMenu = null;
    
    @Nullable
    private Menu weatherMenu = null;
    private Widget wrappedWeatherMenu = null;
    private boolean renderWeatherMenu = false;
    private Button weatherButton = null;
    
    @Nullable
    private Menu gameModeMenu = null;
    private Widget wrappedGameModeMenu = null;
    private boolean renderGameModeMenu = false;
    private Button gameModeButton = null;
    
    public static EntryListWidget getEntryListWidget() {
        return ENTRY_LIST_WIDGET;
    }
    
    @Nullable
    public static FavoritesListWidget getFavoritesListWidget() {
        return favoritesListWidget;
    }
    
    @ApiStatus.Experimental
    @Nullable
    public Menu getSubsetsMenu() {
        return subsetsMenu;
    }
    
    public void removeWeatherMenu() {
        this.renderWeatherMenu = false;
        Widget tmpWeatherMenu = wrappedWeatherMenu;
        AFTER_RENDER.add(() -> this.widgets.remove(tmpWeatherMenu));
        this.weatherMenu = null;
        this.wrappedWeatherMenu = null;
    }
    
    public void removeGameModeMenu() {
        this.renderGameModeMenu = false;
        Widget tmpGameModeMenu = wrappedGameModeMenu;
        AFTER_RENDER.add(() -> this.widgets.remove(tmpGameModeMenu));
        this.gameModeMenu = null;
        this.wrappedGameModeMenu = null;
    }
    
    @Override
    public void queueReloadOverlay() {
        shouldReInit = true;
    }
    
    public void init(boolean useless) {
        init();
    }
    
    public void init() {
        this.shouldReInit = false;
        //Update Variables
        this.children().clear();
        this.wrappedSubsetsMenu = null;
        this.subsetsMenu = null;
        this.weatherMenu = null;
        this.renderWeatherMenu = false;
        this.weatherButton = null;
        this.window = MinecraftClient.getInstance().getWindow();
        this.bounds = DisplayHelper.getInstance().getOverlayBounds(ConfigObject.getInstance().getDisplayPanelLocation(), MinecraftClient.getInstance().currentScreen);
        widgets.add(ENTRY_LIST_WIDGET);
        if (ConfigObject.getInstance().isFavoritesEnabled()) {
            if (favoritesListWidget == null)
                favoritesListWidget = new FavoritesListWidget();
            widgets.add(favoritesListWidget);
        }
        ENTRY_LIST_WIDGET.updateArea(ScreenHelper.getSearchField() == null ? "" : null);
        if (ScreenHelper.getSearchField() == null) {
            ScreenHelper.setSearchField(new OverlaySearchField(0, 0, 0, 0));
        }
        ScreenHelper.getSearchField().getBounds().setBounds(getSearchFieldArea());
        this.widgets.add(ScreenHelper.getSearchField());
        ScreenHelper.getSearchField().setChangedListener(s -> ENTRY_LIST_WIDGET.updateSearch(s, false));
        if (!ConfigObject.getInstance().isEntryListWidgetScrolled()) {
            widgets.add(leftButton = Widgets.createButton(new Rectangle(bounds.x, bounds.y + (ConfigObject.getInstance().getSearchFieldLocation() == SearchFieldLocation.TOP_SIDE ? 24 : 0) + 5, 16, 16), new TranslatableText("text.rei.left_arrow"))
                    .onClick(button -> {
                        ENTRY_LIST_WIDGET.previousPage();
                        if (ENTRY_LIST_WIDGET.getPage() < 0)
                            ENTRY_LIST_WIDGET.setPage(ENTRY_LIST_WIDGET.getTotalPages() - 1);
                        ENTRY_LIST_WIDGET.updateEntriesPosition();
                    })
                    .containsMousePredicate((button, point) -> button.getBounds().contains(point) && isNotInExclusionZones(point.x, point.y))
                    .tooltipLine(I18n.translate("text.rei.previous_page"))
                    .focusable(false));
            widgets.add(rightButton = Widgets.createButton(new Rectangle(bounds.x + bounds.width - 18, bounds.y + (ConfigObject.getInstance().getSearchFieldLocation() == SearchFieldLocation.TOP_SIDE ? 24 : 0) + 5, 16, 16), new TranslatableText("text.rei.right_arrow"))
                    .onClick(button -> {
                        ENTRY_LIST_WIDGET.nextPage();
                        if (ENTRY_LIST_WIDGET.getPage() >= ENTRY_LIST_WIDGET.getTotalPages())
                            ENTRY_LIST_WIDGET.setPage(0);
                        ENTRY_LIST_WIDGET.updateEntriesPosition();
                    })
                    .containsMousePredicate((button, point) -> button.getBounds().contains(point) && isNotInExclusionZones(point.x, point.y))
                    .tooltipLine(I18n.translate("text.rei.next_page"))
                    .focusable(false));
        }
        
        final Rectangle configButtonArea = getConfigButtonArea();
        Widget tmp;
        widgets.add(tmp = InternalWidgets.wrapLateRenderable(InternalWidgets.mergeWidgets(
                Widgets.createButton(configButtonArea, NarratorManager.EMPTY)
                        .onClick(button -> {
                            if (Screen.hasShiftDown()) {
                                ClientHelper.getInstance().setCheating(!ClientHelper.getInstance().isCheating());
                                return;
                            }
                            ConfigManager.getInstance().openConfigScreen(REIHelper.getInstance().getPreviousContainerScreen());
                        })
                        .onRender((matrices, button) -> {
                            if (ClientHelper.getInstance().isCheating() && RoughlyEnoughItemsCore.hasOperatorPermission()) {
                                button.setTint(RoughlyEnoughItemsCore.hasPermissionToUsePackets() ? 721354752 : 1476440063);
                            } else {
                                button.removeTint();
                            }
                        })
                        .focusable(false)
                        .containsMousePredicate((button, point) -> button.getBounds().contains(point) && isNotInExclusionZones(point.x, point.y))
                        .tooltipSupplier(button -> {
                            String tooltips = I18n.translate("text.rei.config_tooltip");
                            tooltips += "\n  ";
                            if (!ClientHelper.getInstance().isCheating())
                                tooltips += "\n" + I18n.translate("text.rei.cheating_disabled");
                            else if (!RoughlyEnoughItemsCore.hasOperatorPermission()) {
                                if (minecraft.interactionManager.hasCreativeInventory())
                                    tooltips += "\n" + I18n.translate("text.rei.cheating_limited_creative_enabled");
                                else tooltips += "\n" + I18n.translate("text.rei.cheating_enabled_no_perms");
                            } else if (RoughlyEnoughItemsCore.hasPermissionToUsePackets())
                                tooltips += "\n" + I18n.translate("text.rei.cheating_enabled");
                            else
                                tooltips += "\n" + I18n.translate("text.rei.cheating_limited_enabled");
                            return tooltips;
                        }),
                Widgets.createDrawableWidget((helper, matrices, mouseX, mouseY, delta) -> {
                    helper.setZOffset(helper.getZOffset() + 1);
                    MinecraftClient.getInstance().getTextureManager().bindTexture(CHEST_GUI_TEXTURE);
                    helper.drawTexture(matrices, configButtonArea.x + 3, configButtonArea.y + 3, 0, 0, 14, 14);
                })
                )
        ));
        tmp.setZ(600);
        if (ConfigObject.getInstance().doesShowUtilsButtons()) {
            widgets.add(gameModeButton = Widgets.createButton(ConfigObject.getInstance().isLowerConfigButton() ? new Rectangle(ConfigObject.getInstance().isLeftHandSidePanel() ? window.getScaledWidth() - 30 : 10, 10, 20, 20) : new Rectangle(ConfigObject.getInstance().isLeftHandSidePanel() ? window.getScaledWidth() - 55 : 35, 10, 20, 20), NarratorManager.EMPTY)
                    .onRender((matrices, button) -> {
                        boolean tmpRender = renderGameModeMenu;
                        renderGameModeMenu = !renderWeatherMenu && (button.isFocused() || button.containsMouse(PointHelper.ofMouse()) || (wrappedGameModeMenu != null && wrappedGameModeMenu.containsMouse(PointHelper.ofMouse())));
                        if (tmpRender != renderGameModeMenu) {
                            if (renderGameModeMenu) {
                                this.gameModeMenu = new Menu(new Point(button.getBounds().x, button.getBounds().getMaxY()),
                                        CollectionUtils.filterAndMap(Arrays.asList(GameMode.values()), mode -> mode != GameMode.NOT_SET, GameModeMenuEntry::new));
                                if (ConfigObject.getInstance().isLeftHandSidePanel())
                                    this.gameModeMenu.menuStartPoint.x -= this.gameModeMenu.getBounds().width - this.gameModeButton.getBounds().width;
                                this.wrappedGameModeMenu = InternalWidgets.wrapTranslate(InternalWidgets.wrapLateRenderable(gameModeMenu), 0, 0, 600);
                                AFTER_RENDER.add(() -> this.widgets.add(wrappedGameModeMenu));
                            } else {
                                removeGameModeMenu();
                            }
                        }
                        button.setText(new LiteralText(getGameModeShortText(getCurrentGameMode())));
                    })
                    .focusable(false)
                    .tooltipLine(I18n.translate("text.rei.gamemode_button.tooltip.all"))
                    .containsMousePredicate((button, point) -> button.getBounds().contains(point) && isNotInExclusionZones(point.x, point.y)));
            widgets.add(weatherButton = Widgets.createButton(new Rectangle(ConfigObject.getInstance().isLeftHandSidePanel() ? window.getScaledWidth() - 30 : 10, 35, 20, 20), NarratorManager.EMPTY)
                    .onRender((matrices, button) -> {
                        boolean tmpRender = renderWeatherMenu;
                        renderWeatherMenu = !renderGameModeMenu && (button.isFocused() || button.containsMouse(PointHelper.ofMouse()) || (wrappedWeatherMenu != null && wrappedWeatherMenu.containsMouse(PointHelper.ofMouse())));
                        if (tmpRender != renderWeatherMenu) {
                            if (renderWeatherMenu) {
                                this.weatherMenu = new Menu(new Point(button.getBounds().x, button.getBounds().getMaxY()),
                                        CollectionUtils.map(Weather.values(), WeatherMenuEntry::new));
                                if (ConfigObject.getInstance().isLeftHandSidePanel())
                                    this.weatherMenu.menuStartPoint.x -= this.weatherMenu.getBounds().width - this.weatherButton.getBounds().width;
                                this.wrappedWeatherMenu = InternalWidgets.wrapTranslate(InternalWidgets.wrapLateRenderable(weatherMenu), 0, 0, 400);
                                AFTER_RENDER.add(() -> this.widgets.add(wrappedWeatherMenu));
                            } else {
                                removeWeatherMenu();
                            }
                        }
                    })
                    .tooltipLine(I18n.translate("text.rei.weather_button.tooltip.all"))
                    .focusable(false)
                    .containsMousePredicate((button, point) -> button.getBounds().contains(point) && isNotInExclusionZones(point.x, point.y)));
            widgets.add(Widgets.createDrawableWidget((helper, matrices, mouseX, mouseY, delta) -> {
                MinecraftClient.getInstance().getTextureManager().bindTexture(CHEST_GUI_TEXTURE);
                RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
                helper.drawTexture(matrices, weatherButton.getBounds().x + 3, weatherButton.getBounds().y + 3, getCurrentWeather().getId() * 14, 14, 14, 14);
            }));
        }
        subsetsButtonBounds = getSubsetsButtonBounds();
        if (ConfigObject.getInstance().isSubsetsEnabled()) {
            widgets.add(InternalWidgets.wrapLateRenderable(InternalWidgets.wrapTranslate(Widgets.createButton(subsetsButtonBounds, ((ClientHelperImpl) ClientHelper.getInstance()).isAprilFools.get() ? new TranslatableText("text.rei.tiny_potato") : new TranslatableText("text.rei.subsets"))
                    .onClick(button -> {
                        if (subsetsMenu == null) {
                            wrappedSubsetsMenu = InternalWidgets.wrapTranslate(InternalWidgets.wrapLateRenderable(this.subsetsMenu = Menu.createSubsetsMenuFromRegistry(new Point(this.subsetsButtonBounds.x, this.subsetsButtonBounds.getMaxY()))), 0, 0, 400);
                            this.widgets.add(this.wrappedSubsetsMenu);
                        } else {
                            this.widgets.remove(this.wrappedSubsetsMenu);
                            this.subsetsMenu = null;
                            this.wrappedSubsetsMenu = null;
                        }
                    }), 0, 0, 600)));
        }
        if (!ConfigObject.getInstance().isEntryListWidgetScrolled()) {
            widgets.add(Widgets.createClickableLabel(new Point(bounds.x + (bounds.width / 2), bounds.y + (ConfigObject.getInstance().getSearchFieldLocation() == SearchFieldLocation.TOP_SIDE ? 24 : 0) + 10), NarratorManager.EMPTY, label -> {
                ENTRY_LIST_WIDGET.setPage(0);
                ENTRY_LIST_WIDGET.updateEntriesPosition();
            }).tooltipLine(I18n.translate("text.rei.go_back_first_page")).focusable(false).onRender((matrices, label) -> {
                label.setClickable(ENTRY_LIST_WIDGET.getTotalPages() > 1);
                label.setText(new LiteralText(String.format("%s/%s", ENTRY_LIST_WIDGET.getPage() + 1, Math.max(ENTRY_LIST_WIDGET.getTotalPages(), 1))));
            }));
        }
        if (ConfigObject.getInstance().isCraftableFilterEnabled()) {
            Rectangle area = getCraftableToggleArea();
            ItemRenderer itemRenderer = MinecraftClient.getInstance().getItemRenderer();
            ItemStack icon = new ItemStack(Blocks.CRAFTING_TABLE);
            this.widgets.add(tmp = InternalWidgets.wrapLateRenderable(InternalWidgets.mergeWidgets(
                    Widgets.createButton(area, NarratorManager.EMPTY)
                            .focusable(false)
                            .onClick(button -> {
                                ConfigManager.getInstance().toggleCraftableOnly();
                                ENTRY_LIST_WIDGET.updateSearch(ScreenHelper.getSearchField().getText(), true);
                            })
                            .onRender((matrices, button) -> button.setTint(ConfigManager.getInstance().isCraftableOnlyEnabled() ? 939579655 : 956235776))
                            .containsMousePredicate((button, point) -> button.getBounds().contains(point) && isNotInExclusionZones(point.x, point.y))
                            .tooltipSupplier(button -> I18n.translate(ConfigManager.getInstance().isCraftableOnlyEnabled() ? "text.rei.showing_craftable" : "text.rei.showing_all")),
                    Widgets.createDrawableWidget((helper, matrices, mouseX, mouseY, delta) -> {
                        Vector4f vector = new Vector4f(area.x + 2, area.y + 2, helper.getZOffset() - 10, 1.0F);
                        vector.transform(matrices.peek().getModel());
                        itemRenderer.zOffset = vector.getZ();
                        itemRenderer.renderGuiItemIcon(icon, (int) vector.getX(), (int) vector.getY());
                        itemRenderer.zOffset = 0.0F;
                    }))
            ));
            tmp.setZ(600);
        }
    }
    
    @ApiStatus.Experimental
    private Rectangle getSubsetsButtonBounds() {
        if (ConfigObject.getInstance().isSubsetsEnabled()) {
            if (MinecraftClient.getInstance().currentScreen instanceof RecipeViewingScreen) {
                RecipeViewingScreen widget = (RecipeViewingScreen) MinecraftClient.getInstance().currentScreen;
                return new Rectangle(widget.getBounds().x, 3, widget.getBounds().width, 18);
            }
            if (MinecraftClient.getInstance().currentScreen instanceof VillagerRecipeViewingScreen) {
                VillagerRecipeViewingScreen widget = (VillagerRecipeViewingScreen) MinecraftClient.getInstance().currentScreen;
                return new Rectangle(widget.bounds.x, 3, widget.bounds.width, 18);
            }
            return new Rectangle(REIHelper.getInstance().getPreviousContainerScreen().x, 3, REIHelper.getInstance().getPreviousContainerScreen().containerWidth, 18);
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
        ClientWorld world = MinecraftClient.getInstance().world;
        if (world.isThundering())
            return Weather.THUNDER;
        if (world.getLevelProperties().isRaining())
            return Weather.RAIN;
        return Weather.CLEAR;
    }
    
    private String getGameModeShortText(GameMode gameMode) {
        return I18n.translate("text.rei.short_gamemode." + gameMode.getName());
    }
    
    private String getGameModeText(GameMode gameMode) {
        return I18n.translate("selectWorld.gameMode." + gameMode.getName());
    }
    
    private GameMode getNextGameMode(boolean reverse) {
        try {
            GameMode current = getCurrentGameMode();
            int next = current.getId() + 1;
            if (reverse)
                next -= 2;
            if (next > 3)
                next = 0;
            if (next < 0)
                next = 3;
            return GameMode.byId(next);
        } catch (Exception e) {
            return GameMode.NOT_SET;
        }
    }
    
    private GameMode getCurrentGameMode() {
        return MinecraftClient.getInstance().getNetworkHandler().getPlayerListEntry(MinecraftClient.getInstance().player.getGameProfile().getId()).getGameMode();
    }
    
    private Rectangle getSearchFieldArea() {
        int widthRemoved = 1 + (ConfigObject.getInstance().isCraftableFilterEnabled() ? 22 : 0) + (ConfigObject.getInstance().isLowerConfigButton() ? 22 : 0);
        SearchFieldLocation searchFieldLocation = ConfigObject.getInstance().getSearchFieldLocation();
        if (searchFieldLocation == SearchFieldLocation.BOTTOM_SIDE)
            return new Rectangle(bounds.x + 2, window.getScaledHeight() - 22, bounds.width - 6 - widthRemoved, 18);
        if (searchFieldLocation == SearchFieldLocation.TOP_SIDE)
            return new Rectangle(bounds.x + 2, 4, bounds.width - 6 - widthRemoved, 18);
        for (OverlayDecider decider : DisplayHelper.getInstance().getSortedOverlayDeciders(MinecraftClient.getInstance().currentScreen.getClass())) {
            if (decider instanceof DisplayHelper.DisplayBoundsProvider) {
                Rectangle containerBounds = ((DisplayHelper.DisplayBoundsProvider<Screen>) decider).getScreenBounds(MinecraftClient.getInstance().currentScreen);
                return new Rectangle(containerBounds.x, window.getScaledHeight() - 22, containerBounds.width - widthRemoved, 18);
            }
        }
        return new Rectangle();
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
        return new Rectangle(ConfigObject.getInstance().isLeftHandSidePanel() ? window.getScaledWidth() - 30 : 10, 10, 20, 20);
    }
    
    private String getCheatModeText() {
        return I18n.translate(String.format("%s%s", "text.rei.", ClientHelper.getInstance().isCheating() ? "cheat" : "nocheat"));
    }
    
    @NotNull
    @Override
    public Rectangle getBounds() {
        return bounds;
    }
    
    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        List<ItemStack> currentStacks = ClientHelper.getInstance().getInventoryItemsTypes();
        if (shouldReInit) {
            ENTRY_LIST_WIDGET.updateSearch(ScreenHelper.getSearchField().getText(), true);
            init();
        } else {
            for (OverlayDecider decider : DisplayHelper.getInstance().getSortedOverlayDeciders(minecraft.currentScreen.getClass())) {
                if (decider != null && decider.shouldRecalculateArea(ConfigObject.getInstance().getDisplayPanelLocation(), bounds)) {
                    init();
                    break;
                }
            }
        }
        if (ConfigManager.getInstance().isCraftableOnlyEnabled() && ((currentStacks.size() != ScreenHelper.inventoryStacks.size()) || !hasSameListContent(new LinkedList<>(ScreenHelper.inventoryStacks), currentStacks))) {
            ScreenHelper.inventoryStacks = currentStacks;
            ENTRY_LIST_WIDGET.updateSearch(ScreenHelper.getSearchField().getText(), true);
        }
        if (OverlaySearchField.isSearching) {
            matrices.push();
            matrices.translate(0, 0, 200f);
            if (MinecraftClient.getInstance().currentScreen instanceof ContainerScreen) {
                ContainerScreen<?> containerScreen = (ContainerScreen<?>) MinecraftClient.getInstance().currentScreen;
                int x = containerScreen.x, y = containerScreen.y;
                for (Slot slot : containerScreen.getContainer().slots)
                    if (!slot.hasStack() || !ENTRY_LIST_WIDGET.canLastSearchTermsBeAppliedTo(EntryStack.create(slot.getStack())))
                        fillGradient(matrices, x + slot.xPosition, y + slot.yPosition, x + slot.xPosition + 16, y + slot.yPosition + 16, -601874400, -601874400);
            }
            matrices.pop();
        }
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.renderWidgets(matrices, mouseX, mouseY, delta);
        if (MinecraftClient.getInstance().currentScreen instanceof ContainerScreen && ConfigObject.getInstance().areClickableRecipeArrowsEnabled()) {
            ContainerScreen<?> containerScreen = (ContainerScreen<?>) MinecraftClient.getInstance().currentScreen;
            for (RecipeHelper.ScreenClickArea area : RecipeHelper.getInstance().getScreenClickAreas())
                if (area.getScreenClass().equals(MinecraftClient.getInstance().currentScreen.getClass()))
                    if (area.getRectangle().contains(mouseX - containerScreen.x, mouseY - containerScreen.y)) {
                        String collect = CollectionUtils.mapAndJoinToString(area.getCategories(), identifier -> RecipeHelper.getInstance().getCategory(identifier).getCategoryName(), ", ");
                        TOOLTIPS.add(Tooltip.create(new TranslatableText("text.rei.view_recipes_for", collect)));
                        break;
                    }
        }
    }
    
    public void lateRender(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        if (ScreenHelper.isOverlayVisible()) {
            ScreenHelper.getSearchField().laterRender(matrices, mouseX, mouseY, delta);
            for (Widget widget : widgets) {
                if (widget instanceof LateRenderable && wrappedSubsetsMenu != widget && wrappedWeatherMenu != widget && wrappedGameModeMenu != widget)
                    widget.render(matrices, mouseX, mouseY, delta);
            }
        }
        if (wrappedWeatherMenu != null) {
            if (wrappedWeatherMenu.containsMouse(mouseX, mouseY)) {
                TOOLTIPS.clear();
            }
            wrappedWeatherMenu.render(matrices, mouseX, mouseY, delta);
        } else if (wrappedGameModeMenu != null) {
            if (wrappedGameModeMenu.containsMouse(mouseX, mouseY)) {
                TOOLTIPS.clear();
            }
            wrappedGameModeMenu.render(matrices, mouseX, mouseY, delta);
        }
        if (wrappedSubsetsMenu != null) {
            TOOLTIPS.clear();
            wrappedSubsetsMenu.render(matrices, mouseX, mouseY, delta);
        }
        Screen currentScreen = MinecraftClient.getInstance().currentScreen;
        if (!(currentScreen instanceof RecipeViewingScreen) || !((RecipeViewingScreen) currentScreen).choosePageActivated)
            for (Tooltip tooltip : TOOLTIPS) {
                if (tooltip != null)
                    renderTooltip(matrices, tooltip);
            }
        for (Runnable runnable : AFTER_RENDER) {
            runnable.run();
        }
        TOOLTIPS.clear();
        AFTER_RENDER.clear();
    }
    
    public void renderTooltip(MatrixStack matrices, Tooltip tooltip) {
        renderTooltip(matrices, tooltip.getText(), tooltip.getX(), tooltip.getY());
    }
    
    public void renderTooltip(MatrixStack matrices, List<Text> lines, int mouseX, int mouseY) {
        if (lines.isEmpty())
            return;
        tooltipWidth = lines.stream().map(font::getWidth).max(Integer::compareTo).get();
        tooltipHeight = lines.size() <= 1 ? 8 : lines.size() * 10;
        tooltipLines = lines;
        ScreenHelper.drawHoveringWidget(matrices, mouseX, mouseY, renderTooltipCallback, tooltipWidth, tooltipHeight, 0);
    }
    
    private boolean hasSameListContent(List<ItemStack> list1, List<ItemStack> list2) {
        list1.sort(Comparator.comparing(Object::toString));
        list2.sort(Comparator.comparing(Object::toString));
        return CollectionUtils.mapAndJoinToString(list1, Object::toString, "").equals(CollectionUtils.mapAndJoinToString(list2, Object::toString, ""));
    }
    
    public void addTooltip(@Nullable Tooltip tooltip) {
        if (tooltip != null)
            TOOLTIPS.add(tooltip);
    }
    
    public void renderWidgets(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        if (!ScreenHelper.isOverlayVisible())
            return;
        if (!ConfigObject.getInstance().isEntryListWidgetScrolled()) {
            leftButton.setEnabled(ENTRY_LIST_WIDGET.getTotalPages() > 1);
            rightButton.setEnabled(ENTRY_LIST_WIDGET.getTotalPages() > 1);
        }
        for (Widget widget : widgets) {
            if (!(widget instanceof LateRenderable))
                widget.render(matrices, mouseX, mouseY, delta);
        }
    }
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (!ScreenHelper.isOverlayVisible())
            return false;
        if (wrappedSubsetsMenu != null && wrappedSubsetsMenu.mouseScrolled(mouseX, mouseY, amount))
            return true;
        if (wrappedWeatherMenu != null && wrappedWeatherMenu.mouseScrolled(mouseX, mouseY, amount))
            return true;
        if (wrappedGameModeMenu != null && wrappedGameModeMenu.mouseScrolled(mouseX, mouseY, amount))
            return true;
        if (isInside(PointHelper.ofMouse())) {
            if (!ConfigObject.getInstance().isEntryListWidgetScrolled()) {
                if (amount > 0 && leftButton.isEnabled())
                    leftButton.onClick();
                else if (amount < 0 && rightButton.isEnabled())
                    rightButton.onClick();
                else
                    return false;
                return true;
            } else if (ENTRY_LIST_WIDGET.mouseScrolled(mouseX, mouseY, amount))
                return true;
        }
        if (isNotInExclusionZones(PointHelper.getMouseX(), PointHelper.getMouseY())) {
            if (favoritesListWidget != null && favoritesListWidget.mouseScrolled(mouseX, mouseY, amount))
                return true;
        }
        for (Widget widget : widgets)
            if (widget != ENTRY_LIST_WIDGET && (favoritesListWidget == null || widget != favoritesListWidget)
                && (wrappedSubsetsMenu == null || widget != wrappedSubsetsMenu)
                && (wrappedWeatherMenu == null || widget != wrappedWeatherMenu)
                && (wrappedGameModeMenu == null || widget != wrappedGameModeMenu)
                && widget.mouseScrolled(mouseX, mouseY, amount))
                return true;
        return false;
    }
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (ScreenHelper.isOverlayVisible()) {
            if (ScreenHelper.getSearchField().keyPressed(keyCode, scanCode, modifiers))
                return true;
            for (Element listener : widgets)
                if (listener != ScreenHelper.getSearchField() && listener.keyPressed(keyCode, scanCode, modifiers))
                    return true;
        }
        if (ConfigObject.getInstance().getHideKeybind().matchesKey(keyCode, scanCode)) {
            ScreenHelper.toggleOverlayVisible();
            return true;
        }
        ItemStack itemStack = null;
        if (MinecraftClient.getInstance().currentScreen instanceof ContainerScreen) {
            ContainerScreen<?> containerScreen = (ContainerScreen<?>) MinecraftClient.getInstance().currentScreen;
            if (containerScreen.focusedSlot != null && !containerScreen.focusedSlot.getStack().isEmpty())
                itemStack = containerScreen.focusedSlot.getStack();
        }
        if (itemStack != null && !itemStack.isEmpty()) {
            EntryStack stack = EntryStack.create(itemStack.copy());
            if (ConfigObject.getInstance().getRecipeKeybind().matchesKey(keyCode, scanCode)) {
                return ClientHelper.getInstance().openView(ClientHelper.ViewSearchBuilder.builder().addRecipesFor(stack).setOutputNotice(stack).fillPreferredOpenedCategory());
            } else if (ConfigObject.getInstance().getUsageKeybind().matchesKey(keyCode, scanCode)) {
                return ClientHelper.getInstance().openView(ClientHelper.ViewSearchBuilder.builder().addUsagesFor(stack).setInputNotice(stack).fillPreferredOpenedCategory());
            } else if (ConfigObject.getInstance().getFavoriteKeyCode().matchesKey(keyCode, scanCode)) {
                stack.setAmount(127);
                if (!CollectionUtils.anyMatchEqualsEntryIgnoreAmount(ConfigObject.getInstance().getFavorites(), stack))
                    ConfigObject.getInstance().getFavorites().add(stack);
                ConfigManager.getInstance().saveConfig();
                FavoritesListWidget favoritesListWidget = ContainerScreenOverlay.getFavoritesListWidget();
                if (favoritesListWidget != null)
                    favoritesListWidget.updateSearch(ContainerScreenOverlay.getEntryListWidget(), ScreenHelper.getSearchField().getText());
                return true;
            }
        }
        if (!ScreenHelper.isOverlayVisible())
            return false;
        if (ConfigObject.getInstance().getFocusSearchFieldKeybind().matchesKey(keyCode, scanCode)) {
            ScreenHelper.getSearchField().setFocused(true);
            setFocused(ScreenHelper.getSearchField());
            ScreenHelper.getSearchField().keybindFocusTime = System.currentTimeMillis();
            ScreenHelper.getSearchField().keybindFocusKey = keyCode;
            return true;
        }
        return false;
    }
    
    @Override
    public boolean charTyped(char char_1, int int_1) {
        if (!ScreenHelper.isOverlayVisible())
            return false;
        if (ScreenHelper.getSearchField().charTyped(char_1, int_1))
            return true;
        for (Element listener : widgets)
            if (listener != ScreenHelper.getSearchField() && listener.charTyped(char_1, int_1))
                return true;
        return false;
    }
    
    @Override
    public List<Widget> children() {
        return widgets;
    }
    
    @Override
    public boolean mouseClicked(double double_1, double double_2, int int_1) {
        if (!ScreenHelper.isOverlayVisible())
            return false;
        if (wrappedSubsetsMenu != null && wrappedSubsetsMenu.mouseClicked(double_1, double_2, int_1)) {
            this.setFocused(wrappedSubsetsMenu);
            if (int_1 == 0)
                this.setDragging(true);
            ScreenHelper.getSearchField().setFocused(false);
            return true;
        }
        if (wrappedWeatherMenu != null) {
            if (wrappedWeatherMenu.mouseClicked(double_1, double_2, int_1)) {
                this.setFocused(wrappedWeatherMenu);
                if (int_1 == 0)
                    this.setDragging(true);
                ScreenHelper.getSearchField().setFocused(false);
                return true;
            } else if (!wrappedWeatherMenu.containsMouse(double_1, double_2) && !weatherButton.containsMouse(double_1, double_2)) {
                removeWeatherMenu();
            }
        }
        if (wrappedGameModeMenu != null) {
            if (wrappedGameModeMenu.mouseClicked(double_1, double_2, int_1)) {
                this.setFocused(wrappedGameModeMenu);
                if (int_1 == 0)
                    this.setDragging(true);
                ScreenHelper.getSearchField().setFocused(false);
                return true;
            } else if (!wrappedGameModeMenu.containsMouse(double_1, double_2) && !gameModeButton.containsMouse(double_1, double_2)) {
                removeGameModeMenu();
            }
        }
        if (MinecraftClient.getInstance().currentScreen instanceof ContainerScreen && ConfigObject.getInstance().areClickableRecipeArrowsEnabled()) {
            ContainerScreen<?> containerScreen = (ContainerScreen<?>) MinecraftClient.getInstance().currentScreen;
            for (RecipeHelper.ScreenClickArea area : RecipeHelper.getInstance().getScreenClickAreas())
                if (area.getScreenClass().equals(containerScreen.getClass()))
                    if (area.getRectangle().contains(double_1 - containerScreen.x, double_2 - containerScreen.y)) {
                        ClientHelper.getInstance().executeViewAllRecipesFromCategories(Arrays.asList(area.getCategories()));
                        MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                        return true;
                    }
        }
        for (Element element : widgets)
            if (element != wrappedSubsetsMenu && element != wrappedWeatherMenu && element != wrappedGameModeMenu && element.mouseClicked(double_1, double_2, int_1)) {
                this.setFocused(element);
                if (int_1 == 0)
                    this.setDragging(true);
                if (!(element instanceof OverlaySearchField))
                    ScreenHelper.getSearchField().setFocused(false);
                return true;
            }
        return false;
    }
    
    @Override
    public boolean mouseDragged(double double_1, double double_2, int int_1, double double_3, double double_4) {
        if (!ScreenHelper.isOverlayVisible())
            return false;
        return (this.getFocused() != null && this.isDragging() && int_1 == 0) && this.getFocused().mouseDragged(double_1, double_2, int_1, double_3, double_4);
    }
    
    public boolean isInside(double mouseX, double mouseY) {
        return bounds.contains(mouseX, mouseY) && isNotInExclusionZones(mouseX, mouseY);
    }
    
    public boolean isNotInExclusionZones(double mouseX, double mouseY) {
        for (OverlayDecider decider : DisplayHelper.getInstance().getSortedOverlayDeciders(MinecraftClient.getInstance().currentScreen.getClass())) {
            ActionResult in = decider.isInZone(mouseX, mouseY);
            if (in != ActionResult.PASS)
                return in == ActionResult.SUCCESS;
        }
        return true;
    }
    
    public boolean isInside(Point point) {
        return isInside(point.getX(), point.getY());
    }
    
}
