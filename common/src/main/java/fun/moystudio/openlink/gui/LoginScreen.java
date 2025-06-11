package fun.moystudio.openlink.gui;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import fun.moystudio.openlink.frpcimpl.FrpcManager;
import fun.moystudio.openlink.frpcimpl.OpenFrpFrpcImpl;
import fun.moystudio.openlink.json.JsonResponseWithData;
import fun.moystudio.openlink.json.JsonUserInfo;
import fun.moystudio.openlink.logic.Utils;
import fun.moystudio.openlink.logic.WebBrowser;
import fun.moystudio.openlink.network.LoginGetCodeHttpServer;
import fun.moystudio.openlink.network.Request;
import fun.moystudio.openlink.network.Uris;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;

public class LoginScreen extends Screen {
    public LoginScreen(Screen last) {
        super(Utils.translatableText("gui.openlink.loginscreentitle"));
        lastscreen=last;
    }
    Screen lastscreen;
    MultiLineLabel loginTips;
    EditBox authorization=new EditBox(Minecraft.getInstance().font, this.width / 2 - 200, this.height / 6 * 3, 355, 20, Utils.translatableText("text.openlink.authorization"));
    WebBrowser browser=new WebBrowser(Uris.openidLoginUri.toString());

    @Override
    protected void init() {
        loginTips = MultiLineLabel.create(this.font, Utils.translatableText("text.openlink.logintips"), this.width - 50);
        this.addRenderableWidget(Button.builder(Utils.translatableText("text.openlink.fastlogin"), (button) ->{
            Gson gson = new Gson();
            LoginGetCodeHttpServer codeHttpServer = new LoginGetCodeHttpServer();
            codeHttpServer.start();
            try {
                JsonResponseWithData<String> response = gson.fromJson(Request.POST("https://api.openfrp.net/oauth2/login?redirect_url=http://localhost:"+codeHttpServer.port,Request.DEFAULT_HEADER, "{}").getFirst(),new TypeToken<JsonResponseWithData<String>>(){}.getType());
                new WebBrowser(response.data).openBrowser();
                this.minecraft.keyboardHandler.setClipboard(response.data);
                button.active=false;
                this.minecraft.setScreen(new ConfirmScreenWithLanguageButton(confirmed -> {if(OpenFrpFrpcImpl.Authorization!=null){this.onClose();}}, Utils.translatableText("text.openlink.fastlogin"), Utils.translatableText("text.openlink.fastloginconfirm")));
            } catch (Exception e) {
                e.printStackTrace();
                this.onClose();
            }
        }).bounds(this.width / 2 - 100, this.height / 6 * 2, 200, 20).build());
        authorization.setMaxLength(100);
        authorization.setX(this.width / 2 - 200);
        authorization.setY(this.height/2);
        this.addRenderableWidget(authorization);
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> {
            OpenFrpFrpcImpl.Authorization = authorization.getValue();
            try {
                JsonResponseWithData<JsonUserInfo> response = OpenFrpFrpcImpl.getUserInfo();
                if(response!=null&&response.flag){
                    OpenFrpFrpcImpl.writeSession();
                    this.onClose();
                } else {
                    OpenFrpFrpcImpl.Authorization = null;
                }
            } catch (Exception e) {
                OpenFrpFrpcImpl.Authorization = null;
            }
            OpenFrpFrpcImpl.writeSession();
        }).bounds(this.width / 2 + 160, this.height / 2, 40, 20).build());
        //注册
        this.addRenderableWidget(Button.builder(Utils.translatableText("text.openlink.no_account"), (button) -> browser.openBrowser()).bounds(this.width / 2 - 100, this.height / 6 * 4 , 200, 20).build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int i, int j, float f) {
        super.render(guiGraphics, i, j, f);
        guiGraphics.drawString(this.font, Utils.translatableText("text.openlink.frptip", FrpcManager.getInstance().getCurrentFrpcName()),0, this.height-this.font.lineHeight, 0xffffff);
        loginTips.renderCentered(guiGraphics, this.width / 2, 15, 16, 0xffffff);
    }

    @Override
    public void tick(){
        if(authorization.getValue().isBlank()){
            authorization.setSuggestion(Utils.translatableText("text.openlink.authorization").getString());
        } else {
            authorization.setSuggestion("");
        }
    }

    @Override
    public void onClose(){
        this.minecraft.setScreen(lastscreen);
    }
}
