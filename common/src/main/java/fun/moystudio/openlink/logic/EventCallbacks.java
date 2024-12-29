package fun.moystudio.openlink.logic;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.mojang.datafixers.util.Pair;
import fun.moystudio.openlink.OpenLink;
import fun.moystudio.openlink.frpc.Frpc;
import fun.moystudio.openlink.gui.*;
import fun.moystudio.openlink.json.JsonResponseWithData;
import fun.moystudio.openlink.json.JsonTotalAndList;
import fun.moystudio.openlink.json.JsonUserProxy;
import fun.moystudio.openlink.mixin.IScreenAccessor;
import fun.moystudio.openlink.mixin.IShareToLanLastScreenAccessor;
import fun.moystudio.openlink.network.Request;
import fun.moystudio.openlink.network.SSLUtils;
import fun.moystudio.openlink.network.Uris;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.ShareToLanScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Map;

public class EventCallbacks {
    private static final ResourceLocation OPENLINK_SETTING = new ResourceLocation("openlink", "textures/gui/setting_button.png");
    private static final ResourceLocation OPENLINK_SETTING_HOVERED = new ResourceLocation("openlink", "textures/gui/setting_button_hovered.png");

    public static void onScreenInit(Minecraft minecraft, Screen screen){
        if(screen instanceof ShareToLanScreen shareToLanScreen){
            minecraft.setScreen(new NewShareToLanScreen(((IShareToLanLastScreenAccessor)shareToLanScreen).getLastScreen()));
        }
        if(screen instanceof TitleScreen){
            if(OpenLink.disabled) return;
            ((IScreenAccessor)screen).invokeAddRenderableWidget(new ImageButtonWithHoveredState(screen.width / 2 + 129, screen.height / 4 + 48 + 72 + 12,
                    20, 20, 0, 0, 20, OPENLINK_SETTING, OPENLINK_SETTING_HOVERED, 20, 20, (button) -> {
                minecraft.setScreen(new SettingScreen(null));
            }));
        }
    }
    public static void onClientStop(){
        Frpc.stopFrpc();
    }
    public static void onLevelClear(){
        if(OpenLink.disabled) return;
        try{
            Pair<String, Map<String, List<String>>> response= Request.POST(Uris.openFrpAPIUri.toString()+"frp/api/getUserProxies",Request.getHeaderWithAuthorization(Request.DEFAULT_HEADER),"{}");
            Gson gson=new Gson();
            JsonResponseWithData<JsonTotalAndList<JsonUserProxy>> userProxies = gson.fromJson(response.getFirst(), new TypeToken<JsonResponseWithData<JsonTotalAndList<JsonUserProxy>>>(){}.getType());
            for (JsonUserProxy jsonUserProxy : userProxies.data.list) {
                if (jsonUserProxy.proxyName.contains("openlink_mc_")) {
                    try {
                        Request.POST(Uris.openFrpAPIUri.toString() + "frp/api/forceOff", Request.getHeaderWithAuthorization(Request.DEFAULT_HEADER), "{\"proxy_id\":" + String.valueOf(jsonUserProxy.id) + "}");
                        Request.POST(Uris.openFrpAPIUri.toString() + "frp/api/removeProxy", Request.getHeaderWithAuthorization(Request.DEFAULT_HEADER), "{\"proxy_id\":" + String.valueOf(jsonUserProxy.id) + "}");
                        OpenLink.LOGGER.info("Deleted proxy: "+jsonUserProxy.proxyName);
                    } catch (Exception e) {
                        break;
                    }
                }
            }//删除隧道
        } catch (Exception ignored){}
    }

    public static void onClientTick(Minecraft minecraft){
        if(minecraft.screen instanceof TitleScreen){
            if(OpenLink.disabled) return;
            if (SSLUtils.sslIgnored){
                minecraft.setScreen(new ConfirmScreenWithLanguageButton(confirmed->{
                    if(confirmed){
                        SSLUtils.sslIgnored=false;
                        try {
                            Frpc.init();//安装/检查更新frpc版本
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                        Request.readSession();//读取以前的SessionID
                    }
                    else{
                        OpenLink.LOGGER.error("Minecraft closed because of SSL.");
                        minecraft.stop();
                    }
                    minecraft.setScreen(null);
                },new TextComponent("SSL Handshake Error"),new TranslatableComponent("text.openlink.sslignored")));
            }
            if (Frpc.hasUpdate) {
                minecraft.setScreen(new UpdateScreen());
            }
            Frpc.stopFrpc();
        }
    }

}
