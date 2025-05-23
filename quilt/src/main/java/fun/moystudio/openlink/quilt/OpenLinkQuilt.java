package fun.moystudio.openlink.quilt;

import fun.moystudio.openlink.OpenLink;
import fun.moystudio.openlink.fabriclike.OpenLinkFabricLike;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.loader.api.QuiltLoader;
import org.quiltmc.loader.impl.QuiltLoaderImpl;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;

public final class OpenLinkQuilt implements ModInitializer {
    @Override
    public void onInitialize(ModContainer mod) {
        // Run the Fabric-like setup.
        try {
            OpenLinkFabricLike.init(QuiltLoader.getModContainer(OpenLink.MOD_ID).get().metadata().version().raw(),"Quilt", QuiltLoaderImpl.VERSION);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
