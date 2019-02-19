package me.shedaniel.rei;

import me.shedaniel.rei.api.IItemRegisterer;
import me.shedaniel.rei.api.IPluginDisabler;
import me.shedaniel.rei.client.*;
import me.shedaniel.rei.gui.ContainerGuiOverlay;
import me.shedaniel.rei.gui.config.ConfigGui;
import me.shedaniel.rei.plugin.PluginManager;
import me.shedaniel.rei.update.UpdateAnnouncer;
import me.shedaniel.rei.update.UpdateChecker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.client.gui.inventory.GuiCrafting;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.item.ItemGroup;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RecipesUpdatedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

@Mod(RoughlyEnoughItemsCore.MOD_ID)
public class RoughlyEnoughItemsCore {
    
    public static final String MOD_ID = "roughlyenoughitems";
    
    public static final Logger LOGGER = LogManager.getFormatterLogger("roughlyenoughitems");
    private static final RecipeHelper RECIPE_HELPER = new RecipeHelper();
    private static final ItemListHelper ITEM_LIST_HELPER = new ItemListHelper();
    private static final PluginManager PLUGIN_MANAGER = new PluginManager();
    private static ConfigHelper configHelper;
    
    public RoughlyEnoughItemsCore() {
        final IEventBus eventBus = MinecraftForge.EVENT_BUS;
        DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
            configHelper = new ConfigHelper();
            UpdateChecker.onInitialization();
            
            // Setup Mod
            FMLJavaModLoadingContext.get().getModEventBus().addListener(EventPriority.NORMAL, this::onModClientSetup);
            
            // Register More Events
            eventBus.addListener(EventPriority.NORMAL, true, TickEvent.ClientTickEvent.class, GuiHelper::clientTick);
            eventBus.addListener(EventPriority.NORMAL, false, TickEvent.ClientTickEvent.class, UpdateAnnouncer::clientTick);
            eventBus.register(this);
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
    
    public void onModClientSetup(FMLClientSetupEvent event) {
        KeyBindHelper.setupKeyBinds();
        RoughlyEnoughItemsPlugin.discoverPlugins();
        
        ModList.get().getModContainerById(MOD_ID).ifPresent(c -> c.registerExtensionPoint(ExtensionPoint.CONFIGGUIFACTORY, () -> (client, parent) -> new ConfigGui(parent)));
    }
    
    @SubscribeEvent
    public void onRecipesUpdated(RecipesUpdatedEvent event) {
        getRecipeHelper().recipesLoaded(Minecraft.getInstance().getConnection().getRecipeManager());
    }
    
    @SubscribeEvent
    public void onGuiInit(GuiScreenEvent.InitGuiEvent.Post event) {
        if (event.getGui() instanceof GuiContainer) {
            GuiContainer container = (GuiContainer) event.getGui();
            GuiHelper.setLastGuiContainer(container);
            ((List) container.getChildren()).add(GuiHelper.getLastOverlay(true));
        }
    }
    
    @SubscribeEvent
    public void onGuiDraw(GuiScreenEvent.DrawScreenEvent.Post event) {
        if (event.getGui() instanceof GuiContainer) {
            GuiContainer container = (GuiContainer) event.getGui();
            if (container instanceof GuiContainerCreative)
                if (((GuiContainerCreative) container).getSelectedTabIndex() != ItemGroup.INVENTORY.getIndex())
                    return;
            GuiHelper.getLastOverlay().renderOverlay(event.getMouseX(), event.getMouseY(), event.getRenderPartialTicks());
        }
    }
    
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onGuiKeyPressed(GuiScreenEvent.KeyboardKeyPressedEvent.Pre event) {
        if (event.getGui() instanceof GuiContainer && GuiHelper.getLastOverlay().keyPressed(event.getKeyCode(), event.getScanCode(), event.getModifiers())) {
            event.setCanceled(true);
            event.setResult(Event.Result.ALLOW);
        }
    }
    
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onMouseScrolled(GuiScreenEvent.MouseScrollEvent.Pre event) {
        if (event.getGui() instanceof GuiContainer) {
            ContainerGuiOverlay overlay = GuiHelper.getLastOverlay();
            if (GuiHelper.isOverlayVisible() && overlay.getRectangle().contains(ClientHelper.getMouseLocation()))
                if (overlay.mouseScrolled(event.getScrollDelta())) {
                    event.setCanceled(true);
                    event.setResult(Event.Result.ALLOW);
                }
        }
    }
    
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onCharTyped(GuiScreenEvent.KeyboardCharTypedEvent.Pre event) {
        if (event.getGui() instanceof GuiContainerCreative) {
            GuiContainerCreative containerCreative = (GuiContainerCreative) event.getGui();
            ContainerGuiOverlay overlay = GuiHelper.getLastOverlay();
            if (GuiHelper.isOverlayVisible() && containerCreative.getSelectedTabIndex() == ItemGroup.INVENTORY.getIndex())
                if (overlay.charTyped(event.getCodePoint(), event.getModifiers())) {
                    event.setCanceled(true);
                    event.setResult(Event.Result.ALLOW);
                }
        } else if (event.getGui() instanceof GuiInventory || event.getGui() instanceof GuiCrafting) {
            GuiContainer container = (GuiContainer) event.getGui();
            ContainerGuiOverlay overlay = GuiHelper.getLastOverlay();
            if (GuiHelper.isOverlayVisible() && overlay.charTyped(event.getCodePoint(), event.getModifiers())) {
                event.setCanceled(true);
                event.setResult(Event.Result.ALLOW);
            }
        }
    }
    
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onGuiShift(GuiScreenEvent.PotionShiftEvent event) {
        event.setCanceled(true);
    }
    
}
