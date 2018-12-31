package me.shedaniel;

import me.shedaniel.api.IREIPlugin;
import me.shedaniel.gui.REIRenderHelper;
import me.shedaniel.impl.REIRecipeManager;
import me.shedaniel.library.KeyBindManager;
import me.shedaniel.listenerdefinitions.DoneLoading;
import me.shedaniel.listenerdefinitions.RecipeLoadListener;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.registry.IRegistry;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClientListener implements DoneLoading, RecipeLoadListener {
    public static KeyBinding recipeKeybind;
    public static KeyBinding hideKeybind;
    public static KeyBinding useKeybind;
    
    private List<IREIPlugin> plugins;
    public static List<ItemStack> stackList;
    
    @Override
    public void onDoneLoading() {
        plugins = new ArrayList<>();
        stackList = new ArrayList<>();
        
        recipeKeybind = KeyBindManager.createKeybinding("key.rei.recipe", KeyEvent.VK_R, "key.rei.category", REIRenderHelper::recipeKeybind);
        hideKeybind = KeyBindManager.createKeybinding("key.rei.hide", KeyEvent.VK_O, "key.rei.category", REIRenderHelper::hideKeybind);
        useKeybind = KeyBindManager.createKeybinding("key.rei.use", KeyEvent.VK_U, "key.rei.category", REIRenderHelper::useKeybind);
        
        buildItemList();
    }
    
    private void buildItemList() {
        if (!IRegistry.ITEM.isEmpty())
            IRegistry.ITEM.forEach(this::processItem);
        if (!IRegistry.ENCHANTMENT.isEmpty())
            IRegistry.ENCHANTMENT.forEach(enchantment -> {
                for(int i = enchantment.getMinLevel(); i < enchantment.getMaxLevel(); i++) {
                    ItemStack stack = new ItemStack(Items.ENCHANTED_BOOK);
                    Map<Enchantment, Integer> map = new HashMap<>();
                    map.put(enchantment, i);
                    EnchantmentHelper.setEnchantments(map, stack);
                    processItemStack(stack);
                }
            });
    }
    
    private void processItem(Item item) {
        NonNullList<ItemStack> items = NonNullList.create();
        try {
            item.fillItemGroup(item.getGroup(), items);
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
    public void recipesLoaded(net.minecraft.item.crafting.RecipeManager recipeManager) {
        REIRecipeManager.instance().RecipesLoaded(recipeManager);
    }
}
