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

package me.shedaniel.rei.impl.client.gui.credits;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.architectury.platform.Platform;
import me.shedaniel.rei.api.common.util.ImmutableTextComponent;
import me.shedaniel.rei.impl.client.gui.credits.CreditsEntryListWidget.TextCreditsItem;
import me.shedaniel.rei.impl.client.gui.credits.CreditsEntryListWidget.TranslationCreditsItem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.Tuple;
import org.jetbrains.annotations.ApiStatus;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@ApiStatus.Internal
public class CreditsScreen extends Screen {
    private final Screen parent;
    private AbstractButton buttonDone;
    private CreditsEntryListWidget entryListWidget;
    
    public CreditsScreen(Screen parent) {
        super(new TextComponent(""));
        this.parent = parent;
    }
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256 && this.shouldCloseOnEsc()) {
            openPrevious();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
    
    public static class TranslatorEntry {
        private final String name;
        private final boolean proofreader;
        
        public TranslatorEntry(String name) {
            this(name, false);
        }
        
        public TranslatorEntry(String name, boolean proofreader) {
            this.name = name;
            this.proofreader = proofreader;
        }
        
        public String getName() {
            return name;
        }
    }
    
    @Override
    public void init() {
        addWidget(entryListWidget = new CreditsEntryListWidget(minecraft, width, height, 32, height - 32));
        entryListWidget.creditsClearEntries();
        List<Tuple<String, List<TranslatorEntry>>> translators = Lists.newArrayList();
        Exception[] exception = {null};
        fillTranslators(exception, translators);
        List<Tuple<String, List<TranslatorEntry>>> translatorsMapped = translators.stream().map(pair -> {
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
                    for (Tuple<String, List<TranslatorEntry>> pair : translatorsMapped) {
                        MutableComponent text = new TextComponent("");
                        boolean isFirst = true;
                        for (TranslatorEntry entry : pair.getB()) {
                            if (!isFirst) {
                                text = text.append(new TextComponent(", "));
                            }
                            isFirst = false;
                            MutableComponent component = new TextComponent(entry.getName());
                            if (entry.proofreader)
                                component = component.withStyle(ChatFormatting.GOLD);
                            text = text.append(component);
                        }
                        entryListWidget.creditsAddEntry(new TranslationCreditsItem(new TranslatableComponent(pair.getA()), text, i - maxWidth - 10, maxWidth));
                    }
                }
            } else entryListWidget.creditsAddEntry(new TextCreditsItem(new ImmutableTextComponent(line)));
        entryListWidget.creditsAddEntry(new TextCreditsItem(NarratorChatListener.NO_TITLE));
        entryListWidget.creditsAddEntry(new CreditsEntryListWidget.LinkItem(new ImmutableTextComponent("Visit the project at GitHub."), "https://www.github.com/shedaniel/RoughlyEnoughItems", entryListWidget.getItemWidth(), false));
        entryListWidget.creditsAddEntry(new CreditsEntryListWidget.LinkItem(new ImmutableTextComponent("Visit the project page at CurseForge."), "https://www.curseforge.com/minecraft/mc-mods/roughly-enough-items", entryListWidget.getItemWidth(), false));
        entryListWidget.creditsAddEntry(new CreditsEntryListWidget.LinkItem(new ImmutableTextComponent("Support the project via Patreon!"), "https://patreon.com/shedaniel", entryListWidget.getItemWidth(), true));
        entryListWidget.creditsAddEntry(new TextCreditsItem(NarratorChatListener.NO_TITLE));
        addRenderableWidget(buttonDone = new Button(width / 2 - 100, height - 26, 200, 20, new TranslatableComponent("gui.done"), button -> openPrevious()));
    }
    
    private static void fillTranslators(Exception[] exception, List<Tuple<String, List<TranslatorEntry>>> translators) {
        try {
            String s = "me.shedaniel.rei.impl.client.gui.credits.%s.CreditsScreenImpl";
            Class.forName(s.contains("%s") ? s.formatted(Platform.isForge() ? "forge" : "fabric") : s)
                    .getDeclaredMethod("fillTranslators", Exception[].class, List.class)
                    .invoke(null, exception, translators);
        } catch (IllegalAccessException | ClassNotFoundException | NoSuchMethodException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
    
    private void openPrevious() {
        Minecraft.getInstance().setScreen(parent);
    }
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (entryListWidget.mouseScrolled(mouseX, mouseY, amount))
            return true;
        return super.mouseScrolled(mouseX, mouseY, amount);
    }
    
    @Override
    public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
        this.renderDirtBackground(0);
        this.entryListWidget.render(matrices, mouseX, mouseY, delta);
        drawCenteredString(matrices, this.font, I18n.get("text.rei.credits"), this.width / 2, 16, 16777215);
        super.render(matrices, mouseX, mouseY, delta);
    }
    
}
