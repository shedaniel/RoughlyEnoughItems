/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import io.netty.buffer.Unpooled;
import me.shedaniel.rei.RoughlyEnoughItemsCore;
import me.shedaniel.rei.RoughlyEnoughItemsNetwork;
import me.shedaniel.rei.api.*;
import me.shedaniel.rei.gui.PreRecipeViewingScreen;
import me.shedaniel.rei.gui.RecipeViewingScreen;
import me.shedaniel.rei.gui.VillagerRecipeViewingScreen;
import me.shedaniel.rei.gui.config.RecipeScreenType;
import me.zeroeightsix.fiber.exception.FiberException;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.DefaultedList;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.util.registry.Registry;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ClientHelperImpl implements ClientHelper, ClientModInitializer {
    
    public static ClientHelperImpl instance;
    private final Identifier recipeKeybind = new Identifier("roughlyenoughitems", "recipe_keybind");
    private final Identifier usageKeybind = new Identifier("roughlyenoughitems", "usage_keybind");
    private final Identifier hideKeybind = new Identifier("roughlyenoughitems", "hide_keybind");
    private final Identifier previousPageKeybind = new Identifier("roughlyenoughitems", "previous_page");
    private final Identifier nextPageKeybind = new Identifier("roughlyenoughitems", "next_page");
    private final Identifier focusSearchFieldKeybind = new Identifier("roughlyenoughitems", "focus_search");
    private final Identifier copyRecipeIdentifierKeybind = new Identifier("roughlyenoughitems", "copy_recipe_id");
    private final Map<String, String> modNameCache = Maps.newHashMap();
    public KeyBinding recipe, usage, hide, previousPage, nextPage, focusSearchField, copyRecipeIdentifier;
    
    @Override
    public String getFormattedModFromItem(Item item) {
        String mod = getModFromItem(item);
        if (mod.isEmpty())
            return "";
        return Formatting.BLUE.toString() + Formatting.ITALIC.toString() + mod;
    }
    
    @Override
    public String getFormattedModFromIdentifier(Identifier identifier) {
        String mod = getModFromIdentifier(identifier);
        if (mod.isEmpty())
            return "";
        return Formatting.BLUE.toString() + Formatting.ITALIC.toString() + mod;
    }
    
    @Override
    public KeyBinding getRecipeKeyBinding() {
        return recipe;
    }
    
    @Override
    public KeyBinding getUsageKeyBinding() {
        return usage;
    }
    
    @Override
    public KeyBinding getHideKeyBinding() {
        return hide;
    }
    
    @Override
    public KeyBinding getPreviousPageKeyBinding() {
        return previousPage;
    }
    
    @Override
    public KeyBinding getNextPageKeyBinding() {
        return nextPage;
    }
    
    @Override
    public KeyBinding getFocusSearchFieldKeyBinding() {
        return focusSearchField;
    }
    
    @Override
    public KeyBinding getCopyRecipeIdentifierKeyBinding() {
        return copyRecipeIdentifier;
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
        return RoughlyEnoughItemsCore.getConfigManager().getConfig().isCheating();
    }
    
    @Override
    public void setCheating(boolean cheating) {
        RoughlyEnoughItemsCore.getConfigManager().getConfig().setCheating(cheating);
        try {
            RoughlyEnoughItemsCore.getConfigManager().saveConfig();
        } catch (IOException | FiberException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public void sendDeletePacket() {
        if (ScreenHelper.getLastContainerScreen() instanceof CreativeInventoryScreen) {
            MinecraftClient.getInstance().player.inventory.setCursorStack(ItemStack.EMPTY);
            return;
        }
        ClientSidePacketRegistry.INSTANCE.sendToServer(RoughlyEnoughItemsNetwork.DELETE_ITEMS_PACKET, new PacketByteBuf(Unpooled.buffer()));
    }
    
    @Override
    public boolean tryCheatingEntry(EntryStack entry) {
        if (entry.getType() != EntryStack.Type.ITEM)
            return false;
        ItemStack cheatedStack = entry.getItemStack().copy();
        if (RoughlyEnoughItemsCore.canUsePackets()) {
            try {
                ClientSidePacketRegistry.INSTANCE.sendToServer(RoughlyEnoughItemsNetwork.CREATE_ITEMS_PACKET, new PacketByteBuf(Unpooled.buffer()).writeItemStack(cheatedStack));
                return true;
            } catch (Exception e) {
                return false;
            }
        } else {
            Identifier identifier = entry.getIdentifier().orElse(null);
            if (identifier == null)
                return false;
            String tagMessage = cheatedStack.copy().getTag() != null && !cheatedStack.copy().getTag().isEmpty() ? cheatedStack.copy().getTag().asString() : "";
            String og = cheatedStack.getCount() == 1 ? RoughlyEnoughItemsCore.getConfigManager().getConfig().getGiveCommand().replaceAll(" \\{count}", "") : RoughlyEnoughItemsCore.getConfigManager().getConfig().getGiveCommand();
            String madeUpCommand = og.replaceAll("\\{player_name}", MinecraftClient.getInstance().player.getEntityName()).replaceAll("\\{item_name}", identifier.getPath()).replaceAll("\\{item_identifier}", identifier.toString()).replaceAll("\\{nbt}", tagMessage).replaceAll("\\{count}", String.valueOf(cheatedStack.getCount()));
            if (madeUpCommand.length() > 256) {
                madeUpCommand = og.replaceAll("\\{player_name}", MinecraftClient.getInstance().player.getEntityName()).replaceAll("\\{item_name}", identifier.getPath()).replaceAll("\\{item_identifier}", identifier.toString()).replaceAll("\\{nbt}", "").replaceAll("\\{count}", String.valueOf(cheatedStack.getCount()));
                MinecraftClient.getInstance().player.addChatMessage(new TranslatableText("text.rei.too_long_nbt"), false);
            }
            MinecraftClient.getInstance().player.sendChatMessage(madeUpCommand);
            return true;
        }
    }
    
    @Override
    public boolean executeRecipeKeyBind(EntryStack stack) {
        Map<RecipeCategory<?>, List<RecipeDisplay>> map = RecipeHelper.getInstance().getRecipesFor(stack);
        if (map.keySet().size() > 0)
            openRecipeViewingScreen(map);
        return map.keySet().size() > 0;
    }
    
    @Override
    public boolean executeUsageKeyBind(EntryStack stack) {
        Map<RecipeCategory<?>, List<RecipeDisplay>> map = RecipeHelper.getInstance().getUsagesFor(stack);
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
        Map<RecipeCategory<?>, List<RecipeDisplay>> map = RecipeHelper.getInstance().getAllRecipes();
        if (map.keySet().size() > 0)
            openRecipeViewingScreen(map);
        return map.keySet().size() > 0;
    }
    
    @Override
    public boolean executeViewAllRecipesFromCategory(Identifier category) {
        Map<RecipeCategory<?>, List<RecipeDisplay>> map = Maps.newLinkedHashMap();
        Optional<RecipeCategory> any = RecipeHelper.getInstance().getAllCategories().stream().filter(c -> c.getIdentifier().equals(category)).findAny();
        if (!any.isPresent())
            return false;
        RecipeCategory<?> recipeCategory = any.get();
        map.put(recipeCategory, RecipeHelper.getInstance().getAllRecipesFromCategory(recipeCategory));
        if (map.keySet().size() > 0)
            openRecipeViewingScreen(map);
        return map.keySet().size() > 0;
    }
    
    @Override
    public boolean executeViewAllRecipesFromCategories(List<Identifier> categories) {
        Map<RecipeCategory<?>, List<RecipeDisplay>> map = Maps.newLinkedHashMap();
        for (Identifier category : categories) {
            Optional<RecipeCategory> any = RecipeHelper.getInstance().getAllCategories().stream().filter(c -> c.getIdentifier().equals(category)).findAny();
            if (!any.isPresent())
                continue;
            RecipeCategory<?> recipeCategory = any.get();
            map.put(recipeCategory, RecipeHelper.getInstance().getAllRecipesFromCategory(recipeCategory));
        }
        if (map.keySet().size() > 0)
            openRecipeViewingScreen(map);
        return map.keySet().size() > 0;
    }
    
    @Override
    public void openRecipeViewingScreen(Map<RecipeCategory<?>, List<RecipeDisplay>> map) {
        Screen screen = null;
        if (RoughlyEnoughItemsCore.getConfigManager().getConfig().getRecipeScreenType() == RecipeScreenType.VILLAGER)
            screen = new VillagerRecipeViewingScreen(map);
        else if (RoughlyEnoughItemsCore.getConfigManager().getConfig().getRecipeScreenType() == RecipeScreenType.UNSET)
            screen = new PreRecipeViewingScreen(map);
        else
            screen = new RecipeViewingScreen(map);
        ScreenHelper.storeRecipeScreen(MinecraftClient.getInstance().currentScreen);
        MinecraftClient.getInstance().openScreen(screen);
    }
    
    @Override
    public void onInitializeClient() {
        ClientHelperImpl.instance = this;
        registerFabricKeyBinds();
        modNameCache.put("minecraft", "Minecraft");
        modNameCache.put("c", "Common");
    }
    
    @Override
    public void registerFabricKeyBinds() {
        String category = "key.rei.category";
        recipe = registerKeyBinding(recipeKeybind, InputUtil.Type.KEYSYM, 82, category);
        usage = registerKeyBinding(usageKeybind, InputUtil.Type.KEYSYM, 85, category);
        hide = registerKeyBinding(hideKeybind, InputUtil.Type.KEYSYM, 79, category);
        previousPage = registerKeyBinding(previousPageKeybind, InputUtil.Type.KEYSYM, -1, category);
        nextPage = registerKeyBinding(nextPageKeybind, InputUtil.Type.KEYSYM, -1, category);
        focusSearchField = registerKeyBinding(focusSearchFieldKeybind, InputUtil.Type.KEYSYM, -1, category);
        copyRecipeIdentifier = registerKeyBinding(copyRecipeIdentifierKeybind, InputUtil.Type.KEYSYM, -1, category);
    }
    
    private KeyBinding registerKeyBinding(Identifier id, InputUtil.Type type, int code, String category) {
        return KeyBindingHelper.registerKeyBinding(new KeyBinding("key." + id.getNamespace() + "." + id.getPath(), type, code, category));
    }
    
}
