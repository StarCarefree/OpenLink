package fun.moystudio.openlink.forge;

import fun.moystudio.openlink.OpenLink;
import fun.moystudio.openlink.frpcimpl.FrpcManager;
import fun.moystudio.openlink.logic.EventCallbacks;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.Commands;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.versions.forge.ForgeVersion;
import org.apache.maven.artifact.versioning.ArtifactVersion;

@Mod.EventBusSubscriber
@Mod(OpenLink.MOD_ID)
public final class OpenLinkForge {
    public OpenLinkForge() throws Exception {
        ArtifactVersion artifactVersion=ModList.get().getModContainerById(OpenLink.MOD_ID).get().getModInfo().getVersion();
        // Run our common setup.
        OpenLink.init(artifactVersion.getMajorVersion()+"."+artifactVersion.getMinorVersion()+"."+artifactVersion.getIncrementalVersion()+(artifactVersion.getQualifier()!=null?"-"+artifactVersion.getQualifier():""),"Forge", ForgeVersion.getVersion());
    }

    @SubscribeEvent
    public static void onClientScreenInit(GuiScreenEvent.InitGuiEvent event){
        EventCallbacks.onScreenInit(event.getGui().getMinecraft(), event.getGui());
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void onClientCommandRegistering(RegisterCommandsEvent event){
        event.getDispatcher().register(Commands.literal("proxyrestart")
                .executes(context -> FrpcManager.getInstance().start(Minecraft.getInstance().getSingleplayerServer().getPort(),"")?1:0));
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        EventCallbacks.onClientTick(Minecraft.getInstance());
    }

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ModEventBusSubscriber {
        @SubscribeEvent
        public static void onFinishLoading(FMLLoadCompleteEvent event) {
            EventCallbacks.onAllModLoadingFinish();
        }
    }
}
