package me.shedaniel.rei.mixin;

import me.shedaniel.rei.listeners.IMixinKeyBinding;
import net.minecraft.client.settings.KeyBinding;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;

@Mixin(KeyBinding.class)
public class MixinKeyBinding implements IMixinKeyBinding {
    
    @Shadow
    @Final
    private static Map<String, Integer> CATEGORY_ORDER;
    
    @Override
    public boolean addCategory(String keyBindingCategory, int id) {
        if (!CATEGORY_ORDER.containsKey(keyBindingCategory)) {
            CATEGORY_ORDER.put(keyBindingCategory, id);
            return true;
        }
        return false;
    }
    
    @Override
    public boolean addCategory(String keyBindingCategory) {
        return addCategory(keyBindingCategory, CATEGORY_ORDER.size() + 1);
    }
    
    @Override
    public boolean hasCategory(String keyCategory) {
        return CATEGORY_ORDER.containsKey(keyCategory);
    }
    
}
