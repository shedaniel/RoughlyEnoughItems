package me.shedaniel.rei.client;

import com.google.common.collect.Lists;
import me.shedaniel.rei.RoughlyEnoughItemsCore;
import me.shedaniel.rei.api.Identifier;
import me.shedaniel.rei.api.KeyBindingHooks;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import org.dimdev.rift.listener.client.KeyBindingAdder;

import java.util.Collection;
import java.util.List;

public class KeybindRegistry implements KeyBindingAdder {
    
    private final Identifier recipeKeybind = new Identifier("roughlyenoughitems", "recipe_keybind");
    private final Identifier usageKeybind = new Identifier("roughlyenoughitems", "usage_keybind");
    private final Identifier hideKeybind = new Identifier("roughlyenoughitems", "hide_keybind");
    private final Identifier previousPageKeybind = new Identifier("roughlyenoughitems", "previous_page");
    private final Identifier nextPageKeybind = new Identifier("roughlyenoughitems", "next_page");
    private final Identifier focusSearchFieldKeybind = new Identifier("roughlyenoughitems", "focus_search");
    public static KeyBinding recipe, usage, hide, previousPage, nextPage, focusSearchField;
    
    @Override
    public Collection<? extends KeyBinding> getKeyBindings() {
        String category = "key.rei.category";
        List<KeyBinding> keyBindings = Lists.newArrayList();
        keyBindings.add(recipe = createKeyBinding(recipeKeybind, InputMappings.Type.KEYSYM, 82, category));
        keyBindings.add(usage = createKeyBinding(usageKeybind, InputMappings.Type.KEYSYM, 85, category));
        keyBindings.add(hide = createKeyBinding(hideKeybind, InputMappings.Type.KEYSYM, 79, category));
        keyBindings.add(previousPage = createKeyBinding(previousPageKeybind, InputMappings.Type.KEYSYM, -1, category));
        keyBindings.add(nextPage = createKeyBinding(nextPageKeybind, InputMappings.Type.KEYSYM, -1, category));
        keyBindings.add(focusSearchField = createKeyBinding(focusSearchFieldKeybind, InputMappings.Type.KEYSYM, -1, category));
        if (!((KeyBindingHooks) recipe).rei_hasCategory(category))
            ((KeyBindingHooks) recipe).rei_addCategory(category);
        return keyBindings;
    }
    
    private KeyBinding createKeyBinding(Identifier resourceLocation, InputMappings.Type inputType, int keyCode, String category) {
        RoughlyEnoughItemsCore.LOGGER.info("Registering: key." + resourceLocation.toString().replaceAll(":", ".") + " in " + category);
        return new KeyBinding("key." + resourceLocation.toString().replaceAll(":", "."), inputType, keyCode, category);
    }
}
