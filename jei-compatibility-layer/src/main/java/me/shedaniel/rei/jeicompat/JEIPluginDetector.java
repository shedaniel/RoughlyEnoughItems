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

package me.shedaniel.rei.jeicompat;

import com.google.common.base.MoreObjects;
import com.google.common.base.Predicates;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.architectury.hooks.fluid.forge.FluidStackHooksForge;
import io.netty.buffer.Unpooled;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.config.ConfigObject;
import me.shedaniel.rei.api.client.gui.AbstractRenderer;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.client.registry.display.reason.DisplayAdditionReason;
import me.shedaniel.rei.api.client.registry.entry.EntryRegistry;
import me.shedaniel.rei.api.client.registry.screen.ScreenRegistry;
import me.shedaniel.rei.api.client.registry.transfer.TransferHandlerRegistry;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.display.DisplaySerializer;
import me.shedaniel.rei.api.common.display.DisplaySerializerRegistry;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.comparison.ComparisonContext;
import me.shedaniel.rei.api.common.entry.comparison.FluidComparatorRegistry;
import me.shedaniel.rei.api.common.entry.comparison.ItemComparatorRegistry;
import me.shedaniel.rei.api.common.entry.type.EntryDefinition;
import me.shedaniel.rei.api.common.entry.type.EntryType;
import me.shedaniel.rei.api.common.entry.type.EntryTypeRegistry;
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes;
import me.shedaniel.rei.api.common.plugins.PluginManager;
import me.shedaniel.rei.api.common.plugins.REIPluginProvider;
import me.shedaniel.rei.api.common.registry.ReloadStage;
import me.shedaniel.rei.api.common.registry.Reloadable;
import me.shedaniel.rei.api.common.util.CollectionUtils;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import me.shedaniel.rei.impl.common.InternalLogger;
import me.shedaniel.rei.jeicompat.imitator.JEIInternalsClickedIngredient;
import me.shedaniel.rei.jeicompat.unwrap.JEIUnwrappedCategory;
import me.shedaniel.rei.jeicompat.wrap.*;
import me.shedaniel.rei.plugin.common.BuiltinPlugin;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.forge.ForgeTypes;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.IIngredientTypeWithSubtypes;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.advanced.IRecipeManagerPlugin;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.category.extensions.IRecipeCategoryExtension;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.logging.log4j.util.TriConsumer;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.*;

public class JEIPluginDetector {
    private static final Renderer EMPTY_RENDERER = new Renderer() {
        @Override
        public void render(PoseStack matrices, Rectangle bounds, int mouseX, int mouseY, float delta) {
            
        }
        
        @Override
        public int getZ() {
            return 0;
        }
        
        @Override
        public void setZ(int z) {
            
        }
    };
    
    public static void detect(BiConsumer<Class<?>, TriConsumer<List<String>, Supplier<?>, Class<?>>> annotationScanner, Consumer<REIPluginProvider> pluginAdder) {
        annotationScanner.accept(JeiPlugin.class, (modIds, plugin, clazz) -> {
            Supplier<JEIPluginWrapper> value = () -> new JEIPluginWrapper(modIds, (IModPlugin) plugin.get());
            pluginAdder.accept(new JEIPluginProvider(modIds, value, clazz));
        });
    }
    
    public static RuntimeException TODO() {
        return new UnsupportedOperationException("This operation has not been implemented yet!");
    }
    
    public static RuntimeException WILL_NOT_BE_IMPLEMENTED() {
        return new UnsupportedOperationException("This operation will not be implemented in REI's JEI Compatibility Layer!");
    }
    
    public static Renderer unwrapRenderer(IDrawable drawable) {
        if (drawable == null) return emptyRenderer();
        return new AbstractRenderer() {
            @Override
            public void render(PoseStack matrices, Rectangle bounds, int mouseX, int mouseY, float delta) {
                matrices.pushPose();
                matrices.translate(0, 0, getZ());
                drawable.draw(matrices, bounds.x, bounds.y);
                matrices.popPose();
            }
        };
    }
    
    public static Renderer emptyRenderer() {
        return EMPTY_RENDERER;
    }
    
    public static IRecipeManagerPlugin wrapDefaultRecipeManagerPlugin() {
        return new IRecipeManagerPlugin() {
            @Override
            @NotNull
            public <V> List<ResourceLocation> getRecipeCategoryUids(@NotNull IFocus<V> focus) {
                throw TODO();
            }
            
            @Override
            @NotNull
            public <T, V> List<T> getRecipes(@NotNull IRecipeCategory<T> recipeCategory, @NotNull IFocus<V> focus) {
                throw TODO();
            }
            
            @Override
            @NotNull
            public <T> List<T> getRecipes(@NotNull IRecipeCategory<T> recipeCategory) {
                CategoryIdentifier<Display> categoryId = categoryId(recipeCategory.getRecipeType());
                return wrapRecipes(categoryId, false);
            }
        };
    }
    
    public static <A extends Display, T> List<T> wrapRecipes(CategoryIdentifier<A> id, boolean checkVisible) {
        return wrapRecipes(CategoryRegistry.getInstance().get(id).getCategory(), DisplayRegistry.getInstance().get(id), checkVisible);
    }
    
    public static <A extends Display, T> List<T> wrapRecipes(DisplayCategory<?> category, List<A> displays, boolean checkVisible) {
        boolean isWrappedCategory = category instanceof JEIWrappedCategory;
        if (checkVisible && CategoryRegistry.getInstance().isCategoryInvisible(category)) return new ArrayList<>();
        if (isWrappedCategory) {
            return CollectionUtils.filterAndMap(displays, display -> !checkVisible || DisplayRegistry.getInstance().isDisplayVisible(display),
                    display -> ((JEIWrappedDisplay<T>) display).getBackingRecipe());
        } else if (checkVisible) {
            return (List<T>) CollectionUtils.filterAndMap(displays, display -> DisplayRegistry.getInstance().isDisplayVisible(display),
                    display -> DisplayRegistry.getInstance().getDisplayOrigin(display));
        } else {
            return (List<T>) CollectionUtils.map(displays, display -> DisplayRegistry.getInstance().getDisplayOrigin(display));
        }
    }
    
    public static <A extends Display> Object jeiValue(A display) {
        boolean isWrappedCategory = display instanceof JEIWrappedDisplay;
        if (isWrappedCategory) {
            return ((JEIWrappedDisplay<?>) display).getBackingRecipe();
        } else {
            return MoreObjects.firstNonNull(DisplayRegistry.getInstance().getDisplayOrigin(display), display);
        }
    }
    
    public static <T> Collection<Display> createDisplayFrom(T object) {
        return DisplayRegistry.getInstance().tryFillDisplay(object);
    }
    
    public static IRecipeCategory<?> wrapCategory(DisplayCategory<?> category) {
        if (category instanceof JEIWrappedCategory) {
            return ((JEIWrappedCategory<?>) category).getBackingCategory();
        }
        return new JEIUnwrappedCategory<>(category);
    }
    
    public static <T> T jeiValue(EntryStack<T> stack) {
        T value = stack.getValue();
        if (value instanceof dev.architectury.fluid.FluidStack) {
            return (T) FluidStackHooksForge.toForge((dev.architectury.fluid.FluidStack) value);
        }
        return value;
    }
    
    public static <T> ITypedIngredient<T> typedJeiValue(EntryStack<T> stack) {
        if (stack == null) return null;
        T value = stack.getValue();
        if (value instanceof dev.architectury.fluid.FluidStack) {
            return (ITypedIngredient<T>) new JEITypedIngredient<>(ForgeTypes.FLUID, FluidStackHooksForge.toForge((dev.architectury.fluid.FluidStack) value));
        }
        return new JEITypedIngredient<>(jeiType(stack.getDefinition()), value);
    }
    
    public static <T> Optional<ITypedIngredient<T>> typedJeiValueOp(EntryStack<T> stack) {
        if (stack == null || stack.isEmpty()) return Optional.empty();
        return Optional.ofNullable(typedJeiValue(stack));
    }
    
    public static <T> Optional<ITypedIngredient<?>> typedJeiValueOpWild(EntryStack<T> stack) {
        return (Optional<ITypedIngredient<?>>) (Optional<?>) typedJeiValueOp(stack);
    }
    
    public static <T> T jeiValueOrNull(EntryStack<T> stack) {
        if (stack.isEmpty()) return null;
        return jeiValue(stack);
    }
    
    public static UidContext wrapContext(ComparisonContext context) {
        return context == ComparisonContext.FUZZY ? UidContext.Recipe : UidContext.Ingredient;
    }
    
    public static ComparisonContext unwrapContext(UidContext context) {
        return context == UidContext.Recipe ? ComparisonContext.FUZZY : ComparisonContext.EXACT;
    }
    
    public static final Map<Class<?>, IIngredientType<?>> INGREDIENT_TYPE_MAP = new HashMap<>();
    public static final BiMap<ResourceLocation, CategoryIdentifier<?>> CATEGORY_ID_MAP = HashBiMap.create();
    public static final Map<ResourceLocation, RecipeType<?>> RECIPE_TYPE_MAP = new HashMap<>();
    
    static {
        INGREDIENT_TYPE_MAP.put(ItemStack.class, makeJeiTypeWithSubtype(ItemStack.class, Item.class, ItemStack::getItem));
        INGREDIENT_TYPE_MAP.put(FluidStack.class, makeJeiTypeWithSubtype(FluidStack.class, Fluid.class, FluidStack::getFluid));
        CATEGORY_ID_MAP.put(new ResourceLocation("minecraft", "crafting"), BuiltinPlugin.CRAFTING);
        CATEGORY_ID_MAP.put(new ResourceLocation("minecraft", "stonecutting"), BuiltinPlugin.STONE_CUTTING);
        CATEGORY_ID_MAP.put(new ResourceLocation("minecraft", "furnace"), BuiltinPlugin.SMELTING);
        CATEGORY_ID_MAP.put(new ResourceLocation("minecraft", "smoking"), BuiltinPlugin.SMOKING);
        CATEGORY_ID_MAP.put(new ResourceLocation("minecraft", "blasting"), BuiltinPlugin.BLASTING);
        CATEGORY_ID_MAP.put(new ResourceLocation("minecraft", "campfire"), BuiltinPlugin.CAMPFIRE);
        CATEGORY_ID_MAP.put(new ResourceLocation("minecraft", "brewing"), BuiltinPlugin.BREWING);
        CATEGORY_ID_MAP.put(new ResourceLocation("minecraft", "anvil"), BuiltinPlugin.ANVIL);
        CATEGORY_ID_MAP.put(new ResourceLocation("minecraft", "smithing"), BuiltinPlugin.SMITHING);
        CATEGORY_ID_MAP.put(new ResourceLocation("minecraft", "compostable"), BuiltinPlugin.COMPOSTING);
        CATEGORY_ID_MAP.put(new ResourceLocation("minecraft", "information"), BuiltinPlugin.INFO);
    }
    
    public static <B, T> IIngredientTypeWithSubtypes<B, T> makeJeiTypeWithSubtype(Class<? extends T> c, Class<? extends B> bc, Function<T, B> mapper) {
        return new IIngredientTypeWithSubtypes<B, T>() {
            @Override
            public Class<? extends T> getIngredientClass() {
                return c;
            }
            
            @Override
            public Class<? extends B> getIngredientBaseClass() {
                return bc;
            }
            
            @Override
            public B getBase(T ingredient) {
                return mapper.apply(ingredient);
            }
        };
    }
    
    public static <T> IIngredientType<T> jeiType(EntryDefinition<T> definition) {
        return jeiType(definition.getValueType());
    }
    
    public static <T> IIngredientType<T> jeiType(Class<? extends T> c) {
        IIngredientType<T> existingType = (IIngredientType<T>) INGREDIENT_TYPE_MAP.get(c);
        if (existingType != null) return existingType;
        IIngredientType<T> type = () -> c;
        INGREDIENT_TYPE_MAP.put(c, type);
        return type;
    }
    
    public static <T extends Display, R> CategoryIdentifier<T> categoryId(RecipeType<R> id) {
        return categoryId(id.getUid());
    }
    
    public static <T extends Display, R> RecipeType<R> asRecipeType(CategoryIdentifier<T> id, Class<? extends R> recipeType) {
        return asRecipeType(id.getIdentifier(), recipeType);
    }
    
    public static <R> RecipeType<R> asRecipeType(ResourceLocation id, Class<? extends R> recipeType) {
        RecipeType<R> existingType = (RecipeType<R>) RECIPE_TYPE_MAP.get(id);
        if (existingType != null) return existingType;
        RecipeType<R> type = new RecipeType<>(id, recipeType);
        RECIPE_TYPE_MAP.putIfAbsent(id, type);
        return type;
    }
    
    public static <T extends Display> CategoryIdentifier<T> categoryId(ResourceLocation id) {
        CategoryIdentifier<?> existingId = CATEGORY_ID_MAP.get(id);
        if (existingId != null) return existingId.cast();
        CategoryIdentifier<T> identifier = CategoryIdentifier.of(id);
        CATEGORY_ID_MAP.forcePut(id, identifier);
        return identifier;
    }
    
    public static <T> EntryStack<T> unwrapStack(T stack, IIngredientType<T> type) {
        return unwrapStack(stack, unwrapType(type).getDefinition());
    }
    
    public static <T> EntryStack<T> unwrapStack(T stack, EntryDefinition<T> definition) {
        if (stack == null) return EntryStack.empty().cast();
        if (definition.getType() == VanillaEntryTypes.FLUID)
            return EntryStack.of(definition, (T) FluidStackHooksForge.fromForge((FluidStack) stack));
        return EntryStack.of(definition, stack);
    }
    
    public static <T> EntryIngredient unwrapList(IIngredientType<T> type, List<T> stack) {
        return unwrapList(unwrapType(type).getDefinition(), stack);
    }
    
    public static <T> EntryIngredient unwrapList(EntryDefinition<T> definition, List<T> stack) {
        if (definition.getType() == VanillaEntryTypes.FLUID)
            return EntryIngredients.of(definition, CollectionUtils.filterAndMap(stack, Predicates.notNull(), s -> (T) FluidStackHooksForge.fromForge((FluidStack) s)));
        return EntryIngredients.of(definition, stack);
    }
    
    public static <T> EntryType<T> unwrapType(IIngredientType<T> type) {
        if (type.getIngredientClass() == FluidStack.class) {
            return VanillaEntryTypes.FLUID.cast();
        }
        for (EntryDefinition<?> definition : EntryTypeRegistry.getInstance().values()) {
            if (Objects.equals(definition.getValueType(), type.getIngredientClass())) {
                return definition.getType().cast();
            }
        }
        throw new IllegalArgumentException("Unknown JEI Ingredient Type! " + type.getIngredientClass().getName());
    }
    
    public static <T> EntryDefinition<T> unwrapDefinition(IIngredientType<T> type) {
        return unwrapType(type).getDefinition();
    }
    
    public static EntryStack<?> unwrapStack(Object stack) {
        if (stack instanceof ITypedIngredient) {
            return unwrapStack(((ITypedIngredient<?>) stack).getIngredient(), unwrapDefinition(((ITypedIngredient<?>) stack).getType()).cast());
        }
        if (stack instanceof JEIInternalsClickedIngredient) {
            return unwrapStack(((JEIInternalsClickedIngredient<?>) stack).getValue());
        }
        return unwrapStack(stack, unwrapDefinition(stack).cast());
    }
    
    public static EntryType<?> unwrapType(Object stack) {
        if (stack instanceof ItemStack) {
            return VanillaEntryTypes.ITEM;
        } else if (stack instanceof FluidStack) {
            return VanillaEntryTypes.FLUID;
        }
        for (EntryDefinition<?> definition : EntryTypeRegistry.getInstance().values()) {
            if (definition.getValueType().isInstance(stack)) {
                return definition.cast().getType();
            }
        }
        throw new IllegalArgumentException("Failed to find EntryType of " + stack + "!");
    }
    
    public static EntryDefinition<?> unwrapDefinition(Object stack) {
        return unwrapType(stack).getDefinition();
    }
    
    public static IRecipeCategoryRegistration wrapCategoryRegistration(CategoryRegistry registry, Consumer<JEIWrappedCategory<?>> added) {
        return new IRecipeCategoryRegistration() {
            @Override
            @NotNull
            public IJeiHelpers getJeiHelpers() {
                return JEIJeiHelpers.INSTANCE;
            }
            
            @Override
            public void addRecipeCategories(@NotNull IRecipeCategory<?> @NotNull ... categories) {
                for (IRecipeCategory<?> category : categories) {
                    JEIWrappedCategory<?> wrappedCategory = new JEIWrappedCategory<>(category);
                    registry.add(wrappedCategory);
                    added.accept(wrappedCategory);
                }
            }
        };
    }
    
    public static class JEIPluginProvider implements REIPluginProvider<REIClientPlugin> {
        public final List<String> modIds;
        public final Supplier<JEIPluginWrapper> supplier;
        private final Class<?> clazz;
        public JEIPluginWrapper wrapper;
        private String nameSuffix;
        
        public JEIPluginProvider(List<String> modIds, Supplier<JEIPluginWrapper> supplier, Class<?> clazz) {
            this.modIds = modIds;
            this.supplier = supplier;
            this.clazz = clazz;
            this.nameSuffix = " [" + String.join(", ", modIds) + "]";
        }
        
        @Override
        public String getPluginProviderName() {
            if (modIds.contains("recipestages")) {
                if (wrapper == null) {
                    wrapper = supplier.get();
                }
            }
            
            String strSuffix = nameSuffix;
            
            if (!ConfigObject.getInstance().isJEICompatibilityLayerEnabled()) {
                strSuffix = " {DISABLED}";
            }
            
            if (wrapper != null) {
                return wrapper.getPluginProviderName() + strSuffix;
            }
            
            String simpleName = clazz.getSimpleName();
            simpleName = simpleName == null ? clazz.getName() : simpleName;
            return "JEI Plugin [" + simpleName + "]" + strSuffix;
        }
        
        @Override
        public Collection<REIClientPlugin> provide() {
            if (ConfigObject.getInstance().isJEICompatibilityLayerEnabled()) {
                if (wrapper != null) {
                    return Collections.singletonList(wrapper);
                } else {
                    return Collections.singletonList(wrapper = supplier.get());
                }
            }
            
            return Collections.emptyList();
        }
        
        @Override
        public Class<REIClientPlugin> getPluginProviderClass() {
            return REIClientPlugin.class;
        }
    }
    
    public static class JEIPluginWrapper implements REIClientPlugin {
        public final boolean mainThread;
        public final boolean forceRuntime;
        public final IModPlugin backingPlugin;
        
        public final Map<DisplayCategory<?>, List<Triple<Class<?>, Predicate<Object>, Function<Object, IRecipeCategoryExtension>>>> categories = new HashMap<>();
        public final List<Runnable> entryRegistry = new ArrayList<>();
        public final List<Runnable> post = new ArrayList<>();
        
        public JEIPluginWrapper(List<String> modIds, IModPlugin backingPlugin) {
            this.backingPlugin = backingPlugin;
            // why are you reloading twice
            this.mainThread = CollectionUtils.anyMatch(Arrays.asList("jeresources", "jepb"), modIds::contains);
            this.forceRuntime = CollectionUtils.anyMatch(Arrays.asList("recipestages"), modIds::contains);
            
            if (forceRuntime) {
                backingPlugin.onRuntimeAvailable(JEIJeiRuntime.INSTANCE);
            }
        }
        
        @Override
        public void registerEntryTypes(EntryTypeRegistry registry) {
            backingPlugin.registerIngredients(new JEIModIngredientRegistration(this, registry));
        }
        
        @Override
        public void registerEntries(EntryRegistry registry) {
            for (Runnable runnable : entryRegistry) {
                runnable.run();
            }
            
            entryRegistry.clear();
        }
        
        @Override
        public void registerItemComparators(ItemComparatorRegistry registry) {
            backingPlugin.registerItemSubtypes(JEISubtypeRegistration.INSTANCE);
        }
        
        @Override
        public void registerFluidComparators(FluidComparatorRegistry registry) {
            backingPlugin.registerFluidSubtypes(JEISubtypeRegistration.INSTANCE);
        }
        
        @Override
        public void registerCategories(CategoryRegistry registry) {
            this.categories.clear();
            backingPlugin.registerCategories(wrapCategoryRegistration(registry, category -> {
                categories.put(category, new ArrayList<>());
                
                DisplayRegistry.getInstance().registerFiller((Class<Object>) category.getRecipeClass(), (o, reason) ->
                                ((JEIWrappedCategory<Object>) category).handlesRecipe(o) && !reason.has(DisplayAdditionReason.RECIPE_MANAGER),
                        recipe -> {
                            return new JEIWrappedDisplay<>((JEIWrappedCategory<Object>) category, recipe);
                        });
                DisplayRegistry.getInstance().registerFiller(JEIWrappedDisplay.class, display -> display.getCategoryIdentifier().getIdentifier().equals(category.getIdentifier()), Function.identity());
                
                post.add(() -> {
                    if (Recipe.class.isAssignableFrom(category.getRecipeClass())) {
                        DisplaySerializerRegistry.getInstance().register(category.getCategoryIdentifier(), new DisplaySerializer<JEIWrappedDisplay<?>>() {
                            @Override
                            public CompoundTag save(CompoundTag tag, JEIWrappedDisplay<?> display) {
                                Recipe<?> recipe = (Recipe<?>) display.getBackingRecipe();
                                FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
                                ((RecipeSerializer<Recipe<?>>) recipe.getSerializer()).toNetwork(buf, recipe);
                                tag.putString("serializer", Registry.RECIPE_SERIALIZER.getKey(recipe.getSerializer()).toString());
                                tag.putString("data", Base64.encodeBase64String(buf.array()));
                                tag.putString("id", recipe.getId().toString());
                                return tag;
                            }
                            
                            @Override
                            public JEIWrappedDisplay<?> read(CompoundTag tag) {
                                RecipeSerializer<?> serializer = Registry.RECIPE_SERIALIZER.get(new ResourceLocation(tag.getString("serializer")));
                                FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.copiedBuffer(Base64.decodeBase64(tag.getString("data"))));
                                Recipe<?> recipe = serializer.fromNetwork(new ResourceLocation(tag.getString("id")), buf);
                                return new JEIWrappedDisplay<>((JEIWrappedCategory<? super Recipe<?>>) category, recipe);
                            }
                            
                            @Override
                            public boolean isPersistent() {
                                return false;
                            }
                        });
                    } else {
                        DisplaySerializerRegistry.getInstance().registerNotSerializable(category.getCategoryIdentifier());
                    }
                });
            }));
            backingPlugin.registerVanillaCategoryExtensions(new JEIVanillaCategoryExtensionRegistration(this));
            if (!registry.getVisibilityPredicates().contains(JEIRecipeManager.INSTANCE.categoryPredicate)) {
                registry.registerVisibilityPredicate(JEIRecipeManager.INSTANCE.categoryPredicate);
            }
        }
        
        @Override
        public void registerDisplays(DisplayRegistry registry) {
            backingPlugin.registerRecipes(new JEIRecipeRegistration(post));
            backingPlugin.registerAdvanced(JEIAdvancedRegistration.INSTANCE);
            if (!registry.getVisibilityPredicates().contains(JEIRecipeManager.INSTANCE.displayPredicate)) {
                registry.registerVisibilityPredicate(JEIRecipeManager.INSTANCE.displayPredicate);
            }
            backingPlugin.registerRecipeCatalysts(JEIRecipeCatalystRegistration.INSTANCE);
        }
        
        @Override
        public void registerScreens(ScreenRegistry registry) {
            backingPlugin.registerGuiHandlers(JEIGuiHandlerRegistration.INSTANCE);
        }
        
        @Override
        public void registerTransferHandlers(TransferHandlerRegistry registry) {
            backingPlugin.registerRecipeTransferHandlers(new JEIRecipeTransferRegistration(post::add));
        }
        
        @Override
        public void postStage(PluginManager<REIClientPlugin> manager, ReloadStage stage) {
            if (stage == ReloadStage.END && Objects.equals(manager, PluginManager.getClientInstance())) {
                InternalLogger.getInstance().debug("Running post-register for %s with %d post tasks", getPluginProviderName(), post.size());
                for (Map.Entry<DisplayCategory<?>, List<Triple<Class<?>, Predicate<Object>, Function<Object, IRecipeCategoryExtension>>>> entry : categories.entrySet()) {
                    DisplayCategory<?> category = entry.getKey();
                    for (Triple<Class<?>, Predicate<Object>, Function<Object, IRecipeCategoryExtension>> pair : entry.getValue()) {
//                    DisplayRegistry.getInstance().registerFiller(pair.getLeft(), pair.getMiddle(), );
                    }
                }
                if (!forceRuntime) {
                    backingPlugin.onRuntimeAvailable(JEIJeiRuntime.INSTANCE);
                }
                for (Runnable runnable : entryRegistry) {
                    try {
                        runnable.run();
                    } catch (Throwable throwable) {
                        throwable.printStackTrace();
                    }
                }
                for (Runnable runnable : post) {
                    try {
                        runnable.run();
                    } catch (Throwable throwable) {
                        throwable.printStackTrace();
                    }
                }
                
                entryRegistry.clear();
                post.clear();
            }
        }
        
        @Override
        public String getPluginProviderName() {
            Class<?> pluginClass = backingPlugin.getClass();
            String simpleName = pluginClass.getSimpleName();
            simpleName = simpleName == null ? pluginClass.getName() : simpleName;
            return "JEI Plugin [" + simpleName + ":" + backingPlugin.getPluginUid() + "]";
        }
        
        @Override
        public boolean shouldBeForcefullyDoneOnMainThread(Reloadable<?> reloadable) {
            if (!mainThread) {
                return false;
            } else return reloadable instanceof CategoryRegistry ||
                          reloadable instanceof DisplayRegistry ||
                          reloadable instanceof ScreenRegistry;
        }
    }
}
