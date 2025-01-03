package fun.moystudio.openlink.mixin;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.mojang.datafixers.util.Pair;
import fun.moystudio.openlink.OpenLink;
import fun.moystudio.openlink.frpc.Frpc;
import fun.moystudio.openlink.json.JsonResponseWithData;
import fun.moystudio.openlink.json.JsonTotalAndList;
import fun.moystudio.openlink.json.JsonUserProxy;
import fun.moystudio.openlink.logic.EventCallbacks;
import fun.moystudio.openlink.logic.LanConfig;
import fun.moystudio.openlink.logic.OnlineModeTabs;
import fun.moystudio.openlink.network.Request;
import fun.moystudio.openlink.network.Uris;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.social.PlayerSocialManager;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Map;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    @Shadow @Final private PlayerSocialManager playerSocialManager;

    @Shadow @Final private static Logger LOGGER;

    @Inject(method = "stop",at = @At("TAIL"))
    public void stop(CallbackInfo ci){
        EventCallbacks.onClientStop();
    }

    @Inject(method = "prepareForMultiplayer", at = @At("HEAD"), cancellable = true)
    public void prepareForMultiplayer(CallbackInfo ci) {
        if(LanConfig.getAuthMode()!=OnlineModeTabs.ONLINE_MODE){
            LOGGER.warn("Server will run in offline mode!");
            ci.cancel();
        }
    }
}
