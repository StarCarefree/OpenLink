package fun.moystudio.openlink.fabriclike;

import fun.moystudio.openlink.OpenLink;
import fun.moystudio.openlink.frpc.FrpcManager;
import fun.moystudio.openlink.logic.EventCallbacks;
import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.minecraft.client.Minecraft;

public final class OpenLinkFabricLike {
    public static void init(String version, String loader, String loader_version) throws Exception {
        // Run our common setup.
        OpenLink.init(version,loader,loader_version);
        ClientCommandManager.DISPATCHER.register(ClientCommandManager
                        .literal("proxyrestart")
                        .executes(context -> FrpcManager.getInstance().start(Minecraft.getInstance().getSingleplayerServer().getPort(),"")?1:0));
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight)->{
            EventCallbacks.onScreenInit(client,screen);
        });
        ClientTickEvents.END_CLIENT_TICK.register(EventCallbacks::onClientTick);
        ClientLifecycleEvents.CLIENT_STARTED.register((Minecraft minecraft)->{
            EventCallbacks.onAllModLoadingFinish();
        });
    }
}