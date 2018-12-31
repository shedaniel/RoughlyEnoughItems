package me.shedaniel;

import me.shedaniel.library.KeyBindManager;
import me.shedaniel.listenerdefinitions.IEvent;
import me.shedaniel.listenerdefinitions.KeybindHandler;
import me.shedaniel.listenerdefinitions.PacketAdder;
import me.shedaniel.listeners.DrawContainerListener;
import me.shedaniel.listeners.ResizeListener;
import me.shedaniel.network.CheatPacket;
import me.shedaniel.network.DeletePacket;
import me.shedaniel.plugin.VanillaPlugin;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.events.client.SpriteEvent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.network.NetworkSide;
import org.apache.commons.lang3.ArrayUtils;

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
    
    @Override
    public void onInitializeClient() {
        registerEvents();
    }
    
    private void registerEvents() {
        registerEvent(new DrawContainerListener());
        registerEvent(new ResizeListener());
        registerEvent(new VanillaPlugin());
        registerEvent(new ClientListener());
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
}
