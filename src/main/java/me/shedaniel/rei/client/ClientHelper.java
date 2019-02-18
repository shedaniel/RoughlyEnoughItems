package me.shedaniel.rei.client;

import com.google.common.collect.ImmutableList;
import me.shedaniel.rei.api.IRecipeCategory;
import me.shedaniel.rei.api.IRecipeDisplay;
import me.shedaniel.rei.gui.ContainerGuiOverlay;
import me.shedaniel.rei.gui.config.ConfigGui;
import me.shedaniel.rei.gui.widget.RecipeViewingWidgetGui;
import me.shedaniel.rei.network.CreateItemsPacket;
import me.shedaniel.rei.network.DeleteItemsPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHelper;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.IRegistry;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.moddiscovery.ModInfo;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ClientHelper {
    
    private static boolean cheating = false;
    
    public static String getModFromItemStack(ItemStack stack) {
        if (!stack.isEmpty()) {
            ResourceLocation location = IRegistry.field_212630_s.getKey(stack.getItem());
            assert location != null;
            String modid = location.getNamespace();
            if (modid.equalsIgnoreCase("minecraft"))
                return "Minecraft";
            return ModList.get().getMods().stream().filter(modInfo -> modInfo.getModId().equalsIgnoreCase(modid)).map(ModInfo::getDisplayName).findAny().orElse(modid);
        }
        return "";
    }
    
    public static Point getMouseLocation() {
        Minecraft client = Minecraft.getInstance();
        MouseHelper mouse = client.mouseHelper;
        double double_1 = mouse.getMouseX() * (double) client.mainWindow.getScaledWidth() / (double) client.mainWindow.getWidth();
        double double_2 = mouse.getMouseY() * (double) client.mainWindow.getScaledHeight() / (double) client.mainWindow.getHeight();
        return new Point((int) double_1, (int) double_2);
    }
    
    public static boolean isCheating() {
        return cheating;
    }
    
    public static void setCheating(boolean cheating) {
        ClientHelper.cheating = cheating;
    }
    
    public static void sendDeletePacket() {
        if (Minecraft.getInstance().playerController.isInCreativeMode()) {
            Minecraft.getInstance().player.inventory.setItemStack(ItemStack.EMPTY);
            return;
        }
        DeleteItemsPacket message = new DeleteItemsPacket();
        Minecraft.getInstance().getConnection().sendPacket(message);
    }
    
    public static boolean tryCheatingStack(ItemStack cheatedStack) {
        if (Minecraft.getInstance().isSingleplayer()) {
            try {
                CreateItemsPacket message = new CreateItemsPacket(cheatedStack.copy());
                Minecraft.getInstance().getConnection().sendPacket(message);
                return true;
            } catch (Exception e) {
                return false;
            }
        } else {
            ResourceLocation location = IRegistry.field_212630_s.getKey(cheatedStack.getItem());
            String tagMessage = cheatedStack.copy().getTag() != null && !cheatedStack.copy().getTag().isEmpty() ? cheatedStack.copy().getTag().toString() : "";
            String madeUpCommand = ConfigHelper.getInstance().getGiveCommandPrefix() + " " + Minecraft.getInstance().player.getScoreboardName() + " " + location.toString() + tagMessage + (cheatedStack.getCount() != 1 ? " " + cheatedStack.getCount() : "");
            if (madeUpCommand.length() > 256)
                madeUpCommand = ConfigHelper.getInstance().getGiveCommandPrefix() + " " + Minecraft.getInstance().player.getScoreboardName() + " " + location.toString() + (cheatedStack.getCount() != 1 ? " " + cheatedStack.getCount() : "");
            Minecraft.getInstance().player.sendChatMessage(madeUpCommand);
            return true;
        }
    }
    
    public static boolean executeRecipeKeyBind(ContainerGuiOverlay overlay, ItemStack stack) {
        Map<IRecipeCategory, List<IRecipeDisplay>> map = RecipeHelper.getInstance().getRecipesFor(stack);
        if (map.keySet().size() > 0)
            Minecraft.getInstance().displayGuiScreen(new RecipeViewingWidgetGui(Minecraft.getInstance().mainWindow, map));
        return map.keySet().size() > 0;
    }
    
    public static boolean executeUsageKeyBind(ContainerGuiOverlay overlay, ItemStack stack) {
        Map<IRecipeCategory, List<IRecipeDisplay>> map = RecipeHelper.getInstance().getUsagesFor(stack);
        if (map.keySet().size() > 0)
            Minecraft.getInstance().displayGuiScreen(new RecipeViewingWidgetGui(Minecraft.getInstance().mainWindow, map));
        return map.keySet().size() > 0;
    }
    
    public static void openConfigWindow(GuiScreen parent) {
        Minecraft.getInstance().displayGuiScreen(new ConfigGui(parent));
    }
    
    public static List<ItemStack> getInventoryItemsTypes() {
        List<NonNullList<ItemStack>> field_7543 = ImmutableList.of(Minecraft.getInstance().player.inventory.mainInventory, Minecraft.getInstance().player.inventory.armorInventory, Minecraft.getInstance().player.inventory.offHandInventory);
        List<ItemStack> inventoryStacks = new ArrayList<>();
        field_7543.forEach(itemStacks -> itemStacks.forEach(itemStack -> {
            if (!itemStack.getItem().equals(Items.AIR))
                inventoryStacks.add(itemStack);
        }));
        return inventoryStacks;
    }
    
}
