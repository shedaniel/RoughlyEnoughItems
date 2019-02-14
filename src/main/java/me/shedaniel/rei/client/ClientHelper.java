package me.shedaniel.rei.client;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import io.netty.buffer.Unpooled;
import me.shedaniel.rei.RoughlyEnoughItemsCore;
import me.shedaniel.rei.api.IRecipeCategory;
import me.shedaniel.rei.api.IRecipeDisplay;
import me.shedaniel.rei.gui.ContainerScreenOverlay;
import me.shedaniel.rei.gui.config.ConfigScreen;
import me.shedaniel.rei.gui.widget.RecipeViewingWidgetScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.FabricKeyBinding;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.fabricmc.fabric.impl.client.keybinding.KeyBindingRegistryImpl;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.gui.Screen;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.DefaultedList;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.util.registry.Registry;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class ClientHelper implements ClientModInitializer {
    
    private static final Identifier RECIPE_KEYBIND = new Identifier("roughlyenoughitems", "recipe_keybind");
    private static final Identifier USAGE_KEYBIND = new Identifier("roughlyenoughitems", "usage_keybind");
    private static final Identifier HIDE_KEYBIND = new Identifier("roughlyenoughitems", "hide_keybind");
    public static FabricKeyBinding RECIPE, USAGE, HIDE;
    private static boolean cheating;
    private final List<ItemStack> itemList;
    
    public ClientHelper() {
        this.itemList = Lists.newLinkedList();
    }
    
    public static ClientHelper getInstance() {
        return RoughlyEnoughItemsCore.getClientHelper();
    }
    
    public static String getModFromItemStack(ItemStack stack) {
        if (!stack.isEmpty()) {
            Identifier location = Registry.ITEM.getId(stack.getItem());
            assert location != null;
            String modid = location.getNamespace();
            if (modid.equalsIgnoreCase("minecraft"))
                return "Minecraft";
            return ((net.fabricmc.loader.FabricLoader) FabricLoader.getInstance()).getModContainers().stream().map(modContainer -> {
                return modContainer.getInfo();
            }).filter(modInfo -> modInfo.getId().equals(modid) || (modInfo.getName() != null && modInfo.getName().equals(modid))).findFirst().map(modInfo -> {
                if (modInfo.getName() != null)
                    return modInfo.getName();
                return modid;
            }).orElse(modid);
        }
        return "";
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
        MinecraftClient.getInstance().openScreen(new ConfigScreen(parent));
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
    
    public List<ItemStack> getItemList() {
        return Collections.unmodifiableList(itemList);
    }
    
    @Deprecated
    public List<ItemStack> getModifiableItemList() {
        return itemList;
    }
    
    public ItemStack[] getAllStacksFromItem(Item item) {
        List<ItemStack> list = Lists.newLinkedList();
        list.add(item.getDefaultStack());
        DefaultedList<ItemStack> stacks = DefaultedList.create();
        item.addStacksForDisplay(item.getItemGroup(), stacks);
        stacks.forEach(list::add);
        TreeSet<ItemStack> stackSet = list.stream().collect(Collectors.toCollection(() -> new TreeSet<ItemStack>((p1, p2) -> ItemStack.areEqual(p1, p2) ? 0 : 1)));
        RoughlyEnoughItemsCore.LOGGER.info("size is " + stackSet.size());
        return Lists.newArrayList(stackSet).toArray(new ItemStack[0]);
    }
    
    public void registerItemStack(Item afterItem, ItemStack stack) {
        if (!stack.isEmpty() && !itemList.stream().anyMatch(stack1 -> ItemStack.areEqual(stack, stack1)))
            if (afterItem == null || afterItem.equals(Items.AIR))
                itemList.add(stack);
            else {
                int last = itemList.size();
                for(int i = 0; i < itemList.size(); i++)
                    if (itemList.get(i).getItem().equals(afterItem))
                        last = i + 1;
                itemList.add(last, stack);
            }
    }
    
    public void registerItemStack(Item afterItem, ItemStack... stacks) {
        for(ItemStack stack : stacks)
            if (stack != null && !stack.isEmpty())
                registerItemStack(afterItem, stack);
    }
    
    public void registerItemStack(ItemStack... stacks) {
        for(ItemStack stack : stacks)
            if (stack != null && !stack.isEmpty())
                registerItemStack(null, stack);
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
