package me.shedaniel.rei.client;

import com.google.common.collect.Lists;
import me.shedaniel.rei.RoughlyEnoughItemsCore;
import me.shedaniel.rei.api.Identifier;
import me.shedaniel.rei.listeners.KeyBindingHooks;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import org.dimdev.rift.listener.client.KeyBindingAdder;

import java.util.Collection;
import java.util.List;

public class KeyBindingRegistry implements KeyBindingAdder {
    
    private static final Identifier RECIPE_KEYBIND = new Identifier("roughlyenoughitems", "recipe_keybind");
    private static final Identifier USAGE_KEYBIND = new Identifier("roughlyenoughitems", "usage_keybind");
    private static final Identifier HIDE_KEYBIND = new Identifier("roughlyenoughitems", "hide_keybind");
    
    @Override
    public Collection<? extends KeyBinding> getKeyBindings() {
        String category = "key.rei.category";
        List<KeyBinding> keyBindings = Lists.newArrayList();
        keyBindings.add(ClientHelper.RECIPE = createKeyBinding(RECIPE_KEYBIND, InputMappings.Type.KEYSYM, 82, category));
        keyBindings.add(ClientHelper.USAGE = createKeyBinding(USAGE_KEYBIND, InputMappings.Type.KEYSYM, 85, category));
        keyBindings.add(ClientHelper.HIDE = createKeyBinding(HIDE_KEYBIND, InputMappings.Type.KEYSYM, 79, category));
        addCategoryIfMissing(ClientHelper.RECIPE, category);
        return keyBindings;
    }
    
    private void addCategoryIfMissing(KeyBinding keyBinding, String category) {
        if (!((KeyBindingHooks) keyBinding).rei_hasCategory(category))
            ((KeyBindingHooks) keyBinding).rei_addCategory(category);
    }
    
    private KeyBinding createKeyBinding(Identifier resourceLocation, InputMappings.Type inputType, int keyCode, String category) {
        RoughlyEnoughItemsCore.LOGGER.info("Registering: key." + resourceLocation.toString().replaceAll(":", ".") + " in " + category);
        return new KeyBinding("key." + resourceLocation.toString().replaceAll(":", "."), inputType, keyCode, category);
    }
    
}
