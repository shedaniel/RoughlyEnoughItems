package me.shedaniel;

import me.shedaniel.config.REIConfig;
import me.shedaniel.listenerdefinitions.ClientTickable;
import me.shedaniel.listenerdefinitions.IEvent;
import me.shedaniel.listeners.DrawContainerListener;
import me.shedaniel.network.CheatPacket;
import me.shedaniel.plugin.VanillaPlugin;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.events.client.ClientTickEvent;
import net.fabricmc.loader.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by James on 7/27/2018.
 */
public class Core implements ClientModInitializer {
    
    private static List<IEvent> events = new LinkedList<>();
    public static final File configFile = new File(FabricLoader.INSTANCE.getConfigDirectory(), "rei.json");
    public static REIConfig config;
    public static ClientListener clientListener;
    public static boolean centreSearchBox;
    
    @Override
    public void onInitializeClient() {
        this.clientListener = new ClientListener();
        registerSelfEvents();
        registerFabricEvents();
        try {
            loadConfig();
            centreSearchBox = config.centreSearchBox;
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.clientListener.onInitializeKeyBind();
    }
    
    private void registerFabricEvents() {
        ClientTickEvent.CLIENT.register(minecraftClient -> {
            getListeners(ClientTickable.class).forEach(ClientTickable::clientTick);
        });
    }
    
    private void registerSelfEvents() {
        registerEvent(new DrawContainerListener());
        registerEvent(clientListener);
        registerPlugin(new VanillaPlugin());
    }
    
    public static void registerPlugin(VanillaPlugin vanillaPlugin) {
        registerEvent(vanillaPlugin);
    }
    
    private static void registerEvent(IEvent event) {
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
        } catch (Exception e) {
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
    
    public static void cheatItems(ItemStack cheatedStack) {
        MinecraftClient.getInstance().getNetworkHandler().sendPacket(new CheatPacket(cheatedStack));
    }
    
}
