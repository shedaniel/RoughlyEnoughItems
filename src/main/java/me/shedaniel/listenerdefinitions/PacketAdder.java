package me.shedaniel.listenerdefinitions;

import net.minecraft.network.NetworkSide;
import net.minecraft.network.NetworkState;
import net.minecraft.network.Packet;

public interface PacketAdder extends IEvent {
    
    interface PacketRegistrationReceiver {
        NetworkState registerPacket(NetworkSide direction, Class<? extends Packet<?>> packetClass);
    }
    
    void registerHandshakingPackets(PacketRegistrationReceiver receiver);
    
    void registerPlayPackets(PacketRegistrationReceiver receiver);
    
    void registerStatusPackets(PacketRegistrationReceiver receiver);
    
    void registerLoginPackets(PacketRegistrationReceiver receiver);
    
}
