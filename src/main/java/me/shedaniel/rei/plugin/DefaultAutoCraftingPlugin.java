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

import me.shedaniel.rei.api.ConfigObject;
import me.shedaniel.rei.api.RecipeHelper;
import me.shedaniel.rei.api.plugins.REIPluginV0;
import me.shedaniel.rei.plugin.autocrafting.DefaultCategoryHandler;
import me.shedaniel.rei.plugin.autocrafting.DefaultRecipeBookHandler;
import net.fabricmc.loader.api.SemanticVersion;
import net.fabricmc.loader.util.version.VersionParsingException;
import net.minecraft.util.Identifier;

public class DefaultAutoCraftingPlugin implements REIPluginV0 {
    
    public static final Identifier PLUGIN = new Identifier("roughlyenoughitems", "default_auto_crafting_plugin");
    
    @Override
    public Identifier getPluginIdentifier() {
        return PLUGIN;
    }
    
    @Override
    public SemanticVersion getMinimumVersion() throws VersionParsingException {
        return SemanticVersion.parse("3.0-pre");
    }
    
    @Override
    public void registerOthers(RecipeHelper recipeHelper) {
        if (!ConfigObject.getInstance().isLoadingDefaultPlugin()) {
            return;
        }
        recipeHelper.registerAutoCraftingHandler(new DefaultCategoryHandler());
        recipeHelper.registerAutoCraftingHandler(new DefaultRecipeBookHandler());
    }
}
