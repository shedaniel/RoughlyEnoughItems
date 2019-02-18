package me.shedaniel.rei.update;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.List;

import static me.shedaniel.rei.update.UpdateChecker.*;

public class UpdateAnnouncer {
    
    private static World lastWorld = null;
    
    public static void clientTick(TickEvent.ClientTickEvent event) {
        Minecraft client = Minecraft.getInstance();
        if (client.world != lastWorld) {
            lastWorld = client.world;
            if (lastWorld != null) {
                if (checkUpdates() && isOutdated()) {
                    String currentVersionString = getCurrentVersion() == null ? "null" : getCurrentVersion().toString();
                    List<Version> versions = getVersionsHigherThan(getCurrentVersion());
                    String t[] = I18n.format("text.rei.update_outdated", currentVersionString, getLatestForGame(), getUpdatePriority(versions).name().toUpperCase()).split("\n");
                    for(String s : t)
                        client.player.sendStatusMessage(new TextComponentString(s), false);
                    getChangelog(getCurrentVersion()).forEach(s -> client.player.sendStatusMessage(new TextComponentString(s), false));
                }
            }
        }
    }
    
    public static List<String> getChangelog(Version currentVersion) {
        List<String> changelogs = Lists.newLinkedList();
        JsonArray array = getElement().getChangelogs().getForge();
        array.forEach(jsonElement -> {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            Version jsonVersion = new Version(jsonObject.get("version").getAsString());
            if (jsonVersion.compareTo(currentVersion) > 0 && jsonVersion.compareTo(getLatestForGame()) <= 0)
                changelogs.add(I18n.format("text.rei.update_changelog_line", jsonObject.get("text").getAsString()));
        });
        return changelogs;
    }
    
}
