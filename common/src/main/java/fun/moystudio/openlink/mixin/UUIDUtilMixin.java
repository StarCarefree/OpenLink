package fun.moystudio.openlink.mixin;

import fun.moystudio.openlink.logic.UUIDFixer;
import net.minecraft.core.UUIDUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(UUIDUtil.class)
public abstract class UUIDUtilMixin {
    @Inject(method = "createOfflinePlayerUUID", at = @At("HEAD"), cancellable = true)
    private static void openLink$createOfflinePlayerUUID(String string, CallbackInfoReturnable<UUID> cir) {
        UUID uuid = UUIDFixer.hookEntry(string);
        if (uuid != null) {
            cir.setReturnValue(uuid);
            cir.cancel();
        }
    }
}