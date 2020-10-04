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

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import it.unimi.dsi.fastutil.objects.ReferenceSet;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.*;
import me.shedaniel.rei.api.fluid.FluidSupportProvider;
import me.shedaniel.rei.api.plugins.REIPlugin;
import me.shedaniel.rei.api.plugins.REIPluginV0;
import me.shedaniel.rei.plugin.autocrafting.DefaultRecipeBookHandler;
import me.shedaniel.rei.plugin.beacon.DefaultBeaconBaseCategory;
import me.shedaniel.rei.plugin.beacon.DefaultBeaconBaseDisplay;
import me.shedaniel.rei.plugin.beacon_payment.DefaultBeaconPaymentCategory;
import me.shedaniel.rei.plugin.beacon_payment.DefaultBeaconPaymentDisplay;
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
import net.minecraft.block.ComposterBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.DisplayEffectsScreen;
import net.minecraft.client.gui.recipebook.IRecipeShownListener;
import net.minecraft.client.gui.screen.inventory.*;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.*;
import net.minecraft.item.crafting.*;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionBrewing;
import net.minecraft.potion.PotionUtils;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ActionResult;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.brewing.BrewingRecipe;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.common.brewing.IBrewingRecipe;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IRegistryDelegate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.UnaryOperator;

import static me.shedaniel.rei.impl.Internals.attachInstance;

@OnlyIn(Dist.CLIENT)
@REIPlugin
public class DefaultPlugin implements REIPluginV0, BuiltinPlugin {
    private static final Logger LOGGER = LogManager.getFormatterLogger("REI/DefaultPlugin");
    public static final ResourceLocation CRAFTING = BuiltinPlugin.CRAFTING;
    public static final ResourceLocation SMELTING = BuiltinPlugin.SMELTING;
    public static final ResourceLocation SMOKING = BuiltinPlugin.SMOKING;
    public static final ResourceLocation BLASTING = BuiltinPlugin.BLASTING;
    public static final ResourceLocation CAMPFIRE = BuiltinPlugin.CAMPFIRE;
    public static final ResourceLocation STONE_CUTTING = BuiltinPlugin.STONE_CUTTING;
    public static final ResourceLocation STRIPPING = BuiltinPlugin.STRIPPING;
    public static final ResourceLocation BREWING = BuiltinPlugin.BREWING;
    public static final ResourceLocation PLUGIN = BuiltinPlugin.PLUGIN;
    public static final ResourceLocation COMPOSTING = BuiltinPlugin.COMPOSTING;
    public static final ResourceLocation FUEL = BuiltinPlugin.FUEL;
    public static final ResourceLocation SMITHING = BuiltinPlugin.SMITHING;
    public static final ResourceLocation BEACON = BuiltinPlugin.BEACON;
    public static final ResourceLocation BEACON_PAYMENT = BuiltinPlugin.BEACON_PAYMENT;
    public static final ResourceLocation TILLING = BuiltinPlugin.TILLING;
    public static final ResourceLocation PATHING = BuiltinPlugin.PATHING;
    public static final ResourceLocation INFO = BuiltinPlugin.INFO;
    private static final ResourceLocation DISPLAY_TEXTURE = new ResourceLocation("roughlyenoughitems", "textures/gui/display.png");
    private static final ResourceLocation DISPLAY_TEXTURE_DARK = new ResourceLocation("roughlyenoughitems", "textures/gui/display_dark.png");
    
    public static ResourceLocation getDisplayTexture() {
        return REIHelper.getInstance().getDefaultDisplayTexture();
    }
    
    public DefaultPlugin() {
        attachInstance(this, BuiltinPlugin.class);
    }
    
    @Deprecated
    @ApiStatus.ScheduledForRemoval
    public static void registerBrewingDisplay(DefaultBrewingDisplay recipe) {
        RecipeHelper.getInstance().registerDisplay(recipe);
    }
    
    public static void registerBrewingRecipe(RegisteredBrewingRecipe recipe) {
        RecipeHelper.getInstance().registerDisplay(new DefaultBrewingDisplay(recipe.input, recipe.ingredient, recipe.output));
    }
    
    public static void registerInfoDisplay(DefaultInformationDisplay display) {
        RecipeHelper.getInstance().registerDisplay(display);
    }
    
    @Override
    public void registerBrewingRecipe(Ingredient input, Ingredient ingredient, ItemStack output) {
        registerBrewingRecipe(new RegisteredBrewingRecipe(input, ingredient, output));
    }
    
    @Override
    public void registerInformation(List<EntryStack> entryStacks, ITextComponent name, UnaryOperator<List<ITextComponent>> textBuilder) {
        registerInfoDisplay(DefaultInformationDisplay.createFromEntries(entryStacks, name).lines(textBuilder.apply(Lists.newArrayList())));
    }
    
    @Override
    public ResourceLocation getPluginIdentifier() {
        return PLUGIN;
    }
    
    @Override
    public void registerEntries(EntryRegistry entryRegistry) {
        for (Item item : ForgeRegistries.ITEMS) {
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
        for (Enchantment enchantment : ForgeRegistries.ENCHANTMENTS) {
            IntConsumer consumer = level -> {
                Map<Enchantment, Integer> map = new HashMap<>();
                map.put(enchantment, level);
                ItemStack itemStack = new ItemStack(Items.ENCHANTED_BOOK);
                EnchantmentHelper.setEnchantments(map, itemStack);
                enchantments.add(EntryStack.create(itemStack).setting(EntryStack.Settings.CHECK_TAGS, EntryStack.Settings.TRUE));
            };
            if (enchantment.getMaxLevel() - enchantment.getMinLevel() >= 10) {
                consumer.accept(enchantment.getMinLevel());
                consumer.accept(enchantment.getMaxLevel());
            } else {
                for (int i = enchantment.getMinLevel(); i <= enchantment.getMaxLevel(); i++) consumer.accept(i);
            }
        }
        entryRegistry.registerEntriesAfter(stack, enchantments);
        for (Fluid fluid : ForgeRegistries.FLUIDS) {
            if (!fluid.defaultFluidState().isEmpty() && fluid.defaultFluidState().isSource())
                entryRegistry.registerEntry(EntryStack.create(new FluidStack(fluid, 1000)));
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
                new DefaultBeaconPaymentCategory(),
                new DefaultTillingCategory(),
                new DefaultPathingCategory(),
                new DefaultInformationCategory()
        );
    }
    
    @Override
    public void registerRecipeDisplays(RecipeHelper recipeHelper) {
        recipeHelper.registerRecipes(CRAFTING, ShapelessRecipe.class, DefaultShapelessDisplay::new);
        recipeHelper.registerRecipes(CRAFTING, ShapedRecipe.class, DefaultShapedDisplay::new);
        recipeHelper.registerRecipes(SMELTING, FurnaceRecipe.class, DefaultSmeltingDisplay::new);
        recipeHelper.registerRecipes(SMOKING, SmokingRecipe.class, DefaultSmokingDisplay::new);
        recipeHelper.registerRecipes(BLASTING, BlastingRecipe.class, DefaultBlastingDisplay::new);
        recipeHelper.registerRecipes(CAMPFIRE, CampfireCookingRecipe.class, DefaultCampfireDisplay::new);
        recipeHelper.registerRecipes(STONE_CUTTING, StonecuttingRecipe.class, DefaultStoneCuttingDisplay::new);
        recipeHelper.registerRecipes(SMITHING, SmithingRecipe.class, DefaultSmithingDisplay::new);
        EntryRegistry.getInstance().getEntryStacks()
                .filter(stack -> stack.getType() == EntryStack.Type.ITEM)
                .forEach(stack -> {
                    int burnTime = ForgeHooks.getBurnTime(stack.getItemStack());
                    if (burnTime > 0) {
                        recipeHelper.registerDisplay(new DefaultFuelDisplay(stack, burnTime));
                    }
                });
        List<EntryStack> arrowStack = Collections.singletonList(EntryStack.create(Items.ARROW));
        ReferenceSet<Potion> registeredPotions = new ReferenceOpenHashSet<>();
        EntryRegistry.getInstance().getEntryStacks().filter(entry -> entry.getItem() == Items.LINGERING_POTION).forEach(entry -> {
            Potion potion = PotionUtils.getPotion(entry.getItemStack());
            if (registeredPotions.add(potion)) {
                List<List<EntryStack>> input = new ArrayList<>();
                for (int i = 0; i < 4; i++)
                    input.add(arrowStack);
                input.add(Collections.singletonList(EntryStack.create(entry.getItemStack())));
                for (int i = 0; i < 4; i++)
                    input.add(arrowStack);
                ItemStack outputStack = new ItemStack(Items.TIPPED_ARROW, 8);
                PotionUtils.setPotion(outputStack, potion);
                PotionUtils.setCustomEffects(outputStack, PotionUtils.getCustomEffects(entry.getItemStack()));
                List<EntryStack> output = Collections.singletonList(EntryStack.create(outputStack).addSetting(EntryStack.Settings.CHECK_TAGS, EntryStack.Settings.TRUE));
                recipeHelper.registerDisplay(new DefaultCustomDisplay(null, input, output));
            }
        });
        if (ComposterBlock.COMPOSTABLES.isEmpty())
            ComposterBlock.bootStrap();
        Object2FloatMap<IItemProvider> compostables = ComposterBlock.COMPOSTABLES;
        int i = 0;
        Iterator<List<Object2FloatMap.Entry<IItemProvider>>> iterator = Iterators.partition(compostables.object2FloatEntrySet().stream().sorted(Map.Entry.comparingByValue()).iterator(), 48);
        while (iterator.hasNext()) {
            List<Object2FloatMap.Entry<IItemProvider>> entries = iterator.next();
            recipeHelper.registerDisplay(new DefaultCompostingDisplay(i, entries, compostables, new ItemStack(Items.BONE_MEAL)));
            i++;
        }
        DummyAxeItem.getStrippedBlocksMap().entrySet().stream().sorted(Comparator.comparing(b -> ForgeRegistries.BLOCKS.getKey(b.getKey()))).forEach(set -> {
            recipeHelper.registerDisplay(new DefaultStrippingDisplay(EntryStack.create(set.getKey()), EntryStack.create(set.getValue())));
        });
        DummyHoeItem.getTilledBlocksMap().entrySet().stream().sorted(Comparator.comparing(b -> ForgeRegistries.BLOCKS.getKey(b.getKey()))).forEach(set -> {
            recipeHelper.registerDisplay(new DefaultTillingDisplay(EntryStack.create(set.getKey()), EntryStack.create(set.getValue().getBlock())));
        });
        DummyShovelItem.getPathBlocksMap().entrySet().stream().sorted(Comparator.comparing(b -> ForgeRegistries.BLOCKS.getKey(b.getKey()))).forEach(set -> {
            recipeHelper.registerDisplay(new DefaultPathingDisplay(EntryStack.create(set.getKey()), EntryStack.create(set.getValue().getBlock())));
        });
        recipeHelper.registerDisplay(new DefaultBeaconBaseDisplay(CollectionUtils.map(Lists.newArrayList(BlockTags.BEACON_BASE_BLOCKS.getValues()), ItemStack::new)));
        recipeHelper.registerDisplay(new DefaultBeaconPaymentDisplay(CollectionUtils.map(Lists.newArrayList(ItemTags.BEACON_PAYMENT_ITEMS.getValues()), ItemStack::new)));
        for (IBrewingRecipe recipe : BrewingRecipeRegistry.getRecipes()) {
            if (recipe instanceof BrewingRecipe) {
                registerBrewingRecipe(((BrewingRecipe) recipe).getIngredient(), ((BrewingRecipe) recipe).getInput(), ((BrewingRecipe) recipe).getOutput());
            }
        }
        Set<Potion> potions = Sets.newLinkedHashSet();
        for (Ingredient container : PotionBrewing.ALLOWED_CONTAINERS) {
            for (Object mix : PotionBrewing.POTION_MIXES) {
                IRegistryDelegate<Potion> from = mixGetFrom(mix);
                Ingredient ingredient = mixGetIngredient(mix);
                IRegistryDelegate<Potion> to = mixGetTo(mix);
                Ingredient base = Ingredient.of(Arrays.stream(container.getItems())
                        .map(ItemStack::copy)
                        .map(stack -> PotionUtils.setPotion(stack, from.get())));
                ItemStack output = Arrays.stream(container.getItems())
                        .map(ItemStack::copy)
                        .map(stack -> PotionUtils.setPotion(stack, to.get()))
                        .findFirst().orElse(ItemStack.EMPTY);
                registerBrewingRecipe(base, ingredient, output);
                potions.add(from.get());
                potions.add(to.get());
            }
        }
        for (Potion potion : potions) {
            for (Object mix : PotionBrewing.CONTAINER_MIXES) {
                IRegistryDelegate<Item> from = mixGetFrom(mix);
                Ingredient ingredient = mixGetIngredient(mix);
                IRegistryDelegate<Item> to = mixGetTo(mix);
                Ingredient base = Ingredient.of(PotionUtils.setPotion(new ItemStack(from.get()), potion));
                ItemStack output = PotionUtils.setPotion(new ItemStack(to.get()), potion);
                registerBrewingRecipe(base, ingredient, output);
            }
        }
    }
    
    private static final Class<Object> MIX_PREDICATE;
    
    static {
        try {
            MIX_PREDICATE = (Class<Object>) Class.forName("net.minecraft.potion.PotionBrewing$MixPredicate");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    private static <T> IRegistryDelegate<T> mixGetFrom(Object mix) {
        try {
            return ObfuscationReflectionHelper.getPrivateValue(MIX_PREDICATE, mix, "field_185198_a");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    private static Ingredient mixGetIngredient(Object mix) {
        try {
            return ObfuscationReflectionHelper.getPrivateValue(MIX_PREDICATE, mix, "field_185199_b");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    private static <T> IRegistryDelegate<T> mixGetTo(Object mix) {
        try {
            return ObfuscationReflectionHelper.getPrivateValue(MIX_PREDICATE, mix, "field_185200_c");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public void postRegister() {
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
        baseBoundsHandler.registerExclusionZones(DisplayEffectsScreen.class, new DefaultPotionEffectExclusionZones());
        baseBoundsHandler.registerExclusionZones(IRecipeShownListener.class, new DefaultRecipeBookExclusionZones());
        displayHelper.registerProvider(new DisplayHelper.DisplayBoundsProvider<ContainerScreen<?>>() {
            @Override
            public Rectangle getScreenBounds(ContainerScreen<?> screen) {
                return new Rectangle(screen.getGuiLeft(), screen.getGuiTop(), screen.getXSize(), screen.getYSize());
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
        recipeHelper.registerWorkingStations(CAMPFIRE, EntryStack.create(Items.CAMPFIRE), EntryStack.create(Items.SOUL_CAMPFIRE));
        recipeHelper.registerWorkingStations(FUEL, EntryStack.create(Items.FURNACE), EntryStack.create(Items.SMOKER), EntryStack.create(Items.BLAST_FURNACE));
        recipeHelper.registerWorkingStations(BREWING, EntryStack.create(Items.BREWING_STAND));
        recipeHelper.registerWorkingStations(STONE_CUTTING, EntryStack.create(Items.STONECUTTER));
        recipeHelper.registerWorkingStations(COMPOSTING, EntryStack.create(Items.COMPOSTER));
        recipeHelper.registerWorkingStations(SMITHING, EntryStack.create(Items.SMITHING_TABLE));
        recipeHelper.registerWorkingStations(BEACON, EntryStack.create(Items.BEACON));
        recipeHelper.registerWorkingStations(BEACON_PAYMENT, EntryStack.create(Items.BEACON));
        Set<Item> axes = Sets.newHashSet(), hoes = Sets.newHashSet(), shovels = Sets.newHashSet();
        EntryRegistry.getInstance().getEntryStacks().filter(stack -> stack.getType() == EntryStack.Type.ITEM).map(EntryStack::getItem).forEach(item -> {
            if (item instanceof AxeItem && axes.add(item)) {
                recipeHelper.registerWorkingStations(STRIPPING, EntryStack.create(item));
            }
            if (item instanceof HoeItem && hoes.add(item)) {
                recipeHelper.registerWorkingStations(TILLING, EntryStack.create(item));
            }
            if (item instanceof ShovelItem && shovels.add(item)) {
                recipeHelper.registerWorkingStations(PATHING, EntryStack.create(item));
            }
        });
        ITag<Item> axesTag = Minecraft.getInstance().getConnection().getTags().getItems().getTag(new ResourceLocation("c", "axes"));
        if (axesTag != null) {
            for (Item item : axesTag.getValues()) {
                if (axes.add(item)) recipeHelper.registerWorkingStations(STRIPPING, EntryStack.create(item));
            }
        }
        ITag<Item> hoesTag = Minecraft.getInstance().getConnection().getTags().getItems().getTag(new ResourceLocation("c", "hoes"));
        if (hoesTag != null) {
            for (Item item : hoesTag.getValues()) {
                if (hoes.add(item)) recipeHelper.registerWorkingStations(TILLING, EntryStack.create(item));
            }
        }
        ITag<Item> shovelsTag = Minecraft.getInstance().getConnection().getTags().getItems().getTag(new ResourceLocation("c", "shovels"));
        if (shovelsTag != null) {
            for (Item item : shovelsTag.getValues()) {
                if (shovels.add(item)) recipeHelper.registerWorkingStations(PATHING, EntryStack.create(item));
            }
        }
        recipeHelper.removeAutoCraftButton(FUEL);
        recipeHelper.removeAutoCraftButton(COMPOSTING);
        recipeHelper.removeAutoCraftButton(BEACON);
        recipeHelper.removeAutoCraftButton(BEACON_PAYMENT);
        recipeHelper.removeAutoCraftButton(INFO);
        recipeHelper.registerContainerClickArea(new Rectangle(88, 32, 28, 23), CraftingScreen.class, CRAFTING);
        recipeHelper.registerContainerClickArea(new Rectangle(137, 29, 10, 13), InventoryScreen.class, CRAFTING);
        recipeHelper.registerContainerClickArea(new Rectangle(97, 16, 14, 30), BrewingStandScreen.class, BREWING);
        recipeHelper.registerContainerClickArea(new Rectangle(78, 32, 28, 23), FurnaceScreen.class, SMELTING);
        recipeHelper.registerContainerClickArea(new Rectangle(78, 32, 28, 23), SmokerScreen.class, SMOKING);
        recipeHelper.registerContainerClickArea(new Rectangle(78, 32, 28, 23), BlastFurnaceScreen.class, BLASTING);
        FluidSupportProvider.getInstance().registerProvider(itemStack -> {
            Item item = itemStack.getItem();
            IFluidHandlerItem fluidHandlerItem = itemStack.getItemStack().getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY).orElse(null);
            if (fluidHandlerItem != null && fluidHandlerItem.getTanks() > 0) {
                List<EntryStack> entryStacks = Lists.newArrayList();
                for (int i = 0; i < fluidHandlerItem.getTanks(); i++) {
                    FluidStack tank = fluidHandlerItem.getFluidInTank(i);
                    if (!tank.isEmpty()) entryStacks.add(EntryStack.create(tank.copy()));
                }
                if (!entryStacks.isEmpty()) return ActionResult.success(entryStacks.stream());
            }
            return ActionResult.pass(null);
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
