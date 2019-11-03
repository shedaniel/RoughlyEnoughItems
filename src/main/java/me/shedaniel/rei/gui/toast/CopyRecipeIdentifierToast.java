/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.gui.toast;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.toast.Toast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.util.Identifier;

import javax.annotation.Nullable;

public class CopyRecipeIdentifierToast implements Toast {
    
    protected static final Identifier TOASTS_TEX = new Identifier("roughlyenoughitems", "textures/gui/toasts.png");
    private String title;
    private String subtitle;
    private long startTime;
    
    public CopyRecipeIdentifierToast(String title, @Nullable String subtitleNullable) {
        this.title = title;
        this.subtitle = subtitleNullable;
    }
    
    public static void addToast(String title, @Nullable String subtitleNullable) {
        MinecraftClient.getInstance().getToastManager().add(new CopyRecipeIdentifierToast(title, subtitleNullable));
    }
    
    @Override
    public Visibility draw(ToastManager toastManager, long var2) {
        toastManager.getGame().getTextureManager().bindTexture(TOASTS_TEX);
        RenderSystem.color3f(1.0F, 1.0F, 1.0F);
        toastManager.blit(0, 0, 0, 0, 160, 32);
        if (this.subtitle == null) {
            toastManager.getGame().textRenderer.draw(this.title, 18.0F, 12.0F, 11141120);
        } else {
            toastManager.getGame().textRenderer.draw(this.title, 18.0F, 7.0F, 11141120);
            toastManager.getGame().textRenderer.draw(this.subtitle, 18.0F, 18.0F, -16777216);
        }
        
        return var2 - this.startTime < 5000L ? Visibility.SHOW : Visibility.HIDE;
    }
    
    @Override
    public Object getType() {
        return Type.THIS_IS_SURE_A_TYPE;
    }
    
    public enum Type {
        THIS_IS_SURE_A_TYPE
    }
    
}
