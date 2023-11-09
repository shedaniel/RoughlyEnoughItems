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

package me.shedaniel.rei.impl.client.gui.config.components;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import me.shedaniel.clothconfig2.api.ModifierKeyCode;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.math.impl.PointHelper;
import me.shedaniel.rei.api.client.gui.widgets.Label;
import me.shedaniel.rei.api.client.gui.widgets.WidgetWithBounds;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.util.MatrixUtils;
import me.shedaniel.rei.api.common.util.CollectionUtils;
import me.shedaniel.rei.impl.client.gui.config.ConfigAccess;
import me.shedaniel.rei.impl.client.gui.config.REIConfigScreen;
import me.shedaniel.rei.impl.client.gui.config.options.CompositeOption;
import me.shedaniel.rei.impl.client.gui.config.options.OptionValueEntry;
import me.shedaniel.rei.impl.client.gui.modules.Menu;
import me.shedaniel.rei.impl.client.gui.modules.entries.ToggleMenuEntry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static me.shedaniel.rei.impl.client.gui.config.options.ConfigUtils.literal;
import static me.shedaniel.rei.impl.client.gui.config.options.ConfigUtils.translatable;

public class ConfigOptionValueWidget {
    public static <T> WidgetWithBounds create(ConfigAccess access, CompositeOption<T> option, int width) {
        Font font = Minecraft.getInstance().font;
        Component[] text = {null};
        Consumer<Component> setText = t -> {
            if (access.getFocusedKeycode() == option) {
                text[0] = literal("> ").withStyle(ChatFormatting.YELLOW)
                        .append(t.copy().withStyle(ChatFormatting.YELLOW))
                        .append(literal(" <").withStyle(ChatFormatting.YELLOW));
            } else if (!(option.getEntry() instanceof OptionValueEntry.Configure<T>) && access.get(option).equals(Objects.requireNonNullElseGet(option.getDefaultValue(), () -> access.getDefault(option)))) {
                text[0] = translatable("config.rei.value.default", t);
                
                if (font.width(text[0]) > width) {
                    int trimTo = width - font.width("...") - (font.width(text[0]) - font.width(t));
                    FormattedText trimmed = font.substrByWidth(t, trimTo);
                    FormattedText composite = FormattedText.composite(trimmed, literal("..."));
                    text[0] = literal(composite.getString());
                    text[0] = translatable("config.rei.value.default", text[0]);
                }
            } else if (font.width(t) > width) {
                int trimTo = width - font.width("...");
                FormattedText trimmed = font.substrByWidth(t, trimTo);
                FormattedText composite = FormattedText.composite(trimmed, literal("..."));
                text[0] = literal(composite.getString());
            } else {
                text[0] = t;
            }
        };
        
        setText.accept(option.getEntry().getOption(access.get(option)));
        
        Matrix4f[] matrix = {new Matrix4f()};
        Label label = Widgets.createLabel(new Point(), text[0]).rightAligned()
                .color(0xFFE0E0E0)
                .hoveredColor(0xFFE0E0E0)
                .onRender((poses, l) -> {
                    if (MatrixUtils.transform(matrix[0], l.getBounds()).contains(PointHelper.ofMouse())) {
                        l.setMessage(text[0].copy().withStyle(ChatFormatting.UNDERLINE));
                    } else {
                        l.setMessage(text[0]);
                    }
                });
        
        if (option.getEntry() instanceof OptionValueEntry.Selection<T> selection) {
            applySelection(access, option, selection, label, setText, matrix);
        } else if (access.get(option) instanceof ModifierKeyCode) {
            applyKeycode(access, option, label, setText, matrix);
        } else if (option.getEntry() instanceof OptionValueEntry.Configure<T>) {
            label.clickable().onClick($ -> {
                ((OptionValueEntry.Configure<T>) option.getEntry()).configure(access, option, () -> {
                    Minecraft.getInstance().setScreen((Screen) access);
                    setText.accept(option.getEntry().getOption(access.get(option)));
                });
            });
        }
        
        return Widgets.concatWithBounds(() -> new Rectangle(-label.getBounds().width, 0, label.getBounds().width + 8, 14),
                label,
                Widgets.createDrawableWidget((helper, matrices, mouseX, mouseY, delta) -> matrix[0] = matrices.last().pose()),
                Widgets.withTranslate(Widgets.createTexturedWidget(new ResourceLocation("roughlyenoughitems:textures/gui/config/selector.png"),
                        new Rectangle(1, 1, 4, 6), 0, 0, 1, 1, 1, 1), 0, 0.5, 0)
        );
    }
    
    private static <T> void applySelection(ConfigAccess access, CompositeOption<T> option, OptionValueEntry.Selection<T> selection, Label label, Consumer<Component> setText, Matrix4f[] matrix) {
        int noOfOptions = selection.getOptions().size();
        if (noOfOptions == 2) {
            label.clickable().onClick($ -> {
                access.set(option, selection.getOptions().get((selection.getOptions().indexOf(access.get(option)) + 1) % 2));
                setText.accept(selection.getOption(access.get(option)));
            });
        } else if (noOfOptions >= 2) {
            label.clickable().onClick($ -> {
                Menu menu = new Menu(MatrixUtils.transform(matrix[0], label.getBounds()), CollectionUtils.map(selection.getOptions(), opt -> {
                    Component selectionOption = selection.getOption(opt);
                    if (opt.equals(access.getDefault(option))) {
                        selectionOption = translatable("config.rei.value.default", selectionOption);
                    }
                    
                    return ToggleMenuEntry.of(selectionOption, () -> false, o -> {
                        ((REIConfigScreen) Minecraft.getInstance().screen).closeMenu();
                        access.set(option, opt);
                        setText.accept(selection.getOption(opt));
                    });
                }), false);
                access.closeMenu();
                access.openMenu(menu);
            });
        }
    }
    
    private static <T> void applyKeycode(ConfigAccess access, CompositeOption<T> option, Label label, Consumer<Component> setText, Matrix4f[] matrix) {
        label.clickable().onClick($ -> {
            access.closeMenu();
            access.focusKeycode((CompositeOption<ModifierKeyCode>) option);
        });
        BiConsumer<PoseStack, Label> render = label.getOnRender();
        label.onRender((poses, $) -> {
            render.accept(poses, $);
            setText.accept(((ModifierKeyCode) access.get(option)).getLocalizedName());
        });
    }
}
