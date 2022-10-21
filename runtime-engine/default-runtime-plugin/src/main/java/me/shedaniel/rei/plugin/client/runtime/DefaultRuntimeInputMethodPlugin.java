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

package me.shedaniel.rei.plugin.client.runtime;

import dev.architectury.platform.Platform;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.search.method.InputMethodRegistry;
import me.shedaniel.rei.impl.client.search.method.unihan.BomopofoInputMethod;
import me.shedaniel.rei.impl.client.search.method.unihan.JyutpingInputMethod;
import me.shedaniel.rei.impl.client.search.method.unihan.PinyinInputMethod;
import me.shedaniel.rei.impl.client.search.method.unihan.UniHanManager;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class DefaultRuntimeInputMethodPlugin implements REIClientPlugin {
    @Override
    public void registerInputMethods(InputMethodRegistry registry) {
        UniHanManager manager = new UniHanManager(Platform.getConfigFolder().resolve("roughlyenoughitems/unihan.zip"));
        registry.add(new ResourceLocation("rei:pinyin"), new PinyinInputMethod(manager));
        registry.add(new ResourceLocation("rei:jyutping"), new JyutpingInputMethod(manager));
        registry.add(new ResourceLocation("rei:bomopofo"), new BomopofoInputMethod(manager));
    }
}
