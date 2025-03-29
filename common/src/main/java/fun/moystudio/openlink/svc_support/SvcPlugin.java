package fun.moystudio.openlink.svc_support;

import de.maxhenkel.voicechat.api.*;
import de.maxhenkel.voicechat.api.events.Event;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.events.VoiceHostEvent;
import de.maxhenkel.voicechat.api.events.VoicechatServerStartedEvent;
import fun.moystudio.openlink.OpenLink;
import fun.moystudio.openlink.frpc.Frpc;
import fun.moystudio.openlink.logic.LanConfig;
import net.minecraft.client.Minecraft;

import java.net.SocketAddress;

@ForgeVoicechatPlugin
public class SvcPlugin implements VoicechatPlugin {
    @Override
    public String getPluginId() {
        return OpenLink.MOD_ID;
    }
    @Override
    public void initialize(VoicechatApi api) {
        OpenLink.SVC_SUPPORT=true;
        if(!OpenLink.disabled){
            OpenLink.LOGGER.info("Simple Voice Chat Support Plugin initialized!");
        }
    }
    @Override
    public void registerEvents(EventRegistration registration) {
//        registration.registerEvent(Event.class);
//        registration.registerEvent(VoiceHostEvent.class, event -> {
//            try{
//                while (!OpenLink.disabled){
//                    if(Frpc.runningSvcProxy!=null||Minecraft.getInstance().level==null){
//                        throw new Exception("");
//                    }
//                }
//            } catch (Exception ignored){};
//
//            event.setVoiceHost(LanConfig.cfg.use_frp&&Frpc.runningSvcProxy!=null?Frpc.runningSvcProxy.connectAddress:event.getVoiceHost());
//        });
    }
}
