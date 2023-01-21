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

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.ClientHelper;
import me.shedaniel.rei.api.client.REIRuntime;
import me.shedaniel.rei.api.client.config.ConfigManager;
import me.shedaniel.rei.api.client.config.ConfigObject;
import me.shedaniel.rei.api.client.favorites.FavoriteMenuEntry;
import me.shedaniel.rei.api.client.gui.config.DisplayPanelLocation;
import me.shedaniel.rei.api.client.gui.config.SyntaxHighlightingMode;
import me.shedaniel.rei.api.client.gui.screen.DisplayScreen;
import me.shedaniel.rei.api.client.gui.widgets.Button;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.impl.client.ClientHelperImpl;
import me.shedaniel.rei.impl.client.REIRuntimeImpl;
import me.shedaniel.rei.impl.client.config.ConfigManagerImpl;
import me.shedaniel.rei.impl.client.config.ConfigObjectImpl;
import me.shedaniel.rei.impl.client.gui.InternalTextures;
import me.shedaniel.rei.impl.client.gui.ScreenOverlayImpl;
import me.shedaniel.rei.impl.client.gui.modules.MenuAccess;
import me.shedaniel.rei.impl.client.gui.modules.entries.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class ConfigButtonWidget {
    private static final UUID CONFIG_MENU_UUID = UUID.fromString("4357bc36-0a4e-47d2-8e07-ddc220df4a0f");
    
    public static Widget create(ScreenOverlayImpl overlay) {
        Rectangle bounds = getConfigButtonBounds();
        MenuAccess access = overlay.menuAccess();
        Button configButton = Widgets.createButton(bounds, Component.empty())
                .onClick(button -> {
                    if (Screen.hasShiftDown() || Screen.hasControlDown()) {
                        ClientHelper.getInstance().setCheating(!ClientHelper.getInstance().isCheating());
                        return;
                    }
                    ConfigManager.getInstance().openConfigScreen(REIRuntime.getInstance().getPreviousScreen());
                })
                .onRender((matrices, button) -> {
                    if (!ClientHelper.getInstance().isCheating() || Minecraft.getInstance().screen instanceof DisplayScreen) {
                        button.removeTint();
                    } else if (!ClientHelperImpl.getInstance().hasOperatorPermission()) {
                        if (Minecraft.getInstance().gameMode.hasInfiniteItems()) {
                            button.setTint(0x2aff0000);
                        } else {
                            button.setTint(0x58fcf003);
                        }
                    } else if (ClientHelperImpl.getInstance().hasPermissionToUsePackets()) {
                        button.setTint(0x2aff0000);
                    } else {
                        button.setTint(0x5800afff);
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
        return Widgets.concat(configButton, overlayWidget);
    }
    
    private static Collection<FavoriteMenuEntry> menuEntries() {
        ConfigObjectImpl config = ConfigManagerImpl.getInstance().getConfig();
        return List.of(
                ToggleMenuEntry.of(Component.translatable("text.rei.cheating"),
                        config::isCheating,
                        config::setCheating
                ),
                new EmptyMenuEntry(4),
                new TextMenuEntry(() -> {
                    if (!ClientHelper.getInstance().isCheating())
                        return Component.translatable("text.rei.cheating_disabled");
                    else if (!ClientHelperImpl.getInstance().hasOperatorPermission()) {
                        if (Minecraft.getInstance().gameMode.hasInfiniteItems())
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
                new SubMenuEntry(Component.translatable("text.rei.config.menu.display"), List.of(
                        ToggleMenuEntry.of(Component.translatable("text.rei.config.menu.display.remove_recipe_book"),
                                config::doesDisableRecipeBook,
                                disableRecipeBook -> {
                                    config.setDisableRecipeBook(disableRecipeBook);
                                    Screen screen = Minecraft.getInstance().screen;
                                    
                                    if (screen != null) {
                                        screen.init(Minecraft.getInstance(), screen.width, screen.height);
                                    }
                                }
                        ),
                        ToggleMenuEntry.of(Component.translatable("text.rei.config.menu.display.left_side_mob_effects"),
                                config::isLeftSideMobEffects,
                                disableRecipeBook -> {
                                    config.setLeftSideMobEffects(disableRecipeBook);
                                    Screen screen = Minecraft.getInstance().screen;
                                    
                                    if (screen != null) {
                                        screen.init(Minecraft.getInstance(), screen.width, screen.height);
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
