package me.shedaniel.rei.update;

import com.google.common.collect.Lists;
import com.google.gson.*;
import com.google.gson.annotations.SerializedName;
import me.shedaniel.rei.RoughlyEnoughItemsCore;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.FabricLoader;
import net.fabricmc.loader.ModContainer;
import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.StringTextComponent;
import net.minecraft.world.World;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringBufferInputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class UpdateChecker implements ClientModInitializer {
    
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static Version currentVersion = null, latestForGame = null;
    private static JsonVersionElement element;
    private static World lastWorld = null;
    private static String VERSION_STRING = "https://raw.githubusercontent.com/shedaniel/RoughlyEnoughItems/1.14/version.json";
    
    public static boolean isOutdated() {
        return latestForGame.compareTo(currentVersion) == 1 && currentVersion != null;
    }
    
    public static UpdatePriority getUpdatePriority(List<Version> versions) {
        UpdatePriority p = UpdatePriority.NONE;
        List<UpdatePriority> priorities = Arrays.asList(UpdatePriority.values());
        for(UpdatePriority priority : versions.stream().map(UpdateChecker::getUpdatePriority).collect(Collectors.toList()))
            if (priority.compareTo(p) > 0)
                p = priority;
        return p;
    }
    
    public static UpdatePriority getUpdatePriority(Version version) {
        JsonArray array = element.getChangelogs().getFabric();
        for(JsonElement element : array) {
            JsonObject jsonObject = element.getAsJsonObject();
            if (jsonObject.has("version") && jsonObject.get("version").getAsString().equals(version.toString()))
                return UpdatePriority.fromString(jsonObject.get("level").getAsString());
        }
        return UpdatePriority.NONE;
    }
    
    public static boolean checkUpdates() {
        return RoughlyEnoughItemsCore.getConfigHelper().checkUpdates();
    }
    
    public static List<String> getChangelog(Version currentVersion) {
        List<String> changelogs = Lists.newLinkedList();
        JsonArray array = element.getChangelogs().getFabric();
        array.forEach(jsonElement -> {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            Version jsonVersion = new Version(jsonObject.get("version").getAsString());
            if (jsonVersion.compareTo(currentVersion) > 0 && jsonVersion.compareTo(latestForGame) <= 0)
                changelogs.add(I18n.translate("text.rei.update_changelog_line", jsonObject.get("text").getAsString()));
        });
        return changelogs;
    }
    
    public static Version getCurrentVersion() {
        return currentVersion;
    }
    
    public static Version getLatestForGame() {
        return latestForGame;
    }
    
    public static void onTick(MinecraftClient client) {
        if (client.world != lastWorld) {
            lastWorld = client.world;
            if (lastWorld != null) {
                if (checkUpdates() && isOutdated()) {
                    String currentVersionString = getCurrentVersion() == null ? "null" : getCurrentVersion().toString();
                    List<Version> versions = getVersionsHigherThan(currentVersion);
                    String t[] = I18n.translate("text.rei.update_outdated", currentVersionString, getLatestForGame(), getUpdatePriority(versions).name().toUpperCase()).split("\n");
                    for(String s : t)
                        client.player.addChatMessage(new StringTextComponent(s), false);
                    getChangelog(currentVersion).forEach(s -> client.player.addChatMessage(new StringTextComponent(s), false));
                }
            }
        }
    }
    
    private static List<Version> getVersionsHigherThan(Version currentVersion) {
        List<Version> versions = Lists.newLinkedList();
        JsonArray array = element.getChangelogs().getFabric();
        array.forEach(jsonElement -> {
            Version jsonVersion = new Version(jsonElement.getAsJsonObject().get("version").getAsString());
            if (jsonVersion.compareTo(currentVersion) > 0)
                versions.add(jsonVersion);
        });
        return versions;
    }
    
    @Override
    public void onInitializeClient() {
        if (!checkUpdates())
            return;
        FabricLoader.INSTANCE.getMods().stream().map(ModContainer::getInfo).forEach(modInfo -> {
            if (modInfo.getId().equals("roughlyenoughitems"))
                try {
                    currentVersion = new Version(modInfo.getVersionString());
                } catch (Exception e) {
                }
        });
        InputStream downloadedStream = downloadVersionString();
        String downloadedString = null;
        try {
            downloadedString = IOUtils.toString(downloadedStream, StandardCharsets.UTF_8);
            element = GSON.fromJson(downloadedString, JsonVersionElement.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (downloadedString != null && !downloadedString.equalsIgnoreCase("{}"))
            latestForGame = new Version(parseLatest(element, SharedConstants.getGameVersion().getName()));
        else latestForGame = new Version("0.0.0");
    }
    
    private InputStream downloadVersionString() {
        try {
            URL versionUrl = new URL(VERSION_STRING);
            return versionUrl.openStream();
        } catch (IOException e) {
            return new StringBufferInputStream("{}");
        }
    }
    
    private String parseLatest(JsonVersionElement element, String gameVersion) {
        List<LatestVersionObject> objects = new LinkedList<>(element.getLatestVersions());
        for(int i = objects.size() - 1; i >= 0; i--)
            if (objects.get(i).getGameVersion().equals(gameVersion))
                return objects.get(i).getModVersion();
        return objects.get(objects.size() - 1).getModVersion();
    }
    
    private class JsonVersionElement {
        @SerializedName("latest")
        private List<LatestVersionObject> latestVersions;
        private ChangelogObject changelogs;
        
        public JsonVersionElement() {
            this.latestVersions = Lists.newArrayList();
            changelogs = new ChangelogObject();
        }
        
        public List<LatestVersionObject> getLatestVersions() {
            return latestVersions;
        }
        
        public ChangelogObject getChangelogs() {
            return changelogs;
        }
    }
    
    private class LatestVersionObject {
        @SerializedName("game")
        private String gameVersion = "";
        @SerializedName("mod")
        private String modVersion = "";
        
        public String getGameVersion() {
            return gameVersion;
        }
        
        public String getModVersion() {
            return modVersion;
        }
        
        @Override
        public String toString() {
            return String.format("LatestVersion[%s] = %s", getGameVersion(), getModVersion());
        }
    }
    
    private class ChangelogObject {
        private JsonArray fabric = new JsonArray();
        private JsonArray rift = new JsonArray();
        
        public JsonArray getFabric() {
            return fabric;
        }
        
        public JsonArray getRift() {
            return rift;
        }
    }
    
}
