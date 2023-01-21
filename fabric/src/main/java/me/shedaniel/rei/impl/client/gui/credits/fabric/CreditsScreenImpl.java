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

package me.shedaniel.rei.impl.client.gui.credits.fabric;

import com.google.common.collect.Lists;
import me.shedaniel.rei.impl.client.gui.credits.CreditsScreen;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.CustomValue;
import net.minecraft.util.Tuple;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class CreditsScreenImpl {
    public static void fillTranslators(Exception[] exception, List<Tuple<String, List<CreditsScreen.TranslatorEntry>>> translators) {
        FabricLoader.getInstance().getModContainer("roughlyenoughitems").ifPresent(rei -> {
            try {
                if (rei.getMetadata().containsCustomValue("rei:translators")) {
                    CustomValue.CvObject jsonObject = rei.getMetadata().getCustomValue("rei:translators").getAsObject();
                    jsonObject.forEach(entry -> {
                        CustomValue value = entry.getValue();
                        List<CreditsScreen.TranslatorEntry> behind = value.getType() == CustomValue.CvType.ARRAY ? Lists.newArrayList(value.getAsArray().iterator()).stream()
                                .map(customValue -> {
                                    if (customValue.getType() == CustomValue.CvType.OBJECT) {
                                        CustomValue.CvObject object = customValue.getAsObject();
                                        // name and proofreader
                                        String name = object.get("name").getAsString();
                                        boolean proofreader = object.containsKey("proofreader") && object.get("proofreader").getAsBoolean();
                                        return new CreditsScreen.TranslatorEntry(name, proofreader);
                                    } else {
                                        return new CreditsScreen.TranslatorEntry(customValue.getAsString(), false);
                                    }
                                })
                                .sorted(Comparator.comparing(CreditsScreen.TranslatorEntry::getName, String::compareToIgnoreCase))
                                .collect(Collectors.toList())
                                : Lists.newArrayList(new CreditsScreen.TranslatorEntry(value.getAsString()));
                        translators.add(new Tuple<>(entry.getKey(), behind));
                    });
                }
                translators.sort(Comparator.comparing(Tuple::getA, String::compareToIgnoreCase));
            } catch (Exception e) {
                exception[0] = e;
                e.printStackTrace();
            }
        });
    }
}
