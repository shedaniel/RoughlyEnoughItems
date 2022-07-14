/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020, 2021, 2022 shedaniel
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

package me.shedaniel.rei.plugin.client;

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import dev.architectury.injectables.annotations.ExpectPlatform;
import dev.architectury.injectables.annotations.PlatformOnly;
import dev.architectury.platform.Platform;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import it.unimi.dsi.fastutil.objects.ReferenceSet;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.favorites.FavoriteEntry;
import me.shedaniel.rei.api.client.favorites.FavoriteEntryType;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.client.registry.entry.EntryRegistry;
import me.shedaniel.rei.api.client.registry.screen.ExclusionZones;
import me.shedaniel.rei.api.client.registry.screen.ScreenRegistry;
import me.shedaniel.rei.api.client.registry.transfer.TransferHandlerRegistry;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.shedaniel.rei.impl.ClientInternals;
import me.shedaniel.rei.plugin.autocrafting.recipebook.DefaultRecipeBookHandler;
import me.shedaniel.rei.plugin.client.categories.*;
import me.shedaniel.rei.plugin.client.categories.anvil.DefaultAnvilCategory;
import me.shedaniel.rei.plugin.client.categories.beacon.DefaultBeaconBaseCategory;
import me.shedaniel.rei.plugin.client.categories.beacon.DefaultBeaconPaymentCategory;
import me.shedaniel.rei.plugin.client.categories.cooking.DefaultCookingCategory;
import me.shedaniel.rei.plugin.client.categories.crafting.DefaultCraftingCategory;
import me.shedaniel.rei.plugin.client.categories.tag.DefaultTagCategory;
import me.shedaniel.rei.plugin.client.exclusionzones.DefaultPotionEffectExclusionZones;
import me.shedaniel.rei.plugin.client.exclusionzones.DefaultRecipeBookExclusionZones;
import me.shedaniel.rei.plugin.client.favorites.GameModeFavoriteEntry;
import me.shedaniel.rei.plugin.client.favorites.TimeFavoriteEntry;
import me.shedaniel.rei.plugin.client.favorites.WeatherFavoriteEntry;
import me.shedaniel.rei.plugin.common.displays.*;
import me.shedaniel.rei.plugin.common.displays.anvil.AnvilRecipe;
import me.shedaniel.rei.plugin.common.displays.anvil.DefaultAnvilDisplay;
import me.shedaniel.rei.plugin.common.displays.beacon.DefaultBeaconBaseDisplay;
import me.shedaniel.rei.plugin.common.displays.beacon.DefaultBeaconPaymentDisplay;
import me.shedaniel.rei.plugin.common.displays.brewing.BrewingRecipe;
import me.shedaniel.rei.plugin.common.displays.brewing.DefaultBrewingDisplay;
import me.shedaniel.rei.plugin.common.displays.cooking.DefaultBlastingDisplay;
import me.shedaniel.rei.plugin.common.displays.cooking.DefaultSmeltingDisplay;
import me.shedaniel.rei.plugin.common.displays.cooking.DefaultSmokingDisplay;
import me.shedaniel.rei.plugin.common.displays.crafting.DefaultCraftingDisplay;
import me.shedaniel.rei.plugin.common.displays.crafting.DefaultCustomDisplay;
import me.shedaniel.rei.plugin.common.displays.tag.DefaultTagDisplay;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.inventory.*;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import org.jetbrains.annotations.ApiStatus;

import java.util.*;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

@Environment(EnvType.CLIENT)
@ApiStatus.Internal
public class DefaultClientPlugin implements REIClientPlugin, BuiltinClientPlugin {
    public DefaultClientPlugin() {
        ClientInternals.attachInstance((Supplier<Object>) () -> this, "builtinClientPlugin");
    }
    
    @Override
    public void registerBrewingRecipe(Ingredient input, Ingredient ingredient, ItemStack output) {
        DisplayRegistry.getInstance().add(new BrewingRecipe(input, ingredient, output));
    }
    
    @Override
    public void registerInformation(EntryIngredient ingredient, Component name, UnaryOperator<List<Component>> textBuilder) {
        DisplayRegistry.getInstance().add(DefaultInformationDisplay.createFromEntries(ingredient, name).lines(textBuilder.apply(Lists.newArrayList())));
    }
    
    @Override
    public void registerEntries(EntryRegistry registry) {
        for (Item item : Registry.ITEM) {
            try {
                registry.addEntries(EntryIngredients.ofItemStacks(registry.appendStacksForItem(item)));
            } catch (Exception ignored) {
                registry.addEntry(EntryStacks.of(item));
            }
        }
        for (Fluid fluid : Registry.FLUID) {
            FluidState state = fluid.defaultFluidState();
            if (!state.isEmpty() && state.isSource()) {
                registry.addEntry(EntryStacks.of(fluid));
            }
        }
    }
    
    @Override
    public void registerCategories(CategoryRegistry registry) {
        registry.add(
                new DefaultCraftingCategory(),
                new DefaultCookingCategory(SMELTING, EntryStacks.of(Items.FURNACE), "category.rei.smelting"),
                new DefaultCookingCategory(SMOKING, EntryStacks.of(Items.SMOKER), "category.rei.smoking"),
                new DefaultCookingCategory(BLASTING, EntryStacks.of(Items.BLAST_FURNACE), "category.rei.blasting"),
                new DefaultCampfireCategory(),
                new DefaultStoneCuttingCategory(),
                new DefaultFuelCategory(),
                new DefaultBrewingCategory(),
                new DefaultCompostingCategory(),
                new DefaultStrippingCategory(),
                new DefaultSmithingCategory(),
                new DefaultAnvilCategory(),
                new DefaultBeaconBaseCategory(),
                new DefaultBeaconPaymentCategory(),
                new DefaultTillingCategory(),
                new DefaultPathingCategory(),
                new DefaultWaxingCategory(),
                new DefaultWaxScrapingCategory(),
                new DefaultOxidizingCategory(),
                new DefaultOxidationScrapingCategory()
        );
        
        registry.addWorkstations(CRAFTING, EntryStacks.of(Items.CRAFTING_TABLE));
        registry.addWorkstations(SMELTING, EntryStacks.of(Items.FURNACE));
        registry.addWorkstations(SMOKING, EntryStacks.of(Items.SMOKER));
        registry.addWorkstations(BLASTING, EntryStacks.of(Items.BLAST_FURNACE));
        registry.addWorkstations(CAMPFIRE, EntryStacks.of(Items.CAMPFIRE), EntryStacks.of(Items.SOUL_CAMPFIRE));
        registry.addWorkstations(FUEL, EntryStacks.of(Items.FURNACE), EntryStacks.of(Items.SMOKER), EntryStacks.of(Items.BLAST_FURNACE));
        registry.addWorkstations(BREWING, EntryStacks.of(Items.BREWING_STAND));
        registry.addWorkstations(ANVIL, EntryStacks.of(Items.ANVIL));
        registry.addWorkstations(STONE_CUTTING, EntryStacks.of(Items.STONECUTTER));
        registry.addWorkstations(COMPOSTING, EntryStacks.of(Items.COMPOSTER));
        registry.addWorkstations(SMITHING, EntryStacks.of(Items.SMITHING_TABLE));
        registry.addWorkstations(BEACON_BASE, EntryStacks.of(Items.BEACON));
        registry.addWorkstations(BEACON_PAYMENT, EntryStacks.of(Items.BEACON));
        registry.addWorkstations(WAXING, EntryStacks.of(Items.HONEYCOMB));
        
        Set<Item> axes = Sets.newHashSet(), hoes = Sets.newHashSet(), shovels = Sets.newHashSet();
        EntryRegistry.getInstance().getEntryStacks().filter(stack -> stack.getValueType() == ItemStack.class).map(stack -> ((ItemStack) stack.getValue()).getItem()).forEach(item -> {
            if (item instanceof AxeItem && axes.add(item)) {
                registry.addWorkstations(STRIPPING, EntryStacks.of(item));
                registry.addWorkstations(WAX_SCRAPING, EntryStacks.of(item));
                registry.addWorkstations(OXIDATION_SCRAPING, EntryStacks.of(item));
            }
            if (item instanceof HoeItem && hoes.add(item)) {
                registry.addWorkstations(TILLING, EntryStacks.of(item));
            }
            if (item instanceof ShovelItem && shovels.add(item)) {
                registry.addWorkstations(PATHING, EntryStacks.of(item));
            }
        });
        for (EntryStack<?> stack : getTag(new ResourceLocation("c", "axes"))) {
            if (axes.add(stack.<ItemStack>castValue().getItem())) {
                registry.addWorkstations(STRIPPING, stack);
                registry.addWorkstations(WAX_SCRAPING, stack);
                registry.addWorkstations(OXIDATION_SCRAPING, stack);
            }
        }
        for (EntryStack<?> stack : getTag(new ResourceLocation("c", "hoes"))) {
            if (hoes.add(stack.<ItemStack>castValue().getItem())) registry.addWorkstations(TILLING, stack);
        }
        for (EntryStack<?> stack : getTag(new ResourceLocation("c", "shovels"))) {
            if (shovels.add(stack.<ItemStack>castValue().getItem())) registry.addWorkstations(PATHING, stack);
        }
    }
    
    private static EntryIngredient getTag(ResourceLocation tagId) {
        return EntryIngredients.ofItemTag(TagKey.create(Registry.ITEM_REGISTRY, tagId));
    }
    
    @Override
    public void registerDisplays(DisplayRegistry registry) {
        CategoryRegistry.getInstance().add(new DefaultInformationCategory(), new DefaultTagCategory());
        
        registry.registerRecipeFiller(CraftingRecipe.class, RecipeType.CRAFTING, DefaultCraftingDisplay::of);
        registry.registerRecipeFiller(SmeltingRecipe.class, RecipeType.SMELTING, DefaultSmeltingDisplay::new);
        registry.registerRecipeFiller(SmokingRecipe.class, RecipeType.SMOKING, DefaultSmokingDisplay::new);
        registry.registerRecipeFiller(BlastingRecipe.class, RecipeType.BLASTING, DefaultBlastingDisplay::new);
        registry.registerRecipeFiller(CampfireCookingRecipe.class, RecipeType.CAMPFIRE_COOKING, DefaultCampfireDisplay::new);
        registry.registerRecipeFiller(StonecutterRecipe.class, RecipeType.STONECUTTING, DefaultStoneCuttingDisplay::new);
        registry.registerRecipeFiller(UpgradeRecipe.class, RecipeType.SMITHING, DefaultSmithingDisplay::new);
        registry.registerFiller(AnvilRecipe.class, DefaultAnvilDisplay::new);
        registry.registerFiller(BrewingRecipe.class, DefaultBrewingDisplay::new);
        registry.registerFiller(TagKey.class, tagKey -> {
            if (tagKey.isFor(Registry.ITEM_REGISTRY)) {
                return DefaultTagDisplay.ofItems(tagKey);
            } else if (tagKey.isFor(Registry.BLOCK_REGISTRY)) {
                return DefaultTagDisplay.ofItems(tagKey);
            } else if (tagKey.isFor(Registry.FLUID_REGISTRY)) {
                return DefaultTagDisplay.ofFluids(tagKey);
            }
            
            return null;
        });
        for (Map.Entry<Item, Integer> entry : AbstractFurnaceBlockEntity.getFuel().entrySet()) {
            registry.add(new DefaultFuelDisplay(Collections.singletonList(EntryIngredients.of(entry.getKey())), Collections.emptyList(), entry.getValue()));
        }
        EntryIngredient arrowStack = EntryIngredient.of(EntryStacks.of(Items.ARROW));
        ReferenceSet<Potion> registeredPotions = new ReferenceOpenHashSet<>();
        EntryRegistry.getInstance().getEntryStacks().filter(entry -> entry.getValueType() == ItemStack.class && entry.<ItemStack>castValue().getItem() == Items.LINGERING_POTION).forEach(entry -> {
            ItemStack itemStack = (ItemStack) entry.getValue();
            Potion potion = PotionUtils.getPotion(itemStack);
            if (registeredPotions.add(potion)) {
                List<EntryIngredient> input = new ArrayList<>();
                for (int i = 0; i < 4; i++)
                    input.add(arrowStack);
                input.add(EntryIngredients.of(itemStack));
                for (int i = 0; i < 4; i++)
                    input.add(arrowStack);
                ItemStack outputStack = new ItemStack(Items.TIPPED_ARROW, 8);
                PotionUtils.setPotion(outputStack, potion);
                PotionUtils.setCustomEffects(outputStack, PotionUtils.getCustomEffects(itemStack));
                EntryIngredient output = EntryIngredients.of(outputStack);
                registry.add(new DefaultCustomDisplay(null, input, Collections.singletonList(output)));
            }
        });
        if (ComposterBlock.COMPOSTABLES.isEmpty()) {
            ComposterBlock.bootStrap();
        }
        Iterator<List<EntryIngredient>> iterator = Iterators.partition(ComposterBlock.COMPOSTABLES.object2FloatEntrySet().stream().sorted(Map.Entry.comparingByValue()).map(entry -> EntryIngredients.of(entry.getKey())).iterator(), 35);
        while (iterator.hasNext()) {
            List<EntryIngredient> entries = iterator.next();
            registry.add(new DefaultCompostingDisplay(entries, Collections.singletonList(EntryIngredients.of(new ItemStack(Items.BONE_MEAL)))));
        }
        DummyAxeItem.getStrippedBlocksMap().entrySet().stream().sorted(Comparator.comparing(b -> Registry.BLOCK.getKey(b.getKey()))).forEach(set -> {
            registry.add(new DefaultStrippingDisplay(EntryStacks.of(set.getKey()), EntryStacks.of(set.getValue())));
        });
        DummyShovelItem.getPathBlocksMap().entrySet().stream().sorted(Comparator.comparing(b -> Registry.BLOCK.getKey(b.getKey()))).forEach(set -> {
            registry.add(new DefaultPathingDisplay(EntryStacks.of(set.getKey()), EntryStacks.of(set.getValue().getBlock())));
        });
        registry.add(new DefaultBeaconBaseDisplay(Collections.singletonList(EntryIngredients.ofItemTag(BlockTags.BEACON_BASE_BLOCKS)), Collections.emptyList()));
        registry.add(new DefaultBeaconPaymentDisplay(Collections.singletonList(EntryIngredients.ofItemTag(ItemTags.BEACON_PAYMENT_ITEMS)), Collections.emptyList()));
        HoneycombItem.WAXABLES.get().entrySet().stream().sorted(Comparator.comparing(b -> Registry.BLOCK.getKey(b.getKey()))).forEach(set -> {
            registry.add(new DefaultWaxingDisplay(EntryStacks.of(set.getKey()), EntryStacks.of(set.getValue())));
        });
        HoneycombItem.WAX_OFF_BY_BLOCK.get().entrySet().stream().sorted(Comparator.comparing(b -> Registry.BLOCK.getKey(b.getKey()))).forEach(set -> {
            registry.add(new DefaultWaxScrapingDisplay(EntryStacks.of(set.getKey()), EntryStacks.of(set.getValue())));
        });
        WeatheringCopper.NEXT_BY_BLOCK.get().entrySet().stream().sorted(Comparator.comparing(b -> Registry.BLOCK.getKey(b.getKey()))).forEach(set -> {
            registry.add(new DefaultOxidizingDisplay(EntryStacks.of(set.getKey()), EntryStacks.of(set.getValue())));
        });
        WeatheringCopper.PREVIOUS_BY_BLOCK.get().entrySet().stream().sorted(Comparator.comparing(b -> Registry.BLOCK.getKey(b.getKey()))).forEach(set -> {
            registry.add(new DefaultOxidationScrapingDisplay(EntryStacks.of(set.getKey()), EntryStacks.of(set.getValue())));
        });
        if (Platform.isFabric()) {
            Set<Potion> potions = Sets.newLinkedHashSet();
            for (Ingredient container : PotionBrewing.ALLOWED_CONTAINERS) {
                for (PotionBrewing.Mix<Potion> mix : PotionBrewing.POTION_MIXES) {
                    Potion from = mix.from;
                    Ingredient ingredient = mix.ingredient;
                    Potion to = mix.to;
                    Ingredient base = Ingredient.of(Arrays.stream(container.getItems())
                            .map(ItemStack::copy)
                            .map(stack -> PotionUtils.setPotion(stack, from)));
                    ItemStack output = Arrays.stream(container.getItems())
                            .map(ItemStack::copy)
                            .map(stack -> PotionUtils.setPotion(stack, to))
                            .findFirst().orElse(ItemStack.EMPTY);
                    registerBrewingRecipe(base, ingredient, output);
                    potions.add(from);
                    potions.add(to);
                }
            }
            for (Potion potion : potions) {
                for (PotionBrewing.Mix<Item> mix : PotionBrewing.CONTAINER_MIXES) {
                    Item from = mix.from;
                    Ingredient ingredient = mix.ingredient;
                    Item to = mix.to;
                    Ingredient base = Ingredient.of(PotionUtils.setPotion(new ItemStack(from), potion));
                    ItemStack output = PotionUtils.setPotion(new ItemStack(to), potion);
                    registerBrewingRecipe(base, ingredient, output);
                }
            }
        } else {
            registerForgePotions(registry, this);
        }
        
        for (Registry<?> reg : Registry.REGISTRY) {
            reg.getTags().forEach(tagPair -> registry.add(tagPair.getFirst()));
        }
    }
    
    @ExpectPlatform
    @PlatformOnly(PlatformOnly.FORGE)
    private static void registerForgePotions(DisplayRegistry registry, BuiltinClientPlugin clientPlugin) {
        
    }
    
    @Override
    public void registerExclusionZones(ExclusionZones zones) {
        zones.register(EffectRenderingInventoryScreen.class, new DefaultPotionEffectExclusionZones());
        zones.register(RecipeUpdateListener.class, new DefaultRecipeBookExclusionZones());
    }
    
    @Override
    public void registerScreens(ScreenRegistry registry) {
        registry.registerContainerClickArea(new Rectangle(88, 32, 28, 23), CraftingScreen.class, CRAFTING);
        registry.registerContainerClickArea(new Rectangle(137, 29, 10, 13), InventoryScreen.class, CRAFTING);
        registry.registerContainerClickArea(new Rectangle(97, 16, 14, 30), BrewingStandScreen.class, BREWING);
        registry.registerContainerClickArea(new Rectangle(78, 32, 28, 23), FurnaceScreen.class, SMELTING);
        registry.registerContainerClickArea(new Rectangle(78, 32, 28, 23), SmokerScreen.class, SMOKING);
        registry.registerContainerClickArea(new Rectangle(78, 32, 28, 23), BlastFurnaceScreen.class, BLASTING);
    }
    
    @Override
    public void registerTransferHandlers(TransferHandlerRegistry registry) {
        registry.register(new DefaultRecipeBookHandler());
    }
    
    @Override
    public void registerFavorites(FavoriteEntryType.Registry registry) {
        registry.register(GameModeFavoriteEntry.ID, GameModeFavoriteEntry.Type.INSTANCE);
        registry.getOrCrateSection(Component.translatable(GameModeFavoriteEntry.TRANSLATION_KEY))
                .add(Stream.concat(
                        Arrays.stream(GameType.values())
                                .filter(type -> type.getId() >= 0),
                        Stream.of((GameType) null)
                ).<FavoriteEntry>map(GameModeFavoriteEntry::new).toArray(FavoriteEntry[]::new));
        registry.register(WeatherFavoriteEntry.ID, WeatherFavoriteEntry.Type.INSTANCE);
        registry.getOrCrateSection(Component.translatable(WeatherFavoriteEntry.TRANSLATION_KEY))
                .add(Stream.concat(
                        Arrays.stream(WeatherFavoriteEntry.Weather.values()),
                        Stream.of((WeatherFavoriteEntry.Weather) null)
                ).<FavoriteEntry>map(WeatherFavoriteEntry::new).toArray(FavoriteEntry[]::new));
        registry.register(TimeFavoriteEntry.ID, TimeFavoriteEntry.Type.INSTANCE);
        registry.getOrCrateSection(Component.translatable(TimeFavoriteEntry.TRANSLATION_KEY))
                .add(Stream.concat(
                        Arrays.stream(TimeFavoriteEntry.Time.values()),
                        Stream.of((TimeFavoriteEntry.Time) null)
                ).<FavoriteEntry>map(TimeFavoriteEntry::new).toArray(FavoriteEntry[]::new));
    }
    
    @Override
    public double getPriority() {
        return -100;
    }
    
    public static class DummyShovelItem extends ShovelItem {
        public DummyShovelItem(Tier tier, float f, float g, Properties properties) {
            super(tier, f, g, properties);
        }
        
        public static Map<Block, BlockState> getPathBlocksMap() {
            return FLATTENABLES;
        }
    }
    
    public static class DummyAxeItem extends AxeItem {
        public DummyAxeItem(Tier tier, float f, float g, Properties properties) {
            super(tier, f, g, properties);
        }
        
        public static Map<Block, Block> getStrippedBlocksMap() {
            return STRIPPABLES;
        }
    }
}
