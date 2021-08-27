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

package me.shedaniel.rei.api.common.plugins;

import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.impl.ClientInternals;
import me.shedaniel.rei.impl.Internals;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public interface PluginView<P extends REIPlugin<?>> {
    @Environment(EnvType.CLIENT)
    static PluginView<REIClientPlugin> getClientInstance() {
        return ClientInternals.getPluginManager().view();
    }
    
    static PluginView<REIPlugin<?>> getInstance() {
        return Internals.getPluginManager().view();
    }
    
    static PluginView<REIServerPlugin> getServerInstance() {
        return Internals.getServerPluginManager().view();
    }
    
    /**
     * Registers a REI plugin
     *
     * @param plugin the plugin instance
     */
    void registerPlugin(REIPluginProvider<? extends P> plugin);
    
    default PluginView<P> then(PluginView<? super P> view) {
        return new PluginView<P>() {
            @Override
            public void registerPlugin(REIPluginProvider<? extends P> plugin) {
                PluginView.this.registerPlugin(plugin);
                view.registerPlugin(plugin);
            }
            
            @Override
            public void pre() {
                PluginView.this.pre();
            }
            
            @Override
            public void post() {
                PluginView.this.post();
            }
        };
    }
    
    void pre();
    
    void post();
}
