package me.shedaniel.rei.client;

import com.google.common.collect.ImmutableList;
import me.shedaniel.rei.RoughlyEnoughItemsCore;
import me.shedaniel.rei.api.Identifier;
import me.shedaniel.rei.api.RecipeCategory;
import me.shedaniel.rei.api.RecipeDisplay;
import me.shedaniel.rei.api.RecipeHelper;
import me.shedaniel.rei.gui.RecipeViewingScreen;
import me.shedaniel.rei.gui.config.ConfigScreen;
import me.shedaniel.rei.network.CreateItemPacket;
import me.shedaniel.rei.network.DeleteItemPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHelper;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.registry.IRegistry;
import net.minecraft.util.text.TextComponentTranslation;
import org.dimdev.riftloader.ModInfo;
import org.dimdev.riftloader.RiftLoader;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ClientHelper {
    
    private static final Identifier RECIPE_KEYBIND = new Identifier("roughlyenoughitems", "recipe_keybind");
    private static final Identifier USAGE_KEYBIND = new Identifier("roughlyenoughitems", "usage_keybind");
    private static final Identifier HIDE_KEYBIND = new Identifier("roughlyenoughitems", "hide_keybind");
    public static KeyBinding RECIPE, USAGE, HIDE;
    
    public static String getModFromItemStack(ItemStack stack) {
        if (!stack.isEmpty()) {
            String modid = IRegistry.ITEM.getKey(stack.getItem()).getNamespace();
            if (modid.equalsIgnoreCase("minecraft"))
                return "Minecraft";
            Optional<ModInfo> modInfo = RiftLoader.instance.getMods().stream().filter(info -> info.id.equals(modid)).findFirst();
            if (modInfo.isPresent())
                if (modInfo.get().name != null)
                    return modInfo.get().name;
                else if (modInfo.get().id != null)
                    return modInfo.get().id;
            return modid;
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
        if (ScreenHelper.getLastContainerScreen() instanceof GuiContainerCreative) {
            Minecraft.getInstance().player.inventory.setPickedItemStack(ItemStack.EMPTY);
            return;
        }
        Minecraft.getInstance().getConnection().sendPacket(new DeleteItemPacket());
    }
    
    public static boolean tryCheatingStack(ItemStack cheatedStack) {
        if (Minecraft.getInstance().isSingleplayer()) {
            try {
                Minecraft.getInstance().getConnection().sendPacket(new CreateItemPacket(cheatedStack.copy()));
                return true;
            } catch (Exception e) {
                return false;
            }
        } else {
            String identifier = IRegistry.ITEM.getKey(cheatedStack.getItem()).toString();
            String tagMessage = cheatedStack.copy().getTag() != null && !cheatedStack.copy().getTag().isEmpty() ? cheatedStack.copy().getTag().getString() : "";
            String og = cheatedStack.getCount() != 1 ? RoughlyEnoughItemsCore.getConfigManager().getConfig().giveCommand.replaceAll(" \\{count}", "").replaceAll("\\{count}", "") : RoughlyEnoughItemsCore.getConfigManager().getConfig().giveCommand;
            String madeUpCommand = og.replaceAll("\\{player_name}", Minecraft.getInstance().player.getScoreboardName()).replaceAll("\\{item_identifier}", identifier.toString()).replaceAll("\\{nbt}", tagMessage).replaceAll("\\{count}", String.valueOf(cheatedStack.getCount()));
            if (madeUpCommand.length() > 256) {
                madeUpCommand = og.replaceAll("\\{player_name}", Minecraft.getInstance().player.getScoreboardName()).replaceAll("\\{item_identifier}", identifier.toString()).replaceAll("\\{nbt}", "").replaceAll("\\{count}", String.valueOf(cheatedStack.getCount()));
                Minecraft.getInstance().player.sendStatusMessage(new TextComponentTranslation("text.rei.too_long_nbt"), false);
                return true;
            }
            Minecraft.getInstance().player.sendChatMessage(madeUpCommand);
            return true;
        }
    }
    
    public static boolean executeRecipeKeyBind(ItemStack stack) {
        Map<RecipeCategory, List<RecipeDisplay>> map = RecipeHelper.getInstance().getRecipesFor(stack);
        if (map.keySet().size() > 0)
            Minecraft.getInstance().displayGuiScreen(new RecipeViewingScreen(Minecraft.getInstance().mainWindow, map));
        return map.keySet().size() > 0;
    }
    
    public static boolean executeUsageKeyBind(ItemStack stack) {
        Map<RecipeCategory, List<RecipeDisplay>> map = RecipeHelper.getInstance().getUsagesFor(stack);
        if (map.keySet().size() > 0)
            Minecraft.getInstance().displayGuiScreen(new RecipeViewingScreen(Minecraft.getInstance().mainWindow, map));
        return map.keySet().size() > 0;
    }
    
    public static void openConfigWindow(GuiScreen parent, boolean initOverlay) {
        Minecraft.getInstance().displayGuiScreen(new ConfigScreen(parent, initOverlay));
    }
    
    public static void openConfigWindow(GuiScreen parent) {
        openConfigWindow(parent, true);
    }
    
    public static List<ItemStack> getInventoryItemsTypes() {
        List<NonNullList<ItemStack>> field_7543 = ImmutableList.of(Minecraft.getInstance().player.inventory.mainInventory, Minecraft.getInstance().player.inventory.armorInventory, Minecraft.getInstance().player.inventory.offHandInventory);
        List<ItemStack> inventoryStacks = new ArrayList<>();
        field_7543.forEach(itemStacks -> itemStacks.forEach(itemStack -> {
            if (!itemStack.isEmpty())
                inventoryStacks.add(itemStack);
        }));
        return inventoryStacks;
    }
    
    public static boolean executeViewAllRecipesKeyBind() {
        Map<RecipeCategory, List<RecipeDisplay>> map = RecipeHelper.getInstance().getAllRecipes();
        if (map.keySet().size() > 0)
            Minecraft.getInstance().displayGuiScreen(new RecipeViewingScreen(Minecraft.getInstance().mainWindow, map));
        return map.keySet().size() > 0;
    }
    
}
