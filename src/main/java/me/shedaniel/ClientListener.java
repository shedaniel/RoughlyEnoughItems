package me.shedaniel;

import me.shedaniel.api.IREIPlugin;
import me.shedaniel.gui.REIRenderHelper;
import me.shedaniel.impl.REIRecipeManager;
import me.shedaniel.library.KeyBindFunction;
import me.shedaniel.listenerdefinitions.DoneLoading;
import me.shedaniel.listenerdefinitions.RecipeLoadListener;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.util.DefaultedList;
import net.minecraft.util.registry.Registry;

import java.util.*;

public class ClientListener implements DoneLoading, RecipeLoadListener {
    
    public static KeyBindFunction recipeKeyBind;
    public static KeyBindFunction hideKeyBind;
    public static KeyBindFunction usageKeyBind;
    public static List<KeyBindFunction> keyBinds = new ArrayList<>();
    
    private List<IREIPlugin> plugins;
    public static List<ItemStack> stackList;
    
    public static boolean processGuiKeyBinds(int typedChar) {
        for(KeyBindFunction keyBind : keyBinds)
            if (keyBind.apply(typedChar))
                return true;
        return false;
    }
    
    @Override
    public void onDoneLoading() {
        plugins = new ArrayList<>();
        stackList = new ArrayList<>();
        
        buildItemList();
    }
    
    public void onInitializeKeyBind() {
        recipeKeyBind = new KeyBindFunction(Core.config.recipeKeyBind) {
            @Override
            public boolean apply(int key) {
                if (key == this.getKey())
                    REIRenderHelper.recipeKeybind();
                return key == this.getKey();
            }
        };
        hideKeyBind = new KeyBindFunction(Core.config.hideKeyBind) {
            @Override
            public boolean apply(int key) {
                if (key == this.getKey())
                    REIRenderHelper.hideKeybind();
                return key == this.getKey();
            }
        };
        usageKeyBind = new KeyBindFunction(Core.config.usageKeyBind) {
            @Override
            public boolean apply(int key) {
                if (key == this.getKey())
                    REIRenderHelper.useKeybind();
                return key == this.getKey();
            }
        };
        keyBinds.addAll(Arrays.asList(recipeKeyBind, hideKeyBind, usageKeyBind));
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
