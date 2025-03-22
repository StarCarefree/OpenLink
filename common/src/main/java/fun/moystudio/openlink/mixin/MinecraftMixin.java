package fun.moystudio.openlink.mixin;

import fun.moystudio.openlink.logic.EventCallbacks;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    @Inject(method = "stop",at = @At("HEAD"))
    public void openLink$stopEvent(CallbackInfo ci){
        EventCallbacks.onClientStop();
    }
    @Inject(method = "allowsMultiplayer",at = @At("RETURN"), cancellable = true)
    public void openLink$allowsMultiplayer(CallbackInfoReturnable<Boolean> cir){
        cir.setReturnValue(true);
        cir.cancel();
    }
}
