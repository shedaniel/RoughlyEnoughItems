package me.shedaniel.rei.api;

import me.shedaniel.rei.client.ClientHelperImpl;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.FabricKeyBinding;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.List;

public interface ClientHelper extends ClientModInitializer {
    static ClientHelper getInstance() {
        return ClientHelperImpl.instance;
    }
    
    boolean isCheating();
    
    void setCheating(boolean cheating);
    
    List<ItemStack> getInventoryItemsTypes();
    
    void registerFabricKeyBinds();
    
    boolean tryCheatingStack(ItemStack stack);
    
    boolean executeRecipeKeyBind(ItemStack stack);
    
    boolean executeUsageKeyBind(ItemStack stack);
    
    String getModFromItem(Item item);
    
    void sendDeletePacket();
    
    String getFormattedModFromItem(Item item);
    
    FabricKeyBinding getRecipeKeyBinding();
    
    FabricKeyBinding getUsageKeyBinding();
    
    FabricKeyBinding getHideKeyBinding();
    
    FabricKeyBinding getPreviousPageKeyBinding();
    
    FabricKeyBinding getNextPageKeyBinding();
    
    boolean executeViewAllRecipesKeyBind();
}
