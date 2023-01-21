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

package me.shedaniel.rei.impl.client.search.method;

import com.google.common.collect.Maps;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.search.method.InputMethod;
import me.shedaniel.rei.api.client.search.method.InputMethodRegistry;
import me.shedaniel.rei.api.common.util.CollectionUtils;
import me.shedaniel.rei.impl.client.config.ConfigManagerImpl;
import me.shedaniel.rei.impl.common.InternalLogger;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class InputMethodRegistryImpl implements InputMethodRegistry {
    private final Map<ResourceLocation, InputMethod<?>> inputMethods = Maps.newHashMap();
    
    @Override
    public void add(ResourceLocation id, InputMethod<?> inputMethod) {
        this.inputMethods.put(id, inputMethod);
        InternalLogger.getInstance().debug("Added input method [%s]: %s", id, inputMethod.getName().getString());
    }
    
    @Override
    @Nullable
    public InputMethod<?> get(@Nullable ResourceLocation id) {
        if (id == null) return null;
        return this.inputMethods.get(id);
    }
    
    @Override
    public InputMethod<?> getOrDefault(@Nullable ResourceLocation id) {
        return Objects.requireNonNullElse(this.get(id), DefaultInputMethod.INSTANCE);
    }
    
    @Override
    public Map<ResourceLocation, InputMethod<?>> getAll() {
        return Collections.unmodifiableMap(this.inputMethods);
    }
    
    @Override
    public void startReload() {
        this.inputMethods.clear();
    }
    
    @Override
    public void endReload() {
        InputMethod<?> active = InputMethod.active();
        String languageCode = Minecraft.getInstance().options.languageCode;
        if (!CollectionUtils.anyMatch(active.getMatchingLocales(), locale -> locale.code().equals(languageCode))) {
            InternalLogger.getInstance().error("Reset active input method because the language code {} is not supported by the active input method.", languageCode);
            ConfigManagerImpl.getInstance().getConfig().setInputMethodId(new ResourceLocation("rei:default"));
            return;
        }
        ExecutorService service = Executors.newSingleThreadExecutor();
        active.prepare(service).whenComplete((unused, throwable) -> {
            if (throwable != null) {
                InternalLogger.getInstance().error("Failed to prepare input method", throwable);
                ConfigManagerImpl.getInstance().getConfig().setInputMethodId(new ResourceLocation("rei:default"));
                
                ExecutorService service2 = Executors.newSingleThreadExecutor();
                active.dispose(service2).whenComplete((unused2, throwable2) -> {
                    if (throwable2 != null) {
                        InternalLogger.getInstance().error("Failed to dispose input method", throwable2);
                    }
                }).join();
                service2.shutdown();
            }
        }).join();
        service.shutdown();
        
        InternalLogger.getInstance().debug("Registered %d input methods: %s", inputMethods.size(),
                inputMethods.values().stream().map(inputMethod -> inputMethod.getName().getString()).collect(Collectors.joining(", ")));
    }
    
    @Override
    public void acceptPlugin(REIClientPlugin plugin) {
        plugin.registerInputMethods(this);
    }
}
