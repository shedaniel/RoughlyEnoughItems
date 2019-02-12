package me.shedaniel.rei.client;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import io.netty.buffer.Unpooled;
import me.shedaniel.rei.RoughlyEnoughItemsCore;
import me.shedaniel.rei.api.IRecipeCategory;
import me.shedaniel.rei.api.IRecipeDisplay;
import me.shedaniel.rei.gui.ContainerScreenOverlay;
import me.shedaniel.rei.gui.widget.ConfigWidget;
import me.shedaniel.rei.gui.widget.RecipeViewingWidgetScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.FabricKeyBinding;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.fabricmc.fabric.impl.client.keybinding.KeyBindingRegistryImpl;
import net.fabricmc.loader.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.gui.Screen;
import net.minecraft.client.util.InputUtil;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.DefaultedList;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.util.registry.Registry;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClientHelper implements ClientModInitializer {
    
    private static final Identifier RECIPE_KEYBIND = new Identifier("roughlyenoughitems", "recipe_keybind");
    private static final Identifier USAGE_KEYBIND = new Identifier("roughlyenoughitems", "usage_keybind");
    private static final Identifier HIDE_KEYBIND = new Identifier("roughlyenoughitems", "hide_keybind");
    public static FabricKeyBinding RECIPE, USAGE, HIDE;
    private static List<ItemStack> itemList;
    private static boolean cheating;
    
    public ClientHelper() {
        this.itemList = Lists.newLinkedList();
    }
    
    public static String getModFromItemStack(ItemStack stack) {
        if (!stack.isEmpty()) {
            Identifier location = Registry.ITEM.getId(stack.getItem());
            assert location != null;
            String modid = location.getNamespace();
            if (modid.equalsIgnoreCase("minecraft"))
                return "Minecraft";
            return FabricLoader.INSTANCE.getModContainers().stream().map(modContainer -> {
                return modContainer.getInfo();
            }).filter(modInfo -> modInfo.getId().equals(modid) || (modInfo.getName() != null && modInfo.getName().equals(modid))).findFirst().map(modInfo -> {
                if (modInfo.getName() != null)
                    return modInfo.getName();
                return modid;
            }).orElse(modid);
        }
        return "";
    }
    
    public static List<ItemStack> getItemList() {
        return itemList;
    }
    
    public static Point getMouseLocation() {
        MinecraftClient client = MinecraftClient.getInstance();
        Mouse mouse = client.mouse;
        double double_1 = mouse.getX() * (double) client.window.getScaledWidth() / (double) client.window.getWidth();
        double double_2 = mouse.getY() * (double) client.window.getScaledHeight() / (double) client.window.getHeight();
        return new Point((int) double_1, (int) double_2);
    }
    
    public static boolean isCheating() {
        return cheating;
    }
    
    public static void setCheating(boolean cheating) {
        ClientHelper.cheating = cheating;
    }
    
    public static void sendDeletePacket() {
        if (MinecraftClient.getInstance().interactionManager.hasCreativeInventory()) {
            MinecraftClient.getInstance().player.inventory.setCursorStack(ItemStack.EMPTY);
            return;
        }
        ClientSidePacketRegistry.INSTANCE.sendToServer(RoughlyEnoughItemsCore.DELETE_ITEMS_PACKET, new PacketByteBuf(Unpooled.buffer()));
    }
    
    public static boolean tryCheatingStack(ItemStack cheatedStack) {
        if (MinecraftClient.getInstance().isInSingleplayer()) {
            try {
                ClientSidePacketRegistry.INSTANCE.sendToServer(RoughlyEnoughItemsCore.CREATE_ITEMS_PACKET, new PacketByteBuf(Unpooled.buffer()).writeItemStack(cheatedStack.copy()));
                return true;
            } catch (Exception e) {
                return false;
            }
        } else {
            Identifier location = Registry.ITEM.getId(cheatedStack.getItem());
            String tagMessage = cheatedStack.copy().getTag() != null && !cheatedStack.copy().getTag().isEmpty() ? cheatedStack.copy().getTag().asString() : "";
            String madeUpCommand = RoughlyEnoughItemsCore.getConfigHelper().getGiveCommandPrefix() + " " + MinecraftClient.getInstance().player.getEntityName() + " " + location.toString() + tagMessage + (cheatedStack.getAmount() != 1 ? " " + cheatedStack.getAmount() : "");
            if (madeUpCommand.length() > 256)
                madeUpCommand = RoughlyEnoughItemsCore.getConfigHelper().getGiveCommandPrefix() + " " + MinecraftClient.getInstance().player.getEntityName() + " " + location.toString() + (cheatedStack.getAmount() != 1 ? " " + cheatedStack.getAmount() : "");
            MinecraftClient.getInstance().player.sendChatMessage(madeUpCommand);
            return true;
        }
    }
    
    public static boolean executeRecipeKeyBind(ContainerScreenOverlay overlay, ItemStack stack) {
        Map<IRecipeCategory, List<IRecipeDisplay>> map = RecipeHelper.getInstance().getRecipesFor(stack);
        if (map.keySet().size() > 0)
            MinecraftClient.getInstance().openScreen(new RecipeViewingWidgetScreen(MinecraftClient.getInstance().window, map));
        return map.keySet().size() > 0;
    }
    
    public static boolean executeUsageKeyBind(ContainerScreenOverlay overlay, ItemStack stack) {
        Map<IRecipeCategory, List<IRecipeDisplay>> map = RecipeHelper.getInstance().getUsagesFor(stack);
        if (map.keySet().size() > 0)
            MinecraftClient.getInstance().openScreen(new RecipeViewingWidgetScreen(MinecraftClient.getInstance().window, map));
        return map.keySet().size() > 0;
    }
    
    public static void openConfigWindow(Screen parent) {
        MinecraftClient.getInstance().openScreen(new ConfigWidget(parent));
    }
    
    public static List<ItemStack> getInventoryItemsTypes() {
        List<DefaultedList<ItemStack>> field_7543 = ImmutableList.of(MinecraftClient.getInstance().player.inventory.main, MinecraftClient.getInstance().player.inventory.armor, MinecraftClient.getInstance().player.inventory.offHand);
        List<ItemStack> inventoryStacks = new ArrayList<>();
        field_7543.forEach(itemStacks -> itemStacks.forEach(itemStack -> {
            if (!itemStack.getItem().equals(Items.AIR))
                inventoryStacks.add(itemStack);
        }));
        return inventoryStacks;
    }
    
    public void clientLoaded() {
        Registry.ITEM.forEach(item -> {
            if (!item.equals(Items.ENCHANTED_BOOK))
                registerItem(item);
        });
        Registry.ENCHANTMENT.forEach(enchantment -> {
            for(int i = enchantment.getMinimumLevel(); i < enchantment.getMaximumLevel(); i++) {
                Map<Enchantment, Integer> map = new HashMap<>();
                map.put(enchantment, i);
                ItemStack itemStack = new ItemStack(Items.ENCHANTED_BOOK);
                EnchantmentHelper.set(map, itemStack);
                registerItemStack(itemStack);
            }
        });
    }
    
    public void registerItem(Item item) {
        registerItemStack(item.getDefaultStack());
        DefaultedList<ItemStack> stacks = DefaultedList.create();
        item.addStacksForDisplay(item.getItemGroup(), stacks);
        stacks.forEach(this::registerItemStack);
    }
    
    public void registerItemStack(ItemStack stack) {
        if (!stack.getItem().equals(Items.AIR) && !alreadyContain(stack))
            itemList.add(stack);
    }
    
    private boolean alreadyContain(ItemStack stack) {
        for(ItemStack itemStack : itemList)
            if (ItemStack.areEqual(stack, itemStack))
                return true;
        return false;
    }
    
    @Override
    public void onInitializeClient() {
        this.cheating = false;
        registerFabricKeyBinds();
    }
    
    private void registerFabricKeyBinds() {
        String category = "key.rei.category";
        KeyBindingRegistryImpl.INSTANCE.addCategory(category);
        KeyBindingRegistryImpl.INSTANCE.register(RECIPE = FabricKeyBinding.Builder.create(RECIPE_KEYBIND, InputUtil.Type.KEY_KEYBOARD, 82, category).build());
        KeyBindingRegistryImpl.INSTANCE.register(USAGE = FabricKeyBinding.Builder.create(USAGE_KEYBIND, InputUtil.Type.KEY_KEYBOARD, 85, category).build());
        KeyBindingRegistryImpl.INSTANCE.register(HIDE = FabricKeyBinding.Builder.create(HIDE_KEYBIND, InputUtil.Type.KEY_KEYBOARD, 79, category).build());
    }
    
}
