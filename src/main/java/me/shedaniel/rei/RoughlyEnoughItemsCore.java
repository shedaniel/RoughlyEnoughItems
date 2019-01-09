package me.shedaniel.rei;

import me.shedaniel.rei.listeners.ClientTick;
import me.shedaniel.rei.listeners.IListener;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.events.client.ClientTickEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RoughlyEnoughItemsCore implements ClientModInitializer {
    
    private static List<IListener> listeners = new ArrayList<>();
    
    @Override
    public void onInitializeClient() {
        registerFabricEvents();
        registerREIListeners();
    }
    
    private void registerREIListeners() {
    
    }
    
    private IListener registerListener(IListener listener) {
        listeners.add(listener);
        return listener;
    }
    
    public static <T> List<T> getListeners(Class<T> listenerClass) {
        return listeners.stream().filter(listener -> {
            return listenerClass.isAssignableFrom(listener.getClass());
        }).map(listener -> {
            return listenerClass.cast(listener);
        }).collect(Collectors.toList());
    }
    
    private void registerFabricEvents() {
        ClientTickEvent.CLIENT.register(minecraftClient -> {
            getListeners(ClientTick.class).forEach(clientTick -> clientTick.onTick(minecraftClient));
        });
    }
    
}
