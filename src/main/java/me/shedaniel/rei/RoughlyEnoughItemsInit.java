package me.shedaniel.rei;

import me.shedaniel.rei.client.ClientHelper;
import me.shedaniel.rei.client.RecipeHelper;
import me.shedaniel.rei.network.CreateItemsMessage;
import me.shedaniel.rei.network.DeleteItemsMessage;
import me.shedaniel.rei.plugin.DefaultPlugin;
import net.minecraft.util.registry.IRegistry;
import org.dimdev.rift.listener.MessageAdder;
import org.dimdev.rift.network.Message;
import org.dimdev.riftloader.listener.InitializationListener;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.Mixins;

import static me.shedaniel.rei.RoughlyEnoughItemsCore.CREATE_ITEMS_PACKET;
import static me.shedaniel.rei.RoughlyEnoughItemsCore.DELETE_ITEMS_PACKET;

public class RoughlyEnoughItemsInit implements InitializationListener, MessageAdder {
    
    @Override
    public void onInitialization() {
        MixinBootstrap.init();
        Mixins.addConfiguration("roughlyenoughitems.client.json");
        registerREIListeners();
        registerDefaultPlugin();
    }
    
    private void registerREIListeners() {
        RoughlyEnoughItemsCore.registerListener(new ClientHelper());
        RoughlyEnoughItemsCore.registerListener(new RecipeHelper());
    }
    
    private void registerDefaultPlugin() {
        RoughlyEnoughItemsCore.registerPlugin(RoughlyEnoughItemsCore.DEFAULT_PLUGIN, new DefaultPlugin());
    }
    
    @Override
    public void registerMessages(IRegistry<Class<? extends Message>> registry) {
        registry.put(DELETE_ITEMS_PACKET, DeleteItemsMessage.class);
        registry.put(CREATE_ITEMS_PACKET, CreateItemsMessage.class);
    }
    
}
