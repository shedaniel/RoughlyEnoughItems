/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020, 2021, 2022, 2023 shedaniel
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

import com.google.common.collect.*;
import com.google.gson.internal.LinkedTreeMap;
import dev.architectury.event.EventResult;
import dev.architectury.networking.NetworkManager;
import dev.architectury.platform.Platform;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.favorites.FavoriteEntry;
import me.shedaniel.rei.api.client.favorites.FavoriteEntryType;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.client.registry.entry.CollapsibleEntryRegistry;
import me.shedaniel.rei.api.client.registry.entry.EntryRegistry;
import me.shedaniel.rei.api.client.registry.screen.ExclusionZones;
import me.shedaniel.rei.api.client.registry.screen.ScreenRegistry;
import me.shedaniel.rei.api.client.registry.transfer.TransferHandlerRegistry;
import me.shedaniel.rei.api.client.registry.transfer.simple.SimpleTransferHandler;
import me.shedaniel.rei.api.common.display.basic.BasicDisplay;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.shedaniel.rei.impl.common.InternalLogger;
import me.shedaniel.rei.impl.ClientInternals;
import me.shedaniel.rei.plugin.autocrafting.InventoryCraftingTransferHandler;
import me.shedaniel.rei.plugin.autocrafting.recipebook.DefaultRecipeBookHandler;
import me.shedaniel.rei.plugin.client.categories.*;
import me.shedaniel.rei.plugin.client.categories.anvil.DefaultAnvilCategory;
import me.shedaniel.rei.plugin.client.categories.beacon.DefaultBeaconBaseCategory;
import me.shedaniel.rei.plugin.client.categories.beacon.DefaultBeaconPaymentCategory;
import me.shedaniel.rei.plugin.client.categories.cooking.DefaultCookingCategory;
import me.shedaniel.rei.plugin.client.categories.crafting.DefaultCraftingCategory;
import me.shedaniel.rei.plugin.client.categories.tag.DefaultTagCategory;
import me.shedaniel.rei.plugin.client.displays.ClientsidedCookingDisplay;
import me.shedaniel.rei.plugin.client.displays.ClientsidedCraftingDisplay;
import me.shedaniel.rei.plugin.client.displays.ClientsidedSmithingDisplay;
import me.shedaniel.rei.plugin.client.displays.ClientsidedStoneCuttingDisplay;
import me.shedaniel.rei.plugin.client.exclusionzones.DefaultPotionEffectExclusionZones;
import me.shedaniel.rei.plugin.client.exclusionzones.DefaultRecipeBookExclusionZones;
import me.shedaniel.rei.plugin.client.favorites.GameModeFavoriteEntry;
import me.shedaniel.rei.plugin.client.favorites.TimeFavoriteEntry;
import me.shedaniel.rei.plugin.client.favorites.WeatherFavoriteEntry;
import me.shedaniel.rei.plugin.common.BuiltinPlugin;
import me.shedaniel.rei.plugin.common.displays.*;
import me.shedaniel.rei.plugin.common.displays.anvil.AnvilRecipe;
import me.shedaniel.rei.plugin.common.displays.anvil.DefaultAnvilDisplay;
import me.shedaniel.rei.plugin.common.displays.beacon.DefaultBeaconBaseDisplay;
import me.shedaniel.rei.plugin.common.displays.beacon.DefaultBeaconPaymentDisplay;
import me.shedaniel.rei.plugin.common.displays.brewing.BrewingRecipe;
import me.shedaniel.rei.plugin.common.displays.brewing.DefaultBrewingDisplay;
import me.shedaniel.rei.plugin.common.displays.tag.DefaultTagDisplay;
import me.shedaniel.rei.plugin.common.displays.tag.TagNodes;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.*;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.display.FurnaceRecipeDisplay;
import net.minecraft.world.item.crafting.display.RecipeDisplayId;
import net.minecraft.world.item.crafting.display.ShapedCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapelessCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.SmithingRecipeDisplay;
import net.minecraft.world.item.crafting.display.StonecutterRecipeDisplay;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.enchantment.Repairable;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.ApiStatus;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Environment(EnvType.CLIENT)
@ApiStatus.Internal
public class DefaultClientPlugin implements REIClientPlugin, BuiltinClientPlugin {
    private static Boolean fluidEntriesAvailable;

    public DefaultClientPlugin() {
        ClientInternals.attachInstance((Supplier<Object>) () -> this, "builtinClientPlugin");
    }
    
    @Override
    public void registerBrewingRecipe(EntryIngredient input, EntryIngredient ingredient, EntryIngredient output) {
        DisplayRegistry.getInstance().add(new BrewingRecipe(input, ingredient, output));
    }
    
    @Override
    public void registerInformation(EntryIngredient ingredient, Component name, UnaryOperator<List<Component>> textBuilder) {
        DisplayRegistry.getInstance().add(DefaultInformationDisplay.createFromEntries(ingredient, name).lines(textBuilder.apply(Lists.newArrayList())));
    }
    
    @Override
    public void registerEntries(EntryRegistry registry) {
        Multimap<Item, EntryStack<ItemStack>> items = Multimaps.newListMultimap(new Reference2ObjectOpenHashMap<>()
                , ArrayList::new);
        
        for (Map.Entry<CreativeModeTab, Collection<ItemStack>> entry : collectTabs().entrySet()) {
            try {
                for (ItemStack stack : entry.getValue()) {
                    try {
                        items.put(stack.getItem(), EntryStacks.of(stack));
                    } catch (Exception ignore) {
                    }
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
        
        for (Item item : BuiltInRegistries.ITEM) {
            Collection<EntryStack<ItemStack>> stacks = items.get(item);
            if (stacks.isEmpty()) {
                try {
                    registry.addEntry(EntryStacks.of(item.getDefaultInstance()));
                } catch (Exception ignore) {
                    registry.addEntry(EntryStacks.of(item));
                }
            } else {
                registry.addEntries(stacks);
            }
        }
        
        if (canUseFluidEntries()) {
            for (Fluid fluid : BuiltInRegistries.FLUID) {
                FluidState state = fluid.defaultFluidState();
                if (!state.isEmpty() && state.isSource()) {
                    registry.addEntry(EntryStacks.of(fluid));
                }
            }
        }
    }
    
    private static Map<CreativeModeTab, Collection<ItemStack>> collectTabs() {
        try {
            return (Map<CreativeModeTab, Collection<ItemStack>>) Class.forName(Platform.isNeoForge() ? "me.shedaniel.rei.impl.client.forge.CreativeModeTabCollectorImpl"
                            : "me.shedaniel.rei.impl.client.fabric.CreativeModeTabCollectorImpl")
                    .getDeclaredMethod("collectTabs")
                    .invoke(null);
        } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public void registerCollapsibleEntries(CollapsibleEntryRegistry registry) {
        registry.group(Identifier.fromNamespaceAndPath("roughlyenoughitems", "enchanted_book"), Component.translatable("item.minecraft.enchanted_book"),
                stack -> stack.getType() == VanillaEntryTypes.ITEM && stack.<ItemStack>castValue().is(Items.ENCHANTED_BOOK));
        registry.group(Identifier.fromNamespaceAndPath("roughlyenoughitems", "potion"), Component.translatable("item.minecraft.potion"),
                stack -> stack.getType() == VanillaEntryTypes.ITEM && stack.<ItemStack>castValue().is(Items.POTION));
        registry.group(Identifier.fromNamespaceAndPath("roughlyenoughitems", "splash_potion"), Component.translatable("item.minecraft.splash_potion"),
                stack -> stack.getType() == VanillaEntryTypes.ITEM && stack.<ItemStack>castValue().is(Items.SPLASH_POTION));
        registry.group(Identifier.fromNamespaceAndPath("roughlyenoughitems", "lingering_potion"), Component.translatable("item.minecraft.lingering_potion"),
                stack -> stack.getType() == VanillaEntryTypes.ITEM && stack.<ItemStack>castValue().is(Items.LINGERING_POTION));
        registry.group(Identifier.fromNamespaceAndPath("roughlyenoughitems", "spawn_egg"), Component.translatable("text.rei.spawn_egg"),
                stack -> stack.getType() == VanillaEntryTypes.ITEM && stack.<ItemStack>castValue().getItem() instanceof SpawnEggItem);
        registry.group(Identifier.fromNamespaceAndPath("roughlyenoughitems", "tipped_arrow"), Component.translatable("item.minecraft.tipped_arrow"),
                stack -> stack.getType() == VanillaEntryTypes.ITEM && stack.<ItemStack>castValue().is(Items.TIPPED_ARROW));
        registry.group(Identifier.fromNamespaceAndPath("roughlyenoughitems", "music_disc"), Component.translatable("text.rei.music_disc"),
                stack -> stack.getType() == VanillaEntryTypes.ITEM && stack.<ItemStack>castValue().has(DataComponents.JUKEBOX_PLAYABLE));
    }
    
    @Override
    public void registerCategories(CategoryRegistry registry) {
        registry.add(
                new DefaultCraftingCategory(),
                new DefaultCookingCategory(SMELTING, EntryStacks.of(Items.FURNACE), "category.rei.smelting", 200),
                new DefaultCookingCategory(SMOKING, EntryStacks.of(Items.SMOKER), "category.rei.smoking", 100),
                new DefaultCookingCategory(BLASTING, EntryStacks.of(Items.BLAST_FURNACE), "category.rei.blasting", 100),
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
        
        registry.configure(INFO, config -> config.setQuickCraftingEnabledByDefault(false));
        registry.configure(TAG, config -> config.setQuickCraftingEnabledByDefault(false));
        
        registry.registerVisibilityPredicate(category -> {
            if (category instanceof DefaultTagCategory && Minecraft.getInstance().getSingleplayerServer() == null && !NetworkManager.canServerReceive(TagNodes.REQUEST_TAGS_C2S_PACKET_ID)) {
                return EventResult.interruptFalse();
            }
            
            return EventResult.pass();
        });
        
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
        for (EntryStack<?> stack : getTag(Identifier.fromNamespaceAndPath("c", "axes"))) {
            if (axes.add(stack.<ItemStack>castValue().getItem())) {
                registry.addWorkstations(STRIPPING, stack);
                registry.addWorkstations(WAX_SCRAPING, stack);
                registry.addWorkstations(OXIDATION_SCRAPING, stack);
            }
        }
        for (EntryStack<?> stack : getTag(Identifier.fromNamespaceAndPath("c", "hoes"))) {
            if (hoes.add(stack.<ItemStack>castValue().getItem())) registry.addWorkstations(TILLING, stack);
        }
        for (EntryStack<?> stack : getTag(Identifier.fromNamespaceAndPath("c", "shovels"))) {
            if (shovels.add(stack.<ItemStack>castValue().getItem())) registry.addWorkstations(PATHING, stack);
        }
    }
    
    private static EntryIngredient getTag(Identifier tagId) {
        return EntryIngredients.ofItemTag(TagKey.create(Registries.ITEM, tagId));
    }

    private static boolean hasCraftingStation(FurnaceRecipeDisplay display, Item station) {
        return EntryIngredients.testFuzzy(EntryIngredients.ofSlotDisplay(display.craftingStation()), EntryStacks.of(station));
    }

    private static ClientsidedCookingDisplay createCookingFallbackDisplay(FurnaceRecipeDisplay display, Optional<RecipeDisplayId> id) {
        if (hasCraftingStation(display, Items.SMOKER)) {
            return new ClientsidedCookingDisplay.Smoking(display, id);
        }
        if (hasCraftingStation(display, Items.BLAST_FURNACE)) {
            return new ClientsidedCookingDisplay.Blasting(display, id);
        }
        // Mojang can omit or flatten the workstation slot in recipe-book fallback displays on multiplayer.
        // When that happens, keep the recipe visible by treating it as a normal furnace recipe.
        return new ClientsidedCookingDisplay.Smelting(display, id);
    }

    private static boolean canUseFluidEntries() {
        if (fluidEntriesAvailable != null) {
            return fluidEntriesAvailable;
        }

        try {
            EntryStacks.of(BuiltInRegistries.FLUID.getValue(Identifier.withDefaultNamespace("water")));
            fluidEntriesAvailable = true;
        } catch (Throwable throwable) {
            fluidEntriesAvailable = false;
            InternalLogger.getInstance().warn("Disabling REI fluid entries because Architectury FluidStack failed to initialize.", throwable);
        }

        return fluidEntriesAvailable;
    }
    
    @Override
    public void registerDisplays(DisplayRegistry registry) {
        CategoryRegistry.getInstance().add(new DefaultInformationCategory(), new DefaultTagCategory());
        
        registry.beginRecipeFiller(ShapedCraftingRecipeDisplay.class)
                .fill(ClientsidedCraftingDisplay.Shaped::new);
        registry.beginRecipeFiller(ShapelessCraftingRecipeDisplay.class)
                .fill(ClientsidedCraftingDisplay.Shapeless::new);
        registry.beginRecipeFiller(FurnaceRecipeDisplay.class)
                .filterType(FurnaceRecipeDisplay.TYPE)
                .fill(DefaultClientPlugin::createCookingFallbackDisplay);
        registry.beginRecipeFiller(StonecutterRecipeDisplay.class)
                .filterType(StonecutterRecipeDisplay.TYPE)
                .fill(ClientsidedStoneCuttingDisplay::new);
        registry.beginRecipeFiller(SmithingRecipeDisplay.class)
                .filterType(SmithingRecipeDisplay.TYPE)
                .fill(ClientsidedSmithingDisplay::new);
        registry.beginFiller(AnvilRecipe.class)
                .fill(DefaultAnvilDisplay::new);
        registry.beginFiller(BrewingRecipe.class)
                .fill(DefaultBrewingDisplay::new);
        registry.beginFiller(TagKey.class)
                .fill(tagKey -> {
                    if (tagKey.isFor(Registries.ITEM)) {
                        return DefaultTagDisplay.ofItems(tagKey);
                    } else if (tagKey.isFor(Registries.BLOCK)) {
                        return DefaultTagDisplay.ofItems(tagKey);
                    } else if (tagKey.isFor(Registries.FLUID)) {
                        if (!canUseFluidEntries()) {
                            return null;
                        }
                        return DefaultTagDisplay.ofFluids(tagKey);
                    }
                    
                    return null;
                });
        // TODO: Fuel
//        for (Map.Entry<Item, Integer> entry : AbstractFurnaceBlockEntity.getFuel().entrySet()) {
//            registry.add(new DefaultFuelDisplay(Collections.singletonList(EntryIngredients.of(entry.getKey())), Collections.emptyList(), entry.getValue()));
//        }
        if (ComposterBlock.COMPOSTABLES.isEmpty()) {
            ComposterBlock.bootStrap();
        }
        Iterator<List<EntryIngredient>> iterator = Iterators.partition(ComposterBlock.COMPOSTABLES.object2FloatEntrySet().stream().sorted(Map.Entry.comparingByValue()).map(entry -> EntryIngredients.of(entry.getKey())).iterator(), 35);
        while (iterator.hasNext()) {
            List<EntryIngredient> entries = iterator.next();
            registry.add(new DefaultCompostingDisplay(entries, Collections.singletonList(EntryIngredients.of(new ItemStack(Items.BONE_MEAL)))));
        }
        DummyAxeItem.getStrippedBlocksMap().entrySet().stream().sorted(Comparator.comparing(b -> BuiltInRegistries.BLOCK.getKey(b.getKey()))).forEach(set -> {
            registry.add(new DefaultStrippingDisplay(EntryStacks.of(set.getKey()), EntryStacks.of(set.getValue())));
        });
        DummyShovelItem.getPathBlocksMap().entrySet().stream().sorted(Comparator.comparing(b -> BuiltInRegistries.BLOCK.getKey(b.getKey()))).forEach(set -> {
            registry.add(new DefaultPathingDisplay(EntryStacks.of(set.getKey()), EntryStacks.of(set.getValue().getBlock())));
        });
        registry.add(new DefaultBeaconBaseDisplay(Collections.singletonList(EntryIngredients.ofItemTag(BlockTags.BEACON_BASE_BLOCKS)), Collections.emptyList()));
        registry.add(new DefaultBeaconPaymentDisplay(Collections.singletonList(EntryIngredients.ofItemTag(ItemTags.BEACON_PAYMENT_ITEMS)), Collections.emptyList()));
        HoneycombItem.WAXABLES.get().entrySet().stream().sorted(Comparator.comparing(b -> BuiltInRegistries.BLOCK.getKey(b.getKey()))).forEach(set -> {
            registry.add(new DefaultWaxingDisplay(EntryStacks.of(set.getKey()), EntryStacks.of(set.getValue())));
        });
        HoneycombItem.WAX_OFF_BY_BLOCK.get().entrySet().stream().sorted(Comparator.comparing(b -> BuiltInRegistries.BLOCK.getKey(b.getKey()))).forEach(set -> {
            registry.add(new DefaultWaxScrapingDisplay(EntryStacks.of(set.getKey()), EntryStacks.of(set.getValue())));
        });
        WeatheringCopper.NEXT_BY_BLOCK.get().entrySet().stream().sorted(Comparator.comparing(b -> BuiltInRegistries.BLOCK.getKey(b.getKey()))).forEach(set -> {
            registry.add(new DefaultOxidizingDisplay(EntryStacks.of(set.getKey()), EntryStacks.of(set.getValue())));
        });
        WeatheringCopper.PREVIOUS_BY_BLOCK.get().entrySet().stream().sorted(Comparator.comparing(b -> BuiltInRegistries.BLOCK.getKey(b.getKey()))).forEach(set -> {
            registry.add(new DefaultOxidationScrapingDisplay(EntryStacks.of(set.getKey()), EntryStacks.of(set.getValue())));
        });
        if (Platform.isFabric()) {
            Set<Holder<Potion>> potions = Collections.newSetFromMap(new LinkedTreeMap<>(Comparator.comparing(Holder::getRegisteredName), false));
            PotionBrewing brewing = Minecraft.getInstance().level.potionBrewing();
            for (Ingredient container : brewing.containers) {
                for (PotionBrewing.Mix<Potion> mix : brewing.potionMixes) {
                    Holder<Potion> from = mix.from();
                    Ingredient ingredient = mix.ingredient();
                    Holder<Potion> to = mix.to();
                    EntryIngredient base = EntryIngredients.ofIngredient(container)
                            .map(stack -> {
                                EntryStack<?> copied = stack.copy();
                                copied.<ItemStack>castValue().set(DataComponents.POTION_CONTENTS, new PotionContents(from));
                                return copied;
                            });
                    EntryIngredient output = EntryIngredients.ofIngredient(container)
                            .map(stack -> {
                                EntryStack<?> copied = stack.copy();
                                copied.<ItemStack>castValue().set(DataComponents.POTION_CONTENTS, new PotionContents(to));
                                return copied;
                            });
                    registerBrewingRecipe(base, EntryIngredients.ofIngredient(ingredient), output);
                    potions.add(from);
                    potions.add(to);
                }
            }
            for (Holder<Potion> potion : potions) {
                for (PotionBrewing.Mix<Item> mix : brewing.containerMixes) {
                    Holder<Item> from = mix.from();
                    Ingredient ingredient = mix.ingredient();
                    Holder<Item> to = mix.to();
                    ItemStack baseStack = new ItemStack(from);
                    baseStack.set(DataComponents.POTION_CONTENTS, new PotionContents(potion));
                    EntryIngredient base = EntryIngredients.of(baseStack);
                    ItemStack output = new ItemStack(to);
                    output.set(DataComponents.POTION_CONTENTS, new PotionContents(potion));
                    registerBrewingRecipe(base, EntryIngredients.ofIngredient(ingredient), EntryIngredients.of(output));
                }
            }
        } else {
            registerForgePotions(registry, this);
        }
        
        for (Item item : BuiltInRegistries.ITEM) {
            ItemStack stack = item.getDefaultInstance();
            if (!stack.isDamageableItem()) continue;
            if (item.components().has(DataComponents.REPAIRABLE)) {
                Repairable repairable = item.components().get(DataComponents.REPAIRABLE);
                EntryIngredient repairMaterialBase = EntryIngredients.ofItemsHolderSet(repairable.items());
                if (repairMaterialBase.isEmpty()) continue;
                
                for (int[] i = {1}; i[0] <= 4; i[0]++) {
                    ItemStack baseStack = item.getDefaultInstance();
                    int toRepair = i[0] == 4 ? baseStack.getMaxDamage() : baseStack.getMaxDamage() / 4 * i[0];
                    baseStack.setDamageValue(toRepair);
                    EntryIngredient repairMaterial = repairMaterialBase.map(s -> {
                        EntryStack<?> newStack = s.copy();
                        newStack.<ItemStack>castValue().setCount(i[0]);
                        return newStack;
                    });
                    Optional<Pair<ItemStack, Integer>> output = DefaultAnvilDisplay.calculateOutput(baseStack, repairMaterial.get(0).castValue());
                    if (output.isEmpty()) continue;
                    registry.add(new DefaultAnvilDisplay(List.of(EntryIngredients.of(baseStack), repairMaterial),
                            Collections.singletonList(EntryIngredients.of(output.get().getLeft())), Optional.empty(), OptionalInt.of(output.get().getRight())));
                }
            }
        }
        List<Pair<EnchantmentInstance, ItemStack>> enchantmentBooks = BasicDisplay.registryAccess().lookup(Registries.ENCHANTMENT)
                .stream()
                .flatMap(HolderLookup::listElements)
                .flatMap(holder -> {
                    if (!holder.isBound()) return Stream.empty();
                    Enchantment enchantment = holder.value();
                    if (enchantment.getMaxLevel() - enchantment.getMinLevel() >= 10) {
                        return IntStream.of(enchantment.getMinLevel(), enchantment.getMaxLevel())
                                .mapToObj(lvl -> new EnchantmentInstance(holder, lvl));
                    } else {
                        return IntStream.rangeClosed(enchantment.getMinLevel(), enchantment.getMaxLevel())
                                .mapToObj(lvl -> new EnchantmentInstance(holder, lvl));
                    }
                })
                .map(instance -> {
                    return Pair.of(instance, EnchantmentHelper.createBook(instance));
                })
                .toList();
        EntryRegistry.getInstance().getEntryStacks().forEach(stack -> {
            if (stack.getType() != VanillaEntryTypes.ITEM) return;
            ItemStack itemStack = stack.castValue();
            if (!itemStack.isEnchantable()) return;
            for (Pair<EnchantmentInstance, ItemStack> pair : enchantmentBooks) {
                if (!pair.getKey().enchantment().value().canEnchant(itemStack)) continue;
                Optional<Pair<ItemStack, Integer>> output = DefaultAnvilDisplay.calculateOutput(itemStack, pair.getValue());
                if (output.isEmpty()) continue;
                registry.add(new DefaultAnvilDisplay(List.of(EntryIngredients.of(itemStack), EntryIngredients.of(pair.getValue())),
                        Collections.singletonList(EntryIngredients.of(output.get().getLeft())), Optional.empty(), OptionalInt.of(output.get().getRight())));
            }
        });
        
        for (Registry<?> reg : BuiltInRegistries.REGISTRY) {
            reg.getTags().forEach(tagPair -> tagPair.unwrap().ifLeft(registry::add));
        }
    }
    
    protected void registerForgePotions(DisplayRegistry registry, BuiltinClientPlugin clientPlugin) {
        
    }
    
    @Override
    public void registerExclusionZones(ExclusionZones zones) {
        zones.register(InventoryScreen.class, new DefaultPotionEffectExclusionZones<>());
        zones.register(CreativeModeInventoryScreen.class, new DefaultPotionEffectExclusionZones<>());
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
        registry.register(SimpleTransferHandler.create(CraftingMenu.class, BuiltinPlugin.CRAFTING,
                new SimpleTransferHandler.IntRange(1, 10)));
        registry.register(new InventoryCraftingTransferHandler(SimpleTransferHandler.create(InventoryMenu.class, BuiltinPlugin.CRAFTING,
                new SimpleTransferHandler.IntRange(1, 5))));
        registry.register(SimpleTransferHandler.create(FurnaceMenu.class, BuiltinPlugin.SMELTING,
                new SimpleTransferHandler.IntRange(0, 1)));
        registry.register(SimpleTransferHandler.create(SmokerMenu.class, BuiltinPlugin.SMOKING,
                new SimpleTransferHandler.IntRange(0, 1)));
        registry.register(SimpleTransferHandler.create(BlastFurnaceMenu.class, BuiltinPlugin.BLASTING,
                new SimpleTransferHandler.IntRange(0, 1)));
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
        public DummyShovelItem(ToolMaterial material, float damage, float speed, Properties properties) {
            super(material, damage, speed, properties);
        }
        
        public static Map<Block, BlockState> getPathBlocksMap() {
            return FLATTENABLES;
        }
    }
    
    public static class DummyAxeItem extends AxeItem {
        public DummyAxeItem(ToolMaterial material, float damage, float speed, Properties properties) {
            super(material, damage, speed, properties);
        }
        
        public static Map<Block, Block> getStrippedBlocksMap() {
            return STRIPPABLES;
        }
    }
}
