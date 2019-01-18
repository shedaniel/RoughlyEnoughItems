package me.shedaniel.rei.client;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import me.shedaniel.rei.api.IRecipeCategory;
import me.shedaniel.rei.api.IRecipeDisplay;
import me.shedaniel.rei.gui.ContainerGuiOverlay;
import me.shedaniel.rei.gui.widget.ConfigWidget;
import me.shedaniel.rei.gui.widget.RecipeViewingWidget;
import me.shedaniel.rei.listeners.ClientLoaded;
import me.shedaniel.rei.listeners.IMixinGuiContainer;
import me.shedaniel.rei.network.CreateItemsMessage;
import me.shedaniel.rei.network.DeleteItemsMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHelper;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.EnumPacketDirection;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.IRegistry;
import org.dimdev.rift.listener.client.KeyBindingAdder;
import org.dimdev.riftloader.RiftLoader;

import java.awt.*;
import java.util.*;
import java.util.List;

public class ClientHelper implements ClientLoaded, KeyBindingAdder {
    
    private static final ResourceLocation RECIPE_KEYBIND = new ResourceLocation("roughlyenoughitems", "recipe_keybind");
    private static final ResourceLocation USAGE_KEYBIND = new ResourceLocation("roughlyenoughitems", "usage_keybind");
    private static final ResourceLocation HIDE_KEYBIND = new ResourceLocation("roughlyenoughitems", "hide_keybind");
    public static KeyBinding RECIPE, USAGE, HIDE;
    private static List<ItemStack> itemList;
    private static boolean cheating = false;
    
    public ClientHelper() {
        this.itemList = Lists.newLinkedList();
    }
    
    public static String getModFromItemStack(ItemStack stack) {
        if (!stack.isEmpty()) {
            ResourceLocation location = IRegistry.ITEM.getKey(stack.getItem());
            assert location != null;
            String modid = location.getNamespace();
            if (modid.equalsIgnoreCase("minecraft"))
                return "Minecraft";
            return RiftLoader.instance.getMods().stream()
                    .filter(modInfo -> modInfo.id.equals(modid) || (modInfo.name != null && modInfo.name.equals(modid)))
                    .findFirst().map(modInfo -> {
                        if (modInfo.name != null)
                            return modInfo.name;
                        return modid;
                    }).orElse(modid);
        }
        return "";
    }
    
    public static List<ItemStack> getItemList() {
        return itemList;
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
        DeleteItemsMessage message = new DeleteItemsMessage();
        Minecraft.getInstance().getConnection().sendPacket(message.toPacket(EnumPacketDirection.CLIENTBOUND));
    }
    
    public static boolean tryCheatingStack(ItemStack cheatedStack) {
        try {
            CreateItemsMessage message = new CreateItemsMessage(cheatedStack.copy());
            Minecraft.getInstance().getConnection().sendPacket(message.toPacket(EnumPacketDirection.CLIENTBOUND));
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    public static boolean executeRecipeKeyBind(ContainerGuiOverlay overlay, ItemStack stack, IMixinGuiContainer parent) {
        Map<IRecipeCategory, List<IRecipeDisplay>> map = RecipeHelper.getRecipesFor(stack);
        if (map.keySet().size() > 0)
            Minecraft.getInstance().displayGuiScreen(new RecipeViewingWidget(Minecraft.getInstance().mainWindow, parent, map));
        return map.keySet().size() > 0;
    }
    
    public static boolean executeUsageKeyBind(ContainerGuiOverlay overlay, ItemStack stack, IMixinGuiContainer parent) {
        Map<IRecipeCategory, List<IRecipeDisplay>> map = RecipeHelper.getUsagesFor(stack);
        if (map.keySet().size() > 0)
            Minecraft.getInstance().displayGuiScreen(new RecipeViewingWidget(Minecraft.getInstance().mainWindow, parent, map));
        return map.keySet().size() > 0;
    }
    
    public static void openConfigWindow(GuiScreen parent) {
        Minecraft.getInstance().displayGuiScreen(new ConfigWidget(parent));
    }
    
    public static List<ItemStack> getInventoryItemsTypes() {
        List<NonNullList<ItemStack>> field_7543 = ImmutableList.of(Minecraft.getInstance().player.inventory.mainInventory, Minecraft.getInstance().player.inventory.armorInventory
                , Minecraft.getInstance().player.inventory.offHandInventory);
        List<ItemStack> inventoryStacks = new ArrayList<>();
        field_7543.forEach(itemStacks -> itemStacks.forEach(itemStack -> {
            if (!itemStack.getItem().equals(Items.AIR))
                inventoryStacks.add(itemStack);
        }));
        return inventoryStacks;
    }
    
    @Override
    public void clientLoaded() {
        IRegistry.ITEM.forEach(item -> {
            if (!item.equals(Items.ENCHANTED_BOOK))
                registerItem(item);
        });
        IRegistry.ENCHANTMENT.forEach(enchantment -> {
            for(int i = enchantment.getMinLevel(); i < enchantment.getMaxLevel(); i++) {
                Map<Enchantment, Integer> map = new HashMap<>();
                map.put(enchantment, i);
                ItemStack itemStack = new ItemStack(Items.ENCHANTED_BOOK);
                EnchantmentHelper.setEnchantments(map, itemStack);
                registerItemStack(itemStack);
            }
        });
    }
    
    public void registerItem(Item item) {
        registerItemStack(item.getDefaultInstance());
        NonNullList<ItemStack> stacks = NonNullList.create();
        item.fillItemGroup(item.getGroup(), stacks);
        stacks.forEach(this::registerItemStack);
    }
    
    public void registerItemStack(ItemStack stack) {
        if (!stack.getItem().equals(Items.AIR) && !alreadyContain(stack))
            itemList.add(stack);
    }
    
    private boolean alreadyContain(ItemStack stack) {
        for(ItemStack itemStack : itemList)
            if (ItemStack.areItemsEqual(stack, itemStack))
                return true;
        return false;
    }
    
    @Override
    public Collection<? extends KeyBinding> getKeyBindings() {
        String category = "key.rei.category";
        List<KeyBinding> keyBindings = Lists.newArrayList();
        keyBindings.add(RECIPE = new KeyBinding(RECIPE_KEYBIND.toString().replaceAll(":", "."), InputMappings.Type.KEYSYM, 82, category));
        keyBindings.add(USAGE = new KeyBinding(USAGE_KEYBIND.toString().replaceAll(":", "."), InputMappings.Type.KEYSYM, 85, category));
        keyBindings.add(HIDE = new KeyBinding(HIDE_KEYBIND.toString().replaceAll(":", "."), InputMappings.Type.KEYSYM, 79, category));
        return keyBindings;
    }
}
