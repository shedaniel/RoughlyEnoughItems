package me.shedaniel.rei;

import me.shedaniel.rei.client.ClientHelper;
import me.shedaniel.rei.client.RecipeHelper;
import me.shedaniel.rei.network.CreateItemsPacket;
import me.shedaniel.rei.network.DeleteItemsPacket;
import me.shedaniel.rei.plugin.DefaultPlugin;
import net.minecraft.util.registry.IRegistry;
import org.dimdev.rift.listener.MessageAdder;
import org.dimdev.rift.network.Message;
import org.dimdev.riftloader.listener.InitializationListener;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.Mixins;

import static me.shedaniel.rei.RoughlyEnoughItemsCore.CREATE_ITEMS_PACKET;
import static me.shedaniel.rei.RoughlyEnoughItemsCore.DELETE_ITEMS_PACKET;

public class RoughlyEnoughItemsInit implements InitializationListener {
    
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
    
}
