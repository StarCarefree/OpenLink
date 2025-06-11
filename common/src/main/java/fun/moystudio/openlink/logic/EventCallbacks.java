package fun.moystudio.openlink.logic;

import com.mojang.datafixers.util.Pair;
import fun.moystudio.openlink.OpenLink;
import fun.moystudio.openlink.frpcimpl.FrpcManager;
import fun.moystudio.openlink.gui.*;
import fun.moystudio.openlink.mixin.IScreenAccessor;
import fun.moystudio.openlink.mixin.IShareToLanLastScreenAccessor;
import fun.moystudio.openlink.network.SSLUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.ShareToLanScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.resources.ResourceLocation;

import javax.net.ssl.SSLHandshakeException;
import java.io.IOException;
import java.net.SocketException;

public class EventCallbacks {
    private static final ResourceLocation OPENLINK_SETTING = Utils.createResourceLocation("openlink", "widget/setting_button");
    private static final ResourceLocation OPENLINK_SETTING_HOVERED = Utils.createResourceLocation("openlink", "widget/setting_button_hovered");
    public static boolean hasUpdate = false;

    public static void onScreenInit(Minecraft minecraft, Screen screen){
        if(screen instanceof ShareToLanScreen shareToLanScreen){
            minecraft.setScreen(new NewShareToLanScreen(((IShareToLanLastScreenAccessor)shareToLanScreen).getLastScreen()));
            return;
        }
        if(screen instanceof TitleScreen){
            FrpcManager.getInstance().stop();
            int l = OpenLink.LOADER.equals("NeoForge")?screen.height / 4 + 32 + 22:screen.height / 4 + 48;
            ((IScreenAccessor)screen).invokeAddRenderableWidget(new ImageButtonWithHoveredState(screen.width / 2 + 129,  l + 72 + 12,
                    20, 20, OPENLINK_SETTING, OPENLINK_SETTING_HOVERED, (button) -> minecraft.setScreen(new SettingScreen(null))));
        }
        for(Pair<String, Class<?>> classPair:OpenLink.CONFLICT_CLASS){
            if(classPair.getSecond().isInstance(screen)){
                if(ConflictSelectionScreen.canOpen!=null&& ConflictSelectionScreen.canOpen.equals(classPair)){
                    continue;
                }
                minecraft.setScreen(new ConflictSelectionScreen(classPair.getFirst()));
                return;
            }
        }
        if(OpenLink.disabled) return;
        if(screen instanceof PauseScreen && FrpcManager.getInstance().getFrpcProcess() != null){
            ((IScreenAccessor)screen).invokeAddRenderableWidget(Button.builder(Utils.translatableText("text.openlink.copyip"),button -> {
                Minecraft.getInstance().keyboardHandler.setClipboard(FrpcManager.getInstance().getCurrentIP());
            }).bounds(0,screen.height-20,150,20).build());
        }
    }
    public static void onClientStop(){
        FrpcManager.getInstance().stop();
    }

    public static void onClientTick(Minecraft minecraft){
        OpenLink.disabled = !FrpcManager.getInstance().isExecutableFileExist(FrpcManager.getInstance().getCurrentFrpcId());
        if(minecraft.screen instanceof TitleScreen){
            if(OpenLink.disabled) return;
            if (SSLUtils.sslIgnored){
                minecraft.setScreen(new ConfirmScreenWithLanguageButton(confirmed->{
                    if(confirmed){
                        SSLUtils.sslIgnored=false;
                        FrpcManager.getInstance().initialized = false;
                        FrpcManager.getInstance().init();//Reinit
                    }
                    else{
                        OpenLink.disabled = true;
                    }
                    minecraft.setScreen(null);
                }, Utils.literalText("SSL Handshake Error"), Utils.translatableText("text.openlink.sslignored")));
            }
            FrpcManager.getInstance().stop();
            if(hasUpdate) {
                minecraft.setScreen(new UpdateScreen());
            }
        }
    }

    public static void onAllModLoadingFinish() {
        FrpcManager.getInstance().init();
        try{
            hasUpdate = FrpcManager.getInstance().getFrpcImplDetail(FrpcManager.getInstance().getCurrentFrpcId()).getSecond().getSecond();
        } catch (Exception e){
            e.printStackTrace();
            hasUpdate = false;
        }
    }
}
