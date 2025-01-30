package fun.moystudio.openlink.quilt;

import fun.moystudio.openlink.OpenLink;
import net.fabricmc.api.ModInitializer;
import org.quiltmc.loader.api.QuiltLoader;
import org.quiltmc.loader.impl.QuiltLoaderImpl;


import fun.moystudio.openlink.fabriclike.OpenLinkFabricLike;

public final class OpenLinkQuilt implements ModInitializer {
    @Override
    public void onInitialize() {
        // Run the Fabric-like setup.
        try {
            OpenLinkFabricLike.init(QuiltLoader.getModContainer(OpenLink.MOD_ID).get().metadata().version().raw(),"Quilt", QuiltLoaderImpl.VERSION);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
