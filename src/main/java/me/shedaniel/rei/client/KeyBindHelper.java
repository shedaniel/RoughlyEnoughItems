package me.shedaniel.rei.client;

import com.google.common.collect.Lists;
import me.shedaniel.rei.RoughlyEnoughItemsCore;
import me.shedaniel.rei.listeners.IMixinKeyBinding;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.util.ResourceLocation;
import org.dimdev.rift.listener.client.KeyBindingAdder;

import java.util.Collection;
import java.util.List;

public class KeyBindHelper implements KeyBindingAdder {
    
    private static final ResourceLocation RECIPE_KEYBIND = new ResourceLocation("roughlyenoughitems", "recipe_keybind");
    private static final ResourceLocation USAGE_KEYBIND = new ResourceLocation("roughlyenoughitems", "usage_keybind");
    private static final ResourceLocation HIDE_KEYBIND = new ResourceLocation("roughlyenoughitems", "hide_keybind");
    public static KeyBinding RECIPE, USAGE, HIDE;
    
    @Override
    public Collection<? extends KeyBinding> getKeyBindings() {
        String category = "key.rei.category";
        List<KeyBinding> keyBindings = Lists.newArrayList();
        keyBindings.add(RECIPE = createKeyBinding(RECIPE_KEYBIND, InputMappings.Type.KEYSYM, 82, category));
        keyBindings.add(USAGE = createKeyBinding(USAGE_KEYBIND, InputMappings.Type.KEYSYM, 85, category));
        keyBindings.add(HIDE = createKeyBinding(HIDE_KEYBIND, InputMappings.Type.KEYSYM, 79, category));
        addCategoryIfMissing(RECIPE, category);
        return keyBindings;
    }
    
    private void addCategoryIfMissing(KeyBinding keyBinding, String category) {
        if (!((IMixinKeyBinding) keyBinding).hasCategory(category))
            ((IMixinKeyBinding) keyBinding).addCategory(category);
    }
    
    private KeyBinding createKeyBinding(ResourceLocation location, InputMappings.Type inputType, int keyCode, String category) {
        RoughlyEnoughItemsCore.LOGGER.info("Registering: key." + location.toString().replaceAll(":", ".") + " in " + category);
        return new KeyBinding("key." + location.toString().replaceAll(":", "."), inputType, keyCode, category);
    }
    
}
