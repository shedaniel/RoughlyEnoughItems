package me.shedaniel.rei.client;

import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class KeyBindHelper {
    
    private static final String RECIPE_KEYBIND = "key.roughlyenoughitems.recipe_keybind";
    private static final String USAGE_KEYBIND = "key.roughlyenoughitems.usage_keybind";
    private static final String HIDE_KEYBIND = "key.roughlyenoughitems.hide_keybind";
    public static KeyBinding RECIPE, USAGE, HIDE;
    
    @SubscribeEvent
    public static void setupKeyBinds(FMLClientSetupEvent event) {
        String category = "key.rei.category";
        ClientRegistry.registerKeyBinding(RECIPE = new KeyBinding(RECIPE_KEYBIND, KeyConflictContext.IN_GAME, KeyModifier.NONE, InputMappings.Type.KEYSYM.getOrMakeInput(82), category));
        ClientRegistry.registerKeyBinding(USAGE = new KeyBinding(USAGE_KEYBIND, KeyConflictContext.IN_GAME, KeyModifier.NONE, InputMappings.Type.KEYSYM.getOrMakeInput(85), category));
        ClientRegistry.registerKeyBinding(HIDE = new KeyBinding(HIDE_KEYBIND, KeyConflictContext.IN_GAME, KeyModifier.NONE, InputMappings.Type.KEYSYM.getOrMakeInput(79), category));
    }
    
}
