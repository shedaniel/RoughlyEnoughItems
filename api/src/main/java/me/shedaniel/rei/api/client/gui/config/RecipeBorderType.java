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

package me.shedaniel.rei.api.client.gui.config;

import me.shedaniel.rei.api.client.gui.widgets.utils.PanelTextures;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.Identifier;

import java.util.Locale;

@Environment(EnvType.CLIENT)
public enum RecipeBorderType implements PanelTextures {
    DEFAULT(Identifier.parse("roughlyenoughitems:widget/panel_default"), Identifier.parse("roughlyenoughitems:widget/panel_default_dark")),
    LIGHTER(Identifier.parse("roughlyenoughitems:widget/panel_lighter"), Identifier.parse("roughlyenoughitems:widget/panel_lighter_dark")),
    NONE(Identifier.parse("roughlyenoughitems:widget/panel_none"), Identifier.parse("roughlyenoughitems:widget/panel_none_dark"));
    
    private final Identifier texture;
    private final Identifier darkTexture;
    
    RecipeBorderType(Identifier texture, Identifier darkTexture) {
        this.texture = texture;
        this.darkTexture = darkTexture;
    }
    
    @Override
    public Identifier texture() {
        return texture;
    }
    
    @Override
    public Identifier darkTexture() {
        return darkTexture;
    }
    
    @Override
    public String toString() {
        return I18n.get("config.rei.value.appearance.recipe_border." + name().toLowerCase(Locale.ROOT));
    }
}
