package me.shedaniel.rei;

//public class RoughlyEnoughItemsMixin implements InitializationListener {
//
//    @Override
//    public void onInitialization() {
//        MixinBootstrap.init();
//        Mixins.addConfiguration("roughlyenoughitems.client.json");
//    }
//
//}

import me.shedaniel.rei.client.*;
import me.shedaniel.rei.gui.ContainerGuiOverlay;
import me.shedaniel.rei.gui.config.ConfigGui;
import me.shedaniel.rei.update.UpdateAnnouncer;
import me.shedaniel.rei.update.UpdateChecker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.client.gui.inventory.GuiCrafting;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.item.ItemGroup;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RecipesUpdatedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.List;

public class RoughlyEnoughItemsClient {
    
    public static void setup() {
        final IEventBus eventBus = MinecraftForge.EVENT_BUS;
        
        RoughlyEnoughItemsCore.configHelper = new ConfigHelper();
        UpdateChecker.onInitialization();
        KeyBindHelper.setupKeyBinds();
        RoughlyEnoughItemsPlugin.discoverPlugins();
        
        ModList.get().getModContainerById(RoughlyEnoughItemsCore.MOD_ID).ifPresent(c -> c.registerExtensionPoint(ExtensionPoint.CONFIGGUIFACTORY, () -> (client, parent) -> new ConfigGui(parent)));
        
        // Register More Events
        eventBus.addListener(EventPriority.NORMAL, true, TickEvent.ClientTickEvent.class, GuiHelper::clientTick);
        eventBus.addListener(EventPriority.NORMAL, false, TickEvent.ClientTickEvent.class, UpdateAnnouncer::clientTick);
        eventBus.register(RoughlyEnoughItemsClient.class);
    }
    
    @SubscribeEvent
    public static void onRecipesUpdated(RecipesUpdatedEvent event) {
        ((RecipeHelperImpl) RoughlyEnoughItemsCore.getRecipeHelper()).recipesLoaded(Minecraft.getInstance().getConnection().getRecipeManager());
    }
    
    @SubscribeEvent
    public static void onGuiInit(GuiScreenEvent.InitGuiEvent.Post event) {
        if (event.getGui() instanceof GuiContainer) {
            GuiContainer container = (GuiContainer) event.getGui();
            GuiHelper.setLastGuiContainer(container);
            ((List) container.getChildren()).add(GuiHelper.getLastOverlay(true, false));
        }
    }
    
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onGuiDraw(GuiScreenEvent.DrawScreenEvent.Post event) {
        if (event.getGui() instanceof GuiContainer) {
            GuiContainer container = (GuiContainer) event.getGui();
            if (container instanceof GuiContainerCreative)
                if (((GuiContainerCreative) container).getSelectedTabIndex() != ItemGroup.INVENTORY.getIndex())
                    return;
            GuiHelper.getLastOverlay().renderOverlay(event.getMouseX(), event.getMouseY(), event.getRenderPartialTicks());
        }
    }
    
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onGuiKeyPressed(GuiScreenEvent.KeyboardKeyPressedEvent.Pre event) {
        if (event.getGui() instanceof GuiContainer && GuiHelper.getLastOverlay().keyPressed(event.getKeyCode(), event.getScanCode(), event.getModifiers())) {
            event.setCanceled(true);
            event.setResult(Event.Result.ALLOW);
        }
    }
    
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onMouseScrolled(GuiScreenEvent.MouseScrollEvent.Pre event) {
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
    public static void onCharTyped(GuiScreenEvent.KeyboardCharTypedEvent.Pre event) {
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
    public static void onGuiShift(GuiScreenEvent.PotionShiftEvent event) {
        event.setCanceled(true);
    }
    
}
