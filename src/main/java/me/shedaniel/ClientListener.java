package me.shedaniel;

import me.shedaniel.api.IAEIPlugin;
import me.shedaniel.gui.AEIRenderHelper;
import me.shedaniel.impl.AEIRecipeManager;
import me.shedaniel.library.KeyBindManager;
import me.shedaniel.listenerdefinitions.DoneLoading;
import me.shedaniel.listenerdefinitions.RecipeLoadListener;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.registry.IRegistry;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

public class ClientListener implements DoneLoading, RecipeLoadListener {
    public static KeyBinding recipeKeybind;
    public static KeyBinding hideKeybind;
    public static KeyBinding useKeybind;
    
    private List<IAEIPlugin> plugins;
    public static List<ItemStack> stackList;
    
    @Override
    public void onDoneLoading() {
        
        plugins = new ArrayList<>();
        stackList = new ArrayList<>();
        
        recipeKeybind = KeyBindManager.createKeybinding("key.aei.recipe", KeyEvent.VK_R, "key.aei.category", AEIRenderHelper::recipeKeybind);
        hideKeybind = KeyBindManager.createKeybinding("key.aei.hide", KeyEvent.VK_O, "key.aei.category", AEIRenderHelper::hideKeybind);
        useKeybind = KeyBindManager.createKeybinding("key.aei.use", KeyEvent.VK_U, "key.aei.category", AEIRenderHelper::useKeybind);
        
        buildItemList();
    }
    
    private void buildItemList() {
        if (!IRegistry.ITEM.isEmpty()) {
            IRegistry.ITEM.forEach(item -> processItem((Item) item));
        }
        
    }
    
    private void processItem(Item item) {
        NonNullList<ItemStack> items = NonNullList.create();
        try {
            item.fillItemGroup(item.getGroup(), items);
            items.forEach(stackList::add);
        } catch (NullPointerException e) {
            if (item == Items.ENCHANTED_BOOK) {
                item.fillItemGroup(ItemGroup.TOOLS, items);
                items.forEach(stackList::add);
            }
        }
    }
    
    @Override
    public void recipesLoaded(net.minecraft.item.crafting.RecipeManager recipeManager) {
        AEIRecipeManager.instance().RecipesLoaded(recipeManager);
    }
}
