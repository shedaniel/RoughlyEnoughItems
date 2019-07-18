/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.client;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.mojang.realmsclient.gui.ChatFormatting;
import me.shedaniel.rei.RoughlyEnoughItemsClient;
import me.shedaniel.rei.api.*;
import me.shedaniel.rei.gui.PreRecipeViewingScreen;
import me.shedaniel.rei.gui.RecipeViewingScreen;
import me.shedaniel.rei.gui.VillagerRecipeViewingScreen;
import me.shedaniel.rei.gui.config.RecipeScreenType;
import me.shedaniel.rei.network.CreateItemPacket;
import me.shedaniel.rei.network.DeleteItemPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.registry.IRegistry;
import net.minecraft.util.text.TextComponentTranslation;
import org.dimdev.riftloader.RiftLoader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ClientHelperImpl implements ClientHelper {
    
    public static ClientHelperImpl instance;
    
    static {
        ClientHelperImpl.instance = new ClientHelperImpl();
        instance.modNameCache.put("minecraft", "Minecraft");
        instance.modNameCache.put("c", "Common");
    }
    
    private final Map<String, String> modNameCache = Maps.newHashMap();
    
    public String getFormattedModNoItalicFromItem(Item item) {
        String mod = getModFromItem(item);
        if (mod.equalsIgnoreCase(""))
            return "";
        return ChatFormatting.BLUE.toString() + mod;
    }
    
    @Override
    public String getFormattedModFromItem(Item item) {
        String mod = getModFromItem(item);
        if (mod.equalsIgnoreCase(""))
            return "";
        return ChatFormatting.BLUE.toString() + ChatFormatting.ITALIC.toString() + mod;
    }
    
    @Override
    public String getFormattedModFromIdentifier(Identifier identifier) {
        String mod = getModFromIdentifier(identifier);
        if (mod.equalsIgnoreCase(""))
            return "";
        return ChatFormatting.BLUE.toString() + ChatFormatting.ITALIC.toString() + mod;
    }
    
    @Override
    public KeyBinding getRecipeKeyBinding() {
        return KeybindRegistry.recipe;
    }
    
    @Override
    public KeyBinding getUsageKeyBinding() {
        return KeybindRegistry.usage;
    }
    
    @Override
    public KeyBinding getHideKeyBinding() {
        return KeybindRegistry.hide;
    }
    
    @Override
    public KeyBinding getPreviousPageKeyBinding() {
        return KeybindRegistry.previousPage;
    }
    
    @Override
    public KeyBinding getNextPageKeyBinding() {
        return KeybindRegistry.nextPage;
    }
    
    @Override
    public KeyBinding getFocusSearchFieldKeyBinding() {
        return KeybindRegistry.focusSearchField;
    }
    
    @Override
    public String getModFromItem(Item item) {
        if (item.equals(Items.AIR))
            return "";
        return getModFromIdentifier(Identifiers.of(IRegistry.ITEM.getKey(item)));
    }
    
    @Override
    public String getModFromIdentifier(Identifier identifier) {
        if (identifier == null)
            return "";
        Optional<String> any = Optional.ofNullable(modNameCache.getOrDefault(identifier.getNamespace(), null));
        if (any.isPresent())
            return any.get();
        String modid = identifier.getNamespace();
        String s = RiftLoader.instance.getMods().stream().filter(modInfo -> modInfo.id.equalsIgnoreCase(modid)).findFirst().map(modInfo -> {
            if (modInfo.name == null)
                return modInfo.id;
            return modInfo.name;
        }).orElse(modid);
        modNameCache.put(modid, s);
        return s;
    }
    
    @Override
    public boolean isCheating() {
        return RoughlyEnoughItemsClient.getConfigManager().getConfig().cheating;
    }
    
    @Override
    public void setCheating(boolean cheating) {
        RoughlyEnoughItemsClient.getConfigManager().getConfig().cheating = cheating;
        try {
            RoughlyEnoughItemsClient.getConfigManager().saveConfig();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public void sendDeletePacket() {
        if (ScreenHelper.getLastContainerScreen() instanceof GuiContainerCreative) {
            Minecraft.getInstance().player.inventory.setItemStack(ItemStack.EMPTY);
            return;
        }
        if (Minecraft.getInstance().isSingleplayer()) {
            Minecraft.getInstance().getConnection().sendPacket(new DeleteItemPacket());
        }
    }
    
    @Override
    public boolean tryCheatingStack(ItemStack cheatedStack) {
        if (Minecraft.getInstance().isSingleplayer()) {
            try {
                Minecraft.getInstance().getConnection().sendPacket(new CreateItemPacket(cheatedStack.copy()));
                return true;
            } catch (Exception e) {
                return false;
            }
        } else {
            Identifier identifier = Identifiers.of(IRegistry.ITEM.getKey(cheatedStack.getItem()));
            String tagMessage = cheatedStack.copy().getTag() != null && !cheatedStack.copy().getTag().isEmpty() ? cheatedStack.copy().getTag().toString() : "";
            String og = cheatedStack.getCount() == 1 ? RoughlyEnoughItemsClient.getConfigManager().getConfig().giveCommand.replaceAll(" \\{count}", "") : RoughlyEnoughItemsClient.getConfigManager().getConfig().giveCommand;
            String madeUpCommand = og.replaceAll("\\{player_name}", Minecraft.getInstance().player.getScoreboardName()).replaceAll("\\{item_name}", identifier.getPath()).replaceAll("\\{item_identifier}", identifier.toString()).replaceAll("\\{nbt}", tagMessage).replaceAll("\\{count}", String.valueOf(cheatedStack.getCount()));
            if (madeUpCommand.length() > 256) {
                madeUpCommand = og.replaceAll("\\{player_name}", Minecraft.getInstance().player.getScoreboardName()).replaceAll("\\{item_name}", identifier.getPath()).replaceAll("\\{item_identifier}", identifier.toString()).replaceAll("\\{nbt}", "").replaceAll("\\{count}", String.valueOf(cheatedStack.getCount()));
                Minecraft.getInstance().player.sendStatusMessage(new TextComponentTranslation("text.rei.too_long_nbt"), false);
            }
            Minecraft.getInstance().player.sendChatMessage(madeUpCommand);
            return true;
        }
    }
    
    @Override
    public boolean executeRecipeKeyBind(ItemStack stack) {
        Map<RecipeCategory<?>, List<RecipeDisplay>> map = RecipeHelper.getInstance().getRecipesFor(stack);
        if (map.keySet().size() > 0)
            openRecipeViewingScreen(map);
        return map.keySet().size() > 0;
    }
    
    @Override
    public boolean executeUsageKeyBind(ItemStack stack) {
        Map<RecipeCategory<?>, List<RecipeDisplay>> map = RecipeHelper.getInstance().getUsagesFor(stack);
        if (map.keySet().size() > 0)
            openRecipeViewingScreen(map);
        return map.keySet().size() > 0;
    }
    
    @Override
    public List<ItemStack> getInventoryItemsTypes() {
        List<NonNullList<ItemStack>> field_7543 = ImmutableList.of(Minecraft.getInstance().player.inventory.mainInventory, Minecraft.getInstance().player.inventory.armorInventory, Minecraft.getInstance().player.inventory.offHandInventory);
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
        for(Identifier category : categories) {
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
        if (RoughlyEnoughItemsClient.getConfigManager().getConfig().screenType == RecipeScreenType.VILLAGER)
            Minecraft.getInstance().displayGuiScreen(new VillagerRecipeViewingScreen(map));
        else if (RoughlyEnoughItemsClient.getConfigManager().getConfig().screenType == RecipeScreenType.UNSET)
            Minecraft.getInstance().displayGuiScreen(new PreRecipeViewingScreen(map));
        else
            Minecraft.getInstance().displayGuiScreen(new RecipeViewingScreen(map));
    }
    
}
