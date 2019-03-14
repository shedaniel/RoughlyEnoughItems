package me.shedaniel.rei;

import info.tehnut.pluginloader.LoaderCreator;
import info.tehnut.pluginloader.PluginLoaderBuilder;
import info.tehnut.pluginloader.ValidationStrategy;
import me.shedaniel.rei.api.REIPlugin;
import net.fabricmc.loader.language.LanguageAdapter;
import net.fabricmc.loader.language.LanguageAdapterException;
import net.minecraft.util.Identifier;

public class RoughlyEnoughItemsPlugins implements LoaderCreator {
    
    @Override
    public void createLoaders() {
        LanguageAdapter.Options instantiationOptions = new LanguageAdapter.Options();
        
        new PluginLoaderBuilder("roughlyenoughitems").withValidator(ValidationStrategy.hasInterface(REIPlugin.class)).withInitializer((aClass, container) -> {
            Identifier id = new Identifier(container.getOwner().getInfo().getId(), container.getInfo().getId());
            try {
                REIPlugin plugin = (REIPlugin) container.getOwner().getAdapter().createInstance(aClass, instantiationOptions);
                RoughlyEnoughItemsCore.registerPlugin(id, plugin);
            } catch (LanguageAdapterException e) {
                RoughlyEnoughItemsCore.LOGGER.error("[REI] Error loading plugin %s", id, e);
            }
        }).build();
    }
}
