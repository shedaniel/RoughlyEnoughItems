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

package me.shedaniel.rei.impl.client.gui.toast;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

@ApiStatus.Internal
public class CopyRecipeIdentifierToast implements Toast {
    
    protected static final ResourceLocation TEXTURE = new ResourceLocation("roughlyenoughitems", "textures/gui/toasts.png");
    private String title;
    private String subtitle;
    private long startTime;
    
    public CopyRecipeIdentifierToast(String title, @Nullable String subtitleNullable) {
        this.title = title;
        this.subtitle = subtitleNullable;
    }
    
    public static void addToast(String title, @Nullable String subtitleNullable) {
        Minecraft.getInstance().getToasts().addToast(new CopyRecipeIdentifierToast(title, subtitleNullable));
    }
    
    @Override
    public Visibility render(GuiGraphics graphics, ToastComponent toastManager, long var2) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        graphics.blit(TEXTURE, 0, 0, 0, 0, 160, 32);
        if (this.subtitle == null) {
            graphics.drawString(toastManager.getMinecraft().font, this.title, 18, 12, 11141120, false);
        } else {
            graphics.drawString(toastManager.getMinecraft().font, this.title, 18, 7, 11141120, false);
            graphics.drawString(toastManager.getMinecraft().font, this.subtitle, 18, 18, -16777216, false);
        }
        
        return var2 - this.startTime < 5000L ? Visibility.SHOW : Visibility.HIDE;
    }
    
    @Override
    public Object getToken() {
        return Type.THIS_IS_SURE_A_TYPE;
    }
    
    public enum Type {
        THIS_IS_SURE_A_TYPE
    }
    
}
