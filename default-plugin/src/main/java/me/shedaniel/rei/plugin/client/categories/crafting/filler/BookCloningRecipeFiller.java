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

package me.shedaniel.rei.plugin.client.categories.crafting.filler;

import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.CollectionUtils;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.shedaniel.rei.plugin.common.displays.crafting.DefaultCustomShapelessDisplay;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.WrittenBookContent;
import net.minecraft.world.item.crafting.BookCloningRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.*;

public class BookCloningRecipeFiller implements CraftingRecipeFiller<BookCloningRecipe> {
    private static final String[] TITLES = new String[]{
            "Adventurer's Dreams", "Adventurer's Diary", "The Lost Journal",
            "The Lost Diary", "The Lost Book", "The Lost Tome", "The Lost Codex",
            "The Last Journal", "The Last Diary", "The Last Book", "The Last Tome",
            "Secrets of the World", "Secrets of the Universe", "Secrets of the Cosmos",
            "Myths of the World", "Myths of the Universe", "Myths of the Cosmos",
            "Old Tales of the World", "Old Tales of the Universe", "Old Tales of the Cosmos",
            "The World of the Legends", "The Universe of the Heroes", "The Cosmos of the Gods",
            "Diary of a Villager", "Diary of a Farmer", "Diary of a Fisherman",
            "Dungeon Journal", "Dungeon Diary", "Dungeon Book", "Dungeon Tome",
            "Dunk Memes", "Top 10 Memes of 2019", "Top 10 Memes of 2020",
            "Plastic Memories", "Kizumonogatari"
    };
    private static final String[] AUTHORS = new String[]{
            "shedaniel", "Steve", "Alex", "Notch", "Herobrine", "God",
            "Santa Claus", "The Easter Bunny", "The Tooth Fairy"
    };
    
    @Override
    public Collection<Display> apply(RecipeHolder<BookCloningRecipe> recipe) {
        List<Display> displays = new ArrayList<>();
        
        for (int i = 1; i <= 8; i++) {
            EntryIngredient.Builder[] inputs = new EntryIngredient.Builder[9];
            for (int j = 0; j < 9; j++) {
                inputs[j] = EntryIngredient.builder();
            }
            EntryIngredient.Builder output = EntryIngredient.builder();
            for (int j = 0; j < 10; j++) {
                ItemStack writtenBook = generateBook();
                ItemStack bookAndQuill = new ItemStack(Items.WRITABLE_BOOK);
                inputs[0].add(EntryStacks.of(writtenBook));
                for (int k = 0; k < i; k++) {
                    inputs[k + 1].add(EntryStacks.of(bookAndQuill));
                }
                ItemStack cloned = writtenBook.copy();
                cloned.update(DataComponents.WRITTEN_BOOK_CONTENT, WrittenBookContent.EMPTY, WrittenBookContent::craftCopy);
                cloned.setCount(i);
                output.add(EntryStacks.of(cloned));
            }
            displays.add(new DefaultCustomShapelessDisplay(
                    CollectionUtils.map(inputs, EntryIngredient.Builder::build),
                    List.of(output.build()),
                    Optional.of(recipe.id().identifier())));
        }
        
        return displays;
    }
    
    private ItemStack generateBook() {
        ItemStack stack = new ItemStack(Items.WRITTEN_BOOK);
        stack.set(DataComponents.WRITTEN_BOOK_CONTENT, new WrittenBookContent(new Filterable<>(TITLES[new Random().nextInt(TITLES.length)], Optional.empty()),
                AUTHORS[new Random().nextInt(AUTHORS.length)], 0, List.of(), true));
        return stack;
    }
    
    @Override
    public Class<BookCloningRecipe> getRecipeClass() {
        return BookCloningRecipe.class;
    }
}
