package me.shedaniel;

import me.shedaniel.api.IREIPlugin;
import me.shedaniel.gui.REIRenderHelper;
import me.shedaniel.impl.REIRecipeManager;
import me.shedaniel.library.KeyBindManager;
import me.shedaniel.listenerdefinitions.DoneLoading;
import me.shedaniel.listenerdefinitions.RecipeLoadListener;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.util.DefaultedList;
import net.minecraft.util.registry.Registry;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClientListener implements DoneLoading, ClientModInitializer, RecipeLoadListener {
    public static KeyBinding recipeKeybind;
    public static KeyBinding hideKeybind;
    public static KeyBinding useKeybind;
    
    private List<IREIPlugin> plugins;
    public static List<ItemStack> stackList;
    
    @Override
    public void onDoneLoading() {
        plugins = new ArrayList<>();
        stackList = new ArrayList<>();
        
        buildItemList();
    }
    
    @Override
    public void onInitializeClient() {
        recipeKeybind = KeyBindManager.createKeybinding("key.rei.recipe", KeyEvent.VK_R, "key.rei.category", REIRenderHelper::recipeKeybind);
        hideKeybind = KeyBindManager.createKeybinding("key.rei.hide", KeyEvent.VK_O, "key.rei.category", REIRenderHelper::hideKeybind);
        useKeybind = KeyBindManager.createKeybinding("key.rei.use", KeyEvent.VK_U, "key.rei.category", REIRenderHelper::useKeybind);
    }
    
    private void buildItemList() {
        if (!Registry.ITEM.isEmpty())
            Registry.ITEM.forEach(this::processItem);
        if (Registry.ENCHANTMENT.stream().count() > 0)
            Registry.ENCHANTMENT.forEach(enchantment -> {
                for(int i = enchantment.getMinimumLevel(); i < enchantment.getMaximumLevel(); i++) {
                    ItemStack stack = new ItemStack(Items.ENCHANTED_BOOK);
                    Map<Enchantment, Integer> map = new HashMap<>();
                    map.put(enchantment, i);
                    EnchantmentHelper.set(map, stack);
                    processItemStack(stack);
                }
            });
    }
    
    private void processItem(Item item) {
        DefaultedList<ItemStack> items = DefaultedList.create();
        try {
            item.addStacksForDisplay(item.getItemGroup(), items);
            items.forEach(stackList::add);
        } catch (NullPointerException e) {
//            if (item == Items.ENCHANTED_BOOK) {
//                item.fillItemGroup(ItemGroup.TOOLS, items);
//                items.forEach(stackList::add);
//            }
        }
    }
    
    private void processItemStack(ItemStack item) {
        stackList.add(item);
    }
    
    @Override
    public void recipesLoaded(RecipeManager recipeManager) {
        REIRecipeManager.instance().RecipesLoaded(recipeManager);
    }
    
}
