package fun.moystudio.openlink.forge;

import net.minecraftforge.fml.common.Mod;

import fun.moystudio.openlink.OpenLink;

@Mod(OpenLink.MOD_ID)
public final class OpenLinkForge {
    public OpenLinkForge() {
        // Run our common setup.
        OpenLink.init();
    }
}
