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

package me.shedaniel.rei.jeicompat;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import me.shedaniel.architectury.hooks.forge.FluidStackHooksForge;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.ClientHelper;
import me.shedaniel.rei.api.REIHelper;
import me.shedaniel.rei.api.config.ConfigObject;
import me.shedaniel.rei.api.gui.AbstractRenderer;
import me.shedaniel.rei.api.gui.Renderer;
import me.shedaniel.rei.api.ingredient.EntryIngredient;
import me.shedaniel.rei.api.ingredient.EntryStack;
import me.shedaniel.rei.api.ingredient.entry.comparison.ComparisonContext;
import me.shedaniel.rei.api.ingredient.entry.comparison.ItemComparator;
import me.shedaniel.rei.api.ingredient.entry.comparison.ItemComparatorRegistry;
import me.shedaniel.rei.api.ingredient.entry.type.EntryDefinition;
import me.shedaniel.rei.api.ingredient.entry.type.EntryType;
import me.shedaniel.rei.api.ingredient.entry.type.EntryTypeRegistry;
import me.shedaniel.rei.api.ingredient.entry.type.VanillaEntryTypes;
import me.shedaniel.rei.api.ingredient.util.EntryIngredients;
import me.shedaniel.rei.api.ingredient.util.EntryStacks;
import me.shedaniel.rei.api.plugins.BuiltinPlugin;
import me.shedaniel.rei.api.plugins.REIPlugin;
import me.shedaniel.rei.api.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.registry.display.Display;
import me.shedaniel.rei.api.registry.display.DisplayCategory;
import me.shedaniel.rei.api.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.registry.display.LiveDisplayGenerator;
import me.shedaniel.rei.api.registry.entry.EntryRegistry;
import me.shedaniel.rei.api.registry.screen.DisplayBoundsProvider;
import me.shedaniel.rei.api.registry.screen.ScreenRegistry;
import me.shedaniel.rei.api.util.CollectionUtils;
import me.shedaniel.rei.api.util.ImmutableLiteralText;
import me.shedaniel.rei.jeicompat.unwrap.JEIUnwrappedCategory;
import me.shedaniel.rei.jeicompat.wrap.JEIEntryDefinition;
import me.shedaniel.rei.jeicompat.wrap.JEIWrappedCategory;
import me.shedaniel.rei.jeicompat.wrap.JEIWrappedDisplay;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.handlers.*;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.helpers.IModIdHelper;
import mezz.jei.api.helpers.IStackHelper;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.ISubtypeManager;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.advanced.IRecipeManagerPlugin;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.vanilla.IVanillaRecipeFactory;
import mezz.jei.api.registration.*;
import mezz.jei.api.runtime.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.data.models.blockstates.PropertyDispatch;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

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
    
    public static void detect(BiConsumer<Class<?>, Consumer<?>> annotationScanner, Consumer<REIPlugin> pluginAdder) {
        annotationScanner.accept(JeiPlugin.class, plugin -> {
            pluginAdder.accept(new JEIPluginWrapper((IModPlugin) plugin));
        });
    }
    
    public static RuntimeException TODO() {
        return new UnsupportedOperationException("This operation has not been implemented yet!");
    }
    
    public static IJeiHelpers wrapJEIHelpers() {
        return new IJeiHelpers() {
            @Override
            @NotNull
            public IGuiHelper getGuiHelper() {
                return JEIGuiHelper.INSTANCE;
            }
            
            @Override
            @NotNull
            public IStackHelper getStackHelper() {
                return wrapStackHelper();
            }
            
            @Override
            @NotNull
            public IModIdHelper getModIdHelper() {
                return wrapModIdHelper(ClientHelper.getInstance());
            }
        };
    }
    
    public static IJeiRuntime wrapRuntime() {
        return new IJeiRuntime() {
            @Override
            @NotNull
            public IRecipeManager getRecipeManager() {
                throw TODO();
            }
            
            @Override
            @NotNull
            public IRecipesGui getRecipesGui() {
                throw TODO();
            }
            
            @Override
            @NotNull
            public IIngredientFilter getIngredientFilter() {
                return wrapIngredientFilter();
            }
            
            @Override
            @NotNull
            public IIngredientListOverlay getIngredientListOverlay() {
                throw TODO();
            }
            
            @Override
            @NotNull
            public IBookmarkOverlay getBookmarkOverlay() {
                throw TODO();
            }
            
            @Override
            @NotNull
            public IIngredientManager getIngredientManager() {
                throw TODO();
            }
        };
    }
    
    public static IIngredientFilter wrapIngredientFilter() {
        return new IIngredientFilter() {
            @Override
            public void setFilterText(@NotNull String filterText) {
                REIHelper.getInstance().getSearchTextField().setText(filterText);
            }
            
            @Override
            @NotNull
            public String getFilterText() {
                return REIHelper.getInstance().getSearchTextField().getText();
            }
            
            @Override
            @NotNull
            public ImmutableList<Object> getFilteredIngredients() {
                throw TODO();
            }
        };
    }
    
    public static Renderer wrapDrawable(IDrawable drawable) {
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
    
    public static IStackHelper wrapStackHelper() {
        return (lhs, rhs, context) -> {
            if (context == UidContext.Ingredient) {
                return EntryStacks.equalsExact(wrap(lhs), wrap(rhs));
            }
            return EntryStacks.equalsFuzzy(wrap(lhs), wrap(rhs));
        };
    }
    
    public static IModIdHelper wrapModIdHelper(ClientHelper helper) {
        return new IModIdHelper() {
            @Override
            @NotNull
            public String getModNameForModId(@NotNull String modId) {
                return helper.getModFromModId(modId);
            }
            
            @Override
            public boolean isDisplayingModNameEnabled() {
                return ConfigObject.getInstance().shouldAppendModNames();
            }
            
            @Override
            @NotNull
            public String getFormattedModNameForModId(@NotNull String modId) {
                String name = getModNameForModId(modId);
                if (name.isEmpty()) return name;
                return "§9§o" + name;
            }
            
            @Override
            @NotNull
            public <T> List<Component> addModNameToIngredientTooltip(@NotNull List<Component> tooltip, @NotNull T ingredient, @NotNull IIngredientHelper<T> ingredientHelper) {
                Optional<ResourceLocation> identifier = wrap(ingredient).getIdentifier();
                identifier.ifPresent(location -> helper.appendModIdToTooltips(tooltip, location.getNamespace()));
                return tooltip;
            }
        };
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
                boolean isWrappedCategory = CategoryRegistry.getInstance().get(recipeCategory.getUid()).getCategory() instanceof JEIWrappedCategory;
                List<Display> displays = DisplayRegistry.getInstance().getDisplays(recipeCategory.getUid());
                if (isWrappedCategory) {
                    return CollectionUtils.map(displays, display -> ((JEIWrappedDisplay<T>) display).getBackingRecipe());
                } else {
                    return (List<T>) displays;
                }
            }
        };
    }
    
    public static IAdvancedRegistration wrapAdvancedRegistration(DisplayRegistry registry) {
        return new IAdvancedRegistration() {
            @Override
            @NotNull
            public IJeiHelpers getJeiHelpers() {
                return wrapJEIHelpers();
            }
            
            @Override
            public void addRecipeManagerPlugin(@NotNull IRecipeManagerPlugin plugin) {
                registry.registerGlobalDisplayGenerator(wrapLiveDisplayGenerator(plugin));
            }
        };
    }
    
    public static LiveDisplayGenerator<Display> wrapLiveDisplayGenerator(IRecipeManagerPlugin plugin) {
        return new LiveDisplayGenerator<Display>() {
            private Optional<List<Display>> getDisplays(EntryStack<?> entry, IFocus.Mode mode) {
                JEIFocus<?> focus = new JEIFocus<>(mode, unwrap(entry));
                List<ResourceLocation> categoryIds = plugin.getRecipeCategoryUids(focus);
                if (categoryIds.isEmpty()) {
                    return Optional.empty();
                }
                List<Display> displays = null;
                for (ResourceLocation categoryId : categoryIds) {
                    IRecipeCategory<Object> category = (IRecipeCategory<Object>) unwrapCategory(CategoryRegistry.getInstance().get(categoryId).getCategory());
                    List<Object> recipes = plugin.getRecipes(category, focus);
                    if (recipes != null && !recipes.isEmpty()) {
                        if (displays == null) displays = CollectionUtils.map(recipes, JEIPluginDetector::createDisplayFrom);
                        else displays.addAll(CollectionUtils.map(recipes, JEIPluginDetector::createDisplayFrom));
                    }
                    recipes = plugin.getRecipes(category);
                    if (recipes != null && !recipes.isEmpty()) {
                        if (displays == null) displays = new ArrayList<>(CollectionUtils.map(recipes, JEIPluginDetector::createDisplayFrom));
                        else displays.addAll(CollectionUtils.map(recipes, JEIPluginDetector::createDisplayFrom));
                    }
                }
                return Optional.ofNullable(CollectionUtils.filterToList(displays, Objects::nonNull));
            }
            
            @Override
            public Optional<List<Display>> getRecipeFor(EntryStack<?> entry) {
                return getDisplays(entry, IFocus.Mode.OUTPUT);
            }
            
            @Override
            public Optional<List<Display>> getUsageFor(EntryStack<?> entry) {
                return getDisplays(entry, IFocus.Mode.INPUT);
            }
        };
    }
    
    private static <T> Display createDisplayFrom(T object) {
        return DisplayRegistry.getInstance().tryFillDisplay(object);
    }
    
    public static IRecipeCategory<?> unwrapCategory(DisplayCategory<?> category) {
        if (category instanceof JEIWrappedCategory) {
            return ((JEIWrappedCategory<?>) category).getBackingCategory();
        }
        return new JEIUnwrappedCategory<>(category);
    }
    
    public static <T> T unwrap(EntryStack<T> stack) {
        T value = stack.getValue();
        if (value instanceof me.shedaniel.architectury.fluid.FluidStack) {
            return (T) FluidStackHooksForge.toForge((me.shedaniel.architectury.fluid.FluidStack) value);
        }
        return value;
    }
    
    public static <T> EntryStack<T> wrap(IIngredientType<T> type, T stack) {
        EntryDefinition<T> definition = wrapEntryDefinition(type);
        if (definition.getType() == VanillaEntryTypes.FLUID)
            return EntryStack.of(definition, (T) FluidStackHooksForge.fromForge((FluidStack) stack));
        return EntryStack.of(definition, stack);
    }
    
    public static <T> EntryIngredient wrapList(IIngredientType<T> type, List<T> stack) {
        EntryDefinition<T> definition = wrapEntryDefinition(type);
        if (definition.getType() == VanillaEntryTypes.FLUID)
            return EntryIngredients.of(definition, CollectionUtils.map(stack, s -> (T) FluidStackHooksForge.fromForge((FluidStack) s)));
        return EntryIngredients.of(definition, stack);
    }
    
    public static <T> EntryDefinition<T> wrapEntryDefinition(IIngredientType<T> type) {
        if (type.getIngredientClass() == FluidStack.class) {
            return VanillaEntryTypes.FLUID.getDefinition().cast();
        }
        for (EntryDefinition<?> definition : EntryTypeRegistry.getInstance().values()) {
            if (Objects.equals(definition.getValueType(), type.getIngredientClass())) {
                return definition.cast();
            }
        }
        throw new IllegalArgumentException("Unknown JEI Ingredient Type! " + type.getIngredientClass().getName());
    }
    
    public static EntryStack<?> wrap(Object stack) {
        if (stack instanceof ItemStack) {
            return EntryStacks.of((ItemStack) stack);
        } else if (stack instanceof FluidStack) {
            return EntryStacks.of(FluidStackHooksForge.fromForge((FluidStack) stack));
        }
        for (EntryDefinition<?> definition : EntryTypeRegistry.getInstance().values()) {
            if (definition.getValueType().isInstance(stack)) {
                return EntryStack.of((EntryDefinition<? super Object>) definition, stack);
            }
        }
        throw new IllegalArgumentException("Failed to wrap EntryStack!");
    }
    
    public static IRecipeCategoryRegistration wrapCategoryRegistration(CategoryRegistry registry, Consumer<JEIWrappedCategory<?>> added) {
        return new IRecipeCategoryRegistration() {
            @Override
            @NotNull
            public IJeiHelpers getJeiHelpers() {
                return wrapJEIHelpers();
            }
            
            @Override
            public void addRecipeCategories(@NotNull IRecipeCategory<?> @NotNull ... categories) {
                for (IRecipeCategory<?> category : categories) {
                    JEIWrappedCategory<?> wrappedCategory = new JEIWrappedCategory<>(category);
                    registry.register(wrappedCategory);
                    added.accept(wrappedCategory);
                }
            }
        };
    }
    
    public static IRecipeCatalystRegistration wrapCatalyst(CategoryRegistry registry) {
        return (ingredient, categoryIds) -> {
            for (ResourceLocation id : categoryIds) {
                registry.addWorkstations(id, wrap(ingredient));
            }
        };
    }
    
    public static IIngredientManager wrapIngredientManager() {
        return new IIngredientManager() {
            @Override
            @NotNull
            public <V> Collection<V> getAllIngredients(@NotNull IIngredientType<V> ingredientType) {
                EntryDefinition<V> definition = wrapEntryDefinition(ingredientType);
                return EntryRegistry.getInstance().getEntryStacks()
                        .filter(stack -> Objects.equals(stack.getDefinition(), definition))
                        .<EntryStack<V>>map(EntryStack::cast)
                        .map(EntryStack::getValue)
                        .collect(Collectors.toList());
            }
            
            @Override
            @NotNull
            public <V> IIngredientHelper<V> getIngredientHelper(@NotNull V ingredient) {
                throw TODO();
            }
            
            @Override
            @NotNull
            public <V> IIngredientHelper<V> getIngredientHelper(@NotNull IIngredientType<V> ingredientType) {
                throw TODO();
            }
            
            @Override
            @NotNull
            public <V> IIngredientRenderer<V> getIngredientRenderer(@NotNull V ingredient) {
                throw TODO();
            }
            
            @Override
            @NotNull
            public <V> IIngredientRenderer<V> getIngredientRenderer(@NotNull IIngredientType<V> ingredientType) {
                throw TODO();
            }
            
            @Override
            @NotNull
            public Collection<IIngredientType<?>> getRegisteredIngredientTypes() {
                return CollectionUtils.map(EntryTypeRegistry.getInstance().values(), definition -> definition::getValueType);
            }
            
            @Override
            public <V> void addIngredientsAtRuntime(@NotNull IIngredientType<V> ingredientType, @NotNull Collection<V> ingredients) {
                throw TODO();
            }
            
            @Override
            public <V> void removeIngredientsAtRuntime(@NotNull IIngredientType<V> ingredientType, @NotNull Collection<V> ingredients) {
                throw TODO();
            }
            
            @Override
            @NotNull
            public <V> IIngredientType<V> getIngredientType(@NotNull V ingredient) {
                return wrap(ingredient).getDefinition().<V>cast()::getValueType;
            }
            
            @Override
            @NotNull
            public <V> IIngredientType<V> getIngredientType(@NotNull Class<? extends V> ingredientClass) {
                return () -> ingredientClass;
            }
        };
    }
    
    public static IRecipeRegistration wrapRecipeRegistration(DisplayRegistry registry) {
        return new IRecipeRegistration() {
            @Override
            @NotNull
            public IJeiHelpers getJeiHelpers() {
                return wrapJEIHelpers();
            }
            
            @Override
            @NotNull
            public IIngredientManager getIngredientManager() {
                return wrapIngredientManager();
            }
            
            @Override
            @NotNull
            public IVanillaRecipeFactory getVanillaRecipeFactory() {
                throw TODO();
            }
            
            @Override
            public void addRecipes(@NotNull Collection<?> recipes, @NotNull ResourceLocation categoryId) {
                CategoryRegistry.CategoryConfiguration<Display> config = CategoryRegistry.getInstance().get(categoryId);
                DisplayCategory<?> category = config.getCategory();
                if (category instanceof JEIWrappedCategory) {
                    for (Object recipe : recipes) {
                        registry.registerDisplay(new JEIWrappedDisplay<>((JEIWrappedCategory) config.getCategory(), recipe));
                    }
                } else {
                    for (Object recipe : recipes) {
                        registry.registerDisplay(createDisplayFrom(recipe));
                    }
                }
            }
            
            @Override
            public <T> void addIngredientInfo(@NotNull T ingredient, @NotNull IIngredientType<T> ingredientType, @NotNull String @NotNull ... descriptionKeys) {
                EntryStack<T> stack = wrap(ingredientType, ingredient);
                BuiltinPlugin.getInstance().registerInformation(stack, stack.asFormattedText(), components -> {
                    for (String key : descriptionKeys) {
                        components.add(new TranslatableComponent(key));
                    }
                    return components;
                });
            }
            
            @Override
            public <T> void addIngredientInfo(@NotNull List<T> ingredients, @NotNull IIngredientType<T> ingredientType, @NotNull String @NotNull ... descriptionKeys) {
                EntryIngredient ingredient = wrapList(ingredientType, ingredients);
                BuiltinPlugin.getInstance().registerInformation(ingredient, ImmutableLiteralText.EMPTY, components -> {
                    for (String key : descriptionKeys) {
                        components.add(new TranslatableComponent(key));
                    }
                    return components;
                });
            }
        };
    }
    
    public static IGuiHandlerRegistration wrapGuiHandlerRegistration(ScreenRegistry registry) {
        return new IGuiHandlerRegistration() {
            @Override
            public <T extends AbstractContainerScreen<?>> void addGuiContainerHandler(@NotNull Class<? extends T> guiClass, @NotNull IGuiContainerHandler<T> guiHandler) {
                this.<T>add(guiClass, guiHandler::getGuiExtraAreas, guiHandler::getIngredientUnderMouse);
            }
            
            @Override
            public <T extends AbstractContainerScreen<?>> void addGenericGuiContainerHandler(@NotNull Class<? extends T> guiClass, @NotNull IGuiContainerHandler<?> guiHandler) {
                addGuiContainerHandler(guiClass, (IGuiContainerHandler<T>) guiHandler);
            }
            
            @Override
            public <T extends Screen> void addGuiScreenHandler(@NotNull Class<T> guiClass, @NotNull IScreenHandler<T> handler) {
                registry.registerDecider(new DisplayBoundsProvider<T>() {
                    @Override
                    public <R extends Screen> boolean isHandingScreen(Class<R> screen) {
                        return guiClass.isAssignableFrom(screen);
                    }
                    
                    @Override
                    public Rectangle getScreenBounds(T screen) {
                        IGuiProperties properties = handler.apply(screen);
                        return new Rectangle(properties.getGuiLeft(), properties.getGuiTop(), properties.getGuiXSize(), properties.getGuiYSize());
                    }
                });
            }
            
            @Override
            public void addGlobalGuiHandler(@NotNull IGlobalGuiHandler globalGuiHandler) {
                add(Screen.class, screen -> globalGuiHandler.getGuiExtraAreas(),
                        (screen, mouseX, mouseY) -> globalGuiHandler.getIngredientUnderMouse(mouseX, mouseY));
            }
            
            private <T extends Screen> void add(Class<? extends T> screenClass, Function<T, Collection<Rect2i>> exclusionZones, PropertyDispatch.TriFunction<T, Double, Double, Object> focusedStack) {
                registry.exclusionZones().register(screenClass, screen -> {
                    return CollectionUtils.map(exclusionZones.apply(screen), rect2i -> new Rectangle(rect2i.getX(), rect2i.getY(), rect2i.getWidth(), rect2i.getHeight()));
                });
                registry.registerFocusedStack((screen, mouse) -> {
                    if (!screenClass.isInstance(screen)) return InteractionResultHolder.pass(EntryStack.empty());
                    Object ingredient = focusedStack.apply((T) screen, (double) mouse.x, (double) mouse.y);
                    if (ingredient == null) return InteractionResultHolder.pass(EntryStack.empty());
                    
                    return InteractionResultHolder.success(wrap(ingredient));
                });
            }
            
            @Override
            public <T extends AbstractContainerScreen<?>> void addRecipeClickArea(@NotNull Class<? extends T> guiContainerClass, int xPos, int yPos, int width, int height, @NotNull ResourceLocation @NotNull ... recipeCategoryUids) {
                registry.registerContainerClickArea(new Rectangle(xPos, yPos, width, height), (Class<? extends AbstractContainerScreen<AbstractContainerMenu>>) guiContainerClass,
                        (ResourceLocation[]) recipeCategoryUids);
            }
            
            @Override
            public <T extends Screen> void addGhostIngredientHandler(@NotNull Class<T> guiClass, @NotNull IGhostIngredientHandler<T> handler) {
                throw TODO();
            }
        };
    }
    
    public static IModIngredientRegistration wrapModIngredientRegistration(JEIPluginWrapper wrapper, EntryTypeRegistry registry) {
        return new IModIngredientRegistration() {
            @Override
            @NotNull
            public ISubtypeManager getSubtypeManager() {
                throw TODO();
            }
            
            @Override
            public <V> void register(@NotNull IIngredientType<V> ingredientType, @NotNull Collection<V> allIngredients, @NotNull IIngredientHelper<V> ingredientHelper,
                    @NotNull IIngredientRenderer<V> ingredientRenderer) {
                ResourceLocation location = new ResourceLocation(wrapper.backingPlugin.getPluginUid() + "_" + ingredientType.getIngredientClass().getSimpleName().toLowerCase(Locale.ROOT));
                registry.register(location, new JEIEntryDefinition<>(EntryType.deferred(location), ingredientType, ingredientHelper, ingredientRenderer));
            }
        };
    }
    
    public static UidContext wrapContext(ComparisonContext context) {
        return context == ComparisonContext.FUZZY ? UidContext.Recipe : UidContext.Ingredient;
    }
    
    private static ISubtypeRegistration wrapSubtypeRegistration(ItemComparatorRegistry registry) {
        return new ISubtypeRegistration() {
            @Override
            public void registerSubtypeInterpreter(@NotNull Item item, @NotNull ISubtypeInterpreter interpreter) {
                registry.register(wrapItemComparator(interpreter), item);
            }
            
            @Override
            public void useNbtForSubtypes(@NotNull Item... items) {
                registry.registerNbt(items);
            }
            
            @Override
            public boolean hasSubtypeInterpreter(@NotNull ItemStack itemStack) {
                throw TODO();
            }
        };
    }
    
    private static ItemComparator wrapItemComparator(ISubtypeInterpreter interpreter) {
        return stack -> interpreter.apply(stack).hashCode();
    }
    
    public static class JEIPluginWrapper implements REIPlugin {
        private final IModPlugin backingPlugin;
        
        private final List<JEIWrappedCategory<?>> categories = new ArrayList<>();
        
        public JEIPluginWrapper(IModPlugin backingPlugin) {
            this.backingPlugin = backingPlugin;
        }
        
        @Override
        public void registerEntryTypes(EntryTypeRegistry registry) {
            backingPlugin.registerIngredients(wrapModIngredientRegistration(this, registry));
        }
        
        @Override
        public void registerItemComparators(ItemComparatorRegistry registry) {
            backingPlugin.registerItemSubtypes(wrapSubtypeRegistration(registry));
        }
        
        @Override
        public void registerCategories(CategoryRegistry registry) {
            this.categories.clear();
            backingPlugin.registerCategories(wrapCategoryRegistration(registry, categories::add));
            backingPlugin.registerRecipeCatalysts(wrapCatalyst(registry));
        }
        
        @Override
        public void registerDisplays(DisplayRegistry registry) {
            backingPlugin.registerRecipes(wrapRecipeRegistration(registry));
            for (JEIWrappedCategory<?> category : categories) {
                if (Recipe.class.isAssignableFrom(category.getRecipeClass())) {
                    registry.registerFiller((Class<Recipe<Container>>) category.getRecipeClass(), ((JEIWrappedCategory<Recipe<Container>>) category)::handlesRecipe,
                            recipe -> new JEIWrappedDisplay(category, recipe));
                }
                registry.registerFiller(JEIWrappedDisplay.class, display -> display.getCategoryIdentifier().equals(category.getIdentifier()), Function.identity());
            }
            backingPlugin.registerAdvanced(wrapAdvancedRegistration(registry));
        }
        
        @Override
        public void registerScreens(ScreenRegistry registry) {
            backingPlugin.registerGuiHandlers(wrapGuiHandlerRegistration(registry));
        }
        
        @Override
        public void postRegister() {
            backingPlugin.onRuntimeAvailable(wrapRuntime());
        }
        
        @Override
        public String getPluginName() {
            return "JEI Plugin [" + backingPlugin.getPluginUid().toString() + "]";
        }
    }
}
