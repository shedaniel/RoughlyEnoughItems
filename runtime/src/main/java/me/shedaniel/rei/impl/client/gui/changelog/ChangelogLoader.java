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

package me.shedaniel.rei.impl.client.gui.changelog;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.architectury.platform.Platform;
import me.shedaniel.rei.impl.client.gui.error.ErrorsEntryListWidget;
import me.shedaniel.rei.impl.client.gui.error.ErrorsScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

public class ChangelogLoader {
    public interface Builder {
        default void add(Component component) {
            add(width -> new ErrorsEntryListWidget.TextEntry(component, width));
        }
        
        void add(Function<Integer, ErrorsEntryListWidget.Entry> function);
    }
    
    private static Boolean visited = null;
    
    public static boolean hasVisited() {
        if (visited == null) {
            visited = false;
            File file = Platform.getConfigFolder().resolve("roughlyenoughitems/changelog.txt").toFile();
            if (file.exists()) {
                try (InputStreamReader reader = new FileReader(file)) {
                    String version = IOUtils.toString(reader).trim();
                    
                    InputStream changesJsonStream = ChangelogLoader.class.getClassLoader().getResourceAsStream("roughlyenoughitems.changes.json");
                    if (changesJsonStream != null) {
                        JsonObject object = JsonParser.parseReader(new InputStreamReader(changesJsonStream))
                                .getAsJsonObject();
                        String currentVersion = object.getAsJsonPrimitive("version").getAsString();
                        if (currentVersion.equals(version)) {
                            visited = true;
                        }
                    }
                } catch (IOException e) {
                }
            }
        }
        
        return visited;
    }
    
    public static void show() {
        class BuilderImpl implements Builder {
            private final List<Object> components = new ArrayList<>();
            
            @Override
            public void add(Function<Integer, ErrorsEntryListWidget.Entry> function) {
                components.add(function);
            }
        }
    
        visited = true;
        BuilderImpl builder = new BuilderImpl();
        
        InputStream changesJsonStream = ChangelogLoader.class.getClassLoader().getResourceAsStream("roughlyenoughitems.changes.json");
        if (changesJsonStream == null) {
            builder.add(Component.translatable("rei.changelog.error.missingChangelogFile"));
        } else {
            JsonObject object = JsonParser.parseReader(new InputStreamReader(changesJsonStream))
                    .getAsJsonObject();
            String version = object.getAsJsonPrimitive("version").getAsString();
            Path file = Platform.getConfigFolder().resolve("roughlyenoughitems/changelog.txt");
            try {
                Files.write(file, version.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            } catch (IOException e) {
                e.printStackTrace();
            }
    
            InputStream changelogStream = ChangelogLoader.class.getClassLoader().getResourceAsStream("roughlyenoughitems/" + version + "/changelog.md");
            
            if (changelogStream == null) {
                builder.add(Component.translatable("rei.changelog.error.missingChangelogFile"));
            } else {
                try {
                    JParseDown parseDown = new JParseDown();
                    LinkedList<JParseDown.Block> blocks = parseDown.linesElements(IOUtils.readLines(changelogStream, StandardCharsets.UTF_8).toArray(new String[0]));
                    for (JParseDown.Block block : blocks) {
                        if (block.autoBreak) {
                            builder.add(width -> new ErrorsEntryListWidget.EmptyEntry(6));
                        }
                        Builder blockBuilder = builder;
                        if (block instanceof JParseDown.BlockHeader) {
                            blockBuilder = function -> {
                                builder.add(width -> new ErrorsEntryListWidget.ScaledEntry(function.apply(Math.round(width / 1.5F)), 1.5F));
                            };
                        }
                        JParseDownToMinecraft.build(blockBuilder, block);
                        if (block.autoBreak) {
                            builder.add(width -> new ErrorsEntryListWidget.EmptyEntry(6));
                        }
                    }
                } catch (IOException e) {
                    builder.add(Component.translatable("rei.changelog.error.failedToReadChangelogFile"));
                }
            }
        }
        
        Minecraft.getInstance().setScreen(new ErrorsScreen(Component.translatable("text.rei.changelog.title"), builder.components, Minecraft.getInstance().screen, true));
    }
}
