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

package mezz.jei.config;

import net.minecraft.resources.ResourceLocation;

public final class Constants {
    public static final String TEXTURE_GUI_PATH = "textures/gui/";
    public static final String TEXTURE_GUI_VANILLA = Constants.TEXTURE_GUI_PATH + "gui_vanilla.png";
    
    public static final ResourceLocation RECIPE_GUI_VANILLA = new ResourceLocation("jei", TEXTURE_GUI_VANILLA);
    
    public static final int MAX_TOOLTIP_WIDTH = 150;
    
    public static final ResourceLocation UNIVERSAL_RECIPE_TRANSFER_UID = new ResourceLocation("jei", "universal_recipe_transfer_handler");
    public static final ResourceLocation LOCATION_JEI_GUI_TEXTURE_ATLAS = new ResourceLocation("jei", "textures/atlas/gui.png");
    
    private Constants() {
    }
}