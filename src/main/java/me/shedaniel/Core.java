package me.shedaniel;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonWriter;
import me.shedaniel.config.REIConfig;
import me.shedaniel.config.REIRuntimeConfig;
import me.shedaniel.network.CheatPacket;
import me.shedaniel.network.DeletePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.network.EnumPacketDirection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dimdev.rift.listener.PacketAdder;
import org.dimdev.riftloader.RiftLoader;
import org.dimdev.riftloader.listener.InitializationListener;

import java.io.*;
import java.nio.file.Files;

/**
 * Created by James on 7/27/2018.
 */
public class Core implements PacketAdder, InitializationListener {
    @Override
    public void registerHandshakingPackets(PacketRegistrationReceiver receiver) {
    }
    
    @Override
    public void registerPlayPackets(PacketRegistrationReceiver receiver) {
        receiver.registerPacket(EnumPacketDirection.SERVERBOUND, CheatPacket.class);
        receiver.registerPacket(EnumPacketDirection.SERVERBOUND, DeletePacket.class);
    }
    
    @Override
    public void registerStatusPackets(PacketRegistrationReceiver receiver) {
    
    }
    
    @Override
    public void registerLoginPackets(PacketRegistrationReceiver receiver) {
    
    }
    
    public static final File configFile = new File(RiftLoader.instance.configDir, "rei.json");
    public static REIConfig config;
    public static REIRuntimeConfig runtimeConfig;
    public static Logger LOGGER = LogManager.getFormatterLogger("REI");
    
    @Override
    public void onInitialization() {
        try {
            loadConfig();
            runtimeConfig = new REIRuntimeConfig();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static void loadConfig() throws IOException {
        if (!configFile.exists() || !configFile.canRead()) {
            config = new REIConfig();
            saveConfig();
            return;
        }
        boolean failed = false;
        try {
            config = REIConfig.GSON.fromJson(new InputStreamReader(Files.newInputStream(configFile.toPath())), REIConfig.class);
        } catch (Exception e) {
            failed = true;
        }
        if (failed || config == null) {
            Core.LOGGER.error("REI: Failed to load config! Overwriting with default config.");
            config = new REIConfig();
        }
        saveConfig();
    }
    
    public static void saveConfig() throws IOException {
        configFile.getParentFile().mkdirs();
        if (!configFile.exists() && !configFile.createNewFile()) {
            Core.LOGGER.error("REI: Failed to save config! Overwriting with default config.");
            config = new REIConfig();
            return;
        }
        FileWriter writer = new FileWriter(configFile, false);
        try {
            REIConfig.GSON.toJson(config, writer);
        } finally {
            writer.close();
        }
    }
    
    public static void cheatItems(ItemStack cheatedStack) {
        Minecraft.getInstance().getConnection().sendPacket(new CheatPacket(cheatedStack));
    }
    
}
