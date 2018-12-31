package me.shedaniel.library;

import net.minecraft.client.settings.KeyBinding;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Created by James on 8/7/2018.
 */
public class KeyBindManager {
    
    private static Map<KeyBinding, Sink> bindingFunctions = new HashMap<>();
    
    public static KeyBinding createKeybinding(String bindingName, int key, String categoryName, Sink function) {
        KeyBinding newBinding = new KeyBinding(bindingName, key, categoryName);
        bindingFunctions.put(newBinding, function);
        return newBinding;
    }
    
    public static boolean processGuiKeybinds(int typedChar) {
        Optional<KeyBinding> binding = bindingFunctions.keySet().stream().filter(f -> f.getDefaultKeyCode().getKeyCode() == typedChar).findFirst();
        if (binding.isPresent()) {
            bindingFunctions.get(binding.get()).Sink();
            return true;
        }
        return false;
    }
}
