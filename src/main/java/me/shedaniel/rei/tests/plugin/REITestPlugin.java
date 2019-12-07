package me.shedaniel.rei.tests.plugin;

import me.shedaniel.rei.api.EntryRegistry;
import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.api.plugins.REIPluginV0;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.LogManager;
import org.jetbrains.annotations.TestOnly;

@TestOnly
@Deprecated
public class REITestPlugin implements REIPluginV0 {
    
    @Override
    public void preRegister() {
        LogManager.getLogger().error("REI Test Plugin is enabled! If you see this unintentionally, please report this!");
    }
    
    @Override
    public Identifier getPluginIdentifier() {
        return new Identifier("roughlyenoughitems:test_dev_plugin");
    }
    
    @Override
    public void registerEntries(EntryRegistry entryRegistry) {
        for (Item item : Registry.ITEM) {
            entryRegistry.registerEntryAfter(null, EntryStack.create(item), false);
            entryRegistry.registerEntryAfter(null, EntryStack.create(item), false);
            entryRegistry.registerEntryAfter(null, EntryStack.create(item), false);
            try {
                for (ItemStack stack : entryRegistry.getAllStacksFromItem(item)) {
                    entryRegistry.registerEntryAfter(null, EntryStack.create(stack), false);
                    entryRegistry.registerEntryAfter(null, EntryStack.create(stack), false);
                    entryRegistry.registerEntryAfter(null, EntryStack.create(stack), false);
                }
            } catch (Exception e) {
            }
        }
    }
}
