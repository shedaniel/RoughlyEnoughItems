package me.shedaniel.rei.impl.client.gui.changelog;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.architectury.platform.Platform;
import me.shedaniel.rei.impl.client.gui.error.ErrorsEntryListWidget;
import me.shedaniel.rei.impl.client.gui.error.ErrorsScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
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
    
    private static Boolean visited = false;
    
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
            builder.add(new TranslatableComponent("rei.changelog.error.missingChangelogFile"));
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
                builder.add(new TranslatableComponent("rei.changelog.error.missingChangelogFile"));
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
                    builder.add(new TranslatableComponent("rei.changelog.error.failedToReadChangelogFile"));
                }
            }
        }
        
        Minecraft.getInstance().setScreen(new ErrorsScreen(new TranslatableComponent("text.rei.changelog.title"), builder.components, Minecraft.getInstance().screen, true));
    }
}
