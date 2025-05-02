package fun.moystudio.openlink.neoforge;

import fun.moystudio.openlink.OpenLink;
import fun.moystudio.openlink.frpc.FrpcManager;
import fun.moystudio.openlink.logic.EventCallbacks;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.Commands;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.event.TickEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.internal.versions.neoforge.NeoForgeVersion;

@Mod.EventBusSubscriber
@Mod(OpenLink.MOD_ID)
public final class OpenLinkNeoForge {
    public OpenLinkNeoForge() throws Exception {
        OpenLink.init(ModList.get().getModFileById(OpenLink.MOD_ID).versionString(),"NeoForge", NeoForgeVersion.getVersion());
    }

    @SubscribeEvent
    public static void onClientScreenInit(ScreenEvent.Init.Post event){
        EventCallbacks.onScreenInit(event.getScreen().getMinecraft(), event.getScreen());
    }

    @SubscribeEvent
    public static void onClientCommandRegistering(RegisterClientCommandsEvent event){
        event.getDispatcher().register(Commands.literal("proxyrestart")
                .executes(context -> FrpcManager.getInstance().start(Minecraft.getInstance().getSingleplayerServer().getPort(),"")?1:0));
    }

    @SubscribeEvent
    public static void onLevelClear(LevelEvent.Unload event){
        EventCallbacks.onLevelClear();
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event){
        EventCallbacks.onClientTick(Minecraft.getInstance());
    }

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
    public static final class ModEventSubscriber {
        @SubscribeEvent
        public static void onFinishLoading(FMLLoadCompleteEvent event) {
            EventCallbacks.onAllModLoadingFinish();
        }
    }
}
