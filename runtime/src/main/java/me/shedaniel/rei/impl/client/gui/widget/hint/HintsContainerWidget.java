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

package me.shedaniel.rei.impl.client.gui.widget.hint;

import com.google.gson.Gson;
import dev.architectury.platform.Platform;
import me.shedaniel.math.Point;
import me.shedaniel.rei.api.client.gui.widgets.DelegateWidget;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.FormattedText;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.function.Supplier;

public class HintsContainerWidget extends DelegateWidget {
    private final List<HintWidget> hints = new ArrayList<>();
    private final Widget delegate = Widgets.concat((List<Widget>) (List<? extends Widget>) hints);
    private HintsConfig config;
    
    public HintsContainerWidget() {
        super(Widgets.noOp());
        this.read();
    }
    
    public void init() {
        for (HintWidget hint : this.hints) {
            hint.recalculateBounds();
        }
    }
    
    @Override
    protected Widget delegate() {
        return this.delegate;
    }
    
    public void addHint(int margin, Supplier<Point> point, String uuid, Collection<? extends FormattedText> lines) {
        if (this.config.shownHints.add(uuid)) {
            this.hints.removeIf(hintWidget -> hintWidget.getUuid().equals(uuid));
            this.hints.add(new HintWidget(this, margin, point, uuid, lines));
            this.write();
        }
    }
    
    void removeHint(HintWidget hintWidget) {
        this.hints.remove(hintWidget);
    }
    
    public void read() {
        Path path = Platform.getConfigFolder().resolve("roughlyenoughitems/hints.json");
        this.config = new HintsConfig();
        String uuid = Minecraft.getInstance().getUser().getUuid();
        if (Files.exists(path)) {
            try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                this.config = new Gson().fromJson(reader, HintsConfig.class);
                if (!uuid.equals(this.config.UUID)) {
                    this.config = new HintsConfig();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.config.UUID = uuid;
        write();
    }
    
    public void write() {
        Path path = Platform.getConfigFolder().resolve("roughlyenoughitems/hints.json");
        try {
            Files.createDirectories(path.getParent());
        } catch (IOException e) {
            e.printStackTrace();
        }
        try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            new Gson().toJson(this.config, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private static class HintsConfig {
        private String UUID;
        private Set<String> shownHints = new LinkedHashSet<>();
    }
}
