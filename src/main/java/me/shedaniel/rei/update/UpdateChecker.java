package me.shedaniel.rei.update;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import me.shedaniel.rei.RoughlyEnoughItemsCore;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.FabricLoader;
import net.fabricmc.loader.ModContainer;
import net.minecraft.SharedConstants;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class UpdateChecker implements ClientModInitializer {
    
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static Version currentVersion = null, latestForGame = null;
    private static JsonVersionElement element;
    private String fakeDownloaded = "{\"latest\":[{\"game\":\"19w04a\",\"mod\":\"2.2.0.48\"},{\"game\":\"19w04b\",\"mod\":\"2.2.0.47\"},{\"game\":\"19w05a\",\"mod\":\"2.2.0.48\"}],\"changelogs\":{\"fabric\":{\"2.2.0.28\":{\"text\":\"Added a currentVersion checker.\",\"level\":\"light\"}}}}";
    
    public static boolean isOutdated() {
        return latestForGame.compareTo(currentVersion) == 1;
    }
    
    public static UpdatePriority getUpdatePriority(List<Version> versions) {
        return Collections.max(versions.stream().map(UpdateChecker::getUpdatePriority).collect(Collectors.toList()));
    }
    
    public static UpdatePriority getUpdatePriority(Version version) {
        JsonObject object = element.getChangelogs().getFabric();
        if (object.has(version.getString()))
            return UpdatePriority.fromString(object.get(version.getString()).getAsString());
        return UpdatePriority.NONE;
    }
    
    public static boolean checkUpdates() {
        return RoughlyEnoughItemsCore.getConfigHelper().checkUpdates();
    }
    
    public static String getChangelog(Version version) {
        return "wip";
    }
    
    public static Version getCurrentVersion() {
        return currentVersion;
    }
    
    public static Version getLatestForGame() {
        return latestForGame;
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
        element = GSON.fromJson(fakeDownloaded, JsonVersionElement.class);
        latestForGame = new Version(parseLatest(element, SharedConstants.getGameVersion().getName()));
        RoughlyEnoughItemsCore.LOGGER.info("REI: On %s, Current = %s, latest = %s, outdated = %b", SharedConstants.getGameVersion().getName(), (currentVersion == null ? "null" : currentVersion.getString()), latestForGame.getString(), isOutdated());
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
        private List<LatestVersionObject> latestVersions = Lists.newArrayList();
        private ChangelogObject changelogs = new ChangelogObject();
        
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
        private JsonObject fabric = new JsonObject();
        private JsonObject rift = new JsonObject();
        
        public JsonObject getFabric() {
            return fabric;
        }
        
        public JsonObject getRift() {
            return rift;
        }
    }
    
}
