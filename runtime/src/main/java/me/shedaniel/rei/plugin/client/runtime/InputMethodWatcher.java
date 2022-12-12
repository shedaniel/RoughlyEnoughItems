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

package me.shedaniel.rei.plugin.client.runtime;

import me.shedaniel.math.Color;
import me.shedaniel.math.Point;
import me.shedaniel.rei.api.client.config.ConfigManager;
import me.shedaniel.rei.api.client.config.ConfigObject;
import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import me.shedaniel.rei.api.client.search.method.InputMethod;
import me.shedaniel.rei.api.client.search.method.InputMethodRegistry;
import me.shedaniel.rei.api.common.plugins.PluginManager;
import me.shedaniel.rei.api.common.util.CollectionUtils;
import me.shedaniel.rei.impl.client.config.ConfigManagerImpl;
import me.shedaniel.rei.impl.client.gui.ScreenOverlayImpl;
import me.shedaniel.rei.impl.client.gui.hints.HintProvider;
import me.shedaniel.rei.impl.client.gui.modules.MenuAccess;
import me.shedaniel.rei.impl.client.gui.widget.CraftableFilterButtonWidget;
import me.shedaniel.rei.impl.client.gui.widget.search.OverlaySearchField;
import me.shedaniel.rei.impl.client.search.method.DefaultInputMethod;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class InputMethodWatcher implements HintProvider {
    @Override
    public List<Component> provide() {
        if (PluginManager.areAnyReloading() || OverlaySearchField.isHighlighting) return Collections.emptyList();
        ResourceLocation id = ConfigObject.getInstance().getInputMethodId();
        if (id == null) {
            String languageCode = Minecraft.getInstance().options.languageCode;
            MutableComponent component = Component.empty();
            int match = 0;
            for (InputMethod<?> method : InputMethodRegistry.getInstance().getAll().values()) {
                if (method instanceof DefaultInputMethod) continue;
                if (CollectionUtils.anyMatch(method.getMatchingLocales(), locale -> locale.code().equals(languageCode))) {
                    if (!component.getString().isEmpty()) {
                        component.append(", ");
                    }
                    
                    component.append(method.getName());
                    match++;
                }
            }
            if (match > 0) {
                return List.of(Component.translatable("text.rei.input.methods.hint"),
                        Component.literal(" "), component);
            }
        }
        
        return Collections.emptyList();
    }
    
    @Override
    @Nullable
    public Tooltip provideTooltip(Point mouse) {
        return null;
    }
    
    @Override
    public Color getColor() {
        return Color.ofTransparent(0x50ffadca);
    }
    
    @Override
    public List<HintButton> getButtons() {
        return List.of(
                new HintButton(Component.translatable("text.rei.hint.configure"), bounds -> {
                    MenuAccess access = ScreenOverlayImpl.getInstance().menuAccess();
                    access.openOrClose(CraftableFilterButtonWidget.FILTER_MENU_UUID, bounds.clone(),
                            () -> CraftableFilterButtonWidget.createInputMethodEntries(access, CraftableFilterButtonWidget.getApplicableInputMethods()));
                }),
                new HintButton(Component.translatable("text.rei.hint.ignore"), bounds -> {
                    ConfigManagerImpl.getInstance().getConfig().setInputMethodId(new ResourceLocation("rei:default"));
                    ConfigManager.getInstance().saveConfig();
                })
        );
    }
}
