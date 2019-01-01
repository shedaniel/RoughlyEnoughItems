package me.shedaniel;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonWriter;
import me.shedaniel.config.REIConfig;
import me.shedaniel.network.CheatPacket;
import me.shedaniel.network.DeletePacket;
import net.minecraft.network.EnumPacketDirection;
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
    
    @Override
    public void onInitialization() {
        try {
            loadConfig();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static void loadConfig() throws IOException {
        if (!configFile.exists())
            loadDefaultConfig();
        boolean failed = false;
        try {
            InputStream in = Files.newInputStream(configFile.toPath());
            config = REIConfig.GSON.fromJson(new InputStreamReader(in), REIConfig.class);
        } catch (Exception e){
            failed = true;
        }
        if (failed || config == null) {
            System.out.println("[REI] Failed to load config! Overwriting with default config.");
            config = new REIConfig();
        }
        saveConfig();
    }
    
    public static void loadDefaultConfig() throws IOException {
        config = new REIConfig();
        saveConfig();
    }
    
    public static void saveConfig() throws IOException {
        configFile.getParentFile().mkdirs();
        if (configFile.exists())
            configFile.delete();
        try (PrintWriter writer = new PrintWriter(configFile)) {
            REIConfig.GSON.toJson(config, writer);
            writer.close();
        }
    }
    
}
