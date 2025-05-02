package fun.moystudio.openlink.gui;

import com.mojang.blaze3d.platform.NativeImage;
import fun.moystudio.openlink.mixin.IDynamicTextureAccessor;
import net.minecraft.client.renderer.texture.DynamicTexture;

public class SelfCleaningDynamicTexture extends DynamicTexture {
    public SelfCleaningDynamicTexture(NativeImage nativeImage) {
        super(() -> "openlink:avatar.png", nativeImage);
    }

    @Override
    public void upload(){
        super.upload();
        ((IDynamicTextureAccessor)this).setPixelsAccess(new NativeImage(1,1,true));
    }
}