package me.shedaniel.rei;

import me.shedaniel.rei.api.IItemRegisterer;
import me.shedaniel.rei.api.IPluginDisabler;
import me.shedaniel.rei.client.*;
import me.shedaniel.rei.gui.ContainerGuiOverlay;
import me.shedaniel.rei.gui.config.ConfigGui;
import me.shedaniel.rei.plugin.PluginManager;
import me.shedaniel.rei.update.UpdateChecker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.item.ItemGroup;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RecipesUpdatedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

@Mod(RoughlyEnoughItemsCore.MOD_ID)
public class RoughlyEnoughItemsCore {
    
    public static final String MOD_ID = "roughlyenoughitems";
    
    public static final Logger LOGGER = LogManager.getFormatterLogger("REI");
    private static final RecipeHelper RECIPE_HELPER = new RecipeHelper();
    private static final ItemListHelper ITEM_LIST_HELPER = new ItemListHelper();
    private static final PluginManager PLUGIN_MANAGER = new PluginManager();
    private static ConfigHelper configHelper;
    
    public RoughlyEnoughItemsCore() {
        DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
            UpdateChecker.onInitialization();
            configHelper = new ConfigHelper();
            
            // Setup keybinds
            FMLJavaModLoadingContext.get().getModEventBus().addListener(KeyBindHelper::setupKeyBinds);
            // Setup plugins
            FMLJavaModLoadingContext.get().getModEventBus().addListener(RoughlyEnoughItemsPlugin::discoverPlugins);
            // Setup Config Button
            FMLJavaModLoadingContext.get().getModEventBus().addListener(event -> ModList.get().getModContainerById(MOD_ID).ifPresent(o -> o.registerExtensionPoint(ExtensionPoint.CONFIGGUIFACTORY, () -> (minecraft, guiScreen) -> new ConfigGui(guiScreen))));
            
            // Register More Events
            FMLJavaModLoadingContext.get().getModEventBus().addListener(GuiHelper::clientTick);
            FMLJavaModLoadingContext.get().getModEventBus().addListener(EventPriority.NORMAL, false, RecipesUpdatedEvent.class, recipesUpdatedEvent -> RoughlyEnoughItemsCore.getRecipeHelper().recipesLoaded(Minecraft.getInstance().getConnection().getRecipeManager()));
            
            MinecraftForge.EVENT_BUS.register(this);
        });
    }
    
    public static ConfigHelper getConfigHelper() {
        return configHelper;
    }
    
    public static RecipeHelper getRecipeHelper() {
        return RECIPE_HELPER;
    }
    
    public static IItemRegisterer getItemRegisterer() {
        return ITEM_LIST_HELPER;
    }
    
    public static IPluginDisabler getPluginDisabler() {
        return PLUGIN_MANAGER;
    }
    
    @SubscribeEvent
    public void onGuiInit(GuiScreenEvent.InitGuiEvent event) {
        if (event.getGui() instanceof GuiContainer) {
            GuiContainer container = (GuiContainer) event.getGui();
            GuiHelper.setLastGuiContainer(container);
            ((List) container.getChildren()).add(GuiHelper.getLastOverlay(true));
        }
    }
    
    @SubscribeEvent
    public void onGuiRender(GuiScreenEvent.DrawScreenEvent event) {
        if (event.getGui() instanceof GuiContainer) {
            GuiContainer container = (GuiContainer) event.getGui();
            if (container instanceof GuiContainerCreative)
                if (((GuiContainerCreative) container).getSelectedTabIndex() != ItemGroup.INVENTORY.getIndex())
                    return;
            GuiHelper.getLastOverlay().renderOverlay(event.getMouseX(), event.getMouseY(), event.getRenderPartialTicks());
        }
    }
    
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onGuiKeyPressed(GuiScreenEvent.KeyboardKeyPressedEvent event) {
        if (event.getGui() instanceof GuiContainer && GuiHelper.getLastOverlay().keyPressed(event.getKeyCode(), event.getScanCode(), event.getModifiers())) {
            event.setCanceled(true);
            event.setResult(Event.Result.ALLOW);
        }
    }
    
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onMouseScrolled(GuiScreenEvent.MouseScrollEvent event) {
        if (event.getGui() instanceof GuiContainer) {
            ContainerGuiOverlay overlay = GuiHelper.getLastOverlay();
            if (GuiHelper.isOverlayVisible() && overlay.getRectangle().contains(ClientHelper.getMouseLocation()))
                if (overlay.mouseScrolled(event.getScrollDelta())) {
                    event.setCanceled(true);
                    event.setResult(Event.Result.ALLOW);
                }
        }
    }
    
}
