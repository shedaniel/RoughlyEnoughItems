/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020, 2021 shedaniel
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

package me.shedaniel.rei.jeicompat.wrap;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import dev.architectury.utils.value.Value;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.widgets.Slot;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.client.registry.transfer.TransferHandler;
import me.shedaniel.rei.api.client.registry.transfer.TransferHandlerRegistry;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.type.EntryType;
import me.shedaniel.rei.api.common.transfer.info.MenuInfoRegistry;
import me.shedaniel.rei.api.common.util.CollectionUtils;
import me.shedaniel.rei.jeicompat.JEIPluginDetector;
import me.shedaniel.rei.jeicompat.transfer.JEIRecipeTransferData;
import me.shedaniel.rei.jeicompat.transfer.JEITransferMenuInfo;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IGuiIngredientGroup;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import mezz.jei.api.recipe.transfer.IRecipeTransferInfo;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static me.shedaniel.rei.jeicompat.JEIPluginDetector.wrapCategoryId;
import static me.shedaniel.rei.jeicompat.JEIPluginDetector.wrapRecipe;

public class JEIRecipeTransferRegistration implements IRecipeTransferRegistration {
    private final Consumer<Runnable> post;
    
    public JEIRecipeTransferRegistration(Consumer<Runnable> post) {
        this.post = post;
    }
    
    @Override
    @NotNull
    public IJeiHelpers getJeiHelpers() {
        return JEIJeiHelpers.INSTANCE;
    }
    
    @Override
    @NotNull
    public IRecipeTransferHandlerHelper getTransferHelper() {
        return JEIRecipeTransferHandlerHelper.INSTANCE;
    }
    
    @Override
    public <C extends AbstractContainerMenu> void addRecipeTransferHandler(Class<C> containerClass, ResourceLocation recipeCategoryUid, int recipeSlotStart, int recipeSlotCount, int inventorySlotStart, int inventorySlotCount) {
        addRecipeTransferHandler(new IRecipeTransferInfo<C>() {
            @Override
            public Class<C> getContainerClass() {
                return containerClass;
            }
            
            @Override
            public ResourceLocation getRecipeCategoryUid() {
                return recipeCategoryUid;
            }
            
            @Override
            public boolean canHandle(C container) {
                return getContainerClass().isInstance(container);
            }
            
            @Override
            public List<net.minecraft.world.inventory.Slot> getRecipeSlots(C container) {
                return IntStream.range(recipeSlotStart, recipeSlotStart + recipeSlotCount)
                        .mapToObj(container::getSlot)
                        .collect(Collectors.toList());
            }
            
            @Override
            public List<net.minecraft.world.inventory.Slot> getInventorySlots(C container) {
                return IntStream.range(inventorySlotStart, inventorySlotStart + inventorySlotCount)
                        .mapToObj(container::getSlot)
                        .collect(Collectors.toList());
            }
        });
    }
    
    @Override
    public <C extends AbstractContainerMenu> void addRecipeTransferHandler(IRecipeTransferInfo<C> info) {
        post.accept(() -> {
            MenuInfoRegistry.getInstance().register(wrapCategoryId(info.getRecipeCategoryUid()), info.getContainerClass(),
                    (categoryId, menuClass) -> Optional.of(new JEITransferMenuInfo.Client<>(menu -> new JEIRecipeTransferData<>(info, menu), info)));
        });
    }
    
    @Override
    public void addRecipeTransferHandler(IRecipeTransferHandler<?> recipeTransferHandler, ResourceLocation recipeCategoryUid) {
        TransferHandlerRegistry.getInstance().register(context -> {
            if (recipeTransferHandler.getContainerClass().isInstance(context.getMenu())) {
                Display display = context.getDisplay();
                if (recipeCategoryUid == null || display.getCategoryIdentifier().getIdentifier().equals(recipeCategoryUid)) {
                    IRecipeLayout layout;
                    Value<IDrawable> background = new Value<IDrawable>() {
                        @Override
                        public void accept(IDrawable iDrawable) {
                        }
                        
                        @Override
                        public IDrawable get() {
                            return JEIGuiHelper.INSTANCE.createBlankDrawable(0, 0);
                        }
                    };
                    if (display instanceof JEIWrappedDisplay) {
                        layout = ((JEIWrappedDisplay<Object>) display).getBackingCategory().createLayout((JEIWrappedDisplay<Object>) display, background);
                    } else {
                        DisplayCategory<Display> category = (DisplayCategory<Display>) CategoryRegistry.getInstance().get(display.getCategoryIdentifier()).getCategory();
                        layout = new JEIWrappingRecipeLayout<>(category, background);
                        List<Widget> widgets = category.setupDisplay(display, new Rectangle(0, 0, category.getDisplayWidth(display), category.getDisplayHeight()));
                        addToLayout(layout, widgets);
                    }
                    if (context.isActuallyCrafting()) {
                        context.getMinecraft().setScreen(context.getContainerScreen());
                    }
                    IRecipeTransferError error = ((IRecipeTransferHandler<AbstractContainerMenu>) recipeTransferHandler).transferRecipe(context.getMenu(), wrapRecipe(context.getDisplay()), layout, context.getMinecraft().player, Screen.hasShiftDown(), context.isActuallyCrafting());
                    if (error == null) {
                        return TransferHandler.Result.createSuccessful();
                    } else if (error instanceof JEIRecipeTransferError) {
                        IntArrayList redSlots = ((JEIRecipeTransferError) error).getRedSlots();
                        if (redSlots == null) redSlots = new IntArrayList();
                        return TransferHandler.Result.createFailed(((JEIRecipeTransferError) error).getText(), redSlots);
                    }
                }
            }
            return TransferHandler.Result.createNotApplicable();
        });
    }
    
    private void addToLayout(IRecipeLayout layout, List<Widget> entries) {
        Map<Boolean, List<Multimap<EntryType<?>, EntryStack<?>>>> groups = new HashMap<>();
        for (Widget widget : entries) {
            if (widget instanceof Slot) {
                Multimap<EntryType<?>, EntryStack<?>> group = HashMultimap.create();
                List<EntryStack<?>> ingredient = ((Slot) widget).getEntries();
                for (EntryStack<?> stack : ingredient) {
                    group.put(stack.getType(), stack);
                }
                groups.computeIfAbsent(((Slot) widget).getNoticeMark() != Slot.OUTPUT, $ -> new ArrayList<>()).add(group);
            }
        }
        for (Map.Entry<Boolean, List<Multimap<EntryType<?>, EntryStack<?>>>> entry : groups.entrySet()) {
            entry.getValue().stream().map(Multimap::keys).flatMap(Collection::stream)
                    .distinct().forEach(type -> {
                        IGuiIngredientGroup<Object> group = layout.getIngredientsGroup(type.getDefinition()::getValueType);
                        int[] i = new int[]{getNextId(group.getGuiIngredients().keySet())};
                        entry.getValue().stream().map(map -> map.get(type))
                                .filter(collection -> !collection.isEmpty())
                                .forEach(stacks -> {
                                    group.set(i[0], CollectionUtils.map(stacks, JEIPluginDetector::unwrap));
                                    group.init(i[0], entry.getKey(), 0, 0);
                                    i[0]++;
                                });
                    });
        }
    }
    
    private int getNextId(Set<Integer> keys) {
        for (int i = 0; ; i++) {
            if (!keys.contains(i)) {
                return i;
            }
        }
    }
    
    @Override
    public void addUniversalRecipeTransferHandler(IRecipeTransferHandler<?> recipeTransferHandler) {
        addRecipeTransferHandler(recipeTransferHandler, null);
    }
}
