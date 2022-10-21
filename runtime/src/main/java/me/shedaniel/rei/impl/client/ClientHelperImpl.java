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

package me.shedaniel.rei.impl.client;

import me.shedaniel.rei.api.client.ClientHelper;
import me.shedaniel.rei.api.client.config.ConfigManager;
import me.shedaniel.rei.api.client.config.ConfigObject;
import me.shedaniel.rei.api.client.gui.config.DisplayScreenType;
import me.shedaniel.rei.api.client.gui.screen.DisplayScreen;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.client.view.ViewSearchBuilder;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.impl.client.gui.DisplayScreenStack;
import me.shedaniel.rei.impl.client.gui.screen.CompositeDisplayViewingScreen;
import me.shedaniel.rei.impl.client.gui.screen.DefaultDisplayViewingScreen;
import me.shedaniel.rei.impl.display.DisplaySpec;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

@ApiStatus.Internal
@Environment(EnvType.CLIENT)
public class ClientHelperImpl extends ClientNetworkHelperImpl implements ClientModNameHelperImpl {
    /**
     * @return the instance of {@link ClientHelperImpl}
     * @see ClientHelper#getInstance()
     */
    @ApiStatus.Internal
    public static ClientHelperImpl getInstance() {
        return (ClientHelperImpl) ClientHelper.getInstance();
    }
    
    @Override
    public boolean openView(ViewSearchBuilder builder) {
        return openView(builder.buildMapInternal(), builder.getPreferredOpenedCategory(), builder.getUsagesFor(), builder.getRecipesFor());
    }
    
    public boolean openView(Map<DisplayCategory<?>, List<DisplaySpec>> map, @Nullable CategoryIdentifier<?> category,
            List<EntryStack<?>> usagesFor, List<EntryStack<?>> recipesFor) {
        if (map.isEmpty()) return false;
        Screen screen;
        if (ConfigObject.getInstance().getRecipeScreenType() == DisplayScreenType.COMPOSITE) {
            screen = new CompositeDisplayViewingScreen(map, category);
        } else if (ConfigObject.getInstance().getRecipeScreenType() == DisplayScreenType.UNSET) {
            ConfigObject.getInstance().setRecipeScreenType(DisplayScreenType.ORIGINAL);
            ConfigManager.getInstance().saveConfig();
            return openView(map, category, usagesFor, recipesFor);
        } else {
            screen = new DefaultDisplayViewingScreen(map, category);
        }
        if (screen instanceof DisplayScreen displayScreen) {
            for (EntryStack<?> stack : usagesFor) {
                displayScreen.addIngredientToNotice(stack);
            }
            for (EntryStack<?> stack : recipesFor) {
                displayScreen.addResultToNotice(stack);
            }
        }
        if (Minecraft.getInstance().screen instanceof DisplayScreen displayScreen) {
            DisplayScreenStack.storeDisplayScreen(displayScreen);
        }
        Minecraft.getInstance().setScreen(screen);
        return true;
    }
}
