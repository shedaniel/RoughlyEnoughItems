package me.shedaniel.rei.client;

import com.google.common.collect.Lists;
import io.netty.buffer.Unpooled;
import me.shedaniel.rei.RoughlyEnoughItemsCore;
import me.shedaniel.rei.api.IRecipeCategory;
import me.shedaniel.rei.api.IRecipeDisplay;
import me.shedaniel.rei.gui.ContainerGuiOverlay;
import me.shedaniel.rei.gui.widget.RecipeViewingWidget;
import me.shedaniel.rei.listeners.ClientLoaded;
import me.shedaniel.rei.listeners.IMixinContainerGui;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.packet.CustomPayloadServerPacket;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.util.registry.Registry;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ClientHelper implements ClientLoaded, ClientModInitializer {
    
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
            return FabricLoader.INSTANCE.getModContainers().stream()
                    .map(modContainer -> {
                        return modContainer.getInfo();
                    })
                    .filter(modInfo -> modInfo.getId().equals(modid) || (modInfo.getName() != null && modInfo.getName().equals(modid)))
                    .findFirst().map(modInfo -> {
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
        double double_1 = mouse.getX() * (double) client.window.getScaledWidth() / (double) client.window.method_4480();
        double double_2 = mouse.getY() * (double) client.window.getScaledHeight() / (double) client.window.method_4507();
        return new Point((int) double_1, (int) double_2);
    }
    
    public static boolean isCheating() {
        return cheating;
    }
    
    public static void setCheating(boolean cheating) {
        ClientHelper.cheating = cheating;
    }
    
    public static void sendDeletePacket() {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        MinecraftClient.getInstance().getNetworkHandler().sendPacket(new CustomPayloadServerPacket(RoughlyEnoughItemsCore.DELETE_ITEMS_PACKET, buf));
    }
    
    public static boolean tryCheatingStack(ItemStack cheatedStack) {
        try {
            PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
            buf.writeItemStack(cheatedStack.copy());
            MinecraftClient.getInstance().getNetworkHandler().sendPacket(new CustomPayloadServerPacket(RoughlyEnoughItemsCore.CREATE_ITEMS_PACKET, buf));
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    public static boolean executeRecipeKeyBind(ContainerGuiOverlay overlay, ItemStack stack, IMixinContainerGui parent) {
        Map<IRecipeCategory, List<IRecipeDisplay>> map = RecipeHelper.getRecipesFor(stack);
        if (map.keySet().size() > 0)
            MinecraftClient.getInstance().openGui(new RecipeViewingWidget(overlay, MinecraftClient.getInstance().window, parent, map));
        return map.keySet().size() > 0;
    }
    
    public static boolean executeUsageKeyBind() {
        return false;
    }
    
    @Override
    public void clientLoaded() {
        Registry.ITEM.forEach(this::registerItem);
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
    }
    
    public void registerItemStack(ItemStack stack) {
        if (!stack.getItem().equals(Items.AIR))
            itemList.add(stack);
    }
    
    @Override
    public void onInitializeClient() {
        this.cheating = false;
    }
    
}
