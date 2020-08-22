/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020 shedaniel
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package me.shedaniel.rei.plugin;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.*;
import me.shedaniel.rei.api.fluid.FluidSupportProvider;
import me.shedaniel.rei.api.plugins.REIPluginV0;
import me.shedaniel.rei.plugin.autocrafting.DefaultRecipeBookHandler;
import me.shedaniel.rei.plugin.beacon.DefaultBeaconBaseCategory;
import me.shedaniel.rei.plugin.beacon.DefaultBeaconBaseDisplay;
import me.shedaniel.rei.plugin.blasting.DefaultBlastingDisplay;
import me.shedaniel.rei.plugin.brewing.DefaultBrewingCategory;
import me.shedaniel.rei.plugin.brewing.DefaultBrewingDisplay;
import me.shedaniel.rei.plugin.brewing.RegisteredBrewingRecipe;
import me.shedaniel.rei.plugin.campfire.DefaultCampfireCategory;
import me.shedaniel.rei.plugin.campfire.DefaultCampfireDisplay;
import me.shedaniel.rei.plugin.composting.DefaultCompostingCategory;
import me.shedaniel.rei.plugin.composting.DefaultCompostingDisplay;
import me.shedaniel.rei.plugin.cooking.DefaultCookingCategory;
import me.shedaniel.rei.plugin.crafting.DefaultCraftingCategory;
import me.shedaniel.rei.plugin.crafting.DefaultCustomDisplay;
import me.shedaniel.rei.plugin.crafting.DefaultShapedDisplay;
import me.shedaniel.rei.plugin.crafting.DefaultShapelessDisplay;
import me.shedaniel.rei.plugin.fuel.DefaultFuelCategory;
import me.shedaniel.rei.plugin.fuel.DefaultFuelDisplay;
import me.shedaniel.rei.plugin.information.DefaultInformationCategory;
import me.shedaniel.rei.plugin.information.DefaultInformationDisplay;
import me.shedaniel.rei.plugin.pathing.DefaultPathingCategory;
import me.shedaniel.rei.plugin.pathing.DefaultPathingDisplay;
import me.shedaniel.rei.plugin.pathing.DummyShovelItem;
import me.shedaniel.rei.plugin.smelting.DefaultSmeltingDisplay;
import me.shedaniel.rei.plugin.smithing.DefaultSmithingCategory;
import me.shedaniel.rei.plugin.smithing.DefaultSmithingDisplay;
import me.shedaniel.rei.plugin.smoking.DefaultSmokingDisplay;
import me.shedaniel.rei.plugin.stonecutting.DefaultStoneCuttingCategory;
import me.shedaniel.rei.plugin.stonecutting.DefaultStoneCuttingDisplay;
import me.shedaniel.rei.plugin.stripping.DefaultStrippingCategory;
import me.shedaniel.rei.plugin.stripping.DefaultStrippingDisplay;
import me.shedaniel.rei.plugin.stripping.DummyAxeItem;
import me.shedaniel.rei.plugin.tilling.DefaultTillingCategory;
import me.shedaniel.rei.plugin.tilling.DefaultTillingDisplay;
import me.shedaniel.rei.plugin.tilling.DummyHoeItem;
import me.shedaniel.rei.utils.CollectionUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.ComposterBlock;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.*;
import net.minecraft.client.gui.screen.recipebook.RecipeBookProvider;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.*;
import net.minecraft.potion.PotionUtil;
import net.minecraft.recipe.*;
import net.minecraft.tag.BlockTags;
import net.minecraft.tag.Tag;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Lazy;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.UnaryOperator;

import static me.shedaniel.rei.impl.Internals.attachInstance;

@Environment(EnvType.CLIENT)
public class DefaultPlugin implements REIPluginV0, BuiltinPlugin {
    private static final Logger LOGGER = LogManager.getFormatterLogger("REI/DefaultPlugin");
    public static final Identifier CRAFTING = BuiltinPlugin.CRAFTING;
    public static final Identifier SMELTING = BuiltinPlugin.SMELTING;
    public static final Identifier SMOKING = BuiltinPlugin.SMOKING;
    public static final Identifier BLASTING = BuiltinPlugin.BLASTING;
    public static final Identifier CAMPFIRE = BuiltinPlugin.CAMPFIRE;
    public static final Identifier STONE_CUTTING = BuiltinPlugin.STONE_CUTTING;
    public static final Identifier STRIPPING = BuiltinPlugin.STRIPPING;
    public static final Identifier BREWING = BuiltinPlugin.BREWING;
    public static final Identifier PLUGIN = BuiltinPlugin.PLUGIN;
    public static final Identifier COMPOSTING = BuiltinPlugin.COMPOSTING;
    public static final Identifier FUEL = BuiltinPlugin.FUEL;
    public static final Identifier SMITHING = BuiltinPlugin.SMITHING;
    public static final Identifier BEACON = BuiltinPlugin.BEACON;
    public static final Identifier TILLING = BuiltinPlugin.TILLING;
    public static final Identifier PATHING = BuiltinPlugin.PATHING;
    public static final Identifier INFO = BuiltinPlugin.INFO;
    private static final Identifier DISPLAY_TEXTURE = new Identifier("roughlyenoughitems", "textures/gui/display.png");
    private static final Identifier DISPLAY_TEXTURE_DARK = new Identifier("roughlyenoughitems", "textures/gui/display_dark.png");
    private static final List<Lazy<DefaultBrewingDisplay>> BREWING_DISPLAYS = Lists.newArrayList();
    private static final List<DefaultInformationDisplay> INFO_DISPLAYS = Lists.newArrayList();
    
    public static Identifier getDisplayTexture() {
        return REIHelper.getInstance().getDefaultDisplayTexture();
    }
    
    public DefaultPlugin() {
        attachInstance(this, BuiltinPlugin.class);
    }
    
    @Deprecated
    @ApiStatus.ScheduledForRemoval
    public static void registerBrewingDisplay(DefaultBrewingDisplay recipe) {
        BREWING_DISPLAYS.add(new Lazy<>(() -> recipe));
    }
    
    public static void registerBrewingRecipe(RegisteredBrewingRecipe recipe) {
        BREWING_DISPLAYS.add(new Lazy<>(() -> new DefaultBrewingDisplay(recipe.input, recipe.ingredient, recipe.output)));
    }
    
    public static void registerInfoDisplay(DefaultInformationDisplay display) {
        INFO_DISPLAYS.add(display);
    }
    
    @Override
    public void registerBrewingRecipe(ItemStack input, Ingredient ingredient, ItemStack output) {
        registerBrewingRecipe(new RegisteredBrewingRecipe(input, ingredient, output));
    }
    
    @Override
    public void registerInformation(List<EntryStack> entryStacks, Text name, UnaryOperator<List<Text>> textBuilder) {
        registerInfoDisplay(DefaultInformationDisplay.createFromEntries(entryStacks, name).lines(textBuilder.apply(Lists.newArrayList())));
    }
    
    @Override
    public Identifier getPluginIdentifier() {
        return PLUGIN;
    }
    
    @Override
    public void preRegister() {
        INFO_DISPLAYS.clear();
    }
    
    @Override
    public void registerEntries(EntryRegistry entryRegistry) {
        for (Item item : Registry.ITEM) {
            List<ItemStack> stacks = null;
            try {
                stacks = entryRegistry.appendStacksForItem(item);
            } catch (Exception ignored) {
            }
            if (stacks != null) {
                for (ItemStack stack : entryRegistry.appendStacksForItem(item)) {
                    entryRegistry.registerEntry(EntryStack.create(stack));
                }
            } else
                entryRegistry.registerEntry(EntryStack.create(item));
        }
        EntryStack stack = EntryStack.create(Items.ENCHANTED_BOOK);
        List<EntryStack> enchantments = new ArrayList<>();
        for (Enchantment enchantment : Registry.ENCHANTMENT) {
            for (int i = enchantment.getMinimumLevel(); i <= enchantment.getMaximumLevel(); i++) {
                Map<Enchantment, Integer> map = new HashMap<>();
                map.put(enchantment, i);
                ItemStack itemStack = new ItemStack(Items.ENCHANTED_BOOK);
                EnchantmentHelper.set(map, itemStack);
                enchantments.add(EntryStack.create(itemStack).setting(EntryStack.Settings.CHECK_TAGS, EntryStack.Settings.TRUE));
            }
        }
        entryRegistry.registerEntriesAfter(stack, enchantments);
        for (Fluid fluid : Registry.FLUID) {
            if (!fluid.getDefaultState().isEmpty() && fluid.getDefaultState().isStill())
                entryRegistry.registerEntry(EntryStack.create(fluid));
        }
    }
    
    @Override
    public void registerPluginCategories(RecipeHelper recipeHelper) {
        recipeHelper.registerCategories(
                new DefaultCraftingCategory(),
                new DefaultCookingCategory(SMELTING, EntryStack.create(Items.FURNACE), "category.rei.smelting"),
                new DefaultCookingCategory(SMOKING, EntryStack.create(Items.SMOKER), "category.rei.smoking"),
                new DefaultCookingCategory(BLASTING, EntryStack.create(Items.BLAST_FURNACE), "category.rei.blasting"), new DefaultCampfireCategory(),
                new DefaultStoneCuttingCategory(),
                new DefaultFuelCategory(),
                new DefaultBrewingCategory(),
                new DefaultCompostingCategory(),
                new DefaultStrippingCategory(),
                new DefaultSmithingCategory(),
                new DefaultBeaconBaseCategory(),
                new DefaultTillingCategory(),
                new DefaultPathingCategory(),
                new DefaultInformationCategory()
        );
    }
    
    @Override
    public void registerRecipeDisplays(RecipeHelper recipeHelper) {
        recipeHelper.registerRecipes(CRAFTING, ShapelessRecipe.class, DefaultShapelessDisplay::new);
        recipeHelper.registerRecipes(CRAFTING, ShapedRecipe.class, DefaultShapedDisplay::new);
        recipeHelper.registerRecipes(SMELTING, SmeltingRecipe.class, DefaultSmeltingDisplay::new);
        recipeHelper.registerRecipes(SMOKING, SmokingRecipe.class, DefaultSmokingDisplay::new);
        recipeHelper.registerRecipes(BLASTING, BlastingRecipe.class, DefaultBlastingDisplay::new);
        recipeHelper.registerRecipes(CAMPFIRE, CampfireCookingRecipe.class, DefaultCampfireDisplay::new);
        recipeHelper.registerRecipes(STONE_CUTTING, StonecuttingRecipe.class, DefaultStoneCuttingDisplay::new);
        recipeHelper.registerRecipes(SMITHING, SmithingRecipe.class, DefaultSmithingDisplay::new);
        for (Lazy<DefaultBrewingDisplay> display : BREWING_DISPLAYS) {
            recipeHelper.registerDisplay(display.get());
        }
        for (Map.Entry<Item, Integer> entry : AbstractFurnaceBlockEntity.createFuelTimeMap().entrySet()) {
            recipeHelper.registerDisplay(new DefaultFuelDisplay(EntryStack.create(entry.getKey()), entry.getValue()));
        }
        List<EntryStack> arrowStack = Collections.singletonList(EntryStack.create(Items.ARROW));
        EntryRegistry.getInstance().getEntryStacks().filter(entry -> entry.getItem() == Items.LINGERING_POTION).forEach(entry -> {
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
            recipeHelper.registerDisplay(new DefaultCustomDisplay(null, input, output));
        });
        Map<ItemConvertible, Float> map = Maps.newLinkedHashMap();
        if (ComposterBlock.ITEM_TO_LEVEL_INCREASE_CHANCE.isEmpty())
            ComposterBlock.registerDefaultCompostableItems();
        for (Object2FloatMap.Entry<ItemConvertible> entry : ComposterBlock.ITEM_TO_LEVEL_INCREASE_CHANCE.object2FloatEntrySet()) {
            if (entry.getFloatValue() > 0)
                map.put(entry.getKey(), entry.getFloatValue());
        }
        List<ItemConvertible> stacks = Lists.newArrayList(map.keySet());
        stacks.sort(Comparator.comparing(map::get));
        for (int i = 0; i < stacks.size(); i += MathHelper.clamp(48, 1, stacks.size() - i)) {
            List<ItemConvertible> thisStacks = Lists.newArrayList();
            for (int j = i; j < i + 48; j++)
                if (j < stacks.size())
                    thisStacks.add(stacks.get(j));
            recipeHelper.registerDisplay(new DefaultCompostingDisplay(MathHelper.floor(i / 48f), thisStacks, map, new ItemStack(Items.BONE_MEAL)));
        }
        DummyAxeItem.getStrippedBlocksMap().entrySet().stream().sorted(Comparator.comparing(b -> Registry.BLOCK.getId(b.getKey()))).forEach(set -> {
            recipeHelper.registerDisplay(new DefaultStrippingDisplay(EntryStack.create(set.getKey()), EntryStack.create(set.getValue())));
        });
        DummyHoeItem.getTilledBlocksMap().entrySet().stream().sorted(Comparator.comparing(b -> Registry.BLOCK.getId(b.getKey()))).forEach(set -> {
            recipeHelper.registerDisplay(new DefaultTillingDisplay(EntryStack.create(set.getKey()), EntryStack.create(set.getValue().getBlock())));
        });
        DummyShovelItem.getPathBlocksMap().entrySet().stream().sorted(Comparator.comparing(b -> Registry.BLOCK.getId(b.getKey()))).forEach(set -> {
            recipeHelper.registerDisplay(new DefaultPathingDisplay(EntryStack.create(set.getKey()), EntryStack.create(set.getValue().getBlock())));
        });
        recipeHelper.registerDisplay(new DefaultBeaconBaseDisplay(CollectionUtils.map(Lists.newArrayList(BlockTags.BEACON_BASE_BLOCKS.values()), ItemStack::new)));
    }
    
    @Override
    public void postRegister() {
        for (DefaultInformationDisplay display : INFO_DISPLAYS)
            RecipeHelper.getInstance().registerDisplay(display);
        // TODO Turn this into an API
        // Sit tight! This will be a fast journey!
        long time = System.currentTimeMillis();
        EntryRegistry.getInstance().getEntryStacks().forEach(this::applyPotionTransformer);
        for (List<RecipeDisplay> displays : RecipeHelper.getInstance().getAllRecipesNoHandlers().values()) {
            for (RecipeDisplay display : displays) {
                for (List<EntryStack> entries : display.getInputEntries())
                    for (EntryStack stack : entries)
                        applyPotionTransformer(stack);
                for (List<EntryStack> entries : display.getResultingEntries())
                    for (EntryStack stack : entries)
                        applyPotionTransformer(stack);
            }
        }
        time = System.currentTimeMillis() - time;
        LOGGER.info("Applied Check Tags for potion in %dms.", time);
    }
    
    private void applyPotionTransformer(EntryStack stack) {
        if (stack.getItem() instanceof PotionItem)
            stack.addSetting(EntryStack.Settings.CHECK_TAGS, EntryStack.Settings.TRUE);
    }
    
    @Override
    public void registerBounds(DisplayHelper displayHelper) {
        BaseBoundsHandler baseBoundsHandler = BaseBoundsHandler.getInstance();
        baseBoundsHandler.registerExclusionZones(AbstractInventoryScreen.class, new DefaultPotionEffectExclusionZones());
        baseBoundsHandler.registerExclusionZones(RecipeBookProvider.class, new DefaultRecipeBookExclusionZones());
        displayHelper.registerProvider(new DisplayHelper.DisplayBoundsProvider<ContainerScreen<?>>() {
            @Override
            public Rectangle getScreenBounds(ContainerScreen<?> screen) {
                return new Rectangle(screen.x, screen.y, screen.containerWidth, screen.containerHeight);
            }
            
            @Override
            public Class<?> getBaseSupportedClass() {
                return ContainerScreen.class;
            }
        });
    }
    
    @Override
    public void registerOthers(RecipeHelper recipeHelper) {
        recipeHelper.registerAutoCraftingHandler(new DefaultRecipeBookHandler());
        
        recipeHelper.registerWorkingStations(CRAFTING, EntryStack.create(Items.CRAFTING_TABLE));
        recipeHelper.registerWorkingStations(SMELTING, EntryStack.create(Items.FURNACE));
        recipeHelper.registerWorkingStations(SMOKING, EntryStack.create(Items.SMOKER));
        recipeHelper.registerWorkingStations(BLASTING, EntryStack.create(Items.BLAST_FURNACE));
        recipeHelper.registerWorkingStations(CAMPFIRE, EntryStack.create(Items.CAMPFIRE));
        recipeHelper.registerWorkingStations(FUEL, EntryStack.create(Items.FURNACE), EntryStack.create(Items.SMOKER), EntryStack.create(Items.BLAST_FURNACE));
        recipeHelper.registerWorkingStations(BREWING, EntryStack.create(Items.BREWING_STAND));
        recipeHelper.registerWorkingStations(STONE_CUTTING, EntryStack.create(Items.STONECUTTER));
        recipeHelper.registerWorkingStations(COMPOSTING, EntryStack.create(Items.COMPOSTER));
        recipeHelper.registerWorkingStations(SMITHING, EntryStack.create(Items.SMITHING_TABLE));
        recipeHelper.registerWorkingStations(BEACON, EntryStack.create(Items.BEACON));
        Tag<Item> axes = MinecraftClient.getInstance().getNetworkHandler().getTagManager().getItems().getTag(new Identifier("fabric", "axes"));
        if (axes != null) {
            for (Item item : axes.values()) {
                recipeHelper.registerWorkingStations(STRIPPING, EntryStack.create(item));
            }
        }
        Tag<Item> hoes = MinecraftClient.getInstance().getNetworkHandler().getTagManager().getItems().getTag(new Identifier("fabric", "hoes"));
        if (hoes != null) {
            for (Item item : hoes.values()) {
                recipeHelper.registerWorkingStations(TILLING, EntryStack.create(item));
            }
        }
        Tag<Item> shovels = MinecraftClient.getInstance().getNetworkHandler().getTagManager().getItems().getTag(new Identifier("fabric", "shovels"));
        if (shovels != null) {
            for (Item item : shovels.values()) {
                recipeHelper.registerWorkingStations(PATHING, EntryStack.create(item));
            }
        }
        recipeHelper.removeAutoCraftButton(FUEL);
        recipeHelper.removeAutoCraftButton(COMPOSTING);
        recipeHelper.removeAutoCraftButton(BEACON);
        recipeHelper.removeAutoCraftButton(INFO);
        recipeHelper.registerScreenClickArea(new Rectangle(88, 32, 28, 23), CraftingTableScreen.class, CRAFTING);
        recipeHelper.registerScreenClickArea(new Rectangle(137, 29, 10, 13), InventoryScreen.class, CRAFTING);
        recipeHelper.registerScreenClickArea(new Rectangle(97, 16, 14, 30), BrewingStandScreen.class, BREWING);
        recipeHelper.registerScreenClickArea(new Rectangle(78, 32, 28, 23), FurnaceScreen.class, SMELTING);
        recipeHelper.registerScreenClickArea(new Rectangle(78, 32, 28, 23), SmokerScreen.class, SMOKING);
        recipeHelper.registerScreenClickArea(new Rectangle(78, 32, 28, 23), BlastFurnaceScreen.class, BLASTING);
        FluidSupportProvider.getInstance().registerFluidProvider(new FluidSupportProvider.FluidProvider() {
            @Override
            public @NotNull EntryStack itemToFluid(@NotNull EntryStack itemStack) {
                Item item = itemStack.getItem();
                if (item instanceof BucketItem)
                    return EntryStack.create(((BucketItem) item).fluid, 1000);
                return EntryStack.empty();
            }
        });
//        SubsetsRegistry subsetsRegistry = SubsetsRegistry.INSTANCE;
//        subsetsRegistry.registerPathEntry("roughlyenoughitems:food", EntryStack.create(Items.MILK_BUCKET));
//        subsetsRegistry.registerPathEntry("roughlyenoughitems:food/roughlyenoughitems:cookies", EntryStack.create(Items.COOKIE));
    }
    
    @Override
    public int getPriority() {
        return -1;
    }
    
}
