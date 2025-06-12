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

public class EventCallbacks {
    private static final ResourceLocation OPENLINK_SETTING = Utils.createResourceLocation("openlink", "textures/gui/setting_button.png");
    private static final ResourceLocation OPENLINK_SETTING_HOVERED = Utils.createResourceLocation("openlink", "textures/gui/setting_button_hovered.png");
    public static boolean hasUpdate = false;

    public static void onScreenInit(Minecraft minecraft, Screen screen){
        if(screen instanceof ShareToLanScreen){
            ShareToLanScreen shareToLanScreen = (ShareToLanScreen)screen;
            minecraft.setScreen(new NewShareToLanScreen(((IShareToLanLastScreenAccessor)shareToLanScreen).getLastScreen()));
            return;
        }
        if(screen instanceof TitleScreen){
            FrpcManager.getInstance().stop();
            ((IScreenAccessor)screen).invokeAddButton(new ImageButtonWithHoveredState(screen.width / 2 + 129, screen.height / 4 + 48 + 72 + 12,
                    20, 20, 0, 0, 20, OPENLINK_SETTING, OPENLINK_SETTING_HOVERED, 20, 20, (button) -> minecraft.setScreen(new SettingScreen(null))));

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
            ((IScreenAccessor)screen).invokeAddButton(new Button(0,screen.height-20,150,20,Utils.translatableText("text.openlink.copyip"),button -> {
                Minecraft.getInstance().keyboardHandler.setClipboard(FrpcManager.getInstance().getCurrentIP());
            }));
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
            OpenLink.LOGGER.error("", e);
            hasUpdate = false;
        }
    }
}
