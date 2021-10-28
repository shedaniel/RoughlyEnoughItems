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

package mezz.jei.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.language.LanguageInfo;
import net.minecraft.client.resources.language.LanguageManager;

import java.util.Locale;

public class Translator {
    public static String translateToLocal(String key) {
        return I18n.get(key);
    }
    
    public static String translateToLocalFormatted(String key, Object... format) {
        return I18n.get(key, format);
    }
    
    public static String toLowercaseWithLocale(String string) {
        return string.toLowerCase(getLocale());
    }
    
    private static Locale getLocale() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft != null) {
            LanguageManager languageManager = minecraft.getLanguageManager();
            if (languageManager != null) {
                LanguageInfo currentLanguage = languageManager.getSelected();
                if (currentLanguage != null) {
                    return currentLanguage.getJavaLocale();
                }
            }
        }
        return Locale.getDefault();
    }
}