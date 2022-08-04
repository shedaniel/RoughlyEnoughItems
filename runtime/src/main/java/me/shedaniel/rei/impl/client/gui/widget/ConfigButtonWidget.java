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

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.ClientHelper;
import me.shedaniel.rei.api.client.REIRuntime;
import me.shedaniel.rei.api.client.config.ConfigManager;
import me.shedaniel.rei.api.client.config.ConfigObject;
import me.shedaniel.rei.api.client.gui.config.AppearanceTheme;
import me.shedaniel.rei.api.client.gui.config.DisplayPanelLocation;
import me.shedaniel.rei.api.client.gui.config.SyntaxHighlightingMode;
import me.shedaniel.rei.api.client.gui.screen.DisplayScreen;
import me.shedaniel.rei.api.client.gui.widgets.Button;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.impl.client.ClientHelperImpl;
import me.shedaniel.rei.impl.client.REIRuntimeImpl;
import me.shedaniel.rei.impl.client.config.ConfigManagerInternal;
import me.shedaniel.rei.impl.client.gui.InternalTextures;
import me.shedaniel.rei.impl.client.gui.ScreenOverlayImpl;
import me.shedaniel.rei.impl.client.gui.menu.MenuAccess;
import me.shedaniel.rei.impl.client.gui.menu.MenuEntry;
import me.shedaniel.rei.impl.client.gui.menu.entries.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TranslatableComponent;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class ConfigButtonWidget {
    private static final UUID CONFIG_MENU_UUID = UUID.fromString("4357bc36-0a4e-47d2-8e07-ddc220df4a0f");
    
    public static Widget create(ScreenOverlayImpl overlay) {
        Rectangle bounds = getConfigButtonBounds();
        MenuAccess access = overlay.menuAccess();
        Button configButton = Widgets.createButton(bounds, NarratorChatListener.NO_TITLE)
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
                    
                    access.openOrClose(CONFIG_MENU_UUID, button.getBounds(), ConfigButtonWidget::menuEntries);
                })
                .focusable(false)
                .containsMousePredicate((button, point) -> button.getBounds().contains(point) && overlay.isNotInExclusionZones(point.x, point.y));
        Widget overlayWidget = Widgets.createDrawableWidget((helper, matrices, mouseX, mouseY, delta) -> {
            helper.setBlitOffset(helper.getBlitOffset() + 1);
            RenderSystem.setShaderTexture(0, InternalTextures.CHEST_GUI_TEXTURE);
            helper.blit(matrices, bounds.x + 3, bounds.y + 3, 0, 0, 14, 14);
            helper.setBlitOffset(helper.getBlitOffset() - 1);
        });
        return InternalWidgets.wrapLateRenderable(Widgets.concat(configButton, overlayWidget));
    }
    
    private static Collection<MenuEntry> menuEntries() {
        ConfigManagerInternal manager = ConfigManagerInternal.getInstance();
        ConfigObject config = ConfigObject.getInstance();
        return List.of(
                ToggleMenuEntry.of(new TranslatableComponent("text.rei.cheating"),
                        config::isCheating,
                        config::setCheating
                ),
                new EmptyMenuEntry(4),
                new TextMenuEntry(() -> {
                    if (!ClientHelper.getInstance().isCheating())
                        return new TranslatableComponent("text.rei.cheating_disabled");
                    else if (!ClientHelperImpl.getInstance().hasOperatorPermission()) {
                        if (Minecraft.getInstance().gameMode.hasInfiniteItems())
                            return new TranslatableComponent("text.rei.cheating_limited_creative_enabled");
                        else return new TranslatableComponent("text.rei.cheating_enabled_no_perms");
                    } else if (ClientHelperImpl.getInstance().hasPermissionToUsePackets())
                        return new TranslatableComponent("text.rei.cheating_enabled");
                    else
                        return new TranslatableComponent("text.rei.cheating_limited_enabled");
                }),
                new SeparatorMenuEntry(),
                ToggleMenuEntry.ofDeciding(new TranslatableComponent("text.rei.config.menu.dark_theme"),
                        config::isUsingDarkTheme,
                        dark -> {
                            manager.set("appearance.theme", dark ? AppearanceTheme.DARK : AppearanceTheme.LIGHT);
                            return false;
                        }
                ),
                ToggleMenuEntry.of(new TranslatableComponent("text.rei.config.menu.craftable_filter"),
                        config::isCraftableFilterEnabled,
                        bool -> manager.set("appearance.layout.showCraftableOnlyButton", bool)
                ),
                new SubMenuEntry(new TranslatableComponent("text.rei.config.menu.display"), List.of(
                        ToggleMenuEntry.of(new TranslatableComponent("text.rei.config.menu.display.remove_recipe_book"),
                                config::doesDisableRecipeBook,
                                disableRecipeBook -> {
                                    manager.set("functionality.disableRecipeBook", disableRecipeBook);
                                    Screen screen = Minecraft.getInstance().screen;
                                    
                                    if (screen != null) {
                                        screen.init(Minecraft.getInstance(), screen.width, screen.height);
                                    }
                                }
                        ),
                        ToggleMenuEntry.of(new TranslatableComponent("text.rei.config.menu.display.left_side_mob_effects"),
                                config::isLeftSideMobEffects,
                                disableRecipeBook -> {
                                    manager.set("functionality.leftSideMobEffects", disableRecipeBook);
                                    Screen screen = Minecraft.getInstance().screen;
                                    
                                    if (screen != null) {
                                        screen.init(Minecraft.getInstance(), screen.width, screen.height);
                                    }
                                }
                        ),
                        ToggleMenuEntry.of(new TranslatableComponent("text.rei.config.menu.display.left_side_panel"),
                                config::isLeftHandSidePanel,
                                bool -> manager.set("advanced.accessibility.displayPanelLocation", bool ? DisplayPanelLocation.LEFT : DisplayPanelLocation.RIGHT)
                        ),
                        ToggleMenuEntry.of(new TranslatableComponent("text.rei.config.menu.display.scrolling_side_panel"),
                                config::isEntryListWidgetScrolled,
                                bool -> manager.set("appearance.scrollingEntryListWidget", bool)
                        ),
                        new SeparatorMenuEntry(),
                        ToggleMenuEntry.of(new TranslatableComponent("text.rei.config.menu.display.caching_entry_rendering"),
                                config::doesCacheEntryRendering,
                                bool -> manager.set("advanced.miscellaneous.cachingFastEntryRendering", bool)
                        ),
                        new SeparatorMenuEntry(),
                        ToggleMenuEntry.of(new TranslatableComponent("text.rei.config.menu.display.syntax_highlighting"),
                                () -> config.getSyntaxHighlightingMode() == SyntaxHighlightingMode.COLORFUL || config.getSyntaxHighlightingMode() == SyntaxHighlightingMode.COLORFUL_UNDERSCORED,
                                bool -> manager.set("appearance.syntaxHighlightingMode", bool ? SyntaxHighlightingMode.COLORFUL : SyntaxHighlightingMode.PLAIN_UNDERSCORED)
                        )
                )),
                new SeparatorMenuEntry(),
                ToggleMenuEntry.ofDeciding(new TranslatableComponent("text.rei.config.menu.config"),
                        () -> false,
                        $ -> {
                            ConfigManager.getInstance().openConfigScreen(REIRuntime.getInstance().getPreviousScreen());
                            return false;
                        })
        );
    }
    
    private static Rectangle getConfigButtonBounds() {
        if (ConfigObject.getInstance().isLowerConfigButton()) {
            Rectangle area = REIRuntimeImpl.getSearchField().getBounds().clone();
            area.setLocation(area.x + area.width + (ConfigObject.getInstance().isCraftableFilterEnabled() ? 26 : 4), area.y - 1);
            area.setSize(20, 20);
            return area;
        }
        Window window = Minecraft.getInstance().getWindow();
        return new Rectangle(ConfigObject.getInstance().isLeftHandSidePanel() ? window.getGuiScaledWidth() - 30 : 10, 10, 20, 20);
    }
}
