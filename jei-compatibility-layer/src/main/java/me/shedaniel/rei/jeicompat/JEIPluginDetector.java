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

import com.mojang.blaze3d.vertex.PoseStack;
import me.shedaniel.architectury.hooks.forge.FluidStackHooksForge;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.ClientHelper;
import me.shedaniel.rei.api.ConfigObject;
import me.shedaniel.rei.api.gui.AbstractRenderer;
import me.shedaniel.rei.api.gui.Renderer;
import me.shedaniel.rei.api.ingredient.EntryIngredient;
import me.shedaniel.rei.api.ingredient.EntryStack;
import me.shedaniel.rei.api.ingredient.entry.EntryDefinition;
import me.shedaniel.rei.api.ingredient.entry.EntryTypeRegistry;
import me.shedaniel.rei.api.ingredient.entry.VanillaEntryTypes;
import me.shedaniel.rei.api.ingredient.util.EntryIngredients;
import me.shedaniel.rei.api.ingredient.util.EntryStacks;
import me.shedaniel.rei.api.plugins.BuiltinPlugin;
import me.shedaniel.rei.api.plugins.REIPlugin;
import me.shedaniel.rei.api.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.registry.display.Display;
import me.shedaniel.rei.api.registry.display.DisplayCategory;
import me.shedaniel.rei.api.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.registry.entry.EntryRegistry;
import me.shedaniel.rei.api.util.CollectionUtils;
import me.shedaniel.rei.api.util.ImmutableLiteralText;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.helpers.IModIdHelper;
import mezz.jei.api.helpers.IStackHelper;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.advanced.IRecipeManagerPlugin;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.vanilla.IVanillaRecipeFactory;
import mezz.jei.api.registration.IAdvancedRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.runtime.IIngredientManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class JEIPluginDetector {
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
    
    public static Renderer wrapDrawable(IDrawable drawable) {
        return new AbstractRenderer() {
            @Override
            public void render(PoseStack matrices, Rectangle bounds, int mouseX, int mouseY, float delta) {
                drawable.draw(matrices, bounds.x, bounds.y);
            }
        };
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
    
    public static IAdvancedRegistration wrapAdvancedRegistration() {
        return new IAdvancedRegistration() {
            @Override
            @NotNull
            public IJeiHelpers getJeiHelpers() {
                return wrapJEIHelpers();
            }
            
            @Override
            public void addRecipeManagerPlugin(@NotNull IRecipeManagerPlugin plugin) {
                throw TODO();
            }
        };
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
                    throw new IllegalArgumentException("Registering to non JEI-wrapped category! " + categoryId);
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
    
    public static class JEIPluginWrapper implements REIPlugin {
        private final IModPlugin backingPlugin;
        private final List<JEIWrappedCategory<?>> categories = new ArrayList<>();
        
        public JEIPluginWrapper(IModPlugin backingPlugin) {
            this.backingPlugin = backingPlugin;
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
                    registry.registerRecipes(recipe -> category.getRecipeClass().isInstance(recipe) && ((JEIWrappedCategory<Recipe<Container>>) category).handlesRecipe(recipe),
                            recipe -> new JEIWrappedDisplay(category, recipe));
                }
            }
        }
        
        @Override
        public void preRegister() {
            backingPlugin.registerAdvanced(wrapAdvancedRegistration());
        }
        
        @Override
        public String getPluginName() {
            return "JEI Plugin [" + backingPlugin.getPluginUid().toString() + "]";
        }
    }
}
