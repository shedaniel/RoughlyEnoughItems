package me.shedaniel.rei.mixin.update;

import com.google.common.collect.Lists;
import me.shedaniel.rei.update.UpdateChecker;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Screen;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.text.StringTextComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.shedaniel.rei.update.UpdateChecker.*;

@Mixin(MinecraftClient.class)
public class MixinMinecraftClient {
    
    @Inject(method = "method_1550", at = @At("RETURN"))
    public void method_1550(ClientWorld clientWorld_1, Screen screen_1, CallbackInfo ci) {
        if (UpdateChecker.checkUpdates() && isOutdated()) {
            String t[] = I18n.translate("text.rei.update_outdated", (getCurrentVersion() == null ? "null" : getCurrentVersion().getString()), getLatestForGame(), getChangelog(getLatestForGame()), getUpdatePriority(Lists.newArrayList())).split("\n");
            for(String s : t)
                ((MinecraftClient) (Object) this).player.addChatMessage(new StringTextComponent(s), false);
        }
    }
    
}
