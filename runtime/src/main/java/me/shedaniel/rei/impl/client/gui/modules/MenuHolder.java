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

package me.shedaniel.rei.impl.client.gui.modules;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import me.shedaniel.math.Point;
import me.shedaniel.math.impl.PointHelper;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.impl.client.gui.ScreenOverlayImpl;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class MenuHolder implements MenuAccess {
    public final List<Runnable> afterRenders = Lists.newArrayList();
    @Nullable
    private OverlayMenu menu = null;
    
    @Override
    public boolean isOpened(UUID uuid) {
        return menu != null && menu.uuid.equals(uuid);
    }
    
    @Override
    public boolean isAnyOpened() {
        return menu != null;
    }
    
    @Override
    public boolean isInBounds(UUID uuid) {
        return isOpened(uuid) && menu.inBounds.test(PointHelper.ofMouse());
    }
    
    private void proceedOpenMenu(UUID uuid, Runnable runnable) {
        proceedOpenMenuOrElse(uuid, runnable, menu -> {});
    }
    
    private void proceedOpenMenuOrElse(UUID uuid, Runnable runnable, Consumer<OverlayMenu> orElse) {
        if (menu == null || !menu.uuid.equals(uuid)) {
            close();
            runnable.run();
        } else {
            orElse.accept(this.menu);
        }
    }
    
    @Override
    public void open(UUID uuid, Menu menu) {
        open(uuid, menu, point -> false, point -> true);
    }
    
    @Override
    public void open(UUID uuid, Menu menu, Predicate<Point> or, Predicate<Point> and) {
        this.menu = new OverlayMenu(uuid, menu, Widgets.withTranslate(menu, 0, 0, 400), or, and);
    }
    
    @Override
    public boolean isValidPoint(Point point) {
        return ScreenOverlayImpl.getInstance().isNotInExclusionZones(point.x, point.y);
    }
    
    @Override
    public void close() {
        this.menu = null;
    }
    
    public void afterRender() {
        for (Runnable runnable : afterRenders) {
            runnable.run();
        }
        afterRenders.clear();
    }
    
    @Nullable
    public Widget widget() {
        return menu != null ? menu.wrappedMenu : null;
    }
    
    public void lateRender(PoseStack matrices, int mouseX, int mouseY, float delta) {
        if (menu != null) {
            if (!menu.inBounds.test(PointHelper.ofMouse())) {
                close();
            } else {
                if (menu.wrappedMenu.containsMouse(mouseX, mouseY)) {
                    ScreenOverlayImpl.getInstance().clearTooltips();
                }
                menu.wrappedMenu.render(matrices, mouseX, mouseY, delta);
            }
        }
    }
    
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        return menu != null && menu.wrappedMenu.mouseScrolled(mouseX, mouseY, amount);
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
}
