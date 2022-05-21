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

package me.shedaniel.rei.jeicompat.imitator;

import java.util.Locale;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.language.LanguageInfo;
import net.minecraft.client.resources.language.LanguageManager;

import javax.annotation.Nullable;

/**
 * The following method is licensed with The MIT License (MIT)
 * Copyright (c) 2014-2015 mezz
 * <p>
 * Full license text can be found in the https://github.com/mezz/JustEnoughItems/blob/1.17/LICENSE.txt
 */
public final class Translator {
    @Nullable
    private static String cachedLocaleCode;
    @Nullable
    private static Locale cachedLocale;
    
    private Translator() {
    }
    
    public static String translateToLocal(String key) {
        return I18n.get(key);
    }
    
    public static String translateToLocalFormatted(String key, Object... format) {
        return I18n.get(key, format);
    }
    
    public static String toLowercaseWithLocale(String string) {
        return string.toLowerCase(getLocale());
    }
    
    @SuppressWarnings("ConstantConditions")
    private static Locale getLocale() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null) {
            return Locale.getDefault();
        }
        LanguageManager languageManager = minecraft.getLanguageManager();
        LanguageInfo selected = languageManager.getSelected();
        return getLocale(selected);
    }
    
    private static Locale getLocale(LanguageInfo currentLanguage) {
        String code = currentLanguage.getCode();
        if (cachedLocale == null || !code.equals(cachedLocaleCode)) {
            cachedLocaleCode = code;
            String[] splitLangCode = code.split("_", 2);
            if (splitLangCode.length == 1) { // Vanilla has some languages without underscores
                cachedLocale = new Locale(code);
            } else {
                cachedLocale = new Locale(splitLangCode[0], splitLangCode[1]);
            }
        }
        return cachedLocale;
    }
}