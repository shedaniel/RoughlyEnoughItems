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

package me.shedaniel.rei.impl.client.gui.overlay.widgets;

import com.mojang.math.Vector4f;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.REIRuntime;
import me.shedaniel.rei.api.client.config.ConfigManager;
import me.shedaniel.rei.api.client.config.ConfigObject;
import me.shedaniel.rei.api.client.favorites.FavoriteMenuEntry;
import me.shedaniel.rei.api.client.gui.config.SearchFieldLocation;
import me.shedaniel.rei.api.client.gui.widgets.Button;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.overlay.OverlayListWidget;
import me.shedaniel.rei.api.client.overlay.ScreenOverlay;
import me.shedaniel.rei.api.common.util.CollectionUtils;
import me.shedaniel.rei.impl.client.config.ConfigManagerInternal;
import me.shedaniel.rei.impl.client.gui.menu.MenuAccess;
import me.shedaniel.rei.impl.client.gui.overlay.menu.entries.SeparatorMenuEntry;
import me.shedaniel.rei.impl.client.gui.overlay.menu.entries.SubMenuEntry;
import me.shedaniel.rei.impl.client.gui.overlay.menu.entries.ToggleMenuEntry;
import me.shedaniel.rei.impl.client.gui.overlay.menu.provider.OverlayMenuEntryProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

import java.util.*;

public class CraftableFilterButtonWidgetProvider implements OverlayWidgetProvider {
    public static final UUID FILTER_MENU_UUID = UUID.fromString("2839e998-1679-4f9e-a257-37411d16f1e6");
    
    @Override
    public void provide(ScreenOverlay overlay, MenuAccess access, WidgetSink sink) {
        if (ConfigObject.getInstance().isCraftableFilterEnabled()) {
            sink.acceptLateRendered(create(overlay, access));
        }
    }
    
    private static Widget create(ScreenOverlay overlay, MenuAccess access) {
        Rectangle bounds = getCraftableFilterBounds();
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        ItemStack icon = new ItemStack(Blocks.CRAFTING_TABLE);
        Button filterButton = Widgets.createButton(bounds, NarratorChatListener.NO_TITLE)
                .focusable(false)
                .onClick(button -> {
                    ConfigManager.getInstance().toggleCraftableOnly();
                    REIRuntime.getInstance().getOverlay().map(ScreenOverlay::getEntryList).ifPresent(OverlayListWidget::queueReloadSearch);
                })
                .onRender((matrices, button) -> {
                    button.setTint(ConfigManager.getInstance().isCraftableOnlyEnabled() ? 0x3800d907 : 0x38ff0000);
                    
                    access.openOrClose(FILTER_MENU_UUID, button.getBounds(), CraftableFilterButtonWidgetProvider::menuEntries);
                })
                .containsMousePredicate((button, point) -> button.getBounds().contains(point) && overlay.isNotInExclusionZones(point.x, point.y))
                .tooltipLineSupplier(button -> new TranslatableComponent(ConfigManager.getInstance().isCraftableOnlyEnabled() ? "text.rei.showing_craftable" : "text.rei.showing_all"));
        Widget overlayWidget = Widgets.createDrawableWidget((helper, matrices, mouseX, mouseY, delta) -> {
            Vector4f vector = new Vector4f(bounds.x + 2, bounds.y + 2, helper.getBlitOffset() - 10, 1.0F);
            vector.transform(matrices.last().pose());
            itemRenderer.blitOffset = vector.z();
            itemRenderer.renderGuiItem(icon, (int) vector.x(), (int) vector.y());
            itemRenderer.blitOffset = 0.0F;
        });
        return Widgets.concat(filterButton, overlayWidget);
    }
    
    private static Collection<FavoriteMenuEntry> menuEntries() {
        ConfigManagerInternal manager = ConfigManagerInternal.getInstance();
        ConfigObject config = ConfigObject.getInstance();
        ArrayList<FavoriteMenuEntry> entries = new ArrayList<>(List.of(
                new SubMenuEntry(new TranslatableComponent("text.rei.config.menu.search_field.position"), Arrays.stream(SearchFieldLocation.values())
                        .<FavoriteMenuEntry>map(location -> ToggleMenuEntry.of(new TextComponent(location.toString()),
                                        () -> config.getSearchFieldLocation() == location,
                                        bool -> manager.set("appearance.layout.searchFieldLocation", location))
                                .withActive(() -> config.getSearchFieldLocation() != location)
                        )
                        .toList())
        ));
        
        List<List<FavoriteMenuEntry>> additionalEntries = new ArrayList<>();
        
        for (OverlayMenuEntryProvider provider : OverlayMenuEntryProvider.PROVIDERS) {
            List<FavoriteMenuEntry> provided = provider.provide(OverlayMenuEntryProvider.Type.CRAFTABLE_FILTER);
            
            if (provided != null && !provided.isEmpty()) {
                additionalEntries.add(provided);
            }
        }
        
        for (List<FavoriteMenuEntry> additionalEntryList : additionalEntries) {
            if (additionalEntryList.size() > 1) {
                entries.add(new SeparatorMenuEntry());
                entries.addAll(additionalEntryList);
            }
        }
        
        List<FavoriteMenuEntry> singleEntries = CollectionUtils.flatMap(CollectionUtils.filterToList(additionalEntries, list -> list.size() == 1), list -> list);
        
        if (!singleEntries.isEmpty()) {
            entries.add(new SeparatorMenuEntry());
            entries.addAll(singleEntries);
        }
        
        return entries;
    }
    
    private static Rectangle getCraftableFilterBounds() {
        Rectangle area = REIRuntime.getInstance().getSearchTextField().asWidget().getBounds().clone();
        area.setLocation(area.x + area.width + 4, area.y - 1);
        area.setSize(20, 20);
        return area;
    }
}
