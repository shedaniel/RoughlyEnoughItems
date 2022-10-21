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

import dev.architectury.platform.Platform;
import me.shedaniel.rei.api.client.ClientHelper;
import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import me.shedaniel.rei.api.common.util.FormattingUtils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public interface ClientModNameHelperImpl extends ClientHelper {
    Map<String, String> MOD_NAME_CACHE = new HashMap<>() {{
        put("minecraft", "Minecraft");
        put("c", "Common");
        put("global", "Global");
    }};
    
    @Override
    default void appendModIdToTooltips(Tooltip components, String modId) {
        final String modName = ClientHelper.getInstance().getModFromModId(modId);
        Iterator<Tooltip.Entry> iterator = components.entries().iterator();
        while (iterator.hasNext()) {
            Tooltip.Entry entry = iterator.next();
            if (entry.isText() && FormattingUtils.stripFormatting(entry.getAsText().getString()).equalsIgnoreCase(modName)) {
                iterator.remove();
            }
        }
        components.add(ClientHelper.getInstance().getFormattedModFromModId(modId));
    }
    
    @Override
    default String getModFromModId(String modId) {
        if (modId == null)
            return "";
        String any = MOD_NAME_CACHE.getOrDefault(modId, null);
        if (any != null)
            return any;
        if (Platform.isModLoaded(modId)) {
            String modName = Platform.getMod(modId).getName();
            MOD_NAME_CACHE.put(modId, modName);
            return modName;
        }
        return modId;
    }
}
