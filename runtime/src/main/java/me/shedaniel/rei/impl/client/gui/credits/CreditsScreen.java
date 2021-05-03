/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020, 2021 shedaniel
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

package me.shedaniel.rei.impl.client.gui.credits;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import me.shedaniel.architectury.platform.Platform;
import me.shedaniel.clothconfig2.impl.EasingMethod;
import me.shedaniel.rei.api.client.config.ConfigObject;
import me.shedaniel.rei.api.common.util.ImmutableTextComponent;
import me.shedaniel.rei.impl.client.gui.credits.CreditsEntryListWidget.TextCreditsItem;
import me.shedaniel.rei.impl.client.gui.credits.CreditsEntryListWidget.TranslationCreditsItem;
import me.shedaniel.rei.impl.client.gui.screen.TransformingScreen;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.Mth;
import net.minecraft.util.Tuple;
import org.apache.commons.lang3.mutable.MutableLong;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@ApiStatus.Internal
public class CreditsScreen extends Screen {
    
    private Screen parent;
    private AbstractButton buttonDone;
    private CreditsEntryListWidget entryListWidget;
    
    public CreditsScreen(Screen parent) {
        super(new TextComponent(""));
        this.parent = parent;
    }
    
    @Override
    public boolean keyPressed(int int_1, int int_2, int int_3) {
        if (int_1 == 256 && this.shouldCloseOnEsc()) {
            openPrevious();
            return true;
        }
        return super.keyPressed(int_1, int_2, int_3);
    }
    
    @Override
    public void init() {
        children.add(entryListWidget = new CreditsEntryListWidget(minecraft, width, height, 32, height - 32));
        entryListWidget.creditsClearEntries();
        List<Tuple<String, String>> translators = Lists.newArrayList();
        Exception[] exception = {null};
        /*FabricLoader.getInstance().getModContainer("roughlyenoughitems").ifPresent(rei -> {
            try {
                if (rei.getMetadata().containsCustomValue("rei:translators")) {
                    CustomValue.CvObject jsonObject = rei.getMetadata().getCustomValue("rei:translators").getAsObject();
                    jsonObject.forEach(entry -> {
                        CustomValue value = entry.getValue();
                        String behind = value.getType() == CustomValue.CvType.ARRAY ? Lists.newArrayList(value.getAsArray().iterator()).stream().map(CustomValue::getAsString).sorted(String::compareToIgnoreCase).collect(Collectors.joining(", ")) : value.getAsString();
                        translators.add(new Tuple<>(entry.getKey(), behind));
                    });
                }
                translators.sort(Comparator.comparing(Tuple::getA, String::compareToIgnoreCase));
            } catch (Exception e) {
                exception[0] = e;
                e.printStackTrace();
            }
        });*/
        List<Tuple<String, String>> translatorsMapped = translators.stream().map(pair -> {
            return new Tuple<>(
                    "  " + (I18n.exists("language.roughlyenoughitems." + pair.getA().toLowerCase(Locale.ROOT).replace(' ', '_')) ? I18n.get("language.roughlyenoughitems." + pair.getA().toLowerCase(Locale.ROOT).replace(' ', '_')) : pair.getA()),
                    pair.getB()
            );
        }).collect(Collectors.toList());
        int i = width - 80 - 6;
        for (String line : String.format("§lRoughly Enough Items (v%s)\n§7Originally a fork for Almost Enough Items.\n\n§lLanguage Translation\n%s\n\n§lLicense\n§7Roughly Enough Items is licensed under MIT.", Platform.getMod("roughlyenoughitems").getVersion(), "%translators%").split("\n"))
            if (line.equalsIgnoreCase("%translators%")) {
                if (exception[0] != null) {
                    entryListWidget.creditsAddEntry(new TextCreditsItem(new ImmutableTextComponent("Failed to get translators: " + exception[0].toString())));
                    for (StackTraceElement traceElement : exception[0].getStackTrace())
                        entryListWidget.creditsAddEntry(new TextCreditsItem(new ImmutableTextComponent("  at " + traceElement)));
                } else {
                    int maxWidth = translatorsMapped.stream().mapToInt(pair -> font.width(pair.getA())).max().orElse(0) + 5;
                    for (Tuple<String, String> pair : translatorsMapped) {
                        entryListWidget.creditsAddEntry(new TranslationCreditsItem(new TranslatableComponent(pair.getA()), new TranslatableComponent(pair.getB()), i - maxWidth - 10, maxWidth));
                    }
                }
            } else entryListWidget.creditsAddEntry(new TextCreditsItem(new ImmutableTextComponent(line)));
        entryListWidget.creditsAddEntry(new TextCreditsItem(NarratorChatListener.NO_TITLE));
        entryListWidget.creditsAddEntry(new CreditsEntryListWidget.LinkItem(new ImmutableTextComponent("Visit the project at GitHub."), "https://www.github.com/shedaniel/RoughlyEnoughItems", entryListWidget.getItemWidth(), false));
        entryListWidget.creditsAddEntry(new CreditsEntryListWidget.LinkItem(new ImmutableTextComponent("Visit the project page at CurseForge."), "https://www.curseforge.com/minecraft/mc-mods/roughly-enough-items", entryListWidget.getItemWidth(), false));
        entryListWidget.creditsAddEntry(new CreditsEntryListWidget.LinkItem(new ImmutableTextComponent("Support the project via Patreon!"), "https://patreon.com/shedaniel", entryListWidget.getItemWidth(), true));
        entryListWidget.creditsAddEntry(new TextCreditsItem(NarratorChatListener.NO_TITLE));
        children.add(buttonDone = new Button(width / 2 - 100, height - 26, 200, 20, new TranslatableComponent("gui.done"), button -> openPrevious()));
    }
    
    private void openPrevious() {
        MutableLong current = new MutableLong(0);
        Minecraft.getInstance().setScreen(new TransformingScreen(true, parent,
                this,
                () -> current.setValue(current.getValue() == 0 ? Util.getMillis() + (!ConfigObject.getInstance().isCreditsScreenAnimated() ? -3000 : 0) : current.getValue()),
                () -> EasingMethod.EasingMethodImpl.EXPO.apply(Mth.clamp((Util.getMillis() - current.getValue()) / 750.0, 0, 1))
                      * Minecraft.getInstance().getWindow().getGuiScaledWidth(),
                () -> 0,
                () -> Util.getMillis() - current.getValue() > 800));
    }
    
    @Override
    public boolean mouseScrolled(double double_1, double double_2, double double_3) {
        if (entryListWidget.mouseScrolled(double_1, double_2, double_3))
            return true;
        return super.mouseScrolled(double_1, double_2, double_3);
    }
    
    @Override
    public void render(PoseStack matrices, int int_1, int int_2, float float_1) {
        this.renderDirtBackground(0);
        this.entryListWidget.render(matrices, int_1, int_2, float_1);
        drawCenteredString(matrices, this.font, I18n.get("text.rei.credits"), this.width / 2, 16, 16777215);
        super.render(matrices, int_1, int_2, float_1);
        buttonDone.render(matrices, int_1, int_2, float_1);
    }
    
}
