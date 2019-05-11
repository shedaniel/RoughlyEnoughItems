/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.client;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import io.netty.buffer.Unpooled;
import me.shedaniel.rei.RoughlyEnoughItemsCore;
import me.shedaniel.rei.RoughlyEnoughItemsNetwork;
import me.shedaniel.rei.api.ClientHelper;
import me.shedaniel.rei.api.RecipeCategory;
import me.shedaniel.rei.api.RecipeDisplay;
import me.shedaniel.rei.api.RecipeHelper;
import me.shedaniel.rei.gui.PreRecipeViewingScreen;
import me.shedaniel.rei.gui.RecipeViewingScreen;
import me.shedaniel.rei.gui.VillagerRecipeViewingScreen;
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

public class ClientHelperImpl implements ClientHelper {
    
    public static ClientHelperImpl instance;
    private final Identifier recipeKeybind = new Identifier("roughlyenoughitems", "recipe_keybind");
    private final Identifier usageKeybind = new Identifier("roughlyenoughitems", "usage_keybind");
    private final Identifier hideKeybind = new Identifier("roughlyenoughitems", "hide_keybind");
    private final Identifier previousPageKeybind = new Identifier("roughlyenoughitems", "previous_page");
    private final Identifier nextPageKeybind = new Identifier("roughlyenoughitems", "next_page");
    private final Map<String, String> modNameCache = Maps.newHashMap();
    public FabricKeyBinding recipe, usage, hide, previousPage, nextPage;
    
    @Override
    public String getFormattedModFromItem(Item item) {
        String mod = getModFromItem(item);
        if (mod.equalsIgnoreCase(""))
            return "";
        return "§9§o" + mod;
    }
    
    @Override
    public String getFormattedModFromIdentifier(Identifier identifier) {
        String mod = getModFromIdentifier(identifier);
        if (mod.equalsIgnoreCase(""))
            return "";
        return "§9§o" + mod;
    }
    
    @Override
    public FabricKeyBinding getRecipeKeyBinding() {
        return recipe;
    }
    
    @Override
    public FabricKeyBinding getUsageKeyBinding() {
        return usage;
    }
    
    @Override
    public FabricKeyBinding getHideKeyBinding() {
        return hide;
    }
    
    @Override
    public FabricKeyBinding getPreviousPageKeyBinding() {
        return previousPage;
    }
    
    @Override
    public FabricKeyBinding getNextPageKeyBinding() {
        return nextPage;
    }
    
    @Override
    public String getModFromItem(Item item) {
        if (item.equals(Items.AIR))
            return "";
        return getModFromIdentifier(Registry.ITEM.getId(item));
    }
    
    @Override
    public String getModFromIdentifier(Identifier identifier) {
        if (identifier == null)
            return "";
        Optional<String> any = Optional.ofNullable(modNameCache.getOrDefault(identifier.getNamespace(), null));
        if (any.isPresent())
            return any.get();
        String modid = identifier.getNamespace();
        String s = FabricLoader.getInstance().getModContainer(modid).map(ModContainer::getMetadata).map(ModMetadata::getName).orElse(modid);
        modNameCache.put(modid, s);
        return s;
    }
    
    @Override
    public boolean isCheating() {
        return RoughlyEnoughItemsCore.getConfigManager().getConfig().cheating;
    }
    
    @Override
    public void setCheating(boolean cheating) {
        RoughlyEnoughItemsCore.getConfigManager().getConfig().cheating = cheating;
        try {
            RoughlyEnoughItemsCore.getConfigManager().saveConfig();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public void sendDeletePacket() {
        if (ScreenHelper.getLastContainerScreen() instanceof CreativePlayerInventoryScreen) {
            MinecraftClient.getInstance().player.inventory.setCursorStack(ItemStack.EMPTY);
            return;
        }
        ClientSidePacketRegistry.INSTANCE.sendToServer(RoughlyEnoughItemsNetwork.DELETE_ITEMS_PACKET, new PacketByteBuf(Unpooled.buffer()));
    }
    
    @Override
    public boolean tryCheatingStack(ItemStack cheatedStack) {
        if (RoughlyEnoughItemsCore.canUsePackets()) {
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
                MinecraftClient.getInstance().player.addChatMessage(new TranslatableTextComponent("text.rei" + ".too_long_nbt"), false);
            }
            MinecraftClient.getInstance().player.sendChatMessage(madeUpCommand);
            return true;
        }
    }
    
    @Override
    public boolean executeRecipeKeyBind(ItemStack stack) {
        Map<RecipeCategory, List<RecipeDisplay>> map = RecipeHelper.getInstance().getRecipesFor(stack);
        if (map.keySet().size() > 0)
            openRecipeViewingScreen(map);
        return map.keySet().size() > 0;
    }
    
    @Override
    public boolean executeUsageKeyBind(ItemStack stack) {
        Map<RecipeCategory, List<RecipeDisplay>> map = RecipeHelper.getInstance().getUsagesFor(stack);
        if (map.keySet().size() > 0)
            openRecipeViewingScreen(map);
        return map.keySet().size() > 0;
    }
    
    @Override
    public List<ItemStack> getInventoryItemsTypes() {
        List<DefaultedList<ItemStack>> field_7543 = ImmutableList.of(MinecraftClient.getInstance().player.inventory.main, MinecraftClient.getInstance().player.inventory.armor, MinecraftClient.getInstance().player.inventory.offHand);
        List<ItemStack> inventoryStacks = new ArrayList<>();
        field_7543.forEach(itemStacks -> itemStacks.forEach(itemStack -> {
            if (!itemStack.isEmpty())
                inventoryStacks.add(itemStack);
        }));
        return inventoryStacks;
    }
    
    @Override
    public boolean executeViewAllRecipesKeyBind() {
        Map<RecipeCategory, List<RecipeDisplay>> map = RecipeHelper.getInstance().getAllRecipes();
        if (map.keySet().size() > 0)
            openRecipeViewingScreen(map);
        return map.keySet().size() > 0;
    }
    
    @Override
    public void openRecipeViewingScreen(Map<RecipeCategory, List<RecipeDisplay>> map) {
        if (RoughlyEnoughItemsCore.getConfigManager().getConfig().screenType == RecipeScreenType.VILLAGER)
            MinecraftClient.getInstance().openScreen(new VillagerRecipeViewingScreen(map));
        else if (RoughlyEnoughItemsCore.getConfigManager().getConfig().screenType == RecipeScreenType.UNSET)
            MinecraftClient.getInstance().openScreen(new PreRecipeViewingScreen(map));
        else
            MinecraftClient.getInstance().openScreen(new RecipeViewingScreen(map));
    }
    
    @Override
    public void onInitializeClient() {
        ClientHelperImpl.instance = (ClientHelperImpl) this;
        registerFabricKeyBinds();
        modNameCache.put("minecraft", "Minecraft");
        modNameCache.put("c", "Common");
    }
    
    @Override
    public void registerFabricKeyBinds() {
        String category = "key.rei.category";
        KeyBindingRegistryImpl.INSTANCE.addCategory(category);
        KeyBindingRegistryImpl.INSTANCE.register(recipe = FabricKeyBinding.Builder.create(recipeKeybind, InputUtil.Type.KEYSYM, 82, category).build());
        KeyBindingRegistryImpl.INSTANCE.register(usage = FabricKeyBinding.Builder.create(usageKeybind, InputUtil.Type.KEYSYM, 85, category).build());
        KeyBindingRegistryImpl.INSTANCE.register(hide = FabricKeyBinding.Builder.create(hideKeybind, InputUtil.Type.KEYSYM, 79, category).build());
        KeyBindingRegistryImpl.INSTANCE.register(previousPage = FabricKeyBinding.Builder.create(previousPageKeybind, InputUtil.Type.KEYSYM, -1, category).build());
        KeyBindingRegistryImpl.INSTANCE.register(nextPage = FabricKeyBinding.Builder.create(nextPageKeybind, InputUtil.Type.KEYSYM, -1, category).build());
    }
    
}
