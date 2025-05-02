package fun.moystudio.openlink.mixin;

import com.mojang.authlib.GameProfile;
import fun.moystudio.openlink.logic.UUIDFixer;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(Player.class)
public abstract class PlayerMixin {
    @Inject(method = "createPlayerUUID(Lcom/mojang/authlib/GameProfile;)Ljava/util/UUID;", at = @At("HEAD"), cancellable = true)
    private static void openLink$createOfflinePlayerUUIDMixin(GameProfile gameProfile, CallbackInfoReturnable<UUID> ci) {
        UUID uuid = UUIDFixer.hookEntry(gameProfile.getName());
        if (uuid != null) {
            ci.setReturnValue(uuid);
            ci.cancel();
        }
    }
}