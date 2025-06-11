package fun.moystudio.openlink.forge;

import fun.moystudio.openlink.OpenLink;
import fun.moystudio.openlink.frpcimpl.FrpcManager;
import fun.moystudio.openlink.logic.EventCallbacks;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.Commands;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.versions.forge.ForgeVersion;

@Mod.EventBusSubscriber
@Mod(OpenLink.MOD_ID)
public final class OpenLinkForge {
    public OpenLinkForge() throws Exception {
        // Run our common setup.
        OpenLink.init(ModList.get().getModFileById(OpenLink.MOD_ID).versionString(),"Forge", ForgeVersion.getVersion());
    }

    @SubscribeEvent
    public static void onClientScreenInit(ScreenEvent.Init event){
        EventCallbacks.onScreenInit(event.getScreen().getMinecraft(), event.getScreen());
    }

    @SubscribeEvent
    public static void onClientCommandRegistering(RegisterClientCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("proxyrestart")
                .executes(context -> FrpcManager.getInstance().start(Minecraft.getInstance().getSingleplayerServer().getPort(),"")?1:0));
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event){
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
