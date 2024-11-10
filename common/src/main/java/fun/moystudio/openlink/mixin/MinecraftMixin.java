package fun.moystudio.openlink.mixin;

import fun.moystudio.openlink.frpc.Frpc;
import fun.moystudio.openlink.logic.OnlineModeTabs;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.social.PlayerSocialManager;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin {
    @Shadow @Final private PlayerSocialManager playerSocialManager;

    @Shadow @Final private static Logger LOGGER;

    @Inject(method = "close",at = @At(value = "INVOKE", target = "Lnet/minecraft/server/packs/resources/ReloadableResourceManager;close()V", shift = At.Shift.AFTER))
    public void close(CallbackInfo ci){
        Frpc.stopFrpc();
    }
    @Inject(method = "prepareForMultiplayer",at = @At("TAIL"))
    public void prepareForMultiplayer(CallbackInfo ci) {
        if(Frpc.onlineModeTabs!=OnlineModeTabs.ONLINE_MODE){
            LOGGER.warn("Server will run in offline mode!");
            this.playerSocialManager.stopOnlineMode();
        }
    }
}
