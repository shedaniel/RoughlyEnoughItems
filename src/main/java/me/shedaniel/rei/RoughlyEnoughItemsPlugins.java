package me.shedaniel.rei;

import info.tehnut.pluginloader.LoaderCreator;
import info.tehnut.pluginloader.PluginLoaderBuilder;
import info.tehnut.pluginloader.ValidationStrategy;
import me.shedaniel.rei.api.IRecipePlugin;
import net.fabricmc.loader.language.LanguageAdapter;
import net.fabricmc.loader.language.LanguageAdapterException;
import net.minecraft.util.Identifier;

public class RoughlyEnoughItemsPlugins implements LoaderCreator {

    @Override
    public void createLoaders() {
        LanguageAdapter.Options instantiationOptions = new LanguageAdapter.Options();

        new PluginLoaderBuilder("roughlyenoughitems")
                .withValidator(ValidationStrategy.hasInterface(IRecipePlugin.class))
                .withInitializer((aClass, container) -> {
                    Identifier id = new Identifier(container.getOwner().getInfo().getId(), container.getInfo().getId());
                    try {
                        IRecipePlugin plugin = (IRecipePlugin) container.getOwner().getAdapter().createInstance(aClass, instantiationOptions);
                        RoughlyEnoughItemsCore.registerPlugin(id, plugin);
                        RoughlyEnoughItemsCore.LOGGER.info("Registered plugin %s from %s", id, aClass);
                    } catch (LanguageAdapterException e) {
                        RoughlyEnoughItemsCore.LOGGER.error("Error loading plugin %s", id, e);
                    }
                })
                .build();
    }
}
