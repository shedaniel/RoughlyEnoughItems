package me.shedaniel.rei.client;

import com.google.common.collect.ImmutableList;
import me.shedaniel.rei.RoughlyEnoughItemsCore;
import me.shedaniel.rei.api.RecipeCategory;
import me.shedaniel.rei.api.RecipeDisplay;
import me.shedaniel.rei.api.RecipeHelper;
import me.shedaniel.rei.gui.RecipeViewingGui;
import me.shedaniel.rei.gui.config.ConfigGui;
import me.shedaniel.rei.network.CreateItemsPacket;
import me.shedaniel.rei.network.DeleteItemsPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHelper;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.IRegistry;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.moddiscovery.ModInfo;
import net.minecraftforge.registries.ForgeRegistries;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ClientHelper {
    
    public static String getModFromItemStack(ItemStack stack) {
        if (!stack.isEmpty()) {
            ResourceLocation location = ForgeRegistries.ITEMS.getKey(stack.getItem());
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
        return RoughlyEnoughItemsCore.getConfigManager().getConfig().cheating;
    }
    
    public static void setCheating(boolean cheating) {
        RoughlyEnoughItemsCore.getConfigManager().getConfig().cheating = cheating;
        try {
            RoughlyEnoughItemsCore.getConfigManager().saveConfig();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static void sendDeletePacket() {
        if (GuiHelper.getLastGuiContainer() instanceof GuiContainerCreative) {
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
            String og = cheatedStack.getCount() != 1 ? RoughlyEnoughItemsCore.getConfigManager().getConfig().giveCommand.replaceAll(" \\{count}", "").replaceAll("\\{count}", "") : RoughlyEnoughItemsCore.getConfigManager().getConfig().giveCommand;
            String madeUpCommand = og.replaceAll("\\{player_name}", Minecraft.getInstance().player.getScoreboardName()).replaceAll("\\{item_identifier}", location.toString()).replaceAll("\\{nbt}", tagMessage).replaceAll("\\{count}", String.valueOf(cheatedStack.getCount()));
            if (madeUpCommand.length() > 256) {
                madeUpCommand = og.replaceAll("\\{player_name}", Minecraft.getInstance().player.getScoreboardName()).replaceAll("\\{item_identifier}", location.toString()).replaceAll("\\{nbt}", "").replaceAll("\\{count}", String.valueOf(cheatedStack.getCount()));
                Minecraft.getInstance().player.sendStatusMessage(new TextComponentTranslation("text.rei.too_long_nbt"), false);
            }
            System.out.println(madeUpCommand);
            Minecraft.getInstance().player.sendChatMessage(madeUpCommand);
            return true;
        }
    }
    
    public static boolean executeRecipeKeyBind(ItemStack stack) {
        Map<RecipeCategory, List<RecipeDisplay>> map = RecipeHelper.getInstance().getRecipesFor(stack);
        if (map.keySet().size() > 0)
            Minecraft.getInstance().displayGuiScreen(new RecipeViewingGui(Minecraft.getInstance().mainWindow, map));
        return map.keySet().size() > 0;
    }
    
    public static boolean executeUsageKeyBind(ItemStack stack) {
        Map<RecipeCategory, List<RecipeDisplay>> map = RecipeHelper.getInstance().getUsagesFor(stack);
        if (map.keySet().size() > 0)
            Minecraft.getInstance().displayGuiScreen(new RecipeViewingGui(Minecraft.getInstance().mainWindow, map));
        return map.keySet().size() > 0;
    }
    
    public static void openConfigWindow(GuiScreen parent) {
        Minecraft.getInstance().displayGuiScreen(new ConfigGui(parent));
    }
    
    public static boolean executeViewAllRecipesKeyBind() {
        Map<RecipeCategory, List<RecipeDisplay>> map = RecipeHelper.getInstance().getAllRecipes();
        if (map.keySet().size() > 0)
            Minecraft.getInstance().displayGuiScreen(new RecipeViewingGui(Minecraft.getInstance().mainWindow, map));
        return map.keySet().size() > 0;
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
