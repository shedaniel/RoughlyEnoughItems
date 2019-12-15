/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.plugin;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import me.shedaniel.math.api.Rectangle;
import me.shedaniel.rei.RoughlyEnoughItemsCore;
import me.shedaniel.rei.api.*;
import me.shedaniel.rei.api.plugins.REIPluginV0;
import me.shedaniel.rei.gui.RecipeViewingScreen;
import me.shedaniel.rei.gui.VillagerRecipeViewingScreen;
import me.shedaniel.rei.gui.widget.CategoryBaseWidget;
import me.shedaniel.rei.impl.ScreenHelper;
import me.shedaniel.rei.plugin.blasting.DefaultBlastingDisplay;
import me.shedaniel.rei.plugin.brewing.DefaultBrewingCategory;
import me.shedaniel.rei.plugin.brewing.DefaultBrewingDisplay;
import me.shedaniel.rei.plugin.campfire.DefaultCampfireCategory;
import me.shedaniel.rei.plugin.campfire.DefaultCampfireDisplay;
import me.shedaniel.rei.plugin.composting.DefaultCompostingCategory;
import me.shedaniel.rei.plugin.composting.DefaultCompostingDisplay;
import me.shedaniel.rei.plugin.cooking.DefaultCookingCategory;
import me.shedaniel.rei.plugin.crafting.DefaultCraftingCategory;
import me.shedaniel.rei.plugin.crafting.DefaultCustomDisplay;
import me.shedaniel.rei.plugin.crafting.DefaultShapedDisplay;
import me.shedaniel.rei.plugin.crafting.DefaultShapelessDisplay;
import me.shedaniel.rei.plugin.smelting.DefaultSmeltingDisplay;
import me.shedaniel.rei.plugin.smoking.DefaultSmokingDisplay;
import me.shedaniel.rei.plugin.stonecutting.DefaultStoneCuttingCategory;
import me.shedaniel.rei.plugin.stonecutting.DefaultStoneCuttingDisplay;
import me.shedaniel.rei.plugin.stripping.DefaultStrippingCategory;
import me.shedaniel.rei.plugin.stripping.DefaultStrippingDisplay;
import me.shedaniel.rei.plugin.stripping.DummyAxeItem;
import net.fabricmc.loader.api.SemanticVersion;
import net.fabricmc.loader.util.version.VersionParsingException;
import net.minecraft.block.ComposterBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.*;
import net.minecraft.client.gui.screen.recipebook.RecipeBookProvider;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.fluid.EmptyFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.*;
import net.minecraft.potion.PotionUtil;
import net.minecraft.recipe.*;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.Registry;

import java.util.*;

public class DefaultPlugin implements REIPluginV0 {
    
    public static final Identifier CRAFTING = new Identifier("minecraft", "plugins/crafting");
    public static final Identifier SMELTING = new Identifier("minecraft", "plugins/smelting");
    public static final Identifier SMOKING = new Identifier("minecraft", "plugins/smoking");
    public static final Identifier BLASTING = new Identifier("minecraft", "plugins/blasting");
    public static final Identifier CAMPFIRE = new Identifier("minecraft", "plugins/campfire");
    public static final Identifier STONE_CUTTING = new Identifier("minecraft", "plugins/stone_cutting");
    public static final Identifier STRIPPING = new Identifier("minecraft", "plugins/stripping");
    public static final Identifier BREWING = new Identifier("minecraft", "plugins/brewing");
    public static final Identifier PLUGIN = new Identifier("roughlyenoughitems", "default_plugin");
    public static final Identifier COMPOSTING = new Identifier("minecraft", "plugins/composting");
    private static final Identifier DISPLAY_TEXTURE = new Identifier("roughlyenoughitems", "textures/gui/display.png");
    private static final Identifier DISPLAY_TEXTURE_DARK = new Identifier("roughlyenoughitems", "textures/gui/display_dark.png");
    private static final List<DefaultBrewingDisplay> BREWING_DISPLAYS = Lists.newArrayList();
    
    public static Identifier getDisplayTexture() {
        return ScreenHelper.isDarkModeEnabled() ? DISPLAY_TEXTURE_DARK : DISPLAY_TEXTURE;
    }
    
    public static void registerBrewingDisplay(DefaultBrewingDisplay display) {
        BREWING_DISPLAYS.add(display);
    }
    
    @Override
    public Identifier getPluginIdentifier() {
        return PLUGIN;
    }
    
    @Override
    public SemanticVersion getMinimumVersion() throws VersionParsingException {
        return SemanticVersion.parse("3.0-pre");
    }
    
    @Override
    public void registerEntries(EntryRegistry entryRegistry) {
        if (!ConfigObject.getInstance().isLoadingDefaultPlugin()) {
            return;
        }
        for (Item item : Registry.ITEM) {
            ItemStack[] stacks = null;
            try {
                stacks = entryRegistry.getAllStacksFromItem(item);
                for (ItemStack stack : entryRegistry.getAllStacksFromItem(item)) {
                    entryRegistry.registerEntry(EntryStack.create(stack));
                }
            } catch (Exception ignored) {
            }
            if (stacks != null) {
                for (ItemStack stack : entryRegistry.getAllStacksFromItem(item)) {
                    entryRegistry.registerEntry(EntryStack.create(stack));
                }
            } else entryRegistry.registerEntry(EntryStack.create(item));
        }
        EntryStack stack = EntryStack.create(Items.ENCHANTED_BOOK);
        for (Enchantment enchantment : Registry.ENCHANTMENT) {
            for (int i = enchantment.getMinimumLevel(); i <= enchantment.getMaximumLevel(); i++) {
                Map<Enchantment, Integer> map = new HashMap<>();
                map.put(enchantment, i);
                ItemStack itemStack = new ItemStack(Items.ENCHANTED_BOOK);
                EnchantmentHelper.set(map, itemStack);
                entryRegistry.registerEntriesAfter(stack, EntryStack.create(itemStack));
            }
        }
        for (Fluid fluid : Registry.FLUID) {
            if (!(fluid instanceof EmptyFluid))
                entryRegistry.registerEntry(EntryStack.create(fluid));
        }
    }
    
    @Override
    public void registerPluginCategories(RecipeHelper recipeHelper) {
        if (!ConfigObject.getInstance().isLoadingDefaultPlugin()) {
            return;
        }
        recipeHelper.registerCategory(new DefaultCraftingCategory());
        recipeHelper.registerCategory(new DefaultCookingCategory(SMELTING, EntryStack.create(Items.FURNACE), "category.rei.smelting"));
        recipeHelper.registerCategory(new DefaultCookingCategory(SMOKING, EntryStack.create(Items.SMOKER), "category.rei.smoking"));
        recipeHelper.registerCategory(new DefaultCookingCategory(BLASTING, EntryStack.create(Items.BLAST_FURNACE), "category.rei.blasting"));
        recipeHelper.registerCategory(new DefaultCampfireCategory());
        recipeHelper.registerCategory(new DefaultStoneCuttingCategory());
        recipeHelper.registerCategory(new DefaultBrewingCategory());
        recipeHelper.registerCategory(new DefaultCompostingCategory());
        recipeHelper.registerCategory(new DefaultStrippingCategory());
    }
    
    @Override
    public void registerRecipeDisplays(RecipeHelper recipeHelper) {
        if (!ConfigObject.getInstance().isLoadingDefaultPlugin()) {
            return;
        }
        recipeHelper.registerRecipes(CRAFTING, ShapelessRecipe.class, DefaultShapelessDisplay::new);
        recipeHelper.registerRecipes(CRAFTING, ShapedRecipe.class, DefaultShapedDisplay::new);
        recipeHelper.registerRecipes(SMELTING, SmeltingRecipe.class, DefaultSmeltingDisplay::new);
        recipeHelper.registerRecipes(SMOKING, SmokingRecipe.class, DefaultSmokingDisplay::new);
        recipeHelper.registerRecipes(BLASTING, BlastingRecipe.class, DefaultBlastingDisplay::new);
        recipeHelper.registerRecipes(CAMPFIRE, CampfireCookingRecipe.class, DefaultCampfireDisplay::new);
        recipeHelper.registerRecipes(STONE_CUTTING, StonecuttingRecipe.class, DefaultStoneCuttingDisplay::new);
        for (DefaultBrewingDisplay display : BREWING_DISPLAYS) {
            recipeHelper.registerDisplay(BREWING, display);
        }
        List<EntryStack> arrowStack = Collections.singletonList(EntryStack.create(Items.ARROW));
        for (EntryStack entry : EntryRegistry.getInstance().getStacksList()) {
            if (entry.getItem() == Items.LINGERING_POTION) {
                List<List<EntryStack>> input = new ArrayList<>();
                for (int i = 0; i < 4; i++)
                    input.add(arrowStack);
                input.add(Collections.singletonList(EntryStack.create(entry.getItemStack())));
                for (int i = 0; i < 4; i++)
                    input.add(arrowStack);
                ItemStack outputStack = new ItemStack(Items.TIPPED_ARROW, 8);
                PotionUtil.setPotion(outputStack, PotionUtil.getPotion(entry.getItemStack()));
                PotionUtil.setCustomPotionEffects(outputStack, PotionUtil.getCustomPotionEffects(entry.getItemStack()));
                List<EntryStack> output = Collections.singletonList(EntryStack.create(outputStack).addSetting(EntryStack.Settings.CHECK_TAGS, EntryStack.Settings.TRUE));
                recipeHelper.registerDisplay(CRAFTING, new DefaultCustomDisplay(null, input, output));
            }
        }
        Map<ItemConvertible, Float> map = Maps.newLinkedHashMap();
        if (ComposterBlock.ITEM_TO_LEVEL_INCREASE_CHANCE.isEmpty())
            ComposterBlock.registerDefaultCompostableItems();
        for (Object2FloatMap.Entry<ItemConvertible> entry : ComposterBlock.ITEM_TO_LEVEL_INCREASE_CHANCE.object2FloatEntrySet()) {
            if (entry.getFloatValue() > 0)
                map.put(entry.getKey(), entry.getFloatValue());
        }
        List<ItemConvertible> stacks = new LinkedList<>(map.keySet());
        stacks.sort((first, second) -> {
            return (int) ((map.get(first) - map.get(second)) * 100);
        });
        for (int i = 0; i < stacks.size(); i += MathHelper.clamp(48, 1, stacks.size() - i)) {
            List<ItemConvertible> thisStacks = Lists.newArrayList();
            for (int j = i; j < i + 48; j++)
                if (j < stacks.size())
                    thisStacks.add(stacks.get(j));
            recipeHelper.registerDisplay(COMPOSTING, new DefaultCompostingDisplay(MathHelper.floor(i / 48f), thisStacks, map, Lists.newArrayList(map.keySet()), new ItemStack[]{new ItemStack(Items.BONE_MEAL)}));
        }
        DummyAxeItem.getStrippedBlocksMap().entrySet().stream().sorted(Comparator.comparing(b -> Registry.BLOCK.getId(b.getKey()))).forEach(set -> {
            recipeHelper.registerDisplay(STRIPPING, new DefaultStrippingDisplay(new ItemStack(set.getKey()), new ItemStack(set.getValue())));
        });
    }
    
    @Override
    public void postRegister() {
        // Sit tight! This will be a fast journey!
        long time = System.currentTimeMillis();
        for (EntryStack stack : EntryRegistry.getInstance().getStacksList())
            applyPotionTransformer(stack);
        for (List<RecipeDisplay> displays : RecipeHelper.getInstance().getAllRecipes().values()) {
            for (RecipeDisplay display : displays) {
                for (List<EntryStack> entries : display.getInputEntries())
                    for (EntryStack stack : entries)
                        applyPotionTransformer(stack);
                for (EntryStack stack : display.getOutputEntries())
                    applyPotionTransformer(stack);
            }
        }
        time = System.currentTimeMillis() - time;
        RoughlyEnoughItemsCore.LOGGER.info("[REI] Applied Check Tags for potion in %dms.", time);
    }
    
    private void applyPotionTransformer(EntryStack stack) {
        if (stack.getItem() instanceof PotionItem)
            stack.addSetting(EntryStack.Settings.CHECK_TAGS, EntryStack.Settings.TRUE);
    }
    
    @Override
    public void registerBounds(DisplayHelper displayHelper) {
        if (!ConfigObject.getInstance().isLoadingDefaultPlugin()) {
            return;
        }
        displayHelper.getBaseBoundsHandler().registerExclusionZones(AbstractInventoryScreen.class, new DefaultPotionEffectExclusionZones());
        displayHelper.getBaseBoundsHandler().registerExclusionZones(RecipeBookProvider.class, new DefaultRecipeBookExclusionZones());
        displayHelper.getBaseBoundsHandler().registerExclusionZones(RecipeViewingScreen.class, isLeftSide -> {
            CategoryBaseWidget widget = ((RecipeViewingScreen) MinecraftClient.getInstance().currentScreen).getWorkingStationsBaseWidget();
            if (widget == null) return Collections.emptyList();
            return Collections.singletonList(widget.getBounds().clone());
        });
        displayHelper.registerBoundsHandler(new DisplayHelper.DisplayBoundsHandler<AbstractContainerScreen<?>>() {
            @Override
            public Class<?> getBaseSupportedClass() {
                return AbstractContainerScreen.class;
            }
            
            @Override
            public Rectangle getLeftBounds(AbstractContainerScreen<?> screen) {
                return new Rectangle(2, 0, ScreenHelper.getLastContainerScreenHooks().rei_getContainerLeft() - 4, MinecraftClient.getInstance().getWindow().getScaledHeight());
            }
            
            @Override
            public Rectangle getRightBounds(AbstractContainerScreen<?> screen) {
                int startX = ScreenHelper.getLastContainerScreenHooks().rei_getContainerLeft() + ScreenHelper.getLastContainerScreenHooks().rei_getContainerWidth() + 2;
                return new Rectangle(startX, 0, MinecraftClient.getInstance().getWindow().getScaledWidth() - startX - 2, MinecraftClient.getInstance().getWindow().getScaledHeight());
            }
            
            @Override
            public float getPriority() {
                return -1.0f;
            }
        });
        displayHelper.registerBoundsHandler(new DisplayHelper.DisplayBoundsHandler<RecipeViewingScreen>() {
            @Override
            public Class<?> getBaseSupportedClass() {
                return RecipeViewingScreen.class;
            }
            
            @Override
            public Rectangle getLeftBounds(RecipeViewingScreen screen) {
                return new Rectangle(2, 0, ((RecipeViewingScreen) screen).getBounds().x - 4, MinecraftClient.getInstance().getWindow().getScaledHeight());
            }
            
            @Override
            public Rectangle getRightBounds(RecipeViewingScreen screen) {
                int startX = ((RecipeViewingScreen) screen).getBounds().x + ((RecipeViewingScreen) screen).getBounds().width + 2;
                return new Rectangle(startX, 0, MinecraftClient.getInstance().getWindow().getScaledWidth() - startX - 2, MinecraftClient.getInstance().getWindow().getScaledHeight());
            }
            
            @Override
            public float getPriority() {
                return -1.0f;
            }
        });
        displayHelper.registerBoundsHandler(new DisplayHelper.DisplayBoundsHandler<VillagerRecipeViewingScreen>() {
            @Override
            public Class<?> getBaseSupportedClass() {
                return VillagerRecipeViewingScreen.class;
            }
            
            @Override
            public Rectangle getLeftBounds(VillagerRecipeViewingScreen screen) {
                return new Rectangle(2, 0, ((VillagerRecipeViewingScreen) screen).bounds.x - 4, MinecraftClient.getInstance().getWindow().getScaledHeight());
            }
            
            @Override
            public Rectangle getRightBounds(VillagerRecipeViewingScreen screen) {
                int startX = ((VillagerRecipeViewingScreen) screen).bounds.x + ((VillagerRecipeViewingScreen) screen).bounds.width + 2;
                return new Rectangle(startX, 0, MinecraftClient.getInstance().getWindow().getScaledWidth() - startX - 2, MinecraftClient.getInstance().getWindow().getScaledHeight());
            }
            
            @Override
            public float getPriority() {
                return -1.0f;
            }
        });
    }
    
    @Override
    public void registerOthers(RecipeHelper recipeHelper) {
        if (!ConfigObject.getInstance().isLoadingDefaultPlugin()) {
            return;
        }
        recipeHelper.registerWorkingStations(CRAFTING, EntryStack.create(Items.CRAFTING_TABLE));
        recipeHelper.registerWorkingStations(SMELTING, EntryStack.create(Items.FURNACE));
        recipeHelper.registerWorkingStations(SMOKING, EntryStack.create(Items.SMOKER));
        recipeHelper.registerWorkingStations(BLASTING, EntryStack.create(Items.BLAST_FURNACE));
        recipeHelper.registerWorkingStations(CAMPFIRE, EntryStack.create(Items.CAMPFIRE));
        recipeHelper.registerWorkingStations(BREWING, EntryStack.create(Items.BREWING_STAND));
        recipeHelper.registerWorkingStations(STONE_CUTTING, EntryStack.create(Items.STONECUTTER));
        recipeHelper.registerWorkingStations(COMPOSTING, EntryStack.create(Items.COMPOSTER));
        recipeHelper.registerAutoCraftButtonArea(CAMPFIRE, bounds -> new Rectangle(bounds.x + 6, bounds.y + 6, 10, 10));
        recipeHelper.removeAutoCraftButton(COMPOSTING);
        recipeHelper.registerScreenClickArea(new Rectangle(88, 32, 28, 23), CraftingTableScreen.class, CRAFTING);
        recipeHelper.registerScreenClickArea(new Rectangle(137, 29, 10, 13), InventoryScreen.class, CRAFTING);
        recipeHelper.registerScreenClickArea(new Rectangle(97, 16, 14, 30), BrewingStandScreen.class, BREWING);
        recipeHelper.registerScreenClickArea(new Rectangle(78, 32, 28, 23), FurnaceScreen.class, SMELTING);
        recipeHelper.registerScreenClickArea(new Rectangle(78, 32, 28, 23), SmokerScreen.class, SMOKING);
        recipeHelper.registerScreenClickArea(new Rectangle(78, 32, 28, 23), BlastFurnaceScreen.class, BLASTING);
    }
    
    @Override
    public int getPriority() {
        return -1;
    }
    
}
