package me.shedaniel.rei.client;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import io.netty.buffer.Unpooled;
import me.shedaniel.rei.RoughlyEnoughItemsCore;
import me.shedaniel.rei.RoughlyEnoughItemsNetwork;
import me.shedaniel.rei.api.RecipeCategory;
import me.shedaniel.rei.api.RecipeDisplay;
import me.shedaniel.rei.api.RecipeHelper;
import me.shedaniel.rei.gui.RecipeViewingScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.FabricKeyBinding;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.fabricmc.fabric.impl.client.keybinding.KeyBindingRegistryImpl;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.ingame.CreativePlayerInventoryScreen;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.TranslatableTextComponent;
import net.minecraft.util.DefaultedList;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.util.registry.Registry;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ClientHelper implements ClientModInitializer {
    
    private static final Identifier RECIPE_KEYBIND = new Identifier("roughlyenoughitems", "recipe_keybind");
    private static final Identifier USAGE_KEYBIND = new Identifier("roughlyenoughitems", "usage_keybind");
    private static final Identifier HIDE_KEYBIND = new Identifier("roughlyenoughitems", "hide_keybind");
    private static final Map<String, String> MOD_NAME_CACHE = Maps.newHashMap();
    public static FabricKeyBinding RECIPE, USAGE, HIDE;
    
    static {
        MOD_NAME_CACHE.put("minecraft", "Minecraft");
        MOD_NAME_CACHE.put("c", "Common");
    }
    
    public static String getFormattedModFromItem(Item item) {
        String mod = getModFromItem(item);
        if (mod.equalsIgnoreCase(""))
            return "";
        return "§9§o" + mod;
    }
    
    public static String getModFromItem(Item item) {
        if (item.equals(Items.AIR))
            return "";
        return getModFromIdentifier(Registry.ITEM.getId(item));
    }
    
    public static String getModFromIdentifier(Identifier identifier) {
        if (identifier == null)
            return "";
        Optional<String> any = Optional.ofNullable(MOD_NAME_CACHE.getOrDefault(identifier.getNamespace(), null));
        if (any.isPresent())
            return any.get();
        String modid = identifier.getNamespace();
        String s = FabricLoader.getInstance().getModContainer(modid).map(ModContainer::getMetadata).map(ModMetadata::getName).orElse(modid);
        MOD_NAME_CACHE.put(modid, s);
        return s;
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
        if (ScreenHelper.getLastContainerScreen() instanceof CreativePlayerInventoryScreen) {
            MinecraftClient.getInstance().player.inventory.setCursorStack(ItemStack.EMPTY);
            return;
        }
        ClientSidePacketRegistry.INSTANCE.sendToServer(RoughlyEnoughItemsNetwork.DELETE_ITEMS_PACKET, new PacketByteBuf(Unpooled.buffer()));
    }
    
    public static boolean tryCheatingStack(ItemStack cheatedStack) {
        if (RoughlyEnoughItemsCore.hasPermissionToUsePackets()) {
            try {
                ClientSidePacketRegistry.INSTANCE.sendToServer(RoughlyEnoughItemsNetwork.CREATE_ITEMS_PACKET, new PacketByteBuf(Unpooled.buffer()).writeItemStack(cheatedStack.copy()));
                return true;
            } catch (Exception e) {
                return false;
            }
        } else {
            Identifier identifier = Registry.ITEM.getId(cheatedStack.getItem());
            String tagMessage = cheatedStack.copy().getTag() != null && !cheatedStack.copy().getTag().isEmpty() ? cheatedStack.copy().getTag().asString() : "";
            String og = cheatedStack.getAmount() != 1 ? RoughlyEnoughItemsCore.getConfigManager().getConfig().giveCommand.replaceAll(" \\{count}", "").replaceAll("\\{count}", "") : RoughlyEnoughItemsCore.getConfigManager().getConfig().giveCommand;
            String madeUpCommand = og.replaceAll("\\{player_name}", MinecraftClient.getInstance().player.getEntityName()).replaceAll("\\{item_identifier}", identifier.toString()).replaceAll("\\{nbt}", tagMessage).replaceAll("\\{count}", String.valueOf(cheatedStack.getAmount()));
            if (madeUpCommand.length() > 256) {
                madeUpCommand = og.replaceAll("\\{player_name}", MinecraftClient.getInstance().player.getEntityName()).replaceAll("\\{item_identifier}", identifier.toString()).replaceAll("\\{nbt}", "").replaceAll("\\{count}", String.valueOf(cheatedStack.getAmount()));
                MinecraftClient.getInstance().player.addChatMessage(new TranslatableTextComponent("text.rei.too_long_nbt"), false);
            }
            MinecraftClient.getInstance().player.sendChatMessage(madeUpCommand);
            return true;
        }
    }
    
    public static boolean executeRecipeKeyBind(ItemStack stack) {
        Map<RecipeCategory, List<RecipeDisplay>> map = RecipeHelper.getInstance().getRecipesFor(stack);
        if (map.keySet().size() > 0)
            MinecraftClient.getInstance().openScreen(new RecipeViewingScreen(MinecraftClient.getInstance().window, map));
        return map.keySet().size() > 0;
    }
    
    public static boolean executeUsageKeyBind(ItemStack stack) {
        Map<RecipeCategory, List<RecipeDisplay>> map = RecipeHelper.getInstance().getUsagesFor(stack);
        if (map.keySet().size() > 0)
            MinecraftClient.getInstance().openScreen(new RecipeViewingScreen(MinecraftClient.getInstance().window, map));
        return map.keySet().size() > 0;
    }
    
    public static List<ItemStack> getInventoryItemsTypes() {
        List<DefaultedList<ItemStack>> field_7543 = ImmutableList.of(MinecraftClient.getInstance().player.inventory.main, MinecraftClient.getInstance().player.inventory.armor, MinecraftClient.getInstance().player.inventory.offHand);
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
            MinecraftClient.getInstance().openScreen(new RecipeViewingScreen(MinecraftClient.getInstance().window, map));
        return map.keySet().size() > 0;
    }
    
    @Override
    public void onInitializeClient() {
        registerFabricKeyBinds();
    }
    
    private void registerFabricKeyBinds() {
        String category = "key.rei.category";
        KeyBindingRegistryImpl.INSTANCE.addCategory(category);
        KeyBindingRegistryImpl.INSTANCE.register(RECIPE = FabricKeyBinding.Builder.create(RECIPE_KEYBIND, InputUtil.Type.KEYSYM, 82, category).build());
        KeyBindingRegistryImpl.INSTANCE.register(USAGE = FabricKeyBinding.Builder.create(USAGE_KEYBIND, InputUtil.Type.KEYSYM, 85, category).build());
        KeyBindingRegistryImpl.INSTANCE.register(HIDE = FabricKeyBinding.Builder.create(HIDE_KEYBIND, InputUtil.Type.KEYSYM, 79, category).build());
    }
    
}
