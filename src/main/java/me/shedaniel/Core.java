package me.shedaniel;

import me.shedaniel.network.CheatPacket;
import me.shedaniel.network.DeletePacket;
import net.minecraft.network.EnumPacketDirection;
import org.dimdev.rift.listener.PacketAdder;

/**
 * Created by James on 7/27/2018.
 */
public class Core implements PacketAdder {
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
    
    
}
