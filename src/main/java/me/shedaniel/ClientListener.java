package me.shedaniel;

import me.shedaniel.api.IREIPlugin;
import me.shedaniel.gui.REIRenderHelper;
import me.shedaniel.impl.REIRecipeManager;
import me.shedaniel.library.KeyBindFunction;
import me.shedaniel.listenerdefinitions.DoneLoading;
import me.shedaniel.listenerdefinitions.RecipeLoadListener;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.registry.IRegistry;

import java.util.*;

public class ClientListener implements DoneLoading, RecipeLoadListener {
    
    public static KeyBindFunction recipeKeybind;
    public static KeyBindFunction hideKeybind;
    public static KeyBindFunction useKeybind;
    public static List<KeyBindFunction> keyBinds = new ArrayList<>();
    
    private List<IREIPlugin> plugins;
    public static List<ItemStack> stackList;
    
    public static boolean processGuiKeybinds(int typedChar) {
        for(KeyBindFunction keyBind : keyBinds)
            if (keyBind.apply(typedChar))
                return true;
        return false;
    }
    
    @Override
    public void onDoneLoading() {
        plugins = new ArrayList<>();
        stackList = new ArrayList<>();
        
        recipeKeybind = new KeyBindFunction(Core.config.recipeKeyBind) {
            @Override
            public boolean apply(int key) {
                if (key == this.getKey())
                    REIRenderHelper.recipeKeybind();
                return key == this.getKey();
            }
        };
        hideKeybind = new KeyBindFunction(Core.config.hideKeyBind) {
            @Override
            public boolean apply(int key) {
                if (key == this.getKey())
                    REIRenderHelper.hideKeybind();
                return key == this.getKey();
            }
        };
        useKeybind = new KeyBindFunction(Core.config.usageKeyBind) {
            @Override
            public boolean apply(int key) {
                if (key == this.getKey())
                    REIRenderHelper.useKeybind();
                return key == this.getKey();
            }
        };
        keyBinds.addAll(Arrays.asList(recipeKeybind, hideKeybind, useKeybind));
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
