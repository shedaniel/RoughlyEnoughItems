package me.shedaniel;

import me.shedaniel.config.REIConfig;
import me.shedaniel.listenerdefinitions.IEvent;
import me.shedaniel.listenerdefinitions.PacketAdder;
import me.shedaniel.listeners.DrawContainerListener;
import me.shedaniel.listeners.ResizeListener;
import me.shedaniel.network.CheatPacket;
import me.shedaniel.network.DeletePacket;
import me.shedaniel.plugin.VanillaPlugin;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.FabricLoader;
import net.minecraft.network.NetworkSide;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by James on 7/27/2018.
 */
public class Core implements PacketAdder, ClientModInitializer {
    @Override
    public void registerHandshakingPackets(PacketRegistrationReceiver receiver) {
    }
    
    @Override
    public void registerPlayPackets(PacketRegistrationReceiver receiver) {
        receiver.registerPacket(NetworkSide.SERVER, CheatPacket.class);
        receiver.registerPacket(NetworkSide.SERVER, DeletePacket.class);
    }
    
    @Override
    public void registerStatusPackets(PacketRegistrationReceiver receiver) {
    
    }
    
    @Override
    public void registerLoginPackets(PacketRegistrationReceiver receiver) {
    
    }
    
    private static List<IEvent> events = new LinkedList<>();
    public static final File configFile = new File(FabricLoader.INSTANCE.getConfigDirectory(), "rei.json");
    public static REIConfig config;
    public static ClientListener clientListener;
    
    @Override
    public void onInitializeClient() {
        this.clientListener = new ClientListener();
        registerEvents();
        try {
            loadConfig();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.clientListener.onInitializeKeyBind();
    }
    
    private void registerEvents() {
        registerEvent(new DrawContainerListener());
        registerEvent(new ResizeListener());
        registerEvent(new VanillaPlugin());
        registerEvent(clientListener);
    }
    
    public static void registerEvent(IEvent event) {
        events.add(event);
    }
    
    public static <T> List<T> getListeners(Class<T> listenerInterface) {
        List<T> list = new ArrayList<>();
        events.forEach(iEvent -> {
            if (listenerInterface.isAssignableFrom(iEvent.getClass()))
                list.add(listenerInterface.cast(iEvent));
        });
        return list;
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
